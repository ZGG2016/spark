RDD.scala

```scala
  /**
   * Mark this RDD for local checkpointing using Spark's existing caching layer.
   * 使用Spark已存在的缓存层，标记这个RDD进行本地的checkpoint
   * 
   * This method is for users who wish to truncate RDD lineages while skipping the expensive
   * step of replicating the materialized data in a reliable distributed file system. This is
   * useful for RDDs with long lineages that need to be truncated periodically (e.g. GraphX).
   * 这种方法适用于希望截断RDD血统，又不希望在可靠的分布式文件系统中复制物化数据这一昂贵步骤的用户
   * 这对于具有需要定期截断的长血统的RDDs非常有用
   * 
   * Local checkpointing sacrifices fault-tolerance for performance. In particular, checkpointed
   * data is written to ephemeral local storage in the executors instead of to a reliable,
   * fault-tolerant storage. The effect is that if an executor fails during the computation,
   * the checkpointed data may no longer be accessible, causing an irrecoverable job failure.
   * 本地checkpoint牺牲了容错能力。
   * 特别是，checkpoint数据被写入executors中的临时本地存储，而不是写入可靠的、容错的存储。
   * 其结果是，如果executors在计算期间失败，则checkpoint数据可能不再可访问，从而导致不可恢复的作业失败。
   * 
   * This is NOT safe to use with dynamic allocation, which removes executors along
   * with their cached blocks. If you must use both features, you are advised to set
   * `spark.dynamicAllocation.cachedExecutorIdleTimeout` to a high value.
   * 这与动态分配一起使用是不安全的，因为动态分配会删除executors及其缓存块。
   * 如果同时使用这两个属性，
   *   可以设置 `spark.dynamicAllocation.cachedExecutorIdleTimeout` 为一个大的值。
   *
   * The checkpoint directory set through `SparkContext#setCheckpointDir` is not used.
   * 通过 `SparkContext#setCheckpointDir` 设置的checkpoint目录将不再使用。
   * 
   * 【对于启用资源动态分配的情况下，executor是会随着任务进行动态分配与释放的，这便有对应的active状态与dead状态，
   * 对于dead之后executor的存储是无法访问了的，又重新分配的executor计算资源时也不一定能读到了】
   */
  def localCheckpoint(): this.type = RDDCheckpointData.synchronized {
    // 【确保没有开启动态分配，且包含了这个属性】
    if (conf.get(DYN_ALLOCATION_ENABLED) && // 默认 false
        conf.contains(DYN_ALLOCATION_CACHED_EXECUTOR_IDLE_TIMEOUT)) {  // 默认 Integer.MAX_VALUE
      logWarning("Local checkpointing is NOT safe to use with dynamic allocation, " +
        "which removes executors along with their cached blocks. If you must use both " +
        "features, you are advised to set `spark.dynamicAllocation.cachedExecutorIdleTimeout` " +
        "to a high value. E.g. If you plan to use the RDD for 1 hour, set the timeout to " +
        "at least 1 hour.")
    }

    // Note: At this point we do not actually know whether the user will call persist() on
    // this RDD later, so we must explicitly call it here ourselves to ensure the cached
    // blocks are registered for cleanup later in the SparkContext.
    //
    // If, however, the user has already called persist() on this RDD, then we must adapt
    // the storage level he/she specified to one that is appropriate for local checkpointing
    // (i.e. uses disk) to guarantee correctness.
    // 【确保使用的存储级别是 磁盘的存储级别】
    if (storageLevel == StorageLevel.NONE) {
      persist(LocalRDDCheckpointData.DEFAULT_STORAGE_LEVEL) // StorageLevel.MEMORY_AND_DISK
    } else {
      // 将指定的存储级别转换为使用磁盘的存储级别。
      persist(LocalRDDCheckpointData.transformStorageLevel(storageLevel), allowOverride = true)
    }

    // If this RDD is already checkpointed and materialized, its lineage is already truncated.
    // We must not override our `checkpointData` in this case because it is needed to recover
    // the checkpointed data. If it is overridden, next time materializing on this RDD will
    // cause error.
    // 1.1节【确保这个RDD还没有checkpointed and materialized】
    if (isCheckpointedAndMaterialized) { 
      logWarning("Not marking RDD for local checkpoint because it was already " +
        "checkpointed and materialized")
    } else {
      // Lineage is not truncated yet, so just override any existing checkpoint data with ours
      // 如果还没checkpointed and materialized，那就要执行 localCheckpoint
      checkpointData match {
        // ReliableRDDCheckpointData: 将RDD数据写入可靠存储的检查点实现。这允许 drivers  在故障时 以之前计算的状态重新启动。
        case Some(_: ReliableRDDCheckpointData[_]) => logWarning(
          "RDD was already marked for reliable checkpointing: overriding with local checkpoint.")
        case _ =>
      }
      // 1.2节
      checkpointData = Some(new LocalRDDCheckpointData(this))
    }
    this
  }
