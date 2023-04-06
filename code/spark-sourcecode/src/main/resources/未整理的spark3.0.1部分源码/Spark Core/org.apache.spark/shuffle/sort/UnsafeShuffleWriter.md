# UnsafeShuffleWriter

```java
package org.apache.spark.shuffle.sort;

import java.nio.channels.Channels;
import java.util.Arrays;
import java.util.Optional;
import javax.annotation.Nullable;
import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Iterator;
...

@Private
public class UnsafeShuffleWriter<K, V> extends ShuffleWriter<K, V> {

  private static final Logger logger = LoggerFactory.getLogger(UnsafeShuffleWriter.class);

  private static final ClassTag<Object> OBJECT_CLASS_TAG = ClassTag$.MODULE$.Object();

  @VisibleForTesting
  static final int DEFAULT_INITIAL_SER_BUFFER_SIZE = 1024 * 1024;

  private final BlockManager blockManager;
  private final TaskMemoryManager memoryManager;
  private final SerializerInstance serializer;
  private final Partitioner partitioner;
  private final ShuffleWriteMetricsReporter writeMetrics;
  private final ShuffleExecutorComponents shuffleExecutorComponents;
  private final int shuffleId;
  private final long mapId;
  private final TaskContext taskContext;
  private final SparkConf sparkConf;
  private final boolean transferToEnabled;
  private final int initialSortBufferSize;
  private final int inputBufferSizeInBytes;

  // 一个ShuffleMapTask返回给一个scheduler的结果。
  // 包括运行任务的块管理器地址以及每个reducer的输出大小，以便传递给reduce任务。
  @Nullable private MapStatus mapStatus;

  // ShuffleExternalSorter：
  // 排序和合并多个类型为(K,V)的键值对，产生类型为(K,C)的key-combiner对。
  // 先使用一个分区器初次把 keys 划分到分区中，
  // 然后在每个分区中使用自定义的 Comparator 对 key 排序[可选步骤]。
  @Nullable private ShuffleExternalSorter sorter;
  private long peakMemoryUsedBytes = 0;

  //ByteArrayOutputStream：将数据写入字节数组的输出流，随着数据的写入，buffer自动增长。

  /** Subclass of ByteArrayOutputStream that exposes `buf` directly. */
  //直接暴露buf
  private static final class MyByteArrayOutputStream extends ByteArrayOutputStream {
    MyByteArrayOutputStream(int size) { super(size); }
    public byte[] getBuf() { return buf; }

    //The buffer where data is stored. 存储数据的buffer
    //protected byte buf[];
  }

  private MyByteArrayOutputStream serBuffer;
  private SerializationStream serOutputStream;  //写序列化对象的流

  /**
   * Are we in the process of stopping? Because map tasks can call stop() with success = true
   * and then call stop() with success = false if they get an exception, we want to make sure
   * we don't try deleting files, etc twice.
   */
    //判断是否处在一个停止的进程中。
  private boolean stopping = false;

  public UnsafeShuffleWriter(
      BlockManager blockManager,
      TaskMemoryManager memoryManager,  //管理一个独立任务分配的内存
      SerializedShuffleHandle<K, V> handle,//用来识别什么时候使用serialized shuffle
      long mapId,
      TaskContext taskContext,
      SparkConf sparkConf,
      ShuffleWriteMetricsReporter writeMetrics,//对每个shuffle，报告shuffle读取指标。
      ShuffleExecutorComponents shuffleExecutorComponents) {
    // 获取分区数
    final int numPartitions = handle.dependency().partitioner().numPartitions();
    //MAX_SHUFFLE_OUTPUT_PARTITIONS_FOR_SERIALIZED_MODE：
    //当以序列化形式缓存 map 输出时，SortShuffleManager 支持的最大 shuffle 输出分区数。
    if (numPartitions > SortShuffleManager.MAX_SHUFFLE_OUTPUT_PARTITIONS_FOR_SERIALIZED_MODE()) {
      throw new IllegalArgumentException(
        "UnsafeShuffleWriter can only be used for shuffles with at most " +
        SortShuffleManager.MAX_SHUFFLE_OUTPUT_PARTITIONS_FOR_SERIALIZED_MODE() +
        " reduce partitions");
    }
    this.blockManager = blockManager;
    this.memoryManager = memoryManager;
    this.mapId = mapId;
    final ShuffleDependency<K, V, V> dep = handle.dependency();
    this.shuffleId = dep.shuffleId();
    this.serializer = dep.serializer().newInstance();
    this.partitioner = dep.partitioner();
    this.writeMetrics = writeMetrics;
    this.shuffleExecutorComponents = shuffleExecutorComponents;
    this.taskContext = taskContext;
    this.sparkConf = sparkConf;
    this.transferToEnabled = sparkConf.getBoolean("spark.file.transferTo", true);
    this.initialSortBufferSize =
      (int) (long) sparkConf.get(package$.MODULE$.SHUFFLE_SORT_INIT_BUFFER_SIZE());
    this.inputBufferSizeInBytes =
      (int) (long) sparkConf.get(package$.MODULE$.SHUFFLE_FILE_BUFFER_SIZE()) * 1024;
    open();
  }

  private void updatePeakMemoryUsed() {
    // sorter can be null if this writer is closed
    if (sorter != null) {
      //回到目前为止使用的峰值内存，以字节为单位。
      long mem = sorter.getPeakMemoryUsedBytes();
      if (mem > peakMemoryUsedBytes) {
        peakMemoryUsedBytes = mem;
      }
    }
  }

  /**
   * Return the peak memory used so far, in bytes.
   */
  //回到目前为止使用的峰值内存，以字节为单位。
  public long getPeakMemoryUsedBytes() {
    updatePeakMemoryUsed();
    return peakMemoryUsedBytes;
  }

  /**
   * This convenience method should only be called in test code.
   */
  @VisibleForTesting
  public void write(Iterator<Product2<K, V>> records) throws IOException {
    write(JavaConverters.asScalaIteratorConverter(records).asScala());
  }

  @Override
  public void write(scala.collection.Iterator<Product2<K, V>> records) throws IOException {
    // Keep track of success so we know if we encountered an exception
    // We do this rather than a standard try/catch/re-throw to handle
    // generic throwables.
    //追踪success，为了知道我们是否遇到了一个异常。
    boolean success = false;
    try {
      while (records.hasNext()) {  //还有记录就插进去
        insertRecordIntoSorter(records.next());
      }
      closeAndWriteOutput();
      success = true;
    } finally {
      if (sorter != null) {
        try {
          sorter.cleanupResources();
        } catch (Exception e) {
          // Only throw this error if we won't be masking another
          // error.
          if (success) {
            throw e;
          } else {
            logger.error("In addition to a failure during writing, we failed during " +
                         "cleanup.", e);
          }
        }
      }
    }
  }

  private void open() {
    assert (sorter == null);
    sorter = new ShuffleExternalSorter(
      memoryManager,
      blockManager,
      taskContext,
      initialSortBufferSize,
      partitioner.numPartitions(),
      sparkConf,
      writeMetrics);
    serBuffer = new MyByteArrayOutputStream(DEFAULT_INITIAL_SER_BUFFER_SIZE);
    serOutputStream = serializer.serializeStream(serBuffer);
  }

  @VisibleForTesting
  void closeAndWriteOutput() throws IOException {
    assert(sorter != null);
    updatePeakMemoryUsed();
    serBuffer = null;
    serOutputStream = null;
    //closeAndGetSpills：关闭sorter，使所有缓冲的数据被排序，并写入磁盘。
    //返回这个sorter写的溢写文件的元数据。
    final SpillInfo[] spills = sorter.closeAndGetSpills();
    sorter = null;
    final long[] partitionLengths;
    try {
      //合并0个或多个溢写文件，返回合并的文件的分区长度的数组
      partitionLengths = mergeSpills(spills);
    } finally {
      for (SpillInfo spill : spills) {
        if (spill.file.exists() && !spill.file.delete()) {
          logger.error("Error while deleting spill file {}", spill.file.getPath());
        }
      }
    }
    mapStatus = MapStatus$.MODULE$.apply(
      blockManager.shuffleServerId(), partitionLengths, mapId);
  }

  @VisibleForTesting
  void insertRecordIntoSorter(Product2<K, V> record) throws IOException {
    assert(sorter != null);
    final K key = record._1();
    final int partitionId = partitioner.getPartition(key);
    // reset：重置字节数组输出流的count字段为0
    // count：缓存中有效的字节数
    serBuffer.reset();
    serOutputStream.writeKey(key, OBJECT_CLASS_TAG);
    serOutputStream.writeValue(record._2(), OBJECT_CLASS_TAG);
    serOutputStream.flush();

    final int serializedRecordSize = serBuffer.size();
    assert (serializedRecordSize > 0);

    //将一条记录写入到 shuffle sorter
    sorter.insertRecord(
      serBuffer.getBuf(), Platform.BYTE_ARRAY_OFFSET, serializedRecordSize, partitionId);
  }

  @VisibleForTesting
  void forceSorterToSpill() throws IOException {
    assert (sorter != null);
    sorter.spill();
  }

  /**
   * 合并0个或多个溢写文件，基于溢写的数量和IO压缩格式，选择最快的合并策略。
   *
   * Merge zero or more spill files together, choosing the fastest merging strategy based on the
   * number of spills and the IO compression codec.
   *
   * @return the partition lengths in the merged file. 返回合并的文件中的分区长度的数组
   */
  private long[] mergeSpills(SpillInfo[] spills) throws IOException {
    long[] partitionLengths;
    if (spills.length == 0) {
//createMapOutputWriter：每个 map task 仅调用它一次，来创建一个 writer，此 writer 负责那个 map task 写入的所有分区字节的持久化
      final ShuffleMapOutputWriter mapWriter = shuffleExecutorComponents
          .createMapOutputWriter(shuffleId, mapId, partitioner.numPartitions());

      //提交所有 partition writers 完成的写入操作
      return mapWriter.commitAllPartitions();
    } else if (spills.length == 1) {

//createSingleFileMapOutputWriter：
//创建一个 map output writer，来优化一个分区文件(一个 map task 的整个结果)到备份存储的传输，
      Optional<SingleSpillShuffleMapOutputWriter> maybeSingleFileWriter =
          shuffleExecutorComponents.createSingleFileMapOutputWriter(shuffleId, mapId);
      
      if (maybeSingleFileWriter.isPresent()) {
        // Here, we don't need to perform any metrics updates because the bytes written to this output file would have already been counted as shuffle bytes written.
      //这里不需要只需任意的度量更新，因为写入到这个输出文件的字节早已作为写入的shuffle字节统计。
        partitionLengths = spills[0].partitionLengths;
        logger.debug("Merge shuffle spills for mapId {} with length {}", mapId,
            partitionLengths.length);

        //get()：返回maybeSingleFileWriter持有的非空值。
        //把文件写入
        maybeSingleFileWriter.get().transferMapSpillFile(spills[0].file, partitionLengths);
      } else {  //maybeSingleFileWriter不存在的话
        partitionLengths = mergeSpillsUsingStandardWriter(spills);
      }
    } else {
      partitionLengths = mergeSpillsUsingStandardWriter(spills);
    }
    return partitionLengths;
  }

  private long[] mergeSpillsUsingStandardWriter(SpillInfo[] spills) throws IOException {
    long[] partitionLengths;
    //取出是否启用了压缩
    final boolean compressionEnabled = (boolean) sparkConf.get(package$.MODULE$.SHUFFLE_COMPRESS());
    //创建一个CompressionCodec对象，压缩数据
    final CompressionCodec compressionCodec = CompressionCodec$.MODULE$.createCodec(sparkConf);
    
    final boolean fastMergeEnabled =
        (boolean) sparkConf.get(package$.MODULE$.SHUFFLE_UNSAFE_FAST_MERGE_ENABLE());
    //如果启用了压缩，再判断
    final boolean fastMergeIsSupported = !compressionEnabled ||
        CompressionCodec$.MODULE$.supportsConcatenationOfSerializedStreams(compressionCodec);
    //是否启用了加密
    final boolean encryptionEnabled = blockManager.serializerManager().encryptionEnabled();

    final ShuffleMapOutputWriter mapWriter = shuffleExecutorComponents
        .createMapOutputWriter(shuffleId, mapId, partitioner.numPartitions());
    try {
      // There are multiple spills to merge, so none of these spill files' lengths were counted towards our shuffle write count or shuffle write time. If we use the slow merge path, then the final output file's size won't necessarily be equal to the sum of the spill files' sizes. To guard against this case, we look at the output file's actual size when computing shuffle bytes written.
      //因为有多个溢写文件要合并，所以这些溢写文件的长度的统计都不会计算到 shuffle write count 或 shuffle write time。
      //如果我们使用 slow merge path，那么最终的输出文件的大小没必要等于这些溢写文件的大小和。
      //为了防止这种情况，在计算写入的shuffle字节时，我们查看输出文件的实际大小。
      //
      // We allow the individual merge methods to report their own IO times since different merge strategies use different IO techniques.  We count IO during merge towards the shuffle write time, which appears to be consistent with the "not bypassing merge-sort" branch in ExternalSorter.
      //我们允许独立的合并方法，来报告它们自己的IO时长，因为不同的合并策略使用不同的IO技术。
      //在合并期间，对IO的统计计算到shuffle write time，这似乎和ExternalSorter中"not bypassing merge-sort"一致。

      //启用了快速合并，且支持
      if (fastMergeEnabled && fastMergeIsSupported) {
        // Compression is disabled or we are using an IO compression codec that supports
        // decompression of concatenated compressed streams, so we can perform a fast spill merge that doesn't need to interpret the spilled bytes.
        //禁用压缩，
        //或者使用IO压缩编解码器，且支持连接的压缩流的解压缩，所以，我们可以执行一个快速溢写合并，而不需要解释溢写字节。

        //启用传输，且没有加密
        if (transferToEnabled && !encryptionEnabled) {
          //使用基于transferTo的快速合并
          logger.debug("Using transferTo-based fast merge");
          mergeSpillsWithTransferTo(spills, mapWriter);
        } else {
          //使用基于fileStream的快速合并
          logger.debug("Using fileStream-based fast merge");
          mergeSpillsWithFileStream(spills, mapWriter, null);
        }
      } else {
        ////使用slow合并
        logger.debug("Using slow merge");
        mergeSpillsWithFileStream(spills, mapWriter, compressionCodec);
      }
      // When closing an UnsafeShuffleExternalSorter that has already spilled once but also has in-memory records, we write out the in-memory records to a file but do not count that final write as bytes spilled (instead, it's accounted as shuffle write). The merge needs to be counted as shuffle write, but this will lead to double-counting of the final SpillInfo's bytes.
  //当关闭一个UnsafeShuffleExternalSorter(它已经溢出了一次，但也有内存中的记录)时，
  //我们将内存中的记录写入到一个文件中，但不将最后的写入计算为字节溢出(相反，它被认为是随机写入)。
  //需要将合并计数为shuffle write，但这将导致对最终SpillInfo字节的重复计数。
      writeMetrics.decBytesWritten(spills[spills.length - 1].file.length());
      partitionLengths = mapWriter.commitAllPartitions();
    } catch (Exception e) {
      try {
        mapWriter.abort(e);
      } catch (Exception e2) {
        logger.warn("Failed to abort writing the map output.", e2);
        e.addSuppressed(e2);
      }
      throw e;
    }
    return partitionLengths;
  }

  /**
   * 使用 Java FileStreams 合并溢写文件。这个通常比基于nio的合并要慢【TransferTo的那个】，
   * 通常用在如下情况：当启用加密，或用户明确地禁用了transferTo时，IO压缩编解码器不支持压缩数据的连接。
   * 这可能在这种情况下会更快：在一个溢写中独立的分区大小不大，..TransferTo..方法会执行很多次小的磁盘io。
   * 在这些情况下，为输入输出文件使用大的缓存可以帮助奸杀磁盘io，让文件合并更快。
   *
   * Merges spill files using Java FileStreams. This code path is typically slower than
   * the NIO-based merge, {@link UnsafeShuffleWriter#mergeSpillsWithTransferTo(SpillInfo[],ShuffleMapOutputWriter)}, and it's mostly used in cases where the IO compression codec does not support concatenation of compressed data, when encryption is enabled, or when users have explicitly disabled use of {@code transferTo} in order to work around kernel bugs. This code path might also be faster in cases where individual partition size in a spill is small and UnsafeShuffleWriter#mergeSpillsWithTransferTo method performs many small disk ios which is inefficient. In those case, Using large buffers for input and output files helps reducing the number of disk ios, making the file merging faster.
   *
   * @param spills the spills to merge. 要合并的溢写
   * @param mapWriter the map output writer to use for output. 为输出使用的一个map output writer
   * @param compressionCodec the IO compression codec, or null if shuffle compression is disabled.
   * @return the partition lengths in the merged file. 返回合并文件中的分区长度。
   */
  private void mergeSpillsWithFileStream(
      SpillInfo[] spills,
      ShuffleMapOutputWriter mapWriter,
      @Nullable CompressionCodec compressionCodec) throws IOException {
    logger.debug("Merge shuffle spills with FileStream for mapId {}", mapId);
    final int numPartitions = partitioner.numPartitions();
    final InputStream[] spillInputStreams = new InputStream[spills.length];

    boolean threwException = true;
    try {
      //为每一个溢写文件创建一个NioBufferedFileInputStream对象，放到spillInputStreams里面
      for (int i = 0; i < spills.length; i++) {
        spillInputStreams[i] = new NioBufferedFileInputStream(
          spills[i].file,
          inputBufferSizeInBytes);
        // Only convert the partitionLengths when debug level is enabled.
        if (logger.isDebugEnabled()) {
          logger.debug("Partition lengths for mapId {} in Spill {}: {}", mapId, i,
              Arrays.toString(spills[i].partitionLengths));
        }
      }
      for (int partition = 0; partition < numPartitions; partition++) {
        boolean copyThrewException = true;

        //getPartitionWriter：打开输出流，持久化给定reduce partition id的字节。
        ShufflePartitionWriter writer = mapWriter.getPartitionWriter(partition);
        OutputStream partitionOutput = writer.openStream();
        try {
          //会跟踪花费在写上的总时间，以更新 shuffle write metrics。
          partitionOutput = new TimeTrackingOutputStream(writeMetrics, partitionOutput);
          //如果启用了加密，为输入流加密
          partitionOutput = blockManager.serializerManager().wrapForEncryption(partitionOutput);
          //压缩
          if (compressionCodec != null) {
            partitionOutput = compressionCodec.compressedOutputStream(partitionOutput);
          }
          for (int i = 0; i < spills.length; i++) {
            final long partitionLengthInSpill = spills[i].partitionLengths[partition];

            if (partitionLengthInSpill > 0) {
              InputStream partitionInputStream = null;
              boolean copySpillThrewException = true;
              try {
                //会限制读取字节数的流
                partitionInputStream = new LimitedInputStream(spillInputStreams[i],
                    partitionLengthInSpill, false);
                //如果启用了加密，为输入流加密
                partitionInputStream = blockManager.serializerManager().wrapForEncryption(
                    partitionInputStream);
                if (compressionCodec != null) {
                  partitionInputStream = compressionCodec.compressedInputStream(
                      partitionInputStream);
                }
                //copies all bytes from the input stream to the output stream.
                ByteStreams.copy(partitionInputStream, partitionOutput);
                copySpillThrewException = false;
              } finally {
                Closeables.close(partitionInputStream, copySpillThrewException);
              }
            }
          }
          copyThrewException = false;
        } finally {
          Closeables.close(partitionOutput, copyThrewException);
        }
        long numBytesWritten = writer.getNumBytesWritten();
        writeMetrics.incBytesWritten(numBytesWritten);
      }
      threwException = false;
    } finally {
      // To avoid masking exceptions that caused us to prematurely enter the finally block, only
      // throw exceptions during cleanup if threwException == false.
      for (InputStream stream : spillInputStreams) {
        Closeables.close(stream, threwException);
      }
    }
  }

  /**
   * 使用 NIO transferTo 合并溢写文件，可以连接溢写分区的字节。
   * 只有在压缩编解码器和序列化器支持序列化流的连接时才是安全的。
   *
   * Merges spill files by using NIO's transferTo to concatenate spill partitions' bytes.
   * This is only safe when the IO compression codec and serializer support concatenation of serialized streams.
   *
   * @param spills the spills to merge.
   * @param mapWriter the map output writer to use for output.
   * @return the partition lengths in the merged file.
   */
  private void mergeSpillsWithTransferTo(
      SpillInfo[] spills,
      ShuffleMapOutputWriter mapWriter) throws IOException {
    logger.debug("Merge shuffle spills with TransferTo for mapId {}", mapId);
    final int numPartitions = partitioner.numPartitions();
    final FileChannel[] spillInputChannels = new FileChannel[spills.length];
    final long[] spillInputChannelPositions = new long[spills.length];

    boolean threwException = true;
    try {
      for (int i = 0; i < spills.length; i++) {
        spillInputChannels[i] = new FileInputStream(spills[i].file).getChannel();
        // Only convert the partitionLengths when debug level is enabled.
        if (logger.isDebugEnabled()) {
          logger.debug("Partition lengths for mapId {} in Spill {}: {}", mapId, i,
              Arrays.toString(spills[i].partitionLengths));
        }
      }
      for (int partition = 0; partition < numPartitions; partition++) {
        boolean copyThrewException = true;
        ShufflePartitionWriter writer = mapWriter.getPartitionWriter(partition);
        WritableByteChannelWrapper resolvedChannel = writer.openChannelWrapper()
            .orElseGet(() -> new StreamFallbackChannelWrapper(openStreamUnchecked(writer)));
        try {
          for (int i = 0; i < spills.length; i++) {
            long partitionLengthInSpill = spills[i].partitionLengths[partition];
            final FileChannel spillInputChannel = spillInputChannels[i];
            final long writeStartTime = System.nanoTime();
            Utils.copyFileStreamNIO(
                spillInputChannel,
                resolvedChannel.channel(),
                spillInputChannelPositions[i],
                partitionLengthInSpill);
            copyThrewException = false;
            spillInputChannelPositions[i] += partitionLengthInSpill;
            writeMetrics.incWriteTime(System.nanoTime() - writeStartTime);
          }
        } finally {
          Closeables.close(resolvedChannel, copyThrewException);
        }
        long numBytes = writer.getNumBytesWritten();
        writeMetrics.incBytesWritten(numBytes);
      }
      threwException = false;
    } finally {
      // To avoid masking exceptions that caused us to prematurely enter the finally block, only
      // throw exceptions during cleanup if threwException == false.
      for (int i = 0; i < spills.length; i++) {
        assert(spillInputChannelPositions[i] == spills[i].file.length());
        Closeables.close(spillInputChannels[i], threwException);
      }
    }
  }

  @Override
  public Option<MapStatus> stop(boolean success) {
    try {
      //更新这个任务的context下的峰值内存度量
      taskContext.taskMetrics().incPeakExecutionMemory(getPeakMemoryUsedBytes());

      if (stopping) {
        return Option.apply(null);
      } else {
        stopping = true;
        if (success) {
          if (mapStatus == null) {
            throw new IllegalStateException("Cannot call stop(true) without having called write()");
          }
          return Option.apply(mapStatus);
        } else {
          return Option.apply(null);
        }
      }
    } finally {
      if (sorter != null) {
        // If sorter is non-null, then this implies that we called stop() in response to an error,  so we need to clean up memory and spill files created by the sorter

        //如果sorter是非空的，那么这意味着我们调用了stop()来响应一个错误，所以我们需要清理内存和溢出sorter创建的文件
        sorter.cleanupResources();
      }
    }
  }

  private static OutputStream openStreamUnchecked(ShufflePartitionWriter writer) {
    try {
      return writer.openStream();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static final class StreamFallbackChannelWrapper implements WritableByteChannelWrapper {
    private final WritableByteChannel channel;

    StreamFallbackChannelWrapper(OutputStream fallbackStream) {
      this.channel = Channels.newChannel(fallbackStream);
    }

    @Override
    public WritableByteChannel channel() {
      return channel;
    }

    @Override
    public void close() throws IOException {
      channel.close();
    }
  }
}

```