# ShuffleExternalSorter

```java
package org.apache.spark.shuffle.sort;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
...

/**
 * 一种专门用于基于排序的 shuffle 的外部 sorter。
 * An external sorter that is specialized for sort-based shuffle.
 * <p>
 *
 * 到来的记录被追加到数据页。当所有的记录被插入(或达到当前线程的shuffle内存的极限值)时，
 *    根据它们的分区id，使用 ShuffleInMemorySorter 排序内存中的记录。
 * 然后排序后的记录被写入一个单个文件中(如果溢写的话，可以有多个文件)。
 * 输出文件的格式和SortShuffleWriter写的最终的输出文件相同：每个输出分区的记录作为一个序列化的、压缩的流来写入，这个流可用使用一个新的解压缩和反序列化的流读取。，
 *
 * Incoming records are appended to data pages. When all records have been inserted (or when the current thread's shuffle memory limit is reached), the in-memory records are sorted according to their partition ids (using a {@link ShuffleInMemorySorter}). The sorted records are then written to a single output file (or multiple files, if we've spilled). The format of the output  files is the same as the format of the final output file written by {@link org.apache.spark.shuffle.sort.SortShuffleWriter}: each output partition's records are written as a single serialized, compressed stream that can be read with a new decompression and deserialization stream.
 * <p>
 *
 * 和ExternalSorter不同，这个sorter不会合并它的溢写文件。
 * 而是在UnsafeShuffleWriter中，使用专门的合并过程执行合并，避免额外的序列化/反序列化。
 *
 * Unlike {@link org.apache.spark.util.collection.ExternalSorter}, this sorter does not merge its spill files. Instead, this merging is performed in {@link UnsafeShuffleWriter}, which uses a specialized merge procedure that avoids extra serialization/deserialization.
 */

//MemoryConsumer：支持溢写的 TaskMemoryManager 的内存消费者。
final class ShuffleExternalSorter extends MemoryConsumer {

  private static final Logger logger = LoggerFactory.getLogger(ShuffleExternalSorter.class);

  @VisibleForTesting
  static final int DISK_WRITE_BUFFER_SIZE = 1024 * 1024;

  private final int numPartitions;
  private final TaskMemoryManager taskMemoryManager; //管理一个独立任务分配的内存。
  private final BlockManager blockManager;
  private final TaskContext taskContext;
  private final ShuffleWriteMetricsReporter writeMetrics;

  /**
   * 当在内存中有很多元素时，强制sorter溢写。
   * Force this sorter to spill when there are this many elements in memory.
   */
  private final int numElementsForSpillThreshold;

  /** The buffer size to use when writing spills using DiskBlockObjectWriter */
  //当使用DiskBlockObjectWriter进行溢写时，使用的缓存大小。
  private final int fileBufferSizeBytes;

  /** The buffer size to use when writing the sorted records to an on-disk file */
  //当将排序了的记录写入一个磁盘文件时，使用的缓存大小。
  private final int diskWriteBufferSize;

  /**
   * Memory pages that hold the records being sorted. The pages in this list are freed when spilling, although in principle we could recycle these pages across spills (on the other hand, this might not be necessary if we maintained a pool of re-usable pages in the TaskMemoryManager itself).
   */

  // 存储正在排序的记录的内存页。
  //这个list中的页面在溢写时被释放，
  //尽管原则上我们可以跨spills回收这些页面(另一方面，如果我们在TaskMemoryManager中维护一个可重用页面池，这可能没有必要)。

  //MemoryBlock：连续的内存块 【一个page是一个MemoryBlock】
  private final LinkedList<MemoryBlock> allocatedPages = new LinkedList<>();

  //SpillInfo：ShuffleExternalSorter写入的数据的一个块的元数据
  private final LinkedList<SpillInfo> spills = new LinkedList<>();

  /** Peak memory used by this sorter so far, in bytes. **/
  //这个sorter到目前为止使用的峰值内存，字节形式。  
  private long peakMemoryUsedBytes;

  // These variables are reset after spilling:
  //溢写完成后，这些变量被重置
  @Nullable private ShuffleInMemorySorter inMemSorter;
  @Nullable private MemoryBlock currentPage = null;
  private long pageCursor = -1;

  ShuffleExternalSorter(
      TaskMemoryManager memoryManager,
      BlockManager blockManager,
      TaskContext taskContext,
      int initialSize,
      int numPartitions,
      SparkConf conf,
      ShuffleWriteMetricsReporter writeMetrics) {
    super(memoryManager,
      (int) Math.min(PackedRecordPointer.MAXIMUM_PAGE_SIZE_BYTES, memoryManager.pageSizeBytes()),
      memoryManager.getTungstenMemoryMode());
    this.taskMemoryManager = memoryManager;
    this.blockManager = blockManager;
    this.taskContext = taskContext;
    this.numPartitions = numPartitions;
    // Use getSizeAsKb (not bytes) to maintain backwards compatibility if no units are provided
    this.fileBufferSizeBytes =
        (int) (long) conf.get(package$.MODULE$.SHUFFLE_FILE_BUFFER_SIZE()) * 1024;
    this.numElementsForSpillThreshold =
        (int) conf.get(package$.MODULE$.SHUFFLE_SPILL_NUM_ELEMENTS_FORCE_SPILL_THRESHOLD());
    this.writeMetrics = writeMetrics;
    this.inMemSorter = new ShuffleInMemorySorter(
      this, initialSize, (boolean) conf.get(package$.MODULE$.SHUFFLE_SORT_USE_RADIXSORT()));
    this.peakMemoryUsedBytes = getMemoryUsage();
    this.diskWriteBufferSize =
        (int) (long) conf.get(package$.MODULE$.SHUFFLE_DISK_WRITE_BUFFER_SIZE());
  }

  /**
   * 先排序内存中的记录，再将排序后的记录写入到一个磁盘上的文件。
   * 这个方法不会释放sort data structures
   *
   * Sorts the in-memory records and writes the sorted records to an on-disk file.
   * This method does not free the sort data structures.
   *
   * @param isLastFile if true, this indicates that we're writing the final output file and that the
   *                   bytes written should be counted towards shuffle spill metrics rather than
   *                   shuffle write metrics.
   * 如果是true，表明我们正在写最后的输出文件，
   *     写入的字节应该计入 shuffle spill metrics，而不是 shuffle write metrics.
   */
  private void writeSortedFile(boolean isLastFile) {

    // This call performs the actual sort.
    // 此步是真正执行了排序 （基数排序和tim排序）
    final ShuffleInMemorySorter.ShuffleSorterIterator sortedRecords =
      inMemSorter.getSortedIterator();

    // If there are no sorted records, so we don't need to create an empty spill file.
    //如果没有了排序了的记录，就不会再创建一个空的溢写文件。
    if (!sortedRecords.hasNext()) {
      return;
    }

    //ShuffleWriteMetricsReporter：对每个shuffle，报告shuffle写入指标
    final ShuffleWriteMetricsReporter writeMetricsToUse;

    if (isLastFile) {
      // We're writing the final non-spill file, so we _do_ want to count this as shuffle bytes.
      //我们正在写最后的非溢写文件，所以我们想把这个计数为shuffle字节。
      writeMetricsToUse = writeMetrics;
    } else {
      // We're spilling, so bytes written should be counted towards spill rather than write.
      // Create a dummy WriteMetrics object to absorb these metrics, since we don't want to count
      // them towards shuffle bytes written.
      //我们正在溢写，应该把写入的字节计数为spill，而不是write
      //创建一个虚拟的WriteMetrics对象来吸收这些度量，因为我们不想将它们计算到写入的shuffle字节中。
      writeMetricsToUse = new ShuffleWriteMetrics();
    }

    // Small writes to DiskBlockObjectWriter will be fairly inefficient. Since there doesn't seem to
    // be an API to directly transfer bytes from managed memory to the disk writer, we buffer
    // data through a byte array. This array does not need to be large enough to hold a single
    // record;

    // 对DiskBlockObjectWriter的小规模写操作将是相当低效的。
    // 因为似乎没有API可以直接将字节从管理的内存中传输到磁盘写入器，
    // 所以我们通过字节数组缓冲数据。这个数组不需要足够大，才能保存一条记录;
    final byte[] writeBuffer = new byte[diskWriteBufferSize];

    // Because this output will be read during shuffle, its compression codec must be controlled by
    // spark.shuffle.compress instead of spark.shuffle.spill.compress, so we need to use
    // createTempShuffleBlock here; see SPARK-3426 for more details.

    // 因为这个输出在shuffle期间会被读取，它的压缩格式必须使用 spark.shuffle.compress 控制，
    // 而不是 spark.shuffle.spill.compress，所有这里需要使用 createTempShuffleBlock

    //createTempShuffleBlock：
   //生成一个唯一的blockId，和适合用来存储shuffle的中间结果的文件。这个blockId对应于这个文件。
    final Tuple2<TempShuffleBlockId, File> spilledFileInfo =
      blockManager.diskBlockManager().createTempShuffleBlock();
    final File file = spilledFileInfo._2();
    final TempShuffleBlockId blockId = spilledFileInfo._1();、
    //创建这个blockId对应的元数据。
    //SpillInfo：ShuffleExternalSorter写入的数据的一个块的元数据
    final SpillInfo spillInfo = new SpillInfo(numPartitions, file, blockId);

    // Unfortunately, we need a serializer instance in order to construct a DiskBlockObjectWriter.
    // Our write path doesn't actually use this serializer (since we end up calling the `write()`
    // OutputStream methods), but DiskBlockObjectWriter still calls some methods on it. To work
    // around this, we pass a dummy no-op serializer.
    //为了构造一个DiskBlockObjectWriter，需要一个序列化器实例。
    //我们的写路径实际并没有使用这个序列化器(因为我们通过调用`write()`输出流方法来结束)，
    //但DiskBlockObjectWriter仍会在序列化器上调用溢写方法

    //DummySerializerInstance：虚拟的，无操作的序列化器
    final SerializerInstance ser = DummySerializerInstance.INSTANCE;

    //当前分区编号
    int currentPartition = -1;
    //根据一个偏移量和一个长度，定义一个文件的片段的引用。
    final FileSegment committedSegment;
    //DiskBlockObjectWriter：将JVM对象直接写入磁盘文件的类
    try (DiskBlockObjectWriter writer =
        blockManager.getDiskWriter(blockId, file, ser, fileBufferSizeBytes, writeMetricsToUse)) {

      final int uaoSize = UnsafeAlignedOffset.getUaoSize();
      while (sortedRecords.hasNext()) {
        sortedRecords.loadNext(); //位置+1
        final int partition = sortedRecords.packedRecordPointer.getPartitionId();
        assert (partition >= currentPartition);
        //取到的分区id不是当前分区
        if (partition != currentPartition) {
          // Switch to the new partition
          if (currentPartition != -1) {
          	//commitAndGet：刷新部分写操作，并将它们作为一个原子块提交。
          	//返回 这次调用中提交的具有先前偏移量和长度的文件段。
            final FileSegment fileSegment = writer.commitAndGet();
            //当前分区的分区长度
            spillInfo.partitionLengths[currentPartition] = fileSegment.length();
          }
          currentPartition = partition;
        }

        final long recordPointer = sortedRecords.packedRecordPointer.getRecordPointer();

        //获得和指定地址相关的page
        final Object recordPage = taskMemoryManager.getPage(recordPointer);
        //获得和指定地址相关的偏移量
        final long recordOffsetInPage = taskMemoryManager.getOffsetInPage(recordPointer);
        int dataRemaining = UnsafeAlignedOffset.getSize(recordPage, recordOffsetInPage);
        long recordReadPosition = recordOffsetInPage + uaoSize; // skip over record length 跳过记录的长度
        while (dataRemaining > 0) {
          final int toTransfer = Math.min(diskWriteBufferSize, dataRemaining);

//public static void copyMemory(Object src, long srcOffset, Object dst, long dstOffset, long length)

          //复制到writeBuffer缓存里
          Platform.copyMemory(
            recordPage, recordReadPosition, writeBuffer, Platform.BYTE_ARRAY_OFFSET, toTransfer);
          writer.write(writeBuffer, 0, toTransfer);
          recordReadPosition += toTransfer;
          dataRemaining -= toTransfer;
        }
        //通知writer已经用OutputStream#write写入了一个值为字节的记录。
        writer.recordWritten();
      }
      //刷新部分写操作，并将它们作为一个原子块提交。
      committedSegment = writer.commitAndGet();
    }
    // If `writeSortedFile()` was called from `closeAndGetSpills()` and no records were inserted,
    // then the file might be empty. Note that it might be better to avoid calling
    // writeSortedFile() in that case.

    //如果从 `closeAndGetSpills()` 中调用 `writeSortedFile()`，且没有记录插入，
    //那么文件可能为空。
    //此时，避免调用`writeSortedFile()`可能更好。
    if (currentPartition != -1) {
      spillInfo.partitionLengths[currentPartition] = committedSegment.length();
      spills.add(spillInfo);
    }

    if (!isLastFile) {  // i.e. this is a spill file
      // The current semantics of `shuffleRecordsWritten` seem to be that it's updated when records are written to disk, not when they enter the shuffle sorting code. DiskBlockObjectWriter relies on its `recordWritten()` method being called in order to trigger periodic updates to `shuffleBytesWritten`. If we were to remove the `recordWritten()` call and increment that counter at a higher-level, then the in-progress metrics for records written and bytes written would get out of sync.
      //
      // When writing the last file, we pass `writeMetrics` directly to the DiskBlockObjectWriter; in all other cases, we pass in a dummy write metrics to capture metrics, then copy those metrics to the true write metrics here. The reason for performing this copying is so that we can avoid reporting spilled bytes as shuffle write bytes.
      //
      // Note that we intentionally ignore the value of `writeMetricsToUse.shuffleWriteTime()`. Consistent with ExternalSorter, we do not count this IO towards shuffle write time. SPARK-3577 tracks the spill time separately.

      // This is guaranteed to be a ShuffleWriteMetrics based on the if check in the beginning of this method.
      writeMetrics.incRecordsWritten(
        ((ShuffleWriteMetrics)writeMetricsToUse).recordsWritten());
      taskContext.taskMetrics().incDiskBytesSpilled(
        ((ShuffleWriteMetrics)writeMetricsToUse).bytesWritten());
    }
  }

  /**
   * 排序并溢写当前记录，以缓解内存压力
   * Sort and spill the current records in response to memory pressure.
   */
  @Override
  public long spill(long size, MemoryConsumer trigger) throws IOException {
    if (trigger != this || inMemSorter == null || inMemSorter.numRecords() == 0) {
      return 0L;
    }

    logger.info("Thread {} spilling sort data of {} to disk ({} {} so far)",
      Thread.currentThread().getId(),
      Utils.bytesToString(getMemoryUsage()),
      spills.size(),
      spills.size() > 1 ? " times" : " time");

    writeSortedFile(false);
    final long spillSize = freeMemory();
    inMemSorter.reset();   //重置inMemSorter
    // Reset the in-memory sorter's pointer array only after freeing up the memory pages holding the records. Otherwise, if the task is over allocated memory, then without freeing the memory pages, we might not be able to get memory for the pointer array.
    //仅在释放内存页持有的记录后，重置内存sorter的指针数组。
    //反之，如果为任务超额分配了内存，那么如果不释放内存页，我们可能无法为指针数组获得内存。
    taskContext.taskMetrics().incMemoryBytesSpilled(spillSize);
    return spillSize;
  }

  private long getMemoryUsage() {
    long totalPageSize = 0;
    for (MemoryBlock page : allocatedPages) {
      totalPageSize += page.size();
    }
    // inMemSorter：ShuffleInMemorySorter实例
    // 如果不空，取这个Sorter使用的内容加上数据使用的内存
    return ((inMemSorter == null) ? 0 : inMemSorter.getMemoryUsage()) + totalPageSize;
  }

  //更新使用过的峰值内存
  private void updatePeakMemoryUsed() {
    long mem = getMemoryUsage();
    if (mem > peakMemoryUsedBytes) {
      peakMemoryUsedBytes = mem;
    }
  }

  /**
   * 返回到目前为止使用的峰值内存，以字节为单位。
   *
   * Return the peak memory used so far, in bytes.
   */
  long getPeakMemoryUsedBytes() {
    updatePeakMemoryUsed();
    return peakMemoryUsedBytes;
  }

  //返回释放的MemoryBlock内存量
  private long freeMemory() {
    updatePeakMemoryUsed();
    long memoryFreed = 0;
    for (MemoryBlock block : allocatedPages) {
      memoryFreed += block.size();
      freePage(block);
    }
    allocatedPages.clear();
    currentPage = null;
    pageCursor = 0;
    return memoryFreed;
  }

  /**
   * 强制删除所有内存和溢写文件；由shuffle 错误处理程序调用。
   * Force all memory and spill files to be deleted; called by shuffle  error-handling code.
   */
  public void cleanupResources() {
//释放内存页中的MemoryBlock(Memory pages that hold the records being sorted. )
    freeMemory(); 
    if (inMemSorter != null) {
//释放一个LongArray占用的内存
//(这个数组是PackedRecordPointer编码的记录指针和分区ids的数组。)
      inMemSorter.free();  
      inMemSorter = null;
    }
    for (SpillInfo spill : spills) {
      if (spill.file.exists() && !spill.file.delete()) {
        logger.error("Unable to delete spill file {}", spill.file.getPath());
      }
    }
  }

  /**
   * 检查是否有足够的空间，来插入额外的记录到sort pointer数组，
   * 并在要求额外空间的情况下，增长数组。
   * 如果不能获得要求的空间，内存中的数据就被溢写到磁盘。
   *
   * Checks whether there is enough space to insert an additional record in to the sort pointer array and grows the array if additional space is required. If the required space cannot be obtained, then the in-memory data will be spilled to disk.
   */
  private void growPointerArrayIfNecessary() throws IOException {
    assert(inMemSorter != null);
    //如果没有足够的额外空间了
    if (!inMemSorter.hasSpaceForAnotherRecord()) {
      long used = inMemSorter.getMemoryUsage();
      LongArray array;
      try {
        // could trigger spilling 可以触发溢写
        //分配一个新数组
        array = allocateArray(used / 8 * 2);
      } catch (TooLargePageException e) {
        // The pointer array is too big to fix in a single page, spill.
        // 指针数组太大，以至于不能放在一个页中，就溢写。
        spill();
        return;
      } catch (SparkOutOfMemoryError e) {
        // should have trigger spilling  应该触发溢写
        if (!inMemSorter.hasSpaceForAnotherRecord()) {
          logger.error("Unable to grow the pointer array");
          throw e;
        }
        return;
      }
      // check if spilling is triggered or not
      // 检查是否触发了溢写 ？？？
      if (inMemSorter.hasSpaceForAnotherRecord()) {
        freeArray(array); 
      } else {
        inMemSorter.expandPointerArray(array);
      }
    }
  }

  /**
   * 为了插入额外的记录，分配更多的内存。
   * 这将向memory manager请求额外的内存，如果不能获得，就溢写。
   *
   * Allocates more memory in order to insert an additional record. This will request additional memory from the memory manager and spill if the requested memory can not be obtained.
   *
   * @param required the required space in the data page, in bytes, including space for storing the record size. This must be less than or equal to the page size (records that exceed the page size are handled via a different code path which uses special overflow pages).
   * 数据页中要求的空间，字节，包括了存储记录大小的空间。
   * 这必须小于等于页大小(超出页大小的记录通过一个不同的代码(使用特殊的溢出页)来处理)
   */
  private void acquireNewPageIfNecessary(int required) {

    // 当前页为null，
    // 或页的偏移量加上要求的空间 大于 页的基础偏移量加上页的大小，
    // 就要分配页了。
    if (currentPage == null ||
      pageCursor + required > currentPage.getBaseOffset() + currentPage.size() ) {
      // TODO: try to find space in previous pages
      currentPage = allocatePage(required);
      pageCursor = currentPage.getBaseOffset();
      allocatedPages.add(currentPage);
    }
  }

  /**
   * 将一条记录写入到 shuffle sorter
   * Write a record to the shuffle sorter.
   */
  public void insertRecord(Object recordBase, long recordOffset, int length, int partitionId)
    throws IOException {

    // for tests
    assert(inMemSorter != null);
    if (inMemSorter.numRecords() >= numElementsForSpillThreshold) {
      logger.info("Spilling data because number of spilledRecords crossed the threshold " +
        numElementsForSpillThreshold);
      spill();
    }

    growPointerArrayIfNecessary();
    final int uaoSize = UnsafeAlignedOffset.getUaoSize();
    // Need 4 or 8 bytes to store the record length.
    final int required = length + uaoSize;
    acquireNewPageIfNecessary(required);

    assert(currentPage != null);
    final Object base = currentPage.getBaseObject();
    final long recordAddress = taskMemoryManager.encodePageNumberAndOffset(currentPage, pageCursor);
    UnsafeAlignedOffset.putSize(base, pageCursor, length);
    pageCursor += uaoSize;
    Platform.copyMemory(recordBase, recordOffset, base, pageCursor, length);
    pageCursor += length;
    inMemSorter.insertRecord(recordAddress, partitionId);
  }

  /**
   * 关闭sorter，使所有缓冲的数据被排序，并写入磁盘。
   *
   * Close the sorter, causing any buffered data to be sorted and written out to disk.
   *
   * @return metadata for the spill files written by this sorter. If no records were ever inserted into this sorter, then this will return an empty array.
   *  返回这个sorter写的溢写文件的元数据。如果没有记录插入到这个sorter，那么返回空数组
   */
  public SpillInfo[] closeAndGetSpills() throws IOException {
    if (inMemSorter != null) {
      // Do not count the final file towards the spill count.
     //先排序内存中的记录，再将排序后的记录写入到一个磁盘上的文件。
      writeSortedFile(true);
      //释放块占用的内存
      freeMemory();
      //释放inMemSorter占用的内存
      inMemSorter.free();
      inMemSorter = null;
    }
    return spills.toArray(new SpillInfo[spills.size()]);
  }

}

```