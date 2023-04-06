
在 `01_写流程-02.md` 中，了解了各个实现类，接下来分析具体的写方法。

## 2.2 writer.write()

### 2.2.1 SortShuffleWriter.write()

```scala
private[spark] class SortShuffleWriter[K, V, C](
    handle: BaseShuffleHandle[K, V, C],
    mapId: Long,
    context: TaskContext,
    shuffleExecutorComponents: ShuffleExecutorComponents)
  extends ShuffleWriter[K, V] with Logging {

  /** 
   * Write a bunch of records to this task's output 
   * 将一堆记录写入到这个任务的输出
   * records的 key 是 和ShuffleMapTask关联的rdd的分区 ， value是 TaskContext
   * */
  override def write(records: Iterator[Product2[K, V]]): Unit = {
    // mapSideCombine: 默认是false
    sorter = if (dep.mapSideCombine) {
      // 2.2.1.1节 【写入内存（排序、聚合）  溢写文件（排序、聚合）  合并溢写文件（排序、聚合）  删除中间文件】            
      new ExternalSorter[K, V, C](
        context, dep.aggregator, Some(dep.partitioner), dep.keyOrdering, dep.serializer)
    } else {
      // In this case we pass neither an aggregator nor an ordering to the sorter, because we don't
      // care whether the keys get sorted in each partition; that will be done on the reduce side
      // if the operation being run is sortByKey.
      /*
          在这种情况下，既不传递一个聚合器，也不传递一个排序器 给sorter, 因为我们不关心每个分区中的 key 是否有序；
          如果将要执行的操作是sortByKey, 这将在 reduce 端完成。 
       */
      // 【写入内存  溢写文件  合并溢写文件  删除中间文件】      
      new ExternalSorter[K, V, V](
        context, aggregator = None, Some(dep.partitioner), ordering = None, dep.serializer)
    }
    // 2.2.1.1节  将分区中的数据放入 map或buffer中（map里有一个可以排序的迭代器方法），再读取它们，溢写到磁盘文件中  （ExternalSorter里有一个comparator）
    sorter.insertAll(records)

    // Don't bother including the time to open the merged output file in the shuffle write time,
    // because it just opens a single file, so is typically too fast to measure accurately
    // (see SPARK-3570).
    // 为每个map任务调用一次，创建一个ShuffleMapOutputWriter, 它负责持久化该map任务写入的所有分区字节
    val mapOutputWriter = shuffleExecutorComponents.createMapOutputWriter(
      dep.shuffleId, mapId, dep.partitioner.numPartitions)
    // 2.2.1.2节 先合并溢写文件中的数据和内存中的数据(排序)，返回的迭代器，再遍历这个迭代器，通过partitionWriter将数据写入到一个序列化流
    sorter.writePartitionedMapOutput(dep.shuffleId, mapId, mapOutputWriter)
    // 提交所有 partitionWriter 完成的写入操作，返回每个分区写入的字节数  （具体实现分析见 `写流程-03-03-BypassMergeSortShuffleWriter的write方法.md`）
    partitionLengths = mapOutputWriter.commitAllPartitions(sorter.getChecksums).getPartitionLengths
    mapStatus = MapStatus(blockManager.shuffleServerId, partitionLengths, mapId)
  }
}
```

#### 2.2.1.1 new ExternalSorter()

