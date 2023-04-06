
PairRDDFunctions.scala

```scala
  /**
   * Merge the values for each key using an associative function and a neutral "zero value" which
   * may be added to the result an arbitrary number of times, and must not change the result
   * (e.g., Nil for list concatenation, 0 for addition, or 1 for multiplication.).
   * 使用一个函数和一个中性的"0值"合并每个 key 对应的 values
   * "0值"可以添加到结果中任意次数，但不能改变结果。
   * 对于0值 --> Nil：列表连接，0：加法，1：乘法
   * 
   * 【0值由用户指定(Nil 0 1)，相同 key 的第一个数据和初始值进行分区内计算，分区内计算规则和分区间计算规则相同】
   */
  def foldByKey(
      zeroValue: V,
      partitioner: Partitioner)(func: (V, V) => V): RDD[(K, V)] = self.withScope {
    // Serialize the zero value to a byte array so that we can get a new clone of it on each key
    // 把0值序列化成一个字节数组，这样，在每个分区上就可以获得一个0值的副本
    val zeroBuffer = SparkEnv.get.serializer.newInstance().serialize(zeroValue)
    // 创建字节数组，大小为zeroBuffer的大小
    val zeroArray = new Array[Byte](zeroBuffer.limit)
    // 将此缓冲区中的字节传输到给定的目标数组中
    zeroBuffer.get(zeroArray)

    // When deserializing, use a lazy val to create just one instance of the serializer per task
    lazy val cachedSerializer = SparkEnv.get.serializer.newInstance()
    // 将前面的字节数组放进缓存，再反序列化
    val createZero = () => cachedSerializer.deserialize[V](ByteBuffer.wrap(zeroArray))

    val cleanedFunc = self.context.clean(func)
    // 调用 combineByKeyWithClassTag 实现聚合
    // (v: V) => cleanedSeqOp(createZero(), v) 创建初始0值（针对combineByKeyWithClassTag）的函数
    combineByKeyWithClassTag[V]((v: V) => cleanedFunc(createZero(), v),
      cleanedFunc, cleanedFunc, partitioner)
  }

  /**
   * Merge the values for each key using an associative function and a neutral "zero value" which
   * may be added to the result an arbitrary number of times, and must not change the result
   * (e.g., Nil for list concatenation, 0 for addition, or 1 for multiplication.).
   */
  def foldByKey(zeroValue: V, numPartitions: Int)(func: (V, V) => V): RDD[(K, V)] = self.withScope {
    foldByKey(zeroValue, new HashPartitioner(numPartitions))(func)
  }

  /**
   * Merge the values for each key using an associative function and a neutral "zero value" which
   * may be added to the result an arbitrary number of times, and must not change the result
   * (e.g., Nil for list concatenation, 0 for addition, or 1 for multiplication.).
   */
  def foldByKey(zeroValue: V)(func: (V, V) => V): RDD[(K, V)] = self.withScope {
    foldByKey(zeroValue, defaultPartitioner(self))(func)
  }
```