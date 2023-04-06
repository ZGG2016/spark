
PairRDDFunctions.scala

```scala
  /**
   * Count the number of elements for each key, collecting the results to a local Map.
   * 统计相同的 key 的元素有多少个，并将结果收集到本地 map
   * 
   * @note This method should only be used if the resulting map is expected to be small, as
   * the whole thing is loaded into the driver's memory.
   * To handle very large results, consider using rdd.mapValues(_ => 1L).reduceByKey(_ + _), which
   * returns an RDD[T, Long] instead of a map.
   * 只有当结果 map 是少量的时候，才使用这个方法。因为所有的数据会加载到 driver 的内存中。
   *
   * 为了处理大量的数据，使用 rdd.mapValues(_ =>1L).reduceByKey(_ + _)。
   * 这会返回一个rdd，而不是map
   */
  def countByKey(): Map[K, Long] = self.withScope {
    // 先 key 对应的值改成 1,再把这个 key 对应所有 1 加起来，就是对应的个数
    self.mapValues(_ => 1L).reduceByKey(_ + _).collect().toMap
  }

```