```scala
/**
 * Sorts and potentially merges a number of key-value pairs of type (K, V) to produce key-combiner
 * pairs of type (K, C). Uses a Partitioner to first group the keys into partitions, and then
 * optionally sorts keys within each partition using a custom Comparator. Can output a single
 * partitioned file with a different byte range for each partition, suitable for shuffle fetches.
 * 排序并合并类型为(K, V)的键值对，产生类型为(K, C)的 key-combiner 对。
 * 使用一个分区器，首先将键分组到分区中，然后可选地，使用自定义的 Comparator 排序每个分区中的键。
 * 会输出一个分区的文件，这个文件的每个分区会有不同字节的范围，方便 shuffle 获取。
 * 
 * If combining is disabled, the type C must equal V -- we'll cast the objects at the end.
 * 如果启用了聚合，类型C必须等于V -- 我们将在最后对对象进行类型转换
 * 
 * Note: Although ExternalSorter is a fairly generic sorter, some of its configuration is tied
 * to its use in sort-based shuffle (for example, its block compression is controlled by
 * `spark.shuffle.compress`).  We may need to revisit this if ExternalSorter is used in other
 * non-shuffle contexts where we might want to use different configuration settings.
 * 注意：尽管 ExternalSorter 是一个通用 sorter, 它的一些配置和基于排序的shuffle是联系在一起的（例如，它的块压缩是由 `spark.shuffle.compress` 控制的）
 * 如果 ExternalSorter 用在其他的非shuffle上下文（我们可能想要使用不同的配置项）中，我们可能需要再次访问这个【？】。
 * 
 * @param aggregator optional Aggregator with combine functions to use for merging data
 *                   可选的聚合器，其聚合函数用来合并数据
 * @param partitioner optional Partitioner; if given, sort by partition ID and then key
 *                    可选的分区器，如果给出了，由分区id排序，然后再根据key排序
 * @param ordering optional Ordering to sort keys within each partition; should be a total ordering
 *                 可选的排序器，为排序每个分区中的 keys, 应该是一个总的排序器
 * @param serializer serializer to use when spilling to disk
 *                   当溢写磁盘的时候，使用的序列化器
 *
 * Note that if an Ordering is given, we'll always sort using it, so only provide it if you really
 * want the output keys to be sorted. In a map task without map-side combine for example, you
 * probably want to pass None as the ordering to avoid extra sorting. On the other hand, if you do
 * want to do combining, having an Ordering is more efficient than not having it.
 * 注意：如果给出了一个排序器，就会使用它来排序。所以，如果你真的想要输出的 keys 是有序的，就提供它。
 *      在一个没有 map 端预聚合的 map 任务中，你可能想要传一个 None 作为排序器，为了避免额外的排序。
 *      另一方面，如果你想预聚合，有一个排序器是更有效率的。             
 *                   
 * Users interact with this class in the following way:
 * 用户和这个类交互有以下几种方式：
 * 1. 实例化一个 ExternalSorter 对象
 * 2. 调用 insertAll() 方法，参数是记录的集合
 * 3. 返回一个 iterator(), 来遍历 有序的/聚合的记录
 *    或
 *    调用 writePartitionedFile() 方法，来创建一个包含有序的/聚合的输出的文件，它可以在 Spark's sort shuffle 中使用。
 *                   
 * 1. Instantiate an ExternalSorter.
 *
 * 2. Call insertAll() with a set of records.
 *
 * 3. Request an iterator() back to traverse sorted/aggregated records.
 *     - or -
 *    Invoke writePartitionedFile() to create a file containing sorted/aggregated outputs
 *    that can be used in Spark's sort shuffle.
 *
 * At a high level, this class works internally as follows:
 * 在较高的层次上，这个类的内部工作如下:
 *       
 *  - We repeatedly fill up buffers of in-memory data, using either a PartitionedAppendOnlyMap if
 *    we want to combine by key, or a PartitionedPairBuffer if we don't.
 *    Inside these buffers, we sort elements by partition ID and then possibly also by key.
 *    To avoid calling the partitioner multiple times with each key, we store the partition ID
 *    alongside each record.
 *    - 我们重复地填满内存中的缓存，如果我们想按键聚合，就使用 PartitionedAppendOnlyMap, 否则就使用 PartitionedPairBuffer.
 *      在这些缓存中，我们先按照分区id排序，再可能按照key排序。
 *      为了避免对每个键调用分区器多次，我们为每条记录存储分区id. 
 *                   
 *  - When each buffer reaches our memory limit, we spill it to a file. This file is sorted first
 *    by partition ID and possibly second by key or by hash code of the key, if we want to do
 *    aggregation. For each file, we track how many objects were in each partition in memory, so we
 *    don't have to write out the partition ID for every element.
 *    - 当每个缓存达到它的内存极限时，我们就将其溢写到一个文件。这个文件首先按分区id排序、然后按键排序或按键的哈希值排序，如果想要聚合的话。
 *      对于每个文件，会跟踪内存中每个分区里的对象的数量，所以，我们并不需要为每个元素写分区id. 
 *                   
 *  - When the user requests an iterator or file output, the spilled files are merged, along with
 *    any remaining in-memory data, using the same sort order defined above (unless both sorting
 *    and aggregation are disabled). If we need to aggregate by key, we either use a total ordering
 *    from the ordering parameter, or read the keys with the same hash code and compare them with
 *    each other for equality to merge values.
 *    - 当用户请求一个迭代器或文件输出时，溢出的文件和内存中剩余的数据将使用上面定义的相同排序顺序合并(除非同时禁用排序和聚合)。
 *      如果需要按键聚合，我们要么从排序器参数中使用总的排序器，或者读取具有相同哈希值的键，并互相比较它们，相等就合并值。
 *                   
 *  - Users are expected to call stop() at the end to delete all the intermediate files.
 *    - 在最后，需要调用 stop() 方法，来删除所有的中间文件。               
 */
private[spark] class ExternalSorter[K, V, C](
    context: TaskContext,
    aggregator: Option[Aggregator[K, V, C]] = None,
    partitioner: Option[Partitioner] = None,
    ordering: Option[Ordering[K]] = None,
    serializer: Serializer = SparkEnv.get.serializer)
  extends Spillable[WritablePartitionedPairCollection[K, C]](context.taskMemoryManager())
  with Logging with ShuffleChecksumSupport {

      // Data structures to store in-memory objects before we spill. Depending on whether we have an
      // Aggregator set, we either put objects into an AppendOnlyMap where we combine them, or we
      // store them in an array buffer.
      /*
          用来，在溢写前，存储内存中对象的数据结构。
          取决于，是否设置了 Aggregator, 设置了就使用 AppendOnlyMap，否则就使用 array buffer
       */
      // 2.2.1.1.1节  它封装了一个 map, 这个 map 中，key 是 （分区id, key）的元组
      @volatile private var map = new PartitionedAppendOnlyMap[K, C]
      // 2.2.1.1.2节  仅能追加的键值对形式的缓存区
      @volatile private var buffer = new PartitionedPairBuffer[K, C]

      // 将分区中的数据放入 map或buffer中，再读取它们，溢写到磁盘文件中
      // records 的 key 是 和ShuffleMapTask关联的rdd的分区 ， value是 TaskContext
      def insertAll(records: Iterator[Product2[K, V]]): Unit = {
        // TODO: stop combining if we find that the reduction factor isn't high
        val shouldCombine = aggregator.isDefined
    
        // 判断要不要做 map 端的聚合
        if (shouldCombine) {
          // Combine values in-memory first using our AppendOnlyMap
          // 分区内聚合函数
          val mergeValue = aggregator.get.mergeValue
          // 创建聚合初始值的函数
          val createCombiner = aggregator.get.createCombiner
          // records(kv)的 key 是 和ShuffleMapTask关联的rdd的分区 ， value是 TaskContext
          var kv: Product2[K, V] = null
          // 使用 分区内聚合函数 执行聚合
          val update = (hadValue: Boolean, oldValue: C) => {
            if (hadValue) mergeValue(oldValue, kv._2) else createCombiner(kv._2)
          }
          // 遍历含有数据的迭代器
          while (records.hasNext) {
            // 从上次溢写结束后，每从输入读取一条记录，就将计数(_elementsRead)加1
            addElementsRead()
            kv = records.next()
            // 2.2.1.1.3节  使用updateFunc更新key的值  
            // key 是 （分区id, key）的元组  value是封装了 分区内聚合函数 的函数 
            // 这个 map 里有一个返回迭代器的方法，它会对数据排序
            map.changeValue((getPartition(kv._1), kv._1), update)
            // 2.2.1.1.4节  
            // 随着数据的进入，判断要不要溢写，如果要溢写，就获取更多内存，并读取内存集合中的数据，溢写到磁盘文件中
            maybeSpillCollection(usingMap = true)
          }
        } else {
          // Stick values into our buffer
          while (records.hasNext) {
            addElementsRead()
            val kv = records.next()
            // 2.2.1.1.5节   不聚合的话，就直接插入buffer里
            buffer.insert(getPartition(kv._1), kv._1, kv._2.asInstanceOf[C])
            maybeSpillCollection(usingMap = false)
          }
        }
      }
}
```

