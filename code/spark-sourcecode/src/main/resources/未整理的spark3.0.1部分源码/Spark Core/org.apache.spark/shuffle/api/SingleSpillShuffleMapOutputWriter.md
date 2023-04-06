# SingleSpillShuffleMapOutputWriter

```java
package org.apache.spark.shuffle.api;

import java.io.File;
import java.io.IOException;

import org.apache.spark.annotation.Private;

/**
 * 优化把一个文件传输到备份存储的分区写。
 *
 * Optional extension for partition writing that is optimized for transferring a single
 * file to the backing store.
 */
@Private
public interface SingleSpillShuffleMapOutputWriter {

  /**
   * 传输包含这个map task写入的所有分区的字节的文件
   * Transfer a file that contains the bytes of all the partitions written by this map task.
   */
  void transferMapSpillFile(File mapOutputFile, long[] partitionLengths) throws IOException;
}

```