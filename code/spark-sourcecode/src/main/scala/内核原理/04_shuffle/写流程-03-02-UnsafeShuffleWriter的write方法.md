

### 2.2.2 UnsafeShuffleWriter.write()

UnsafeShuffleWriter.java

```java
public class UnsafeShuffleWriter<K, V> extends ShuffleWriter<K, V> {
  // key:partition  value:TaskContext
  public void write(scala.collection.Iterator<Product2<K, V>> records) throws IOException {
    // Keep track of success so we know if we encountered an exception
    // We do this rather than a standard try/catch/re-throw to handle
    // generic throwables.
    boolean success = false;
    try {
        // 遍历每条记录，将这条记录的地址和分区id编码成一个指针，放到 ShuffleInMemorySorter 中的一个 LongArray 的指定位置上
      while (records.hasNext()) {
        insertRecordIntoSorter(records.next());
      }
      closeAndWriteOutput();
      success = true;
    } finally {
      //....
    }
  }
}
```

UnsafeShuffleWriter.java

```java
public class UnsafeShuffleWriter<K, V> extends ShuffleWriter<K, V> {

    private MyByteArrayOutputStream serBuffer;
    private SerializationStream serOutputStream;
    @Nullable private ShuffleExternalSorter sorter;
    /*
            1. 遍历每条数据，接下来对每条数据操作
            2. 将这条数据序列化后，写入字节数组输出流（缓存）中
            3. 分配存放数据的内存空间，如果空间不足，就会将之间的数据排序后，写入文件
            4. 在复制这条数据前，对memory page的当前偏移量进行编码
            5. 将这条数据从字节数组输出流复制到 memory page
            6. 将这条数据的地址和分区id编码成一个指针，放到一个 LongArray 的指定位置上 
            7. 此时，就可以从 LongArray 中找到这条数据
     */
    // key:partition  value:TaskContext
    void insertRecordIntoSorter(Product2<K, V> record) throws IOException {
        assert (sorter != null);
        final K key = record._1();
        // 取到分区id
        final int partitionId = partitioner.getPartition(key);
        serBuffer.reset();
        // 将 key value 依次写到序列化流，最终进入到 serBuffer 里
        serOutputStream.writeKey(key, OBJECT_CLASS_TAG);
        serOutputStream.writeValue(record._2(), OBJECT_CLASS_TAG);
        serOutputStream.flush();

        final int serializedRecordSize = serBuffer.size();
        assert (serializedRecordSize > 0);
        /*
           serBuffer.getBuf(): 取到存储数据的缓存区     
         */
        // 将一个记录的地址和分区id编码成一个指针，放到 ShuffleInMemorySorter 中的一个 LongArray 的指定位置上
        sorter.insertRecord(
                serBuffer.getBuf(), Platform.BYTE_ARRAY_OFFSET, serializedRecordSize, partitionId);
    }
}
```
ShuffleExternalSorter.java

```java
final class ShuffleExternalSorter extends MemoryConsumer implements ShuffleChecksumSupport {

    // 2.2.2.1 节
    @Nullable private ShuffleInMemorySorter inMemSorter;
    
    /**
     * Write a record to the shuffle sorter.  将一个记录的地址和分区id编码成一个指针，放到 ShuffleInMemorySorter 中的一个 LongArray 的指定位置上
     */
    public void insertRecord(Object recordBase, long recordOffset, int length, int partitionId)
            throws IOException {
        
        // for tests
        assert (inMemSorter != null);
        if (inMemSorter.numRecords() >= numElementsForSpillThreshold) {
            logger.info("Spilling data because number of spilledRecords crossed the threshold " +
                    numElementsForSpillThreshold);
            spill();
        }
        // 2.2.2.2 节  【扩大排序指针数组的大小，也会分配对应的page，存在溢写的可能】
        // 检查是否有足够的空间向 排序指针数组 中插入额外的记录，如果需要额外的空间，则扩大数组 【ShuffleExternalSorter类中全局的LongArray】。
        // 如果无法获得所需的空间，那么内存中的数据将溢出到磁盘
        growPointerArrayIfNecessary();
        final int uaoSize = UnsafeAlignedOffset.getUaoSize();
        // Need 4 or 8 bytes to store the record length.
        final int required = length + uaoSize;
        // 【分配更多的内存，为这个4 或 8个字节】 分配更多的内存，以插入额外的记录。这将从内存管理器请求额外的内存，如果无法获得所请求的内存，则溢出。
        acquireNewPageIfNecessary(required);

        assert (currentPage != null);
        final Object base = currentPage.getBaseObject();
        // 【编码当前page中的当前偏移量】 。只要相应的page没有被释放，这个地址就会一直有效
        final long recordAddress = taskMemoryManager.encodePageNumberAndOffset(currentPage, pageCursor);
        UnsafeAlignedOffset.putSize(base, pageCursor, length);
        // 当前page偏移量往前移 （当前page又被占用了一部分）
        pageCursor += uaoSize;
        // 2.2.2.3 节 把记录复制到当前page中  源->目标
        Platform.copyMemory(recordBase, recordOffset, base, pageCursor, length);
        // 当前page偏移量往前移 （当前page又被占用了一部分）
        pageCursor += length;
        // 2.2.2.4 节  将一个记录的地址和分区id编码成一个指针，放到一个 LongArray 的指定位置上 【此时，就可以从 LongArray 中找到记录】
        inMemSorter.insertRecord(recordAddress, partitionId);
    }
}
```

