
### 2.2.3 BypassMergeSortShuffleWriter.write()

BypassMergeSortShuffleWriter.java

```java
final class BypassMergeSortShuffleWriter<K, V>
  extends ShuffleWriter<K, V> implements ShuffleChecksumSupport {
    // key:partition  value:TaskContext 
    public void write(Iterator<Product2<K, V>> records) throws IOException {
        assert (partitionWriters == null);
        // 为每个map任务调用一次，创建一个ShuffleMapOutputWriter, 它负责持久化该map任务写入的所有分区字节
        ShuffleMapOutputWriter mapOutputWriter = shuffleExecutorComponents
                .createMapOutputWriter(shuffleId, mapId, numPartitions);
        try {
            // 提交所有 partitionWriter 完成的写入操作，返回每个分区写入的字节数
            if (!records.hasNext()) {
                partitionLengths = mapOutputWriter.commitAllPartitions(
                        ShuffleChecksumHelper.EMPTY_CHECKSUM_VALUE).getPartitionLengths();
                mapStatus = MapStatus$.MODULE$.apply(
                        blockManager.shuffleServerId(), partitionLengths, mapId);
                return;
            }
            final SerializerInstance serInstance = serializer.newInstance();
            final long openStartTime = System.nanoTime();
            // DiskBlockObjectWriter 会直接将 JVM 对象写到磁盘文件
            partitionWriters = new DiskBlockObjectWriter[numPartitions];
            // 根据偏移量和长度引用文件中的特定段(可能是整个文件)。 【文件的一部分】
            partitionWriterSegments = new FileSegment[numPartitions];
            // 遍历每个分区，对每个分区创建一个 DiskBlockObjectWriter，放入一个 "数组" 中 （所以，每个分区会产生一个文件）
            for (int i = 0; i < numPartitions; i++) {
                // 2.2.3.1节  产生一个唯一的块id及其对应的文件
                final Tuple2<TempShuffleBlockId, File> tempShuffleBlockIdPlusFile =
                        blockManager.diskBlockManager().createTempShuffleBlock();
                final File file = tempShuffleBlockIdPlusFile._2();
                final BlockId blockId = tempShuffleBlockIdPlusFile._1();
                // 直接 new 一个 DiskBlockObjectWriter 
                DiskBlockObjectWriter writer =
                        blockManager.getDiskWriter(blockId, file, serInstance, fileBufferSize, writeMetrics);
                if (partitionChecksums.length > 0) {
                    writer.setChecksum(partitionChecksums[i]);
                }
                // 放入一个 "数组" 中
                partitionWriters[i] = writer;
            }
            // Creating the file to write to and creating a disk writer both involve interacting with
            // the disk, and can take a long time in aggregate when we open many files, so should be
            // included in the shuffle write time.
            writeMetrics.incWriteTime(System.nanoTime() - openStartTime);
            
            // 直接写一个分区的数据
            while (records.hasNext()) {
                final Product2<K, V> record = records.next();
                final K key = record._1();
                partitionWriters[partitioner.getPartition(key)].write(key, record._2());
            }
            
            // 写完后，刷新，并将其作为单个原子块提交
            for (int i = 0; i < numPartitions; i++) {
                try (DiskBlockObjectWriter writer = partitionWriters[i]) {
                    // 返回: 在此调用中，提交的之前的偏移量和长度的 FileSegment
                    partitionWriterSegments[i] = writer.commitAndGet();
                }
            }
            // 2.2.3.2节 将前面写入的每个分区文件的数据写入一个文件
            partitionLengths = writePartitionedData(mapOutputWriter);
            mapStatus = MapStatus$.MODULE$.apply(
                    blockManager.shuffleServerId(), partitionLengths, mapId);
        } catch (Exception e) {
           //...
        }
    }
}
```

#### 2.2.3.1 blockManager.diskBlockManager().createTempShuffleBlock()

BlockManager.scala

