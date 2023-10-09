
[TOC]

##### Spark SQL
###### Runtime SQL Configuration

> Runtime SQL configurations are per-session, mutable Spark SQL configurations. They can be set with initial values by the config file and command-line options with `--conf/-c` prefixed, or by setting SparkConf that are used to create SparkSession. Also, they can be set and queried by `SET` commands and rest to their initial values by `RESET` command, or by `SparkSession.conf`’s setter and getter methods in runtime.

运行时 sql 配置是会话级的、可变的 spark sql 配置。

它们可使用配置文件和使用 `--conf/-c` 前缀的命令行选项设置初始化值，或者通过设置 SparkConf 设置。也可也使用 SET 命令设置和查询，使用 RESET 命令充值初始值，或通过 SparkSession.conf 的 setter 和 getter 方法在运行时设置。

- spark.sql.adaptive.advisoryPartitionSizeInBytes	

	Default: (value of spark.sql.adaptive.shuffle.targetPostShuffleInputSize)	

	Since Version: 3.0.0

	> The advisory size in bytes of the shuffle partition during adaptive optimization (when `spark.sql.adaptive.enabled` is true). It takes effect when Spark coalesces small shuffle partitions or splits skewed shuffle partition.

	在自适应优化期间，shuffle 分区的推荐大小（`spark.sql.adaptive.enabled`设为true时）。当 spark 减少 shuffle 分区或切分倾斜的 shuffle 分区时，会发挥作用。

- spark.sql.adaptive.autoBroadcastJoinThreshold	

	Default: (none)	

	Since Version: 3.2.0

	> Configures the maximum size in bytes for a table that will be broadcast to all worker nodes when performing a join. By setting this value to -1 broadcasting can be disabled. The default value is same with `spark.sql.autoBroadcastJoinThreshold`. Note that, this config is used only in adaptive framework.

	当执行 join 时，将被广播到所有 worker 节点的表的最大大小。

	将此值设为-1，表示禁用广播。默认值和 `spark.sql.autoBroadcastJoinThreshold` 相同。

	注意，这个配置仅用于自适应框架。

- spark.sql.adaptive.coalescePartitions.enabled	

	Default: true	

	Since Version: 3.0.0

	> When true and 'spark.sql.adaptive.enabled' is true, Spark will coalesce contiguous shuffle partitions according to the target size (specified by 'spark.sql.adaptive.advisoryPartitionSizeInBytes'), to avoid too many small tasks.

	当为 true 且 `spark.sql.adaptive.enabled` 为 true 时，spark 将根据目标大小（由 `spark.sql.adaptive.advisoryPartitionSizeInBytes` 指定），减少连续的 shuffle 分区，以避免太多的小任务。

- spark.sql.adaptive.coalescePartitions.initialPartitionNum	

	Default: (none)	

	Since Version: 3.0.0

	> The initial number of shuffle partitions before coalescing. If not set, it equals to `spark.sql.shuffle.partitions`. This configuration only has an effect when 'spark.sql.adaptive.enabled' and 'spark.sql.adaptive.coalescePartitions.enabled' are both true.

	在减少分区之前，shuffle 分区的初始数量。如果没有设置，那么就等于 `spark.sql.shuffle.partitions` 配置。

	这个配置仅在 `spark.sql.adaptive.enabled` 和 `spark.sql.adaptive.coalescePartitions.enabled` 同时为 true 时，才会起作用。

- spark.sql.adaptive.coalescePartitions.minPartitionSize	

	Default: 1MB	

	Since Version: 3.2.0

	> The minimum size of shuffle partitions after coalescing. This is useful when the adaptively calculated target size is too small during partition coalescing.

	在减少分区之后，shuffle 分区的最小大小。在分区减少期间，自适应地计算出的目标大小太小时是有用的。

- spark.sql.adaptive.coalescePartitions.parallelismFirst	

	Default: true

	Since Version: 3.2.0

	> When true, Spark does not respect the target size specified by 'spark.sql.adaptive.advisoryPartitionSizeInBytes' (default 64MB) when coalescing contiguous shuffle partitions, but adaptively calculate the target size according to the default parallelism of the Spark cluster. The calculated size is usually smaller than the configured target size. This is to maximize the parallelism and avoid performance regression when enabling adaptive query execution. It's recommended to set this config to false and respect the configured target size.

	当为 true 时，spark 在减少连续 shuffle 分区时不会考虑由 `spark.sql.adaptive.advisoryPartitionSizeInBytes` 指定的目标大小，而是根据 spark 集群的默认并行度自适应地计算目标大小。

	计算的大小通常比配置的目标大小小。这就在启用自适应查询执行时最大化了并行度，并避免了性能回退。

	推荐将此值设为 false, 并考虑配置的目标大小。

- spark.sql.adaptive.customCostEvaluatorClass	

	Default: (none)	

	Since Version: 3.2.0

	> The custom cost evaluator class to be used for adaptive execution. If not being set, Spark will use its own SimpleCostEvaluator by default.

	用于自适应执行的自定义的成本评估类。如果不设置，spark 默认使用它自己的 SimpleCostEvaluator

- spark.sql.adaptive.enabled	

	Default: true	

	Since Version: 1.6.0

	> When true, enable adaptive query execution, which re-optimizes the query plan in the middle of query execution, based on accurate runtime statistics.

	当为 true 时，启用自适应查询执行，在查询执行期间，根据运行时统计信息，再次优化查询计划

- spark.sql.adaptive.forceOptimizeSkewedJoin	

	Default: false	

	Since Version: 3.3.0

	> When true, force enable OptimizeSkewedJoin even if it introduces extra shuffle.

	当为 true 时，强制启用 OptimizeSkewedJoin，即使引入额外的 shuffle

- spark.sql.adaptive.localShuffleReader.enabled	

	Default: true	

	Since Version: 3.0.0

	> When true and 'spark.sql.adaptive.enabled' is true, Spark tries to use local shuffle reader to read the shuffle data when the shuffle partitioning is not needed, for example, after converting sort-merge join to broadcast-hash join.

	当为 true 且 `spark.sql.adaptive.enabled` 为 true 时，当不再需要 shuffle 分区时，例如，排序合并join转为广播哈希join后，spark 尝试使用本地 shuffle 阅读器读取 shuffle 数据。

- spark.sql.adaptive.maxShuffledHashJoinLocalMapThreshold	

	Default: 0b	

	Since Version: 3.2.0

	> Configures the maximum size in bytes per partition that can be allowed to build local hash map. If this value is not smaller than `spark.sql.adaptive.advisoryPartitionSizeInBytes` and all the partition size are not larger than this config, join selection prefer to use shuffled hash join instead of sort merge join regardless of the value of `spark.sql.join.preferSortMergeJoin`.

	配置可允许构建本地 hash map 的每个分区的最大字节大小。如果这个值比 `spark.sql.adaptive.advisoryPartitionSizeInBytes` 小，且所有分区大小不大于这个配置，那么不管 `spark.sql.join.preferSortMergeJoin` 值，join 的选择更倾向使用 shuffled hash join ，而不是 sort merge join

- spark.sql.adaptive.optimizeSkewsInRebalancePartitions.enabled	

	Default: true	

	Since Version: 3.2.0

	> When true and 'spark.sql.adaptive.enabled' is true, Spark will optimize the skewed shuffle partitions in RebalancePartitions and split them to smaller ones according to the target size (specified by 'spark.sql.adaptive.advisoryPartitionSizeInBytes'), to avoid data skew.

	当为 true 且 `spark.sql.adaptive.enabled` 为 true 时，spark 将优化 RebalancePartitions 中的倾斜的 shuffle 分区，根据 `spark.sql.adaptive.advisoryPartitionSizeInBytes` 指定的目标大小，将它们划分成更小的分区，以避免数据倾斜。

- spark.sql.adaptive.optimizer.excludedRules	

	Default: (none)

	Since Version: 3.1.0

	> Configures a list of rules to be disabled in the adaptive optimizer, in which the rules are specified by their rule names and separated by comma. The optimizer will log the rules that have indeed been excluded.

	在自适应优化器中，配置禁用的规则列表，在列表中，规则使用规则名称指定和逗号划分。优化器将记录被排除的规则。

- spark.sql.adaptive.rebalancePartitionsSmallPartitionFactor	

	Default: 0.2	

	Since Version: 3.3.0

	> A partition will be merged during splitting if its size is small than this factor multiply `spark.sql.adaptive.advisoryPartitionSizeInBytes`.

	如果一个分区大小小于这个因子乘以 `spark.sql.adaptive.advisoryPartitionSizeInBytes`，那么在划分期间，分区将被合并。

- spark.sql.adaptive.skewJoin.enabled	

	Default: true	

	Since Version: 3.0.0

	> When true and 'spark.sql.adaptive.enabled' is true, Spark dynamically handles skew in shuffled join (sort-merge and shuffled hash) by splitting (and replicating if needed) skewed partitions.

	当为 true 且 `spark.sql.adaptive.enabled` 为 true 时，spark 通过划分（和复制，如果需要）倾斜的分区，动态地处理 shuffled join 中的倾斜

- spark.sql.adaptive.skewJoin.skewedPartitionFactor	

	Default: 5	

	Since Version: 3.0.0

	> A partition is considered as skewed if its size is larger than this factor multiplying the median partition size and also larger than 'spark.sql.adaptive.skewJoin.skewedPartitionThresholdInBytes'

	如果一个分区的大小大于这个因子乘以中位分区大小，且大于 `spark.sql.adaptive.skewJoin.skewedPartitionThresholdInBytes`,那么该分区就被认为是倾斜的。

- spark.sql.adaptive.skewJoin.skewedPartitionThresholdInBytes	

	Default: 256MB	

	Since Version: 3.0.0

	> A partition is considered as skewed if its size in bytes is larger than this threshold and also larger than 'spark.sql.adaptive.skewJoin.skewedPartitionFactor' multiplying the median partition size. Ideally this config should be set larger than 'spark.sql.adaptive.advisoryPartitionSizeInBytes'.

	如果一个分区的大小大于这个阈值，且大于 `spark.sql.adaptive.skewJoin.skewedPartitionFactor` 乘以中位分区大小的结果，那么该分区就被认为是倾斜的。

	理想情况下，应该设置这个值大于 `spark.sql.adaptive.advisoryPartitionSizeInBytes`

- spark.sql.ansi.enabled	

	Default: false	

	Since Version: 3.0.0

	> When true, Spark SQL uses an ANSI compliant dialect instead of being Hive compliant. For example, Spark will throw an exception at runtime instead of returning null results when the inputs to a SQL operator/function are invalid.For full details of this dialect, you can find them in the section "ANSI Compliance" of Spark's documentation. Some ANSI dialect features may be not from the ANSI SQL standard directly, but their behaviors align with ANSI SQL's style

	当为 true 时，spark sql 使用 ANSI 兼容语义，而不是 hive 兼容。例如，当输入的 SQL 运算符/函数是无效时，spark 将在运行时抛出一个异常，而不是返回 null。

- spark.sql.ansi.enforceReservedKeywords	

	Default: false	

	Since Version: 3.3.0

	> When true and 'spark.sql.ansi.enabled' is true, the Spark SQL parser enforces the ANSI reserved keywords and forbids SQL queries that use reserved keywords as alias names and/or identifiers for table, view, function, etc.

	当为 true 且 `spark.sql.adaptive.enabled` 为 true 时，spark sql 解析器强制执行 ANSI 保留关键字，且禁止使用保留字作为别名和/或标识符的查询。

- spark.sql.autoBroadcastJoinThreshold	

	Default: 10MB	

	Since Version: 1.1.0

	> Configures the maximum size in bytes for a table that will be broadcast to all worker nodes when performing a join. By setting this value to -1 broadcasting can be disabled. Note that currently statistics are only supported for Hive Metastore tables where the command `ANALYZE TABLE <tableName> COMPUTE STATISTICS noscan` has been run, and file-based data source tables where the statistics are computed directly on the files of data.

	配置一张表在执行 join 时，将被广播到所有 worker 节点的最大大小。将这个值设为-1表示禁用广播。

	注意，当前，统计信息仅支持 Hive Metastore tables 和基于文件的数据源表。

- spark.sql.avro.compression.codec	

	Default: snappy	

	Since Version: 2.4.0

	> Compression codec used in writing of AVRO files. Supported codecs: uncompressed, deflate, snappy, bzip2, xz and zstandard. Default codec is snappy.

	写入 AVRO 文件使用的压缩编解码。

- spark.sql.avro.deflate.level	

	Default: -1	

	Since Version: 2.4.0

	> Compression level for the deflate codec used in writing of AVRO files. Valid value must be in the range of from 1 to 9 inclusive or -1. The default value is -1 which corresponds to 6 level in the current implementation.

	写入 AVRO 文件使用的 deflate 压缩编解码的压缩级别。有效值必须在1至9范围内，不包含9，或者是-1，默认值-1对应于级别6

- spark.sql.avro.filterPushdown.enabled	

	Default: true	

	Since Version: 3.1.0

	> When true, enable filter pushdown to Avro datasource.

	当为 true 时，将过滤器下推至 Avro 数据源。

- spark.sql.broadcastTimeout	

	Default: 300	

	Since Version: 1.3.0

	> Timeout in seconds for the broadcast wait time in broadcast joins.

	在广播 join 中，广播所等待的超时时长。

- spark.sql.bucketing.coalesceBucketsInJoin.enabled	

	Default: false

	Since Version: 3.1.0

	> When true, if two bucketed tables with the different number of buckets are joined, the side with a bigger number of buckets will be coalesced to have the same number of buckets as the other side. Bigger number of buckets is divisible by the smaller number of buckets. Bucket coalescing is applied to sort-merge joins and shuffled hash join. Note: Coalescing bucketed table can avoid unnecessary shuffling in join, but it also reduces parallelism and could possibly cause OOM for shuffled hash join.

	当为 true 时，如果两个具有不同桶数的分桶表 join, 则具有较多桶数的一方将合并为具有与另一方相同数量的桶。

	较大的桶数可以被较小的桶数整除。桶合并应用到 sort-merge joins 和 shuffled hash join.

	注意，合并的分桶表可用在 join 中避免不必要的 shuffle, 但是也减少了并行度，可能会造成 shuffled hash join 的 OOM

- spark.sql.bucketing.coalesceBucketsInJoin.maxBucketRatio	

	Default: 4	

	Since Version: 3.1.0

	> The ratio of the number of two buckets being coalesced should be less than or equal to this value for bucket coalescing to be applied. This configuration only has an effect when 'spark.sql.bucketing.coalesceBucketsInJoin.enabled' is set to true.

	被合并的两个桶的数量的比例应该小于或等于这个值。这个配置仅在 `spark.sql.bucketing.coalesceBucketsInJoin.enabled` 为 true 时有用。

- spark.sql.catalog.spark_catalog	

	Default: (none)	

	Since Version: 3.0.0

	> A catalog implementation that will be used as the v2 interface to Spark's built-in v1 catalog: spark_catalog. This catalog shares its identifier namespace with the spark_catalog and must be consistent with it; for example, if a table can be loaded by the spark_catalog, this catalog must also return the table metadata. To delegate operations to the spark_catalog, implementations can extend 'CatalogExtension'.

- spark.sql.cbo.enabled	

	Default: false

	Since Version: 2.2.0

	> Enables CBO for estimation of plan statistics when set true.

	当设为 true 时，为计划统计信息的估算启用 CBO

- spark.sql.cbo.joinReorder.dp.star.filter	

	Default: false	

	Since Version: 2.2.0

	> Applies star-join filter heuristics to cost based join enumeration.


- spark.sql.cbo.joinReorder.dp.threshold	

	Default: 12

	Since Version: 2.2.0

	> The maximum number of joined nodes allowed in the dynamic programming algorithm.

	在动态编程算法中，允许 join 的节点的最大数量。

