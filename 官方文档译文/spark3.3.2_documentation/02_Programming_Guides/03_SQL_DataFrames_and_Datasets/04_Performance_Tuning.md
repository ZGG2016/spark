# Performance Tuning

[TOC]

> For some workloads, it is possible to improve performance by either caching data in memory, or by turning on some experimental options.

考虑到工作负载，可以将数据缓存到内存，或开启一些实验性选项，来优化性能。

## Caching Data In Memory

> Spark SQL can cache tables using an in-memory columnar format by calling `spark.catalog.cacheTable("tableName")` or `dataFrame.cache()`. Then Spark SQL will scan only required columns and will automatically tune compression to minimize memory usage and GC pressure. You can call `spark.catalog.uncacheTable("tableName")` or `dataFrame.unpersist()` to remove the table from memory.

通过调用 `spark.catalog.cacheTable("tableName")` 或 `dataFrame.cache()` 以一种柱状格式缓存表。

然后 Spark SQL 将仅扫描要求的列，自动压缩，以最小化内存使用和 gc 压力。

也可以调用 `spark.catalog.uncacheTable("tableName")` 移除内存中的表。

> Configuration of in-memory caching can be done using the `setConf` method on `SparkSession` or by running `SET key=value` commands using SQL.

内存缓存的配置可以使用 `SparkSession` 上的 `setConf` 方法或使用 SQL 运行 `SET key=value` 命令来完成。

Property Name | Default | Meaning | Since Version
---|:---|:---|:---
`spark.sql.inMemoryColumnarStorage.compressed` | true | When set to true Spark SQL will automatically select a compression codec for each column based on statistics of the data.【当设为true时，基于数据的统计信息，会自动为每列选择一个压缩格式】 | 1.0.1
`spark.sql.inMemoryColumnarStorage.batchSize` | 10000 | Controls the size of batches for columnar caching. Larger batch sizes can improve memory utilization and compression, but risk OOMs when caching data.【控制着列缓存的批次大小。在缓存数据时，更大的批次大小可以改善内存利用和压缩，但是会有OOM的风险】 | 1.1.1

## Other Configuration Options

> The following options can also be used to tune the performance of query execution. It is possible that these options will be deprecated in future release as more optimizations are performed automatically.

下面的选项可以用来优化查询性能。这些选项可能会在将来的版本中被废弃，因为更多的优化是自动执行的。


