

RDD.scala

```scala
/**
 * Return a new RDD that has exactly numPartitions partitions.
 * 改变当前rdd的分区数，到numPartitions值。
 *
 * Can increase or decrease the level of parallelism in this RDD. Internally, this uses
 * a shuffle to redistribute data.
 * 可增大，可减少。
 * 在内部，它使用 shuffle 来重新分配数据
 * 
 * If you are decreasing the number of partitions in this RDD, consider using `coalesce`,
 * which can avoid performing a shuffle.
 * 如果想要减少分区数，优先使用coalesce，可避免shuffle
 */
def repartition(numPartitions: Int)(implicit ord: Ordering[T] = null): RDD[T] = withScope {
  coalesce(numPartitions, shuffle = true)
}
```