- spark.sql.cbo.joinReorder.enabled	

	Default: false

	Since Version: 2.2.0

	> Enables join reorder in CBO.

	在 CBO 中启用 join 的重排序

- spark.sql.cbo.planStats.enabled	

	Default: false	

	Since Version: 3.0.0

	> When true, the logical plan will fetch row counts and column statistics from catalog.

	当设为 true 时，逻辑计划将从 catalog 获取行计数和列统计信息。

- spark.sql.cbo.starSchemaDetection	

	Default: false

	Since Version: 2.2.0

	> When true, it enables join reordering based on star schema detection.

	当设为 true 时，基于星型模型检测，启用 join 的重排序

- spark.sql.charAsVarchar	

	Default: false	

	Since Version: 3.3.0

	> When true, Spark replaces CHAR type with VARCHAR type in CREATE/REPLACE/ALTER TABLE commands, so that newly created/updated tables will not have CHAR type columns/fields. Existing tables with CHAR type columns/fields are not affected by this config.

	当设为 true 时，spark 在 CREATE/REPLACE/ALTER TABLE 命令中使用 VARCHAR 类型取代 CHAR 类型，为了创建/更新的表不再有 CHAR 类型的列/字段。具有 CHAR 类型的列/字段的已存在的表不受这个配置影响。

- spark.sql.cli.print.header	

	Default: false	

	Since Version: 3.2.0

	> When set to true, spark-sql CLI prints the names of the columns in query output.

	当设为 true 时，在查询输出中，spark-sql CLI 打印列名。

- spark.sql.columnNameOfCorruptRecord	

	Default: `_corrupt_record`	

	Since Version: 1.2.0

	> The name of internal column for storing raw/un-parsed JSON and CSV records that fail to parse.

	为存储原生的、未解析的 JSON 和 CSV 记录的中间列的名称。

- spark.sql.csv.filterPushdown.enabled	

	Default: true

	Since Version: 3.0.0

	> When true, enable filter pushdown to CSV datasource.

	当设为 true 时，将过滤器下推到 CSV 数据源。

- spark.sql.datetime.java8API.enabled	

	Default: false	

	Since Version: 3.0.0

	> If the configuration property is set to true, `java.time.Instant` and `java.time.LocalDate` classes of Java 8 API are used as external types for Catalyst's TimestampType and DateType. If it is set to false, `java.sql.Timestamp` and `java.sql.Date` are used for the same purpose.

	当设为 true 时，Java 8 API 的 `java.time.Instant` 和 `java.time.LocalDate` 类用作 Catalyst 的 TimestampType 和 DateType 的外部类型。如果为 false, `java.sql.Timestamp` 和 `java.sql.Date` 用于相同的目的。

- spark.sql.debug.maxToStringFields	

	Default: 25	

	Since Version: 3.0.0

	> Maximum number of fields of sequence-like entries can be converted to strings in debug output. Any elements beyond the limit will be dropped and replaced by a "... N more fields" placeholder.

	类似序列的项的字段最大数量在 debug 输出中被转换成字符串。超过这个限制的任意元素将被删除，由 "... N more fields" 占位符取代。

- spark.sql.defaultCatalog	

	Default: spark_catalog	

	Since Version: 3.0.0

	> Name of the default catalog. This will be the current catalog if users have not explicitly set the current catalog yet.

	默认的 catalog 的名称。如果用户没有明确设置当前的 catalog, 那么这将就是当前的 catalog.

- spark.sql.execution.arrow.enabled	

	Default: false

	Since Version: 2.3.0

	> (Deprecated since Spark 3.0, please set 'spark.sql.execution.arrow.pyspark.enabled'.)

- spark.sql.execution.arrow.fallback.enabled	

	Default: true	

	Since Version: 2.4.0

	> (Deprecated since Spark 3.0, please set 'spark.sql.execution.arrow.pyspark.fallback.enabled'.)

- spark.sql.execution.arrow.maxRecordsPerBatch	

	Default: 10000	

	Since Version: 2.3.0

	> When using Apache Arrow, limit the maximum number of records that can be written to a single ArrowRecordBatch in memory. If set to zero or negative there is no limit.

	当使用 Apache Arrow 时，限制写入内存 ArrowRecordBatch 的记录的最大数量。如果为0或负数，则没有限制。

- spark.sql.execution.arrow.pyspark.enabled	

	Default: (value of spark.sql.execution.arrow.enabled)	

	Since Version: 3.0.0

	> When true, make use of Apache Arrow for columnar data transfers in PySpark. This optimization applies to: 1. `pyspark.sql.DataFrame.toPandas` 2. `pyspark.sql.SparkSession.createDataFrame` when its input is a Pandas DataFrame The following data types are unsupported: ArrayType of TimestampType, and nested StructType.

	当设为 true 时，在 PySpark 中，使用 Apache Arrow 传输列式数据。这种优化应用于：1. `pyspark.sql.DataFrame.toPandas` 2. `pyspark.sql.SparkSession.createDataFrame`，当它的输入是 Pandas DataFrame

	不支持下列数据类型: 元素为 TimestampType 的 ArrayType, 和嵌套的 StructType.

- spark.sql.execution.arrow.pyspark.fallback.enabled	

	Default: (value of spark.sql.execution.arrow.fallback.enabled)

	Since Version: 3.0.0

	> When true, optimizations enabled by 'spark.sql.execution.arrow.pyspark.enabled' will fallback automatically to non-optimized implementations if an error occurs.

	当设为 true 时，通过 `spark.sql.execution.arrow.pyspark.enabled` 启用的优化，在出现错误时，将自动回退到非优化的实现。

- spark.sql.execution.arrow.pyspark.selfDestruct.enabled	

	Default: false

	Since Version: 3.2.0

	> (Experimental) When true, make use of Apache Arrow's self-destruct and split-blocks options for columnar data transfers in PySpark, when converting from Arrow to Pandas. This reduces memory usage at the cost of some CPU time. This optimization applies to: `pyspark.sql.DataFrame.toPandas` when 'spark.sql.execution.arrow.pyspark.enabled' is set.

	（实验）当为 true 时，将 Arrow 转换成 Pandas 时，在 PySpark 中，使用 Apache Arrow 的自毁和拆分块选项传输列式数据。这会以花费一些 CPU 时间减少内存使用。

	这个优化在启用 spark.sql.execution.arrow.pyspark.enabled 时应用于 `pyspark.sql.DataFrame.toPandas`

- spark.sql.execution.arrow.sparkr.enabled	

	Default: false	

	Since Version: 3.0.0

	> When true, make use of Apache Arrow for columnar data transfers in SparkR. This optimization applies to: 1. createDataFrame when its input is an R DataFrame 2. collect 3. dapply 4. gapply The following data types are unsupported: FloatType, BinaryType, ArrayType, StructType and MapType.

	当设为 true 时，在 SparkR 中，使用 Apache Arrow 传输列式数据。这种优化应用于：1. createDataFrame, 当它的输入是 R DataFrame 时  2. collect 3. dapply 4. gapply

	不支持下列数据类型: FloatType, BinaryType, ArrayType, StructType and MapType

- spark.sql.execution.pandas.udf.buffer.size	

	Default: (value of spark.buffer.size)	

	Since Version: 3.0.0

	> Same as `spark.buffer.size` but only applies to Pandas UDF executions. If it is not set, the fallback is `spark.buffer.size`. Note that Pandas execution requires more than 4 bytes. Lowering this value could make small Pandas UDF batch iterated and pipelined; however, it might degrade performance. See SPARK-27870.

	和 `spark.buffer.size` 相同，但仅用于 Pandas UDF 执行。如果没有设置，就会使用 `spark.buffer.size`.

	Pandas 执行要求多于4个字节。降低这个值会让小的 Pandas UDF 批次迭代和管道化，然而会降低性能。

- spark.sql.execution.pyspark.udf.simplifiedTraceback.enabled	

	Default: true	

	Since Version: 3.1.0

	> When true, the traceback from Python UDFs is simplified. It hides the Python worker, (de)serialization, etc from PySpark in tracebacks, and only shows the exception messages from UDFs. Note that this works only with CPython 3.7+.

	当设为 true 时，来自 Python UDFs 的回溯被简化。它隐藏了来自 PySpark 的 Python worker, (de)serialization 等，仅展示来自 UDFs 的异常信息。

	注意这仅应用于 CPython 3.7+.

- spark.sql.execution.topKSortFallbackThreshold	

	Default: 2147483632

	Since Version: 2.4.0

	> In SQL queries with a SORT followed by a LIMIT like 'SELECT x FROM t ORDER BY y LIMIT m', if m is under this threshold, do a top-K sort in memory, otherwise do a global sort which spills to disk if necessary.

	在 SORT 后紧跟 LIMIT 的查询中，就像是 `SELECT x FROM t ORDER BY y LIMIT m`, 如果 m 低于这个阈值，就会在内存中做 top-K 排序，否则就会做全局排序，如果必要的话，就会溢写磁盘。

- spark.sql.files.ignoreCorruptFiles	

	Default: false	

	Since Version: 2.1.1

	> Whether to ignore corrupt files. If true, the Spark jobs will continue to run when encountering corrupted files and the contents that have been read will still be returned. This configuration is effective only when using file-based sources such as Parquet, JSON and ORC.

	是否忽略损坏的文件。如果为 true, spark 作业在遇到损坏文件时继续运行，读取到的内存将仍被返回。

	这个配置仅在使用基于文件的数据源时才有效。

- spark.sql.files.ignoreMissingFiles	

	Default: false	

	Since Version: 2.3.0

	> Whether to ignore missing files. If true, the Spark jobs will continue to run when encountering missing files and the contents that have been read will still be returned. This configuration is effective only when using file-based sources such as Parquet, JSON and ORC.

	是否忽略缺失的文件。如果为 true, spark 作业在遇到缺失文件时继续运行，读取到的内存将仍被返回。

	这个配置仅在使用基于文件的数据源时才有效。

- spark.sql.files.maxPartitionBytes	

	Default: 128MB	

	Since Version: 2.0.0

	> The maximum number of bytes to pack into a single partition when reading files. This configuration is effective only when using file-based sources such as Parquet, JSON and ORC.

	当读取文件时，打包进一个分区的最大字节数。这个配置仅在使用基于文件的数据源时才有效。

- spark.sql.files.maxRecordsPerFile	

	Default: 0	

	Since Version: 2.2.0

	> Maximum number of records to write out to a single file. If this value is zero or negative, there is no limit.

	写入一个文件的最大记录数量。如果值为0或负数，就没有限制。

- spark.sql.files.minPartitionNum	

	Default: (none)	

	Since Version: 3.1.0

	> The suggested (not guaranteed) minimum number of split file partitions. If not set, the default value is `spark.default.parallelism`. This configuration is effective only when using file-based sources such as Parquet, JSON and ORC.

	建议的（不是保证）划分文件分区的最小数量。如果不设置，默认值就是 `spark.default.parallelism`. 

	这个配置仅在使用基于文件的数据源时才有效。

- spark.sql.function.concatBinaryAsString	

	Default: false	

	Since Version: 2.3.0

	> When this option is set to false and all inputs are binary, `functions.concat` returns an output as binary. Otherwise, it returns as a string.

	当这个选项是 false 且所有的输入是二进制时，`functions.concat` 返回二进制的输出。否则，返回字符串。

- spark.sql.function.eltOutputAsString	

	Default: false	

	Since Version: 2.3.0

	> When this option is set to false and all inputs are binary, elt returns an output as binary. Otherwise, it returns as a string.

	当这个选项是 false 且所有的输入是二进制时，elt 返回二进制的输出。否则，返回字符串。

- spark.sql.groupByAliases	

	Default: true	

	Since Version: 2.2.0

	> When true, aliases in a select list can be used in group by clauses. When false, an analysis exception is thrown in the case.

	当为 true 时，select 列表中的别名可用在 group by 子句中。当为 false 时，就会抛出分析异常。

- spark.sql.groupByOrdinal	

	Default: true	

	Since Version: 2.0.0

	> When true, the ordinal numbers in group by clauses are treated as the position in the select list. When false, the ordinal numbers are ignored.

	当为 true 时，group by 子句中的序数被当作 select 列表中的位置。当为 false 时，就会忽略序数。

- spark.sql.hive.convertInsertingPartitionedTable	

	Default: true	

	Since Version: 3.0.0

	> When set to true, and `spark.sql.hive.convertMetastoreParquet` or `spark.sql.hive.convertMetastoreOrc` is true, the built-in ORC/Parquet writer is used to process inserting into partitioned ORC/Parquet tables created by using the HiveSQL syntax.

	当为 true 时，同时 `spark.sql.hive.convertMetastoreParquet` 或 `spark.sql.hive.convertMetastoreOrc` 也为 true 时，内建 ORC/Parquet 写入器用于处理插入分区的 ORC/Parquet 表，此表使用 HiveSQL 语法创建。

- spark.sql.hive.convertMetastoreCtas	

	Default: true	

	Since Version: 3.0.0

	> When set to true, Spark will try to use built-in data source writer instead of Hive serde in CTAS. This flag is effective only if `spark.sql.hive.convertMetastoreParquet` or `spark.sql.hive.convertMetastoreOrc` is enabled respectively for Parquet and ORC formats

	当为 true 时，在 CTAS 中，spark 将尝试使用内建的数据源写入器，而不是 Hive serde

	对于 Parquet 和 ORC 格式，这个标志仅在 `spark.sql.hive.convertMetastoreParquet` 或 `spark.sql.hive.convertMetastoreOrc` 启用时有效。

- spark.sql.hive.convertMetastoreInsertDir	

	Default: true	

	Since Version: 3.3.0

	> When set to true, Spark will try to use built-in data source writer instead of Hive serde in INSERT OVERWRITE DIRECTORY. This flag is effective only if `spark.sql.hive.convertMetastoreParquet` or `spark.sql.hive.convertMetastoreOrc` is enabled respectively for Parquet and ORC formats
	
	当为 true 时，在 INSERT OVERWRITE DIRECTORY 中，spark 将尝试使用内建的数据源写入器，而不是 Hive serde

	对于 Parquet 和 ORC 格式，这个标志仅在 `spark.sql.hive.convertMetastoreParquet` 或 `spark.sql.hive.convertMetastoreOrc` 启用时有效。

- spark.sql.hive.convertMetastoreOrc	

	Default: true	

	Since Version: 2.0.0

	> When set to true, the built-in ORC reader and writer are used to process ORC tables created by using the HiveQL syntax, instead of Hive serde.

	当为 true 时，内建的 ORC 读取器和写入器用于处理使用 HiveQL 语法创建的 ORC 表，而不是 Hive serde 

- spark.sql.hive.convertMetastoreParquet	

	Default: true	

	Since Version: 1.1.1

	> When set to true, the built-in Parquet reader and writer are used to process parquet tables created by using the HiveQL syntax, instead of Hive serde.

	当为 true 时，内建的 Parquet 读取器和写入器用于处理使用 HiveQL 语法创建的 parquet 表，而不是 Hive serde 

- spark.sql.hive.convertMetastoreParquet.mergeSchema	

	Default: false

	Since Version: 1.3.1

	> When true, also tries to merge possibly different but compatible Parquet schemas in different Parquet data files. This configuration is only effective when "spark.sql.hive.convertMetastoreParquet" is true.

	当为 true 时，尝试合并可能不同，但在不同 Parquet 数据文件中兼容的 Parquet schemas

	这个配置仅在 `spark.sql.hive.convertMetastoreParquet` 为 true 时有效。