##### 2.2.1.1.1 PartitionedAppendOnlyMap

```scala
/**
 * Implementation of WritablePartitionedPairCollection that wraps a map in which the keys are tuples
 * of (partition ID, K)
 * 封装了一个 map, 这个 map 中，key 是 （分区id, key）的元组
 */
private[spark] class PartitionedAppendOnlyMap[K, V]
  extends SizeTrackingAppendOnlyMap[(Int, K), V] with WritablePartitionedPairCollection[K, V] {
      
      // 返回一个迭代器，内部会对数据排序
      def partitionedDestructiveSortedIterator(keyComparator: Option[Comparator[K]])
      : Iterator[((Int, K), V)] = {
        val comparator = keyComparator.map(partitionKeyComparator).getOrElse(partitionComparator)
        destructiveSortedIterator(comparator)
      }
}
```

##### 2.2.1.1.2 PartitionedPairBuffer

```scala
/**
 * Append-only buffer of key-value pairs, each with a corresponding partition ID, that keeps track
 * of its estimated size in bytes.
 * 仅能追加的键值对形式的缓存区，每个键值对都有一个对应的分区ID，以字节为单位跟踪其估计大小。【？】
 * The buffer can support up to 1073741819 elements.
 */
private[spark] class PartitionedPairBuffer[K, V](initialCapacity: Int = 64)
  extends WritablePartitionedPairCollection[K, V] with SizeTracker {}
```

