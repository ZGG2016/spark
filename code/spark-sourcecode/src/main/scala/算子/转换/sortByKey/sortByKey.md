
OrderedRDDFunctions.scala

```scala
  /**
   * Sort the RDD by key, so that each partition contains a sorted range of the elements. Calling
   * `collect` or `save` on the resulting RDD will return or output an ordered list of records
   * (in the `save` case, they will be written to multiple `part-X` files in the filesystem, in
   * order of the keys).
   * 根据 key 排序 rdd，这样每个分区包含 一个元素有序的范围  
   * 在排序后的rdd上调用 `collect` 或 `save` 可以返回或输出一个记录的有序列表 
   * 如果是调用了 `save` ，那么会按照不同的key的顺序，写到多个文件中。(一个分区一个文件)
   */
  // TODO: this currently doesn't work on P other than Tuple2!
  /*
      - ascending，决定排序后的RDD是升序还是降序，默认是true，即升序
      - numPartitions，决定排序后的RDD的分区个数，默认排序后的分区个数和排序之前的个数相等
   */
  def sortByKey(ascending: Boolean = true, numPartitions: Int = self.partitions.length)
      : RDD[(K, V)] = self.withScope
  {
    // 按范围将可排序的记录划分为大致相等的范围的分区器 【数据进入哪个分区由它决定】
    val part = new RangePartitioner(numPartitions, self, ascending)
    new ShuffledRDD[K, V, V](self, part)
      .setKeyOrdering(if (ascending) ordering else ordering.reverse)
  }
```

```scala
  // 决定了 ShuffledRDD 是升序还是降序
  /** Set key ordering for RDD's shuffle. */
  def setKeyOrdering(keyOrdering: Ordering[K]): ShuffledRDD[K, V, C] = {
    this.keyOrdering = Option(keyOrdering)
    this
  }
```