Property Name | Default | Meaning | Since Version
---|:---|:---|:---
`spark.sql.files.maxPartitionBytes`	| 134217728 (128 MB) | The maximum number of bytes to pack into a single partition when reading files. This configuration is effective only when using file-based sources such as Parquet, JSON and ORC.【读取文件时，打包进一个分区的数据的最大字节数。这个配置仅对基于文件的源有效，如Parquet, JSON and ORC（一个分区的最大大小）】 | 2.0.0
`spark.sql.files.openCostInBytes` | 4194304 (4 MB) | The estimated cost to open a file, measured by the number of bytes could be scanned in the same time. This is used when putting multiple files into a partition. It is better to over-estimated, then the partitions with small files will be faster than partitions with bigger files (which is scheduled first). This configuration is effective only when using file-based sources such as Parquet, JSON and ORC.【打开一个文件的估计代价(字节)。当把多个文件放入一个分区时是有用的。最好是往高了估算，因为具有小文件的分区会比具有大文件的分区快(大文件首先要被调度)。这个配置仅对基于文件的源有效，如Parquet, JSON and ORC （打开一个文件占用的内存大小）】 | 2.0.0
`spark.sql.files.minPartitionNum` | Default Parallelism | The suggested (not guaranteed) minimum number of split file partitions. If not set, the default value is `spark.default.parallelism`. This configuration is effective only when using file-based sources such as Parquet, JSON and ORC.【建议的（但不保证）最小文件分区数量。如果不设置，默认是`spark.default.parallelism`。这个配置仅对基于文件的源有效，如Parquet, JSON and ORC 】 | 3.1.0
`spark.sql.broadcastTimeout` | 300	| Timeout in seconds for the broadcast wait time in broadcast joins 【在广播连接中，广播的等待时间的超时时长（秒）】 | 1.3.0
`spark.sql.autoBroadcastJoinThreshold` | 10485760 (10 MB) | Configures the maximum size in bytes for a table that will be broadcast to all worker nodes when performing a join. By setting this value to -1 broadcasting can be disabled. Note that currently statistics are only supported for Hive Metastore tables where the command `ANALYZE TABLE <tableName> COMPUTE STATISTICS noscan` has been run.【当执行join时，广播给所有工作节点的表的最大字节数。设为-1表示禁用广播。（广播join的表阈值）】 	| 1.1.0
`spark.sql.shuffle.partitions` | 200 | Configures the number of partitions to use when shuffling data for joins or aggregations.【对于join或聚合操作，进行shuffle 时，使用的分区数】 | 1.1.0
`spark.sql.sources.parallelPartitionDiscovery.threshold` | 32 | Configures the threshold to enable parallel listing for job input paths. If the number of input paths is larger than this threshold, Spark will list the files by using Spark distributed job. Otherwise, it will fallback to sequential listing. This configuration is only effective when using file-based data sources such as Parquet, ORC and JSON.【对于作业输入路径，启用并行列表的阈值。如果输入路径的数量大于这个阈值，spark将使用分布式作业列出所有文件。否则，回滚到顺序列表。（并行读取数据的阈值）】 | 1.5.0
`spark.sql.sources.parallelPartitionDiscovery.parallelism` | 10000 | Configures the maximum listing parallelism for job input paths. In case the number of input paths is larger than this value, it will be throttled down to use this value. Same as above, this configuration is only effective when using file-based data sources such as Parquet, ORC and JSON. 【对于作业输入路径，最大的列表并行度。输入路径的数量大于这个值时，使用这个值就会成为瓶颈。（并行读取数据的并行度）】 | 2.1.1

## Join Strategy Hints for SQL Queries

> The join strategy hints, namely `BROADCAST`, `MERGE`, `SHUFFLE_HASH` and `SHUFFLE_REPLICATE_NL`, instruct Spark to use the hinted strategy on each specified relation when joining them with another relation. For example, when the `BROADCAST` hint is used on table ‘t1’, broadcast join (either broadcast hash join or broadcast nested loop join depending on whether there is any equi-join key) with ‘t1’ as the build side will be prioritized by Spark even if the size of table ‘t1’ suggested by the statistics is above the configuration `spark.sql.autoBroadcastJoinThreshold`.

join 策略提示(`BROADCAST`, `MERGE`, `SHUFFLE_HASH` and `SHUFFLE_REPLICATE_NL`)指示着 Spark 在将每个指定的关系与另一个关系 join 时，对它们使用提示策略。

例如，当在表 t1 上使用 `BROADCAST` 提示时，和作为构建端的 t1 表的广播 join(是广播哈希join还是广播嵌套循环join取决于是否存在等值连接key) 将被 spark 优先处理，即使 t1 表的大小大于属性 `spark.sql.autoBroadcastJoinThreshold`.

> When different join strategy hints are specified on both sides of a join, Spark prioritizes the `BROADCAST` hint over the `MERGE` hint over the `SHUFFLE_HASH` hint over the `SHUFFLE_REPLICATE_NL` hint. When both sides are specified with the `BROADCAST` hint or the `SHUFFLE_HASH` hint, Spark will pick the build side based on the join type and the sizes of the relations.

当在 join 的两端指定不同的 join 策略提示时，Spark 的 join 策略提示的优先级是: `BROADCAST > MERGE > SHUFFLE_HASH > SHUFFLE_REPLICATE_NL`.

如果两端同时指定的是 `BROADCAST` 提示或 `SHUFFLE_HASH` 提示, spark 将基于 join 类型和关系的大小，选择构建端。

> Note that there is no guarantee that Spark will choose the join strategy specified in the hint since a specific strategy may not support all join types.

注意，不能保证 Spark 会选择提示中指定的 join 策略，因为指定的策略可能不支持所有的 join 类型。

```scala
spark.table("src").join(spark.table("records").hint("broadcast"), "key").show()
```

