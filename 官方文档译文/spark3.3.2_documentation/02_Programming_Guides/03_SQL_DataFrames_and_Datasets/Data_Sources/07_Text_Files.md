# Text Files

> Spark SQL provides `spark.read().text("file_name")` to read a file or directory of text files into a Spark DataFrame, and `dataframe.write().text("path")` to write to a text file. When reading a text file, each line becomes each row that has string “value” column by default. The line separator can be changed as shown in the example below. The `option()` function can be used to customize the behavior of reading or writing, such as controlling behavior of the line separator, compression, and so on.

使用 `spark.read().text("file_name")` 读取一个文件或文本文件组成的目录，返回 DataFrame. 使用 `dataframe.write().text("path")` 将数据写入到文本文件。

当读取一个文本文件时，每行会成为一个拥有字符串“value”列的 row.

函数 `option()` 用来个性化读写行为，例如控制行分隔符、压缩等。

```scala
// A text dataset is pointed to by path.
// The path can be either a single text file or a directory of text files
val path = "examples/src/main/resources/people.txt"

val df1 = spark.read.text(path)
df1.show()
// +-----------+
// |      value|
// +-----------+
// |Michael, 29|
// |   Andy, 30|
// | Justin, 19|
// +-----------+

// You can use 'lineSep' option to define the line separator.
// The line separator handles all `\r`, `\r\n` and `\n` by default.
val df2 = spark.read.option("lineSep", ",").text(path)
df2.show()
// +-----------+
// |      value|
// +-----------+
// |    Michael|
// |   29\nAndy|
// | 30\nJustin|
// |       19\n|
// +-----------+

// You can also use 'wholetext' option to read each input file as a single row.
val df3 = spark.read.option("wholetext", true).text(path)
df3.show()
//  +--------------------+
//  |               value|
//  +--------------------+
//  |Michael, 29\nAndy...|
//  +--------------------+

// "output" is a folder which contains multiple text files and a _SUCCESS file.
df1.write.text("output")

// You can specify the compression format using the 'compression' option.
df1.write.option("compression", "gzip").text("output_compressed")
```
> Find full example code at "examples/src/main/scala/org/apache/spark/examples/sql/SQLDataSourceExample.scala" in the Spark repo.

```java
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

// A text dataset is pointed to by path.
// The path can be either a single text file or a directory of text files
String path = "examples/src/main/resources/people.txt";

Dataset<Row> df1 = spark.read().text(path);
df1.show();
// +-----------+
// |      value|
// +-----------+
// |Michael, 29|
// |   Andy, 30|
// | Justin, 19|
// +-----------+

// You can use 'lineSep' option to define the line separator.
// The line separator handles all `\r`, `\r\n` and `\n` by default.
Dataset<Row> df2 = spark.read().option("lineSep", ",").text(path);
df2.show();
// +-----------+
// |      value|
// +-----------+
// |    Michael|
// |   29\nAndy|
// | 30\nJustin|
// |       19\n|
// +-----------+

// You can also use 'wholetext' option to read each input file as a single row.
Dataset<Row> df3 = spark.read().option("wholetext", "true").text(path);
df3.show();
//  +--------------------+
//  |               value|
//  +--------------------+
//  |Michael, 29\nAndy...|
//  +--------------------+

// "output" is a folder which contains multiple text files and a _SUCCESS file.
df1.write().text("output");

// You can specify the compression format using the 'compression' option.
df1.write().option("compression", "gzip").text("output_compressed");
```
> Find full example code at "examples/src/main/java/org/apache/spark/examples/sql/JavaSQLDataSourceExample.java" in the Spark repo.

```python
# spark is from the previous example
sc = spark.sparkContext

# A text dataset is pointed to by path.
# The path can be either a single text file or a directory of text files
path = "examples/src/main/resources/people.txt"

df1 = spark.read.text(path)
df1.show()
# +-----------+
# |      value|
# +-----------+
# |Michael, 29|
# |   Andy, 30|
# | Justin, 19|
# +-----------+

# You can use 'lineSep' option to define the line separator.
# The line separator handles all `\r`, `\r\n` and `\n` by default.
df2 = spark.read.text(path, lineSep=",")
df2.show()
# +-----------+
# |      value|
# +-----------+
# |    Michael|
# |   29\nAndy|
# | 30\nJustin|
# |       19\n|
# +-----------+

# You can also use 'wholetext' option to read each input file as a single row.
df3 = spark.read.text(path, wholetext=True)
df3.show()
# +--------------------+
# |               value|
# +--------------------+
# |Michael, 29\nAndy...|
# +--------------------+

# "output" is a folder which contains multiple text files and a _SUCCESS file.
df1.write.csv("output")

# You can specify the compression format using the 'compression' option.
df1.write.text("output_compressed", compression="gzip")
```
> Find full example code at "examples/src/main/python/sql/datasource.py" in the Spark repo.

## Data Source Option

> Data source options of text can be set via:

- the .option/.options methods of
	- DataFrameReader
	- DataFrameWriter
	- DataStreamReader
	- DataStreamWriter

- the built-in functions below
	- from_csv
	- to_csv
	- schema_of_csv

- `OPTIONS` clause at [`CREATE TABLE USING DATA_SOURCE`](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-ddl-create-table-datasource.html)

Property Name | Default | Meaning | Scope
---|:---|:---|:---
`wholetext`	|`false` | If true, read each file from input path(s) as a single row.【如果是true,那么将从输入目录中读取的每个文件作为一个row】	|read
`lineSep` | `\r`, `\r\n`, `\n` (for reading), `\n` (for writing) | Defines the line separator that should be used for reading or writing. 【定义行分隔符】 | read/write
`compression` | (none)	|Compression codec to use when saving to file. This can be one of the known case-insensitive shorten names (none, bzip2, gzip, lz4, snappy and deflate).【保存文件时使用的压缩格式】| write

> Other generic options can be found in [Generic File Source Options](https://spark.apache.org/docs/latest/sql-data-sources-generic-options.html).