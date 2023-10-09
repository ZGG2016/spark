# Hints

[TOC]

## Description

> Hints give users a way to suggest how Spark SQL to use specific approaches to generate its execution plan.

Hints 为用户提供了一种方式，即 Spark SQL 如何使用指定的方式生成它的执行计划的方式。

### Syntax

	/*+ hint [ , ... ] */

## Partitioning Hints

> Partitioning hints allow users to suggest a partitioning strategy that Spark should follow. `COALESCE`, `REPARTITION`, and `REPARTITION_BY_RANGE` hints are supported and are equivalent to `coalesce`, `repartition`, and `repartitionByRange` [Dataset APIs](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/sql/Dataset.html), respectively. The `REBALANCE` can only be used as a hint .These hints give users a way to tune performance and control the number of output files in Spark SQL. When multiple partitioning hints are specified, multiple nodes are inserted into the logical plan, but the leftmost hint is picked by the optimizer.

分区提示允许用户建议 spark 应该遵循的分区策略。

`COALESCE`, `REPARTITION` 和 `REPARTITION_BY_RANGE` 提示，等价于 `coalesce`, `repartition` 和 `repartitionByRange` Dataset APIs.

`REBALANCE` 仅能用作提示。

这些提示为用户提供了调优性能，和控制输出文件数量的方式。

当指定了多个分区提示时，往逻辑计划中会插入多个节点，但是优化器选择最左边的提示。

### Partitioning Hints Types

- COALESCE

	The `COALESCE` hint can be used to reduce the number of partitions to the specified number of partitions. It takes a partition number as a parameter.

	`COALESCE` 提示可以用于将分区数量减少到指定值。它接收分区数量作为参数。

- REPARTITION

	The `REPARTITION` hint can be used to repartition to the specified number of partitions using the specified partitioning expressions. It takes a partition number, column names, or both as parameters.

	`REPARTITION` 提示可以用于重分区，使用指定的分区表达式，将分区数量变化到指定值。

	它接收分区数量、列名或同时接收分区数量和列名作为参数。

- REPARTITION_BY_RANGE

	The `REPARTITION_BY_RANGE` hint can be used to repartition to the specified number of partitions using the specified partitioning expressions. It takes column names and an optional partition number as parameters.

	`REPARTITION_BY_RANGE` 提示可以用于重分区，使用指定的分区表达式，将分区数量变化到指定值。

	它接收列名和可选的分区数量作为参数。

- REBALANCE

	The `REBALANCE` hint can be used to rebalance the query result output partitions, so that every partition is of a reasonable size (not too small and not too big). It can take column names as parameters, and try its best to partition the query result by these columns. This is a best-effort: if there are skews, Spark will split the skewed partitions, to make these partitions not too big. This hint is useful when you need to write the result of this query to a table, to avoid too small/big files. This hint is ignored if AQE is not enabled.

	`REBALANCE` 提示用于再平衡查询结果的输出分区，使得每个分区具有合理的大小（不太小，也不太大）。

	它接收列名作为参数，尽最大努力基于这些列分区查询结果。

	这是最佳效果：如果存在倾斜，spark 将划分倾斜的分区，让这些分区不太大。

	当你需要将查询结果写入到一张表时，为了避免太大或太小的文件，这个提示是有用的。

	如果没有启用 AQE, 这个提示就被忽略。

### Examples

```sql
SELECT /*+ COALESCE(3) */ * FROM t;

SELECT /*+ REPARTITION(3) */ * FROM t;

SELECT /*+ REPARTITION(c) */ * FROM t;

SELECT /*+ REPARTITION(3, c) */ * FROM t;

SELECT /*+ REPARTITION_BY_RANGE(c) */ * FROM t;

SELECT /*+ REPARTITION_BY_RANGE(3, c) */ * FROM t;

SELECT /*+ REBALANCE */ * FROM t;

SELECT /*+ REBALANCE(3) */ * FROM t;

SELECT /*+ REBALANCE(c) */ * FROM t;

SELECT /*+ REBALANCE(3, c) */ * FROM t;

-- multiple partitioning hints
EXPLAIN EXTENDED SELECT /*+ REPARTITION(100), COALESCE(500), REPARTITION_BY_RANGE(3, c) */ * FROM t;
== Parsed Logical Plan ==
'UnresolvedHint REPARTITION, [100]
+- 'UnresolvedHint COALESCE, [500]
   +- 'UnresolvedHint REPARTITION_BY_RANGE, [3, 'c]
      +- 'Project [*]
         +- 'UnresolvedRelation [t]

== Analyzed Logical Plan ==
name: string, c: int
Repartition 100, true
+- Repartition 500, false
   +- RepartitionByExpression [c#30 ASC NULLS FIRST], 3
      +- Project [name#29, c#30]
         +- SubqueryAlias spark_catalog.default.t
            +- Relation[name#29,c#30] parquet

== Optimized Logical Plan ==
Repartition 100, true
+- Relation[name#29,c#30] parquet

== Physical Plan ==
Exchange RoundRobinPartitioning(100), false, [id=#121]
+- *(1) ColumnarToRow
   +- FileScan parquet default.t[name#29,c#30] Batched: true, DataFilters: [], Format: Parquet,
      Location: CatalogFileIndex[file:/spark/spark-warehouse/t], PartitionFilters: [],
      PushedFilters: [], ReadSchema: struct<name:string>
```