#### 2.2.2.1 ShuffleInMemorySorter

ShuffleInMemorySorter.java

```java
final class ShuffleInMemorySorter {

    private final MemoryConsumer consumer;
    
    /**
     * An array of record pointers and partition ids that have been encoded by
     * {@link PackedRecordPointer}. The sort operates on this array instead of directly manipulating
     * records.
     * 记录指针和分区id的数组
     * 在这个数组上执行排序操作，而不是直接操作记录。
     * 
     * Only part of the array will be used to store the pointers, the rest part is preserved as
     * temporary buffer for sorting.
     * 数组的一部分用来存储指针，剩余部分为排序用作临时缓存
     */
    private LongArray array;

    /**
     * The position in the pointer array where new records can be inserted.
     * 新记录被插入到指针数组的位置
     */
    private int pos = 0;

    /**
     * How many records could be inserted, because part of the array should be left for sorting.
     * 多少条记录可以被插入，因为数组的一部分要用来排序
     */
    private int usableCapacity = 0;

    // 新记录插入到的位置小于可以插入的记录数
    public boolean hasSpaceForAnotherRecord() {
        return pos < usableCapacity;
    }

    public long getMemoryUsage() {
        return array.size() * 8;
    }

    // 所以，在从 LongArray 中取数据的时候，会对数据排序
    /**
     * Return an iterator over record pointers in sorted order.
     */
    public ShuffleSorterIterator getSortedIterator() {
        int offset = 0;
        if (useRadixSort) {
            offset = RadixSort.sort(
                    array, pos,
                    PackedRecordPointer.PARTITION_ID_START_BYTE_INDEX,
                    PackedRecordPointer.PARTITION_ID_END_BYTE_INDEX, false, false);
        } else {
            MemoryBlock unused = new MemoryBlock(
                    array.getBaseObject(),
                    array.getBaseOffset() + pos * 8L,
                    (array.size() - pos) * 8L);
            LongArray buffer = new LongArray(unused);
            Sorter<PackedRecordPointer, LongArray> sorter =
                    new Sorter<>(new ShuffleSortDataFormat(buffer));

            sorter.sort(array, 0, pos, SORT_COMPARATOR);
        }
        return new ShuffleSorterIterator(pos, array, offset);
    }
}
```

#### 2.2.2.2 growPointerArrayIfNecessary()

ShuffleExternalSorter.java

```java
final class ShuffleExternalSorter extends MemoryConsumer implements ShuffleChecksumSupport {

    /**
     * Checks whether there is enough space to insert an additional record in to the sort pointer
     * array and grows the array if additional space is required. If the required space cannot be
     * obtained, then the in-memory data will be spilled to disk.
     * 检查是否有足够的空间向 排序指针数组 中插入额外的记录，如果需要额外的空间，则扩大数组 【ShuffleExternalSorter类中全局的LongArray】。
     * 如果无法获得所需的空间，那么内存中的数据将溢出到磁盘
     */
    private void growPointerArrayIfNecessary() throws IOException {
        assert(inMemSorter != null);
        // 新记录插入到的位置 超过 可以插入的记录数
        if (!inMemSorter.hasSpaceForAnotherRecord()) {
            long used = inMemSorter.getMemoryUsage();
            LongArray array;
            try {
                // 2.2.2.2.1节 【扩大了LongArray的大小(used)；创建了一个新的LongArray，其元素是MemoryBlock】
                // 【为数组扩大的那部分分配了一个内存page】
                // could trigger spilling
                array = allocateArray(used / 8 * 2);
            } 
            // 如果这个 LongArray 太大，以致于无法装入一个page里，就抛 `TooLargePageException`
            catch (TooLargePageException e) {
                // The pointer array is too big to fix in a single page, spill. 
                // 2.2.2.2.2节 溢写
                spill();
                return;
            }
            // 如果 spark 为这次分配没有足够的内存，这个方法会抛 `SparkOutOfMemoryError`, 
            catch (SparkOutOfMemoryError e) { 
                // should have trigger spilling
                if (!inMemSorter.hasSpaceForAnotherRecord()) {
                    logger.error("Unable to grow the pointer array");
                    throw e;
                }
                return;
            }
            // check if spilling is triggered or not 
            // 如果溢写了，那么原来的 LongArray(`long used = inMemSorter.getMemoryUsage()`语句执行中的)就有空间存放新记录
            if (inMemSorter.hasSpaceForAnotherRecord()) {
                freeArray(array);
            } else {
                // 如果没有溢写，就扩展上面新创建的array
                inMemSorter.expandPointerArray(array); // 这里的 array 是上面新创建的
            }
        }
    }
}
```