- spark.sql.hive.filesourcePartitionFileCacheSize	

	Default: 262144000

	Since Version: 2.1.1

	> When nonzero, enable caching of partition file metadata in memory. All tables share a cache that can use up to specified num bytes for file metadata. This conf only has an effect when hive filesource partition management is enabled.

	当为非0时，启用内存中的分区文件元数据的缓存。所有表共享一个缓存区，该缓存最多可为文件元数据使用指定的 num 字节。

	这个配置仅在 hive 文件源分区管理启用时有效。

- spark.sql.hive.manageFilesourcePartitions	

	Default: true	

	Since Version: 2.1.1

	> When true, enable metastore partition management for file source tables as well. This includes both datasource and converted Hive tables. When partition management is enabled, datasource tables store partition in the Hive metastore, and use the metastore to prune partitions during query planning when `spark.sql.hive.metastorePartitionPruning` is set to true.

	当为 true 时，为文件源表启用元数据分区管理。这包含数据源和转换的 hive 表。

	当启用分区管理时，数据源表在 hive 元数据库中存储分区，在 `spark.sql.hive.metastorePartitionPruning` 为 true, 且在查询计划期间，使用元数据库进行修剪分区。

- spark.sql.hive.metastorePartitionPruning	

	Default: true	

	Since Version: 1.5.0

	> When true, some predicates will be pushed down into the Hive metastore so that unmatching partitions can be eliminated earlier.

	当为 true 时，一些谓词将下推到 hive 元数据库，为了能及早消除未匹配的分区。

- spark.sql.hive.metastorePartitionPruningFallbackOnException	

	Default: false

	Since Version: 3.3.0

	> Whether to fallback to get all partitions from Hive metastore and perform partition pruning on Spark client side, when encountering MetaException from the metastore. Note that Spark query performance may degrade if this is enabled and there are many partitions to be listed. If this is disabled, Spark will fail the query instead.

	当在元数据库中遭遇 MetaException 异常时，是否进行回退，以从 hive 元数据库中获取所有的分区，并在 spark 客户端执行分区裁剪。

	如果启用，会降低 spark 查询性能，会列出很多分区。如果禁用，那么 spark 将会查询失败。 

- spark.sql.hive.metastorePartitionPruningFastFallback	

	Default: false	

	Since Version: 3.3.0

	> When this config is enabled, if the predicates are not supported by Hive or Spark does fallback due to encountering MetaException from the metastore, Spark will instead prune partitions by getting the partition names first and then evaluating the filter expressions on the client side. Note that the predicates with TimeZoneAwareExpression is not supported.

	当启用时，如果 hive 不支持这个谓词，或者 spark 由于在元数据库中遭遇 MetaException 异常进行回退，那么 spark 将剪裁分区： 首先获取分区名，然后在客户端计算过滤表达式。

	不支持具有 TimeZoneAwareExpression 的谓词。

- spark.sql.hive.thriftServer.async	

	Default: true	

	Since Version: 1.5.0

	> When set to true, Hive Thrift server executes SQL queries in an asynchronous way.

	当设为 true 时，Hive Thrift server 以异步的方式执行 sql 查询。

- spark.sql.hive.verifyPartitionPath	

	Default: false

	Since Version: 1.4.0

	> When true, check all the partition paths under the table's root directory when reading data stored in HDFS. This configuration will be deprecated in the future releases and replaced by `spark.files.ignoreMissingFiles`.

	当设为 true, 且读取 HDFS 中的数据时，会检查表的根目录下的所有分区路径。

	未来将使用 `spark.files.ignoreMissingFiles` 替代它。

- spark.sql.inMemoryColumnarStorage.batchSize	

	Default: 10000

	Since Version: 1.1.1

	> Controls the size of batches for columnar caching. Larger batch sizes can improve memory utilization and compression, but risk OOMs when caching data.

	控制着列式缓存的批次大小。大批次可用改善内存利用率和压缩性能，但是在缓存数据时，会有 OOM 的风险。

- spark.sql.inMemoryColumnarStorage.compressed	

	Default: true	

	Since Version: 1.0.1

	> When set to true Spark SQL will automatically select a compression codec for each column based on statistics of the data.

	当设为 true 时，spark sql 将根据数据的统计信息，自动为每列选择一个压缩编解码器。

- spark.sql.inMemoryColumnarStorage.enableVectorizedReader	

	Default: true

	Since Version: 2.3.1

	> Enables vectorized reader for columnar caching.

	为列式缓存，启用向量化阅读器。

- spark.sql.json.filterPushdown.enabled	

	Default: true

	Since Version: 3.1.0

	> When true, enable filter pushdown to JSON datasource.

	当设为 true 时，将过滤器下推到 JSON 数据源。

- spark.sql.jsonGenerator.ignoreNullFields	

	Default: true	

	Since Version: 3.0.0

	> Whether to ignore null fields when generating JSON objects in JSON data source and JSON functions such as to_json. If false, it generates null for null fields in JSON objects.

	在 JSON 数据源和 JSON 函数中生成 JSON 对象时，是否忽略 null 字段。

	如果为 false, 将会在 JSON 对象中为 null 字段生成 null

- spark.sql.leafNodeDefaultParallelism	

	Default: (none)	

	Since Version: 3.2.0

	> The default parallelism of Spark SQL leaf nodes that produce data, such as the file scan node, the local data scan node, the range node, etc. The default value of this config is 'SparkContext#defaultParallelism'.

	spark sql 生成数据的叶节点的默认并行度，例如文件扫描节点、本地数据扫描节点、范围节点等。

	这个配置的默认值是 `SparkContext#defaultParallelism`

- spark.sql.mapKeyDedupPolicy	

	Default: EXCEPTION	

	Since Version: 3.0.0

	> The policy to deduplicate map keys in builtin function: CreateMap, MapFromArrays, MapFromEntries, StringToMap, MapConcat and TransformKeys. When EXCEPTION, the query fails if duplicated map keys are detected. When LAST_WIN, the map key that is inserted at last takes precedence.

	在内建函数中删除重复的 map keys 的策略。当为 EXCEPTION 时，如果检查到了重复的 map keys, 那么查询失败。当为 LAST_WIN 时，最后插入的 map key 优先。

- spark.sql.maven.additionalRemoteRepositories	

	Default: https://maven-central.storage-download.googleapis.com/maven2/

	Since Version: 3.0.0

	> A comma-delimited string config of the optional additional remote Maven mirror repositories. This is only used for downloading Hive jars in IsolatedClientLoader if the default Maven Central repo is unreachable.

	逗号分隔的可选的额外远程 Maven 镜像仓库的字符串。这个仅用于默认的 Maven 中心仓库不可获取时，在 IsolatedClientLoader 下载 Hive jars 的情况。

- spark.sql.maxMetadataStringLength	

	Default: 100	

	Since Version: 3.1.0

	> Maximum number of characters to output for a metadata string. e.g. file location in `DataSourceScanExec`, every value will be abbreviated if exceed length.

	输出的一个元数据字符串的最大字符数量，例如，DataSourceScanExec 中的文件位置，如果超过长度，那么每个值将被缩写

- spark.sql.maxPlanStringLength	

	Default: 2147483632	

	Since Version: 3.0.0

	> Maximum number of characters to output for a plan string. If the plan is longer, further output will be truncated. The default setting always generates a full plan. Set this to a lower value such as 8k if plan strings are taking up too much memory or are causing OutOfMemory errors in the driver or UI processes.

	输出的一个计划字符串的最大字符数量。计划越长，输出被截断的就越多。默认的设置总会生成一个完整的计划。如果计划字符串在驱动或 UI 进程中占据了太多的内存，或造成了 OOM 错误，那么就将这个值设置成一个较小的值，例如 8K

- spark.sql.optimizer.collapseProjectAlwaysInline	

	Default: false	

	Since Version: 3.3.0

	> Whether to always collapse two adjacent projections and inline expressions even if it causes extra duplication.

	是否总是折叠两个相邻的投影和内联表达式，即使这会导致额外的重复。

- spark.sql.optimizer.dynamicPartitionPruning.enabled	

	Default: true

	Since Version: 3.0.0

	> When true, we will generate predicate for partition column when it's used as join key

	如果为 true, 且分区列用作 join 键，那么将为分区列生成谓词。 

- spark.sql.optimizer.enableCsvExpressionOptimization	

	Default: true

	Since Version: 3.2.0

	> Whether to optimize CSV expressions in SQL optimizer. It includes pruning unnecessary columns from from_csv.

	是否在 SQL 优化器中优化 CSV 表达式。它包括了裁剪 from_csv 中的不必要的列。

- spark.sql.optimizer.enableJsonExpressionOptimization	

	Default: true	

	Since Version: 3.1.0

	> Whether to optimize JSON expressions in SQL optimizer. It includes pruning unnecessary columns from from_json, simplifying `from_json + to_json`, `to_json + named_struct(from_json.col1, from_json.col2, ....)`.

	是否在 SQL 优化器中优化 JSON 表达式。它包括了裁剪 from_json 中的不必要的列，简化 `from_json + to_json`, `to_json + named_struct(from_json.col1, from_json.col2, ....)`.

- spark.sql.optimizer.excludedRules	

	Default: (none)	

	Since Version: 2.4.0

	> Configures a list of rules to be disabled in the optimizer, in which the rules are specified by their rule names and separated by comma. It is not guaranteed that all the rules in this configuration will eventually be excluded, as some rules are necessary for correctness. The optimizer will log the rules that have indeed been excluded.

	配置优化器中禁用的规则列表，在列表中，使用规则名称指定，逗号分隔规则。

	它并不保证这个配置中的所有规则最终都被排除，因为一些规则对于正确性是必要的。优化器将记录实际被排除的规则。

- spark.sql.optimizer.runtime.bloomFilter.applicationSideScanSizeThreshold	

	Default: 10GB	

	Since Version: 3.3.0

	> Byte size threshold of the Bloom filter application side plan's aggregated scan size. Aggregated scan byte size of the Bloom filter application side needs to be over this value to inject a bloom filter.

	布隆过滤器应用程序端计划的聚合扫描大小的阈值。它需要超过这个值，以映射到一个布隆过滤器上。

- spark.sql.optimizer.runtime.bloomFilter.creationSideThreshold	

	Default: 10MB	

	Since Version: 3.3.0

	> Size threshold of the bloom filter creation side plan. Estimated size needs to be under this value to try to inject bloom filter.

	布隆过滤器创建端计划的阈值。估算大小需要低于这个值，以尝试映射到一个布隆过滤器上。

- spark.sql.optimizer.runtime.bloomFilter.enabled	

	Default: false

	Since Version: 3.3.0

	> When true and if one side of a shuffle join has a selective predicate, we attempt to insert a bloom filter in the other side to reduce the amount of shuffle data.

	当为 true 时，且如果 shuffle join 的一端有一个可选的谓词，那么我们尝试在另一端插入一个布隆过滤器，以减少 shuffle 数据的数据量。

- spark.sql.optimizer.runtime.bloomFilter.expectedNumItems	

	Default: 1000000	

	Since Version: 3.3.0

	> The default number of expected items for the runtime bloomfilter

	对于运行时布隆过滤器，预期项的默认数量。

- spark.sql.optimizer.runtime.bloomFilter.maxNumBits	

	Default: 67108864

	Since Version: 3.3.0

	> The max number of bits to use for the runtime bloom filter

	用于运行时布隆过滤器的位的最大数量。

- spark.sql.optimizer.runtime.bloomFilter.maxNumItems	

	Default: 4000000	

	Since Version: 3.3.0

	> The max allowed number of expected items for the runtime bloom filter

	对于运行时布隆过滤器，预期项的最大允许的数量。

- spark.sql.optimizer.runtime.bloomFilter.numBits	

	Default: 8388608	

	Since Version: 3.3.0

	> The default number of bits to use for the runtime bloom filter

	用于运行时布隆过滤器的位的默认数量。

- spark.sql.optimizer.runtimeFilter.number.threshold	

	Default: 10	

	Since Version: 3.3.0

	> The total number of injected runtime filters (non-DPP) for a single query. This is to prevent driver OOMs with too many Bloom filters.

	对于一个查询，映射的运行时过滤器的总数量。这就避免了驱动因太多的布隆过滤器而产生 OOMs

- spark.sql.optimizer.runtimeFilter.semiJoinReduction.enabled	

	Default: false	

	Since Version: 3.3.0

	> When true and if one side of a shuffle join has a selective predicate, we attempt to insert a semi join in the other side to reduce the amount of shuffle data.
	
	当为 true 时，且如果 shuffle join 的一端有一个可选的谓词，那么我们尝试在另一端插入一个 semi join，以减少 shuffle 数据的数据量。

- spark.sql.orc.aggregatePushdown	

	Default: false	

	Since Version: 3.3.0

	> If true, aggregates will be pushed down to ORC for optimization. Support MIN, MAX and COUNT as aggregate expression. For MIN/MAX, support boolean, integer, float and date type. For COUNT, support all data types. If statistics is missing from any ORC file footer, exception would be thrown.

	当为 true 时，将聚合下推到 ORC. 支持将 MIN, MAX 和 COUNT 作为聚合表达式。对于 MIN/MAX, 支持 boolean, integer, float 和 date 类型。对于 COUNT, 支持所有的数据类型。

	如果在任意的 ORC 文件页脚缺少统计信息，将会抛异常。

- spark.sql.orc.columnarReaderBatchSize	

	Default: 4096	

	Since Version: 2.4.0

	> The number of rows to include in a orc vectorized reader batch. The number should be carefully chosen to minimize overhead and avoid OOMs in reading data.

	在 orc 向量化的阅读器批次中，包含的行的数量。在读取数据中，应该小心地选择数据，以最小化开销，避免 OOMs.

- spark.sql.orc.compression.codec	

	Default: snappy	

	Since Version: 2.3.0

	> Sets the compression codec used when writing ORC files. If either `compression` or `orc.compress` is specified in the table-specific options/properties, the precedence would be `compression`, `orc.compress`, `spark.sql.orc.compression.codec`. Acceptable values include: none, uncompressed, snappy, zlib, lzo, zstd, lz4.

	在写入 ORC 文件时，设置使用的压缩编解码器。如果在表的选项/属性中，指定了 `compression` 或 `orc.compress`, 那么优先级将会是 `compression`, `orc.compress`, `spark.sql.orc.compression.codec`。可接受的值包含: none, uncompressed, snappy, zlib, lzo, zstd, lz4.

- spark.sql.orc.enableNestedColumnVectorizedReader	

	Default: false	

	Since Version: 3.2.0

	> Enables vectorized orc decoding for nested column.

	为嵌套的列，启用向量化的 ORC 解码。

- spark.sql.orc.enableVectorizedReader	

	Default: true	

	Since Version: 2.3.0

	> Enables vectorized orc decoding.

	启用向量化的 ORC 解码

- spark.sql.orc.filterPushdown	

	Default: true

	Since Version: 1.4.0

	> When true, enable filter pushdown for ORC files.

	当为 true 时，为 ORC 文件启用过滤下推。

- spark.sql.orc.mergeSchema	

	Default: false	

	Since Version: 3.0.0

	> When true, the Orc data source merges schemas collected from all data files, otherwise the schema is picked from a random data file.

	当为 true 时，ORC 数据源合并来自所有数据文件的 schemas, 否则将从随机数据文件中选择。

- spark.sql.orderByOrdinal	

	Default: true

	Since Version: 2.0.0

	> When true, the ordinal numbers are treated as the position in the select list. When false, the ordinal numbers in order/sort by clause are ignored.

	当为 true 时，序号被作为 select 列表中的位置。当为 false 时，会忽略 order/sort by 子句中的序号。

