
RDD.scala

```scala
  /**
   * Return the number of elements in the RDD.
   * 统计rdd中元素的个数，结果为Long类型 
   */
  // 先算每个分区的个数，再加一起
  def count(): Long = sc.runJob(this, Utils.getIteratorSize _).sum
```

```scala
  /**
   * Run a job on all partitions in an RDD and return the results in an array.
   * 在RDD上的每个分区上作用这个函数，计算结果以数组的形式返回
   * 
   * @param rdd target RDD to run tasks on
   *            任务运行在的目标 RDD
   * @param func a function to run on each partition of the RDD
   *             运行在 RDD 的每个分区上的函数
   * @return in-memory collection with a result of the job (each collection element will contain
   * a result from one partition)
   *         具有 job 结果的 内存中的集合（每个集合元素将包含来自一个分区的结果）
   */
  def runJob[T, U: ClassTag](rdd: RDD[T], func: Iterator[T] => U): Array[U] = {
    runJob(rdd, func, 0 until rdd.partitions.length)
  }

    /**
     * Run a function on a given set of partitions in an RDD and return the results as an array.
     *
     * @param rdd target RDD to run tasks on
     * @param func a function to run on each partition of the RDD
     * @param partitions set of partitions to run on; some jobs may not want to compute on all
     * partitions of the target RDD, e.g. for operations like `first()`
     * @return in-memory collection with a result of the job (each collection element will contain
     * a result from one partition)
     */
    def runJob[T, U: ClassTag](
                                rdd: RDD[T],
                                func: Iterator[T] => U,
                                partitions: Seq[Int]): Array[U] = {
      val cleanedFunc = clean(func)
      // (ctx: TaskContext, it: Iterator[T]) => cleanedFunc(it) 对每个分区操作
      runJob(rdd, (ctx: TaskContext, it: Iterator[T]) => cleanedFunc(it), partitions)
    }
```

```scala
 /**
   * 统计一个迭代器中的元素的个数
   *
   * Counts the number of elements of an iterator using a while loop rather than calling
   * [[scala.collection.Iterator#size]] because it uses a for loop, which is slightly slower
   * in the current version of Scala.
   */
  def getIteratorSize(iterator: Iterator[_]): Long = {
    var count = 0L
    while (iterator.hasNext) {
      count += 1L
      iterator.next()
    }
    count
  }
```

```scala
def sum[B >: A](implicit num: Numeric[B]): B = foldLeft(num.zero)(num.plus)

  //这里的(z: B)是0
  //(op: (B, A) => B)是加法运算，把A加到B上，返回B
  def foldLeft[B](z: B)(op: (B, A) => B): B = {
    var result = z
    this foreach (x => result = op(result, x))  //循环相加
    result
  }

  def zero = fromInt(0)  
  def fromInt(x: Int): T

  def plus(x: T, y: T): T
  def +(rhs: T) = plus(lhs, rhs)
```