PairRDDFunctions.scala

```scala
/**
 * Pass each value in the key-value pair RDD through a flatMap function without changing the
 * keys; this also retains the original RDD's partitioning.
 * 针对分区操作
 *
 * 使用一个 flatMap 函数，操作 RDD 中的每个 value，而不改变 key
 *
 * 和原 RDD 的分区数相同。【preservesPartitioning参数】
 */
def flatMapValues[U](f: V => TraversableOnce[U]): RDD[(K, U)] = self.withScope {
  val cleanF = self.context.clean(f)
  new MapPartitionsRDD[(K, U), (K, V)](self,
    // 会对f处理的结果，再次调用map处理，所以会展平
    (context, pid, iter) => iter.flatMap { case (k, v) =>
      cleanF(v).map(x => (k, x))
    },
    preservesPartitioning = true)
}
```