```

## 1.1 isCheckpointedAndMaterialized

RDD.scala

```scala
abstract class RDD[T: ClassTag](
       @transient private var _sc: SparkContext,
       @transient private var deps: Seq[Dependency[_]]
  ) extends Serializable with Logging {
  
  private[spark] var checkpointData: Option[RDDCheckpointData[T]] = None

  /**
   * Return whether this RDD is checkpointed and materialized, either reliably or locally.
   * This is introduced as an alias for `isCheckpointed` to clarify the semantics of the
   * return value. Exposed for testing.
   * 返回这个 RDD 是否已经 checkpointed and materialized, 不管是可靠的还是本地的。
   * 这是作为 ischeckpoint 的别名引入的，以澄清返回值的语义
   */
  private[spark] def isCheckpointedAndMaterialized: Boolean =
    checkpointData.exists(_.isCheckpointed)
}
```

RDDCheckpointData.scala
```scala
/**
 * Enumeration to manage state transitions of an RDD through checkpointing
 * checkpoint过程的三个状态
 * [ Initialized --{@literal >} checkpointing in progress --{@literal >} checkpointed ]
 */
private[spark] object CheckpointState extends Enumeration {
  type CheckpointState = Value
  val Initialized, CheckpointingInProgress, Checkpointed = Value
}

/**
 * This class contains all the information related to RDD checkpointing. Each instance of this
 * class is associated with an RDD. It manages process of checkpointing of the associated RDD,
 * as well as, manages the post-checkpoint state by providing the updated partitions,
 * iterator and preferred locations of the checkpointed RDD.
 * 这个类包含了和 RDD checkpoint 相关的所有信息。
 * 这个类的每个实例都和一个 RDD 相关。它管理着相关 RDD 的 checkpoint 过程，
 * 通过提供 checkpointed RDD 的更新的分区、迭代器和首选位置来管理检查点后的状态
 * 
 */
private[spark] abstract class RDDCheckpointData[T: ClassTag](@transient private val rdd: RDD[T])
  extends Serializable {
      // The checkpoint state of the associated RDD.
      protected var cpState = Initialized
      
      /**
       * Return whether the checkpoint data for this RDD is already persisted.
       * 返回这个 RDD 的 checkpoint 数据已经持久化了
       */
      def isCheckpointed: Boolean = RDDCheckpointData.synchronized {
        cpState == Checkpointed
      }
}
```

## 1.2 LocalRDDCheckpointData

LocalRDDCheckpointData.scala

```scala
/**
 * An implementation of checkpointing implemented on top of Spark's caching layer.
 * 在 Spark 的缓存层上实现的检查点的实现
 * 
 * Local checkpointing trades off fault tolerance for performance by skipping the expensive
 * step of saving the RDD data to a reliable and fault-tolerant storage. Instead, the data
 * is written to the local, ephemeral block storage that lives in each executor. This is useful
 * for use cases where RDDs build up long lineages that need to be truncated often (e.g. GraphX).
 */
private[spark] class LocalRDDCheckpointData[T: ClassTag](@transient private val rdd: RDD[T])
  extends RDDCheckpointData[T](rdd) with Logging {}
```