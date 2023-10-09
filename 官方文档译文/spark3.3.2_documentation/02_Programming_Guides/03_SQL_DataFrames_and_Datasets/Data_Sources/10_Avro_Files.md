# Apache Avro Data Source Guide

[TOC]

> Since Spark 2.4 release, Spark SQL provides built-in support for reading and writing Apache Avro data.

从 Spark 2.4 开始, Spark SQL 提供了读写 Apache Avro 数据的内建支持。

## Deploying

> The `spark-avro` module is external and not included in `spark-submit` or `spark-shell` by default.

`spark-avro` 模块是外部的，默认并不包含在 `spark-submit` 或 `spark-shell` 中。

> As with any Spark applications, `spark-submit` is used to launch your application. `spark-avro_2.12` and its dependencies can be directly added to `spark-submit` using `--packages`, such as,

`spark-avro_2.12` 和它的依赖可以直接使用 `--packages` 添加到 `spark-submit`

```
./bin/spark-submit --packages org.apache.spark:spark-avro_2.12:3.3.2 ...
```

> For experimenting on `spark-shell`, you can also use `--packages` to add `org.apache.spark:spark-avro_2.12` and its dependencies directly,

也可以使用 `--packages` ，将 `org.apache.spark:spark-avro_2.12` 和它的依赖添加到 `spark-shell`

```
./bin/spark-shell --packages org.apache.spark:spark-avro_2.12:3.3.2 ...
```

