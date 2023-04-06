
RDD.scala

```scala
  /**
   * Return this RDD sorted by the given key function.
   * 按照给定的 key 函数排序这个 RDD, 返回一个新的 RDD
   */
  /*
     参数：
        - 一个函数，该函数的输入是 T 类型(泛型)，返回值类型为K；
        - ascending，决定排序后的RDD是升序还是降序，默认是true，即升序；
        - numPartitions，决定排序后的RDD的分区个数，默认排序后的分区个数和排序之前的个数相等，即为this.partitions.size。
   */
  def sortBy[K](
      f: (T) => K,
      ascending: Boolean = true,
      numPartitions: Int = this.partitions.length)
      (implicit ord: Ordering[K], ctag: ClassTag[K]): RDD[T] = withScope {
    // 先转成键值对形式的RDD
    this.keyBy[K](f)
        .sortByKey(ascending, numPartitions)
        .values
  }
```

```scala
  /**
   * Creates tuples of the elements in this RDD by applying `f`.
   * 将传进来的每个元素作用于 f(x) 中，并返回tuples类型的元素，也就变成了Key-Value类型的RDD
   */
  def keyBy[K](f: T => K): RDD[(K, T)] = withScope {
    val cleanedF = sc.clean(f)
    // 经 f(x) 转换的元素为 key
    map(x => (cleanedF(x), x))
  }
```

```sh
scala> val rdd = sc.parallelize(List(1,2,3,4))
rdd: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[21] at parallelize at <console>:24

#  括号里的就是参数f
scala> rdd.sortBy(x => x).collect()
res11: Array[Int] = Array(1, 2, 3, 4)

scala> rdd.sortBy(x => x%3).collect()
res12: Array[Int] = Array(3, 4, 1, 2)

scala> rdd.keyBy(x=>x%3).collect()
res5: Array[(Int, Int)] = Array((1,1), (2,2), (0,3), (1,4))

scala> val srdd = rdd.keyBy(x=>x%3)
srdd: org.apache.spark.rdd.RDD[(Int, Int)] = MapPartitionsRDD[6] at keyBy at <console>:25

scala> srdd.sortByKey().collect()
res7: Array[(Int, Int)] = Array((0,3), (1,1), (1,4), (2,2))


scala> rdd.sortByKey().collect()
<console>:26: error: value sortByKey is not a member of org.apache.spark.rdd.RDD[Int]
       rdd.sortByKey().collect()
```

```sh
scala> val prdd = sc.parallelize(List(("a",3),("b",2),("c",1)))
prdd: org.apache.spark.rdd.RDD[(String, Int)] = ParallelCollectionRDD[10] at parallelize at <console>:24

scala> prdd.collect()
res11: Array[(String, Int)] = Array((a,3), (b,2), (c,1))

scala> prdd.sortBy(x=>x._2).collect()
res14: Array[(String, Int)] = Array((c,1), (b,2), (a,3))

scala> val sprdd = prdd.keyBy(x=>x._2).collect()
prdd: Array[(Int, (String, Int))] = Array((3,(a,3)), (2,(b,2)), (1,(c,1)))

scala> prdd.sortByKey().collect()
res15: Array[(String, Int)] = Array((a,3), (b,2), (c,1))
```

## 2、sortByKey、sortBy区别与联系

sortByKey

	由 PairRDD 调用；
	有两个参数：升降序、分区数；
	根据key排序

sortBy

	由 RDD 或 PairRDD 调用；
	有三个参数：函数、升降序、分区数;
	通过参数f函数可以实现自定义排序方式

都默认：升序、排序后的RDD的分区数和排序前的RDD的分区数相同

sortBy 实际调用了 sortByKey 排序的。