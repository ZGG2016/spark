
PairRDDFunctions.scala

```scala
  /**
   * Return the key-value pairs in this RDD to the master as a Map.
   * 将 RDD 中的键值对以 map 的形式返回给 master
 * 
   * Warning: this doesn't return a multimap (so if you have multiple values to the same key, only
   *          one value per key is preserved in the map returned)
   * 注意：如果一个 key 有多个 value，map 中只会取其中一个
   * 
   * @note this method should only be used if the resulting data is expected to be small, as
   * all the data is loaded into the driver's memory.
   * 这个方法只有在结果的数据量不大的情况下使用。因为所有的数据都会载入到driver内存。 
   */
  def collectAsMap(): Map[K, V] = self.withScope {
    // 先调用 RDD.scala 中的 collect，将数据拉取回来
    val data = self.collect()
    // 再转成 hashmap
    val map = new mutable.HashMap[K, V]
    // 设置map的大小
    map.sizeHint(data.length)
    // 遍历数据，放入map
    data.foreach { pair => map.put(pair._1, pair._2) }
    map
  }
```