
RDD.scala

```scala
  /**
   * Aggregate the elements of each partition, and then the results for all the partitions, using a
   * given associative function and a neutral "zero value". The function
   * op(t1, t2) is allowed to modify t1 and return it as its result value to avoid object
   * allocation; however, it should not modify t2.
   * 使用一个函数和中立的"0值"，先聚合每个分区的元素，再聚合所有分区。
   *
   * 函数 op(t1, t2) 可以修改t1值，将它作为返回值返回，避免了对象分配，然而不要修改 t2
   * 
   * This behaves somewhat differently from fold operations implemented for non-distributed
   * collections in functional languages like Scala. This fold operation may be applied to
   * partitions individually, and then fold those results into the final result, rather than
   * apply the fold to each element sequentially in some defined ordering. For functions
   * that are not commutative, the result may differ from that of a fold applied to a
   * non-distributed collection.
   * 这与Scala等函数式语言中，为非分布式集合实现的fold操作有些不同。
   *
   * 这个fold可以分别应用到的分区上，再fold这些结果，成最终结果。
   * 而不是按顺序作用在每个元素上。
   *
   * 如果使用了非commutative的函数，得到的结果可能和应用到非分布式集合上得到的结果不同。
   * 
   * @param zeroValue the initial value for the accumulated result of each partition for the `op`
   *                  operator, and also the initial value for the combine results from different
   *                  partitions for the `op` operator - this will typically be the neutral
   *                  element (e.g. `Nil` for list concatenation or `0` for summation)
   *                 初始0值不仅用在每个分区内的累积，也是在合并分区间结果时的初始值。
   *                 Nil：列表连接，0：加法
   * @param op an operator used to both accumulate results within a partition and combine results
   *                  from different partitions
   */
  def fold(zeroValue: T)(op: (T, T) => T): T = withScope {
    // Clone the zero value since we will also be serializing it as part of tasks
    // 把0值序列化成一个字节数组，这样，在每个分区上就可以获得一个0值的副本
    var jobResult = Utils.clone(zeroValue, sc.env.closureSerializer.newInstance())
    val cleanOp = sc.clean(op)
    // 使用 op 计算每个分区，使用了0值
    val foldPartition = (iter: Iterator[T]) => iter.fold(zeroValue)(cleanOp)
    // 使用 op 合并各分区，使用了0值
    val mergeResult = (_: Int, taskResult: T) => jobResult = op(jobResult, taskResult)
    sc.runJob(this, foldPartition, mergeResult)
    jobResult
  }
```

```scala
def fold[A1 >: A](z: A1)(op: (A1, A1) => A1): A1 = foldLeft(z)(op)

def foldLeft[B](z: B)(op: (B, A) => B): B = {
  //avoid the LazyRef as we don't have an @eager object
  class folder extends AbstractFunction1[A, Unit] {
    var result = z
    override def apply(v1: A): Unit = result = op(result,v1)
  }
  val folder = new folder
  this foreach folder
  folder.result
}
```