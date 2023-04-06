# BypassMergeSortShuffleWriter

```java
package org.apache.spark.shuffle.sort;

import java.io.File;
import java.io.FileInputStream;
...

/**
 * 这个类实现了 sort-based shuffle 的散列样式的 shuffle 回退路径。
 * 这个写入路径将传入的记录写入到独立的文件，一个 reduce 分区对应一个文件，
 *    然后连接这些每个分区文件，形成一个输出文件，regions of which are served to reducers.
 *
 * 记录不会缓存在内存中。它以一种可以通过 IndexShuffleBlockResolver 的服务/消费的格式写入输出。
 *
 * This class implements sort-based shuffle's hash-style shuffle fallback path. This write path writes incoming records to separate files, one file per reduce partition, then concatenates these per-partition files to form a single output file, regions of which are served to reducers. Records are not buffered in memory. It writes output in a format that can be served / consumed via {@link org.apache.spark.shuffle.IndexShuffleBlockResolver}.
 *
 * <p>
 * This write path is inefficient for shuffles with large numbers of reduce partitions because it simultaneously opens separate serializers and file streams for all partitions. As a result,
 * {@link SortShuffleManager} only selects this write path when
 * <ul>
 *    <li>no map-side combine is specified, and</li>
 *    <li>the number of partitions is less than or equal to
 *      <code>spark.shuffle.sort.bypassMergeThreshold</code>.</li>
 * </ul>
 *
* 这个写入路径对具有大量 reduce 分区的 shuffle 来说是低效的，
 * 因为它同时为每个分区打开一个单独的序列化器和文件流。
 *
 * 只有在如下三种情况下，SortShuffleManager 才会选择这种写入路径：
 *     没有指定map端的combine；
 *     分区数小于等于 spark.shuffle.sort.bypassMergeThreshold
 *
 * This code used to be part of {@link org.apache.spark.util.collection.ExternalSorter} but was refactored into its own class in order to reduce code complexity; see SPARK-7855 for details.
 * 这个类过去是 org.apache.spark.util.collection.ExternalSorter 的一部分，
 * 但是被重构为自己的类，以减少代码的复杂性;
 * <p>
 * There have been proposals to completely remove this code path; see SPARK-6026 for details.
 */
final class BypassMergeSortShuffleWriter<K, V> extends ShuffleWriter<K, V> {

  private static final Logger logger = LoggerFactory.getLogger(BypassMergeSortShuffleWriter.class);

  private final int fileBufferSize;
  private final boolean transferToEnabled;
  private final int numPartitions;
  private final BlockManager blockManager;
  private final Partitioner partitioner;
  //对每个shuffle，报告 shuffle write 指标。
  private final ShuffleWriteMetricsReporter writeMetrics;
  private final int shuffleId;
  private final long mapId;
  private final Serializer serializer;
  //为Executors，构建的支持shuffle的接口。
  private final ShuffleExecutorComponents shuffleExecutorComponents;

  /** Array of file writers, one for each partition */
  // file writers的数组，一个分区对应一个writer

  //DiskBlockObjectWriter：一个将JVM对象直接写入磁盘文件的类。
  private DiskBlockObjectWriter[] partitionWriters;
  //根据一个偏移量和一个长度，定义文件的一个片段。
  private FileSegment[] partitionWriterSegments;
  //一个ShuffleMapTask返回给一个scheduler的结果。
  @Nullable private MapStatus mapStatus;
  private long[] partitionLengths;

  /**
   * Are we in the process of stopping? Because map tasks can call stop() with success = true
   * and then call stop() with success = false if they get an exception, we want to make sure
   * we don't try deleting files, etc twice.
   */
  //判断是否处在stop的进程中。
  private boolean stopping = false;

  BypassMergeSortShuffleWriter(
      BlockManager blockManager,
      BypassMergeSortShuffleHandle<K, V> handle,//用来识别什么时候使用bypass merge sort shuffle 路径
      long mapId,
      SparkConf conf,
      ShuffleWriteMetricsReporter writeMetrics,
      ShuffleExecutorComponents shuffleExecutorComponents) {
    // Use getSizeAsKb (not bytes) to maintain backwards compatibility if no units are provided
    this.fileBufferSize = (int) (long) conf.get(package$.MODULE$.SHUFFLE_FILE_BUFFER_SIZE()) * 1024;
    this.transferToEnabled = conf.getBoolean("spark.file.transferTo", true);
    this.blockManager = blockManager;
    final ShuffleDependency<K, V, V> dep = handle.dependency();
    this.mapId = mapId;
    this.shuffleId = dep.shuffleId();
    this.partitioner = dep.partitioner();
    this.numPartitions = partitioner.numPartitions();
    this.writeMetrics = writeMetrics;
    this.serializer = dep.serializer();
    this.shuffleExecutorComponents = shuffleExecutorComponents;
  }

//spark.file.transferTo决定在使用BypassMerge...Writer过程中，最后对文件进行合并时是否使用NIO方式进行file stream的copy。
//默认为true，在为false的情况下合并文件效率比较低(创建一个大小为8192的字节数组作为buffer，从in stream中读满后写入out stream,单线程读写)，
//版本号为2.6.32的linux内核在使用NIO方式会产生bug，需要将该参数设置为false。


  @Override
  public void write(Iterator<Product2<K, V>> records) throws IOException {
    assert (partitionWriters == null);

//createMapOutputWriter：每个 map task 仅调用它一次，来创建一个 writer，此 writer 负责那个 map task 写入的所有分区字节的持久化
    ShuffleMapOutputWriter mapOutputWriter = shuffleExecutorComponents
        .createMapOutputWriter(shuffleId, mapId, numPartitions);
    try {
      // 如果没有记录了
      if (!records.hasNext()) {
        //提交所有 partition writers 完成的写入操作
        partitionLengths = mapOutputWriter.commitAllPartitions();
        mapStatus = MapStatus$.MODULE$.apply(
          blockManager.shuffleServerId(), partitionLengths, mapId);
        return;
      }
      final SerializerInstance serInstance = serializer.newInstance();

      final long openStartTime = System.nanoTime();

      //DiskBlockObjectWriter：一个将JVM对象直接写入磁盘文件的类。
      partitionWriters = new DiskBlockObjectWriter[numPartitions];
      partitionWriterSegments = new FileSegment[numPartitions];
      for (int i = 0; i < numPartitions; i++) {
// createTempShuffleBlock：生成一个唯一的block id，和适合用来存储shuffle的中间结果的文件。
        final Tuple2<TempShuffleBlockId, File> tempShuffleBlockIdPlusFile =
            blockManager.diskBlockManager().createTempShuffleBlock();
        final File file = tempShuffleBlockIdPlusFile._2();
        final BlockId blockId = tempShuffleBlockIdPlusFile._1();
        partitionWriters[i] =
        //getDiskWriter:获得一个可以将数据直接写入到磁盘的一个block writer。
            blockManager.getDiskWriter(blockId, file, serInstance, fileBufferSize, writeMetrics);
      }
      // Creating the file to write to and creating a disk writer both involve interacting with the disk, and can take a long time in aggregate when we open many files, so should be included in the shuffle write time.
      //创建要写入的文件和磁盘writer都涉及到与磁盘的交互，并且当我们打开许多文件时可能会花费很长时间，所以应该包括在随机写入时间中。
      writeMetrics.incWriteTime(System.nanoTime() - openStartTime);

      while (records.hasNext()) {
        final Product2<K, V> record = records.next();
        final K key = record._1();
        partitionWriters[partitioner.getPartition(key)].write(key, record._2());
      }

      for (int i = 0; i < numPartitions; i++) {
        try (DiskBlockObjectWriter writer = partitionWriters[i]) {
          //刷新部分写操作，并将它们作为一个原子块提交。
          partitionWriterSegments[i] = writer.commitAndGet();
        }
      }

      //连接每个分区文件成一个合并了的文件
      partitionLengths = writePartitionedData(mapOutputWriter);
      mapStatus = MapStatus$.MODULE$.apply(
        blockManager.shuffleServerId(), partitionLengths, mapId);
    } catch (Exception e) {
      try {
        mapOutputWriter.abort(e);
      } catch (Exception e2) {
        logger.error("Failed to abort the writer after failing to write map output.", e2);
        e.addSuppressed(e2);
      }
      throw e;
    }
  }

  @VisibleForTesting
  long[] getPartitionLengths() {
    return partitionLengths;
  }

  /**
   * 连接每个分区文件成一个合并了的文件
   *
   * Concatenate all of the per-partition files into a single combined file.
   *
   * @return array of lengths, in bytes, of each partition of the file (used by map output tracker). 文件的每个分区的长度的数组，字节形式。
   */
  private long[] writePartitionedData(ShuffleMapOutputWriter mapOutputWriter) throws IOException {
    // Track location of the partition starts in the output file
    if (partitionWriters != null) {
      final long writeStartTime = System.nanoTime();
      try {
        for (int i = 0; i < numPartitions; i++) {
          final File file = partitionWriterSegments[i].file();
          //getPartitionWriter：
          //创建一个writer，可以打开输出流，来持久化给定reduce partition id的字节。
          ShufflePartitionWriter writer = mapOutputWriter.getPartitionWriter(i);
          if (file.exists()) {
            if (transferToEnabled) {
              // Using WritableByteChannelWrapper to make resource closing consistent between this implementation and UnsafeShuffleWriter.

              //可以写字节的Channel
              Optional<WritableByteChannelWrapper> maybeOutputChannel = writer.openChannelWrapper();
              //如果存在这个maybeOutputChannel
              if (maybeOutputChannel.isPresent()) {
                //使用Channel写
                writePartitionedDataWithChannel(file, maybeOutputChannel.get());
              } else {
                //使用文件流写
                writePartitionedDataWithStream(file, writer);
              }
            } else {
              //使用文件流写
              writePartitionedDataWithStream(file, writer);
            }
            if (!file.delete()) {
              logger.error("Unable to delete file for partition {}", i);
            }
          }
        }
      } finally {
        writeMetrics.incWriteTime(System.nanoTime() - writeStartTime);
      }
      partitionWriters = null;
    }
    return mapOutputWriter.commitAllPartitions();
  }

  //使用Channel写
  private void writePartitionedDataWithChannel(
      File file,
      WritableByteChannelWrapper outputChannel) throws IOException {
    boolean copyThrewException = true;
    try {
      FileInputStream in = new FileInputStream(file);
      try (FileChannel inputChannel = in.getChannel()) {
        Utils.copyFileStreamNIO(
            inputChannel, outputChannel.channel(), 0L, inputChannel.size());
        copyThrewException = false;
      } finally {
        Closeables.close(in, copyThrewException);
      }
    } finally {
      Closeables.close(outputChannel, copyThrewException);
    }
  }

  //使用文件流写
  private void writePartitionedDataWithStream(File file, ShufflePartitionWriter writer)
      throws IOException {
    boolean copyThrewException = true;
    FileInputStream in = new FileInputStream(file);
    OutputStream outputStream;
    try {
      outputStream = writer.openStream();
      try {
        Utils.copyStream(in, outputStream, false, false);
        copyThrewException = false;
      } finally {
        Closeables.close(outputStream, copyThrewException);
      }
    } finally {
      Closeables.close(in, copyThrewException);
    }
  }

  @Override
  public Option<MapStatus> stop(boolean success) {
    if (stopping) {
      return None$.empty();
    } else {
      stopping = true;
      if (success) {
        if (mapStatus == null) {
          throw new IllegalStateException("Cannot call stop(true) without having called write()");
        }
        return Option.apply(mapStatus);
      } else {
        // The map task failed, so delete our output data.
        if (partitionWriters != null) {
          try {
            for (DiskBlockObjectWriter writer : partitionWriters) {
              // This method explicitly does _not_ throw exceptions:
              File file = writer.revertPartialWritesAndClose();
              if (!file.delete()) {
                logger.error("Error while deleting file {}", file.getAbsolutePath());
              }
            }
          } finally {
            partitionWriters = null;
          }
        }
        return None$.empty();
      }
    }
  }
}

```