# Metrics

```java
package org.apache.spark.shuffle

/**
 * 对每个shuffle，报告shuffle读取指标。
 *
 * 假设调用的所有方法都在一个线程上执行。如，具体的实现不需要异步。
 * 
 * An interface for reporting shuffle read metrics, for each shuffle. This interface assumes
 * all the methods are called on a single-threaded, i.e. concrete implementations would not need
 * to synchronize.
 *
 * All methods have additional Spark visibility modifier to allow public, concrete implementations
 * that still have these methods marked as private[spark].
 */
private[spark] trait ShuffleReadMetricsReporter {
  private[spark] def incRemoteBlocksFetched(v: Long): Unit
  private[spark] def incLocalBlocksFetched(v: Long): Unit
  private[spark] def incRemoteBytesRead(v: Long): Unit
  private[spark] def incRemoteBytesReadToDisk(v: Long): Unit
  private[spark] def incLocalBytesRead(v: Long): Unit
  private[spark] def incFetchWaitTime(v: Long): Unit
  private[spark] def incRecordsRead(v: Long): Unit
}


/**
 * 对每个shuffle，报告 shuffle write 指标。
 * 这个接口假设所有的方法都是在一个线程下调用，如，具体的实现不需要同步
 * An interface for reporting shuffle write metrics. This interface assumes all the methods are
 * called on a single-threaded, i.e. concrete implementations would not need to synchronize.
 *
 * All methods have additional Spark visibility modifier to allow public, concrete implementations
 * that still have these methods marked as private[spark].
 */
private[spark] trait ShuffleWriteMetricsReporter {
  private[spark] def incBytesWritten(v: Long): Unit
  private[spark] def incRecordsWritten(v: Long): Unit
  private[spark] def incWriteTime(v: Long): Unit
  private[spark] def decBytesWritten(v: Long): Unit
  private[spark] def decRecordsWritten(v: Long): Unit
}

```