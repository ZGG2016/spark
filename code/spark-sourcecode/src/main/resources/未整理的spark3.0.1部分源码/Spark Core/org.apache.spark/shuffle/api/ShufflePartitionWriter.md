# ShufflePartitionWriter

```java
package org.apache.spark.shuffle.api;

import java.io.IOException;
import java.util.Optional;
import java.io.OutputStream;

import org.apache.spark.annotation.Private;

/**
 * 打开流来持久化分区字节到备份数据存储中的一个接口。
 *
 * :: Private ::
 * An interface for opening streams to persist partition bytes to a backing data store.
 * <p>
 * This writer stores bytes for one (mapper, reducer) pair, corresponding to one shuffle
 * block. 这个writer为一个(mapper, reducer)对存储字节，对应于一个 shuffle 块。
 *
 * @since 3.0.0
 */
@Private
public interface ShufflePartitionWriter {

  /**
   * 打开并返回一个OutputStream，它可以将字节写入到底层数据存储中。
   *
   * Open and return an {@link OutputStream} that can write bytes to the underlying data store.
   * <p>
   *
   * 这个方法仅会在 map task 的 partition writer 上调用一次，以此来将字节写入到分区。
   * 输出流将仅被用来为这个分区写字节。
   * 写完这个块的所有字节后，或这个 write 故障了，map task 就关闭这个输出流。
   *
   * This method will only be called once on this partition writer in the map task, to write the  bytes to the partition. The output stream will only be used to write the bytes for this partition. The map task closes this output stream upon writing all the bytes for this block, or if the write fails for any reason.
   * <p>
   *
   * 合并这个 map task 写入的所有分区的字节应该重用相同的 OutputStream 实例，在父 ShuffleMapOutputWriter 提供的 partition writers 之间。
   * 如果这么做了，确保 OutputStream#close() 不会关闭资源，因为它将在 partition writers 间被重用。
   * 底层的资源应该在 ShuffleMapOutputWriter#commitAllPartitions() 和 ShuffleMapOutputWriter#abort(Throwable) 中清理。
   *
   * Implementations that intend on combining the bytes for all the partitions written by this map task should reuse the same OutputStream instance across all the partition writers provided by the parent {@link ShuffleMapOutputWriter}. If one does so, ensure that {@link OutputStream#close()} does not close the resource, since it will be reused across partition writes. The underlying resources should be cleaned up in {@link ShuffleMapOutputWriter#commitAllPartitions()} and {@link ShuffleMapOutputWriter#abort(Throwable)}.
   */
  OutputStream openStream() throws IOException;

  /**
   * 打开并返回一个 WritableByteChannelWrapper，用来将输入字节channels中的字节传输到底层的shuffle数据存储中。
   *
   * Opens and returns a {@link WritableByteChannelWrapper} for transferring bytes from
   * input byte channels to the underlying shuffle data store.
   * <p>
   *
   * 这个方法仅会在 map task 中的这个 partition writer 上调用一次，来将字节写入分区。
   * channel将仅被用作写字节入分区。
   * 写完这个块的所有字节后，或这个 write 故障了，map task 就关闭这个channel。
   *
   * This method will only be called once on this partition writer in the map task, to write the bytes to the partition. The channel will only be used to write the bytes for this partition. The map task closes this channel upon writing all the bytes for this block, or if the write fails for any reason.
   * <p>
   *
   *
   * 合并这个 map task 写入的所有分区的字节应该重用相同的 channel 实例，在父 ShuffleMapOutputWriter 提供的 partition writers 之间。
   * 如果这么做了，确保 WritableByteChannelWrapper#close() 不会关闭资源，因为它将在 partition writers 间被重用。
   * 底层的资源应该在 ShuffleMapOutputWriter#commitAllPartitions() 和 ShuffleMapOutputWriter#abort(Throwable) 中清理。
   *
   * Implementations that intend on combining the bytes for all the partitions written by this map task should reuse the same channel instance across all the partition writers provided by the parent {@link ShuffleMapOutputWriter}. If one does so, ensure that {@link WritableByteChannelWrapper#close()} does not close the resource, since the channel will be reused across partition writes. The underlying resources should be cleaned up in {@link ShuffleMapOutputWriter#commitAllPartitions()} and {@link ShuffleMapOutputWriter#abort(Throwable)}.
   * <p>
   *
   * 这个方法主要是为了高级优化，字节可以从输入溢写文件复制到输出 channel，而不用复制到内存中。
   * 如果不支持这个优化，这个实现应当返回 Optional#empty()。默认下就是返回这个。
   *
   * This method is primarily for advanced optimizations where bytes can be copied from the input spill files to the output channel without copying data into memory. If such optimizations are not supported, the implementation should return {@link Optional#empty()}. By default, the implementation returns {@link Optional#empty()}.
   * <p>
   *
   * 注意，返回的 WritableByteChannelWrapper 本身是关闭的，WritableByteChannelWrapper#channel() 返回的底层 channel 不会关闭。
   * 确保底层 channel 在 WritableByteChannelWrapper#close()、ShuffleMapOutputWriter#commitAllPartitions()、ShuffleMapOutputWriter#abort(Throwable) 中清理。
   *
   * Note that the returned {@link WritableByteChannelWrapper} itself is closed, but not the underlying channel that is returned by {@link WritableByteChannelWrapper#channel()}. Ensure that the underlying channel is cleaned up in {@link WritableByteChannelWrapper#close()}, {@link ShuffleMapOutputWriter#commitAllPartitions()}, or {@link ShuffleMapOutputWriter#abort(Throwable)}.
   */
  default Optional<WritableByteChannelWrapper> openChannelWrapper() throws IOException {
    return Optional.empty();
  }

  /**
   * Returns the number of bytes written either by this writer's output stream opened by
   * {@link #openStream()} or the byte channel opened by {@link #openChannelWrapper()}.
   * <p>
   * This can be different from the number of bytes given by the caller. For example, the
   * stream might compress or encrypt the bytes before persisting the data to the backing
   * data store.
   */
  long getNumBytesWritten();
}

```