- spark.sql.parquet.aggregatePushdown	

	Default: false	

	Since Version: 3.3.0

	> If true, aggregates will be pushed down to Parquet for optimization. Support MIN, MAX and COUNT as aggregate expression. For MIN/MAX, support boolean, integer, float and date type. For COUNT, support all data types. If statistics is missing from any Parquet file footer, exception would be thrown.

	当为 true 时，将聚合下推到 Parquet. 支持将 MIN, MAX 和 COUNT 作为聚合表达式。对于 MIN/MAX, 支持 boolean, integer, float 和 date 类型。对于 COUNT, 支持所有的数据类型。

	如果在任意的 Parquet 文件页脚缺少统计信息，将会抛异常。

- spark.sql.parquet.binaryAsString	

	Default: false	

	Since Version: 1.1.1

	> Some other Parquet-producing systems, in particular Impala and older versions of Spark SQL, do not differentiate between binary data and strings when writing out the Parquet schema. This flag tells Spark SQL to interpret binary data as a string to provide compatibility with these systems.

	一些其他的 Parquet 生成系统，特别是 Impala 和旧版本的Spark SQL，在编写 Parquet schema 时，不区分二进制数据和字符串。

	这个标志告诉 Spark SQL 将二进制数据解释成字符串，以保证兼容性。

- spark.sql.parquet.columnarReaderBatchSize	

	Default: 4096	

	Since Version: 2.4.0

	> The number of rows to include in a parquet vectorized reader batch. The number should be carefully chosen to minimize overhead and avoid OOMs in reading data.

	在 parquet 向量化的阅读器批次中，包含的行的数量。在读取数据中，应该小心地选择数据，以最小化开销，避免 OOMs.

- spark.sql.parquet.compression.codec	

	Default: snappy	

	Since Version: 1.1.1

	> Sets the compression codec used when writing Parquet files. If either `compression` or `parquet.compression` is specified in the table-specific options/properties, the precedence would be `compression`, `parquet.compression`, `spark.sql.parquet.compression.codec`. Acceptable values include: none, uncompressed, snappy, gzip, lzo, brotli, lz4, zstd.

	在写入 Parquet 文件时，设置使用的压缩编解码器。如果在表的选项/属性中，指定了 `compression` 或 `parquet.compression`, 那么优先级将会是 `compression`, `parquet.compression`, `spark.sql.parquet.compression.codec`。可接受的值包含: none, uncompressed, snappy, gzip, lzo, brotli, lz4, zstd.

- spark.sql.parquet.enableNestedColumnVectorizedReader	

	Default: false	

	Since Version: 3.3.0

	> Enables vectorized Parquet decoding for nested columns (e.g., struct, list, map). Requires `spark.sql.parquet.enableVectorizedReader` to be enabled.

	为嵌套的列，启用向量化的 Parquet 解码。需要启用 `spark.sql.parquet.enableVectorizedReader`

- spark.sql.parquet.enableVectorizedReader	

	Default: true	

	Since Version: 2.0.0

	> Enables vectorized parquet decoding.

	启用向量化的 parquet 解码

- spark.sql.parquet.fieldId.read.enabled	

	Default: false	

	Since Version: 3.3.0

	> Field ID is a native field of the Parquet schema spec. When enabled, Parquet readers will use field IDs (if present) in the requested Spark schema to look up Parquet fields instead of using column names

	字段 ID 是 Parquet schema 规范中的原生字段。当启用时，Parquet 阅读器将使用请求的 Spark schema 中的字段 IDs （若存在），来查找 Parquet 字段，而不是使用列名。 

- spark.sql.parquet.fieldId.read.ignoreMissing	

	Default: false	

	Since Version: 3.3.0

	> When the Parquet file doesn't have any field IDs but the Spark read schema is using field IDs to read, we will silently return nulls when this flag is enabled, or error otherwise.

	当 Parquet 文件没有任意字段 IDs, 但 spark 读取 schema 正使用字段 IDs, 此时如果启用了这个标志，那么将返回 nulls, 否则返回错误。 

- spark.sql.parquet.fieldId.write.enabled	

	Default: true	

	Since Version: 3.3.0

	> Field ID is a native field of the Parquet schema spec. When enabled, Parquet writers will populate the field Id metadata (if present) in the Spark schema to the Parquet schema.

	字段 ID 是 Parquet schema 规范中的原生字段。当启用时，Parquet 写入器将 Spark schema 中的字段Id元数据（若存在）填充到 Parquet schema 中。

- spark.sql.parquet.filterPushdown	

	Default: true

	Since Version: 1.2.0

	> Enables Parquet filter push-down optimization when set to true.

	当为 true 时，启用 Parquet 过滤器的下推优化。

- spark.sql.parquet.int96AsTimestamp	

	Default: true	

	Since Version: 1.3.0

	> Some Parquet-producing systems, in particular Impala, store Timestamp into INT96. Spark would also store Timestamp as INT96 because we need to avoid precision lost of the nanoseconds field. This flag tells Spark SQL to interpret INT96 data as a timestamp to provide compatibility with these systems.

	一些 Parquet 生成系统，特别是 Impala, 将 Timestamp 存储成 INT96. spark 也会将 Timestamp 存储成 INT96, 因为我们需要避免纳秒字段的精度丢失。这个标志告诉 spark sql 将 INT96 数据解释成一个时间戳，以提供兼容性。

- spark.sql.parquet.int96TimestampConversion	

	Default: false	

	Since Version: 2.3.0

	> This controls whether timestamp adjustments should be applied to INT96 data when converting to timestamps, for data written by Impala. This is necessary because Impala stores INT96 data with a different timezone offset than Hive & Spark.

	对于由 Impala 写入的数据，这控制着在转换成时间戳时，是否将时间戳调整应用到 INT96 数据。这是必要的，因为 Impala 使用与 Hive & Spark 不同的时区偏移存储 INT96 数据。

- spark.sql.parquet.mergeSchema	

	Default: false	

	Since Version: 1.5.0

	> When true, the Parquet data source merges schemas collected from all data files, otherwise the schema is picked from the summary file or a random data file if no summary file is available.

	当为 true 时，Parquet 数据源合并来自所有数据文件的 schemas, 否则将从概要文件中选择，如果没有可用的概要文件，那么就从随机数据文件中选择。

- spark.sql.parquet.outputTimestampType	

	Default: INT96	

	Since Version: 2.3.0

	> Sets which Parquet timestamp type to use when Spark writes data to Parquet files. INT96 is a non-standard but commonly used timestamp type in Parquet. TIMESTAMP_MICROS is a standard timestamp type in Parquet, which stores number of microseconds from the Unix epoch. TIMESTAMP_MILLIS is also standard, but with millisecond precision, which means Spark has to truncate the microsecond portion of its timestamp value.

	当 spark 将数据写入 Parquet 文件时，设置使用哪种 Parquet 时间戳类型。

	INT96 是非标准的，但在 Parquet 中普遍使用。

	TIMESTAMP_MICROS 在 Parquet 中是标准的时间戳类型，存储着从 Unix 纪元以来的微秒数。 

	TIMESTAMP_MILLIS 也是标准的，但是毫秒精度的，这就意味着 spark 必须截断时间戳值的微秒部分。

- spark.sql.parquet.recordLevelFilter.enabled	

	Default: false	

	Since Version: 2.3.0

	> If true, enables Parquet's native record-level filtering using the pushed down filters. This configuration only has an effect when 'spark.sql.parquet.filterPushdown' is enabled and the vectorized reader is not used. You can ensure the vectorized reader is not used by setting 'spark.sql.parquet.enableVectorizedReader' to false.

	如果为 true, 使用下推的过滤器，启用 Parquet 原生的记录级别的过滤。这个配置只有在 `spark.sql.parquet.filterPushdown` 启用，且不使用向量化阅读器时才有效。你可以将 `spark.sql.parquet.enableVectorizedReader` 设置为 false, 确保不使用向量化阅读器。

- spark.sql.parquet.respectSummaryFiles	

	Default: false

	Since Version: 1.5.0

	> When true, we make assumption that all part-files of Parquet are consistent with summary files and we will ignore them when merging schema. Otherwise, if this is false, which is the default, we will merge all part-files. This should be considered as expert-only option, and shouldn't be enabled before knowing what it means exactly.

	当为 true 时，假设 Parquet 的所有部分文件都和概要文件一致，并且在合并 schema 时忽略它们。如果这为 false, 也就是默认情况，我们将合并所有的部分文件。

	这应该作为仅专家使用的选项，不应该在不知道它的含义前启用。

- spark.sql.parquet.writeLegacyFormat	

	Default: false	

	Since Version: 1.6.0

	> If true, data will be written in a way of Spark 1.4 and earlier. For example, decimal values will be written in Apache Parquet's fixed-length byte array format, which other systems such as Apache Hive and Apache Impala use. If false, the newer format in Parquet will be used. For example, decimals will be written in int-based format. If Parquet output is intended for use with systems that do not support this newer format, set to true.

	如果为 true, 数据将以 spark 1.4 和更早版本的方式写入。例如，将以 Apache Parquet 固定长度字节数组的格式写入 decimal 值，也就是像 Apache Hive 和 Apache Impala 这种系统使用的格式。

	如果为 false, 将使用更新的格式。例如，将以基于 int 格式写入 decimals. 如果 Parquet 输出用于和不支持这种格式的系统一起使用，那么就设为 true.

- spark.sql.parser.quotedRegexColumnNames	

	Default: false	

	Since Version: 2.3.0

	> When true, quoted Identifiers (using backticks) in SELECT statement are interpreted as regular expressions.

	当为 true 时，在 SELECT 语句中使用反引号引起来的标识符被解释成正则表达式。

- spark.sql.pivotMaxValues	

	Default: 10000	

	Since Version: 1.6.0

	> When doing a pivot without specifying values for the pivot column this is the maximum number of (distinct) values that will be collected without error.

	当做一个透视操作，而不为透视列指定值时，这是将被收集、而不报错的值的最大数量

- spark.sql.pyspark.inferNestedDictAsStruct.enabled	

	Default: false	

	Since Version: 3.3.0

	> PySpark's `SparkSession.createDataFrame` infers the nested dict as a map by default. When it set to true, it infers the nested dict as a struct.

	默认，PySpark 的 `SparkSession.createDataFrame` 将嵌套字典推断成一个 map. 当设为 true 时，将嵌套字典推断成一个 struct.

- spark.sql.pyspark.jvmStacktrace.enabled	

	Default: false	

	Since Version: 3.0.0

	> When true, it shows the JVM stacktrace in the user-facing PySpark exception together with Python stacktrace. By default, it is disabled and hides JVM stacktrace and shows a Python-friendly exception only.

	当为 true 时，在用户面对 PySpark 异常时，它展示了 JVM 堆栈信息和 Python 堆栈信息。默认情况下，它是禁用的，隐藏 JVM 堆栈信息，并仅展示 Python 友好的异常。

- spark.sql.redaction.options.regex	

	Default: (?i)url	

	Since Version: 2.2.2

	> Regex to decide which keys in a Spark SQL command's options map contain sensitive information. The values of options whose names that match this regex will be redacted in the explain output. This redaction is applied on top of the global redaction configuration defined by `spark.redaction.regex`.

	正则，决定了在 Spark SQL 命令的选项 map 中哪些 keys 包含敏感信息。名称与此正则表达式匹配的选项的值将在 explain 输出中被校正。这个校正被应用在由 `spark.redaction.regex` 定义的全局校正配置之上。

- spark.sql.redaction.string.regex	

	Default: (value of spark.redaction.string.regex)	

	Since Version: 2.3.0

	> Regex to decide which parts of strings produced by Spark contain sensitive information. When this regex matches a string part, that string part is replaced by a dummy value. This is currently used to redact the output of SQL explain commands. When this conf is not set, the value from `spark.redaction.string.regex` is used.

	正则，决定了在 Spark 产生的字符串的哪些部分包含敏感信息。当这个正则匹配一个字符串部分时，那个字符串部分被假值取代。

	当前这个配置用于校正 SQL explain 命令的输出。当不设置这个配置时，就使用 `spark.redaction.string.regex` 这个值。

- spark.sql.repl.eagerEval.enabled	

	Default: false	

	Since Version: 2.4.0

	> Enables eager evaluation or not. When true, the top K rows of Dataset will be displayed if and only if the REPL supports the eager evaluation. Currently, the eager evaluation is supported in PySpark and SparkR. In PySpark, for the notebooks like Jupyter, the HTML table (generated by repr_html) will be returned. For plain Python REPL, the returned outputs are formatted like `dataframe.show()`. In SparkR, the returned outputs are showed similar to R `data.frame` would.

	是否启用立即计算。当为 true 时，仅在 REPL 立即计算的情况下，展示 Dataset 的前 K 行。

	当前，在 PySpark 和 SparkR 中支持立即计算。在 PySpark 中，对于像 Jupyter 的笔记本，将返回 HTML 表（由 repr_html 生成）。对于纯 Python REPL, 就像 `dataframe.show()` 一样格式化返回的输出。在 SparkR 中，展示返回的输出就像 R `data.frame` 一样。

- spark.sql.repl.eagerEval.maxNumRows	

	Default: 20	

	Since Version: 2.4.0

	> The max number of rows that are returned by eager evaluation. This only takes effect when `spark.sql.repl.eagerEval.enabled` is set to true. The valid range of this config is from 0 to `(Int.MaxValue - 1)`, so the invalid config like negative and greater than `(Int.MaxValue - 1)` will be normalized to 0 and `(Int.MaxValue - 1)`.

	由立即计算返回的行的最大数量。这仅在 `spark.sql.repl.eagerEval.enabled` 设为 true 时有效。这个配置的有效范围在 0 和 `(Int.MaxValue - 1)` 之间，所以像负数和大于 `(Int.MaxValue - 1)` 的无效配置将被规范化成 0 和 `(Int.MaxValue - 1)`

- spark.sql.repl.eagerEval.truncate	

	Default: 20	

	Since Version: 2.4.0

	> The max number of characters for each cell that is returned by eager evaluation. This only takes effect when `spark.sql.repl.eagerEval.enabled` is set to true.

	立即计算返回的每个单元格的字符的最大数量。这仅在 `spark.sql.repl.eagerEval.enabled` 设为 true 时有效。

- spark.sql.session.timeZone	

	Default: (value of local timezone)

	Since Version: 2.2.0

	> The ID of session local timezone in the format of either region-based zone IDs or zone offsets. Region IDs must have the form 'area/city', such as 'America/Los_Angeles'. Zone offsets must be in the format '(+|-)HH', '(+|-)HH:mm' or '(+|-)HH:mm:ss', e.g '-08', '+01:00' or '-13:33:33'. Also 'UTC' and 'Z' are supported as aliases of '+00:00'. Other short names are not recommended to use because they can be ambiguous.

	会话本地时区的 ID, 格式是基于区域的 zone IDs 或 zone offsets.

	区域 ID 必须是 'area/city' 形式，例如 'America/Los_Angeles'. 区域偏移必须是 '(+|-)HH', '(+|-)HH:mm' 或 '(+|-)HH:mm:ss' 格式，例如 '-08', '+01:00' or '-13:33:33'.

	'UTC' 和 'Z' 作为 '+00:00' 的别名支持使用。其他段名称并不推荐使用，因为它们是模糊的。

- spark.sql.shuffle.partitions	

	Default: 200	

	Since Version: 1.1.0

	> The default number of partitions to use when shuffling data for joins or aggregations. Note: For structured streaming, this configuration cannot be changed between query restarts from the same checkpoint location.

	当在 join 或聚合操作 shuffle 数据时，使用的默认分区数。注意，对于结构化流，从相同的 checkpoint 位置处，重启查询之间，这个配置不能改变。

