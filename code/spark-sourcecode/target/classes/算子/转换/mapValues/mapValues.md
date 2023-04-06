PairRDDFunctions.scala

```scala
  /**
   * Pass each value in the key-value pair RDD through a map function without changing the keys;
   * this also retains the original RDD's partitioning.
   * 针对分区操作
   *
   * 使用一个 map 函数，操作 RDD 中的每个 value，而不改变 key
   *
   * 和原 RDD 的分区数相同。【preservesPartitioning参数】
   */
  def mapValues[U](f: V => U): RDD[(K, U)] = self.withScope {
    val cleanF = self.context.clean(f)
    // 针对分区操作。对每个分区，操作每个 (k,v) 中的 v，k保持不变   pid:分区id
    new MapPartitionsRDD[(K, U), (K, V)](self,
      (context, pid, iter) => iter.map { case (k, v) => (k, cleanF(v)) },
      preservesPartitioning = true)
  }
```