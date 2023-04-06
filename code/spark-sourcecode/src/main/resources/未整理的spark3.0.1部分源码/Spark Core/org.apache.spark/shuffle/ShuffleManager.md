# ShuffleManager

```java
package org.apache.spark.shuffle

import org.apache.spark.{ShuffleDependency, TaskContext}

/**
 * Pluggable interface for shuffle systems. A ShuffleManager is created in SparkEnv on the driver and on each executor, based on the spark.shuffle.manager setting. 
 * The driver registers shuffles with it, and executors (or tasks running locally in the driver) can ask to read and write data.
 *
 * NOTE: this will be instantiated by SparkEnv so its constructor can take a SparkConf and
 * boolean isDriver as parameters.
 *
 * 一个可拔插的shuffle系统接口。在 driver 和各个 executor 的 SparkEnv 中创建。
 * 可以通过配置 `spark.shuffle.manager` 属性来设置 shuffle manager 类型。
 * 
 * driver 使用它注册 shuffles，executors (或driver本地运行的任务)请求读写数据。
 */
private[spark] trait ShuffleManager {

  /**
   * Register a shuffle with the manager and obtain a handle for it to pass to tasks.
   * 注册一个 shuffle，返回一个 ShuffleHandle，传给任务。
   */
  def registerShuffle[K, V, C](
      shuffleId: Int,  //为一次shuffle，创建的一个id标识
      dependency: ShuffleDependency[K, V, C]): ShuffleHandle

  // ShuffleHandle
  /*
   * 
   * An opaque handle to a shuffle, used by a ShuffleManager to pass information about it to tasks.
   *  一个 shuffle 的不透明句柄，它会由 ShuffleManager 把它的信息传给任务。
   * @param shuffleId ID of the shuffle
   *
   * @DeveloperApi
   * abstract class ShuffleHandle(val shuffleId: Int) extends Serializable {}
   */


  /** Get a writer for a given partition. Called on executors by map tasks. */
  // 由 map tasks 在 executors 上调用该方法。
  // 为一个给定分区创建一个 writer，返回一个 ShuffleWriter
  def getWriter[K, V](
      handle: ShuffleHandle,
      mapId: Long,
      context: TaskContext,  // 一个任务操作一个分区
      // 对每个shuffle，报告 shuffle write 指标。
      metrics: ShuffleWriteMetricsReporter): ShuffleWriter[K, V]

  /**
   * Get a reader for a range of reduce partitions (startPartition to endPartition-1, inclusive). 
   * Called on executors by reduce tasks.
   *
   * 创建一个 reader，用来读取一定范围内的分区[startPartition,endPartition-1]
   * 由 reduce tasks 在 executors 上调用
   */
  def getReader[K, C](
      handle: ShuffleHandle,
      startPartition: Int,
      endPartition: Int,
      context: TaskContext,
      metrics: ShuffleReadMetricsReporter): ShuffleReader[K, C]

  /**
   * [startPartition to endPartition-1)、[startMapIndex to endMapIndex - 1)
   * 为从一定范围内的 map 输出中读取一定范围内的 reduce 分区，获取一个 reader。
   * 
   * 在 executors 上，由 reduce tasks 调用。
   *
   * Get a reader for a range of reduce partitions (startPartition to endPartition-1, inclusive) 
   *   to read from map output (startMapIndex to endMapIndex - 1, inclusive). 
   * Called on executors by reduce tasks.
   */
  def getReaderForRange[K, C](
      handle: ShuffleHandle,
      startMapIndex: Int,
      endMapIndex: Int,
      startPartition: Int,
      endPartition: Int,
      context: TaskContext,
      metrics: ShuffleReadMetricsReporter): ShuffleReader[K, C]

  /**
   * 从 ShuffleManager 中移除一个 shuffle 的元数据
   * Remove a shuffle's metadata from the ShuffleManager.
   * @return true if the metadata removed successfully, otherwise false.
   */
  def unregisterShuffle(shuffleId: Int): Boolean

  /**
   * 返回一个解析器，能根据块坐标取出块数据。
   * Return a resolver capable of retrieving shuffle block data based on block coordinates.
   */
  def shuffleBlockResolver: ShuffleBlockResolver

  /** Shut down this ShuffleManager. */
  def stop(): Unit
}

```