- spark.sql.shuffledHashJoinFactor	

	Default: 3	

	Since Version: 3.3.0

	> The shuffle hash join can be selected if the data size of small side multiplied by this factor is still smaller than the large side.

	如果数据量小的一端的数据大小乘以这个因子后，仍比数据量大的一端还小，那么就可以选择 shuffle hash join

- spark.sql.sources.bucketing.autoBucketedScan.enabled	

	Default: true	

	Since Version: 3.1.0

	> When true, decide whether to do bucketed scan on input tables based on query plan automatically. Do not use bucketed scan if 1. query does not have operators to utilize bucketing (e.g. join, group-by, etc), or 2. there's an exchange operator between these operators and table scan. Note when 'spark.sql.sources.bucketing.enabled' is set to false, this configuration does not take any effect.

	当为 true 时，基于查询计划，自动地决定是否在输入表上进行分桶扫描。

	以下情况不要使用分桶扫描: 1. 查询操作没有利用分桶的算子（例如join, group-by） 2. 在这些算子和表扫描间存在交换算子。

	注意，当 `spark.sql.sources.bucketing.enabled` 为 false 时，这个配置不起作用。

- spark.sql.sources.bucketing.enabled	

	Default: true	

	Since Version: 2.0.0

	> When false, we will treat bucketed table as normal table

	当为 false 时，会将分桶表视作正常表。

- spark.sql.sources.bucketing.maxBuckets	

	Default: 100000	

	Since Version: 2.4.0

	> The maximum number of buckets allowed.

	允许的桶的最大数量

- spark.sql.sources.default	

	Default: parquet	

	Since Version: 1.3.0

	> The default data source to use in input/output.

	在输入/输出中使用的默认数据源。

- spark.sql.sources.parallelPartitionDiscovery.threshold	

	Default: 32	

	Since Version: 1.5.0

	> The maximum number of paths allowed for listing files at driver side. If the number of detected paths exceeds this value during partition discovery, it tries to list the files with another Spark distributed job. This configuration is effective only when using file-based sources such as Parquet, JSON and ORC.

	在驱动端，对于列出的文件所允许的路径最大数量。如果在分区发现期间，检测到的路径数量超过这个值，就尝试和其他的 spark 分布式作业一起列出文件。这个配置仅在使用基于文件的源时有效。

- spark.sql.sources.partitionColumnTypeInference.enabled	

	Default: true	

	Since Version: 1.5.0

	> When true, automatically infer the data types for partitioned columns.

	当为 true 时，自动推断分区列的数据类型。

- spark.sql.sources.partitionOverwriteMode	

	Default: STATIC	

	Since Version: 2.3.0

	> When INSERT OVERWRITE a partitioned data source table, we currently support 2 modes: static and dynamic. In static mode, Spark deletes all the partitions that match the partition specification(e.g. PARTITION(a=1,b)) in the INSERT statement, before overwriting. In dynamic mode, Spark doesn't delete partitions ahead, and only overwrite those partitions that have data written into it at runtime. By default we use static mode to keep the same behavior of Spark prior to 2.3. Note that this config doesn't affect Hive serde tables, as they are always overwritten with dynamic mode. This can also be set as an output option for a data source using key partitionOverwriteMode (which takes precedence over this setting), e.g. `dataframe.write.option("partitionOverwriteMode", "dynamic").save(path)`.

	当 INSERT OVERWRITE 入一个分区数据源表时，当前支持两种模式: 静态和动态。

	在静态模式下，spark 会在覆盖前，在 INSERT 语句中删除所有匹配到分区规范的所有分区。在动态模式中，spark 并不会提前删除分区，仅覆盖在运行时数据写入的分区。

	默认情况下，我们使用静态模式保持和 spark 2.3 版本之前的相同行为。

	注意，这个配置不会影响 hive serde 表，因为它们总会使用动态模式覆盖。

	这配置也会为数据源设置成一个输出选项，使用 partitionOverwriteMode 作为 key （优先级高于这个设置），例如，`dataframe.write.option("partitionOverwriteMode", "dynamic").save(path)`

- spark.sql.sources.v2.bucketing.enabled	

	Default: false	

	Since Version: 3.3.0

	> Similar to `spark.sql.sources.bucketing.enabled`, this config is used to enable bucketing for V2 data sources. When turned on, Spark will recognize the specific distribution reported by a V2 data source through SupportsReportPartitioning, and will try to avoid shuffle if necessary.

	和 `spark.sql.sources.bucketing.enabled` 类似，这个配置用于为 V2 数据源启用分桶。当打开时，spark 将识别由 V2 数据源通过 SupportsReportPartitioning 报告的特定分布，并如果必要，将尝试避免 shuffle.

- spark.sql.statistics.fallBackToHdfs	

	Default: false	

	Since Version: 2.0.0

	> When true, it will fall back to HDFS if the table statistics are not available from table metadata. This is useful in determining if a table is small enough to use broadcast joins. This flag is effective only for non-partitioned Hive tables. For non-partitioned data source tables, it will be automatically recalculated if table statistics are not available. For partitioned data source and partitioned Hive tables, It is 'spark.sql.defaultSizeInBytes' if table statistics are not available.

	当为 true 时，如果表的统计信息不能从表元数据获取，那么将回退到 HDFS. 这在判断一个表是否小到可以使用广播 join 时是有用的。

	这个标准仅在非分区 hive 表有效。

	对于非分区数据源表，如果表的统计信息不可用，那么将自动重新计算。

	对于分区数据源和分区 hive 表，如果表的统计信息不可用，它就是 `spark.sql.defaultSizeInBytes`

- spark.sql.statistics.histogram.enabled	

	Default: false

	Since Version: 2.3.0

	> Generates histograms when computing column statistics if enabled. Histograms can provide better estimation accuracy. Currently, Spark only supports equi-height histogram. Note that collecting histograms takes extra cost. For example, collecting column statistics usually takes only one table scan, but generating equi-height histogram will cause an extra table scan.

	如果启用，在计算列统计信息时，生成柱状图。

	柱状图可以提供更好的计算精度。当前，spark 仅支持等高柱状图。注意，收集中的柱状图会有额外的成本。例如，收集列的统计信息通常只会扫描一张表，但是生成的等高柱状图将造成额外的表扫描。

- spark.sql.statistics.size.autoUpdate.enabled	

	Default: false	

	Since Version: 2.3.0

	> Enables automatic update for table size once table's data is changed. Note that if the total number of files of the table is very large, this can be expensive and slow down data change commands.

	一旦修改了表数据，启用对表大小的自动更新。注意，如果表文件的总数量非常大，这会是昂贵的操作，降低数据修改命令的速度。

- spark.sql.storeAssignmentPolicy	

	Default: ANSI	

	Since Version: 3.0.0

	> When inserting a value into a column with different data type, Spark will perform type coercion. Currently, we support 3 policies for the type coercion rules: ANSI, legacy and strict. With ANSI policy, Spark performs the type coercion as per ANSI SQL. In practice, the behavior is mostly the same as PostgreSQL. It disallows certain unreasonable type conversions such as converting string to int or double to boolean. With legacy policy, Spark allows the type coercion as long as it is a valid `Cast`, which is very loose. e.g. converting string to int or double to boolean is allowed. It is also the only behavior in Spark 2.x and it is compatible with Hive. With strict policy, Spark doesn't allow any possible precision loss or data truncation in type coercion, e.g. converting double to int or decimal to double is not allowed.

	当向一列插入一个不同数据类型的值时，spark 将执行类型强制转换。

	当前支持三种类型强制转换策略: ANSI, legacy 和 strict.

	对于 ANSI 策略，spark 为每个 ANSI SQL 执行类型强制转换。在实际操作中，大部分行为和 PostgreSQL 相同。它不允许特定的不合理的类型转换，例如将字符串转成 int, 或将 double 转成 boolean.

	对于 legacy 策略，只有是有效的 `Cast`, spark 就允许类型强制转换，例如，将字符串转成 int, 或将 double 转成 boolean 是允许的。这个仅是 Spark 2.x 行为，和 hive 兼容。

	对于 strict 策略，spark 不允许在类型强制转换中任意可能的精度丢失或数据截断，例如将 double 转成 int, 或将 decimal 转成 double 是不允许的。

- spark.sql.streaming.checkpointLocation	

	Default: (none)	

	Since Version: 2.0.0

	> The default location for storing checkpoint data for streaming queries.

	对于流查询，存储 checkpoint 数据的默认路径。

- spark.sql.streaming.continuous.epochBacklogQueueSize	

	Default: 10000	

	Since Version: 3.0.0

	> The max number of entries to be stored in queue to wait for late epochs. If this parameter is exceeded by the size of the queue, stream will stop with an error.

- spark.sql.streaming.disabledV2Writers

	Since Version: 2.3.1

	> A comma-separated list of fully qualified data source register class names for which StreamWriteSupport is disabled. Writes to these sources will fall back to the V1 Sinks.

	逗号分隔的全限定数据源注册类名的列表，StreamWriteSupport 是禁用的。写入这些源将回退到 V1 Sinks.

- spark.sql.streaming.fileSource.cleaner.numThreads	

	Default: 1	

	Since Version: 3.0.0

	> Number of threads used in the file source completed file cleaner.

	在完成文件清理的文件源中使用的线程数

- spark.sql.streaming.forceDeleteTempCheckpointLocation	

	Default: false

	Since Version: 3.0.0

	> When true, enable temporary checkpoint locations force delete.

	当为 true 时，启用临时 checkpoint 路径强制删除

- spark.sql.streaming.metricsEnabled	

	Default: false	

	Since Version: 2.0.2

	> Whether Dropwizard/Codahale metrics will be reported for active streaming queries.

	对于活跃的流查询，是否报告 Dropwizard/Codahale 度量

- spark.sql.streaming.multipleWatermarkPolicy	

	Default: min	

	Since Version: 2.4.0

	> Policy to calculate the global watermark value when there are multiple watermark operators in a streaming query. The default value is 'min' which chooses the minimum watermark reported across multiple operators. Other alternative value is 'max' which chooses the maximum across multiple operators. Note: This configuration cannot be changed between query restarts from the same checkpoint location.

	当在一个流查询中存在多种水印算子时，计算全局水印值的策略。默认值是 min, 也就是选择多个算子报告的最小水印。其他的可供替换的值是 max, 也即选择最大的。

	注意，在相同的 checkpoint 路径下，这个配置不能在查询重启之间改变。

- spark.sql.streaming.noDataMicroBatches.enabled	

	Default: true	

	Since Version: 2.4.1

	> Whether streaming micro-batch engine will execute batches without data for eager state management for stateful streaming queries.

	对于状态流查询，流微批引擎是否执行用于立即状态管理的没有数据的批次。

- spark.sql.streaming.numRecentProgressUpdates	

	Default: 100	

	Since Version: 2.1.1

	> The number of progress updates to retain for a streaming query

	对于一个流查询，保留的进度更新次数

- spark.sql.streaming.sessionWindow.merge.sessions.in.local.partition	

	Default: false	

	Since Version: 3.2.0

	> When true, streaming session window sorts and merge sessions in local partition prior to shuffle. This is to reduce the rows to shuffle, but only beneficial when there're lots of rows in a batch being assigned to same sessions.

	当为 true 时，在 shuffle 前，流会话窗口排序并合并本地分区中的会话。这是为了减少 shuffle 的行，但是仅在批次中有多行被分配到相同的会话中时有用。

- spark.sql.streaming.stateStore.stateSchemaCheck

	Default: true

	Since Version: 3.1.0

	> When true, Spark will validate the state schema against schema on existing state and fail query if it's incompatible.

	当为 true 时，spark 将验证已存在的状态上的 schema 和现在的状态 schema, 如果不兼容，那么查询就会失败。

- spark.sql.streaming.stopActiveRunOnRestart	

	Default: true	

	Since Version: 3.0.0

	> Running multiple runs of the same streaming query concurrently is not supported. If we find a concurrent active run for a streaming query (in the same or different SparkSessions on the same cluster) and this flag is true, we will stop the old streaming query run to start the new one.

	不支持并行多个运行相同的流查询。如果找到一个流查询的并发活跃运行（在同一集群上的相同的或不同的SparkSessions中），且这个标志为 true, 我们将停止旧的流查询运行，以开启新的运行。

- spark.sql.streaming.stopTimeout

	Default: 0	

	Since Version: 3.0.0

	> How long to wait in milliseconds for the streaming execution thread to stop when calling the streaming query's `stop()` method. 0 or negative values wait indefinitely.

	当调用流查询的 `stop()` 方法时，等待流执行线程停止的时间。0或负数表示等待无限时间。

- spark.sql.thriftServer.interruptOnCancel	

	Default: false

	Since Version: 3.2.0

	> When true, all running tasks will be interrupted if one cancels a query. When false, all running tasks will remain until finished.

	当为 true 时，如果取消了一个查询，那么所有运行的任务将被中断。当为 false 时，所有运行的任务仍将继续运行，直到完成。

- spark.sql.thriftServer.queryTimeout	

	Default: 0ms	

	Since Version: 3.1.0

	> Set a query duration timeout in seconds in Thrift Server. If the timeout is set to a positive value, a running query will be cancelled automatically when the timeout is exceeded, otherwise the query continues to run till completion. If timeout values are set for each statement via `java.sql.Statement.setQueryTimeout` and they are smaller than this configuration value, they take precedence. If you set this timeout and prefer to cancel the queries right away without waiting task to finish, consider enabling `spark.sql.thriftServer.interruptOnCancel` together.

	设置一个在 Thrift Server 中的查询持续时长。如果设为一个负数，当超过了这个时长，一个正在运行的查询将自动取消，否则查询继续运行，直到完成。

	如果通过 `java.sql.Statement.setQueryTimeout` 为每个语句设置一个时长值，且它们小于这个配置值，那么它们优先级更高。

	如果你设置了这个时长，且更倾向于不等待任务完成，立即取消查询，那么考虑一起启用 `spark.sql.thriftServer.interruptOnCancel`

- spark.sql.thriftserver.scheduler.pool	

	Default: (none)	

	Since Version: 1.1.1

	> Set a Fair Scheduler pool for a JDBC client session.

	为 JDBC 客户端会话设置一个公平调度器池。

- spark.sql.thriftserver.ui.retainedSessions	

	Default: 200	

	Since Version: 1.4.0

	> The number of SQL client sessions kept in the JDBC/ODBC web UI history.

	保存在 JDBC/ODBC web UI 历史中的 SQL 客户端会话的数量

- spark.sql.thriftserver.ui.retainedStatements	

	Default: 200	

	Since Version: 1.4.0

	> The number of SQL statements kept in the JDBC/ODBC web UI history.

	保存在 JDBC/ODBC web UI 历史中的 SQL 语句的数量

- spark.sql.ui.explainMode	

	Default: formatted	

	Since Version: 3.1.0

	> Configures the query explain mode used in the Spark SQL UI. The value can be 'simple', 'extended', 'codegen', 'cost', or 'formatted'. The default value is 'formatted'.

	配置用在 Spark SQL UI 的查询 explain 模式。此值可以是 'simple', 'extended', 'codegen', 'cost', or 'formatted'

- spark.sql.variable.substitute	

	Default: true

	Since Version: 2.0.0

	> This enables substitution using syntax like `${var}`, `${system:var}`, and `${env:var}`.

	使用 `${var}`, `${system:var}` 和 `${env:var}` 语法，启用替换。

###### Static SQL Configuration

> Static SQL configurations are cross-session, immutable Spark SQL configurations. They can be set with final values by the config file and command-line options with `--conf/-c` prefixed, or by setting `SparkConf` that are used to create `SparkSession`. External users can query the static sql config values via `SparkSession.conf` or via `set` command, e.g. `SET spark.sql.extensions;`, but cannot set/unset them.

