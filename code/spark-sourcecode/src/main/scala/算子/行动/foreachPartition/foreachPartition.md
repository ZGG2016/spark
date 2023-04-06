RDD.scala

```scala
  /**
   * Applies a function f to each partition of this RDD.
   * 应用一个函数到这个RDD的每个分区上
   * 
   * 这里 (f: Iterator[T] => Unit)的 Iterator[T] 的 f 作用的对象
   * 
   * 因为没有返回值并且是action操作，所以一般用在程序末尾，
   *    比如，要将数据存储到mysql、es或hbase等存储系统中
   */
def foreachPartition(f: Iterator[T] => Unit): Unit = withScope {
  val cleanF = sc.clean(f)
  sc.runJob(this, (iter: Iterator[T]) => cleanF(iter)) // 直接作用到分区
}
```