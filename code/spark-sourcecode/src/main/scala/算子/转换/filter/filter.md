

```scala
  /**
   * Return a new RDD containing only the elements that satisfy a predicate.
   * 返回满足条件的元素，组成的新的RDD 
   */
  def filter(f: T => Boolean): RDD[T] = withScope {
    val cleanF = sc.clean(f)
    // 针对分区操作
    new MapPartitionsRDD[T, T](
      this,
      (_, _, iter) => iter.filter(cleanF), // scala包下的filter   // (TaskContext, partition index, iterator) 
      preservesPartitioning = true)
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

```scala
 /** Returns an iterator over all the elements of this iterator that satisfy the predicate `p`.
   *  The order of the elements is preserved. 
   *  保留元素顺序
   *  @param p the predicate used to test values.
   *  @return  an iterator which produces those values of this iterator which satisfy the predicate `p`.
   *  @note    Reuse: $consumesAndProducesIterator
   */
  def filter(p: A => Boolean): Iterator[A] = new AbstractIterator[A] {
    // TODO 2.12 - Make a full-fledged FilterImpl that will reverse sense of p
    private var hd: A = _
    private var hdDefined: Boolean = false

    def hasNext: Boolean = hdDefined || {
      do {
        if (!self.hasNext) return false
        hd = self.next()
      } while (!p(hd))  //判断hd是否满足p条件
      hdDefined = true
      true
    }

    def next() = if (hasNext) { hdDefined = false; hd } else empty.next()
  }
```

面试题：filter是rdd操作吗

	是，既是rdd的transformation，也是scala的函数。

	当rdd调用filter过滤时，会对每个分区执行iter.filter(cleanF)，这里的filter就是scala的函数