##### 2.2.1.1.3 map.changeValue()

```scala
/**
 * An append-only map that keeps track of its estimated size in bytes.
 * 仅能追加的map, 它追踪其估计的大小，以字节形式
 */
private[spark] class SizeTrackingAppendOnlyMap[K, V]
  extends AppendOnlyMap[K, V] with SizeTracker{

  override def changeValue(key: K, updateFunc: (Boolean, V) => V): V = {
    // 使用updateFunc更新key的值
    val newValue = super.changeValue(key, updateFunc)
    super.afterUpdate()
    newValue
  }
}
```

##### 2.2.1.1.4 maybeSpillCollection()

ExternalSorter.scala

```scala
/**
   * Spill the current in-memory collection to disk if needed.
   * 将当前内存中的集合溢写到磁盘
   * 
   * @param usingMap whether we're using a map or buffer as our current in-memory collection
   *                是否使用一个 map 或缓存作为我们当前内存中的集合
   */
  private def maybeSpillCollection(usingMap: Boolean): Unit = {
    var estimatedSize = 0L
    // 使用一个 map 作为我们当前内存中的集合
    if (usingMap) {
      // 估计的内存中的集合的当前大小
      estimatedSize = map.estimateSize()
      // 2.2.1.1.4.1节 根据已读取数据的大小和内存的大小，判断要不要溢写，如果要溢写，就获取更多内存，并读取内存集合中的数据，开始真正的溢写
      if (maybeSpill(map, estimatedSize)) {
        // 溢写完，重新 new 一个 map
        map = new PartitionedAppendOnlyMap[K, C]
      }
    } 
    // 使用一个缓存作为我们当前内存中的集合
    else {
      estimatedSize = buffer.estimateSize()
      if (maybeSpill(buffer, estimatedSize)) {
        buffer = new PartitionedPairBuffer[K, C]
      }
    }
    // _peakMemoryUsedBytes: 到目前为止，观察到的内存中数据结构的峰值大小
    if (estimatedSize > _peakMemoryUsedBytes) {
      _peakMemoryUsedBytes = estimatedSize
    }
  }
```

