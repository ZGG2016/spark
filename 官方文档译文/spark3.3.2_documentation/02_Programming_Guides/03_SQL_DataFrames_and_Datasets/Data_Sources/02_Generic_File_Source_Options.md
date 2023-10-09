# Generic File Source Options

[TOC]

> These generic options/configurations are effective only when using file-based sources: parquet, orc, avro, json, csv, text.

只有在使用基于文件的源(如 parquet, orc, avro, json, csv, text)时，这些通用配置才有效。

> Please note that the hierarchy of directories used in examples below are:

本页例子中目录层次结构如下：

```
dir1/
 ├── dir2/
 │    └── file2.parquet (schema: <file: string>, content: "file2.parquet")
 └── file1.parquet (schema: <file, string>, content: "file1.parquet")
 └── file3.json (schema: <file, string>, content: "{'file':'corrupt.json'}")
```

## Ignore Corrupt Files

> Spark allows you to use `spark.sql.files.ignoreCorruptFiles` to ignore corrupt files while reading data from files. When set to true, the Spark jobs will continue to run when encountering corrupted files and the contents that have been read will still be returned.

当从文件读取数据遇到损坏的文件时，设置 `spark.sql.files.ignoreCorruptFiles = true` 可以忽略损坏的文件，使程序继续运行，最后返回读到内容。

> To ignore corrupt files while reading data files, you can use:

```scala
// enable ignore corrupt files
spark.sql("set spark.sql.files.ignoreCorruptFiles=true")
// dir1/file3.json is corrupt from parquet's view
val testCorruptDF = spark.read.parquet(
  "examples/src/main/resources/dir1/",
  "examples/src/main/resources/dir1/dir2/")
testCorruptDF.show()
// +-------------+
// |         file|
// +-------------+
// |file1.parquet|
// |file2.parquet|
// +-------------+
```
> Find full example code at "examples/src/main/scala/org/apache/spark/examples/sql/SQLDataSourceExample.scala" in the Spark repo.

```java
// enable ignore corrupt files
spark.sql("set spark.sql.files.ignoreCorruptFiles=true");
// dir1/file3.json is corrupt from parquet's view
Dataset<Row> testCorruptDF = spark.read().parquet(
        "examples/src/main/resources/dir1/",
        "examples/src/main/resources/dir1/dir2/");
testCorruptDF.show();
// +-------------+
// |         file|
// +-------------+
// |file1.parquet|
// |file2.parquet|
// +-------------+
```
> Find full example code at "examples/src/main/java/org/apache/spark/examples/sql/JavaSQLDataSourceExample.java" in the Spark repo.

```python
# enable ignore corrupt files
spark.sql("set spark.sql.files.ignoreCorruptFiles=true")
# dir1/file3.json is corrupt from parquet's view
test_corrupt_df = spark.read.parquet("examples/src/main/resources/dir1/",
                                     "examples/src/main/resources/dir1/dir2/")
test_corrupt_df.show()
# +-------------+
# |         file|
# +-------------+
# |file1.parquet|
# |file2.parquet|
# +-------------+
```
> Find full example code at "examples/src/main/python/sql/datasource.py" in the Spark repo.

```r
# enable ignore corrupt files
sql("set spark.sql.files.ignoreCorruptFiles=true")
# dir1/file3.json is corrupt from parquet's view
testCorruptDF <- read.parquet(c("examples/src/main/resources/dir1/", "examples/src/main/resources/dir1/dir2/"))
head(testCorruptDF)
#            file
# 1 file1.parquet
# 2 file2.parquet
```
> Find full example code at "examples/src/main/r/RSparkSQLExample.R" in the Spark repo.

## Ignore Missing Files

> Spark allows you to use `spark.sql.files.ignoreMissingFiles` to ignore missing files while reading data from files. Here, missing file really means the deleted file under directory after you construct the DataFrame. When set to true, the Spark jobs will continue to run when encountering missing files and the contents that have been read will still be returned.

缺失文件指构造 DataFrame 后，删除的目录下的文件。

当从文件读取数据遇到缺失文件时，设置 `spark.sql.files.ignoreMissingFiles = true` 可以使程序继续运行，最后返回读到内容。

