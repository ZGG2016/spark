
PairRDDFunctions.scala

```scala
  /**
   * Aggregate the values of each key, using given combine functions and a neutral "zero value".
   * This function can return a different result type, U, than the type of the values in this RDD,
   * V. Thus, we need one operation for merging a V into a U and one operation for merging two U's,
   * as in scala.TraversableOnce. The former operation is used for merging values within a
   * partition, and the latter is used for merging values between partitions. To avoid memory
   * allocation, both of these functions are allowed to modify and return their first argument
   * instead of creating a new U.
   * 使用聚合函数和一个中性的"0值"，聚合每个key对应的values。
   *
   * 这个函数会返回的结果类型U和rdd的value类型V不同。
   *
   * 因此，需要一个操作将V合并到U，再使用另一个操作合并两个U。
   *
   * 前一个操作用来在一个分区内合并value，后一个操作用来在分区间合并value.
   *
   * 为避免内存分配，这两个函数被允许修改、返回第一个参数，而不是创建一个新的U。
   *
   * 【0值由用户指定，相同 key 的第一个数据和初始值进行分区内计算，分区内计算规则和分区间计算规则不同】   
   */
  def aggregateByKey[U: ClassTag](zeroValue: U, partitioner: Partitioner)(seqOp: (U, V) => U,
      combOp: (U, U) => U): RDD[(K, U)] = self.withScope {
    // Serialize the zero value to a byte array so that we can get a new clone of it on each key
    //把0值序列化成一个ByteBuffer，这样，在每个分区上就可以获得一个0值的副本
    val zeroBuffer = SparkEnv.get.serializer.newInstance().serialize(zeroValue)
    //创建字节数组，大小为zeroBuffer的大小
    val zeroArray = new Array[Byte](zeroBuffer.limit)
    // 将此缓冲区中的字节传输到给定的目标数组中
    zeroBuffer.get(zeroArray)

    lazy val cachedSerializer = SparkEnv.get.serializer.newInstance()
    // 将前面的字节数组放进缓存，再反序列化
    val createZero = () => cachedSerializer.deserialize[U](ByteBuffer.wrap(zeroArray))

    // We will clean the combiner closure later in `combineByKey`
    val cleanedSeqOp = self.context.clean(seqOp)
    // 调用 combineByKeyWithClassTag 实现聚合
    // (v: V) => cleanedSeqOp(createZero(), v) 创建初始0值（针对combineByKeyWithClassTag）的函数
    combineByKeyWithClassTag[U]((v: V) => cleanedSeqOp(createZero(), v),
      cleanedSeqOp, combOp, partitioner)
  }

  // 使用 指定分区数 的哈希分区器
  def aggregateByKey[U: ClassTag](zeroValue: U, numPartitions: Int)(seqOp: (U, V) => U,
         combOp: (U, U) => U): RDD[(K, U)] = self.withScope {
    aggregateByKey(zeroValue, new HashPartitioner(numPartitions))(seqOp, combOp)
  }
  // 使用默认分区器，如果配置了默认分区数，就使用默认的，否则，就使用这个RDD的分区数
  def aggregateByKey[U: ClassTag](zeroValue: U)(seqOp: (U, V) => U,
         combOp: (U, U) => U): RDD[(K, U)] = self.withScope {
    aggregateByKey(zeroValue, defaultPartitioner(self))(seqOp, combOp)
 }
```