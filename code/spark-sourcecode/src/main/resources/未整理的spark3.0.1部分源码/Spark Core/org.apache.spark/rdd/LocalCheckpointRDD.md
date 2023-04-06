# LocalCheckpointRDD

```java
package org.apache.spark.rdd

import scala.reflect.ClassTag

import org.apache.spark.{Partition, SparkContext, SparkException, TaskContext}
import org.apache.spark.storage.RDDBlockId

/**
 * 存在的一个虚拟CheckpointRDD，用于在失败期间提供有用的错误消息。
 * A dummy CheckpointRDD that exists to provide informative error messages during failures.
 *
 * 这只是一个占位符，因为期望原始的checkpointedRDD将被完全缓存。
 * 只有在executor失败或者用户显式地unpersists了原始RDD时，才会尝试计算这个CheckpointRDD。
 * 当发生时，必须提供有用的错误消息。
 *
 * This is simply a placeholder because the original checkpointed RDD is expected to be
 * fully cached. Only if an executor fails or if the user explicitly unpersists the original
 * RDD will Spark ever attempt to compute this CheckpointRDD. When this happens, however,
 * we must provide an informative error message.
 *
 * @param sc the active SparkContext
 * @param rddId the ID of the checkpointed RDD
 * @param numPartitions the number of partitions in the checkpointed RDD
 */
private[spark] class LocalCheckpointRDD[T: ClassTag](
    sc: SparkContext,
    rddId: Int,
    numPartitions: Int)
  extends CheckpointRDD[T](sc) {

  def this(rdd: RDD[T]) {
    this(rdd.context, rdd.id, rdd.partitions.length)
  }

  protected override def getPartitions: Array[Partition] = {
    (0 until numPartitions).toArray.map { i => new CheckpointRDDPartition(i) }
  }

  /**
   * 抛出异常说明相关块没被找到。
   * 
   * 只有在原始RDD明确地被unpersisted，或者丢失了executor，这才会被调用。
   * 正常情况下，原始RDD期望被完成缓存，所以所有的分区应当被计算，且在块存储中可用。
   *
   * Throw an exception indicating that the relevant block is not found.
   *
   * This should only be called if the original RDD is explicitly unpersisted or if an
   * executor is lost. Under normal circumstances, however, the original RDD (our child)
   * is expected to be fully cached and so all partitions should already be computed and
   * available in the block storage.
   */
  override def compute(partition: Partition, context: TaskContext): Iterator[T] = {
    throw new SparkException(
      s"Checkpoint block ${RDDBlockId(rddId, partition.index)} not found! Either the executor " +
      s"that originally checkpointed this partition is no longer alive, or the original RDD is " +
      s"unpersisted. If this problem persists, you may consider using `rdd.checkpoint()` " +
      s"instead, which is slower than local checkpointing but more fault-tolerant.")
  }

}

```