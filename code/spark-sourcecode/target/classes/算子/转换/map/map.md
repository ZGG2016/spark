RDD.scala
```scala
  /**
   * Return a new RDD by applying a function to all elements of this RDD.
   * 将函数应用在RDD的所有元素上，返回一个新的RDD
   */
  def map[U: ClassTag](f: T => U): RDD[U] = withScope {
    val cleanF = sc.clean(f)
    new MapPartitionsRDD[U, T](this, (_, _, iter) => iter.map(cleanF))
  }
```

```scala
  /** Creates a new iterator that maps all produced values of this iterator
   *  to new values using a transformation function.
   *
   *  @param f  the transformation function
   *  @return a new iterator which transforms every value produced by this
   *          iterator by applying the function `f` to it.
   *  @note   Reuse: $consumesAndProducesIterator
   */
  def map[B](f: A => B): Iterator[B] = new AbstractIterator[B] {
    def hasNext = self.hasNext
    def next() = f(self.next())
  }
```