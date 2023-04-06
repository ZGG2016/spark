# FileSegment

```java
package org.apache.spark.storage

import java.io.File

/**
 * 根据一个偏移量和一个长度，定义一个文件的片段的引用。
 *
 * References a particular segment of a file (potentially the entire file),
 * based off an offset and a length.
 */
private[spark] class FileSegment(val file: File, val offset: Long, val length: Long) {
  require(offset >= 0, s"File segment offset cannot be negative (got $offset)")
  require(length >= 0, s"File segment length cannot be negative (got $length)")
  override def toString: String = {
    "(name=%s, offset=%d, length=%d)".format(file.getName, offset, length)
  }
}
```