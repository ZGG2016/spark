# ShuffleReader

```java
package org.apache.spark.shuffle

/**
 * 在 reduce task 中，获得从 mappers 中读取的合并记录。
 * Obtained inside a reduce task to read combined records from the mappers.
 */
private[spark] trait ShuffleReader[K, C] {
  /** Read the combined key-values for this reduce task */
  // 对这个 reduce task， 读取合并的键值对记录。
  def read(): Iterator[Product2[K, C]]

  /**
   * Close this reader.
   * TODO: Add this back when we make the ShuffleReader a developer API that others can implement
   * (at which point this will likely be necessary).
   */
  // def stop(): Unit
}
```