> See [Application Submission Guide](https://spark.apache.org/docs/3.3.2/submitting-applications.html) for more details about submitting applications with external dependencies.

## Load and Save Functions

> Since `spark-avro` module is external, there is no `.avro` API in `DataFrameReader` or `DataFrameWriter`.

因为`spark-avro` 模块是外部的，所以，在 `DataFrameReader` 或 `DataFrameWriter` 中，没有 `.avro` API.

> To load/save data in Avro format, you need to specify the data source option `format` as `avro`(or `org.apache.spark.sql.avro`).

所以，为了以 Avro 格式载入或保存数据，需要指定数据源选项 `format` 为 `avro` 或 `org.apache.spark.sql.avro`**

```scala
val usersDF = spark.read.format("avro").load("examples/src/main/resources/users.avro")
usersDF.select("name", "favorite_color").write.format("avro").save("namesAndFavColors.avro")
```

```java
Dataset<Row> usersDF = spark.read().format("avro").load("examples/src/main/resources/users.avro");
usersDF.select("name", "favorite_color").write().format("avro").save("namesAndFavColors.avro");
```

```python
df = spark.read.format("avro").load("examples/src/main/resources/users.avro")
df.select("name", "favorite_color").write.format("avro").save("namesAndFavColors.avro")
```

```r
df <- read.df("examples/src/main/resources/users.avro", "avro")
write.df(select(df, "name", "favorite_color"), "namesAndFavColors.avro", "avro")
```

## to_avro() and from_avro()

> The Avro package provides function `to_avro` to encode a column as binary in Avro format, and `from_avro()` to decode Avro binary data into a column. Both functions transform one column to another column, and the input/output SQL data type can be a complex type or a primitive type.

Avro 包提供了 `to_avro` 函数，将一列数据编码成 Avro 格式的二进制数据，`from_avro` 函数将二进制数据解码成一列数据。

这两个函数都是将一列转换成另一列。输入输出 SQL 数据类型可以是复杂类型，也可以是基本类型。

> Using Avro record as columns is useful when reading from or writing to a streaming source like Kafka. Each Kafka key-value record will be augmented with some metadata, such as the ingestion timestamp into Kafka, the offset in Kafka, etc.

当从 Kafka 这样的流数据源读写数据时，使用 Avro 记录作为列是非常有用的。

每个 Kafka 键值记录都将被添加一些元数据，比如进入 Kafka 的时间戳、在 Kafka 中的偏移量等等。

> If the “value” field that contains your data is in Avro, you could use `from_avro()` to extract your data, enrich it, clean it, and then push it downstream to Kafka again or write it out to a file.
> `to_avro()` can be used to turn structs into Avro records. This method is particularly useful when you would like to re-encode multiple columns into a single one when writing data out to Kafka.

- 如果包含数据的 value 字段在 Avro 中，你可以使用 `from_avro()` 抽取你的数据、丰富它、清理它、再把它推向 Kafka 的下游，或者写入到文件。

- `to_avro()` 可以把 structs 转成 Avro 记录。当你想把多列重新编码成一列，写入 Kafka 时，这个方法是有用的。

```scala
import org.apache.spark.sql.avro.functions._

// `from_avro` requires Avro schema in JSON string format.
val jsonFormatSchema = new String(Files.readAllBytes(Paths.get("./examples/src/main/resources/user.avsc")))

val df = spark
  .readStream
  .format("kafka")
  .option("kafka.bootstrap.servers", "host1:port1,host2:port2")
  .option("subscribe", "topic1")
  .load()

// 1. Decode the Avro data into a struct;
// 2. Filter by column `favorite_color`;
// 3. Encode the column `name` in Avro format.
val output = df
  .select(from_avro($"value", jsonFormatSchema) as $"user")
  .where("user.favorite_color == \"red\"")
  .select(to_avro($"user.name") as $"value")

val query = output
  .writeStream
  .format("kafka")
  .option("kafka.bootstrap.servers", "host1:port1,host2:port2")
  .option("topic", "topic2")
  .start()
```

```java
import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.avro.functions.*;

// `from_avro` requires Avro schema in JSON string format.
String jsonFormatSchema = new String(Files.readAllBytes(Paths.get("./examples/src/main/resources/user.avsc")));

Dataset<Row> df = spark
  .readStream()
  .format("kafka")
  .option("kafka.bootstrap.servers", "host1:port1,host2:port2")
  .option("subscribe", "topic1")
  .load();

// 1. Decode the Avro data into a struct;
// 2. Filter by column `favorite_color`;
// 3. Encode the column `name` in Avro format.
Dataset<Row> output = df
  .select(from_avro(col("value"), jsonFormatSchema).as("user"))
  .where("user.favorite_color == \"red\"")
  .select(to_avro(col("user.name")).as("value"));

StreamingQuery query = output
  .writeStream()
  .format("kafka")
  .option("kafka.bootstrap.servers", "host1:port1,host2:port2")
  .option("topic", "topic2")
  .start();
```

```python
from pyspark.sql.avro.functions import from_avro, to_avro

# `from_avro` requires Avro schema in JSON string format.
jsonFormatSchema = open("examples/src/main/resources/user.avsc", "r").read()

df = spark\
  .readStream\
  .format("kafka")\
  .option("kafka.bootstrap.servers", "host1:port1,host2:port2")\
  .option("subscribe", "topic1")\
  .load()

# 1. Decode the Avro data into a struct;
# 2. Filter by column `favorite_color`;
# 3. Encode the column `name` in Avro format.
output = df\
  .select(from_avro("value", jsonFormatSchema).alias("user"))\
  .where('user.favorite_color == "red"')\
  .select(to_avro("user.name").alias("value"))

query = output\
  .writeStream\
  .format("kafka")\
  .option("kafka.bootstrap.servers", "host1:port1,host2:port2")\
  .option("topic", "topic2")\
  .start()
```

```r
# `from_avro` requires Avro schema in JSON string format.
jsonFormatSchema <- paste0(readLines("examples/src/main/resources/user.avsc"), collapse=" ")

df <- read.stream(
  "kafka",
  kafka.bootstrap.servers = "host1:port1,host2:port2",
  subscribe = "topic1"
)

# 1. Decode the Avro data into a struct;
# 2. Filter by column `favorite_color`;
# 3. Encode the column `name` in Avro format.

output <- select(
  filter(
    select(df, alias(from_avro("value", jsonFormatSchema), "user")),
    column("user.favorite_color") == "red"
  ),
  alias(to_avro("user.name"), "value")
)

write.stream(
  output,
  "kafka",
  kafka.bootstrap.servers = "host1:port1,host2:port2",
  topic = "topic2"
)
```

## Data Source Option

> Data source options of Avro can be set via:

- the `.option` method on `DataFrameReader` or `DataFrameWriter`.
- the `options` parameter in function `from_avro`.

Property Name | Default | Meaning | Since Version
---|:---|:---|:---
`avroSchema` | None | Optional schema provided by a user in JSON format.【用户提供的JSON格式的可选结构】<br/> When reading Avro files or calling function `from_avro`, this option can be set to an evolved schema, which is compatible but different with the actual Avro schema. The deserialization schema will be consistent with the evolved schema. For example, if we set an evolved schema containing one additional column with a default value, the reading result in Spark will contain the new column too. Note that when using this option with `from_avro`, you still need to pass the actual Avro schema as a parameter to the function.【当读取Avro文件或调用from_avro函数时，可以设置这个选项，成为一个演化的结构，和实际的Avro结构兼容但又不同。反序列化结构将和演化结构一致。例如，如果我们设置了一个包含额外列的演化结构，spark中读取的结果也将包含新列。注意当这个选项和from_avro一起使用时，你仍需要给函数传递实际的avro结构作为参数。】<br/> When writing Avro, this option can be set if the expected output Avro schema doesn't match the schema converted by Spark. For example, the expected schema of one column is of "enum" type, instead of "string" type in the default converted schema.【当写入avro时，如果期待的输出avro结构和spark转换的结构不匹配，可以设置这个选项。】 | read, write and function `from_avro` | 2.4.0
`recordName` | topLevelRecord | Top level record name in write result, which is required in Avro spec.【在写结果中顶层的记录名，这在Avro规范中是必需的】 | write | 2.4.0
`recordNamespace` | "" | Record namespace in write result.【在写结果中记录名称空间】| write	| 2.4.0
`ignoreExtension` | true | The option controls ignoring of files without `.avro` extensions in read.
If the option is enabled, all files (with and without `.avro` extension) are loaded.
The option has been deprecated, and it will be removed in the future releases. Please use the general data source option [pathGlobFilter](https://spark.apache.org/docs/3.3.2/sql-data-sources-generic-options.html#path-global-filter) for filtering file names.【这个选项控制着，在读数据时，是否忽略没有`.avro`后缀的文件。如果启用了，不管有没有这个后缀，所有文件都被载入。这个选项将在未来版本中移出。请使用通用数据源选项pathGlobFilter来过滤文件名。】 | read | 2.4.0
`compression` | snappy | The `compression` option allows to specify a compression codec used in write.
Currently supported codecs are `uncompressed`, `snappy`, `deflate`, `bzip2`, `xz` and `zstandard`.
If the option is not set, the configuration `spark.sql.avro.compression.codec` config is taken into account.【在写数据时，使用的压缩格式。如果没有设置这个选项，那么就会考虑使用 `spark.sql.avro.compression.codec` 这个配置】 | write | 2.4.0
`mode` | `FAILFAST` | The `mode` option allows to specify parse mode for function `from_avro`.Currently supported modes are:【为from_avro函数指定解析模式】 `FAILFAST`: Throws an exception on processing corrupted record.【在处理损坏记录时，抛异常】<br/> `PERMISSIVE`: Corrupt records are processed as null result.【损坏的记录作为null值处理】 <br/> Therefore, the data schema is forced to be fully nullable, which might be different from the one user provided.【因此，数据结构被强制成为可为null的，这可能会和用户提供的不同】| function `from_avro`  |	2.4.0
`datetimeRebaseMode` | (value of `spark.sql.avro.datetimeRebaseModeInRead` configuration) |	 The `datetimeRebaseMode` option allows to specify the rebasing mode for the values of the `date`, `timestamp-micros`, `timestamp-millis` logical types from the Julian to Proleptic Gregorian calendar.
Currently supported modes are: <br/>`EXCEPTION`: fails in reads of ancient dates/timestamps that are ambiguous between the two calendars. <br/>`CORRECTED`: loads dates/timestamps without rebasing. <br/>`LEGACY`: performs rebasing of ancient dates/timestamps from the Julian to Proleptic Gregorian calendar. | read and function from_avro	|	3.2.0
`positionalFieldMatching` | `false`	| This can be used in tandem with the `avroSchema` option to adjust the behavior for matching the fields in the provided Avro schema with those in the SQL schema. By default, the matching will be performed using field names, ignoring their positions. If this option is set to "true", the matching will be based on the position of the fields. 【这个选项可以和avroSchema选项串联使用，来调整提供的Avro结构和sql结构匹配字段的行为。默认使用字段名来匹配，忽略位置。如果设为true，将基于位置匹配】| read and write | 3.2.0

## Configuration

> Configuration of Avro can be done using the `setConf` method on `SparkSession` or by running `SET key=value` commands using SQL.

Avro 的配置可以使用 `SparkSession` 中的 `setConf` 方法或在 SQL 中运行 `SET key=value` 命令。

Property Name | Default | Meaning | Since Version
---|:---|:---|:---
`spark.sql.legacy.replaceDatabricksSparkAvro.enabled` | `true` | If it is set to true, the data source provider `com.databricks.spark.avro` is mapped to the built-in but external Avro data source module for backward compatibility.【如果设为true,将使用内建但是外部的avro数据源模块支持替换`com.databricks.spark.avro`。】<br/> Note: the SQL config has been deprecated in Spark 3.2 and might be removed in the future. | 2.4.0
`spark.sql.avro.compression.codec` | `snappy` | Compression codec used in writing of AVRO files. Supported codecs: uncompressed, deflate, snappy, bzip2 and xz. Default codec is snappy. 【在AVRO文件写入中使用的压缩格式】| 2.4.0
`spark.sql.avro.deflate.level` | -1 | Compression level for the deflate codec used in writing of AVRO files. Valid value must be in the range of from 1 to 9 inclusive or -1. The default value is -1 which corresponds to 6 level in the current implementation.【在AVRO文件写入中使用的deflate压缩格式的压缩级别。有效值必须是[1,9),或-1。默认值是-1，对应着当前实现中的6级】 | 2.4.0
`spark.sql.avro.datetimeRebaseModeInRead` | `EXCEPTION`	 | The rebasing mode for the values of the `date`, `timestamp-micros`, `timestamp-millis` logical types from the Julian to Proleptic Gregorian calendar: <br/> `EXCEPTION`: Spark will fail the reading if it sees ancient dates/timestamps that are ambiguous between the two calendars. <br/>`CORRECTED`: Spark will not do rebase and read the dates/timestamps as it is. <br/>`LEGACY`: Spark will rebase dates/timestamps from the legacy hybrid (Julian + Gregorian) calendar to Proleptic Gregorian calendar when reading Avro files. <br/>This config is only effective if the writer info (like Spark, Hive) of the Avro files is unknown. | 3.0.0
`spark.sql.avro.datetimeRebaseModeInWrite` | `EXCEPTION` | The rebasing mode for the values of the `date`, `timestamp-micros`, `timestamp-millis` logical types from the Proleptic Gregorian to Julian calendar:<br/> `EXCEPTION`: Spark will fail the writing if it sees ancient dates/timestamps that are ambiguous between the two calendars.<br/> `CORRECTED`: Spark will not do rebase and write the dates/timestamps as it is.<br/> `LEGACY`: Spark will rebase dates/timestamps from Proleptic Gregorian calendar to the legacy hybrid (Julian + Gregorian) calendar when writing Avro files. | 3.0.0

## Compatibility with Databricks spark-avro

> This Avro data source module is originally from and compatible with Databricks’s open source repository [spark-avro](https://github.com/databricks/spark-avro).

此 Avro 数据源模块来源于、且兼容 Databricks 的 spark-avro.

> By default with the SQL configuration `spark.sql.legacy.replaceDatabricksSparkAvro.enabled` enabled, the data source provider `com.databricks.spark.avro` is mapped to this built-in Avro module. For the Spark tables created with `Provider` property as `com.databricks.spark.avro` in catalog meta store, the mapping is essential to load these tables if you are using this built-in Avro module.

默认情况下，`spark.sql.legacy.replaceDatabricksSparkAvro.enabled` 是启用的，数据源提供者 `com.databricks.spark.avro ` 映射到内置的 Avro 模块。

对于使用 `com.databricks.spark.avro` 属性创建的 Spark 表，如果你使用这个内置的 Avro 模块，映射对于载入这些表是必要的。

> Note in Databricks’s [spark-avro](https://github.com/databricks/spark-avro), implicit classes `AvroDataFrameWriter` and `AvroDataFrameReader` were created for shortcut function `.avro()`. In this built-in but external module, both implicit classes are removed. Please use `.format("avro")` in `DataFrameWriter` or `DataFrameReader` instead, which should be clean and good enough.

在 Databricks 的 spark-avro 中，为快捷函数 `.avro()`, 创建隐式类 `DataFrameWriter` 或 `DataFrameReader`.

对于内建的、外部的模块，这两个隐式类都被移除。请在 `DataFrameWriter` 或 `DataFrameReader` 中使用 `.format("avro")`.

> If you prefer using your own build of `spark-avro` jar file, you can simply disable the configuration `spark.sql.legacy.replaceDatabricksSparkAvro.enabled`, and use the option `--jars` on deploying your applications. Read the [Advanced Dependency Management](https://spark.apache.org/docs/latest/submitting-applications.html#advanced-dependency-management) section in Application Submission Guide for more details.

如果你想使用自己构建的 `spark-avro` jar 文件，你可以禁用 `spark.sql.legacy.replaceDatabricksSparkAvro.enabled`, 使用 `--jars` 选项来部署你的应用程序。

## Supported types for Avro -> Spark SQL conversion

> Currently Spark supports reading all [primitive types](https://avro.apache.org/docs/1.11.0/spec.html#schema_primitive) and [complex types](https://avro.apache.org/docs/1.11.0/spec.html#schema_complex) under records of Avro.

Avro type   |   Spark SQL type
---|:---
boolean     |   BooleanType
int         |   IntegerType
long        |   LongType
float       |   FloatType
double      |   DoubleType
string      |   StringType
enum        |   StringType
fixed       |   BinaryType
bytes       |   BinaryType
record      |   StructType
array       |   ArrayType
map         |   MapType
union       |   See below

> In addition to the types listed above, it supports reading union types. The following three types are considered basic union types:

除了上述列的类型，还支持读取 `union` 类型：

- `union(int, long)` 被映射到 LongType
- `union(float, double)` 被映射到 DoubleType
- `union(something, null)`, something是任意支持的 Avro 类型，这将被映射到相同的 Spark SQL 类型，nullable 设为 true。所有其他的 union 类型都被当作复杂类型。它们将被映射到 StructType, 其字段名是 member0\member1...。

> `union(int, long)` will be mapped to LongType.
> `union(float, double)` will be mapped to DoubleType.
> `union(something, null)`, where something is any supported Avro type. This will be mapped to the same Spark SQL type as that of something, with nullable set to true. All other union types are considered complex. They will be mapped to StructType where field names are member0, member1, etc., in accordance with members of the union. This is consistent with the behavior when converting between Avro and Parquet.

> It also supports reading the following Avro [logical types](https://avro.apache.org/docs/1.11.0/spec.html#Logical+Types):

也支持下列的 Avro 逻辑类型：

Avro logical type | Avro type | Spark SQL type
---|:---|:---
date | int | DateType
timestamp-millis | long | TimestampType
timestamp-micros | long | TimestampType
decimal | fixed | DecimalType
decimal | bytes | DecimalType

> At the moment, it ignores docs, aliases and other properties present in the Avro file.

在 Avro 文件中，会忽略 docs、别名和其他属性表示。

## Supported types for Spark SQL -> Avro conversion

> Spark supports writing of all Spark SQL types into Avro. For most types, the mapping from Spark types to Avro types is straightforward (e.g. IntegerType gets converted to int); however, there are a few special cases which are listed below:

Spark 支持所有 Spark SQL 类型写入 Avro. 对于大多数类型，Spark 类型到 Avro 类型的映射是直接的(如，IntegerType转换成int)。

然而，也有下列几种特殊情况：

Spark SQL type | Avro type | Avro logical type
---|:---|:---
ByteType | int | 	
ShortType | int | 	
BinaryType | bytes | 	
DateType | int | date
TimestampType | long | timestamp-micros
DecimalType | fixed | decimal

> You can also specify the whole output Avro schema with the option `avroSchema`, so that Spark SQL types can be converted into other Avro types. The following conversions are not applied by default and require user specified Avro schema:

你也可以使用选项 `avroSchema` 指定整个输出的 Avro 结构，为了 Spark SQL 类型能转换成其他 Avro 类型。下面的转换默认是不允许的，除非用户指定 Avro schema：

Spark SQL type | Avro type | Avro logical type
---|:---|:---
BinaryType | fixed | 	
StringType | enum | 	
TimestampType | long | timestamp-millis
DecimalType | bytes | decimal