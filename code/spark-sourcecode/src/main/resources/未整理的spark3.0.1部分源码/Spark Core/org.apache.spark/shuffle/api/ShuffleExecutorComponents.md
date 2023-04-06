# ShuffleExecutorComponents

```java
package org.apache.spark.shuffle.api;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import org.apache.spark.annotation.Private;

/**
 * 为Executors，构建的支持shuffle的接口。
 *
 * :: Private ::
 * An interface for building shuffle support for Executors.
 * 
 * @since 3.0.0
 */
@Private
public interface ShuffleExecutorComponents {

  /**
   * Called once per executor to bootstrap this module with state that is specific to
   * that executor, specifically the application ID and executor ID.
   *
   * @param appId The Spark application id
   * @param execId The unique identifier of the executor being initialized
   * @param extraConfigs Extra configs that were returned by
   *                     {@link ShuffleDriverComponents#initializeApplication()}
   */
  void initializeExecutor(String appId, String execId, Map<String, String> extraConfigs);

  /**
   * 每个 map task 仅调用它一次，来创建一个 writer，
   *       此 writer 负责那个 map task 写入的所有分区字节的持久化
   *
   * Called once per map task to create a writer that will be responsible for persisting all the
   * partitioned bytes written by that map task.
   *
   * @param shuffleId Unique identifier for the shuffle the map task is a part of 
   *                   map task 所属的 shuffle 的唯一标识符。
   * @param mapTaskId An ID of the map task. The ID is unique within this Spark application.  map task 的一个 ID。这个 ID 在这个 Spark 应用程序中是唯一的。
   * @param numPartitions The number of partitions that will be written by the map task. Some of these partitions may be empty. 将被map task写入的分区数量
   */
  ShuffleMapOutputWriter createMapOutputWriter(
      int shuffleId,
      long mapTaskId,
      int numPartitions) throws IOException;

  /**
   * 一个可选地扩展。
   * 用来创建一个 map output writer，它可以优化一个分区文件(一个 map task 的整个结果)到备份存储的传输，
   *
   * 大部分的实现应该返回默认(Optional.empty())，来表示不支持这个优化。
   *
   * 这个主要是在本地磁盘 shuffle 存储实现中保留优化，以维护向后兼容。
   *
   * An optional extension for creating a map output writer that can optimize the transfer of a
   * single partition file, as the entire result of a map task, to the backing store.
   * <p>
   * Most implementations should return the default {@link Optional#empty()} to indicate that
   * they do not support this optimization. This primarily is for backwards-compatibility in
   * preserving an optimization in the local disk shuffle storage implementation.
   *
   * @param shuffleId Unique identifier for the shuffle the map task is a part of
   * @param mapId An ID of the map task. The ID is unique within this Spark application.
   */
  default Optional<SingleSpillShuffleMapOutputWriter> createSingleFileMapOutputWriter(
      int shuffleId,
      long mapId) throws IOException {
    return Optional.empty();
  }
}

```