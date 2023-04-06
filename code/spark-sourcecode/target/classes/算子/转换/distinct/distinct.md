
RDD.scala

```scala
  /**
   * Return a new RDD containing the distinct elements in this RDD.
   * 对 RDD 中的元素去重，返回一个新的 RDD 
   *
   * 为新RDD指定分区数  
   */
  def distinct(numPartitions: Int)(implicit ord: Ordering[T] = null): RDD[T] = withScope {
    def removeDuplicatesInPartition(partition: Iterator[T]): Iterator[T] = {
      // Create an instance of external append only map which ignores values.
      val map = new ExternalAppendOnlyMap[T, Null, Null](
        createCombiner = _ => null,
        mergeValue = (a, b) => a, // 只取分区中的一个值
        mergeCombiners = (a, b) => a)
      map.insertAll(partition.map(_ -> null)) // 插入前，先转成键值对形式
      map.iterator.map(_._1)
    }
    // 对 调用这个算子的 RDD 的分区器 进行模式匹配
    partitioner match {
      // 匹配上后，如果 传入的输出RDD的分区数 和 调用这个算子的 RDD 的分区数相同，对每个分区执行上面创建的方法操作
      case Some(_) if numPartitions == partitions.length =>
        mapPartitions(removeDuplicatesInPartition, preservesPartitioning = true)
      // 如果没有匹配上（如果partitioner是None，会匹配到这里）， 先转成键值对形式，然后相同的key分到一个分区，但只取分区中的一个值，最后取其中的key
      case _ => map(x => (x, null)).reduceByKey((x, _) => x, numPartitions).map(_._1)
    }
  }

  /**
   * Return a new RDD containing the distinct elements in this RDD.
   */
  def distinct(): RDD[T] = withScope {
    distinct(partitions.length)  //和 调用这个算子的 RDD的分区数 相同
  }

```