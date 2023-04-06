RDD.scala

```scala
  /**
   * Return a new RDD by applying a function to each partition of this RDD.
   * 对这个 rdd 的每个分区使用函数操作，返回一个新的rdd
   * 
   * `preservesPartitioning` indicates whether the input function preserves the partitioner, which
   * should be `false` unless this is a pair RDD and the input function doesn't modify the keys.
   * preservesPartitioning 表示是否保留父rdd的分区信息，默认是不保留。
   * 如果这是一个pair RDD，且输入函数不会修改key，那么就保留
   */
  def mapPartitions[U: ClassTag](
      f: Iterator[T] => Iterator[U],
      preservesPartitioning: Boolean = false): RDD[U] = withScope {
    
    val cleanedF = sc.clean(f)
    // 将函数作用在父rdd的每个分区产生的rdd
    new MapPartitionsRDD(
      this,
      //匿名函数，参数：(TaskContext, partition index, iterator)
      (_: TaskContext, _: Int, iter: Iterator[T]) => cleanedF(iter),
      preservesPartitioning)
  }
```

```scala
/**
 * An RDD that applies the provided function to every partition of the parent RDD.
 * 应用提供的函数到父RDD的每个分区
 * 
 * @param prev the parent RDD.
 * @param f The function used to map a tuple of (TaskContext, partition index, input iterator) to
 *          an output iterator.
 * @param preservesPartitioning Whether the input function preserves the partitioner, which should
 *                              be `false` unless `prev` is a pair RDD and the input function
 *                              doesn't modify the keys.
 * @param isFromBarrier Indicates whether this RDD is transformed from an RDDBarrier, a stage
 *                      containing at least one RDDBarrier shall be turned into a barrier stage.
 *                      表示这个RDD是否从一个 RDDBarrier 转换而来，包含至少一个 RDDBarrier 的阶段将被转成一个 barrier stage
 * @param isOrderSensitive whether or not the function is order-sensitive. If it's order
 *                         sensitive, it may return totally different result when the input order
 *                         is changed. Mostly stateful functions are order-sensitive.
 *                         函数是否是顺序敏感的。如果是，当输入顺序改变时，将返回完全不同的结果。大多数的函数是顺序敏感的
 */
private[spark] class MapPartitionsRDD[U: ClassTag, T: ClassTag](
    var prev: RDD[T],
    f: (TaskContext, Int, Iterator[T]) => Iterator[U],  // (TaskContext, partition index, iterator)
    preservesPartitioning: Boolean = false,
    isFromBarrier: Boolean = false,
    isOrderSensitive: Boolean = false)
  extends RDD[U](prev) {}
```