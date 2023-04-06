SparkContext.scala

```scala
  /**
   * Read a text file from HDFS, a local file system (available on all nodes), or any
   * Hadoop-supported file system URI, and return it as an RDD of Strings.
   * The text files must be encoded as UTF-8.
   * 读取文本文件，从HDFS、本地文件系统(所有结点可访问)或任意hadoop支持的文件系统URI
   * 文本文件必须是 UTF-8 编码
   * 
   * @param path path to the text file on a supported file system
   * @param minPartitions suggested minimum number of partitions for the resulting RDD
   * @return RDD of lines of the text file 文本文件行的RDD
   */
  def textFile(
      path: String,
      minPartitions: Int = defaultMinPartitions): RDD[String] = withScope {
    assertNotStopped()
    hadoopFile(path, classOf[TextInputFormat], classOf[LongWritable], classOf[Text],
      minPartitions)
      // 文本文件行转字符串
      .map(pair => pair._2.toString).setName(path)
  }

//def defaultMinPartitions: Int = math.min(defaultParallelism, 2)
```

```scala
 /** 
   * 使用任意的 InputFormat ，从hadoop file中获取RDD.
   *
   * Get an RDD for a Hadoop file with an arbitrary InputFormat
   *
   * 因为Hadoop's RecordReader会重复使用相同的Writable对象处理每条记录。
   * 所以，直接缓存返回的RDD、或直接传给一个聚合操作或shuffle操作，会创建相同对象的多个引用。
   *
   * 如果你计划直接缓存、排序、聚合Hadoop writable对象，你应该先使用map函数复制它们。
   * 
   * @note Because Hadoop's RecordReader class re-uses the same Writable object for each
   * record, directly caching the returned RDD or directly passing it to an aggregation or shuffle
   * operation will create many references to the same object.
   * If you plan to directly cache, sort, or aggregate Hadoop writable objects, you should first
   * copy them using a `map` function.
   * @param path directory to the input data files, the path can be comma separated paths
   * as a list of inputs
   * @param inputFormatClass storage format of the data to be read
   * @param keyClass `Class` of the key associated with the `inputFormatClass` parameter
   * @param valueClass `Class` of the value associated with the `inputFormatClass` parameter
   * @param minPartitions suggested minimum number of partitions for the resulting RDD
   * @return RDD of tuples of key and corresponding value  元组形式的RDD
   */
  def hadoopFile[K, V](
      path: String,
      inputFormatClass: Class[_ <: InputFormat[K, V]],
      keyClass: Class[K],
      valueClass: Class[V],
      minPartitions: Int = defaultMinPartitions): RDD[(K, V)] = withScope {
    assertNotStopped()

    // 强制加载hdfs-site.xml
    // This is a hack to enforce loading hdfs-site.xml.
    // See SPARK-11227 for details.
    FileSystem.getLocal(hadoopConfiguration)

    // A Hadoop configuration can be about 10 KB, which is pretty big, so broadcast it.
    //广播hadoop配置
    val confBroadcast = broadcast(new SerializableConfiguration(hadoopConfiguration))
    //设置输入路径
    val setInputPathsFunc = (jobConf: JobConf) => FileInputFormat.setInputPaths(jobConf, path)

    new HadoopRDD(
      this,
      confBroadcast,
      Some(setInputPathsFunc),
      inputFormatClass,
      keyClass,
      valueClass,
      minPartitions).setName(path)
  }

```