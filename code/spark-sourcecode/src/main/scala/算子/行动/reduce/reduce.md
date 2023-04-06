
RDD.scala

```scala
  /**
   * Reduces the elements of this RDD using the specified commutative and
   * associative binary operator.
   * 使用二元运算符聚合 RDD 中的元素
   */
  def reduce(f: (T, T) => T): T = withScope {
    val cleanF = sc.clean(f)
    // 分区内操作
    // 定义一个 reducePartition 方法，使用f，依次操作每个分区内的数据
    val reducePartition: Iterator[T] => Option[T] = iter => {
      if (iter.hasNext) {
        // 1.1节
        Some(iter.reduceLeft(cleanF))
      } else {
        None
      }
    }
    var jobResult: Option[T] = None
    // 分区间操作
    // 合并每个分区的结果   _: Int --> 分区id   taskResult --> 每个分区的计算结果
    val mergeResult = (_: Int, taskResult: Option[T]) => {
      /*
         如果当前分区有计算结果(taskResult)，且匹配到已有的最终结果(jobResult)，就对分区结果和最终结果执行 方法f 。否则就返回当前结果
         或者这么理解：一开始jobResult是None，所以执行第二个case，后面再匹配的时候，就有值了，就追加到前面的结果
       */
      if (taskResult.isDefined) {
        jobResult = jobResult match {
          case Some(value) => Some(f(value, taskResult.get))
          case None => taskResult
        }
      }
    }
    // 1.2节
    sc.runJob(this, reducePartition, mergeResult)
    // Get the final result out of our Option, or throw an exception if the RDD was empty
    jobResult.getOrElse(throw SparkCoreErrors.emptyCollectionError())
  }
```

## 1.1 reduceLeft
```scala
 /** 
   * 应用二元运算符从左到右地作用每个元素上。
   * 
   * Applies a binary operator to all elements of this $coll,
   *  going left to right.
   *  $willNotTerminateInf
   *  $orderDependentFold
   *
   *  @param  op    the binary operator.
   *  @tparam  B    the result type of the binary operator.  返回值类型
   *  @return  the result of inserting `op` between consecutive elements of this $coll,
   *           going left to right:
   *           {{{
   *             op( op( ... op(x_1, x_2) ..., x_{n-1}), x_n)
   *           }}}
   *           where `x,,1,,, ..., x,,n,,` are the elements of this $coll.
   *
   *  1，2，3，4.  应用加法  
   *  1+2=3
   *  3+3=6
   *  6+4=10  结果为10
   *
   *  @throws UnsupportedOperationException if this $coll is empty.   */
  def reduceLeft[B >: A](op: (B, A) => B): B = {
    if (isEmpty)
      throw new UnsupportedOperationException("empty.reduceLeft")

    var first = true

    //确定一个初始0值，类型是返回值类型B
    var acc: B = 0.asInstanceOf[B]

    for (x <- self) {
      if (first) {  //如果是第一个值的话，直接复制给结果acc
        acc = x
        first = false
      }
      else acc = op(acc, x)
    }
    acc
  }
```

## 1.2 runJob

```scala
  /**
   * 在RDD的所有分区上运行一个job，并将结果传递给handler function
   *
   * Run a job on all partitions in an RDD and pass the results to a handler function.
   *
   * @param rdd target RDD to run tasks on
   * @param processPartition a function to run on each partition of the RDD
   * @param resultHandler callback to pass each result to
   */
  def runJob[T, U: ClassTag](
      rdd: RDD[T],
      processPartition: Iterator[T] => U, // 分区内操作
      resultHandler: (Int, U) => Unit)  // 分区间操作
  {
  	//函数作用在每个分区上
    val processFunc = (context: TaskContext, iter: Iterator[T]) => processPartition(iter)
    runJob[T, U](rdd, processFunc, 0 until rdd.partitions.length, resultHandler)
  }
```
```