###### 2.2.1.1.4.1 maybeSpill()

Spillable.scala

Spillable 是 ExternalSorter 的父类 

```scala
 /**
   * Spills the current in-memory collection to disk if needed. Attempts to acquire more
   * memory before spilling.
   * 
   * @param collection collection to spill to disk
   * @param currentMemory estimated size of the collection in bytes
   * @return true if `collection` was spilled to disk; false otherwise
   */
  protected def maybeSpill(collection: C, currentMemory: Long): Boolean = {
    var shouldSpill = false
    if (elementsRead % 32 == 0 && currentMemory >= myMemoryThreshold) {
      // Claim up to double our current memory from the shuffle memory pool
      val amountToRequest = 2 * currentMemory - myMemoryThreshold
      val granted = acquireMemory(amountToRequest)
      myMemoryThreshold += granted
      // If we were granted too little memory to grow further (either tryToAcquire returned 0,
      // or we already had more memory than myMemoryThreshold), spill the current collection
      // 申请的内存大小还是不够
      shouldSpill = currentMemory >= myMemoryThreshold
    }
    // 强制溢写阈值
    shouldSpill = shouldSpill || _elementsRead > numElementsForceSpillThreshold
    // Actually spill
    if (shouldSpill) {
      _spillCount += 1
      logSpillage(currentMemory)
      // 将内存集合中的数据溢写到一个有序文件 
      spill(collection)
      _elementsRead = 0
      _memoryBytesSpilled += currentMemory
      // 溢写完释放内存
      releaseMemory()
    }
    shouldSpill
  }
```

查看 ExternalSorter 的 spill 方法

```scala

    private val keyComparator: Comparator[K] = ordering.getOrElse((a: K, b: K) => {
      val h1 = if (a == null) 0 else a.hashCode()
      val h2 = if (b == null) 0 else b.hashCode()
      if (h1 < h2) -1 else if (h1 == h2) 0 else 1
    })
    
    private def comparator: Option[Comparator[K]] = {
      if (ordering.isDefined || aggregator.isDefined) {
        Some(keyComparator)
      } else {
        None
      }
    }

/**
   * Spill our in-memory collection to a sorted file that we can merge later.
   * We add this file into `spilledFiles` to find it later.
   * 将内存集合溢写到一个有序文件，以便后面合并。
   * 
   * @param collection whichever collection we're using (map or buffer)
   */
  override protected[this] def spill(collection: WritablePartitionedPairCollection[K, C]): Unit = {
    // 会对迭代器中的数据排序
    val inMemoryIterator = collection.destructiveSortedWritablePartitionedIterator(comparator)
    // 将迭代器元素写入到 DiskBlockObjectWriter （它 writing JVM objects directly to a file on disk）
    val spillFile = spillMemoryIteratorToDisk(inMemoryIterator)
    // 把创建的溢写文件统一放到一个数组里
    spills += spillFile
  }
```

