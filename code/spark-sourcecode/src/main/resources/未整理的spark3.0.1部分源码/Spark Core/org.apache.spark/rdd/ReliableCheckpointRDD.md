# ReliableCheckpointRDD

```java
package org.apache.spark.rdd

import java.io.{FileNotFoundException, IOException}
import java.util.concurrent.TimeUnit

import scala.collection.mutable
import scala.reflect.ClassTag
import scala.util.control.NonFatal

import com.google.common.cache.{CacheBuilder, CacheLoader}
import org.apache.hadoop.fs.Path

import org.apache.spark._
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.internal.Logging
import org.apache.spark.internal.config.{BUFFER_SIZE, CACHE_CHECKPOINT_PREFERRED_LOCS_EXPIRE_TIME, CHECKPOINT_COMPRESS}
import org.apache.spark.io.CompressionCodec
import org.apache.spark.util.{SerializableConfiguration, Utils}

/**
 * An RDD that reads from checkpoint files previously written to reliable storage. 从先前写入到可靠存储中的checkpoint文件中读取的RDD
 */
private[spark] class ReliableCheckpointRDD[T: ClassTag](
    sc: SparkContext,
    val checkpointPath: String,
    _partitioner: Option[Partitioner] = None
  ) extends CheckpointRDD[T](sc) {

  @transient private val hadoopConf = sc.hadoopConfiguration
  @transient private val cpath = new Path(checkpointPath)
  @transient private val fs = cpath.getFileSystem(hadoopConf)
  private val broadcastedConf = sc.broadcast(new SerializableConfiguration(hadoopConf)) //广播

  // Fail fast if checkpoint directory does not exist
  require(fs.exists(cpath), s"Checkpoint directory does not exist: $checkpointPath")

  /**
   * 返回这个RDD读取数据的checkpoint目录的路径
   * Return the path of the checkpoint directory this RDD reads data from.
   */
  override val getCheckpointFile: Option[String] = Some(checkpointPath)

  override val partitioner: Option[Partitioner] = {
    _partitioner.orElse {
      //从Checkpoint目录中，读取分区器对象
      ReliableCheckpointRDD.readCheckpointedPartitionerFile(context, checkpointPath)
    }
  }
//  @inline final def orElse[B >: A](alternative: => Option[B]): Option[B] =
//    if (isEmpty) alternative else this


  /**
   * 返回checkpoint目录文件中描述的分区
   *
   * Return partitions described by the files in the checkpoint directory.
   *
   * 由于原始RDD可能属于以前的应用程序，因此无法预先知道预期的分区数量。
   * 此方法假定原始的checkpoint文件集 在跨应用程序生命周期的可靠存储中 被完全保存。
   *
   * Since the original RDD may belong to a prior application, there is no way to know a
   * priori the number of partitions to expect. This method assumes that the original set of
   * checkpoint files are fully preserved in a reliable storage across application lifespans.
   */
  protected override def getPartitions: Array[Partition] = {
    // listStatus can throw exception if path does not exist.
    //listStatus：列出给定目录下的文件/目录状态
    val inputFiles = fs.listStatus(cpath)
      .map(_.getPath)
      .filter(_.getName.startsWith("part-"))  //过滤符合条件的元素
      .sortBy(_.getName.stripPrefix("part-").toInt)  //排序
    // Fail fast if input files are invalid
    // 检查分区文件目录格式
    //zipWithIndex：RDD和它的元素索引组合。
    inputFiles.zipWithIndex.foreach { case (path, i) =>
      //checkpointFileName：根据给定的分区索引，返回checkpoint文件名称
      if (path.getName != ReliableCheckpointRDD.checkpointFileName(i)) {
        throw new SparkException(s"Invalid checkpoint file: $path")
      }
    }
//def tabulate[T: ClassTag](n: Int)(f: Int => T): Array[T]
//将第二个参数f作用在数组的每个元素上，返回一个数组。
    Array.tabulate(inputFiles.length)(i => new CheckpointRDDPartition(i))
  }

  // Cache of preferred locations of checkpointed files.
  //缓存checkpointed文件的优先位置
  @transient private[spark] lazy val cachedPreferredLocations = CacheBuilder.newBuilder()
    .expireAfterWrite(
      SparkEnv.get.conf.get(CACHE_CHECKPOINT_PREFERRED_LOCS_EXPIRE_TIME).get,
      TimeUnit.MINUTES)
    .build(
      new CacheLoader[Partition, Seq[String]]() {
        override def load(split: Partition): Seq[String] = {
          getPartitionBlockLocations(split)
        }
      })

  // Returns the block locations of given partition on file system.
  // 返回给定分区的块的位置
  private def getPartitionBlockLocations(split: Partition): Seq[String] = {
    //给定分区下文件的状态
    val status = fs.getFileStatus(
      new Path(checkpointPath, ReliableCheckpointRDD.checkpointFileName(split.index)))
//Return an array containing hostnames, offset and size of portions of the given file. 
    val locations = fs.getFileBlockLocations(status, 0, status.getLen)
//headOption：不空的话，选择第一个元素
    locations.headOption.toList.flatMap(_.getHosts).filter(_ != "localhost")
  }

  private lazy val cachedExpireTime =
    SparkEnv.get.conf.get(CACHE_CHECKPOINT_PREFERRED_LOCS_EXPIRE_TIME)

  /**
   * Return the locations of the checkpoint file associated with the given partition.
   */
  protected override def getPreferredLocations(split: Partition): Seq[String] = {
    if (cachedExpireTime.isDefined && cachedExpireTime.get > 0) {
      cachedPreferredLocations.get(split)
    } else {
      getPartitionBlockLocations(split)
    }
  }

  /**
   * 读取 和给定分区相关的checkpoint文件的内容
   * Read the content of the checkpoint file associated with the given partition.
   */
  override def compute(split: Partition, context: TaskContext): Iterator[T] = {
    val file = new Path(checkpointPath, ReliableCheckpointRDD.checkpointFileName(split.index))
    ReliableCheckpointRDD.readCheckpointFile(file, broadcastedConf, context)
  }

}

private[spark] object ReliableCheckpointRDD extends Logging {

  /**
   * 根据给定的分区索引，返回checkpoint文件名称
   * Return the checkpoint file name for the given partition.
   */
  private def checkpointFileName(partitionIndex: Int): String = {
    "part-%05d".format(partitionIndex)  //part+分区索引
  }

  private def checkpointPartitionerFileName(): String = {
    "_partitioner"
  }

  /**
   * 把这个RDD写入到Checkpoint文件,返回一个ReliableCheckpointRDD表示这个RDD
   * Write RDD to checkpoint files and return a ReliableCheckpointRDD representing the RDD.
   */
  def writeRDDToCheckpointDirectory[T: ClassTag](
      originalRDD: RDD[T],
      checkpointDir: String,
      blockSize: Int = -1): ReliableCheckpointRDD[T] = {
    val checkpointStartTimeNs = System.nanoTime()

    val sc = originalRDD.sparkContext

    // Create the output path for the checkpoint
    val checkpointDirPath = new Path(checkpointDir)
    val fs = checkpointDirPath.getFileSystem(sc.hadoopConfiguration)
    //创建checkpoint目录失败就抛异常
    if (!fs.mkdirs(checkpointDirPath)) {
      throw new SparkException(s"Failed to create checkpoint path $checkpointDirPath")
    }

    // Save to file, and reload it as an RDD

    //广播配置文件
    val broadcastedConf = sc.broadcast(
 //Hadoop configuration but serializable. Use `value` to access the Hadoop configuration.
      new SerializableConfiguration(sc.hadoopConfiguration))
    // TODO: This is expensive because it computes the RDD again unnecessarily (SPARK-8582)
    //这里会再次计算RDD
    //runJob:将后面的函数作用在originalRDD的每个分区上.
    //即把每个分区的数据写到Checkpoint文件
    sc.runJob(originalRDD,
      writePartitionToCheckpointFile[T](checkpointDirPath.toString, broadcastedConf) _)

//上下两行代码:做checkpoint的时候，写入的hdfs中的数据主要包括：RDD中每个parition的实际数据，以及可能的partitioner对象

    if (originalRDD.partitioner.nonEmpty) {
    //将分区器写入给定的 RDD 的 checkpoint点目录。
      writePartitionerToCheckpointDir(sc, originalRDD.partitioner.get, checkpointDirPath)
    }

    //计算 checkpoint 时长
    val checkpointDurationMs =
      TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - checkpointStartTimeNs)
    logInfo(s"Checkpointing took $checkpointDurationMs ms.")

    val newRDD = new ReliableCheckpointRDD[T](
      sc, checkpointDirPath.toString, originalRDD.partitioner)

    if (newRDD.partitions.length != originalRDD.partitions.length) {
      throw new SparkException(
        "Checkpoint RDD has a different number of partitions from original RDD. Original " +
          s"RDD [ID: ${originalRDD.id}, num of partitions: ${originalRDD.partitions.length}]; " +
          s"Checkpoint RDD [ID: ${newRDD.id}, num of partitions: " +
          s"${newRDD.partitions.length}].")
    }
    newRDD
  }

  /**
   * 把每个分区的数据写到Checkpoint文件
   * Write an RDD partition's data to a checkpoint file.
   */
  def writePartitionToCheckpointFile[T: ClassTag](
      path: String,
      broadcastedConf: Broadcast[SerializableConfiguration],
      blockSize: Int = -1)(ctx: TaskContext, iterator: Iterator[T]): Unit = {
    val env = SparkEnv.get
    val outputDir = new Path(path)
    val fs = outputDir.getFileSystem(broadcastedConf.value.value)

    val finalOutputName = ReliableCheckpointRDD.checkpointFileName(ctx.partitionId())
    val finalOutputPath = new Path(outputDir, finalOutputName)
    val tempOutputPath =
      new Path(outputDir, s".$finalOutputName-attempt-${ctx.attemptNumber()}")

    val bufferSize = env.conf.get(BUFFER_SIZE)

    val fileOutputStream = if (blockSize < 0) {
    //create：Create an FSDataOutputStream at the indicated Path.
      val fileStream = fs.create(tempOutputPath, false, bufferSize)
// CHECKPOINT_COMPRESS：Whether to compress RDD checkpoints。默认false
      if (env.conf.get(CHECKPOINT_COMPRESS)) {
        CompressionCodec.createCodec(env.conf).compressedOutputStream(fileStream)
      } else {
        fileStream
      }
    } else {
      // This is mainly for testing purpose
      fs.create(tempOutputPath, false, bufferSize,
        fs.getDefaultReplication(fs.getWorkingDirectory), blockSize)
    }
//getDefaultReplication：获取默认副本数，默认是1

    val serializer = env.serializer.newInstance()
    //返回SerializationStream对象
    val serializeStream = serializer.serializeStream(fileOutputStream)
    Utils.tryWithSafeFinally {
      serializeStream.writeAll(iterator)
    } {
      serializeStream.close()
    }

    if (!fs.rename(tempOutputPath, finalOutputPath)) {
      if (!fs.exists(finalOutputPath)) {
        logInfo(s"Deleting tempOutputPath $tempOutputPath")
        fs.delete(tempOutputPath, false)
        throw new IOException("Checkpoint failed: failed to save output of task: " +
          s"${ctx.attemptNumber()} and final output path does not exist: $finalOutputPath")
      } else {
        // Some other copy of this task must've finished before us and renamed it
        logInfo(s"Final output path $finalOutputPath already exists; not overwriting it")
        if (!fs.delete(tempOutputPath, false)) {
          logWarning(s"Error deleting ${tempOutputPath}")
        }
      }
    }
  }

  /**
   * 将分区器写入给定的 RDD 的 checkpoint点目录。
   * Write a partitioner to the given RDD checkpoint directory. This is done on a best-effort
   * basis; any exception while writing the partitioner is caught, logged and ignored.
   */
  private def writePartitionerToCheckpointDir(
    sc: SparkContext, partitioner: Partitioner, checkpointDirPath: Path): Unit = {
    try {
      //分区器文件路径
      val partitionerFilePath = new Path(checkpointDirPath, checkpointPartitionerFileName)
      val bufferSize = sc.conf.get(BUFFER_SIZE)
      val fs = partitionerFilePath.getFileSystem(sc.hadoopConfiguration)
      val fileOutputStream = fs.create(partitionerFilePath, false, bufferSize)
      val serializer = SparkEnv.get.serializer.newInstance()
      val serializeStream = serializer.serializeStream(fileOutputStream)
      Utils.tryWithSafeFinally {
        serializeStream.writeObject(partitioner)
      } {
        serializeStream.close()
      }
      logDebug(s"Written partitioner to $partitionerFilePath")
    } catch {
      case NonFatal(e) =>
        logWarning(s"Error writing partitioner $partitioner to $checkpointDirPath")
    }
  }


  /**
   * 从给定RDD checkpoint目录读取一个分区器，如果存在的话。
   *
   * Read a partitioner from the given RDD checkpoint directory, if it exists.
   * This is done on a best-effort basis; any exception while reading the partitioner is
   * caught, logged and ignored.
   */
  private def readCheckpointedPartitionerFile(
      sc: SparkContext,
      checkpointDirPath: String): Option[Partitioner] = {
    try {

      // private[spark] val BUFFER_SIZE =
      // ...
      // .createWithDefault(65536)
      val bufferSize = sc.conf.get(BUFFER_SIZE)

      //将checkpoint目录路径和要读的分区器文件名，拼成一个Path对象返回
      val partitionerFilePath = new Path(checkpointDirPath, checkpointPartitionerFileName)
      val fs = partitionerFilePath.getFileSystem(sc.hadoopConfiguration)
      val fileInputStream = fs.open(partitionerFilePath, bufferSize)
      val serializer = SparkEnv.get.serializer.newInstance()
      val partitioner = Utils.tryWithSafeFinally {
//deserializeStream是DeserializationStream子类对象：读取序列化对象的流
        val deserializeStream = serializer.deserializeStream(fileInputStream)
        Utils.tryWithSafeFinally {
          //读取Partitioner对象
          deserializeStream.readObject[Partitioner]
        } {
          deserializeStream.close()
        }
      } {
        fileInputStream.close()
      }

      logDebug(s"Read partitioner from $partitionerFilePath")
      Some(partitioner)
    } catch {
      case e: FileNotFoundException =>
        logDebug("No partitioner file", e)
        None
      case NonFatal(e) =>
        logWarning(s"Error reading partitioner from $checkpointDirPath, " +
            s"partitioner will not be recovered which may lead to performance loss", e)
        None
    }
  }

  /**
   * 读取指定的checkpoint文件的内容
   * Read the content of the specified checkpoint file.
   */
  def readCheckpointFile[T](
      path: Path,
      broadcastedConf: Broadcast[SerializableConfiguration],
      context: TaskContext): Iterator[T] = {
    val env = SparkEnv.get
    val fs = path.getFileSystem(broadcastedConf.value.value)
    val bufferSize = env.conf.get(BUFFER_SIZE)

    val fileInputStream = {
      val fileStream = fs.open(path, bufferSize)
      if (env.conf.get(CHECKPOINT_COMPRESS)) {  //获取压缩类型
        CompressionCodec.createCodec(env.conf).compressedInputStream(fileStream)
      } else {  //未经压缩
        fileStream
      }
    }
    val serializer = env.serializer.newInstance()
    val deserializeStream = serializer.deserializeStream(fileInputStream)

    // Register an on-task-completion callback to close the input stream.
    //任务完成监听器
    context.addTaskCompletionListener[Unit](context => deserializeStream.close())

    deserializeStream.asIterator.asInstanceOf[Iterator[T]]
  }

}

```