## Join Hints

> Join hints allow users to suggest the join strategy that Spark should use. Prior to Spark 3.0, only the `BROADCAST` Join Hint was supported. `MERGE`, `SHUFFLE_HASH` and `SHUFFLE_REPLICATE_NL` Joint Hints support was added in 3.0. When different join strategy hints are specified on both sides of a join, Spark prioritizes hints in the following order: `BROADCAST` over `MERGE` over `SHUFFLE_HASH` over `SHUFFLE_REPLICATE_NL`. When both sides are specified with the `BROADCAST` hint or the `SHUFFLE_HASH` hint, Spark will pick the build side based on the join type and the sizes of the relations. Since a given strategy may not support all join types, Spark is not guaranteed to use the join strategy suggested by the hint.

join 提示允许用户建议 spark 应该使用的 join 策略。

在 Spark 3.0 版本之前，仅支持 `BROADCAST` join 提示。之后添加了 `MERGE`, `SHUFFLE_HASH` 和 `SHUFFLE_REPLICATE_NL`.

当在 join 的两端指定了不同的 join 策略时，spark 按如下顺序选择提示: `BROADCAST`>`MERGE`>`SHUFFLE_HASH`>`SHUFFLE_REPLICATE_NL`

当两端都使用 `BROADCAST` 提示或 `SHUFFLE_HASH` 提示时，spark 将基于 join 类型和关系实体的大小选择构建端的提示。

由于一个给定的策略可能不支持所有的 join 类型，spark 不能保证会使用提示建议的 join 策略。

### Join Hints Types

- BROADCAST

	Suggests that Spark use broadcast join. The join side with the hint will be broadcast regardless of `autoBroadcastJoinThreshold`. If both sides of the join have the broadcast hints, the one with the smaller size (based on stats) will be broadcast. The aliases for `BROADCAST` are `BROADCASTJOIN` and `MAPJOIN`.

	建议使用广播 join. 

	不管 `autoBroadcastJoinThreshold` 参数，将广播具有提示的 join 端。

	如果 join 两端都有广播提示，具有更小大小的关系实体将被广播。

	`BROADCAST` 的别名是 `BROADCASTJOIN` 和 `MAPJOIN`.

- MERGE

	Suggests that Spark use shuffle sort merge join. The aliases for `MERGE` are `SHUFFLE_MERGE` and `MERGEJOIN`.

	建议使用 shuffle 排序合并 join.

	`MERGE` 的别名是 `SHUFFLE_MERGE` 和 `MERGEJOIN`.

- SHUFFLE_HASH

	Suggests that Spark use shuffle hash join. If both sides have the shuffle hash hints, Spark chooses the smaller side (based on stats) as the build side.

	建议使用 shuffle 哈希 join.

	如果两端都有 shuffle 哈希提示，spark 使用更小大小（基于统计信息）的一端作为构建端。

- SHUFFLE_REPLICATE_NL

	Suggests that Spark use shuffle-and-replicate nested loop join.

	建议使用 shuffle-and-replicate 嵌套循环 join.

### Examples

```sql
-- Join Hints for broadcast join
SELECT /*+ BROADCAST(t1) */ * FROM t1 INNER JOIN t2 ON t1.key = t2.key;
SELECT /*+ BROADCASTJOIN (t1) */ * FROM t1 left JOIN t2 ON t1.key = t2.key;
SELECT /*+ MAPJOIN(t2) */ * FROM t1 right JOIN t2 ON t1.key = t2.key;

-- Join Hints for shuffle sort merge join
SELECT /*+ SHUFFLE_MERGE(t1) */ * FROM t1 INNER JOIN t2 ON t1.key = t2.key;
SELECT /*+ MERGEJOIN(t2) */ * FROM t1 INNER JOIN t2 ON t1.key = t2.key;
SELECT /*+ MERGE(t1) */ * FROM t1 INNER JOIN t2 ON t1.key = t2.key;

-- Join Hints for shuffle hash join
SELECT /*+ SHUFFLE_HASH(t1) */ * FROM t1 INNER JOIN t2 ON t1.key = t2.key;

-- Join Hints for shuffle-and-replicate nested loop join
SELECT /*+ SHUFFLE_REPLICATE_NL(t1) */ * FROM t1 INNER JOIN t2 ON t1.key = t2.key;

-- When different join strategy hints are specified on both sides of a join, Spark
-- prioritizes the BROADCAST hint over the MERGE hint over the SHUFFLE_HASH hint
-- over the SHUFFLE_REPLICATE_NL hint.
-- Spark will issue Warning in the following example
-- org.apache.spark.sql.catalyst.analysis.HintErrorLogger: Hint (strategy=merge)
-- is overridden by another hint and will not take effect.
SELECT /*+ BROADCAST(t1), MERGE(t1, t2) */ * FROM t1 INNER JOIN t2 ON t1.key = t2.key;
```