##### 2.2.2.2.1 allocateArray()

```java
/**
 * A memory consumer of {@link TaskMemoryManager} that supports spilling.
 * 支持溢写的 TaskMemoryManager 的内存消费者
 * 
 * Note: this only supports allocation / spilling of Tungsten memory.
 * 注意：这仅支持 Tungsten 内存的分配、溢写
 */
public abstract class MemoryConsumer {

    /**
     * Allocates a LongArray of `size`. Note that this method may throw `SparkOutOfMemoryError`
     * if Spark doesn't have enough memory for this allocation, or throw `TooLargePageException`
     * if this `LongArray` is too large to fit in a single page. The caller side should take care of
     * these two exceptions, or make sure the `size` is small enough that won't trigger exceptions.
     * 分配一个 `size` 的 LongArray。
     * 注意，如果 spark 为这次分配没有足够的内存，这个方法会抛 `SparkOutOfMemoryError`, 
     *      如果这个 LongArray 太大，以致于无法装入一个page里，就抛 `TooLargePageException`
     * 调用者应该关注这两个异常，或确保 `size` 足够小，不会触发异常
     * 
     * 【扩大了可以使用的已有内存；创建了一个LongArray】
     * @throws SparkOutOfMemoryError
     * @throws TooLargePageException
     */
    public LongArray allocateArray(long size) { // used / 8 * 2
        long required = size * 8L;
        // 分配了一个内存块
        MemoryBlock page = taskMemoryManager.allocatePage(required, this);
        if (page == null || page.size() < required) {
            throwOom(page, required);
        }
        used += required;
        return new LongArray(page);
    }
}
```

##### 2.2.2.2.2 spill()

MemoryConsumer.java

```java
/**
 * A memory consumer of {@link TaskMemoryManager} that supports spilling.
 * 支持溢写的 memory consumer
 * Note: this only supports allocation / spilling of Tungsten memory.
 */
public abstract class MemoryConsumer {
    /**
     * Force spill during building.
     */
    public void spill() throws IOException {
        spill(Long.MAX_VALUE, this);
    }

    /**
     * Spill some data to disk to release memory, which will be called by TaskMemoryManager
     * when there is not enough memory for the task.
     * 将一些数据溢写到磁盘，来释放内存，
     * 这将在没有足够内存时，由 TaskMemoryManager 调用
     * 
     * This should be implemented by subclass.
     *
     * Note: In order to avoid possible deadlock, should not call acquireMemory() from spill().
     *
     * Note: today, this only frees Tungsten-managed pages.
     * 当前，这只释放 Tungsten-managed pages.
     * @param size the amount of memory should be released
     * @param trigger the MemoryConsumer that trigger this spilling
     * @return the amount of released memory in bytes
     */
    public abstract long spill(long size, MemoryConsumer trigger) throws IOException;
}
```

查看其子类

ShuffleExternalSorter.java

```java
final class ShuffleExternalSorter extends MemoryConsumer implements ShuffleChecksumSupport {
    /**
     * Sort and spill the current records in response to memory pressure.
     * 排序，并溢写当前记录
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
        inMemSorter.reset();
        // Reset the in-memory sorter's pointer array only after freeing up the memory pages holding the
        // records. Otherwise, if the task is over allocated memory, then without freeing the memory
        // pages, we might not be able to get memory for the pointer array.
        taskContext.taskMetrics().incMemoryBytesSpilled(spillSize);
        return spillSize;
    }
}
```

ShuffleExternalSorter.java