```scala
 /**
   * Spill contents of in-memory iterator to a temporary file on disk.
   * 将内存迭代器中的内容溢写到磁盘上的一个临时文件 
   */
  private[this] def spillMemoryIteratorToDisk(inMemoryIterator: WritablePartitionedIterator[K, C])
      : SpilledFile = {
    // Because these files may be read during shuffle, their compression must be controlled by
    // spark.shuffle.compress instead of spark.shuffle.spill.compress, so we need to use
    // createTempShuffleBlock here; see SPARK-3426 for more context.
    val (blockId, file) = diskBlockManager.createTempShuffleBlock()

    // These variables are reset after each flush
    var objectsWritten: Long = 0
    val spillMetrics: ShuffleWriteMetrics = new ShuffleWriteMetrics
    // 这里有个缓冲区的大小 fileBufferSize 大小是32k
    val writer: DiskBlockObjectWriter =
      blockManager.getDiskWriter(blockId, file, serInstance, fileBufferSize, spillMetrics)

    // List of batch sizes (bytes) in the order they are written to disk
    val batchSizes = new ArrayBuffer[Long]

    // How many elements we have in each partition
    val elementsPerPartition = new Array[Long](numPartitions)

    // Flush the disk writer's contents to disk, and update relevant variables.
    // The writer is committed at the end of this process.
    def flush(): Unit = {
      val segment = writer.commitAndGet()
      batchSizes += segment.length
      _diskBytesSpilled += segment.length
      objectsWritten = 0
    }

    var success = false
    try {
      // 循环迭代器
      while (inMemoryIterator.hasNext) {
        val partitionId = inMemoryIterator.nextPartition()
        require(partitionId >= 0 && partitionId < numPartitions,
          s"partition Id: ${partitionId} should be in the range [0, ${numPartitions})")
        // 将当前元素写入到 DiskBlockObjectWriter （它 writing JVM objects directly to a file on disk）
        inMemoryIterator.writeNext(writer)
        elementsPerPartition(partitionId) += 1
        objectsWritten += 1

        if (objectsWritten == serializerBatchSize) {
          flush()
        }
      }
      if (objectsWritten > 0) {
        flush()
        writer.close()
      } else {
        writer.revertPartialWritesAndClose()
      }
      success = true
    } finally {
      if (!success) {
        // This code path only happens if an exception was thrown above before we set success;
        // close our stuff and let the exception be thrown further
        writer.closeAndDelete()
      }
    }

    SpilledFile(file, blockId, batchSizes.toArray, elementsPerPartition)
  }
```

##### 2.2.1.1.5 buffer.insert()

PartitionedPairBuffer.scala

```scala
  /** Add an element into the buffer */
  def insert(partition: Int, key: K, value: V): Unit = {
    // 当前缓存大小等于了缓存的容量，就增长数组的大小
    if (curSize == capacity) {
      growArray()
    }
    // 在 2 * curSize 位置放置 partition和key  
    // 在 2 * curSize + 1 位置放置value
    data(2 * curSize) = (partition, key.asInstanceOf[AnyRef])
    data(2 * curSize + 1) = value.asInstanceOf[AnyRef]
    curSize += 1
    afterUpdate()
  }
```

#### 2.2.1.2 sorter.writePartitionedMapOutput()

ExternalSorter.scala

