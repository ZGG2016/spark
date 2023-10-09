# Parquet Files

[TOC]

> [Parquet](https://parquet.apache.org/) is a columnar format that is supported by many other data processing systems. Spark SQL provides support for both reading and writing Parquet files that automatically preserves the schema of the original data. When reading Parquet files, all columns are automatically converted to be nullable for compatibility reasons.

Parquet 是一种柱状格式，很多数据处理系统都支持此种格式。

Spark SQL 支持读写 Parquet 文件，可自动保存原始数据的结构。当读取 Parquet 文件时，出于兼容性原因，所有列都将自动转换为可为空的。

## Loading Data Programmatically

> Using the data from the above example:

```scala
// Encoders for most common types are automatically provided by importing spark.implicits._
import spark.implicits._

val peopleDF = spark.read.json("examples/src/main/resources/people.json")

// DataFrames can be saved as Parquet files, maintaining the schema information
peopleDF.write.parquet("people.parquet")

// Read in the parquet file created above
// Parquet files are self-describing so the schema is preserved
// The result of loading a Parquet file is also a DataFrame
val parquetFileDF = spark.read.parquet("people.parquet")

// Parquet files can also be used to create a temporary view and then used in SQL statements
parquetFileDF.createOrReplaceTempView("parquetFile")
val namesDF = spark.sql("SELECT name FROM parquetFile WHERE age BETWEEN 13 AND 19")
namesDF.map(attributes => "Name: " + attributes(0)).show()
// +------------+
// |       value|
// +------------+
// |Name: Justin|
// +------------+
```
> Find full example code at "examples/src/main/scala/org/apache/spark/examples/sql/SQLDataSourceExample.scala" in the Spark repo.

```java
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

Dataset<Row> peopleDF = spark.read().json("examples/src/main/resources/people.json");

// DataFrames can be saved as Parquet files, maintaining the schema information
peopleDF.write().parquet("people.parquet");

// Read in the Parquet file created above.
// Parquet files are self-describing so the schema is preserved
// The result of loading a parquet file is also a DataFrame
Dataset<Row> parquetFileDF = spark.read().parquet("people.parquet");

// Parquet files can also be used to create a temporary view and then used in SQL statements
parquetFileDF.createOrReplaceTempView("parquetFile");
Dataset<Row> namesDF = spark.sql("SELECT name FROM parquetFile WHERE age BETWEEN 13 AND 19");
Dataset<String> namesDS = namesDF.map(
    (MapFunction<Row, String>) row -> "Name: " + row.getString(0),
    Encoders.STRING());
namesDS.show();
// +------------+
// |       value|
// +------------+
// |Name: Justin|
// +------------+
```
> Find full example code at "examples/src/main/java/org/apache/spark/examples/sql/JavaSQLDataSourceExample.java" in the Spark repo.

```python
peopleDF = spark.read.json("examples/src/main/resources/people.json")

# DataFrames can be saved as Parquet files, maintaining the schema information.
peopleDF.write.parquet("people.parquet")

# Read in the Parquet file created above.
# Parquet files are self-describing so the schema is preserved.
# The result of loading a parquet file is also a DataFrame.
parquetFile = spark.read.parquet("people.parquet")

# Parquet files can also be used to create a temporary view and then used in SQL statements.
parquetFile.createOrReplaceTempView("parquetFile")
teenagers = spark.sql("SELECT name FROM parquetFile WHERE age >= 13 AND age <= 19")
teenagers.show()
# +------+
# |  name|
# +------+
# |Justin|
# +------+
```
> Find full example code at "examples/src/main/python/sql/datasource.py" in the Spark repo.

```r
df <- read.df("examples/src/main/resources/people.json", "json")

# SparkDataFrame can be saved as Parquet files, maintaining the schema information.
write.parquet(df, "people.parquet")

# Read in the Parquet file created above. Parquet files are self-describing so the schema is preserved.
# The result of loading a parquet file is also a DataFrame.
parquetFile <- read.parquet("people.parquet")

# Parquet files can also be used to create a temporary view and then used in SQL statements.
createOrReplaceTempView(parquetFile, "parquetFile")
teenagers <- sql("SELECT name FROM parquetFile WHERE age >= 13 AND age <= 19")
head(teenagers)
##     name
## 1 Justin

# We can also run custom R-UDFs on Spark DataFrames. Here we prefix all the names with "Name:"
schema <- structType(structField("name", "string"))
teenNames <- dapply(df, function(p) { cbind(paste("Name:", p$name)) }, schema)
for (teenName in collect(teenNames)$name) {
  cat(teenName, "\n")
}
## Name: Michael
## Name: Andy
## Name: Justin
```
> Find full example code at "examples/src/main/r/RSparkSQLExample.R" in the Spark repo.

```sql
CREATE TEMPORARY VIEW parquetTable
USING org.apache.spark.sql.parquet
OPTIONS (
  path "examples/src/main/resources/people.parquet"
)

SELECT * FROM parquetTable
```

## Partition Discovery

> Table partitioning is a common optimization approach used in systems like Hive. In a partitioned table, data are usually stored in different directories, with partitioning column values encoded in the path of each partition directory. All built-in file sources (including Text/CSV/JSON/ORC/Parquet) are able to discover and infer partitioning information automatically. For example, we can store all our previously used population data into a partitioned table using the following directory structure, with two extra columns, gender and country as partitioning columns:

表分区是一种常见的优化方法。

在分区表中，数据存储在不同的目录中，分区的列值被编码成每个分区的路径。

所有内置的文件数据源(including Text/CSV/JSON/ORC/Parquet)都能自动发现和推断分区信息。

例如，我们可以使用以下目录结构将所有以前使用的人口数据存储到分区表中，其中有两个额外的列 gender 和 country 作为分区列:

```
path
└── to
    └── table
        ├── gender=male
        │   ├── ...
        │   │
        │   ├── country=US
        │   │   └── data.parquet
        │   ├── country=CN
        │   │   └── data.parquet
        │   └── ...
        └── gender=female
            ├── ...
            │
            ├── country=US
            │   └── data.parquet
            ├── country=CN
            │   └── data.parquet
            └── ...
```

> By passing `path/to/table` to either `SparkSession.read.parquet` or `SparkSession.read.load`, Spark SQL will automatically extract the partitioning information from the paths. Now the schema of the returned DataFrame becomes:

通过将 `path/to/table` 传递给 `SparkSession.read.parquet` 或 `SparkSession.read.load`, Spark SQL 将自动从路径中提取分区信息。【读数据的时候，根据目录名和层级推断分区信息】

现在返回的 DataFrame 的结构变成:

```
root
|-- name: string (nullable = true)
|-- age: long (nullable = true)
|-- gender: string (nullable = true)
|-- country: string (nullable = true)
```

> Notice that the data types of the partitioning columns are automatically inferred. Currently, numeric data types, date, timestamp and string type are supported. Sometimes users may not want to automatically infer the data types of the partitioning columns. For these use cases, the automatic type inference can be configured by `spark.sql.sources.partitionColumnTypeInference.enabled`, which is default to true. When type inference is disabled, string type will be used for the partitioning columns.

分区列的数据类型可以被自动推断，目前支持数值、日期、时间戳和字符串。

如果不想自动推断，可以配置属性 `spark.sql.sources.partitionColumnTypeInference.enabled`，默认是 true. 当禁用自动推断时，分区列的类型就是字符串。

> Starting from Spark 1.6.0, partition discovery only finds partitions under the given paths by default. For the above example, if users pass `path/to/table/gender=male` to either SparkSession.read.parquet or SparkSession.read.load, gender will not be considered as a partitioning column. If users need to specify the base path that partition discovery should start with, they can set `basePath` in the data source options. For example, when `path/to/table/gender=male` is the path of the data and users set `basePath` to `path/to/table/`, gender will be a partitioning column.

从 Spark 1.6.0 开始，默认情况下，分区发现只能找到给定路径下的分区。

对于上述示例，如果用户将 `path/to/table/gender=male` 传递给 `SparkSession.read.parquet` 或 `SparkSession.read.load`, 则 gender 将不被视为分区列。

如果用户需要指定分区发现开始的基本路径，则可以在数据源选项中设置 `basePath`. 例如，当 `path/to/table/gender=male` 是数据的路径，并且用户将 `basePath` 设置为 `path/to/table/`, gender 将是一个分区列。

## Schema Merging

> Like Protocol Buffer, Avro, and Thrift, Parquet also supports schema evolution. Users can start with a simple schema, and gradually add more columns to the schema as needed. In this way, users may end up with multiple Parquet files with different but mutually compatible schemas. The Parquet data source is now able to automatically detect this case and merge schemas of all these files.

用户可以从一个简单的结构开始，逐渐地添加列。

在这种情况下，用户最终可能会得到多个不同的但又相互兼容模式的 Parquet 文件。 

Parquet 数据源现在能够自动检测这种情况，并合并所有这些文件的结构。

> Since schema merging is a relatively expensive operation, and is not a necessity in most cases, we turned it off by default starting from 1.5.0. You may enable it by

结构合并是一个昂贵且不必要的操作，默认关闭此功能。你可以这样启动：

- 在读文件时，设置 `mergeSchema` 选项为 true
- 或全局设置 `spark.sql.parquet.mergeSchema = true`

> setting data source option `mergeSchema` to true when reading Parquet files (as shown in the examples below), or

> setting the global SQL option spark.sql.parquet.mergeSchema to true.

```scala
// This is used to implicitly convert an RDD to a DataFrame.
import spark.implicits._

// Create a simple DataFrame, store into a partition directory
val squaresDF = spark.sparkContext.makeRDD(1 to 5).map(i => (i, i * i)).toDF("value", "square")
squaresDF.write.parquet("data/test_table/key=1")

// Create another DataFrame in a new partition directory,
// adding a new column and dropping an existing column
val cubesDF = spark.sparkContext.makeRDD(6 to 10).map(i => (i, i * i * i)).toDF("value", "cube")
cubesDF.write.parquet("data/test_table/key=2")

// Read the partitioned table
val mergedDF = spark.read.option("mergeSchema", "true").parquet("data/test_table")
mergedDF.printSchema()

// The final schema consists of all 3 columns in the Parquet files together
// with the partitioning column appeared in the partition directory paths
// root
//  |-- value: int (nullable = true)
//  |-- square: int (nullable = true)
//  |-- cube: int (nullable = true)
//  |-- key: int (nullable = true)
```
> Find full example code at "examples/src/main/scala/org/apache/spark/examples/sql/SQLDataSourceExample.scala" in the Spark repo.

```java
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

public static class Square implements Serializable {
  private int value;
  private int square;

  // Getters and setters...

}

public static class Cube implements Serializable {
  private int value;
  private int cube;

  // Getters and setters...

}

List<Square> squares = new ArrayList<>();
for (int value = 1; value <= 5; value++) {
  Square square = new Square();
  square.setValue(value);
  square.setSquare(value * value);
  squares.add(square);
}

// Create a simple DataFrame, store into a partition directory
Dataset<Row> squaresDF = spark.createDataFrame(squares, Square.class);
squaresDF.write().parquet("data/test_table/key=1");

List<Cube> cubes = new ArrayList<>();
for (int value = 6; value <= 10; value++) {
  Cube cube = new Cube();
  cube.setValue(value);
  cube.setCube(value * value * value);
  cubes.add(cube);
}

// Create another DataFrame in a new partition directory,
// adding a new column and dropping an existing column
Dataset<Row> cubesDF = spark.createDataFrame(cubes, Cube.class);
cubesDF.write().parquet("data/test_table/key=2");

// Read the partitioned table
Dataset<Row> mergedDF = spark.read().option("mergeSchema", true).parquet("data/test_table");
mergedDF.printSchema();

// The final schema consists of all 3 columns in the Parquet files together
// with the partitioning column appeared in the partition directory paths
// root
//  |-- value: int (nullable = true)
//  |-- square: int (nullable = true)
//  |-- cube: int (nullable = true)
//  |-- key: int (nullable = true)
```
> Find full example code at "examples/src/main/java/org/apache/spark/examples/sql/JavaSQLDataSourceExample.java" in the Spark repo.

```python
from pyspark.sql import Row

# spark is from the previous example.
# Create a simple DataFrame, stored into a partition directory
sc = spark.sparkContext

squaresDF = spark.createDataFrame(sc.parallelize(range(1, 6))
                                  .map(lambda i: Row(single=i, double=i ** 2)))
squaresDF.write.parquet("data/test_table/key=1")

# Create another DataFrame in a new partition directory,
# adding a new column and dropping an existing column
cubesDF = spark.createDataFrame(sc.parallelize(range(6, 11))
                                .map(lambda i: Row(single=i, triple=i ** 3)))
cubesDF.write.parquet("data/test_table/key=2")

# Read the partitioned table
mergedDF = spark.read.option("mergeSchema", "true").parquet("data/test_table")
mergedDF.printSchema()

# The final schema consists of all 3 columns in the Parquet files together
# with the partitioning column appeared in the partition directory paths.
# root
#  |-- double: long (nullable = true)
#  |-- single: long (nullable = true)
#  |-- triple: long (nullable = true)
#  |-- key: integer (nullable = true)
```
> Find full example code at "examples/src/main/python/sql/datasource.py" in the Spark repo.

```r
df1 <- createDataFrame(data.frame(single=c(12, 29), double=c(19, 23)))
df2 <- createDataFrame(data.frame(double=c(19, 23), triple=c(23, 18)))

# Create a simple DataFrame, stored into a partition directory
write.df(df1, "data/test_table/key=1", "parquet", "overwrite")

# Create another DataFrame in a new partition directory,
# adding a new column and dropping an existing column
write.df(df2, "data/test_table/key=2", "parquet", "overwrite")

# Read the partitioned table
df3 <- read.df("data/test_table", "parquet", mergeSchema = "true")
printSchema(df3)
# The final schema consists of all 3 columns in the Parquet files together
# with the partitioning column appeared in the partition directory paths
## root
##  |-- single: double (nullable = true)
##  |-- double: double (nullable = true)
##  |-- triple: double (nullable = true)
##  |-- key: integer (nullable = true)
```
> Find full example code at "examples/src/main/r/RSparkSQLExample.R" in the Spark repo.

## Hive metastore Parquet table conversion

> When reading from Hive metastore Parquet tables and writing to non-partitioned Hive metastore Parquet tables, Spark SQL will try to use its own Parquet support instead of Hive SerDe for better performance. This behavior is controlled by the `spark.sql.hive.convertMetastoreParquet` configuration, and is turned on by default.

当从 Hive 元数据库的 Parquet 表中读写数据时, Spark SQL 将尝试使用自己的 Parquet 支持，而不是使用 Hive SerDe 来获得更好的性能。

此行为由 `spark.sql.hive.convertMetastoreParquet` 配置控制，默认情况下打开。

### Hive/Parquet Schema Reconciliation

> There are two key differences between Hive and Parquet from the perspective of table schema processing.

> Hive is case insensitive, while Parquet is not
> Hive considers all columns nullable, while nullability in Parquet is significant

从表结构处理视角看，Hive 和 Parquet 有两点不同：

- Hive 不区分大小写, Parquet 区分
- Hive 认为所有列都可以为空，而 Parquet 不允许 

> Due to this reason, we must reconcile Hive metastore schema with Parquet schema when converting a Hive metastore Parquet table to a Spark SQL Parquet table. The reconciliation rules are:

> Fields that have the same name in both schema must have the same data type regardless of nullability. The reconciled field should have the data type of the Parquet side, so that nullability is respected.

> The reconciled schema contains exactly those fields defined in Hive metastore schema.

> Any fields that only appear in the Parquet schema are dropped in the reconciled schema.
> Any fields that only appear in the Hive metastore schema are added as nullable field in the reconciled schema.

由于这个原因，当将 Hive 元数据库的 Parquet 表转换为 Spark SQL Parquet 表时，必须使 Hive 元数据库结构与 Parquet 结构一致。规则是:

- 在两个结构中具有相同名称的字段必须具有相同的数据类型，而不管可空性。调整的字段应具有 Parquet 侧的数据类型。

- 调整的结构要包含 Hive 元数据库的 Parquet 表中定义的那些字段：

	- 只出现在 Parquet 结构中的任何字段都要在调整的结构中将被删除。

	- 仅在 Hive 元数据库结构中出现的任何字段在调整的结构中作为可空字段添加。

### Metadata Refreshing

> Spark SQL caches Parquet metadata for better performance. When Hive metastore Parquet table conversion is enabled, metadata of those converted tables are also cached. If these tables are updated by Hive or other external tools, you need to refresh them manually to ensure consistent metadata.

Spark SQL 缓存 Parquet 元数据以获得更好的性能。

当启用 Hive 元数据库的 Parquet 表转换时，这些 转换表的元数据也被缓存。

如果这些表由 Hive 或其他外部工具更新，则需要手动刷新以确保元数据一致。

```scala
// spark is an existing SparkSession
spark.catalog.refreshTable("my_table")
```

```java
// spark is an existing SparkSession
spark.catalog().refreshTable("my_table");
```

```python
# spark is an existing SparkSession
spark.catalog.refreshTable("my_table")
```

```r
refreshTable("my_table")
```

```sql
REFRESH TABLE my_table;
```

## Columnar Encryption

> Since Spark 3.2, columnar encryption is supported for Parquet tables with Apache Parquet 1.12+.

从 Spark 3.2 、 Parquet 1.12+ 开始，Parquet表支持列加密。

> Parquet uses the envelope encryption practice, where file parts are encrypted with “data encryption keys” (DEKs), and the DEKs are encrypted with “master encryption keys” (MEKs). The DEKs are randomly generated by Parquet for each encrypted file/column. The MEKs are generated, stored and managed in a Key Management Service (KMS) of user’s choice. The Parquet Maven [repository](https://repo1.maven.org/maven2/org/apache/parquet/parquet-hadoop/1.12.0/) has a jar with a mock KMS implementation that allows to run column encryption and decryption using a spark-shell only, without deploying a KMS server (download the `parquet-hadoop-tests.jar` file and place it in the Spark jars folder):

Parquet 使用信封加密方案，文件部分使用 DEKs 加密，而 DEKs 使用 MEKs 加密。

DEKs 是由 Parquet 为每个加密文件列随机生成的。 MEKs 在 KMS 中生成、存储和管理。

Parquet Maven 仓库有一个模拟 KMS 实现的 jar 包，可以（仅）在 spark-shell 中运行列加密和解密，而不用部署 KMS 服务。（下载 `parquet-hadoop-tests.jar` 文件，将其放在 spark jars 文件夹中）

```sh
sc.hadoopConfiguration.set("parquet.encryption.kms.client.class" ,
                           "org.apache.parquet.crypto.keytools.mocks.InMemoryKMS")

// Explicit master keys (base64 encoded) - required only for mock InMemoryKMS
sc.hadoopConfiguration.set("parquet.encryption.key.list" ,
                   "keyA:AAECAwQFBgcICQoLDA0ODw== ,  keyB:AAECAAECAAECAAECAAECAA==")

// Activate Parquet encryption, driven by Hadoop properties
sc.hadoopConfiguration.set("parquet.crypto.factory.class" ,
                   "org.apache.parquet.crypto.keytools.PropertiesDrivenCryptoFactory")

// Write encrypted dataframe files. 
// Column "square" will be protected with master key "keyA".
// Parquet file footers will be protected with master key "keyB"
squaresDF.write.
   option("parquet.encryption.column.keys" , "keyA:square").
   option("parquet.encryption.footer.key" , "keyB").
parquet("/path/to/table.parquet.encrypted")

// Read encrypted dataframe files
val df2 = spark.read.parquet("/path/to/table.parquet.encrypted")
```

### KMS Client

> The InMemoryKMS class is provided only for illustration and simple demonstration of Parquet encryption functionality. It should not be used in a real deployment. The master encryption keys must be kept and managed in a production-grade KMS system, deployed in user’s organization. Rollout of Spark with Parquet encryption requires implementation of a client class for the KMS server. Parquet provides a plug-in [interface](https://github.com/apache/parquet-mr/blob/apache-parquet-1.12.0/parquet-hadoop/src/main/java/org/apache/parquet/crypto/keytools/KmsClient.java) for development of such classes,

InMemoryKMS 类仅用来对 Parquet 加密功能的说明和简单阐述，不应该将其用在真实环境。

主加密密钥必须在生产级别的 KMS 系统中保存和管理。使用 Parquet 加密的 Spark 的推出需要为 KMS 服务器实现一个客户端类。 Parquet 为此类的部署提供了一个可拔插的接口。

```java
public interface KmsClient {
  // Wraps a key - encrypts it with the master key.
  public String wrapKey(byte[] keyBytes, String masterKeyIdentifier);

  // Decrypts (unwraps) a key with the master key. 
  public byte[] unwrapKey(String wrappedKey, String masterKeyIdentifier);

  // Use of initialization parameters is optional.
  public void initialize(Configuration configuration, String kmsInstanceID, 
                         String kmsInstanceURL, String accessToken);
}
```

> An [example](https://github.com/apache/parquet-mr/blob/master/parquet-hadoop/src/test/java/org/apache/parquet/crypto/keytools/samples/VaultClient.java) of such class for an open source [KMS](https://www.vaultproject.io/api/secret/transit) can be found in the parquet-mr repository. The production KMS client should be designed in cooperation with organization’s security administrators, and built by developers with an experience in access control management. Once such class is created, it can be passed to applications via the `parquet.encryption.kms.client.class` parameter and leveraged by general Spark users as shown in the encrypted dataframe write/read sample above.

开源 KMS 的此类示例可以在 parquet-mr 仓库中找到。生产 KMS 客户端应该和组织的安全管理员协调一致，由具备访问控制管理经验的开发者构建。

一旦创建了这种类，它应该通过 `parquet.encryption.kms.client.class` 参数传给应用程序，正如上面加密 dataframe 写/读示例所示。

> Note: By default, Parquet implements a “double envelope encryption” mode, that minimizes the interaction of Spark executors with a KMS server. In this mode, the DEKs are encrypted with “key encryption keys” (KEKs, randomly generated by Parquet). The KEKs are encrypted with MEKs in KMS; the result and the KEK itself are cached in Spark executor memory. Users interested in regular envelope encryption, can switch to it by setting the `parquet.encryption.double.wrapping` parameter to false. For more details on Parquet encryption parameters, visit the parquet-hadoop configuration [page](https://github.com/apache/parquet-mr/blob/master/parquet-hadoop/README.md#class-propertiesdrivencryptofactory).

注意：默认情况下, Parquet 实现一个“双层信封加密”模式，这最小化了 Spark executors 和 KMS 服务相互影响。在这种模式下，使用 KEKs 加密 DEKs. KEKs 在 KMS 中使用 MEKs 加密；结果和 KEK 本身被缓存在 Spark executor 内存中。对常信封加密的用户可以通过设置 `parquet.encryption.double.wrapping` 为 false 来切换。对于关于 Parquet 加密参数的更多细节，访问 parquet-hadoop 配置页。

## Data Source Option

> Data source options of Parquet can be set via:

Parquet 的数据源选项可以通过如下方式设置：

- the `.option/.options` methods of
    + DataFrameReader
    + DataFrameWriter
    + DataStreamReader
    + DataStreamWriter

- `OPTIONS` clause at [`CREATE TABLE USING DATA_SOURCE`](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-ddl-create-table-datasource.html)

Property Name | Default | Meaning | Scope
---|:---|:---|:---
`datetimeRebaseMode` | (value of `spark.sql.parquet.datetimeRebaseModeInRead` configuration) | The `datetimeRebaseMode` option allows to specify the rebasing mode for the values of the **`DATE`, `TIMESTAMP_MILLIS`, `TIMESTAMP_MICROS`** logical types from the Julian to Proleptic Gregorian calendar.【指定重定模式】<br/><br/>Currently supported modes are: `EXCEPTION`: fails in reads of ancient dates/timestamps that are ambiguous between the two calendars. `CORRECTED`: loads dates/timestamps without rebasing. `LEGACY`: performs rebasing of ancient dates/timestamps from the Julian to Proleptic Gregorian calendar. 【`EXCEPTION`： 在读取远古dates/timestamps时失败，dates/timestamps在两个日历中是模糊的。`CORRECTED`：无需重定，即载入dates/timestamps。`LEGACY`：指向远古dates/timestamps的重定】 | read
`int96RebaseMode` | (value of `spark.sql.parquet.int96RebaseModeInRead` configuration) | The `int96RebaseMode` option allows to specify the rebasing mode for **INT96 timestamps** from the Julian to Proleptic Gregorian calendar.【指定重定模式】<br/><br/>Currently supported modes are:`EXCEPTION`: fails in reads of ancient INT96 timestamps that are ambiguous between the two calendars.`CORRECTED`: loads INT96 timestamps without rebasing.`LEGACY`: performs rebasing of ancient timestamps from the Julian to Proleptic Gregorian calendar. | read
mergeSchema | (value of `spark.sql.parquet.mergeSchema` configuration)  | Sets whether we should merge schemas collected from all Parquet part-files. This will override `spark.sql.parquet.mergeSchema`. 【是否应该合并所有文件的结构】| read
`compression` | `snappy`  | Compression codec to use when saving to file. This can be one of the known case-insensitive shorten names (none, uncompressed, snappy, gzip, lzo, brotli, lz4, and zstd). This will override spark.sql.parquet.compression.codec. 【保存到文件时使用的压缩格式，大小写不敏感】| write

> Other generic options can be found in [Generic Files Source Options](https://spark.apache.org/docs/latest/sql-data-sources-generic-options.html)

### Configuration

> Configuration of Parquet can be done using the `setConf` method on `SparkSession` or by running `SET key=value` commands using SQL.

可以使用 SparkSession 上的 `setConf` 方法或使用 SQL 运行 `SET key = value` 命令来完成 Parquet 的配置.

Property Name | Default | Meaning | Since Version
---|:---|:---|:---
`spark.sql.parquet.binaryAsString` | false | Some other Parquet-producing systems, in particular Impala, Hive, and older versions of Spark SQL, do not differentiate between binary data and strings when writing out the Parquet schema. This flag tells Spark SQL to interpret binary data as a string to provide compatibility with these systems. 【将二进制数据解释成字符串】| 1.1.1
`spark.sql.parquet.int96AsTimestamp` | true | Some Parquet-producing systems, in particular Impala and Hive, store Timestamp into INT96. This flag tells Spark SQL to interpret INT96 data as a timestamp to provide compatibility with these systems. 【将INT96数据解释成时间戳】| 1.3.0
`spark.sql.parquet.compression.codec` | snappy | Sets the compression codec used when writing Parquet files. If either `compression` or `parquet.compression` is specified in the table-specific options/properties, the precedence would be `compression`, `parquet.compression`, `spark.sql.parquet.compression.codec`. Acceptable values include: none, uncompressed, snappy, gzip, lzo, brotli, lz4, zstd. Note that zstd requires ZStandardCodec to be installed before Hadoop 2.9.0, brotli requires BrotliCodec to be installed. 【在写入Parquet文件时使用的压缩格式。如果没有在表级别指定`compression`或`parquet.compression`，那么依次使用`compression`, `parquet.compression`, `spark.sql.parquet.compression.codec`】 | 1.1.1
spark.sql.parquet.filterPushdown | true | Enables Parquet filter push-down optimization when set to true. 【启用Parquet谓词下推】| 1.2.0
`spark.sql.hive.convertMetastoreParquet` | true | When set to false, Spark SQL will use the Hive SerDe for parquet tables instead of the built in support.【如果设置为false，那么Spark SQL使用Hive SerDe处理parquet表，而不是内建支持】 | 1.1.1
`spark.sql.parquet.mergeSchema` | false | When true, the Parquet data source merges schemas collected from all data files, otherwise the schema is picked from the summary file or a random data file if no summary file is available. 【如果是true，合并所有文件的结构；否则就从摘要文件中选择一个结构，或者如果没有摘要文件，就选一个随机数据文件】| 1.5.0
`spark.sql.parquet.writeLegacyFormat` | false | If true, data will be written in a way of Spark 1.4 and earlier. For example, decimal values will be written in Apache Parquet's fixed-length byte array format, which other systems such as Apache Hive and Apache Impala use. If false, the newer format in Parquet will be used. For example, decimals will be written in int-based format. If Parquet output is intended for use with systems that do not support this newer format, set to true. 【如果是true,那么数据以Spark 1.4及更早版本的方式写入。例如decimal值将以Parquet固定长度字节数组格式写入，其他系统也使用这种格式。如果是false,将使用Parquet中更新的格式，例如decimal值以int-based格式写入。如果Parquet的输出是为了和不支持更新格式的系统一起使用，就设置为true】 | 1.6.0
`spark.sql.parquet.datetimeRebaseModeInRead` | `EXCEPTION` | The rebasing mode for the values of the `DATE`, `TIMESTAMP_MILLIS`, `TIMESTAMP_MICROS` logical types from the Julian to Proleptic Gregorian calendar: `EXCEPTION`: Spark will fail the reading if it sees ancient dates/timestamps that are ambiguous between the two calendars.`CORRECTED`: Spark will not do rebase and read the dates/timestamps as it is.`LEGACY`: Spark will rebase dates/timestamps from the legacy hybrid (Julian + Gregorian) calendar to Proleptic Gregorian calendar when reading Parquet files. This config is only effective if the writer info (like Spark, Hive) of the Parquet files is unknown. | 3.0.0
`spark.sql.parquet.datetimeRebaseModeInWrite` | `EXCEPTION` | The rebasing mode for the values of the `DATE`, `TIMESTAMP_MILLIS`, `TIMESTAMP_MICROS` logical types from the Proleptic Gregorian to Julian calendar: `EXCEPTION`: Spark will fail the writing if it sees ancient dates/timestamps that are ambiguous between the two calendars.`CORRECTED`: Spark will not do rebase and write the dates/timestamps as it is.`LEGACY`: Spark will rebase dates/timestamps from Proleptic Gregorian calendar to the legacy hybrid (Julian + Gregorian) calendar when writing Parquet files. | 3.0.0
`spark.sql.parquet.int96RebaseModeInRead` | `EXCEPTION` | The rebasing mode for the values of the INT96 timestamp type from the Julian to Proleptic Gregorian calendar:`EXCEPTION`: Spark will fail the reading if it sees ancient INT96 timestamps that are ambiguous between the two calendars.`CORRECTED`: Spark will not do rebase and read the dates/timestamps as it is.`LEGACY`: Spark will rebase INT96 timestamps from the legacy hybrid (Julian + Gregorian) calendar to Proleptic Gregorian calendar when reading Parquet files. This config is only effective if the writer info (like Spark, Hive) of the Parquet files is unknown. | 3.1.0
`spark.sql.parquet.int96RebaseModeInWrite` | `EXCEPTION` | The rebasing mode for the values of the INT96 timestamp type from the Proleptic Gregorian to Julian calendar:`EXCEPTION`: Spark will fail the writing if it sees ancient timestamps that are ambiguous between the two calendars.`CORRECTED`: Spark will not do rebase and write the dates/timestamps as it is.`LEGACY`: Spark will rebase INT96 timestamps from Proleptic Gregorian calendar to the legacy hybrid (Julian + Gregorian) calendar when writing Parquet files. | 3.1.0