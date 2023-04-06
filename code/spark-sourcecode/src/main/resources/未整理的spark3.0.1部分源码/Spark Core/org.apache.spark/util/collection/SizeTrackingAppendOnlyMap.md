# SizeTrackingAppendOnlyMap

```java

package org.apache.spark.util.collection

/**
 *  一个追踪自身大小的append-only map
 * An append-only map that keeps track of its estimated size in bytes.
 */
private[spark] class SizeTrackingAppendOnlyMap[K, V]
  extends AppendOnlyMap[K, V] with SizeTracker
{
  override def update(key: K, value: V): Unit = {
    super.update(key, value)
    super.afterUpdate()
  }

  //调父类方法执行
  override def changeValue(key: K, updateFunc: (Boolean, V) => V): V = {
    val newValue = super.changeValue(key, updateFunc)
    super.afterUpdate()
    newValue
  }

  override protected def growTable(): Unit = {
    super.growTable()
    resetSamples()
  }
}

```