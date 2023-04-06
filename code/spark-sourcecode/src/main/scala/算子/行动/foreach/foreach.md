RDD.scala

```scala
  /**
   * Applies a function f to all elements of this RDD.
   * 应用一个函数到这个RDD的所有元素上
   * 这里 (f: T => Unit) 的 T 是 f 作用的对象
   */
  def foreach(f: T => Unit): Unit = withScope {
    val cleanF = sc.clean(f)
    // 对每个分区的元素应用 f 函数
    sc.runJob(this, (iter: Iterator[T]) => iter.foreach(cleanF))
  }
```

```scala
 /** Applies a function `f` to all values produced by this iterator.
   *
   *  @param  f   the function that is applied for its side-effect to every element.
   *              The result of function `f` is discarded. 函数结果被丢弃
   *
   *  @tparam  U  the type parameter describing the result of function `f`.
   *              This result will always be ignored. Typically `U` is `Unit`,
   *              but this is not necessary.
   *              描述了函数 f  结果的类型参数。这个结果会被忽略
   *  @note    Reuse: $consumesIterator
   *
   *  @usecase def foreach(f: A => Unit): Unit
   *    @inheritdoc
   *    scala中的函数
   */
  def foreach[U](f: A => U) { while (hasNext) f(next()) }
```