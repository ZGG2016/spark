
RDD.scala

```scala
  /**
   * Take the first num elements of the RDD. It works by first scanning one partition, and use the
   * results from that partition to estimate the number of additional partitions needed to satisfy
   * the limit.
   * 取 RDD 的前 num 个元素
   * 它首先扫描一个分区，然后使用该分区的结果来估计还需要取其他分区的元素数量达到满足数量
   * 【先从第一个分区中取数据，如果能取到 num 个元素，就结束，如果不能，就再取下一个分区的数据】
   * @note This method should only be used if the resulting array is expected to be small, as
   * all the data is loaded into the driver's memory.
   * 适合结果数组的数据量少的情况，因为它会把数据全部都载入driver内存
   * 
   * @note Due to complications in the internal implementation, this method will raise
   * an exception if called on an RDD of `Nothing` or `Null`.
   * 如果RDD是空的，方法会抛异常
   */
  def take(num: Int): Array[T] = withScope {
    // 默认是4  scaleUpFactor=4
    val scaleUpFactor = Math.max(conf.get(RDD_LIMIT_SCALE_UP_FACTOR), 2) 
    // 返回空数组
    if (num == 0) {
      new Array[T](0)
    } else {
      val buf = new ArrayBuffer[T]
      // 取这个RDD有多少个分区
      val totalParts = this.partitions.length
      var partsScanned = 0
      // 缓存区大小小于要取的数量，且 现在要扫描的分区索引没超过总分区数量 
      // 0 < 10(num) && 0 < 5(totalParts)
      // 7 < 10(num) && 1 < 5(totalParts)
      // 10 = 10(num) && 2 < 5(totalParts) 跳出循环
      while (buf.size < num && partsScanned < totalParts) {
        // The number of partitions to try in this iteration. It is ok for this number to be
        // greater than totalParts because we actually cap it at totalParts in runJob.
        // 在本次迭代中，要尝试的分区的数量。这个数量大于 totalParts 是可以的e
        var numPartsToTry = 1L
        // 10 = 10 - 0  还需要再读10个元素
        // 3 = 10 - 7  还需要再读3个元素
        val left = num - buf.size
        // partsScanned=1
        if (partsScanned > 0) {
          // If we didn't find any rows after the previous iteration, quadruple and retry.
          // Otherwise, interpolate the number of partitions we need to try, but overestimate
          // it by 50%. We also cap the estimation in the end.
          // 如果在前一次迭代之后没有找到任何行，则重复四次并重试。
          // 否则，插入我们需要尝试的分区数量，但将其高估50%。我们还在最后对估计进行了限制。
          if (buf.isEmpty) {
            numPartsToTry = partsScanned * scaleUpFactor
          } else {
            // As left > 0, numPartsToTry is always >= 1
            // 1 = 1.5*3*1 / 7 
            numPartsToTry = Math.ceil(1.5 * left * partsScanned / buf.size).toInt
            // 1 = Math.min(1,1*4)
            numPartsToTry = Math.min(numPartsToTry, partsScanned * scaleUpFactor)
          }
        }

        // 下面要操作的分区 0
        // 下面要操作的分区 1
        val p = partsScanned.until(math.min(partsScanned + numPartsToTry, totalParts).toInt)
        //  取分区0的前10个元素，假设分区0只有7个元素，所以就只能取这7个元素
        // 取分区1的前3个元素
        val res = sc.runJob(this, (it: Iterator[T]) => it.take(left).toArray, p)
        // 将这个7个元素放入缓存区
        // 将这个3个元素放入缓存区
        res.foreach(buf ++= _.take(num - buf.size))
        // 接下来，操作分区1了
        // 接下来，操作分区2了
        partsScanned += p.size
      }

      buf.toArray
    }
  }
```