```scala
/**
 * Manager running on every node (driver and executors) which provides interfaces for putting and
 * retrieving blocks both locally and remotely into various stores (memory, disk, and off-heap).
 * 运行在每个节点（driver executors） 上的管理器，它提供了一个接口，来【放置和接收本地和远程的块到不同的存储中（内存、次哦和堆外内存）】
 * 
 * Note that [[initialize()]] must be called before the BlockManager is usable.
 * 注意： initialize 必须在 BlockManager 可用前调用
 */
private[spark] class BlockManager(
    val executorId: String,
    rpcEnv: RpcEnv,
    val master: BlockManagerMaster,
    val serializerManager: SerializerManager,
    val conf: SparkConf,
    memoryManager: MemoryManager,
    mapOutputTracker: MapOutputTracker,
    shuffleManager: ShuffleManager,
    val blockTransferService: BlockTransferService,
    securityManager: SecurityManager,
    externalBlockStoreClient: Option[ExternalBlockStoreClient])
  extends BlockDataManager with BlockEvictionHandler with Logging {

      val diskBlockManager = {
        // Only perform cleanup if an external service is not serving our shuffle files.
        val deleteFilesOnStop =
          !externalShuffleServiceEnabled || isDriver
          
        // 创建，并维护一个在逻辑块和物理磁盘上的位置的逻辑映射
        new DiskBlockManager(conf, deleteFilesOnStop = deleteFilesOnStop, isDriver = isDriver)
      }
}
```
DiskBlockManager.scala 

```scala
/**
 * Creates and maintains the logical mapping between logical blocks and physical on-disk
 * locations. One block is mapped to one file with a name given by its BlockId.
 * 创建，并维护一个在逻辑块和物理磁盘上的位置的逻辑映射。
 * 一个块被映射到一个文件，这个文件具有一个它的 BlockId 提供的名字
 * 
 * Block files are hashed among the directories listed in spark.local.dir (or in
 * SPARK_LOCAL_DIRS, if it's set).
 * 块文件在 `spark.local.dir` 中列出的目录中进行哈希
 *
 * ShuffleDataIO also can change the behavior of deleteFilesOnStop.
 */
private[spark] class DiskBlockManager(
    conf: SparkConf,
    var deleteFilesOnStop: Boolean,
    isDriver: Boolean)
  extends Logging {

      /** 
       * Produces a unique block id and File suitable for storing shuffled intermediate results. 
       * 产生一个唯一的块id和文件，文件用来存储shuffled的中间结果
       * */
      def createTempShuffleBlock(): (TempShuffleBlockId, File) = {
        // 与临时shuffle数据关联的Id。不能被序列化。
        var blockId = new TempShuffleBlockId(UUID.randomUUID())
        while (getFile(blockId).exists()) {
          blockId = new TempShuffleBlockId(UUID.randomUUID())
        }
        // getFile 会根据这个 blockid 的名字创建一个 File
        val tmpFile = getFile(blockId)
        if (permissionChangingRequired) {
          // SPARK-37618: we need to make the file world readable because the parent will
          // lose the setgid bit when making it group writable. Without this the shuffle
          // service can't read the shuffle files in a secure setup.
          createWorldReadableFile(tmpFile)
        }
        (blockId, tmpFile)
      }
}
```


#### 2.2.3.2 writePartitionedData()

BypassMergeSortShuffleWriter.java

```java
final class BypassMergeSortShuffleWriter<K, V>
  extends ShuffleWriter<K, V> implements ShuffleChecksumSupport {

    /**
     * Concatenate all of the per-partition files into a single combined file.
     * 将所有的分区文件连接成一个文件
     * 
     * @return array of lengths, in bytes, of each partition of the file (used by map output tracker).
     *         返回的是长度的数组，每个元素是文件的每个分区的长度
     */
    private long[] writePartitionedData(ShuffleMapOutputWriter mapOutputWriter) throws IOException {
        // Track location of the partition starts in the output file
        if (partitionWriters != null) {
            final long writeStartTime = System.nanoTime();
            try {
                // 遍历每个分区，操作每个分区对应的 FileSegment
                for (int i = 0; i < numPartitions; i++) {
                    // 找到其所属的文件
                    final File file = partitionWriterSegments[i].file();
                    
                    /*
                            ShuffleMapOutputWriter 有一个唯一实现类 LocalDiskShuffleMapOutputWriter
                               它会将shuffle的数据文件和索引文件一起持久化到本地磁盘
                               
                            ShufflePartitionWriter 有一个唯一子类 LocalDiskShufflePartitionWriter
                            
                            在 LocalDiskShuffleMapOutputWriter 类中，有一个 WritableByteChannelWrapper 的实现类 PartitionWriterChannel
                     */
                    
                    // ShufflePartitionWriter: 打开流，将分区字节持久化到后备数据存储。 
                    ShufflePartitionWriter writer = mapOutputWriter.getPartitionWriter(i);
                    // 判断文件存在
                    if (file.exists()) {
                        // 默认true
                        if (transferToEnabled) { 
                            // Using WritableByteChannelWrapper to make resource closing consistent between
                            // this implementation and UnsafeShuffleWriter.
                     
                            //  openChannelWrapper: 打开并返回 WritableByteChannelWrapper
                            Optional<WritableByteChannelWrapper> maybeOutputChannel = writer.openChannelWrapper();
                            if (maybeOutputChannel.isPresent()) {
                                // 将 file 中的数据写入到 WritableByteChannelWrapper(PartitionWriterChannel)
                                writePartitionedDataWithChannel(file, maybeOutputChannel.get());
                            } else {
                                // 直接写入一个输出流里
                                writePartitionedDataWithStream(file, writer);
                            }
                        } else {
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
        // 提交所有每个分区的 partitionWriter 完成的写入操作，返回每个分区写入的字节数
        return mapOutputWriter.commitAllPartitions(getChecksumValues(partitionChecksums))
                .getPartitionLengths();
    }
}
```

