PairRDDFunctions.scala

```scala
/**
 * Save this RDD as a text file, using string representations of elements.
 * 以 text file 的形式存储这个RDD。    【参数是路径，不是文件名称】
 *
 * 元素类型是字符串
 *
 * 底层调用 saveAsHadoopFile 方法，只不过 key 是 NullWritable
 */
def saveAsTextFile(path: String): Unit = withScope {
  saveAsTextFile(path, null)
}

/**
 * Save this RDD as a compressed text file, using string representations of elements.
 * 将这个 RDD 保存为一个压缩text file
 */
def saveAsTextFile(path: String, codec: Class[_ <: CompressionCodec]): Unit = withScope {
  // 先转键值对形式
  this.mapPartitions { iter =>
    val text = new Text()
    iter.map { x =>
      require(x != null, "text files do not allow null rows")
      text.set(x.toString)
      (NullWritable.get(), text)
    }
  }.saveAsHadoopFile[TextOutputFormat[NullWritable, Text]](path, codec)
}
```