```java
spark.table("src").join(spark.table("records").hint("broadcast"), "key").show();
```

```python
spark.table("src").join(spark.table("records").hint("broadcast"), "key").show()
```

```r
src <- sql("SELECT * FROM src")
records <- sql("SELECT * FROM records")
head(join(src, hint(records, "broadcast"), src$key == records$key))
```

```sql
-- We accept BROADCAST, BROADCASTJOIN and MAPJOIN for broadcast hint
SELECT /*+ BROADCAST(r) */ * FROM records r JOIN src s ON r.key = s.key
```

> For more details please refer to the documentation of [Join Hints](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-qry-select-hints.html#join-hints).

## Coalesce Hints for SQL Queries

> Coalesce hints allows the Spark SQL users to control the number of output files just like the `coalesce`, `repartition` and `repartitionByRange` in Dataset API, they can be used for performance tuning and reducing the number of output files. The “COALESCE” hint only has a partition number as a parameter. The “REPARTITION” hint has a partition number, columns, or both/neither of them as parameters. The “REPARTITION_BY_RANGE” hint must have column names and a partition number is optional. The “REBALANCE” hint has an initial partition number, columns, or both/neither of them as parameters.

Coalesce 提示可以控制输出文件的数量，就像 Dataset API 中的 `coalesce`、`repartition` 和 `repartitionByRange`，可以用于性能调优和减少输出文件的数量。

- `COALESCE` 只接受 分区数 作为参数
- `REPARTITION` 接收 分区数、列、或同时都有这两项 作为参数
- `REPARTITION_BY_RANGE` 必须有 列名作为参数。分区数参数可选
- `REBALANCE` 有一个初始的分区数、列或同时都有这两项 作为参数

```sql
SELECT /*+ COALESCE(3) */ * FROM t
SELECT /*+ REPARTITION(3) */ * FROM t
SELECT /*+ REPARTITION(c) */ * FROM t
SELECT /*+ REPARTITION(3, c) */ * FROM t
SELECT /*+ REPARTITION */ * FROM t
SELECT /*+ REPARTITION_BY_RANGE(c) */ * FROM t
SELECT /*+ REPARTITION_BY_RANGE(3, c) */ * FROM t
SELECT /*+ REBALANCE */ * FROM t
SELECT /*+ REBALANCE(3) */ * FROM t
SELECT /*+ REBALANCE(c) */ * FROM t
SELECT /*+ REBALANCE(3, c) */ * FROM t
```

> For more details please refer to the documentation of [Partitioning Hints](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-qry-select-hints.html#partitioning-hints).

## Adaptive Query Execution

> Adaptive Query Execution (AQE) is an optimization technique in Spark SQL that makes use of the runtime statistics to choose the most efficient query execution plan, which is enabled by default since Apache Spark 3.2.0. Spark SQL can turn on and off AQE by `spark.sql.adaptive.enabled` as an umbrella configuration. As of Spark 3.0, there are three major features in AQE: including coalescing post-shuffle partitions, converting sort-merge join to broadcast join, and skew join optimization.

Adaptive Query Execution(AQE) 是 Spark SQL 中的一种优化技术。

使用运行时统计信息，来选择最有效率的查询执行计划，从 Spark 3.2.0 开始，默认是关闭的。

通过 `spark.sql.adaptive.enabled` 参数控制开关。

Spark 3.0 中有三种 AOE 的主要特性：

- coalescing post-shuffle partitions
- converting sort-merge join to broadcast join
- skew join optimization

## Coalescing Post Shuffle Partitions

> This feature coalesces the post shuffle partitions based on the map output statistics when both `spark.sql.adaptive.enabled` and `spark.sql.adaptive.coalescePartitions.enabled` configurations are true. This feature simplifies the tuning of shuffle partition number when running queries. You do not need to set a proper shuffle partition number to fit your dataset. Spark can pick the proper shuffle partition number at runtime once you set a large enough initial number of shuffle partitions via `spark.sql.adaptive.coalescePartitions.initialPartitionNum` configuration.

当 `spark.sql.adaptive.enabled=true` 和 `spark.sql.adaptive.coalescePartitions.enabled=true` 时，基于 map 输出的统计信息，合并 post shuffle 的分区数。

这个特性简化了运行查询时的 shuffle 分区数的优化。

通过 `spark.sql.adaptive.coalescePartitions.initialPartitionNum` 属性，为 shuffle 分区数设置一个足够大的初始值，Spark 会在运行时选择一个合适的 shuffle 分区数。

Property Name | Default | Meaning | Since Version
---|:---|:---|:---
`spark.sql.adaptive.coalescePartitions.enabled`	| `true` | When `true` and `spark.sql.adaptive.enabled` is true, Spark will coalesce contiguous shuffle partitions according to the target size (specified by `spark.sql.adaptive.advisoryPartitionSizeInBytes`), to avoid too many small tasks. | 3.0.0
`spark.sql.adaptive.coalescePartitions.parallelismFirst` | `true` | When `true`, Spark ignores the target size specified by `spark.sql.adaptive.advisoryPartitionSizeInBytes` (default 64MB) when coalescing contiguous shuffle partitions, and only respect the minimum partition size specified by `spark.sql.adaptive.coalescePartitions.minPartitionSize` (default 1MB), to maximize the parallelism. This is to avoid performance regression when enabling adaptive query execution. It's recommended to set this config to false and respect the target size specified by `spark.sql.adaptive.advisoryPartitionSizeInBytes`.【为了最大化并行度，当为true时，且合并连续的shuffle分区时，spark忽略通过`spark.sql.adaptive.advisoryPartitionSizeInBytes`指定的目标大小，仅保证通过`spark.sql.adaptive.coalescePartitions.minPartitionSize`设置的最小分区大小。这就避免了当启用AQE时的性能降低。推荐将其设置为false,使用通过`spark.sql.adaptive.advisoryPartitionSizeInBytes`设置的目标大小。】	| 3.2.0
`spark.sql.adaptive.coalescePartitions.minPartitionSize` | 1MB | The minimum size of shuffle partitions after coalescing. Its value can be at most 20% of `spark.sql.adaptive.advisoryPartitionSizeInBytes`. This is useful when the target size is ignored during partition coalescing, which is the default case.	【合并分区后，shuffle分区的最小大小。它的值最大可以是`spark.sql.adaptive.advisoryPartitionSizeInBytes`的20%。在分区合并期间，当目标大小被忽略时是有用的，这也是默认情况】 | 3.2.0
`spark.sql.adaptive.coalescePartitions.initialPartitionNum` | (none) | The initial number of shuffle partitions before coalescing. If not set, it equals to `spark.sql.shuffle.partitions`. This configuration only has an effect when `spark.sql.adaptive.enabled` and `spark.sql.adaptive.coalescePartitions.enabled` are both enabled.【在合并分区前，初始shuffle分区的数量。如果不设置，就等于`spark.sql.shuffle.partitions`属性值。此配置仅在`spark.sql.adaptive.enabled`和`spark.sql.adaptive.coalescePartitions.enabled`同时启用时有效。】 | 3.0.0
`spark.sql.adaptive.advisoryPartitionSizeInBytes` | 64 MB | The advisory size in bytes of the shuffle partition during adaptive optimization (when `spark.sql.adaptive.enabled` is true). It takes effect when Spark coalesces small shuffle partitions or splits skewed shuffle partition.【启用AQE优化期间，shuffle 分区的建议大小(以字节为单位)。当Spark合并小的shuffle分区或拆分倾斜的shuffle分区时生效。】	| 3.0.0

## Converting sort-merge join to broadcast join

> AQE converts sort-merge join to broadcast hash join when the runtime statistics of any join side is smaller than the adaptive broadcast hash join threshold. This is not as efficient as planning a broadcast hash join in the first place, but it’s better than keep doing the sort-merge join, as we can save the sorting of both the join sides, and read shuffle files locally to save network traffic(if `spark.sql.adaptive.localShuffleReader.enabled` is true)

当 join 的任意一侧运行时统计信息小于自适应广播哈希 join 阈值时，AQE 将 sort-merge join 转换为广播哈希 join. 

这和一开始就设置广播哈希 join 相比，并不高效，但这比继续执行 sort-merge join 要好，因为我们可以保存 join 两边的顺序，并读取本地的 shuffle 文件，以节省网络流量(如果 `spark.sql.adaptive.localShuffleReader.enabled` 设为 true).


Property Name | Default | Meaning | Since Version
---|:---|:---|:---
`spark.sql.adaptive.autoBroadcastJoinThreshold`	| (none) | Configures the maximum size in bytes for a table that will be broadcast to all worker nodes when performing a join. By setting this value to -1 broadcasting can be disabled. The default value is same with `spark.sql.autoBroadcastJoinThreshold`. Note that, this config is used only in adaptive framework. 【在执行join时，配置一个表的大小阈值，超过这个阈值，这个表就被广播到所有worker节点。设为-1，表示禁用广播。】 | 3.2.0

## Converting sort-merge join to shuffled hash join

> AQE converts sort-merge join to shuffled hash join when all post shuffle partitions are smaller than a threshold, the max threshold can see the config `spark.sql.adaptive.maxShuffledHashJoinLocalMapThreshold`.

当所有的 post shuffle 分区数量小于 `spark.sql.adaptive.maxShuffledHashJoinLocalMapThreshold` 时, AQE 将 sort-merge join 转换为 shuffled 哈希 join

Property Name | Default | Meaning | Since Version
---|:---|:---|:---
`spark.sql.adaptive.maxShuffledHashJoinLocalMapThreshold` | 0 | Configures the maximum size in bytes per partition that can be allowed to build local hash map. If this value is not smaller than `spark.sql.adaptive.advisoryPartitionSizeInBytes` and all the partition size are not larger than this config, join selection prefer to use shuffled hash join instead of sort merge join regardless of the value of `spark.sql.join.preferSortMergeJoin`.	【配置每个分区的最大大小，这个分区能被允许构建本地hash map.如果这个值并不比`spark.sql.adaptive.advisoryPartitionSizeInBytes`小，且所有的分区大小并不比这个配置大，更倾向选择shuffled 哈希 join，而不是sort merge join，此时不会关注`spark.sql.join.preferSortMergeJoin`的值】 | 3.2.0

## Optimizing Skew Join

> Data skew can severely downgrade the performance of join queries. This feature dynamically handles skew in sort-merge join by splitting (and replicating if needed) skewed tasks into roughly evenly sized tasks. It takes effect when both `spark.sql.adaptive.enabled` and `spark.sql.adaptive.skewJoin.enabled` configurations are enabled.

数据倾斜会严重降低连接查询的性能。

该特性通过将倾斜任务拆分(并在需要时复制)为大小大致相同的任务，动态处理 sort-merge join 中的倾斜。

当 `sql.adaptive.enabled` 和 `spark.sql.adaptive.skewJoin` 启用时生效。

Property Name | Default | Meaning | Since Version
---|:---|:---|:---
`spark.sql.adaptive.skewJoin.enabled` | true | When true and `spark.sql.adaptive.enabled` is true, Spark dynamically handles skew in sort-merge join by splitting (and replicating if needed) skewed partitions. 【当设为true,且`spark.sql.adaptive.enabled`为true时，spark会动态处理sort-merge join中的倾斜，通过将倾斜任务拆分(并在需要时复制)】 | 3.0.0
`spark.sql.adaptive.skewJoin.skewedPartitionFactor`	| 5	 | A partition is considered as skewed if its size is larger than this factor multiplying the median partition size and also larger than `spark.sql.adaptive.skewJoin.skewedPartitionThresholdInBytes`. 【如果一个分区的大小大于这个因子乘以中间的分区大小，同时也大于`spark.sql.adaptive.skewJoin.skewedPartitionThresholdInBytes`值，那么就认为这个分区是倾斜的】| 3.0.0
`spark.sql.adaptive.skewJoin.skewedPartitionThresholdInBytes` | 256MB | A partition is considered as skewed if its size in bytes is larger than this threshold and also larger than `spark.sql.adaptive.skewJoin.skewedPartitionFactor` multiplying the median partition size. Ideally this config should be set larger than `spark.sql.adaptive.advisoryPartitionSizeInBytes`.	| 3.0.0