静态的 SQL 配置是跨会话的，不可变的 Spark SQL 配置。它们可以通过配置文件和使用 `--conf/-c` 前缀的命令行选项设置一个最终值，或者通过设置用于创建 `SparkSession` 的 `SparkConf`. 外部用户可以通过 `SparkSession.conf` 或 `set` 命令查询静态 sql 配置，例如 `SET spark.sql.extensions;`, 但是不能设置或取消设置它们。

- spark.sql.cache.serializer	

	Default: org.apache.spark.sql.execution.columnar.DefaultCachedBatchSerializer

	Since Version: 3.1.0

	> The name of a class that implements `org.apache.spark.sql.columnar.CachedBatchSerializer`. It will be used to translate SQL data into a format that can more efficiently be cached. The underlying API is subject to change so use with caution. Multiple classes cannot be specified. The class must have a no-arg constructor.

	实现了 `org.apache.spark.sql.columnar.CachedBatchSerializer` 的类名。它将用于将 SQL 数据翻译成能更高效缓存的格式。底层 API 可能会变化，所以小心使用。不能指定多个类。类必须有无参构造。

- spark.sql.event.truncate.length	

	Default: 2147483647	

	Since Version: 3.0.0

	> Threshold of SQL length beyond which it will be truncated before adding to event. Defaults to no truncation. If set to 0, callsite will be logged instead.

	SQL 长度的阈值，超过这个阈值的话，就会在添加到事件前，被截断。默认是不截断的。

- spark.sql.extensions	

	Default: (none)	

	Since Version: 2.2.0

	> A comma-separated list of classes that implement `Function1[SparkSessionExtensions, Unit]` used to configure Spark Session extensions. The classes must have a no-args constructor. If multiple extensions are specified, they are applied in the specified order. For the case of rules and planner strategies, they are applied in the specified order. For the case of parsers, the last parser is used and each parser can delegate to its predecessor. For the case of function name conflicts, the last registered function name is used.

	实现 `Function1[SparkSessionExtensions, Unit]` 的类的列表，用逗号分隔，用于配置 spark 会话扩展。 类必须有无参构造。如果指定了多个扩展，将按指定的顺序使用。

	对于规则和计划策略的情况，它们按照指定的顺序应用。对于解析器，使用最后一个解析器，每个解析器都可以委托给它的前任。对于函数名冲突的情况，使用最后一个注册的函数名。

- spark.sql.hive.metastore.barrierPrefixes

	Since Version: 1.4.0

	> A comma separated list of class prefixes that should explicitly be reloaded for each version of Hive that Spark SQL is communicating with. For example, Hive UDFs that are declared in a prefix that typically would be shared (i.e. `org.apache.spark.*`).

	类前缀的列表，逗号分隔，类前缀应该为 Spark SQL 正在通信的每个 hive 版本明确地再次载入。

	例如，在前缀中声明的 Hive UDFs 应该被共享。

- spark.sql.hive.metastore.jars	

	Default: builtin	

	Since Version: 1.4.0

	> Location of the jars that should be used to instantiate the HiveMetastoreClient. This property can be one of four options: 1. "builtin" Use Hive 2.3.9, which is bundled with the Spark assembly when `-Phive` is enabled. When this option is chosen, `spark.sql.hive.metastore.version` must be either 2.3.9 or not defined. 2. "maven" Use Hive jars of specified version downloaded from Maven repositories. 3. "path" Use Hive jars configured by `spark.sql.hive.metastore.jars.path` in comma separated format. Support both local or remote paths.The provided jars should be the same version as `spark.sql.hive.metastore.version`. 4. A classpath in the standard format for both Hive and Hadoop. The provided jars should be the same version as `spark.sql.hive.metastore.version`.

	用于初始化 HiveMetastoreClient 的 jars 的位置。

	这个属性可以是下面四个选项中的一个: 

	1."builtin": 在启用 `-Phive` 时，使用和 spark 绑定 hive 2.3.9 版本。当选择这个选项时，要么将 `spark.sql.hive.metastore.version` 设置成 2.3.9，要么不定义。

	2."maven": 使用能从 Maven 仓库下载指定版本的 hive jars

	3."path": 使用 `spark.sql.hive.metastore.jars.path` 配置的 hive jars, 同时支持本地和远程路径。提供的 jars 应该和 `spark.sql.hive.metastore.version` 相同版本。

	4.满足 hive 和 hadoop 的标准格式的类路径。提供的 jars 应该和 `spark.sql.hive.metastore.version` 相同版本。

