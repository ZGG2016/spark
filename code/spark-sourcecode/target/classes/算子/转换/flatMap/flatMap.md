RDD.scala
```scala
 /**
 *  Return a new RDD by first applying a function to all elements of this
 *  RDD, and then flattening the results.
 *  首先将函数应用在RDD的所有元素上，然后将结果展平，返回一个新的RDD
 */
def flatMap[U: ClassTag](f: T => TraversableOnce[U]): RDD[U] = withScope {
  val cleanF = sc.clean(f)
  new MapPartitionsRDD[U, T](this, (_, _, iter) => iter.flatMap(cleanF))
}
```

```scala
  /** Creates a new iterator by applying a function to all values produced by this iterator
 *  and concatenating the results.
 *
 *  @param f the function to apply on each element.
 *  @return  the iterator resulting from applying the given iterator-valued function
 *           `f` to each value produced by this iterator and concatenating the results.
 *  @note    Reuse: $consumesAndProducesIterator
 */
def flatMap[B](f: A => GenTraversableOnce[B]): Iterator[B] = new AbstractIterator[B] {
  private var cur: Iterator[B] = empty
  private def nextCur(): Unit = { cur = null ; cur = f(self.next()).toIterator }
  def hasNext: Boolean = {
    // Equivalent to cur.hasNext || self.hasNext && { nextCur(); hasNext }
    // but slightly shorter bytecode (better JVM inlining!)
    while (!cur.hasNext) {
      if (!self.hasNext) return false
      nextCur()
    }
    true
  }
  def next(): B = (if (hasNext) cur else empty).next()
}
```