
PairRDDFunctions.scala

```scala
  /**
   * Generic function to combine the elements for each key using a custom set of aggregation
   * functions. This method is here for backward compatibility. It does not provide combiner
   * classtag information to the shuffle.
   * 一个通用函数，使用一系列的自定义的聚合函数，合并每个 key 的 value
   * 这个函数不会向 shuffle 提供 combiner classtag information (??)
   * 
   * 【0值由用户指定的函数确定，分区内计算规则和分区间计算规则不同】    
   * @see `combineByKeyWithClassTag`
   */
  def combineByKey[C](
      createCombiner: V => C,
      mergeValue: (C, V) => C,
      mergeCombiners: (C, C) => C,
      partitioner: Partitioner,
      mapSideCombine: Boolean = true,  // 是否在map端本地合并，默认true
      serializer: Serializer = null): RDD[(K, C)] = self.withScope {
    // 自己指定分区器
    combineByKeyWithClassTag(createCombiner, mergeValue, mergeCombiners,
      partitioner, mapSideCombine, serializer)(null)
  }

  /**
   * Simplified version of combineByKeyWithClassTag that hash-partitions the output RDD.
   * This method is here for backward compatibility. It does not provide combiner
   * classtag information to the shuffle.
   * 简化版的 combineByKeyWithClassTag，对输出 RDD 进行 hash 分区
   * 
   * @see `combineByKeyWithClassTag`
   */
  def combineByKey[C](
      createCombiner: V => C,
      mergeValue: (C, V) => C,
      mergeCombiners: (C, C) => C,
      numPartitions: Int): RDD[(K, C)] = self.withScope {
    // 使用 HashPartitioner
    combineByKeyWithClassTag(createCombiner, mergeValue, mergeCombiners, numPartitions)(null)
  }
```