## Path Global Filter

> `pathGlobFilter` is used to only include files with file names matching the pattern. The syntax follows `org.apache.hadoop.fs.GlobFilter`. It does not change the behavior of partition discovery.

`pathGlobFilter` 用来过滤掉不符合匹配规则的文件。需要配置 `org.apache.hadoop.fs.GlobFilter.`

它不会改变分区发现的行为。

> To load files with paths matching a given glob pattern while keeping the behavior of partition discovery, you can use:

```scala
val testGlobFilterDF = spark.read.format("parquet")
  .option("pathGlobFilter", "*.parquet") // json file should be filtered out
  .load("examples/src/main/resources/dir1")
testGlobFilterDF.show()
// +-------------+
// |         file|
// +-------------+
// |file1.parquet|
// +-------------+
```
> Find full example code at "examples/src/main/scala/org/apache/spark/examples/sql/SQLDataSourceExample.scala" in the Spark repo.

```java
Dataset<Row> testGlobFilterDF = spark.read().format("parquet")
        .option("pathGlobFilter", "*.parquet") // json file should be filtered out
        .load("examples/src/main/resources/dir1");
testGlobFilterDF.show();
// +-------------+
// |         file|
// +-------------+
// |file1.parquet|
// +-------------+
```
> Find full example code at "examples/src/main/java/org/apache/spark/examples/sql/JavaSQLDataSourceExample.java" in the Spark repo.

```python
df = spark.read.load("examples/src/main/resources/dir1",
                     format="parquet", pathGlobFilter="*.parquet")
df.show()
# +-------------+
# |         file|
# +-------------+
# |file1.parquet|
# +-------------+
```
> Find full example code at "examples/src/main/python/sql/datasource.py" in the Spark repo.

```r
df <- read.df("examples/src/main/resources/dir1", "parquet", pathGlobFilter = "*.parquet")
#            file
# 1 file1.parquet
```

> Find full example code at "examples/src/main/r/RSparkSQLExample.R" in the Spark repo.

## Recursive File Lookup

> `recursiveFileLookup` is used to recursively load files and it disables partition inferring. Its default value is `false`. If data source explicitly specifies the `partitionSpec` when `recursiveFileLookup` is true, exception will be thrown.

`recursiveFileLookup` 用来递归载入文件，但会停止分区推断，默认 false.

当设为 true 时，如果数据源明确指定了 `partitionSpec`, 则会抛出异常。

> To load all files recursively, you can use:

```scala
val recursiveLoadedDF = spark.read.format("parquet")
  .option("recursiveFileLookup", "true")
  .load("examples/src/main/resources/dir1")
recursiveLoadedDF.show()
// +-------------+
// |         file|
// +-------------+
// |file1.parquet|
// |file2.parquet|
// +-------------+
```
> Find full example code at "examples/src/main/scala/org/apache/spark/examples/sql/SQLDataSourceExample.scala" in the Spark repo.

```java
Dataset<Row> recursiveLoadedDF = spark.read().format("parquet")
        .option("recursiveFileLookup", "true")
        .load("examples/src/main/resources/dir1");
recursiveLoadedDF.show();
// +-------------+
// |         file|
// +-------------+
// |file1.parquet|
// |file2.parquet|
// +-------------+
```
> Find full example code at "examples/src/main/java/org/apache/spark/examples/sql/JavaSQLDataSourceExample.java" in the Spark repo.

```python
recursive_loaded_df = spark.read.format("parquet")\
    .option("recursiveFileLookup", "true")\
    .load("examples/src/main/resources/dir1")
recursive_loaded_df.show()
# +-------------+
# |         file|
# +-------------+
# |file1.parquet|
# |file2.parquet|
# +-------------+
```
> Find full example code at "examples/src/main/python/sql/datasource.py" in the Spark repo.

```r
recursiveLoadedDF <- read.df("examples/src/main/resources/dir1", "parquet", recursiveFileLookup = "true")
head(recursiveLoadedDF)
#            file
# 1 file1.parquet
# 2 file2.parquet
```
> Find full example code at "examples/src/main/r/RSparkSQLExample.R" in the Spark repo.

