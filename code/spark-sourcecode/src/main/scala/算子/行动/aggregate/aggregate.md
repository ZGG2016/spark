
RDD.scala

```scala
  /**
   * Aggregate the elements of each partition, and then the results for all the partitions, using
   * given combine functions and a neutral "zero value". This function can return a different result
   * type, U, than the type of this RDD, T. Thus, we need one operation for merging a T into an U
   * and one operation for merging two U's, as in scala.TraversableOnce. Both of these functions are
   * allowed to modify and return their first argument instead of creating a new U to avoid memory
   * allocation.
   * 使用一个聚合函数和一个中性的"0值"，聚合每个分区的元素，再聚合这些分区的结果。
   *
   * 这个函数会返回的结果的类型U和rdd的T类型不同。【zeroValue的类型就是返回值的类型】
   *
   * 因此，需要一个操作，将T合并到U里，再使用另一个操作合并两个U。 
   *
   * 为避免内存分配，这两个函数被允许修改、返回第一个参数，而不是创建一个新的U。
   * 
   * @param zeroValue the initial value for the accumulated result of each partition for the
   *                  `seqOp` operator, and also the initial value for the combine results from
   *                  different partitions for the `combOp` operator - this will typically be the
   *                  neutral element (e.g. `Nil` for list concatenation or `0` for summation)
   *                  初始0值不仅用在每个分区内的累积，也是在合并分区间结果时的初始值。
   *                   Nil：列表连接，0：加法
   * @param seqOp an operator used to accumulate results within a partition
   *              用来在一个分区内累积结果的操作
   * @param combOp an associative operator used to combine results from different partitions
   *               用来合并来自不同分区结果的联合操作
   */
  def aggregate[U: ClassTag](zeroValue: U)(seqOp: (U, T) => U, combOp: (U, U) => U): U = withScope {
    // Clone the zero value since we will also be serializing it as part of tasks
    // 克隆一个0值，将会作为任务的一部分序列化它
    // clone：使用Spark serializer克隆一个对象.（先序列化，再反序列化）
    var jobResult = Utils.clone(zeroValue, sc.env.serializer.newInstance())
    val cleanSeqOp = sc.clean(seqOp)
    val cleanCombOp = sc.clean(combOp)
    // 1.1节  执行seqOp，实际调用了foldLeft方法：对传来的每一个元素，使用op操作新元素和0值，再返回更新后的0值
    val aggregatePartition = (it: Iterator[T]) => it.aggregate(zeroValue)(cleanSeqOp, cleanCombOp)
    // 执行combOp：使用combOp操作0值和每个分区的计算结果
    val mergeResult = (_: Int, taskResult: U) => jobResult = combOp(jobResult, taskResult)
    // runJob：Run a job on all partitions in an RDD and pass the results to a handler function.
    sc.runJob(this, aggregatePartition, mergeResult)
    jobResult
  }
```

## 1.1 it.aggregate

TraversableOnce.scala

```scala
def aggregate[B](z: =>B)(seqop: (B, A) => B, combop: (B, B) => B): B = foldLeft(z)(seqop)

// 对传来的每一个元素v1，使用op操作v1和z，z即为这里的0值，再返回更新后的z
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