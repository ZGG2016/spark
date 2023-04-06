RDD.scala

```scala
    /**
     * Return an RDD of grouped items. Each group consists of a key and a sequence of elements
     * mapping to that key. The ordering of elements within each group is not guaranteed, and
     * may even differ each time the resulting RDD is evaluated.
     * 返回一个对数据分了组的 RDD。
     * 每组由一个key及其映射的元素组成。
     *
     * 不保证每组内元素的顺序，甚至可能每次分组后的结果的顺序都不同。
     *
     * 参数f理解成分组规则
     * @note This operation may be very expensive. If you are grouping in order to perform an
     * aggregation (such as a sum or average) over each key, using `PairRDDFunctions.aggregateByKey`
     * or `PairRDDFunctions.reduceByKey` will provide much better performance.
     * 如果分组是为了聚合， 使用 `PairRDDFunctions.aggregateByKey`、`PairRDDFunctions.reduceByKey`
     */
    def groupBy[K](f: T => K, p: Partitioner)(implicit kt: ClassTag[K], ord: Ordering[K] = null)
    : RDD[(K, Iterable[T])] = withScope {
      val cleanF = sc.clean(f)
      // 先使用传入的函数f作用在每条数据上，产生一个键值对形式，再调用 groupByKey
      this.map(t => (cleanF(t), t)).groupByKey(p)
    }

  /**
   * Return an RDD of grouped items. Each group consists of a key and a sequence of elements
   * mapping to that key. The ordering of elements within each group is not guaranteed, and
   * may even differ each time the resulting RDD is evaluated.
   *
   * @note This operation may be very expensive. If you are grouping in order to perform an
   * aggregation (such as a sum or average) over each key, using `PairRDDFunctions.aggregateByKey`
   * or `PairRDDFunctions.reduceByKey` will provide much better performance.
   */
  def groupBy[K](f: T => K)(implicit kt: ClassTag[K]): RDD[(K, Iterable[T])] = withScope {
    groupBy[K](f, defaultPartitioner(this))
  }

  /**
   * Return an RDD of grouped elements. Each group consists of a key and a sequence of elements
   * mapping to that key. The ordering of elements within each group is not guaranteed, and
   * may even differ each time the resulting RDD is evaluated.
   *
   * @note This operation may be very expensive. If you are grouping in order to perform an
   * aggregation (such as a sum or average) over each key, using `PairRDDFunctions.aggregateByKey`
   * or `PairRDDFunctions.reduceByKey` will provide much better performance.
   */
  def groupBy[K](
      f: T => K,
      numPartitions: Int)(implicit kt: ClassTag[K]): RDD[(K, Iterable[T])] = withScope {
    groupBy(f, new HashPartitioner(numPartitions))
  }
```