LocalDiskShuffleMapOutputWriter.scala

```java
public class LocalDiskShuffleMapOutputWriter implements ShuffleMapOutputWriter {
    public MapOutputCommitMessage commitAllPartitions(long[] checksums) throws IOException {
        // Check the position after transferTo loop to see if it is in the right position and raise a
        // exception if it is incorrect. The position will not be increased to the expected length
        // after calling transferTo in kernel version 2.6.32. This issue is described at
        // https://bugs.openjdk.java.net/browse/JDK-7052359 and SPARK-3948.
        if (outputFileChannel != null && outputFileChannel.position() != bytesWrittenToMergedFile) {
            throw new IOException(
                    "Current position " + outputFileChannel.position() + " does not equal expected " +
                            "position " + bytesWrittenToMergedFile + " after transferTo. Please check your " +
                            " kernel version to see if it is 2.6.32, as there is a kernel bug which will lead " +
                            "to unexpected behavior when using transferTo. You can set " +
                            "spark.file.transferTo=false to disable this NIO feature.");
        }
        cleanUp();
        File resolvedTmp = outputTempFile != null && outputTempFile.isFile() ? outputTempFile : null;
        log.debug("Writing shuffle index file for mapId {} with length {}", mapId,
                partitionLengths.length);
        // 将 返回的数据文件中的分区的长度的数组  复制到  LocalDiskShuffleMapOutputWriter类的属性partitionLengths数组中 【合并】
        blockResolver
                .writeMetadataFileAndCommit(shuffleId, mapId, partitionLengths, checksums, resolvedTmp);
        // 返回 MapOutputCommitMessage 对象，它  Represents the result of writing map outputs for a shuffle map task.
        // 而 partitionLengths分区长度表示在map任务中的每个块的长度。这可以用于下游读取器分配资源，例如内存中的缓冲区。
        return MapOutputCommitMessage.of(partitionLengths);
    }
}
```

