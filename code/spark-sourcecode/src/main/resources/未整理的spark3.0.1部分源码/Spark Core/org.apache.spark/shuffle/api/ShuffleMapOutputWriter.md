# ShuffleMapOutputWriter

```java
package org.apache.spark.shuffle.api;

import java.io.IOException;

import org.apache.spark.annotation.Private;

/**
 * 一个返回子 writers 的顶层 writers，为持久化一个 map task 输出，
 *                                   然后作为一个原子操作来，提交写入的所有数据。
 *
 * :: Private ::
 * A top-level writer that returns child writers for persisting the output of a map task,
 * and then commits all of the writes as one atomic operation.
 *
 * @since 3.0.0
 */
@Private
public interface ShuffleMapOutputWriter {

  /**
   * 创建一个writer，可以打开输出流，来持久化给定reduce partition id的字节。
   *
   * Creates a writer that can open an output stream to persist bytes targeted for a given reduce partition id.
   * <p>
   *
   * chunk 对应于给定的 reduce 分区中的字节。
   * 对于任何给定 map task 中的相同分区，不会调用两次。
   * 分区标识符将精确地在[0 numPartitions)范围内，
   *         其中numPartitions是在创建这个 map output writer 时提供的
   * 
   * The chunk corresponds to bytes in the given reduce partition. This will not be called twice for the same partition within any given map task. The partition identifier will be in the range of precisely 0 (inclusive) to numPartitions (exclusive), where numPartitions was provided upon the creation of this map output writer via {@link ShuffleExecutorComponents#createMapOutputWriter(int, long, int)}.
   * <p>
   *
   * 对该方法的调用将附以调用单调递增的reducepartitionid;
   * 对该方法的每次调用都将使用一个reducePartitionId，这个id严格地大于之前对该方法的任何调用所提供的reducePartitionId。
   * 不能保证为上述范围中的每个分区id调用此方法。特别是，不能保证是否会对空分区调用此方法。
   *
   * Calls to this method will be invoked with monotonically increasing reducePartitionIds; each call to this method will be called with a reducePartitionId that is strictly greater than the reducePartitionIds given to any previous call to this method. This method is not guaranteed to be called for every partition id in the above described range. In particular, no guarantees are made as to whether or not this method will be called for empty partitions.
   */
  ShufflePartitionWriter getPartitionWriter(int reducePartitionId) throws IOException;

  /**
   * 提交所有 partition writers 完成的写入操作，返回每个分区写入的字节数。partition writers 是所有调用这个对象的 getPartitionWriter(int) 方法返回的。
   *
   * Commits the writes done by all partition writers returned by all calls to this object's {@link #getPartitionWriter(int)}, and returns the number of bytes written for each partition.
   *
   * 这应该确保这个模块的 partition writers 产生的 writes 对下游 reduce tasks 可用。
   * 如果这个方法抛出任意异常，就会调用这个模块的 abort(Throwable) 方法，在传播执行之前。
   *
   * 这也可以关闭资源，清理临时状态。
   *
   * <p>
   * This should ensure that the writes conducted by this module's partition writers are
   * available to downstream reduce tasks. If this method throws any exception, this module's {@link #abort(Throwable)} method will be invoked before propagating the exception.
   * <p>
   * This can also close any resources and clean up temporary state if necessary.
   * <p>
   *
   * 返回的数组应该包含 partition writer 往这个分区id 写入的字节数，
   *                 分区是从0到(numPartitions - 1)
   *
   * The returned array should contain, for each partition from (0) to (numPartitions - 1), the number of bytes written by the partition writer for that partition id.
   */
  long[] commitAllPartitions() throws IOException;

  /**
   * 中止所有writers完成的写入操作。
   * Abort all of the writes done by any writers returned by {@link #getPartitionWriter(int)}.
   * <p>
   *
   * 这将使写入字节的结果无效。如果需要，这还可以关闭任何资源并清理临时状态。
   * This should invalidate the results of writing bytes. This can also close any resources and clean up temporary state if necessary.
   */
  void abort(Throwable error) throws IOException;
}

```