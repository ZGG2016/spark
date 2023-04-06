
PairRDDFunctions.scala

```scala
  /**
   * Generic function to combine the elements for each key using a custom set of aggregation
   * functions. Turns an RDD[(K, V)] into a result of type RDD[(K, C)], for a "combined type" C
   * 个通用函数，使用一系列自定义的聚合函数合并每个 key 的元素。
   * RDD[(K, V)] ---> RDD[(K, C)]  C是一个聚合类型
   * 
   * Users provide three functions:
   *
   *  - `createCombiner`, which turns a V into a C (e.g., creates a one-element list)
   *  - `mergeValue`, to merge a V into a C (e.g., adds it to the end of a list)
   *  - `mergeCombiners`, to combine two C's into a single one.
   * 用户提供了三种函数：
   *   - createCombiner ： 把一个 V 变成一个 C。（如：创建一个单元素的列表） 【它的结果类型就是最终的结果类型】
   *   - mergeValue ： 将一个 V 合并到一个 C 中。（如：追加到列表尾）
   *   - mergeCombiners ： 两个 C 聚合成一个。
   *   
   * In addition, users can control the partitioning of the output RDD, and whether to perform
   * map-side aggregation (if a mapper can produce multiple items with the same key).
   * 用户可以控制 输出 RDD 的分区、在 map 端是否执行聚合操作 (在mapper端同一key产生多个项的情况)
   * 【比如reduceByKey有一个分区的参数】、【mapSideCombine参数】
   * 
   * @note V and C can be different -- for example, one might group an RDD of type
   * (Int, Int) into an RDD of type (Int, Seq[Int]).
   *  V 和 C 的类型可以不同 -- 例如，分组一个 (Int, Int) RDD 成 (Int, Seq[Int]) 类型.
   * 【groupByKey产生的效果，value变成了一个序列】
   */
  def combineByKeyWithClassTag[C](
      createCombiner: V => C,
      mergeValue: (C, V) => C,
      mergeCombiners: (C, C) => C,
      partitioner: Partitioner,    // 控制 输出 RDD 的分区
      mapSideCombine: Boolean = true,  // 在 map 端是否执行本地合并操作
      serializer: Serializer = null)(implicit ct: ClassTag[C]): RDD[(K, C)] = self.withScope {
    require(mergeCombiners != null, "mergeCombiners must be defined") // required as of Spark 0.9.0

    // 判断 key 是不是数组类型 
    // 如果是数组类型，就不能执行 map 端聚合，也不能使用 HashPartitioner
    if (keyClass.isArray) {
      // 默认 true，数组类型的 key 不能本地聚合
      if (mapSideCombine) {
        throw SparkCoreErrors.cannotUseMapSideCombiningWithArrayKeyError()
      }
      // HashPartitioner 不能分区数组类型的 key
      if (partitioner.isInstanceOf[HashPartitioner]) {
        throw SparkCoreErrors.hashPartitionerCannotPartitionArrayKeyError()
      }
    }
    // 1.1节 包含了聚合的数据的函数
    val aggregator = new Aggregator[K, V, C](
      self.context.clean(createCombiner),
      self.context.clean(mergeValue),
      self.context.clean(mergeCombiners))
    
    // 如果 调用这个算子的rdd的分区数 和 这个算子的输出RDD的分区数 相同，就不 shuffle 了，
    if (self.partitioner == Some(partitioner)) {
      // 对每个分区，将分区数据直接写入内存或溢写磁盘文件，等待另一端读取
      self.mapPartitions(iter => {
        val context = TaskContext.get()
        /*
            InterruptibleIterator: 一个封装了杀死任务的迭代器
            aggregator.combineValuesByKey: 
              将 Aggregator 中传入的聚合函数作用在分区里的数据上，再插入这个 ExternalAppendOnlyMap 里，期间可能会有溢写操作
         */
        new InterruptibleIterator(context, aggregator.combineValuesByKey(iter, context))
      },
        // 表示输入函数是否保留分区器，保留的话就使用父RDD的分区器
        preservesPartitioning = true)
    } 
    // 如果不同，就执行shuffle
    else {
      new ShuffledRDD[K, V, C](self, partitioner)
        .setSerializer(serializer)
        .setAggregator(aggregator)
        .setMapSideCombine(mapSideCombine)
    }
  }

    /**
     * Simplified version of combineByKeyWithClassTag that hash-partitions the output RDD.
     */
    def combineByKeyWithClassTag[C](
                                     createCombiner: V => C,
                                     mergeValue: (C, V) => C,
                                     mergeCombiners: (C, C) => C,
                                     numPartitions: Int)(implicit ct: ClassTag[C]): RDD[(K, C)] = self.withScope {
      combineByKeyWithClassTag(createCombiner, mergeValue, mergeCombiners,
        new HashPartitioner(numPartitions))
    }
```

## 1.1 new Aggregator()

```scala
/**
 * :: DeveloperApi ::
 * A set of functions used to aggregate data.
 * 用来聚合数据的函数的集合
 * 
 * @param createCombiner function to create the initial value of the aggregation.
 *                       创建聚合的初始值的函数
 * @param mergeValue function to merge a new value into the aggregation result.
 *                   将一个新值合并到聚合结果的函数
 * @param mergeCombiners function to merge outputs from multiple mergeValue function.
 *                       合并从多个 mergeValue 函数的输出的函数
 */
@DeveloperApi
case class Aggregator[K, V, C] (
    createCombiner: V => C,
    mergeValue: (C, V) => C,
    mergeCombiners: (C, C) => C) {
  
      def combineValuesByKey(
              iter: Iterator[_ <: Product2[K, V]],
              context: TaskContext): Iterator[(K, C)] = {
        val combiners = new ExternalAppendOnlyMap[K, V, C](createCombiner, mergeValue, mergeCombiners)
        // 将传入的聚合函数作用在分区里的数据上，再插入这个map里，期间，当内存不够时，会溢写磁盘文件
        combiners.insertAll(iter)
        updateMetrics(context, combiners)
        combiners.iterator
      }
}
```