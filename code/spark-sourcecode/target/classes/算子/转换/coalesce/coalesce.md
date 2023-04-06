
coalesce的作用是减少rdd的分区到 `numPartitions` 个数。

默认shuffle = false。

如果减少的分区过大，需要设置shuffle = true

想要合并到更多的分区，需要设置shuffle = true（如，100到1000）

RDD.scala

```scala
  /**
   * Return a new RDD that is reduced into `numPartitions` partitions.
   * 返回一个新的 RDD, 它的分区数减少到 `numPartitions` 个
   * 
   * This results in a narrow dependency, e.g. if you go from 1000 partitions
   * to 100 partitions, there will not be a shuffle, instead each of the 100
   * new partitions will claim 10 of the current partitions. If a larger number
   * of partitions is requested, it will stay at the current number of partitions.
   * 这会导致一个窄依赖。例如，如果从1000减少到100个分区，不会shuffle。
   *     相反，这100个新分区中的每个分区将占用当前分区中的10个。
   *
   * 如果设置了一个比当前分区数更大的一个值，分区数将会保持不变。
   * 
   * However, if you're doing a drastic coalesce, e.g. to numPartitions = 1,
   * this may result in your computation taking place on fewer nodes than
   * you like (e.g. one node in the case of numPartitions = 1). To avoid this,
   * you can pass shuffle = true. This will add a shuffle step, but means the
   * current upstream partitions will be executed in parallel (per whatever
   * the current partitioning is).
   * 如果减少的分区数过大，如，直接减少到1个，那么会导致你的计算只使用少量的节点
   *                                    (numPartitions = 1会使用一个节点)
   *  为了避免这种情况，可以设置 shuffle = true。这会添加shuffle步骤，
   *  意味着当前上游分区将并行执行(无论当前分区是什么)。
   *  
   * @note With shuffle = true, you can actually coalesce to a larger number
   * of partitions. This is useful if you have a small number of partitions,
   * say 100, potentially with a few partitions being abnormally large. Calling
   * coalesce(1000, shuffle = true) will result in 1000 partitions with the
   * data distributed using a hash partitioner. The optional partition coalescer
   * passed in must be serializable.
   * 设置shuffle = true，可以合并到更多的分区。
   * 如果您只有少量的分区(比如100个)，而其中一些分区可能异常大，那么这将非常有用。
   * 调用 coalesce(1000, shuffle = true) 将产生1000个分区，使用 hash partitioner 分发数据。
   *
   * 传入的 partitionCoalescer 参数必须是可序列化的
   * partitionCoalescer：定义了如何合并给定RDD的分区
   * partitionCoalescer：how to coalesce the partitions of a given RDD
   */
  def coalesce(numPartitions: Int, shuffle: Boolean = false,
               partitionCoalescer: Option[PartitionCoalescer] = Option.empty)
              (implicit ord: Ordering[T] = null)
      : RDD[T] = withScope {
    require(numPartitions > 0, s"Number of partitions ($numPartitions) must be positive.")
    if (shuffle) {
      /** 
       * Distributes elements evenly across output partitions, starting from a random partition.
       * 从随机分区开始，将元素均匀分布到各个输出分区。
       * */
      // 定义一个针对分区操作的方法  (index: Int, items: Iterator[T]) index为分区索引，item包含了分区中的数据
      // 随机取一个分区，对分区里的每个元素加一个key，输出 (元素位置，元素内容) 形式  
      val distributePartition = (index: Int, items: Iterator[T]) => {
        // 随机选一个分区。一个分区的开始位置
        var position = new Random(hashing.byteswap32(index)).nextInt(numPartitions)
        // 迭代分区内元素
        items.map { t =>
          // Note that the hash code of the key will just be the key itself. The HashPartitioner
          // will mod it with the number of total partitions.
          position = position + 1
          //每个分区中的元素格式：(元素位置，元素内容)
          (position, t)
        }
      } : Iterator[(Int, T)]  

      // include a shuffle step so that our upstream tasks are still distributed
      // 因为需要shuffle，所以上游任务仍需要被分发，所以需要 new ShuffledRDD
      // 1.1节 基于 ShuffledRDD，创建一个 CoalescedRDD 
      new CoalescedRDD(
        // 1.2节  基于mapPartitionsWithIndexInternal返回的RDD，创建一个 ShuffledRDD 
        new ShuffledRDD[Int, T, T](
          // 1.3节  使用 distributePartition 方法作用在每个分区上，返回一个RDD
          mapPartitionsWithIndexInternal(distributePartition, isOrderSensitive = true),
          new HashPartitioner(numPartitions)),
        numPartitions,
        partitionCoalescer).values // 去掉key，即元素位置
    } else {
      // 不做shuffle
      new CoalescedRDD(this, numPartitions, partitionCoalescer)
    }
  }
```
## 1.1 CoalescedRDD