```scala

  private val spills = new ArrayBuffer[SpilledFile]

/**
   * Write all the data added into this ExternalSorter into a map output writer that pushes bytes
   * to some arbitrary backing store. This is called by the SortShuffleWriter.
   * 将添加到这个ExternalSorter中的所有数据写入 map output writer， 
   * 该写入器将字节推送到某个任意的后台存储。这由SortShuffleWriter调用。
   * @return array of lengths, in bytes, of each partition of the file (used by map output tracker)
   */
  def writePartitionedMapOutput(
      shuffleId: Int,
      mapId: Long,
      mapOutputWriter: ShuffleMapOutputWriter): Unit = {
    var nextPartitionId = 0
    // 这里的 spills 就是 前面溢写文件后，返回的 SpilledFile 对象放入的数组
    if (spills.isEmpty) {
      // Case where we only have in-memory data
      val collection = if (aggregator.isDefined) map else buffer
      val it = collection.destructiveSortedWritablePartitionedIterator(comparator)
      while (it.hasNext) {
        val partitionId = it.nextPartition()
        var partitionWriter: ShufflePartitionWriter = null
        var partitionPairsWriter: ShufflePartitionPairsWriter = null
        TryUtils.tryWithSafeFinally {
          partitionWriter = mapOutputWriter.getPartitionWriter(partitionId)
          val blockId = ShuffleBlockId(shuffleId, mapId, partitionId)
          partitionPairsWriter = new ShufflePartitionPairsWriter(
            partitionWriter,
            serializerManager,
            serInstance,
            blockId,
            context.taskMetrics().shuffleWriteMetrics,
            if (partitionChecksums.nonEmpty) partitionChecksums(partitionId) else null)
          while (it.hasNext && it.nextPartition() == partitionId) {
            it.writeNext(partitionPairsWriter)
          }
        } {
          if (partitionPairsWriter != null) {
            partitionPairsWriter.close()
          }
        }
        nextPartitionId = partitionId + 1
      }
    } else {
      // We must perform merge-sort; get an iterator by partition and write everything directly.
      // 执行合并排序
      // 2.2.1.2.1节 partitionedIterator: 合并溢写文件中的数据和内存中的数据，返回的迭代器
      for ((id, elements) <- this.partitionedIterator) {
        val blockId = ShuffleBlockId(shuffleId, mapId, id)
        var partitionWriter: ShufflePartitionWriter = null
        var partitionPairsWriter: ShufflePartitionPairsWriter = null
        TryUtils.tryWithSafeFinally {
          // ShufflePartitionWriter: 打开流，将分区字节持久化到后备数据存储
          partitionWriter = mapOutputWriter.getPartitionWriter(id)
          // ShufflePartitionPairsWriter: 将字节推送到任意partition writer(ShufflePartitionWriter)，而不是通过块管理器写入本地磁盘(DiskBlockObjectWriter )。
          partitionPairsWriter = new ShufflePartitionPairsWriter(
            partitionWriter,
            serializerManager,
            serInstance,
            blockId,
            context.taskMetrics().shuffleWriteMetrics,
            if (partitionChecksums.nonEmpty) partitionChecksums(id) else null)
          if (elements.hasNext) {
            for (elem <- elements) {
              // 写入到一个序列化流，先写key 再写value
              partitionPairsWriter.write(elem._1, elem._2)
            }
          }
        } {
          if (partitionPairsWriter != null) {
            partitionPairsWriter.close()
          }
        }
        nextPartitionId = id + 1
      }
    }
    //...
  }
```

##### 2.2.1.2.1 this.partitionedIterator

ExternalSorter.scala

```scala
/**
   * Return an iterator over all the data written to this object, grouped by partition and
   * aggregated by the requested aggregator. For each partition we then have an iterator over its
   * contents, and these are expected to be accessed in order (you can't "skip ahead" to one
   * partition without reading the previous one). Guaranteed to return a key-value pair for each
   * partition, in order of partition ID.
   * 返回一个迭代器，它包含了所有写入这个对象的数据，数据按照分区分组，按照请求的聚合器聚合。
   * 对于每个分区，我们都有一个包含它所有内容的迭代器，这些内容期待被按顺序访问（不能在不读取前一个分区的情况下“跳过”到一个分区）。
   * 保证为每个分区返回一个键值对，按分区ID的顺序排列 
   * 
   * For now, we just merge all the spilled files in once pass, but this can be modified to
   * support hierarchical merging.
   * 现在，我们只是在一次传递中合并所有溢出的文件，但这可以修改为支持分层合并。 
   * Exposed for testing.
   */
  def partitionedIterator: Iterator[(Int, Iterator[Product2[K, C]])] = {
    val usingMap = aggregator.isDefined
    val collection: WritablePartitionedPairCollection[K, C] = if (usingMap) map else buffer
    if (spills.isEmpty) {
      // Special case: if we have only in-memory data, we don't need to merge streams, and perhaps
      // we don't even need to sort by anything other than partition ID
      if (ordering.isEmpty) {
        // The user hasn't requested sorted keys, so only sort by partition ID, not key
        groupByPartition(destructiveIterator(collection.partitionedDestructiveSortedIterator(None)))
      } else {
        // We do need to sort by both partition ID and key
        groupByPartition(destructiveIterator(
          collection.partitionedDestructiveSortedIterator(Some(keyComparator))))
      }
    } else {
      // Merge spilled and in-memory data
      // 合并溢写的和内存中的数据
      merge(spills.toSeq, destructiveIterator(
        collection.partitionedDestructiveSortedIterator(comparator)))
    }
  }
```

