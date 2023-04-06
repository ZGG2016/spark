PairRDDFunctions.scala

```scala
  /**
   * Return the list of values in the RDD for key `key`. This operation is done efficiently if the
   * RDD has a known partitioner by only searching the partition that the key maps to.
   * 在 RDD 中，根据 key 取values，返回 值的列表。 
   *
   * 如果 RDD 已分区，那么通过 key 搜索到的对应分区，这个操作就可以高效地完成。
   *  【直接从key所在的分区上找】
   */
  def lookup(key: K): Seq[V] = self.withScope {
    self.partitioner match {
      case Some(p) =>
        // 根据key取到所在分区
        val index = p.getPartition(key)
        val process = (it: Iterator[(K, V)]) => {
          val buf = new ArrayBuffer[V]
          // 判断元素的key是否等于传入的key，若等于就添加到 ArrayBuffer 里
          for (pair <- it if pair._1 == key) {
            buf += pair._2
          }
          buf.toSeq
        } : Seq[V]
        val res = self.context.runJob(self, process, Array(index))
        res(0)
      case None =>
        // 先过滤出符合条件的，再取出其 value
        self.filter(_._1 == key).map(_._2).collect()
    }
  }
```