```scala
/**
 * Create and maintain the shuffle blocks' mapping between logic block and physical file location.
 * Data of shuffle blocks from the same map task are stored in a single consolidated data file.
 * The offsets of the data blocks in the data file are stored in a separate index file.
 * 创建，并维护逻辑块和物理文件位置的shuffle块的映射。
 * 来自相同 map 任务的shuffle块数据存储在一个统一的数据文件中。 【合并】
 * 数据文件中的数据块的偏移量存储在一个单独的索引文件中。 【合并】
 * 
 * We use the name of the shuffle data's shuffleBlockId with reduce ID set to 0 and add ".data"
 * as the filename postfix for data file, and ".index" as the filename postfix for index file.
 */
// Note: Changes to the format in this file should be kept in sync with
// org.apache.spark.network.shuffle.ExternalShuffleBlockResolver#getSortBasedShuffleBlockData().
private[spark] class IndexShuffleBlockResolver(
    conf: SparkConf,
    // var for testing
    var _blockManager: BlockManager = null)
  extends ShuffleBlockResolver
  with Logging with MigratableResolver {

  /**
   * Commit the data and metadata files as an atomic operation, use the existing ones, or
   * replace them with new ones. Note that the metadata parameters (`lengths`, `checksums`)
   * will be updated to match the existing ones if use the existing ones.
   * 提交数据和元数据文件的操作作为一次原子操作执行，使用已存在的，或使用新的取代旧的
   * 注意：元数据参数将被更新
   * 
   * There're two kinds of metadata files:
   * 有两种形式的元数据文件：
   * 
   * - index file
   * An index file contains the offsets of each block, plus a final offset at the end
   * for the end of the output file. It will be used by [[getBlockData]] to figure out
   * where each block begins and ends.
   * - 索引文件
   *   索引文件包含了每个块的偏移量，和输出文件末尾的最后偏移量。
   *   它由 getBlockData 方法使用，用来知道每个块的开始和结束位置
   *
   * - checksum file (optional)
   * An checksum file contains the checksum of each block. It will be used to diagnose
   * the cause when a block is corrupted. Note that empty `checksums` indicate that
   * checksum is disabled.
   */
  def writeMetadataFileAndCommit(
                                  shuffleId: Int,
                                  mapId: Long,
                                  lengths: Array[Long],
                                  checksums: Array[Long],
                                  dataTmp: File): Unit = {
    // 取索引文件
    val indexFile = getIndexFile(shuffleId, mapId)
    // 创建一个临时文件，它将在最后重命名为结果文件 
    val indexTmp = createTempFile(indexFile)

    val checksumEnabled = checksums.nonEmpty
    val (checksumFileOpt, checksumTmpOpt) = if (checksumEnabled) {
      assert(lengths.length == checksums.length,
        "The size of partition lengths and checksums should be equal")
      val checksumFile =
        getChecksumFile(shuffleId, mapId, conf.get(config.SHUFFLE_CHECKSUM_ALGORITHM))
      (Some(checksumFile), Some(createTempFile(checksumFile)))
    } else {
      (None, None)
    }

    try {
      // 取数据文件
      val dataFile = getDataFile(shuffleId, mapId)
      // There is only one IndexShuffleBlockResolver per executor, this synchronization make sure
      // the following check and rename are atomic.
      this.synchronized {
        // 检查索引文件和数据文件是否匹配，匹配的话就返回数据文件中的分区的长度的数组
        val existingLengths = checkIndexAndDataFile(indexFile, dataFile, lengths.length)
        if (existingLengths != null) {
          // Another attempt for the same task has already written our map outputs successfully,
          // so just use the existing partition lengths and delete our temporary map outputs.
          // 将 返回的数据文件中的分区的长度的数组  复制到  LocalDiskShuffleMapOutputWriter类的属性partitionLengths数组中 【合并数据文件】
          System.arraycopy(existingLengths, 0, lengths, 0, lengths.length)
          if (checksumEnabled) {
            val existingChecksums = getChecksums(checksumFileOpt.get, checksums.length)
            if (existingChecksums != null) {
              System.arraycopy(existingChecksums, 0, checksums, 0, lengths.length)
            } else {
              // It's possible that the previous task attempt succeeded writing the
              // index file and data file but failed to write the checksum file. In
              // this case, the current task attempt could write the missing checksum
              // file by itself.
              writeMetadataFile(checksums, checksumTmpOpt.get, checksumFileOpt.get, false)
            }
          }
          if (dataTmp != null && dataTmp.exists()) {
            dataTmp.delete()
          }
        } else {
          // This is the first successful attempt in writing the map outputs for this task,
          // so override any existing index and data files with the ones we wrote.
          val offsets = lengths.scanLeft(0L)(_ + _)
          // 写入元数据文件(索引或校验和)。
          // 元数据值将首先写入tmp文件，tmp文件将在最后重命名为目标文件，以避免脏写。 【合并索引文件】
          writeMetadataFile(offsets, indexTmp, indexFile, true)

          if (dataFile.exists()) {
            dataFile.delete()
          }
          // dataTmp 重命名为 dataFile
          if (dataTmp != null && dataTmp.exists() && !dataTmp.renameTo(dataFile)) {
            throw new IOException("fail to rename file " + dataTmp + " to " + dataFile)
          }

          // write the checksum file
          checksumTmpOpt.zip(checksumFileOpt).foreach { case (checksumTmp, checksumFile) =>
            try {
              writeMetadataFile(checksums, checksumTmp, checksumFile, false)
            } catch {
              case e: Exception =>
                // It's not worthwhile to fail here after index file and data file are
                // already successfully stored since checksum is only a best-effort for
                // the corner error case.
                logError("Failed to write checksum file", e)
            }
          }
        }
      }
    } finally {
      //....
    }
  }
}
```