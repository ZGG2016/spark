# ORC Files

[TOC]

> [Apache ORC](https://orc.apache.org/) is a columnar format which has more advanced features like native zstd compression, bloom filter and columnar encryption.

Apache ORC 是一种柱状格式，具有更高级的特性，例如原生 zstd 压缩，布隆过滤器和列加密。

## ORC Implementation

> Spark supports two ORC implementations (`native` and `hive`) which is controlled by `spark.sql.orc.impl`. Two implementations share most functionalities with different design goals.

Spark 支持两种 ORC 实现: `native` 和 `hive`, 由 `spark.sql.orc.impl` 参数控制。这两种实现具有不同的设计目标，但共享大部分的功能。

- `native`: 用来遵循 Spark 数据源行为，就像 Parquet.
- `hive`: 用来遵循 Hive 行为，使用 Hive SerDe.

> `native` implementation is designed to follow Spark’s data source behavior like Parquet.
> `hive` implementation is designed to follow Hive’s behavior and uses Hive SerDe.

> For example, historically, `native` implementation handles `CHAR/VARCHAR` with Spark’s native `String` while `hive` implementation handles it via Hive `CHAR/VARCHAR`. The query results are different. Since Spark 3.1.0, [SPARK-33480](https://issues.apache.org/jira/browse/SPARK-33480) removes this difference by supporting `CHAR/VARCHAR` from Spark-side.

例如，从历史上看, `native` 使用 Spark 的原生字符串处理 `CHAR/VARCHAR`, 而 `hive` 通过 hive 的 `CHAR/VARCHAR` 处理。查询结果是不同的。从 Spark 3.1.0 开始, SPARK-33480 通过在 spark 端支持 `CHAR/VARCHAR` 移出了这个不同点。

## Vectorized Reader

> `native` implementation supports a vectorized ORC reader and has been the default ORC implementaion since Spark 2.3. The vectorized reader is used for the native ORC tables (e.g., the ones created using the clause `USING ORC`) when `spark.sql.orc.impl` is set to `native` and `spark.sql.orc.enableVectorizedReader` is set to `true`. For nested data types (array, map and struct), vectorized reader is disabled by default. Set `spark.sql.orc.enableNestedColumnVectorizedReader` to `true` to enable vectorized reader for these types.

`native` 实现支持一个向量化的 ORC 阅读器，从 Spark 2.3 开始，是 ORC 的默认实现。

通过设置属性 `spark.sql.orc.impl=native` 和 `spark.sql.orc.enableVectorizedReader=true` 在原生 ORC 表（使用`USING ORC`子句创建）上使用向量化阅读器。

对于嵌套数据类型，默认禁用向量化阅读器，通过设置 `spark.sql.orc.enableNestedColumnVectorizedReader=true` 再次启用。 

> For the Hive ORC serde tables (e.g., the ones created using the clause `USING HIVE OPTIONS(fileFormat 'ORC')`), the vectorized reader is used when `spark.sql.hive.convertMetastoreOrc` is also set to `true`, and is turned on by default.

对于 Hive ORC 序列化/反序列化表（使用`USING HIVE OPTIONS(fileFormat 'ORC')`子句创建），通过设置 `spark.sql.hive.convertMetastoreOrc=true` 使用向量化阅读器，它默认是打开的。

## Schema Merging

> Like Protocol Buffer, Avro, and Thrift, ORC also supports schema evolution. Users can start with a simple schema, and gradually add more columns to the schema as needed. In this way, users may end up with multiple ORC files with different but mutually compatible schemas. The ORC data source is now able to automatically detect this case and merge schemas of all these files.

就像 Protocol Buffer, Avro, and Thrift, ORC 也支持结构演化。

用户可以以一个简单的结构开始，逐步添加更多的列。最后，得到一个具有类型不同但又互相兼容的结构的 ORC 文件。

ORC 数据源现在能够自动检测这种情况，合并所有文件的结构。

> Since schema merging is a relatively expensive operation, and is not a necessity in most cases, we turned it off by default . You may enable it by

> setting data source option `mergeSchema` to `true` when reading ORC files, or
> setting the global SQL option `spark.sql.orc.mergeSchema` to `true`.

因为结构合并是一个相对昂贵的操作，在大多数情况下不是必要的，所以默认情况下是关闭的。可以通过如下方式打开：

- 在读 ORC 文件时，设置数据源选项 `mergeSchema` 为 `true`
- 设置全局选项 `spark.sql.orc.mergeSchema` 为 `true`

## Zstandard

> Spark supports both Hadoop 2 and 3. Since Spark 3.2, you can take advantage of Zstandard compression in ORC files on both Hadoop versions. Please see [Zstandard](https://facebook.github.io/zstd/) for the benefits.

Spark 同时支持 Hadoop 2 和 3. 从 Spark 3.2 开始，你可以在两个 Hadoop 版本中的 ORC 文件中利用 Zstandard 压缩。

```SQL
CREATE TABLE compressed (
  key STRING,
  value STRING
)
USING ORC
OPTIONS (
  compression 'zstd'
)
```

## Bloom Filters

> You can control bloom filters and dictionary encodings for ORC data sources. The following ORC example will create bloom filter and use dictionary encoding only for favorite_color. To find more detailed information about the extra ORC options, visit the official Apache ORC websites.

你可以控制 ORC 数据源的布隆过滤器和字典编码。

```SQL
CREATE TABLE users_with_options (
  name STRING,
  favorite_color STRING,
  favorite_numbers array<integer>
)
USING ORC
OPTIONS (
  orc.bloom.filter.columns 'favorite_color',
  orc.dictionary.key.threshold '1.0',
  orc.column.encoding.direct 'name'
)
```

## Columnar Encryption

> Since Spark 3.2, columnar encryption is supported for ORC tables with Apache ORC 1.6. The following example is using Hadoop KMS as a key provider with the given location. Please visit [Apache Hadoop KMS](https://hadoop.apache.org/docs/current/hadoop-kms/index.html) for the detail.

从 Spark 3.2 开始，Apache ORC 1.6 支持 ORC 表的列加密。

```SQL
CREATE TABLE encrypted (
  ssn STRING,
  email STRING,
  name STRING
)
USING ORC
OPTIONS (
  hadoop.security.key.provider.path "kms://http@localhost:9600/kms",
  orc.key.provider "hadoop",
  orc.encrypt "pii:ssn,email",
  orc.mask "nullify:ssn;sha256:email"
)
```

## Hive metastore ORC table conversion

> When reading from Hive metastore ORC tables and inserting to Hive metastore ORC tables, Spark SQL will try to use its own ORC support instead of Hive SerDe for better performance. For CTAS statement, only non-partitioned Hive metastore ORC tables are converted. This behavior is controlled by the `spark.sql.hive.convertMetastoreOrc` configuration, and is turned on by default.

当从 Hive 元数据库 ORC 表中读写数据时，为了更好的性能, Spark SQL将尝试使用自己的 ORC 支持，而不是使用 Hive SerDe.

对于 CTAS 语句，仅转换非分区 Hive 元数据库 ORC 表。这个行为由 `spark.sql.hive.convertMetastoreOrc` 参数控制，默认是打开的。

## Configuration

Property Name | Default | Meaning | Since Version
---|:---|:---|:---
`spark.sql.orc.impl` | `native` | The name of ORC implementation. It can be one of `native` and `hive`. `native` means the native ORC support. `hive` means the ORC library in Hive. 【选择其中一种ORC实现。`native`表示原生ORC支持，`hive`表示Hive中的ORC库】 | 2.3.0
`spark.sql.orc.enableVectorizedReader` | `true` | Enables vectorized orc decoding in `native` implementation. If `false`, a new non-vectorized ORC reader is used in `native` implementation. For `hive` implementation, this is ignored. 【在`native`实现中启用向量化orc编码。如果是false,那么在`native`实现中使用一个新的非向量化ORC阅读器。对于`hive`实现，这个被忽略】 | 2.3.0
`spark.sql.orc.enableNestedColumnVectorizedReader` | `false` | Enables vectorized orc decoding in `native` implementation for nested data types (array, map and struct). If `spark.sql.orc.enableVectorizedReader` is set to `false`, this is ignored. 【对于嵌套数据类型，在`native`实现中启用向量化orc编码。如果`spark.sql.orc.enableVectorizedReader`是false,那么这个被忽略】 | 3.2.0
`spark.sql.orc.mergeSchema` | `false` | When true, the ORC data source merges schemas collected from all data files, otherwise the schema is picked from a random data file. 【如果是true,orc数据源合并所有数据文件的结构。否则，就随机算一个数据文件的结构】 | 3.0.0
`spark.sql.hive.convertMetastoreOrc` | `true` | When set to `false`, Spark SQL will use the Hive SerDe for ORC tables instead of the built in support. 【如果设为false,Spark SQL使用Hive SerDe，而不是使用内建支持】 | 2.0.0

## Data Source Option

> Data source options of ORC can be set via:

- the `.option/.options` methods of
	- DataFrameReader
	- DataFrameWriter
	- DataStreamReader
	- DataStreamWriter

- OPTIONS clause at [`CREATE TABLE USING DATA_SOURCE`](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-ddl-create-table-datasource.html)

Property Name | Default | Meaning | Scope
---|:---|:---|:---
`mergeSchema` | `false`	| sets whether we should merge schemas collected from all ORC part-files. This will override `spark.sql.orc.mergeSchema`. The default value is specified in `spark.sql.orc.mergeSchema`. 【设置是否合并所有数据文件的结构，这将会覆盖`spark.sql.orc.mergeSchema`，默认值在`spark.sql.orc.mergeSchema`中指定】 | read
`compression` | `snappy` | compression codec to use when saving to file. This can be one of the known case-insensitive shorten names (none, snappy, zlib, lzo, zstd and lz4). This will override `orc.compress` and `spark.sql.orc.compression.codec`. 【当保存文件使用使用的压缩编码，大小写不敏感。这会覆盖`orc.compress` 和 `spark.sql.orc.compression.codec`】 | write

> Other generic options can be found in [Generic File Source Options](https://spark.apache.org/docs/latest/sql-data-sources-generic-options.html).