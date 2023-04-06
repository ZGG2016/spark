
RDD.scala

```scala
  /**
   * Mark this RDD for checkpointing. It will be saved to a file inside the checkpoint
   * directory set with `SparkContext#setCheckpointDir` and all references to its parent
   * RDDs will be removed. This function must be called before any job has been
   * executed on this RDD. It is strongly recommended that this RDD is persisted in
   * memory, otherwise saving it on a file will require recomputation.
   * 标记这个RDD做checkpoint。
   * 将会存储到 `SparkContext#setCheckpointDir` 指定的目录下的文件中。
   * 所有对它的父RDD的引用都会被移除。【切断依赖】
   * 必须在这个RDD上执行任何job之前调用此函数。
   * 建议将这个RDD持久化到内存，否则，将其保存到文件中将需要重新计算。
   */
  def checkpoint(): Unit = RDDCheckpointData.synchronized {
    // NOTE: we use a global lock here due to complexities downstream with ensuring
    // children RDD partitions point to the correct parent partitions. In the future
    // we should revisit this consideration.
    // 注意:为了确保子RDD分区指向正确的父分区，在这里使用了一个全局锁。今后我们应重新考虑这一问题。
    if (context.checkpointDir.isEmpty) {
      throw SparkCoreErrors.checkpointDirectoryHasNotBeenSetInSparkContextError()
    } else if (checkpointData.isEmpty) {
      //将RDD数据写入到可靠存储的checkpoint的实现  内部会执行 sc.runJob
      checkpointData = Some(new ReliableRDDCheckpointData(this))
    }
  }
```