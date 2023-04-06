# ShuffleWriter

```java
package org.apache.spark.shuffle

import java.io.IOException

import org.apache.spark.scheduler.MapStatus

/**
 * 在一个 map task 中，获取写入到 shuffle 系统 的记录。
 * Obtained inside a map task to write out records to the shuffle system.
 */
private[spark] abstract class ShuffleWriter[K, V] {
  /** Write a sequence of records to this task's output */
  // 将一系列记录写入到这个任务的输出
  @throws[IOException]
  def write(records: Iterator[Product2[K, V]]): Unit

  /** Close this writer, passing along whether the map completed */
  def stop(success: Boolean): Option[MapStatus]
}


```