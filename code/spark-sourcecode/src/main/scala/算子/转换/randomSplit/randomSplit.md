

```scala
  /**
   * Randomly splits this RDD with the provided weights.
   * 根据提供的权重，随机划分RDD
   * 
   * @param weights weights for splits, will be normalized if they don't sum to 1 
   *                如果权重和不为1，将被标准化
   * @param seed random seed
   *
   * @return split RDDs in an array   
   *           返回值: `Array[RDD[T]]`
   */
  def randomSplit(
      weights: Array[Double],
      seed: Long = Utils.random.nextLong): Array[RDD[T]] = {

    // 权重必须大于等于0，其和要大于0
    require(weights.forall(_ >= 0),
      s"Weights must be nonnegative, but got ${weights.mkString("[", ",", "]")}")
    require(weights.sum > 0,
      s"Sum of weights must be positive, but got ${weights.mkString("[", ",", "]")}")

    withScope {
      val sum = weights.sum
      // 标准化权重
      val normalizedCumWeights = weights.map(_ / sum).scanLeft(0.0d)(_ + _)
      // 定义一个滑动窗口，窗口大小就是2
      normalizedCumWeights.sliding(2).map { x => // 对每个权重，应用在所有分区上
        // 不放回随机抽样
        randomSampleWithRange(x(0), x(1), seed)
      }.toArray
    }
  }

/**
 * Internal method exposed for Random Splits in DataFrames. Samples an RDD given a probability
 * range.
 * 给定一个概率范围，对RDD抽样（伯努利抽样器）
 * 
 * @param lb lower bound to use for the Bernoulli sampler
 * @param ub upper bound to use for the Bernoulli sampler
 * @param seed the seed for the Random number generator
 * @return A random sub-sample of the RDD without replacement. 不放回抽样
 */
private[spark] def randomSampleWithRange(lb: Double, ub: Double, seed: Long): RDD[T] = {

  // 在每个分区中，定义一个抽样器，分别抽样
  this.mapPartitionsWithIndex( { (index, partition) =>
    val sampler = new BernoulliCellSampler[T](lb, ub)
    sampler.setSeed(seed + index)
    sampler.sample(partition)
  }, isOrderSensitive = true, preservesPartitioning = true)
}
```

```scala
  def scanLeft[B, That](z: B)(op: (B, A) => B)(implicit bf: CanBuildFrom[Repr, B, That]): That = {
    val b = bf(repr)
    b.sizeHint(this, 1)
    var acc = z
    b += acc
    for (x <- this) { acc = op(acc, x); b += acc }   //更新acc，放进b中
    b.result
  }
```

```scala
  /** Groups elements in fixed size blocks by passing a "sliding window"
   *  over them (as opposed to partitioning them, as is done in `grouped`.)
   *  The "sliding window" step is set to one.
   *  @see [[scala.collection.Iterator]], method `sliding`
   *
   *  @param size the number of elements per group
   *  @return An iterator producing ${coll}s of size `size`, except the
   *          last element (which may be the only element) will be truncated
   *          if there are fewer than `size` elements remaining to be grouped.
   */
  def sliding(size: Int): Iterator[Repr] = sliding(size, 1)

```