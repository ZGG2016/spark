# Generic Load/Save Functions

[TOC]

> In the simplest form, the default data source (parquet unless otherwise configured by spark.sql.sources.default) will be used for all operations.

所有操作默认使用的数据源是 parquet, 除非配置 `spark.sql.sources.default`

```scala
val usersDF = spark.read.load("examples/src/main/resources/users.parquet")
usersDF.select("name", "favorite_color").write.save("namesAndFavColors.parquet")
```

> Find full example code at "examples/src/main/scala/org/apache/spark/examples/sql/SQLDataSourceExample.scala" in the Spark repo.

```java
Dataset<Row> usersDF = spark.read().load("examples/src/main/resources/users.parquet");
usersDF.select("name", "favorite_color").write().save("namesAndFavColors.parquet");
```

> Find full example code at "examples/src/main/java/org/apache/spark/examples/sql/JavaSQLDataSourceExample.java" in the Spark repo.

```python
df = spark.read.load("examples/src/main/resources/users.parquet")
df.select("name", "favorite_color").write.save("namesAndFavColors.parquet")
```

> Find full example code at "examples/src/main/python/sql/datasource.py" in the Spark repo.

```r
df <- read.df("examples/src/main/resources/users.parquet")
write.df(select(df, "name", "favorite_color"), "namesAndFavColors.parquet")
```

> Find full example code at "examples/src/main/r/RSparkSQLExample.R" in the Spark repo.

## Manually Specifying Options

> You can also manually specify the data source that will be used along with any extra options that you would like to pass to the data source. Data sources are specified by their fully qualified name (i.e., `org.apache.spark.sql.parquet`), but for built-in sources you can also use their short names (`json`, `parquet`, `jdbc`, `orc`, `libsvm`, `csv`, `text`). DataFrames loaded from any data source type can be converted into other types using this syntax.

可以通过额外的选项(format)手动指定数据源类型，但是要使用完整的限定名，如 `org.apache.spark.sql.parquet`。

对于内置源，可以使用简单的名字，如json`, `parquet`, `jdbc`, `orc`, `libsvm`, `csv`, `text`.

> Please refer the API documentation for available options of built-in sources, for example, `org.apache.spark.sql.DataFrameReader` and `org.apache.spark.sql.DataFrameWriter`. The options documented there should be applicable through non-Scala Spark APIs (e.g. PySpark) as well. For other formats, refer to the API documentation of the particular format.

内置源的可选项请见 API. 例如，`org.apache.spark.sql.DataFrameReader` 和 `org.apache.spark.sql.DataFrameWriter` 。

可选项也应当能用在非 Scala 的 Spark APIs, 例如 PySpark.

> To load a JSON file you can use:

载入 JSON 文件：

```scala
val peopleDF = spark.read.format("json").load("examples/src/main/resources/people.json")
peopleDF.select("name", "age").write.format("parquet").save("namesAndAges.parquet")
```

> Find full example code at "examples/src/main/scala/org/apache/spark/examples/sql/SQLDataSourceExample.scala" in the Spark repo.

```java
Dataset<Row> peopleDF =
  spark.read().format("json").load("examples/src/main/resources/people.json");
peopleDF.select("name", "age").write().format("parquet").save("namesAndAges.parquet");
```

> Find full example code at "examples/src/main/java/org/apache/spark/examples/sql/JavaSQLDataSourceExample.java" in the Spark repo.

```python
df = spark.read.load("examples/src/main/resources/people.json", format="json")
df.select("name", "age").write.save("namesAndAges.parquet", format="parquet")
```

> Find full example code at "examples/src/main/python/sql/datasource.py" in the Spark repo.

```r
df <- read.df("examples/src/main/resources/people.json", "json")
namesAndAges <- select(df, "name", "age")
write.df(namesAndAges, "namesAndAges.parquet", "parquet")
```

> Find full example code at "examples/src/main/r/RSparkSQLExample.R" in the Spark repo.

> To load a CSV file you can use:

载入 CSV 文件：

```scala
val peopleDFCsv = spark.read.format("csv")
  .option("sep", ";")
  .option("inferSchema", "true")
  .option("header", "true")
  .load("examples/src/main/resources/people.csv")
