# JSON Files

[TOC]

### Scala

> Spark SQL can automatically infer the schema of a JSON dataset and load it as a `Dataset[Row]`. This conversion can be done using `SparkSession.read.json()` on either a `Dataset[String]`, or a JSON file.

Spark SQL 可以自动推断 JSON dataset 的结构，将其载入为 `Dataset[Row]`.

这个转换在执行 `SparkSession.read.json()` 时完成。

> Note that the file that is offered as a `json file` is not a typical JSON file. Each line must contain a separate, self-contained valid JSON object. For more information, please see [JSON Lines text format, also called newline-delimited JSON](http://jsonlines.org/).

注意，以 `json file` 格式提供的文件并不是典型的 JSON 文件。每行必须包含一个独立的、自包含的有效 JSON 对象。如下：

```
{"name":"Michael"}
{"name":"Andy", "age":30}
{"name":"Justin", "age":19}
```

> For a regular multi-line JSON file, set the `multiLine` option to `true`.

对于常规的多行 JSON 文件，将 `multiLine` 设为 `true`.

```scala
// Primitive types (Int, String, etc) and Product types (case classes) encoders are
// supported by importing this when creating a Dataset.
import spark.implicits._

// A JSON dataset is pointed to by path.
// The path can be either a single text file or a directory storing text files
val path = "examples/src/main/resources/people.json"
val peopleDF = spark.read.json(path)

// The inferred schema can be visualized using the printSchema() method
peopleDF.printSchema()
// root
//  |-- age: long (nullable = true)
//  |-- name: string (nullable = true)

// Creates a temporary view using the DataFrame
peopleDF.createOrReplaceTempView("people")

// SQL statements can be run by using the sql methods provided by spark
val teenagerNamesDF = spark.sql("SELECT name FROM people WHERE age BETWEEN 13 AND 19")
teenagerNamesDF.show()
// +------+
// |  name|
// +------+
// |Justin|
// +------+

// Alternatively, a DataFrame can be created for a JSON dataset represented by
// a Dataset[String] storing one JSON object per string
val otherPeopleDataset = spark.createDataset(
  """{"name":"Yin","address":{"city":"Columbus","state":"Ohio"}}""" :: Nil)
val otherPeople = spark.read.json(otherPeopleDataset)
otherPeople.show()
// +---------------+----+
// |        address|name|
// +---------------+----+
// |[Columbus,Ohio]| Yin|
// +---------------+----+
```
> Find full example code at "examples/src/main/scala/org/apache/spark/examples/sql/SQLDataSourceExample.scala" in the Spark repo.

### Java

> Spark SQL can automatically infer the schema of a JSON dataset and load it as a `Dataset<Row>`. This conversion can be done using `SparkSession.read().json()` on either a `Dataset<String>`, or a JSON file.

> Note that the file that is offered as a json file is not a typical JSON file. Each line must contain a separate, self-contained valid JSON object. For more information, please see [JSON Lines text format, also called newline-delimited JSON](http://jsonlines.org/).

> For a regular multi-line JSON file, set the `multiLine` option to `true`.

```java
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

// A JSON dataset is pointed to by path.
// The path can be either a single text file or a directory storing text files
Dataset<Row> people = spark.read().json("examples/src/main/resources/people.json");

// The inferred schema can be visualized using the printSchema() method
people.printSchema();
// root
//  |-- age: long (nullable = true)
//  |-- name: string (nullable = true)

// Creates a temporary view using the DataFrame
people.createOrReplaceTempView("people");

// SQL statements can be run by using the sql methods provided by spark
Dataset<Row> namesDF = spark.sql("SELECT name FROM people WHERE age BETWEEN 13 AND 19");
namesDF.show();
// +------+
// |  name|
// +------+
// |Justin|
// +------+

// Alternatively, a DataFrame can be created for a JSON dataset represented by
// a Dataset<String> storing one JSON object per string.
List<String> jsonData = Arrays.asList(
        "{\"name\":\"Yin\",\"address\":{\"city\":\"Columbus\",\"state\":\"Ohio\"}}");
Dataset<String> anotherPeopleDataset = spark.createDataset(jsonData, Encoders.STRING());
Dataset<Row> anotherPeople = spark.read().json(anotherPeopleDataset);
anotherPeople.show();
// +---------------+----+
// |        address|name|
// +---------------+----+
// |[Columbus,Ohio]| Yin|
// +---------------+----+
```
> Find full example code at "examples/src/main/java/org/apache/spark/examples/sql/JavaSQLDataSourceExample.java" in the Spark repo.

### Python

> Spark SQL can automatically infer the schema of a JSON dataset and load it as a DataFrame. This conversion can be done using `SparkSession.read.json` on a JSON file.

> Note that the file that is offered as a `json file` is not a typical JSON file. Each line must contain a separate, self-contained valid JSON object. For more information, please see [JSON Lines text format, also called newline-delimited JSON](http://jsonlines.org/).

> For a regular multi-line JSON file, set the `multiLine` parameter to `True`.

```python
# spark is from the previous example.
sc = spark.sparkContext

# A JSON dataset is pointed to by path.
# The path can be either a single text file or a directory storing text files
path = "examples/src/main/resources/people.json"
peopleDF = spark.read.json(path)

# The inferred schema can be visualized using the printSchema() method
peopleDF.printSchema()
# root
#  |-- age: long (nullable = true)
#  |-- name: string (nullable = true)

# Creates a temporary view using the DataFrame
peopleDF.createOrReplaceTempView("people")

# SQL statements can be run by using the sql methods provided by spark
teenagerNamesDF = spark.sql("SELECT name FROM people WHERE age BETWEEN 13 AND 19")
teenagerNamesDF.show()
# +------+
# |  name|
# +------+
# |Justin|
# +------+

# Alternatively, a DataFrame can be created for a JSON dataset represented by
# an RDD[String] storing one JSON object per string
jsonStrings = ['{"name":"Yin","address":{"city":"Columbus","state":"Ohio"}}']
otherPeopleRDD = sc.parallelize(jsonStrings)
otherPeople = spark.read.json(otherPeopleRDD)
otherPeople.show()
# +---------------+----+
# |        address|name|
# +---------------+----+
# |[Columbus,Ohio]| Yin|
# +---------------+----+
```
> Find full example code at "examples/src/main/python/sql/datasource.py" in the Spark repo.

### R

> Spark SQL can automatically infer the schema of a JSON dataset and load it as a DataFrame. using the `read.json()` function, which loads data from a directory of JSON files where each line of the files is a JSON object.

> Note that the file that is offered as a `json file` is not a typical JSON file. Each line must contain a separate, self-contained valid JSON object. For more information, please see [JSON Lines text format, also called newline-delimited JSON](http://jsonlines.org/).

> For a regular multi-line JSON file, set a named parameter `multiLine` to `TRUE`.

```r
# A JSON dataset is pointed to by path.
# The path can be either a single text file or a directory storing text files.
path <- "examples/src/main/resources/people.json"
# Create a DataFrame from the file(s) pointed to by path
people <- read.json(path)

# The inferred schema can be visualized using the printSchema() method.
printSchema(people)
## root
##  |-- age: long (nullable = true)
##  |-- name: string (nullable = true)

# Register this DataFrame as a table.
createOrReplaceTempView(people, "people")

# SQL statements can be run by using the sql methods.
teenagers <- sql("SELECT name FROM people WHERE age >= 13 AND age <= 19")
head(teenagers)
##     name
## 1 Justin
```
> Find full example code at "examples/src/main/r/RSparkSQLExample.R" in the Spark repo.

### SQL

```sql
CREATE TEMPORARY VIEW jsonTable
USING org.apache.spark.sql.json
OPTIONS (
  path "examples/src/main/resources/people.json"
)

SELECT * FROM jsonTable
```

## Data Source Option

> Data source options of JSON can be set via:

- the `.option/.options` methods of
	- DataFrameReader
	- DataFrameWriter
	- DataStreamReader
	- DataStreamWriter

- the built-in functions below
	- from_json
	- to_json
	- schema_of_json
	
- OPTIONS clause at [`CREATE TABLE USING DATA_SOURCE`](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-ddl-create-table-datasource.html)

Property Name | Default | Meaning | Scope
---|:---|:---|:---
`timeZone` | (value of `spark.sql.session.timeZone` configuration) | Sets the string that indicates a time zone ID to be used to format timestamps in the JSON datasources or partition values. 【表示时区id的字符串】<br/>The following formats of `timeZone` are supported: Region-based zone ID: It should have the form `'area/city'`, such as `'America/Los_Angeles'`. Zone offset: It should be in the format `'(+|-)HH:mm'`, for example `'-08:00'` or `'+01:00'`. Also `'UTC'` and `'Z'` are supported as aliases of `'+00:00'`. 【支持两种格式：一种区域名称，一种时间偏移量】 <br/>Other short names like `'CST'` are not recommended to use because they can be ambiguous. 【不推荐使用`'CST'`这个短名字，因为语义模糊】| read/write
`primitivesAsString` | `false` | Infers all primitive values as a string type.【将所有原始值当作字符串】 | read
`prefersDecimal` | `false` | Infers all floating-point values as a decimal type. If the values do not fit in decimal, then it infers them as doubles. 【将所有浮点值当作decimal类型。如果不能转入decimal，就当作double】 | read
`allowComments`	| `false` | Ignores Java/C++ style comment in JSON records.	【在json记录中，忽略Java/C++类型的评论】 | read
`allowUnquotedFieldNames` | `false`	| Allows unquoted JSON field names.	【允许未引用的JSON字段名】 | read
`allowSingleQuotes`	 | `true` | Allows single quotes in addition to double quotes. 【允许单引号】	| read
`allowNumericLeadingZero` | `false`	| Allows leading zeros in numbers (e.g. 00012).	【允许数字以0开头】 | read
`allowBackslashEscapingAnyCharacter` | `false` | Allows accepting quoting of all character using backslash quoting mechanism. 【允许使用反斜杠引用】 | read
`mode` | `PERMISSIVE` | Allows a mode for dealing with corrupt records during parsing. 【在解析记录时，处理损坏记录的模式】 <br/>`PERMISSIVE`: when it meets a corrupted record, puts the malformed string into a field configured by `columnNameOfCorruptRecord`, and sets malformed fields to null. To keep corrupt records, an user can set a string type field named `columnNameOfCorruptRecord` in an user-defined schema. If a schema does not have the field, it drops corrupt records during parsing. When inferring a schema, it implicitly adds a `columnNameOfCorruptRecord` field in an output schema.【将畸形字符串放入到由`columnNameOfCorruptRecord`配置的一个字段里，并设置未null。为了保存损坏的记录，用户可以在结构中设置一个名为`columnNameOfCorruptRecord`的字符串类型的字段。如果结构中没有这个字段，就会在解析时删除损坏的记录。当推断一个结构时，会隐式地在输出结构中添加`columnNameOfCorruptRecord`字段】 <br/>`DROPMALFORMED`: ignores the whole corrupted records. This mode is unsupported in the JSON built-in functions. 【忽略整个损坏的记录。这个模式在JSON内建函数中不支持】 <br/>`FAILFAST`: throws an exception when it meets corrupted records.【当遇到损坏记录时，抛异常】 | read
`columnNameOfCorruptRecord`	| (value of `spark.sql.columnNameOfCorruptRecord` configuration) | Allows renaming the new field having malformed string created by `PERMISSIVE` mode. This overrides `spark.sql.columnNameOfCorruptRecord`. 【在`PERMISSIVE`模式下，创建的具有畸形字符串的新字段。这会覆盖`spark.sql.columnNameOfCorruptRecord`】 | read
`dateFormat` | `yyyy-MM-dd`	| Sets the string that indicates a date format. Custom date formats follow the formats at [datetime pattern](https://spark.apache.org/docs/latest/sql-ref-datetime-pattern.html). This applies to date type. 【日期格式】 | read/write
`timestampFormat` | `yyyy-MM-dd'T'HH:mm:ss[.SSS][XXX]` | Sets the string that indicates a timestamp format. Custom date formats follow the formats at [datetime pattern](https://spark.apache.org/docs/latest/sql-ref-datetime-pattern.html). This applies to timestamp type. 【时间戳格式】 | read/write
`timestampNTZFormat` | `yyyy-MM-dd'T'HH:mm:ss[.SSS]`  | Sets the string that indicates a timestamp without timezone format. Custom date formats follow the formats at [Datetime Patterns](https://spark.apache.org/docs/latest/sql-ref-datetime-pattern.html). This applies to timestamp without timezone type, note that zone-offset and time-zone components are not supported when writing or reading this data type. 【不含时区格式的时间戳】 | read/write
`multiLine` | `false` | Parse one record, which may span multiple lines, per file. JSON built-in functions ignore this option. 【解析文件中一条跨多行的记录。JSON内建函数忽略这个选项】 | read
`allowUnquotedControlChars` | `false` | Allows JSON Strings to contain unquoted control characters (ASCII characters with value less than 32, including tab and line feed characters) or not. 【允许JSON字符串包含未引用的控制字符】 | read
`encoding`	| Detected automatically when `multiLine` is set to `true` (for reading), `UTF-8` (for writing) | For reading, allows to forcibly set one of standard basic or extended encoding for the JSON files. For example UTF-16BE, UTF-32LE. For writing, Specifies encoding (charset) of saved json files. JSON built-in functions ignore this option. 【对于读数据，允许强行设置一个基础的或扩展的编码，例如UTF-16BE, UTF-32LE。对于写数据，指定保存的json文件的编码】 | read/write
`lineSep` | `\r`, `\r\n`, `\n` (for reading), `\n`(for writing)	| Defines the line separator that should be used for parsing. JSON built-in functions ignore this option. 【定义行分隔符】 | read/write
`samplingRatio`	| `1.0` | Defines fraction of input JSON objects used for schema inferring. 【为推断结构，定义的输入json对象的比例】 | read
`dropFieldIfAllNull` | `false` | Whether to ignore column of all null values or empty array/struct during schema inference. 【在结构推断期间，是否忽略所有的null值或空数组/结构体】| read
`locale` | `en-US` | Sets a locale as language tag in IETF BCP 47 format. For instance, locale is used while parsing dates and timestamps. 【设置语言】 | read
`allowNonNumericNumbers` | `true` | Allows JSON parser to recognize set of “Not-a-Number” (NaN) tokens as legal floating number values.【允许JSON解析器NaN识别为合法的单浮点数】 <br/> `+INF`: for positive infinity, as well as alias of `+Infinity` and `Infinity`.<br/> `-INF`: for negative infinity, alias `-Infinity`. <br/> `NaN`: for other not-a-numbers, like result of division by zero. | read
`compression` | (none)	| Compression codec to use when saving to file. This can be one of the known case-insensitive shorten names (none, bzip2, gzip, lz4, snappy and deflate). JSON built-in functions ignore this option. 【保存文件时使用的压缩格式】 | write
`ignoreNullFields`	| (value of `spark.sql.jsonGenerator.ignoreNullFields` configuration)	| Whether to ignore null fields when generating JSON objects. 【当生成JSON对象时，是否忽略null字段】 | write

> Other generic options can be found in [Generic File Source Options](https://spark.apache.org/docs/latest/sql-data-sources-generic-options.html).