- spark.sql.hive.metastore.jars.path

	Since Version: 3.1.0

	> Comma-separated paths of the jars that used to instantiate the HiveMetastoreClient. This configuration is useful only when `spark.sql.hive.metastore.jars` is set as `path`. The paths can be any of the following format: 1. `file://path/to/jar/foo.jar` 2. `hdfs://nameservice/path/to/jar/foo.jar` 3. `/path/to/jar/` (path without URI scheme follow conf `fs.defaultFS`'s URI schema) 4. `[http/https/ftp]://path/to/jar/foo.jar` Note that 1, 2, and 3 support wildcard. For example: 1. `file://path/to/jar/,file://path2/to/jar//.jar` 2. `hdfs://nameservice/path/to/jar/,hdfs://nameservice2/path/to/jar//.jar`

	用于初始化 HiveMetastoreClient 的 jars 的路径，逗号分隔。这个配置仅在 `spark.sql.hive.metastore.jars` 设置为 `path` 时有用。

- spark.sql.hive.metastore.sharedPrefixes	

	Default: `com.mysql.jdbc,org.postgresql`,`com.microsoft.sqlserver`,`oracle.jdbc`

	Since Version: 1.4.0

	> A comma separated list of class prefixes that should be loaded using the classloader that is shared between Spark SQL and a specific version of Hive. An example of classes that should be shared is JDBC drivers that are needed to talk to the metastore. Other classes that need to be shared are those that interact with classes that are already shared. For example, custom appenders that are used by log4j.

	应该使用类加载器载入的类前缀的列表，类加载器在 spark sql 和一个特定的 hive 版本间共享

	应被共享的类的示例就是 JDBC 驱动，它需要和元数据库通信。另外需要被共享的类是和已被共享的类交互的类，例如，log4j 使用的自定义的 appenders

- spark.sql.hive.metastore.version	

	Default: 2.3.9	

	Since Version: 1.4.0

	> Version of the Hive metastore. Available options are 0.12.0 through 2.3.9 and 3.0.0 through 3.1.2.

	hive 元数据库版本。可用的选项是 0.12.0 到2.3.9 和 3.0.0 到3.1.2。

- spark.sql.hive.thriftServer.singleSession	

	Default: false

	Since Version: 1.6.0

	> When set to true, Hive Thrift server is running in a single session mode. All the JDBC/ODBC connections share the temporary views, function registries, SQL configuration and the current database.

	当设为 true 时，Hive Thrift server 运行在单会话模式下。所有的 JDBC/ODBC 链接共享临时视图、函数注册、sql配置和当前的数据库。

- spark.sql.hive.version	

	Default: 2.3.9	

	Since Version: 1.1.1

	> The compiled, a.k.a, builtin Hive version of the Spark distribution bundled with. Note that, this a read-only conf and only used to report the built-in hive version. If you want a different metastore client for Spark to call, please refer to `spark.sql.hive.metastore.version`.

	spark 发行版内部绑定的 hive 版本。

	这是一个仅读配置，仅用于报告内建 hive 版本。如果你想调用不同的元数据库客户端，参考 `spark.sql.hive.metastore.version`.

- spark.sql.metadataCacheTTLSeconds	

	Default: -1000ms	

	Since Version: 3.1.0

	> Time-to-live (TTL) value for the metadata caches: partition file metadata cache and session catalog cache. This configuration only has an effect when this value having a positive value (> 0). It also requires setting 'spark.sql.catalogImplementation' to `hive`, setting 'spark.sql.hive.filesourcePartitionFileCacheSize' `> 0` and setting 'spark.sql.hive.manageFilesourcePartitions' to `true` to be applied to the partition file metadata cache.

	元数据缓存的 TTL 值：分区文件元数据缓存和会话 catalog 缓存。这个配置仅在是正值时有效。也要求 `spark.sql.catalogImplementation=hive` hive, `spark.sql.hive.filesourcePartitionFileCacheSize>0`, `spark.sql.hive.manageFilesourcePartitions=true`

- spark.sql.queryExecutionListeners	

	Default: (none)	

	Since Version: 2.3.0

	> List of class names implementing `QueryExecutionListener` that will be automatically added to newly created sessions. The classes should have either a no-arg constructor, or a constructor that expects a `SparkConf` argument.

	实现了 `QueryExecutionListener` 的类的名称的列表，`QueryExecutionListener` 将自动添加到新创建的会话中。类应该有一个无参构造，或者有一个含 `SparkConf` 参数的有参构造。

- spark.sql.sources.disabledJdbcConnProviderList

	Since Version: 3.1.0

	> Configures a list of JDBC connection providers, which are disabled. The list contains the name of the JDBC connection providers separated by comma.

	配置一个 JDBC 链接提供者的列表，且都是禁用的。列表包含 JDBC 链接提供者的名称，用逗号分隔。

- spark.sql.streaming.streamingQueryListeners	

	Default: (none)	

	Since Version: 2.4.0

	> List of class names implementing `StreamingQueryListener` that will be automatically added to newly created sessions. The classes should have either a no-arg constructor, or a constructor that expects a `SparkConf` argument.

	实现了 `StreamingQueryListener` 的类的名称的列表，`StreamingQueryListener` 将自动添加到新创建的会话中。类应该有一个无参构造，或者有一个含 `SparkConf` 参数的有参构造。

- spark.sql.streaming.ui.enabled	

	Default: true

	Since Version: 3.0.0

	> Whether to run the Structured Streaming Web UI for the Spark application when the Spark Web UI is enabled.

	当启用 Spark Web UI 时，是否为 spark 应用程序运行 Structured Streaming Web UI

- spark.sql.streaming.ui.retainedProgressUpdates	

	Default: 100	

	Since Version: 3.0.0

	> The number of progress updates to retain for a streaming query for Structured Streaming UI.

	对于 Structured Streaming UI, 为一个流查询保留的进度更新的数量。

- spark.sql.streaming.ui.retainedQueries	

	Default: 100	

	Since Version: 3.0.0

	> The number of inactive queries to retain for Structured Streaming UI.

	对于 Structured Streaming UI, 保留的不活跃的查询的数量

- spark.sql.ui.retainedExecutions	

	Default: 1000	

	Since Version: 1.5.0

	> Number of executions to retain in the Spark UI.

	在 spark ui 中保留的执行的数量

- spark.sql.warehouse.dir	

	Default: (value of $PWD/spark-warehouse)

	Since Version: 2.0.0

	> The default location for managed databases and tables.

	管理数据库和表的默认路径

##### Spark Streaming

- spark.streaming.backpressure.enabled	

	Default: false	

	Since Version: 1.5.0

	> Enables or disables Spark Streaming's internal backpressure mechanism (since 1.5). This enables the Spark Streaming to control the receiving rate based on the current batch scheduling delays and processing times so that the system receives only as fast as the system can process. Internally, this dynamically sets the maximum receiving rate of receivers. This rate is upper bounded by the values `spark.streaming.receiver.maxRate` and `spark.streaming.kafka.maxRatePerPartition` if they are set (see below).	

	启用或禁用 Spark Streaming 的内部背压机制（从1.5版本开始）。这会让 Spark Streaming 基于当前的批次调度延迟和处理事件，控制接收速率，以保证系统接收和系统处理保持相同速度。

	在内部，这会动态地设置接收器的最大接收速率。这个速率的上限由 `spark.streaming.receiver.maxRate` 和 `spark.streaming.kafka.maxRatePerPartition` 绑定。

- spark.streaming.backpressure.initialRate	

	Default: not set	

	Since Version: 2.0.0

	> This is the initial maximum receiving rate at which each receiver will receive data for the first batch when the backpressure mechanism is enabled.	

	初始的最大接收速率，当启用被压机制时，每个接收器就以此速率接收第一批数据。

- spark.streaming.blockInterval	

	Default: 200ms	

	Since Version: 0.8.0

	> Interval at which data received by Spark Streaming receivers is chunked into blocks of data before storing them in Spark. Minimum recommended - 50 ms. See the [performance tuning](https://spark.apache.org/docs/3.3.2/streaming-programming-guide.html#level-of-parallelism-in-data-receiving) section in the Spark Streaming programming guide for more details.	

	在这个间隔期间，Spark Streaming 接收器接收的数据在存储前，会将其划分到数据块中。最小推荐50ms

- spark.streaming.receiver.maxRate	

	Default: not set	

	Since Version: 1.0.2

	> Maximum rate (number of records per second) at which each receiver will receive data. Effectively, each stream will consume at most this number of records per second. Setting this configuration to 0 or a negative number will put no limit on the rate. See the [deployment guide](https://spark.apache.org/docs/3.3.2/streaming-programming-guide.html#deploying-applications) in the Spark Streaming programming guide for mode details.	

	每个接收器接收数据的最大速率（每秒接收的记录数）。实际上，每条流每秒最多消耗这个数量的记录。将这个配置设为0或负数，表示对速率没有限制。

- spark.streaming.receiver.writeAheadLog.enable	

	Default: false	

	Since Version: 1.2.1

	> Enable write-ahead logs for receivers. All the input data received through receivers will be saved to write-ahead logs that will allow it to be recovered after driver failures. See the [deployment guide](https://spark.apache.org/docs/3.3.2/streaming-programming-guide.html#deploying-applications) in the Spark Streaming programming guide for more details.	

	为接收器启用WAL. 通过接收器接收的所有输入数据将被保存到提前预写日志中，这能在驱动失败后，恢复数据。

- spark.streaming.unpersist	

	Default: true	

	Since Version: 0.9.0

	> Force RDDs generated and persisted by Spark Streaming to be automatically unpersisted from Spark's memory. The raw input data received by Spark Streaming is also automatically cleared. Setting this to false will allow the raw data and persisted RDDs to be accessible outside the streaming application as they will not be cleared automatically. But it comes at the cost of higher memory usage in Spark.	

	强制从 Spark Streaming 生成和持久化的 RDDs 从内存中取消持久化。Spark Streaming 接收到的原生输入数据也被自动清除。

	将此设为 false 将允许原生数据和持久化的 RDDs 能在流应用程序外访问，因为它们将不被自动清除。但是它会花费更高的内存作为代价。

- spark.streaming.stopGracefullyOnShutdown	

	Default: false	

	Since Version: 1.4.0

	> If true, Spark shuts down the StreamingContext gracefully on JVM shutdown rather than immediately.	

	如果为 true, spark 会在 JVM 关闭时优雅地关闭 StreamingContext, 而不是立即关闭。

- spark.streaming.kafka.maxRatePerPartition	

	Default: not set	

	Since Version: 1.3.0

	> Maximum rate (number of records per second) at which data will be read from each Kafka partition when using the new Kafka direct stream API. See the [Kafka Integration guide](https://spark.apache.org/docs/3.3.2/streaming-kafka-0-10-integration.html) for more details.	

	当使用 kafka 直连流 API 时，从每个 kafka 分区读取数据的最大速率（每秒读的记录数）

- spark.streaming.kafka.minRatePerPartition	

	Default: 1	

	Since Version: 2.4.0

	> Minimum rate (number of records per second) at which data will be read from each Kafka partition when using the new Kafka direct stream API.	

	当使用 kafka 直连流 API 时，从每个 kafka 分区读取数据的最小速率（每秒读的记录数）

- spark.streaming.ui.retainedBatches	

	Default: 1000	

	Since Version: 1.0.0

	> How many batches the Spark Streaming UI and status APIs remember before garbage collecting.	

	在垃圾回收前，Spark Streaming UI 和状态 APIs 记住的批次数量

- spark.streaming.driver.writeAheadLog.closeFileAfterWrite	

	Default: false	

	Since Version: 1.6.0

	> Whether to close the file after writing a write-ahead log record on the driver. Set this to 'true' when you want to use S3 (or any file system that does not support flushing) for the metadata WAL on the driver.	

	在写入驱动上的提前预写日志记录后，是否关闭文件。当你想要因驱动上的元数据 WAL 而使用 S3（或任意不支持flush的文件系统）时，将此设为 true.

- spark.streaming.receiver.writeAheadLog.closeFileAfterWrite	

	Default: false	

	Since Version: 1.6.0

	> Whether to close the file after writing a write-ahead log record on the receivers. Set this to 'true' when you want to use S3 (or any file system that does not support flushing) for the data WAL on the receivers.	

	在写入接收器上的提前预写日志记录后，是否关闭文件。当你想要因驱动上的元数据 WAL 而使用 S3（或任意不支持flush的文件系统）时，将此设为 true.

##### SparkR

- spark.r.numRBackendThreads	

	Default: 2	

	Since Version: 1.4.0

	> Number of threads used by RBackend to handle RPC calls from SparkR package.	

- spark.r.command	

	Default: Rscript	

	Since Version: 1.5.3

	> Executable for executing R scripts in cluster modes for both driver and workers.	

- spark.r.driver.command	

	Default: spark.r.command	

	Since Version: 1.5.3

	> Executable for executing R scripts in client modes for driver. Ignored in cluster modes.	

- spark.r.shell.command	

	Default: R	

	Since Version: 2.1.0

	> Executable for executing sparkR shell in client modes for driver. Ignored in cluster modes. It is the same as environment variable SPARKR_DRIVER_R, but take precedence over it. spark.r.shell.command is used for sparkR shell while spark.r.driver.command is used for running R script.	

- spark.r.backendConnectionTimeout	

	Default: 6000	

	Since Version: 2.1.0

	> Connection timeout set by R process on its connection to RBackend in seconds.	

- spark.r.heartBeatInterval	

	Default: 100	

	Since Version: 2.1.0

	> Interval for heartbeats sent from SparkR backend to R process to prevent connection timeout.	

##### GraphX

- spark.graphx.pregel.checkpointInterval	

	Default: -1	

	Since Version: 2.2.0

	> Checkpoint interval for graph and message in Pregel. It used to avoid stackOverflowError due to long lineage chains after lots of iterations. The checkpoint is disabled by default.	

##### Deploy

- spark.deploy.recoveryMode	

	Default:NONE	

	Since Version: 0.8.1

	> The recovery mode setting to recover submitted Spark jobs with cluster mode when it failed and relaunches. This is only applicable for cluster mode when running with Standalone or Mesos.	

	在作业失败并重启时，恢复作业的模式。这仅运行在 Standalone 或 Mesos 下的集群模式下可用。

- spark.deploy.zookeeper.url	

	Default:None	

	Since Version: 0.8.1

	> When `spark.deploy.recoveryMode` is set to `ZOOKEEPER`, this configuration is used to set the zookeeper URL to connect to.	

	当 `spark.deploy.recoveryMode` 设为 `ZOOKEEPER` 时，这个配置用于设置要连接的 zookeeper URL

- spark.deploy.zookeeper.dir	

	Default:None	

	Since Version: 0.8.1

	> When `spark.deploy.recoveryMode` is set to `ZOOKEEPER`, this configuration is used to set the zookeeper directory to store recovery state.	

	当 `spark.deploy.recoveryMode` 设为 `ZOOKEEPER` 时，这个配置用于设置存储恢复状态的 zookeeper 目录。

##### Cluster Managers

> Each cluster manager in Spark has additional configuration options. Configurations can be found on the pages for each mode:

###### [YARN](https://spark.apache.org/docs/3.3.2/running-on-yarn.html#configuration)
###### [Mesos](https://spark.apache.org/docs/3.3.2/running-on-mesos.html#configuration)
###### [Kubernetes](https://spark.apache.org/docs/3.3.2/running-on-kubernetes.html#configuration)
###### [Standalone Mode](https://spark.apache.org/docs/3.3.2/spark-standalone.html#cluster-launch-scripts)

## Environment Variables

> Certain Spark settings can be configured through environment variables, which are read from the `conf/spark-env.sh` script in the directory where Spark is installed (or `conf/spark-env.cmd` on Windows). In Standalone and Mesos modes, this file can give machine specific information such as hostnames. It is also sourced when running local Spark applications or submission scripts.

特定的 spark 设置可以通过环境变量配置，可以从 spark 安装目录下的 `conf/spark-env.sh` 脚本读取。在 Standalone 和 Mesos 模式下，这个文件可以给出机器的特定信息，例如主机名。当运行本地 spark 应用程序或提交脚本时，也可以使用它。

> Note that `conf/spark-env.sh` does not exist by default when Spark is installed. However, you can copy `conf/spark-env.sh.template` to create it. Make sure you make the copy executable.

注意，安装 spark 之后，默认情况下，`conf/spark-env.sh` 并不存在。然而，你可以复制 `conf/spark-env.sh.template` 文件创建它。确保副本是可执行的。

> The following variables can be set in spark-env.sh:

Environment Variable | Meaning
---|:---
`JAVA_HOME` | Location where Java is installed (if it's not on your default PATH).
`PYSPARK_PYTHON` | Python binary executable to use for PySpark in both driver and workers (default is python3 if available, otherwise python). Property `spark.pyspark.python` take precedence if it is set
`PYSPARK_DRIVER_PYTHON` | Python binary executable to use for PySpark in driver only (default is `PYSPARK_PYTHON`). Property `spark.pyspark.driver.python` take precedence if it is set
`SPARKR_DRIVER_R`	R binary executable to use for SparkR shell (default is R). Property `spark.r.shell.command` take precedence if it is set
`SPARK_LOCAL_IP` | IP address of the machine to bind to.
`SPARK_PUBLIC_DNS` | Hostname your Spark program will advertise to other machines.

> In addition to the above, there are also options for setting up the Spark [standalone cluster scripts](https://spark.apache.org/docs/3.3.2/spark-standalone.html#cluster-launch-scripts), such as number of cores to use on each machine and maximum memory.

除了上述环境变量，还存在设置 spark standalone 集群脚本的选项，例如，在每台机器上使用的核心数，和最大内存。

> Since `spark-env.sh` is a shell script, some of these can be set programmatically – for example, you might compute `SPARK_LOCAL_IP` by looking up the IP of a specific network interface.

由于 `spark-env.sh` 是 shell 脚本，一些变量可以通过编程的方式设置，例如，你可以通过查看特定网络接口的 IP 来计算 `SPARK_LOCAL_IP`

> Note: When running Spark on YARN in `cluster` mode, environment variables need to be set using the `spark.yarn.appMasterEnv.[EnvironmentVariableName]` property in your `conf/spark-defaults.conf` file. Environment variables that are set in `spark-env.sh` will not be reflected in the YARN Application Master process in `cluster` mode. See the [YARN-related Spark Properties](https://spark.apache.org/docs/3.3.2/running-on-yarn.html#spark-properties) for more information.

注意：当在 yarn 的 `cluster` 模式下运行 spark 时，需要在 `conf/spark-defaults.conf` 文件中使用 `spark.yarn.appMasterEnv.[EnvironmentVariableName]` 属性设置环境变量。

在 `spark-env.sh` 中设置的环境变量将不会映射在 `cluster` 模式下的 YARN Application Master 进程中。

## Configuring Logging

> Spark uses [log4j](http://logging.apache.org/log4j/) for logging. You can configure it by adding a `log4j2.properties` file in the `conf` directory. One way to start is to copy the existing `log4j2.properties.template` located there.

spark 使用 log4j 记录日志。你可以通过在 `conf` 目录下添加 `log4j2.properties` 文件进行配置。或者复制已存在的 `log4j2.properties.template` 文件。

> By default, Spark adds 1 record to the MDC (Mapped Diagnostic Context): `mdc.taskName`, which shows something like `task 1.0` in `stage 0.0`. You can add `%X{mdc.taskName}` to your patternLayout in order to print it in the logs. Moreover, you can use `spark.sparkContext.setLocalProperty(s"mdc.$name", "value")` to add user specific data into MDC. The key in MDC will be the string of “mdc.$name”.

默认，spark 给 MDC 添加一条记录: `mdc.taskName`, 它展示了像在 `stage 0.0` 中的 `task 1.0` 信息。你可以为你的 patternLayout 添加 `%X{mdc.taskName}`，为了能在日志中打印它。

而且，你可以使用 `spark.sparkContext.setLocalProperty(s"mdc.$name", "value")` 将用户特定的数据添加到 MDC. MDC 中的 key 是 “mdc.$name” 字符串。

## Overriding configuration directory

> To specify a different configuration directory other than the default “SPARK_HOME/conf”, you can set `SPARK_CONF_DIR`. Spark will use the configuration files (`spark-defaults.conf`, `spark-env.sh`, `log4j2.properties`, etc) from this directory.

为了指定不同于默认配置目录 “SPARK_HOME/conf” 的不同的目录，你可以设置 `SPARK_CONF_DIR`. spark 将使用来自这个目录的配置文件(`spark-defaults.conf`, `spark-env.sh`, `log4j2.properties`, etc)。

## Inheriting Hadoop Cluster Configuration

> If you plan to read and write from HDFS using Spark, there are two Hadoop configuration files that should be included on Spark’s classpath:

如果你计划使用 spark 读写来自 HDFS 的数据，应该在 spark 的类路径下包含两类 hadoop 配置文件：

- hdfs-site.xml: 提供了 HDFS 客户端默认的行为
- core-site.xml: 设置了默认的文件系统名称

> hdfs-site.xml, which provides default behaviors for the HDFS client.
> core-site.xml, which sets the default filesystem name.

> The location of these configuration files varies across Hadoop versions, but a common location is inside of `/etc/hadoop/conf`. Some tools create configurations on-the-fly, but offer a mechanism to download copies of them.

这些配置文件的位置会随 hadoop 版本的变化而变化，但一般是在 `/etc/hadoop/conf` 中。一些工具动态地创建配置，但提供了下载配置副本的机制。

> To make these files visible to Spark, set `HADOOP_CONF_DIR` in `$SPARK_HOME/conf/spark-env.sh` to a location containing the configuration files.

为了让这些文件对 spark 可见，将 `$SPARK_HOME/conf/spark-env.sh` 中的 `HADOOP_CONF_DIR` 设置成一个包含配置文件的路径。

## Custom Hadoop/Hive Configuration

> If your Spark application is interacting with Hadoop, Hive, or both, there are probably Hadoop/Hive configuration files in Spark’s classpath.

如果你的 spark 应用程序正和 hadoop 或 hive 或 hadoop和hive 交互，那么可能 hadoop/hive 配置文件已在 spark 类路径下。

> Multiple running applications might require different Hadoop/Hive client side configurations. You can copy and modify `hdfs-site.xml`, `core-site.xml`, `yarn-site.xml`, `hive-site.xml` in Spark’s classpath for each application. In a Spark cluster running on YARN, these configuration files are set cluster-wide, and cannot safely be changed by the application.

多个运行中的应用程序可能要求不同的 hadoop/hive 客户端配置。你可以为每个应用程序复制并修改 spark 类路径下的 `hdfs-site.xml`, `core-site.xml`, `yarn-site.xml`, `hive-site.xml` 文件。在运行在 yarn 之上的 spark 集群，这些配置文件是集群范围的，不能被应用程序安全地修改。

> The better choice is to use spark hadoop properties in the form of `spark.hadoop.*`, and use spark hive properties in the form of `spark.hive.*`. For example, adding configuration “spark.hadoop.abc.def=xyz” represents adding hadoop property “abc.def=xyz”, and adding configuration “spark.hive.abc=xyz” represents adding hive property “hive.abc=xyz”. They can be considered as same as normal spark properties which can be set in `$SPARK_HOME/conf/spark-defaults.conf`

最好的选择就是使用 `spark.hadoop.*` 这种形式的 spark hadoop 属性，使用 `spark.hive.*` 这种形式的 spark hive 属性。例如，添加 `spark.hadoop.abc.def=xyz` 配置表示添加 hadoop 属性 `abc.def=xyz`, 添加 `spark.hive.abc=xyz` 配置表示添加 hive 属性 `hive.abc=xyz`. 它们可被认为是与在 `$SPARK_HOME/conf/spark-defaults.conf` 中的属性相同。

> In some cases, you may want to avoid hard-coding certain configurations in a SparkConf. For instance, Spark allows you to simply create an empty conf and set spark/spark hadoop/spark hive properties.

一些情况下，你可能想避免在 SparkConf 中硬编码特定配置。例如，spark 允许你创建空的 conf, 并设置 `spark`/`spark hadoop`/`spark hive` 属性。

	val conf = new SparkConf().set("spark.hadoop.abc.def", "xyz")
	val sc = new SparkContext(conf)

Also, you can modify or add configurations at runtime:

你可以在运行时修改或添加配置

	./bin/spark-submit \ 
	  	--name "My app" \ 
	  	--master local[4] \  
	  	--conf spark.eventLog.enabled=false \ 
	  	--conf "spark.executor.extraJavaOptions=-XX:+PrintGCDetails -XX:+PrintGCTimeStamps" \ 
	  	--conf spark.hadoop.abc.def=xyz \
	  	--conf spark.hive.abc=xyz
	  	myApp.jar

## Custom Resource Scheduling and Configuration Overview

> GPUs and other accelerators have been widely used for accelerating special workloads, e.g., deep learning and signal processing. Spark now supports requesting and scheduling generic resources, such as GPUs, with a few caveats. The current implementation requires that the resource have addresses that can be allocated by the scheduler. It requires your cluster manager to support and be properly configured with the resources.

GPUs 和其他加速器被广泛用于加速特殊工作负载，例如深度学习和信息处理。spark 现在支持请求和调度通用资源，例如 GPUs. 当前实现要求，资源要有调度器分配的地址。它要求你的集群管理器支持，且使用资源正确地进行了配置。

> There are configurations available to request resources for the driver: `spark.driver.resource.{resourceName}.amount`, request resources for the executor(s): `spark.executor.resource.{resourceName}.amount` and specify the requirements for each task: `spark.task.resource.{resourceName}.amount`. The `spark.driver.resource.{resourceName}.discoveryScript` config is required on YARN, Kubernetes and a client side Driver on Spark Standalone. `spark.executor.resource.{resourceName}.discoveryScript` config is required for YARN and Kubernetes. Kubernetes also requires `spark.driver.resource.{resourceName}.vendor` and/or `spark.executor.resource.{resourceName}.vendor`. See the config descriptions above for more information on each.

为 driver 请求资源的可用配置: `spark.driver.resource.{resourceName}.amount`

为 executor 请求资源的可用配置: `spark.executor.resource.{resourceName}.amount`

为每个任务指定要求: `spark.task.resource.{resourceName}.amount`

`spark.driver.resource.{resourceName}.discoveryScript` 配置要求在 YARN, Kubernetes 和 Spark Standalone 上的客户端 Driver

`spark.executor.resource.{resourceName}.discoveryScript` 配置要求在 YARN 和 Kubernetes

Kubernetes 还要求 `spark.driver.resource.{resourceName}.vendor` 和/或 `spark.executor.resource.{resourceName}.vendor`

> Spark will use the configurations specified to first request containers with the corresponding resources from the cluster manager. Once it gets the container, Spark launches an Executor in that container which will discover what resources the container has and the addresses associated with each resource. The Executor will register with the Driver and report back the resources available to that Executor. The Spark scheduler can then schedule tasks to each Executor and assign specific resource addresses based on the resource requirements the user specified. The user can see the resources assigned to a task using the `TaskContext.get().resources` api. On the driver, the user can see the resources assigned with the SparkContext `resources` call. It’s then up to the user to use the assignedaddresses to do the processing they want or pass those into the ML/AI framework they are using.

spark 将使用指定的配置，首先请求来自集群管理器的对应资源中的 containers. 一旦获得 containers, spark 就在那个 containers 中启动 Executor，就会发现那个 containers 中有什么资源和与每个资源关联的地址。

Executor 向 Driver 注册，Driver 告诉 Executor 哪些资源可用。然后，spark 调度器将任务调度给每个 Executor，并根据用户指定的资源请求，分配特定资源地址。

用户可以使用 `TaskContext.get().resources` api 看到分配给任务的资源。在 driver 上，用户使用 SparkContext `resources` 调用可以看到分配的资源。然后由用户决定使用分配的地址进行他们想要的处理或将其传递到他们正在使用的 ML/AI 框架中。

> See your cluster manager specific page for requirements and details on each of - [YARN](https://spark.apache.org/docs/3.3.2/running-on-yarn.html#resource-allocation-and-configuration-overview), [Kubernetes](https://spark.apache.org/docs/3.3.2/running-on-kubernetes.html#resource-allocation-and-configuration-overview) and [Standalone Mode](https://spark.apache.org/docs/3.3.2/spark-standalone.html#resource-allocation-and-configuration-overview). It is currently not available with Mesos or local mode. And please also note that local-cluster mode with multiple workers is not supported(see Standalone documentation).

## Stage Level Scheduling Overview

> The stage level scheduling feature allows users to specify task and executor resource requirements at the stage level. This allows for different stages to run with executors that have different resources. A prime example of this is one ETL stage runs with executors with just CPUs, the next stage is an ML stage that needs GPUs. Stage level scheduling allows for user to request different executors that have GPUs when the ML stage runs rather then having to acquire executors with GPUs at the start of the application and them be idle while the ETL stage is being run. This is only available for the RDD API in Scala, Java, and Python. It is available on YARN and Kubernetes when dynamic allocation is enabled. See the [YARN](https://spark.apache.org/docs/3.3.2/running-on-yarn.html#stage-level-scheduling-overview) page or [Kubernetes](https://spark.apache.org/docs/3.3.2/running-on-kubernetes.html#stage-level-scheduling-overview) page for more implementation details.

stage 级别调度特点允许用户在 stage 级别指定任务和 executor 资源要求。这就允许具有不同资源的 executors 运行不同的 stages. 一个例子就是仅具有 CPUs 的 executors 运行一个 ETL stage, 下一个 stage 是 ML stage, 需要 GPUs.

stage 级别的调度允许用户，在运行 ML stage 时，请求具有 GPUs 的不同 executors, 而不是必须在应用程序的初始阶段获取具有 GPUs 的 executors, 在 ETL stage 运行时，它们就空闲了。

这仅对 Scala, Java 和 Python 下的 RDD API 可用。当启用动态分配时，在 YARN 和 Kubernetes 上可用。

> See the `RDD.withResources` and `ResourceProfileBuilder` API’s for using this feature. The current implementation acquires new executors for each `ResourceProfile` created and currently has to be an exact match. Spark does not try to fit tasks into an executor that require a different ResourceProfile than the executor was created with. Executors that are not in use will idle timeout with the dynamic allocation logic. The default configuration for this feature is to only allow one ResourceProfile per stage. If the user associates more then 1 ResourceProfile to an RDD, Spark will throw an exception by default. See config `spark.scheduler.resource.profileMergeConflicts` to control that behavior. The current merge strategy Spark implements when `spark.scheduler.resource.profileMergeConflicts` is enabled is a simple max of each resource within the conflicting ResourceProfiles. Spark will create a new ResourceProfile with the max of each of the resources.

查看 `RDD.withResources` 和 `ResourceProfileBuilder` API 来使用这个特性。当前实现为创建的每个 `ResourceProfile` 获取新的 executors, 且必须精确匹配。spark 不会尝试将任务装入要求不同于创建 executor 的 ResourceProfile 的 executor 中。未使用的 executors 将使用动态分配逻辑使超时闲置。

这个特性的默认配置是仅允许每个 stage 一个 ResourceProfile. 如果用户给 RDD 关联了超过1个 ResourceProfile, spark 将默认抛出异常。配置 `spark.scheduler.resource.profileMergeConflicts` 控制这那个行为。

当 `spark.scheduler.resource.profileMergeConflicts` 启用时，当前 spark 实现的合并策略是在冲突的 ResourceProfiles 中取每个资源的最大。

spark 将使用每个资源的最大创建一个新的 ResourceProfiles

## Push-based shuffle overview

> Push-based shuffle helps improve the reliability and performance of spark shuffle. It takes a best-effort approach to push the shuffle blocks generated by the map tasks to remote external shuffle services to be merged per shuffle partition. Reduce tasks fetch a combination of merged shuffle partitions and original shuffle blocks as their input data, resulting in converting small random disk reads by external shuffle services into large sequential reads. Possibility of better data locality for reduce tasks additionally helps minimize network IO. Push-based shuffle takes priority over batch fetch for some scenarios, like partition coalesce when merged output is available.

基于 push 的 shuffle 帮助改善可靠性和 spark shuffle 的性能。把 map 任务生成的 shuffle 块推送到远程外部 shuffle 服务，以便每个 shuffle 分区合并块，这需要尽最大努力。

reduce 任务获取合并的 shuffle 分区和作为它们输入数据的原始 shuffle 块，使得由外部 shuffle 服务将小的随机磁盘读取转换成大的序列化读取。

对于 reduce 任务，更佳的数据本地化可能性会额外帮助减少网络IO. 在一些场景下，基于 push 的 shuffle 会比批次获取具有更高优先级，就像当合并的输出可用时，合并分区。

> Push-based shuffle improves performance for long running jobs/queries which involves large disk I/O during shuffle. Currently it is not well suited for jobs/queries which runs quickly dealing with lesser amount of shuffle data. This will be further improved in the future releases.

基于 push 的 shuffle 会改善长时间运行的作业/查询的性能，此类作业/查询在 shuffle 期间涉及大的磁盘I/O. 

当前它并不非常适合快速运行、处理少量 shuffle 数据的作业/查询。这将在未来版本中改善。

> Currently push-based shuffle is only supported for Spark on YARN with external shuffle service.

当前，基于 push 的 shuffle 仅支持使用外部 shuffle 服务的 Spark on YARN

### External Shuffle service(server) side configuration options

- spark.shuffle.push.server.mergedShuffleFileManagerImpl	

	Default: org.apache.spark.network.shuffle.NoOpMergedShuffleFileManager	

	Since Version: 3.2.0

	> Class name of the implementation of `MergedShuffleFileManager` that manages push-based shuffle. This acts as a server side config to disable or enable push-based shuffle. By default, push-based shuffle is disabled at the server side.

	实现了 MergedShuffleFileManager 的类名管理着基于 push 的 shuffle. 这个行为是服务端的配置，以禁用或启用基于 push 的 shuffle.

	默认情况下，在服务端，禁用基于 push 的 shuffle.

	> To enable push-based shuffle on the server side, set this config to `org.apache.spark.network.shuffle.RemoteBlockPushResolver`

	为了启用基于 push 的 shuffle, 将这个配置设置为 `org.apache.spark.network.shuffle.RemoteBlockPushResolver`

- spark.shuffle.push.server.minChunkSizeInMergedShuffleFile	

	Default: 2m	

	Since Version: 3.2.0

	> The minimum size of a chunk when dividing a merged shuffle file into multiple chunks during push-based shuffle. A merged shuffle file consists of multiple small shuffle blocks. Fetching the complete merged shuffle file in a single disk I/O increases the memory requirements for both the clients and the external shuffle services. Instead, the external shuffle service serves the merged file in MB-sized chunks.

	在基于 push 的 shuffle 期间，将一个合并的 shuffle 文件划分成多个块时，一个块的最小大小。一个合并的 shuffle 文件由多个小的 shuffle 块组成。

	在单个磁盘 I/O 中获取完整的合并后的 shuffle 文件同时增加了客户端和外部 shuffle 服务对内存的要求。

	外部的 shuffle 服务以 MB 大小的块来服务的。

	> This configuration controls how big a chunk can get. A corresponding index file for each merged shuffle file will be generated indicating chunk boundaries.

	这个配置控制可以获得多大的块。对于每个合并的 shuffle 文件，将会生成表示块边界的索引文件。

	> Setting this too high would increase the memory requirements on both the clients and the external shuffle service.

	将这个值设置的太高会增加客户端和外部 shuffle 服务对内存的要求。

	> Setting this too low would increase the overall number of RPC requests to external shuffle service unnecessarily.

	将这个值设置的太低会减少对不必要的外部 shuffle 服务的请求数。

- spark.shuffle.push.server.mergedIndexCacheSize	

	Default: 100m	

	Since Version: 3.2.0

	> The maximum size of cache in memory which could be used in push-based shuffle for storing merged index files. This cache is in addition to the one configured via `spark.shuffle.service.index.cache.size`.	

	内存中缓存的最大大小，它将在基于 push 的 shuffle 中用于存储合并的索引文件。此缓存是通过 `spark.shuffle.service.index.cache.size` 配置的缓存之外的。

### Client side configuration options

- spark.shuffle.push.enabled	

	Default: false	

	Since Version: 3.2.0

	> Set to true to enable push-based shuffle on the client side and works in conjunction with the server side flag `spark.shuffle.push.server.mergedShuffleFileManagerImpl`.	

	设为 true, 以启用客户端的基于 push 的 shuffle, 和服务端标志 `spark.shuffle.push.server.mergedShuffleFileManagerImpl` 一起工作。

- spark.shuffle.push.finalize.timeout	

	Default: 10s	

	Since Version: 3.2.0

	> The amount of time driver waits in seconds, after all mappers have finished for a given shuffle map stage, before it sends merge finalize requests to remote external shuffle services. This gives the external shuffle services extra time to merge blocks. Setting this too long could potentially lead to performance regression.	

	一个给定 shuffle map stage 的所有 mapper 完成之后，在给远程外部 shuffle 服务发送合并完成请求之前，驱动等待的时长。

	这给了外部 shuffle 服务额外的时间来合并块。将这个值设置的太长会导致潜在地性能衰退。

- spark.shuffle.push.maxRetainedMergerLocations	

	Default: 500	

	Since Version: 3.2.0

	> Maximum number of merger locations cached for push-based shuffle. Currently, merger locations are hosts of external shuffle services responsible for handling pushed blocks, merging them and serving merged blocks for later shuffle fetch.	

	对于基于 push 的 shuffle, 缓存的合并路径的最大数量。当前，合并路径是外部 shuffle 服务的主机，负责处理推送块、合并块、为后面的 shuffle 获取服务合并的块。

- spark.shuffle.push.mergersMinThresholdRatio	

	Default: 0.05	

	Since Version: 3.2.0

	> Ratio used to compute the minimum number of shuffle merger locations required for a stage based on the number of partitions for the reducer stage. For example, a reduce stage which has 100 partitions and uses the default value 0.05 requires at least 5 unique merger locations to enable push-based shuffle.	

	基于 reducer stage 的分区数量，用来计算一个 stage 要求的 shuffle 合并路径的最小数量的比例。

	例如，一个具有100个分区的 reduce stage，此配置使用默认的值是0.05，就要求至少需要5个唯一的合并路径来启用基于 push 的 shuffle.

- spark.shuffle.push.mergersMinStaticThreshold	

	Default: 5	

	Since Version: 3.2.0

	> The static threshold for number of shuffle push merger locations should be available in order to enable push-based shuffle for a stage. Note this config works in conjunction with `spark.shuffle.push.mergersMinThresholdRatio`. Maximum of `spark.shuffle.push.mergersMinStaticThreshold` and `spark.shuffle.push.mergersMinThresholdRatio` ratio number of mergers needed to enable push-based shuffle for a stage. For example: with 1000 partitions for the child stage with `spark.shuffle.push.mergersMinStaticThreshold` as 5 and `spark.shuffle.push.mergersMinThresholdRatio` set to 0.05, we would need at least 50 mergers to enable push-based shuffle for that stage.	

	为启用一个 stage 的基于 push 的 shuffle, 可用的 shuffle push merger 路径数量的静态阈值。注意，这个配置和 `spark.shuffle.push.mergersMinThresholdRatio` 一起使用。

	`spark.shuffle.push.mergersMinStaticThreshold` 和 基于 `spark.shuffle.push.mergersMinThresholdRatio` 计算出的在一个 stage 中启用基于 push 的 shuffle 所需的 mergers 数的最大值。

	例如，对于具有1000个分区的子 stage, 将 `spark.shuffle.push.mergersMinStaticThreshold` 设为5，且 `spark.shuffle.push.mergersMinThresholdRatio` 设为0.05，我们将需要至少50个 mergers, 为那个 stage 启用基于 push 的 shuffle.

- spark.shuffle.push.maxBlockSizeToPush	

	Default: 1m

	Since Version: 3.2.0

	> The max size of an individual block to push to the remote external shuffle services. Blocks larger than this threshold are not pushed to be merged remotely. These shuffle blocks will be fetched in the original manner.

	被推送到远程外部 shuffle 服务的一个独立块的最大大小。大于这个阈值的块不会被推送。将以原始的方式获取这些 shuffle 块。

	> Setting this too high would result in more blocks to be pushed to remote external shuffle services but those are already efficiently fetched with the existing mechanisms resulting in additional overhead of pushing the large blocks to remote external shuffle services. It is recommended to set `spark.shuffle.push.maxBlockSizeToPush` lesser than `spark.shuffle.push.maxBlockBatchSize` config's value.

	设置的太高将导致更多的块被推送到远程的外部 shuffle 服务，但是这些块早已使用已有机制获取，这就产生了额外的开销。

	推荐将这个值设置的小于 `spark.shuffle.push.maxBlockBatchSize` 值。

	> Setting this too low would result in lesser number of blocks getting merged and directly fetched from mapper external shuffle service results in higher small random reads affecting overall disk I/O performance.

	设置的太小将导致更少的块被合并，且直接从 mapper 外部 shuffle 服务获取，导致更多的小的随机读取，从而影响总体的磁盘 I/O 性能。

- spark.shuffle.push.maxBlockBatchSize	

	Default: 3m	

	Since Version: 3.2.0

	> The max size of a batch of shuffle blocks to be grouped into a single push request. Default is set to 3m in order to keep it slightly higher than `spark.storage.memoryMapThreshold` default which is 2m as it is very likely that each batch of block gets memory mapped which incurs higher overhead.	

	划分到一个 push 请求的由 shuffle 块组成的批次的最大大小。

	默认为3m，是为了保持它比 `spark.storage.memoryMapThreshold` 的默认值略高。因为很可能每个块的批次都要映射内存，这会导致更高的开销。

- spark.shuffle.push.minShuffleSizeToWait	

	Default: 500m	

	Since Version: 3.3.0

	> Driver will wait for merge finalization to complete only if total shuffle data size is more than this threshold. If total shuffle size is less, driver will immediately finalize the shuffle output.	

	只有当总的 shuffle 大小超过此阈值时，驱动才会等待合并完成。

	如果总的 shuffle 大小更小，驱动将自动完成 shuffle 输出。

- spark.shuffle.push.minCompletedPushRatio	

	Default: 1.0	

	Since Version: 3.3.0

	> Fraction of minimum map partitions that should be push complete before driver starts shuffle merge finalization during push based shuffle.	

	在基于 push 的 shuffle 期间，在驱动开始 shuffle 合并完成前，应该被推送完成的 map 分区的最小比例。