```scala

  private val numPartitions = partitioner.map(_.numPartitions).getOrElse(1)

  /**
   * Merge a sequence of sorted files, giving an iterator over partitions and then over elements
   * inside each partition. This can be used to either write out a new file or return data to
   * the user.
   * 合并一个有序文件的序列，
   * 给分区一个迭代器，然后迭代每个分区内的元素。这可以用于写出一个新文件或返回数据给用户。 
   *
   * Returns an iterator over all the data written to this object, grouped by partition. For each
   * partition we then have an iterator over its contents, and these are expected to be accessed
   * in order (you can't "skip ahead" to one partition without reading the previous one).
   * Guaranteed to return a key-value pair for each partition, in order of partition ID.
   * 返回一个迭代器，它包含了所有写入这个对象的数据，数据按照分区分组，按照请求的聚合器聚合。
   * 对于每个分区，我们都有一个包含它所有内容的迭代器，这些内容期待被按顺序访问（不能在不读取前一个分区的情况下“跳过”到一个分区）。
   * 保证为每个分区返回一个键值对，按分区ID的顺序排列 
   */
  private def merge(spills: Seq[SpilledFile], inMemory: Iterator[((Int, K), C)])
      : Iterator[(Int, Iterator[Product2[K, C]])] = {
    // 这里的 spills 就是 前面溢写文件后，返回的 SpilledFile 对象放入的数组
    // 对每个 SpilledFile，封装成一个 SpillReader （SpillReader逐个分区的读取溢出的文件）
    // 溢写文件
    val readers = spills.map(new SpillReader(_))
    // 内存中的数据
    val inMemBuffered = inMemory.buffered
    // 对每个分区执行map
    (0 until numPartitions).iterator.map { p =>
      // 一种迭代器，它只从底层缓冲流中读取给定分区ID的元素，并假定该分区是下一个要读取的分区。用于更容易地从内存集合中返回分区的迭代器。
      // 封装内存数据的迭代器
      val inMemIterator = new IteratorForPartition(p, inMemBuffered)
      // 对每个 SpillReader 调用 readNextPartition 方法，返回一个迭代器
      // iterators里就包含了溢写文件中的数据和内存中的数据  【合并】
      val iterators = readers.map(_.readNextPartition()) ++ Seq(inMemIterator)
      // 定义了预聚合
      if (aggregator.isDefined) {
        // Perform partial aggregation across partitions
        // 先使用 mergeSort 排序，再使用 mergeCombiners 聚合
        (p, mergeWithAggregation(
          iterators, aggregator.get.mergeCombiners, keyComparator, ordering.isDefined))
      } 
      // 没有定义预聚合，但定义了排序器（例如在 sortByKey 中由 reduce 任务使用）
      else if (ordering.isDefined) {
        // No aggregator given, but we have an ordering (e.g. used by reduce tasks in sortByKey);
        // sort the elements without trying to merge them
        // mergeSort: 使用一个优先队列排序
        (p, mergeSort(iterators, ordering.get))
      }
      // 没有定义预聚合，也没有定义排序器
      else {
        (p, iterators.iterator.flatten)
      }
    }
  }
```