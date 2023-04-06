# WritableByteChannelWrapper

```java
package org.apache.spark.shuffle.api;

import java.io.Closeable;
import java.nio.channels.WritableByteChannel;

import org.apache.spark.annotation.Private;

/**
 * :: Private ::
 * 对 WritableByteChannel 的封装
 * A thin wrapper around a {@link WritableByteChannel}.
 * <p>
 * This is primarily provided for the local disk shuffle implementation to provide a
 * {@link java.nio.channels.FileChannel} that keeps the channel open across partition writes.
 * 这注意提供给本地磁盘shuffle实现，来提供 FileChanne，以保持 channel 在跨 partition writes 中打开。
 * @since 3.0.0
 */
@Private
public interface WritableByteChannelWrapper extends Closeable {

  /**
   * The underlying channel to write bytes into. 写字节的底层channel
   */
  WritableByteChannel channel();
  //WritableByteChannel:A channel that can write bytes.
}

```