## Modification Time Path Filters

> `modifiedBefore` and `modifiedAfter` are options that can be applied together or separately in order to achieve greater granularity over which files may load during a Spark batch query. (Note that Structured Streaming file sources don’t support these options.)

`modifiedBefore` 和 `modifiedAfter` 选项可以同时或分别使用，在 Spark 批量查询期间，实现更大的文件筛选加载粒度。

> `modifiedBefore`: an optional timestamp to only include files with modification times occurring before the specified time. The provided timestamp must be in the following format: `YYYY-MM-DDTHH:mm:ss` (e.g. 2020-06-01T13:00:00)

- `modifiedBefore`: 一个可选时间戳，仅包含在指定时间之前出现的文件。时间戳格式必须是 `YYYY-MM-DDTHH:mm:ss`

> `modifiedAfter`: an optional timestamp to only include files with modification times occurring after the specified time. The provided timestamp must be in the following format: YYYY-MM-DDTHH:mm:ss (e.g. 2020-06-01T13:00:00)

- `modifiedAfter`: 一个可选时间戳，仅包含在指定时间之后出现的文件。时间戳格式必须是 `YYYY-MM-DDTHH:mm:ss`

> When a timezone option is not provided, the timestamps will be interpreted according to the Spark session timezone (`spark.sql.session.timeZone`).

如果没有提供时区选项，那么时间戳将根据 Spark 会话时区推断(`spark.sql.session.timeZone`).

> To load files with paths matching a given modified time range, you can use:

```scala
val beforeFilterDF = spark.read.format("parquet")
  // Files modified before 07/01/2020 at 05:30 are allowed
  .option("modifiedBefore", "2020-07-01T05:30:00")
  .load("examples/src/main/resources/dir1");
beforeFilterDF.show();
// +-------------+
// |         file|
// +-------------+
// |file1.parquet|
// +-------------+
val afterFilterDF = spark.read.format("parquet")
   // Files modified after 06/01/2020 at 05:30 are allowed
  .option("modifiedAfter", "2020-06-01T05:30:00")
  .load("examples/src/main/resources/dir1");
afterFilterDF.show();
// +-------------+
// |         file|
// +-------------+
// +-------------+
```
> Find full example code at "examples/src/main/scala/org/apache/spark/examples/sql/SQLDataSourceExample.scala" in the Spark repo.

```java
Dataset<Row> beforeFilterDF = spark.read().format("parquet")
        // Only load files modified before 7/1/2020 at 05:30
        .option("modifiedBefore", "2020-07-01T05:30:00")
        // Only load files modified after 6/1/2020 at 05:30
        .option("modifiedAfter", "2020-06-01T05:30:00")
        // Interpret both times above relative to CST timezone
        .option("timeZone", "CST")
        .load("examples/src/main/resources/dir1");
beforeFilterDF.show();
// +-------------+
// |         file|
// +-------------+
// |file1.parquet|
// +-------------+
```
> Find full example code at "examples/src/main/java/org/apache/spark/examples/sql/JavaSQLDataSourceExample.java" in the Spark repo.

```python
# Only load files modified before 07/1/2050 @ 08:30:00
df = spark.read.load("examples/src/main/resources/dir1",
                     format="parquet", modifiedBefore="2050-07-01T08:30:00")
df.show()
# +-------------+
# |         file|
# +-------------+
# |file1.parquet|
# +-------------+
# Only load files modified after 06/01/2050 @ 08:30:00
df = spark.read.load("examples/src/main/resources/dir1",
                     format="parquet", modifiedAfter="2050-06-01T08:30:00")
df.show()
# +-------------+
# |         file|
# +-------------+
# +-------------+
```
> Find full example code at "examples/src/main/python/sql/datasource.py" in the Spark repo.

```r
beforeDF <- read.df("examples/src/main/resources/dir1", "parquet", modifiedBefore= "2020-07-01T05:30:00")
#            file
# 1 file1.parquet
afterDF <- read.df("examples/src/main/resources/dir1", "parquet", modifiedAfter = "2020-06-01T05:30:00")
#            file
```
> Find full example code at "examples/src/main/r/RSparkSQLExample.R" in the Spark repo.