```

> Find full example code at "examples/src/main/scala/org/apache/spark/examples/sql/SQLDataSourceExample.scala" in the Spark repo.

```java
Dataset<Row> peopleDFCsv = spark.read().format("csv")
  .option("sep", ";")
  .option("inferSchema", "true")
  .option("header", "true")
  .load("examples/src/main/resources/people.csv");
```

> Find full example code at "examples/src/main/java/org/apache/spark/examples/sql/JavaSQLDataSourceExample.java" in the Spark repo.

```python
df = spark.read.load("examples/src/main/resources/people.csv",
                     format="csv", sep=";", inferSchema="true", header="true")
```

> Find full example code at "examples/src/main/python/sql/datasource.py" in the Spark repo.

```r
df <- read.df("examples/src/main/resources/people.csv", "csv", sep = ";", inferSchema = TRUE, header = TRUE)
namesAndAges <- select(df, "name", "age")
```

> Find full example code at "examples/src/main/r/RSparkSQLExample.R" in the Spark repo.

> The extra options are also used during write operation. For example, you can control bloom filters and dictionary encodings for ORC data sources. The following ORC example will create bloom filter and use dictionary encoding only for `favorite_color`. For Parquet, there exists `parquet.bloom.filter.enabled` and `parquet.enable.dictionary`, too. To find more detailed information about the extra ORC/Parquet options, visit the official Apache [ORC](https://orc.apache.org/docs/spark-config.html) / [Parquet](https://github.com/apache/parquet-mr/tree/master/parquet-hadoop) websites.

写操作也有一些其他可选项，如，控制 ORC 数据源的 bloom filters 和 dictionary encodings。

对于 Parquet, 也有 `parquet.bloom.filter.enabled` 和 `parquet.enable.dictionary`. 关于额外的 ORC/Parquet 选项，访问官方 Apache ORC / Parquet 网站。

> ORC data source:

ORC 数据源

```scala
usersDF.write.format("orc")
  .option("orc.bloom.filter.columns", "favorite_color")
  .option("orc.dictionary.key.threshold", "1.0")
  .option("orc.column.encoding.direct", "name")
  .save("users_with_options.orc")
```

> Find full example code at "examples/src/main/scala/org/apache/spark/examples/sql/SQLDataSourceExample.scala" in the Spark repo.

```java
usersDF.write().format("orc")
  .option("orc.bloom.filter.columns", "favorite_color")
  .option("orc.dictionary.key.threshold", "1.0")
  .option("orc.column.encoding.direct", "name")
  .save("users_with_options.orc");
```

> Find full example code at "examples/src/main/java/org/apache/spark/examples/sql/JavaSQLDataSourceExample.java" in the Spark repo.

```python
df = spark.read.orc("examples/src/main/resources/users.orc")
(df.write.format("orc")
    .option("orc.bloom.filter.columns", "favorite_color")
    .option("orc.dictionary.key.threshold", "1.0")
    .option("orc.column.encoding.direct", "name")
    .save("users_with_options.orc"))
```

> Find full example code at "examples/src/main/python/sql/datasource.py" in the Spark repo.

```r
df <- read.df("examples/src/main/resources/users.orc", "orc")
write.orc(df, "users_with_options.orc", orc.bloom.filter.columns = "favorite_color", orc.dictionary.key.threshold = 1.0, orc.column.encoding.direct = "name")
```

> Find full example code at "examples/src/main/r/RSparkSQLExample.R" in the Spark repo.

```sql
CREATE TABLE users_with_options (
  name STRING,
  favorite_color STRING,
  favorite_numbers array<integer>
) USING ORC
OPTIONS (
  orc.bloom.filter.columns 'favorite_color',
  orc.dictionary.key.threshold '1.0',
  orc.column.encoding.direct 'name'
)
```

> Parquet data source:

Parquet 数据源

```scala
usersDF.write.format("parquet")
  .option("parquet.bloom.filter.enabled#favorite_color", "true")
  .option("parquet.bloom.filter.expected.ndv#favorite_color", "1000000")
  .option("parquet.enable.dictionary", "true")
  .option("parquet.page.write-checksum.enabled", "false")
  .save("users_with_options.parquet")
