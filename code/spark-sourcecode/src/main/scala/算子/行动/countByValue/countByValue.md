
PairRDDFunctions.scala

```scala
  /**
   * Return the count of each unique value in this RDD as a local map of (value, count) pairs.
   * 这个 rdd 中，统计唯一值(key和value作为一个整体)的个数(不是<k,v>里的v)，并以(value, count)本地map的形式返回。
   * 
   * @note This method should only be used if the resulting map is expected to be small, as
   * the whole thing is loaded into the driver's memory.
   * To handle very large results, consider using
   * 只有当结果map是少量的时候，才使用这个方法。因为所有的数据会加载到driver的内存中。
   * 为了处理大量的数据，使用 rdd.map(x => (x, 1L)).reduceByKey(_ + _) 这会返回一个rdd，而不是map。 
   * 
   * {{{
   * rdd.map(x => (x, 1L)).reduceByKey(_ + _)
   * }}}
   *
   * , which returns an RDD[T, Long] instead of a map.
   */
  def countByValue()(implicit ord: Ordering[T] = null): Map[T, Long] = withScope {
    // map:让原key和原value作为新rdd的key，null作为新rdd的value
    map(value => (value, null)).countByKey()
  }
```