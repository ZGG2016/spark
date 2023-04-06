# LocalRDDCheckpointData

```java
package org.apache.spark.rdd

import scala.reflect.ClassTag

import org.apache.spark.{SparkEnv, TaskContext}
import org.apache.spark.internal.Logging
import org.apache.spark.storage.{RDDBlockId, StorageLevel}
import org.apache.spark.util.Utils

/**
 * 在Spark缓存层之上实现的checkpoint实现。
 * An implementation of checkpointing implemented on top of Spark's caching layer.
 *
 * 本地checkpoint牺牲了容错能力。
 * 通过跳过在可靠的分布式文件系统中保存物化数据这一昂贵步骤。
 * 
 * checkpoint数据被写入到每个executors的本地临时块存储中。
 *
 * 这对于需要经常截断的长血统的RDDs非常有用。
 *
 * Local checkpointing trades off fault tolerance for performance by skipping the expensive
 * step of saving the RDD data to a reliable and fault-tolerant storage. Instead, the data
 * is written to the local, ephemeral block storage that lives in each executor. This is useful
 * for use cases where RDDs build up long lineages that need to be truncated often (e.g. GraphX).
 */
private[spark] class LocalRDDCheckpointData[T: ClassTag](@transient private val rdd: RDD[T])
  extends RDDCheckpointData[T](rdd) with Logging {

  /**
   * 为了之后可以恢复分区，这里确保RDD被完全缓存。
   * Ensure the RDD is fully cached so the partitions can be recovered later.
   */
  protected override def doCheckpoint(): CheckpointRDD[T] = {
    val level = rdd.getStorageLevel

    // Assume storage level uses disk; otherwise memory eviction may cause data loss  使用磁盘
    assume(level.useDisk, s"Storage level $level is not appropriate for local checkpointing")

    // Not all actions compute all partitions of the RDD (e.g. take). For correctness, we
    // must cache any missing partitions. TODO: avoid running another job here (SPARK-8582).
    //并不是所有的action操作会计算RDD的所有分区。
    //为了正确性，必须缓存任何缺失的分区。【囿于某些action的限制，例如take等，并没有触发所有分区的转换。这样对于那些未经计算的RDD分区需要重新生成。】
    //计算分区包含元素数量
    val action = (tc: TaskContext, iterator: Iterator[T]) => Utils.getIteratorSize(iterator)
    val missingPartitionIndices = rdd.partitions.map(_.index).filter { i =>
      !SparkEnv.get.blockManager.master.contains(RDDBlockId(rdd.id, i))
    }

    if (missingPartitionIndices.nonEmpty) {

   //在给定的分区集上运行一个函数
   //def runJob[T, U: ClassTag](rdd: RDD[T],func: (TaskContext, Iterator[T]) => U,partitions: Seq[Int]): Array[U]
      rdd.sparkContext.runJob(rdd, action, missingPartitionIndices)
    }

    new LocalCheckpointRDD[T](rdd)
  }

}

   //@DeveloperApi
   //case class RDDBlockId(rddId: Int, splitIndex: Int) extends BlockId {
   //	override def name: String = "rdd_" + rddId + "_" + splitIndex
   //}

  /**
   * 检查block manager master是否有一个指定的块。
   * 只能用来检查向它汇报的块。
   * Check if block manager master has a block. Note that this can be used to check for only
   * those blocks that are reported to block manager master.
   */
  //def contains(blockId: BlockId): Boolean = {
  //  !getLocations(blockId).isEmpty
  //}

   /** Get locations of the blockId from the driver */
  //def getLocations(blockId: BlockId): Seq[BlockManagerId] = {
  //  driverEndpoint.askSync[Seq[BlockManagerId]](GetLocations(blockId))
  //}

private[spark] object LocalRDDCheckpointData {

  val DEFAULT_STORAGE_LEVEL = StorageLevel.MEMORY_AND_DISK

  /**
   * 把指定的存储级别转换成使用磁盘的一种。
   *
   * 这就保证了，只要executors不故障，RDD可以被正确地再次计算多次。
   * 如果RDD仅被缓存在内存，如果内存上的相关块丢失，那么checkpoint数据也会丢失。
   *
   * Transform the specified storage level to one that uses disk.
   *
   * This guarantees that the RDD can be recomputed multiple times correctly as long as
   * executors do not fail. Otherwise, if the RDD is cached in memory only, for instance,
   * the checkpoint data will be lost if the relevant block is evicted from memory.
   *
   * This method is idempotent.
   */
  def transformStorageLevel(level: StorageLevel): StorageLevel = {
    StorageLevel(useDisk = true, level.useMemory, level.deserialized, level.replication)
  }
}

```