```java
final class ShuffleExternalSorter extends MemoryConsumer implements ShuffleChecksumSupport {
    /**
     * Sorts the in-memory records and writes the sorted records to an on-disk file.
     * This method does not free the sort data structures.
     * 排序内存中的记录，并将其写入磁盘文件
     * 
     * @param isLastFile if true, this indicates that we're writing the final output file and that the
     *                   bytes written should be counted towards shuffle spill metrics rather than
     *                   shuffle write metrics.
     *                   如果是true，表示我们正在写入最终的输出文件，
     */
    private void writeSortedFile(boolean isLastFile) {
        // 返回的迭代器，包含的记录指针是有序的 
        // This call performs the actual sort.
        final ShuffleInMemorySorter.ShuffleSorterIterator sortedRecords =
                inMemSorter.getSortedIterator();
        // ...
        DiskBlockObjectWriter writer =
                blockManager.getDiskWriter(blockId, file, ser, fileBufferSizeBytes, writeMetricsToUse)
        while (sortedRecords.hasNext()) {
            //...
            // 取到记录指针
            final long recordPointer = sortedRecords.packedRecordPointer.getRecordPointer();
            // 取到记录指针所在的 Memory Page
            final Object recordPage = taskMemoryManager.getPage(recordPointer);
            // 取到记录指针所在的 Memory Page 的偏移量
            final long recordOffsetInPage = taskMemoryManager.getOffsetInPage(recordPointer);
            int dataRemaining = UnsafeAlignedOffset.getSize(recordPage, recordOffsetInPage);
            // 从 recordReadPosition 位置开始读取
            long recordReadPosition = recordOffsetInPage + uaoSize; // skip over record length
            while (dataRemaining > 0) {
                final int toTransfer = Math.min(diskWriteBufferSize, dataRemaining);
                Platform.copyMemory(
                        recordPage, recordReadPosition, writeBuffer, Platform.BYTE_ARRAY_OFFSET, toTransfer);
                // 将记录从 Memory Page 复制到buffer里后，写入文件
                writer.write(writeBuffer, 0, toTransfer);
                recordReadPosition += toTransfer;
                dataRemaining -= toTransfer;
            }
            writer.recordWritten();
        }

        committedSegment = writer.commitAndGet();
        //...
    }
}
```

#### 2.2.2.3 Platform.copyMemory

Platform.java

```java
public final class Platform {
    public static void copyMemory(
            Object src, long srcOffset, Object dst, long dstOffset, long length) {
        // Check if dstOffset is before or after srcOffset to determine if we should copy
        // forward or backwards. This is necessary in case src and dst overlap.
        if (dstOffset < srcOffset) {
            while (length > 0) {
                long size = Math.min(length, UNSAFE_COPY_THRESHOLD);
                _UNSAFE.copyMemory(src, srcOffset, dst, dstOffset, size);
                length -= size;
                srcOffset += size;
                dstOffset += size;
            }
        } else {
            srcOffset += length;
            dstOffset += length;
            while (length > 0) {
                long size = Math.min(length, UNSAFE_COPY_THRESHOLD);
                srcOffset -= size;
                dstOffset -= size;
                _UNSAFE.copyMemory(src, srcOffset, dst, dstOffset, size);
                length -= size;
            }

        }
    }
}
```

#### 2.2.2.4 inMemSorter.insertRecord()

ShuffleInMemorySorter.java

```java
final class ShuffleInMemorySorter {

    private LongArray array;
    private int pos = 0;
    
    /**
     * Inserts a record to be sorted.
     * 
     * @param recordPointer a pointer to the record, encoded by the task memory manager. Due to
     *                      certain pointer compression techniques used by the sorter, the sort can
     *                      only operate on pointers that point to locations in the first
     *                      {@link PackedRecordPointer#MAXIMUM_PAGE_SIZE_BYTES} bytes of a data page.
     *                      一个指向记录的指针，由 task memory manage 编码。
     *                      由于 sorter 使用的特定的指针压缩技术，sorter 仅可以操作指向一个数据页中第一个 MAXIMUM_PAGE_SIZE_BYTES 字节的位置。
     * @param partitionId the partition id, which must be less than or equal to
     *                    {@link PackedRecordPointer#MAXIMUM_PARTITION_ID}.
     *                    分区id, 必须小于等于MAXIMUM_PARTITION_ID
     */
    public void insertRecord(long recordPointer, int partitionId) {
        if (!hasSpaceForAnotherRecord()) {
            throw new IllegalStateException("There is no space for new record");
        }
        // set: 在指定位置索引上设置一个值
        // packPointer: 将一个记录地址和分区id编码成一个word, 返回一个已打包的指针，它会由 PackedRecordPointer 类解码.
        // 此时，就可以根据这个array中找到记录
        array.set(pos, PackedRecordPointer.packPointer(recordPointer, partitionId));
        pos++;
    }
}
```