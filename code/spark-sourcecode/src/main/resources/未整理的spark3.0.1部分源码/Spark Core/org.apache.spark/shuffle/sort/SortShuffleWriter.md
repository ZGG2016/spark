# SortShuffleWriter

```java
package org.apache.spark.shuffle.sort

import org.apache.spark._
import org.apache.spark.internal.{config, Logging}
import org.apache.spark.scheduler.MapStatus
import org.apache.spark.shuffle.{BaseShuffleHandle, IndexShuffleBlockResolver, ShuffleWriter}
import org.apache.spark.shuffle.api.ShuffleExecutorComponents
import org.apache.spark.util.collection.ExternalSorter

private[spark] class SortShuffleWriter[K, V, C](
    shuffleBlockResolver: IndexShuffleBlockResolver,
    handle: BaseShuffleHandle[K, V, C],
    mapId: Long,
    context: TaskContext,
    shuffleExecutorComponents: ShuffleExecutorComponents)
  extends ShuffleWriter[K, V] with Logging {

  private val dep = handle.dependency

  //blockManager：放置、检索块
  private val blockManager = SparkEnv.get.blockManager

  //排序和合并多个类型为(K,V)的键值对，产生类型为(K,C)的key-combiner对。
  //先使用一个分区器初次把 keys 划分到分区中，
  //然后在每个分区中使用自定义的 Comparator 对 key 排序[可选步骤]。
  private var sorter: ExternalSorter[K, V, _] = null

  // Are we in the process of stopping? Because map tasks can call stop() with success = true
  // and then call stop() with success = false if they get an exception, we want to make sure
  // we don't try deleting files, etc twice.
  //判断是否处在一个停止的进程中。
  private var stopping = false

/**
 * MapStatus：
 * Result returned by a ShuffleMapTask to a scheduler. Includes the block manager address that the
 * task ran on as well as the sizes of outputs for each reducer, for passing on to the reduce tasks.
 */

  private var mapStatus: MapStatus = null

//TaskMetrics类：在任务执行期间跟踪的指标。
  /**
   * //Metrics related to shuffle write, defined only in shuffle map stages.
   * val shuffleWriteMetrics: ShuffleWriteMetrics = new ShuffleWriteMetrics()
   */
  private val writeMetrics = context.taskMetrics().shuffleWriteMetrics

  /** Write a bunch of records to this task's output */
  //把记录写入到这个任务的输出
  override def write(records: Iterator[Product2[K, V]]): Unit = {
    sorter = if (dep.mapSideCombine) { //要先判断是否要进行map端的聚合
      new ExternalSorter[K, V, C](
        context, dep.aggregator, Some(dep.partitioner), dep.keyOrdering, dep.serializer)
    } else {
      // In this case we pass neither an aggregator nor an ordering to the sorter, because we don't
      // care whether the keys get sorted in each partition; that will be done on the reduce side
      // if the operation being run is sortByKey.
      new ExternalSorter[K, V, V](
        context, aggregator = None, Some(dep.partitioner), ordering = None, dep.serializer)
    }
    sorter.insertAll(records)

    // Don't bother including the time to open the merged output file in the shuffle write time,
    // because it just opens a single file, so is typically too fast to measure accurately
    // (see SPARK-3570).
    val mapOutputWriter = shuffleExecutorComponents.createMapOutputWriter(
      dep.shuffleId, mapId, dep.partitioner.numPartitions)

    //writePartitionedMapOutput：
    //把ExternalSorter中所有数据写入到 map output writer，可以把字节推向一些任意的后备存储中。
    sorter.writePartitionedMapOutput(dep.shuffleId, mapId, mapOutputWriter)

    //commitAllPartitions：
    //提交所有 partition writers 完成的写入操作，返回每个分区写入的字节数。
    val partitionLengths = mapOutputWriter.commitAllPartitions()
    mapStatus = MapStatus(blockManager.shuffleServerId, partitionLengths, mapId)
  }

  /** Close this writer, passing along whether the map completed */
  override def stop(success: Boolean): Option[MapStatus] = {
    try {
      if (stopping) {
        return None
      }
      stopping = true
      if (success) {
        return Option(mapStatus)
      } else {
        return None
      }
    } finally {
      // Clean up our sorter, which may have its own intermediate files
      if (sorter != null) {
        val startTime = System.nanoTime()
        sorter.stop()
        writeMetrics.incWriteTime(System.nanoTime - startTime)
        sorter = null
      }
    }
  }
}

private[spark] object SortShuffleWriter {
  //判断是否 Bypass Merge Sort
  def shouldBypassMergeSort(conf: SparkConf, dep: ShuffleDependency[_, _, _]): Boolean = {
    // We cannot bypass sorting if we need to do map-side aggregation.
    // 如果需要进行 map 端的聚合，就不能 bypass sort
    if (dep.mapSideCombine) {
      false
    } else {
      val bypassMergeThreshold: Int = conf.get(config.SHUFFLE_SORT_BYPASS_MERGE_THRESHOLD)
      //分区数要小于等于200
      dep.partitioner.numPartitions <= bypassMergeThreshold
    }
  }
}

```