```scala
/**
 * Represents a coalesced RDD that has fewer partitions than its parent RDD
 * This class uses the PartitionCoalescer class to find a good partitioning of the parent RDD
 * so that each new partition has roughly the same number of parent partitions and that
 * the preferred location of each new partition overlaps with as many preferred locations of its
 * parent partitions
 * 这个 RDD 具有比它父 RDD 更少的分区。
 * 这个类使用 PartitionCoalescer 找到一个好的父 RDD 的分区，
 * 为了使用每个新分区的父分区的数量大致相同，每个新分区的偏好位置和它的父分区的偏好位置重叠。
 * @param prev RDD to be coalesced
 * @param maxPartitions number of desired partitions in the coalesced RDD (must be positive)
 * @param partitionCoalescer [[PartitionCoalescer]] implementation to use for coalescing
 */
private[spark] class CoalescedRDD[T: ClassTag](
    @transient var prev: RDD[T],
    maxPartitions: Int,
    partitionCoalescer: Option[PartitionCoalescer] = None)
  extends RDD[T](prev.context, Nil) {}
```

## 1.2 ShuffledRDD

```scala
/**
 * :: DeveloperApi ::
 * The resulting RDD from a shuffle (e.g. repartitioning of data).
 * 一个 shuffle 产生的 RDD（例如，数据的重分区）
 * 
 * @param prev the parent RDD.
 * @param part the partitioner used to partition the RDD
 * @tparam K the key class.
 * @tparam V the value class.
 * @tparam C the combiner class.
 */
// TODO: Make this return RDD[Product2[K, C]] or have some way to configure mutable pairs
@DeveloperApi
class ShuffledRDD[K: ClassTag, V: ClassTag, C: ClassTag](
         @transient var prev: RDD[_ <: Product2[K, V]],
         part: Partitioner)
  extends RDD[(K, C)](prev.context, Nil) {}
```

## 1.3 mapPartitionsWithIndexInternal

```scala
  /**
   * [performance] Spark's internal mapPartitionsWithIndex method that skips closure cleaning.
   * It is a performance API to be used carefully only if we are sure that the RDD elements are
   * serializable and don't require closure cleaning.
   * 一个跳过闭包清理的内部方法。
   * 只有在我们确信RDD元素是可序列化的，并且不需要清理闭包的情况下，才应该谨慎使用这个性能API。   
   * @param preservesPartitioning indicates whether the input function preserves the partitioner,
   *                              which should be `false` unless this is a pair RDD and the input
   *                              function doesn't modify the keys.
   *                              表示输入函数是否保留分区器，应设置为false，除非这是一个 pair RDD，且输入函数不修改 keys                              
   * @param isOrderSensitive whether or not the function is order-sensitive. If it's order
   *                         sensitive, it may return totally different result when the input order
   *                         is changed. Mostly stateful functions are order-sensitive.
   *                         函数是否对顺序敏感。如果是，那么当输入顺序变化了，计算结果就会完全不同。
   *                         大多数状态函数是顺序敏感  
   */
  private[spark] def mapPartitionsWithIndexInternal[U: ClassTag](
      f: (Int, Iterator[T]) => Iterator[U], // Int 为分区索引，Iterator[T] 包含了分区中的数据
      preservesPartitioning: Boolean = false,
      isOrderSensitive: Boolean = false): RDD[U] = withScope {
    // 将函数作用在父rdd的每个分区产生的rdd
    new MapPartitionsRDD(
      // 父rdd  调用这个方法的rdd
      this, 
      // 使用 参数f 作用在每个分区上
      (_: TaskContext, index: Int, iter: Iterator[T]) => f(index, iter),
      preservesPartitioning = preservesPartitioning,
      isOrderSensitive = isOrderSensitive)
  }
```