```

> Find full example code at "examples/src/main/scala/org/apache/spark/examples/sql/SQLDataSourceExample.scala" in the Spark repo.

```java
usersDF.write().format("parquet")
    .option("parquet.bloom.filter.enabled#favorite_color", "true")
    .option("parquet.bloom.filter.expected.ndv#favorite_color", "1000000")
    .option("parquet.enable.dictionary", "true")
    .option("parquet.page.write-checksum.enabled", "false")
    .save("users_with_options.parquet");
```

> Find full example code at "examples/src/main/java/org/apache/spark/examples/sql/JavaSQLDataSourceExample.java" in the Spark repo.

```python
df = spark.read.parquet("examples/src/main/resources/users.parquet")
(df.write.format("parquet")
    .option("parquet.bloom.filter.enabled#favorite_color", "true")
    .option("parquet.bloom.filter.expected.ndv#favorite_color", "1000000")
    .option("parquet.enable.dictionary", "true")
    .option("parquet.page.write-checksum.enabled", "false")
    .save("users_with_options.parquet"))
```

> Find full example code at "examples/src/main/python/sql/datasource.py" in the Spark repo.

```r
df <- read.df("examples/src/main/resources/users.parquet", "parquet")
write.parquet(df, "users_with_options.parquet", parquet.bloom.filter.enabled#favorite_color = true, parquet.bloom.filter.expected.ndv#favorite_color = 1000000, parquet.enable.dictionary = true, parquet.page.write-checksum.enabled = false)
```

> Find full example code at "examples/src/main/r/RSparkSQLExample.R" in the Spark repo.

```sql
CREATE TABLE users_with_options (
  name STRING,
  favorite_color STRING,
  favorite_numbers array<integer>
) USING parquet
OPTIONS (
  `parquet.bloom.filter.enabled#favorite_color` true,
  `parquet.bloom.filter.expected.ndv#favorite_color` 1000000,
  parquet.enable.dictionary true,
  parquet.page.write-checksum.enabled true
)
```

## Run SQL on files directly

> Instead of using read API to load a file into DataFrame and query it, you can also query that file directly with SQL.

除了使用 read 方法将文件载入到 DataFrame, 并查询它。也可以直接在文件上运行 SQL.

```scala
val sqlDF = spark.sql("SELECT * FROM parquet.`examples/src/main/resources/users.parquet`")
```

> Find full example code at "examples/src/main/scala/org/apache/spark/examples/sql/SQLDataSourceExample.scala" in the Spark repo.

```java
Dataset<Row> sqlDF =
  spark.sql("SELECT * FROM parquet.`examples/src/main/resources/users.parquet`");
```

> Find full example code at "examples/src/main/java/org/apache/spark/examples/sql/JavaSQLDataSourceExample.java" in the Spark repo.

```python
df = spark.sql("SELECT * FROM parquet.`examples/src/main/resources/users.parquet`")
```

> Find full example code at "examples/src/main/python/sql/datasource.py" in the Spark repo.

```r
df <- sql("SELECT * FROM parquet.`examples/src/main/resources/users.parquet`")
```

> Find full example code at "examples/src/main/r/RSparkSQLExample.R" in the Spark repo.

## Save Modes

> Save operations can optionally take a `SaveMode`, that specifies how to handle existing data if present. It is important to realize that these save modes do not utilize any locking and are not atomic. Additionally, when performing an `Overwrite`, the data will be deleted before writing out the new data.

存储操作可选地接收一个 `SaveMode` 选项，指定了如果数据已存在，如何处理数据。

这个选项不使用任何锁，并且不是原子的。另外，当执行 `Overwrite` 时，数据将在新数据写出之前被删除。

Scala/Java | Any Language | Meaning
---|:---|:---
SaveMode.ErrorIfExists (default)  |  "error" or "errorifexists" (default)  | When saving a DataFrame to a data source, if data already exists, an exception is expected to be thrown.【存储的时候，如果数据已存在，就抛异常。】
SaveMode.Append | "append"  | When saving a DataFrame to a data source, if data/table already exists, contents of the DataFrame are expected to be appended to existing data.【存储的时候，如果数据已存在，就追加。】
SaveMode.Overwrite |  "overwrite"  | Overwrite mode means that when saving a DataFrame to a data source, if data/table already exists, existing data is expected to be overwritten by the contents of the DataFrame.【存储的时候，如果数据已存在，就覆盖。】
SaveMode.Ignore | "ignore"  | Ignore mode means that when saving a DataFrame to a data source, if data already exists, the save operation is expected not to save the contents of the DataFrame and not to change the existing data. This is similar to a `CREATE TABLE IF NOT EXISTS` in SQL.【存储的时候，如果数据已存在，就不再往里写数据，已存在的数据保存不变。】

## Saving to Persistent Tables

> `DataFrames` can also be saved as persistent tables into Hive metastore using the `saveAsTable` command. Notice that an existing Hive deployment is not necessary to use this feature. Spark will create a default local Hive metastore (using Derby) for you. Unlike the `createOrReplaceTempView` command, `saveAsTable` will materialize the contents of the DataFrame and create a pointer to the data in the Hive metastore. Persistent tables will still exist even after your Spark program has restarted, as long as you maintain your connection to the same metastore. A DataFrame for a persistent table can be created by calling the `table` method on a `SparkSession` with the name of the table.

DataFrames 可以使用 `saveAsTable` 命令以持久表的形式存入 Hive 元数据库。此特性不需要先部署 Hive.Spark 会创建一个默认的本地 Hive 元数据库(使用 Derby)。

和 `createOrReplaceTempView` 命令不同, `saveAsTable` 会物化 DataFrame 的内容，创建一个指向 Hive 元数据库的指针。

只要维持和元数据库的连接，即使 Spark 程序重启后，持久表也会存在。 

可以通过在 `SparkSession` 上调用 `table` 方法（使用表名称）来创建持久表的 DataFrame.

> For file-based data source, e.g. text, parquet, json, etc. you can specify a custom table path via the `path` option, e.g. `df.write.option("path", "/some/path").saveAsTable("t")`. When the table is dropped, the custom table path will not be removed and the table data is still there. If no custom table path is specified, Spark will write data to a default table path under the warehouse directory. When the table is dropped, the default table path will be removed too.

对于 text\parquet\json 等的基于文件的数据源，可以通过 `path` 参数指定自定义表的路径，如 `df.write.option("path", "/some/path").saveAsTable("t")` 。

当表被删除时，这个自定义路径和表数据仍旧会存在。

如果不指定一个路径，Spark 会写入数据到仓库目录下的默认表路径，当表删除后，此默认表路径也会被删除。

> Starting from Spark 2.1, persistent datasource tables have per-partition metadata stored in the Hive metastore. This brings several benefits:

从 Spark 2.1 开始，持久性数据源表将每个分区的元数据存储在 Hive 元数据库中。这带来了几个好处:

> Since the metastore can return only necessary partitions for a query, discovering all the partitions on the first query to the table is no longer needed.

- 由于元数据库只能返回查询所需的分区，因此不再需要第一个查询语句就返回所有分区。

> Hive DDLs such as `ALTER TABLE PARTITION ... SET LOCATION` are now available for tables created with the Datasource API.

- 像 `ALTER TABLE PARTITION ... SET LOCATION` 的 Hive DDLs 现在可用于使用 Datasource API 创建的表。

> Note that partition information is not gathered by default when creating external datasource tables (those with a `path` option). To sync the partition information in the metastore, you can invoke `MSCK REPAIR TABLE`.

注意，当创建外部数据源表（带有 `path` 选项）时，默认情况下不会收集分区信息。要同步元数据库中的分区信息，可以调用 `MSCK REPAIR TABLE`.

## Bucketing, Sorting and Partitioning

> For file-based data source, it is also possible to bucket and sort or partition the output. Bucketing and sorting are applicable only to persistent tables:

对于基于文件的数据源，可以对输出结果进行分桶、排序、分区。分桶和排序只能用在持久表。

```scala
peopleDF.write.bucketBy(42, "name").sortBy("age").saveAsTable("people_bucketed")
```
> Find full example code at "examples/src/main/scala/org/apache/spark/examples/sql/SQLDataSourceExample.scala" in the Spark repo.

```java
peopleDF.write().bucketBy(42, "name").sortBy("age").saveAsTable("people_bucketed");
```
> Find full example code at "examples/src/main/java/org/apache/spark/examples/sql/JavaSQLDataSourceExample.java" in the Spark repo.

```python
df.write.bucketBy(42, "name").sortBy("age").saveAsTable("people_bucketed")
```
> Find full example code at "examples/src/main/python/sql/datasource.py" in the Spark repo.

```sql
CREATE TABLE users_bucketed_by_name(
  name STRING,
  favorite_color STRING,
  favorite_numbers array<integer>
) USING parquet
CLUSTERED BY(name) INTO 42 BUCKETS;
```

> while partitioning can be used with both `save` and `saveAsTable` when using the Dataset APIs.

当使用 Dataset APIs 时，分区也可以用于 save 和 saveAsTable 

```scala
usersDF.write.partitionBy("favorite_color").format("parquet").save("namesPartByColor.parquet")
```
> Find full example code at "examples/src/main/scala/org/apache/spark/examples/sql/SQLDataSourceExample.scala" in the Spark repo.

```java
usersDF
  .write()
  .partitionBy("favorite_color")
  .format("parquet")
  .save("namesPartByColor.parquet");
```
> Find full example code at "examples/src/main/java/org/apache/spark/examples/sql/JavaSQLDataSourceExample.java" in the Spark repo.

```python
df.write.partitionBy("favorite_color").format("parquet").save("namesPartByColor.parquet")
```
> Find full example code at "examples/src/main/python/sql/datasource.py" in the Spark repo.

```sql
CREATE TABLE users_by_favorite_color(
  name STRING,
  favorite_color STRING,
  favorite_numbers array<integer>
) USING csv PARTITIONED BY(favorite_color);
```

> It is possible to use both partitioning and bucketing for a single table:

在一个表上，可以同时分区和分桶

```scala
usersDF
  .write
  .partitionBy("favorite_color")
  .bucketBy(42, "name")
  .saveAsTable("users_partitioned_bucketed")
```
> Find full example code at "examples/src/main/scala/org/apache/spark/examples/sql/SQLDataSourceExample.scala" in the Spark repo.

```java
usersDF
  .write()
  .partitionBy("favorite_color")
  .bucketBy(42, "name")
  .saveAsTable("users_partitioned_bucketed");
```
> Find full example code at "examples/src/main/java/org/apache/spark/examples/sql/JavaSQLDataSourceExample.java" in the Spark repo.

```python
df = spark.read.parquet("examples/src/main/resources/users.parquet")
(df
    .write
    .partitionBy("favorite_color")
    .bucketBy(42, "name")
    .saveAsTable("users_partitioned_bucketed"))
```
> Find full example code at "examples/src/main/python/sql/datasource.py" in the Spark repo.

```sql
CREATE TABLE users_bucketed_and_partitioned(
  name STRING,
  favorite_color STRING,
  favorite_numbers array<integer>
) USING parquet
PARTITIONED BY (favorite_color)
CLUSTERED BY(name) SORTED BY (favorite_numbers) INTO 42 BUCKETS;
```

> `partitionBy` creates a directory structure as described in the [Partition Discovery](https://spark.apache.org/docs/3.3.2/sql-data-sources-parquet.html#partition-discovery) section. Thus, it has limited applicability to columns with high cardinality. In contrast `bucketBy` distributes data across a fixed number of buckets and can be used when the number of unique values is unbounded.

`partitionBy` 会创建一个目录结构。因此，对基数较高的列的适用性有限。

相反, `bucketBy` 可以在固定数量的桶中分发数据，并且当唯一值的数量是无限时使用。