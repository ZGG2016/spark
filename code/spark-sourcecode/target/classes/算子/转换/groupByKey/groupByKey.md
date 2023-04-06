
PairRDDFunctions.scala

```scala
    /**
     * Group the values for each key in the RDD into a single sequence. Allows controlling the
     * partitioning of the resulting key-value pair RDD by passing a Partitioner.
     * The ordering of elements within each group is not guaranteed, and may even differ
     * each time the resulting RDD is evaluated.
     * 在 rdd 中，根据每个 key, 分组 value，成一个序列。
     *
     * 通过传 Partitioner 参数，可以控制结果 rdd 的分区。
     * 但不能保证组内元素有序，可能每次计算的结果 rdd 都不同。
     * 
     * @note This operation may be very expensive. If you are grouping in order to perform an
     * aggregation (such as a sum or average) over each key, using `PairRDDFunctions.aggregateByKey`
     * or `PairRDDFunctions.reduceByKey` will provide much better performance.
     * 1、这个操作是昂贵的。如果你分组是为了在每个 key 上执行聚合操作(如sum、average)，
     *    那么最好使用 aggregateByKey 或 reduceByKey。
     *    
     * @note As currently implemented, groupByKey must be able to hold all the key-value pairs for any
     * key in memory. If a key has too many values, it can result in an `OutOfMemoryError`.
     * 2、groupByKey 操作会把所有的键值对放到内存，如果一个 key 有太多的 value，
     *    那么会 OutOfMemoryError
     * 
     */
    def groupByKey(partitioner: Partitioner): RDD[(K, Iterable[V])] = self.withScope {
      // groupByKey shouldn't use map side combine because map side combine does not
      // reduce the amount of data shuffled and requires all map side data be inserted
      // into a hash table, leading to more objects in the old gen.
      /**
       * groupByKey不应该使用map端的combine。因为map端的combine不会减少shuffle的数据量，
       * 还会要求把所有map端的数据插入到hash table里，导致老生代有很多对象。
       **/
      // 建一个仅能追加的 CompactBuffer
      val createCombiner = (v: V) => CompactBuffer(v)
      // 将新来的数据放到这个 CompactBuffer
      val mergeValue = (buf: CompactBuffer[V], v: V) => buf += v
      // 将两个 CompactBuffer 中的数据合并到一个
      val mergeCombiners = (c1: CompactBuffer[V], c2: CompactBuffer[V]) => c1 ++= c2
      val bufs = combineByKeyWithClassTag[CompactBuffer[V]](
        createCombiner, mergeValue, mergeCombiners, partitioner, mapSideCombine = false) // 没有map端的combine
      bufs.asInstanceOf[RDD[(K, Iterable[V])]]
    }

    /**
     * Group the values for each key in the RDD into a single sequence. Hash-partitions the
     * resulting RDD with the existing partitioner/parallelism level. The ordering of elements
     * within each group is not guaranteed, and may even differ each time the resulting RDD is
     * evaluated.
     *
     * @note This operation may be very expensive. If you are grouping in order to perform an
     * aggregation (such as a sum or average) over each key, using `PairRDDFunctions.aggregateByKey`
     * or `PairRDDFunctions.reduceByKey` will provide much better performance.
     */
    def groupByKey(): RDD[(K, Iterable[V])] = self.withScope {
      groupByKey(defaultPartitioner(self))
    }

    /**
     * Group the values for each key in the RDD into a single sequence. Hash-partitions the
     * resulting RDD with into `numPartitions` partitions. The ordering of elements within
     * each group is not guaranteed, and may even differ each time the resulting RDD is evaluated.
     *
     * @note This operation may be very expensive. If you are grouping in order to perform an
     * aggregation (such as a sum or average) over each key, using `PairRDDFunctions.aggregateByKey`
     * or `PairRDDFunctions.reduceByKey` will provide much better performance.
     *
     * @note As currently implemented, groupByKey must be able to hold all the key-value pairs for any
     * key in memory. If a key has too many values, it can result in an `OutOfMemoryError`.
     */
    def groupByKey(numPartitions: Int): RDD[(K, Iterable[V])] = self.withScope {
      groupByKey(new HashPartitioner(numPartitions))
    }

```