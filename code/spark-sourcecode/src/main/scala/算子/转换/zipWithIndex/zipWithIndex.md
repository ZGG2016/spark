
```scala
  /**
 * Zips this RDD with its element indices. The ordering is first based on the partition index
 * and then the ordering of items within each partition. So the first item in the first
 * partition gets index 0, and the last item in the last partition receives the largest index.
 * 组合RDD和它的元素索引
 *
 * 顺序是首先根据分区索引，然后根据每个分区中的项的顺序。
 * 所以，【第一个分区的第一项索引是0，最后一个分区的最后一项索引是最大的索引】。
 *  
 * This is similar to Scala's zipWithIndex but it uses Long instead of Int as the index type.
 * This method needs to trigger a spark job when this RDD contains more than one partitions.
 * 和scala的zipWithIndex类似。但它使用Long作为索引类型，而不是Int
 * 【当这个RDD包含多于一个分区时，这个方法需要触发一个spark job】 （在计算每个分区的开始索引时）
 * 
 * @note Some RDDs, such as those returned by groupBy(), do not guarantee order of
 * elements in a partition. The index assigned to each element is therefore not guaranteed,
 * and may even change if the RDD is reevaluated. If a fixed ordering is required to guarantee
 * the same index assignments, you should sort the RDD with sortByKey() or save it to a file.
 * groupBy()这种算子返回的RDD，不能保证分区内的元素顺序。
 * 分配给元素的索引的顺序也不能被保证。如果RDD被重新计算，也会还会变化。
 *
 * 如果想要一个确定的顺序，来保证相同的索引，需要使用sortByKey() 排序RDD，或者存入文件。
 */
def zipWithIndex(): RDD[(T, Long)] = withScope {
  // 1.1节
  new ZippedWithIndexRDD(this)
}

/**
 * Zips this RDD with generated unique Long ids. Items in the kth partition will get ids k, n+k,
 * 2*n+k, ..., where n is the number of partitions. So there may exist gaps, but this method
 * won't trigger a spark job, which is different from [[org.apache.spark.rdd.RDD#zipWithIndex]].
 * 用一个唯一的 Long 类型的 ID 和 RDD 组合。
 *
 * 第k个分区的项获得的ID是k、n+k、2*n+k...，n是分区的数量
 *
 * 所以，索引间不连续，但这个方法【不会触发 spark job】. 
 * 
 * @note Some RDDs, such as those returned by groupBy(), do not guarantee order of
 * elements in a partition. The unique ID assigned to each element is therefore not guaranteed,
 * and may even change if the RDD is reevaluated. If a fixed ordering is required to guarantee
 * the same index assignments, you should sort the RDD with sortByKey() or save it to a file.
 */
def zipWithUniqueId(): RDD[(T, Long)] = withScope {
  val n = this.partitions.length.toLong
  this.mapPartitionsWithIndex { case (k, iter) => // k=partition index, iterator
    // 对每个分区的所有数据操作，返回一个迭代器，给每条数据一个id
    Utils.getIteratorZipWithIndex(iter, 0L)
         .map { case (item, i) =>
              (item, i * n + k)
            }
  }
}
```
```scala
  /**
   * Generate a zipWithIndex iterator, avoid index value overflowing problem
   * in scala's zipWithIndex
   */
  def getIteratorZipWithIndex[T](iter: Iterator[T], startIndex: Long): Iterator[(T, Long)] = {
    new Iterator[(T, Long)] {
      require(startIndex >= 0, "startIndex should be >= 0.")
      var index: Long = startIndex - 1L
      def hasNext: Boolean = iter.hasNext
      def next(): (T, Long) = {
        index += 1L
        (iter.next(), index)
      }
    }
  }
```

## 1.1 ZippedWithIndexRDD

```scala
private[spark]
class ZippedWithIndexRDDPartition(val prev: Partition, val startIndex: Long)
  extends Partition with Serializable {
  override val index: Int = prev.index
}

/**
 * Represents an RDD zipped with its element indices. The ordering is first based on the partition
 * index and then the ordering of items within each partition. So the first item in the first
 * partition gets index 0, and the last item in the last partition receives the largest index.
 *
 * @param prev parent RDD
 * @tparam T parent RDD item type
 */
private[spark]
class ZippedWithIndexRDD[T: ClassTag](prev: RDD[T]) extends RDD[(T, Long)](prev) {

  // startIndices(x.index) 计算每个分区的开始索引 
  // 第一个分区的第一项是0，第二个分区的第一项是 0+第一个分区的元素数量 
  /** The start index of each partition. */
  @transient private val startIndices: Array[Long] = {
    val n = prev.partitions.length
    if (n == 0) {
      Array.empty
    } else if (n == 1) {
      Array(0L)
    } else {
      prev.context.runJob(
        prev,
        Utils.getIteratorSize _, // 得到分区大小
        0 until n - 1 // do not need to count the last partition
      )
        // 0 + 分区大小 -> 得到下一个分区的开始索引
        .scanLeft(0L)(_ + _)
    }
  }

  override def getPartitions: Array[Partition] = {
    firstParent[T].partitions.map(x => new ZippedWithIndexRDDPartition(x, startIndices(x.index)))
  }

  override def getPreferredLocations(split: Partition): Seq[String] =
    firstParent[T].preferredLocations(split.asInstanceOf[ZippedWithIndexRDDPartition].prev)

  override def compute(splitIn: Partition, context: TaskContext): Iterator[(T, Long)] = {
    val split = splitIn.asInstanceOf[ZippedWithIndexRDDPartition]
    val parentIter = firstParent[T].iterator(split.prev, context)
    Utils.getIteratorZipWithIndex(parentIter, split.startIndex)
  }
}
```