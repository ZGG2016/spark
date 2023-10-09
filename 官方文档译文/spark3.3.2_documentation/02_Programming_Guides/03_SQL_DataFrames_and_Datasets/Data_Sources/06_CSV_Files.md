# CSV Files

> Spark SQL provides `spark.read().csv("file_name")` to read a file or directory of files in CSV format into Spark DataFrame, and `dataframe.write().csv("path")` to write to a CSV file. Function `option()` can be used to customize the behavior of reading or writing, such as controlling behavior of the header, delimiter character, character set, and so on.

使用 `spark.read().csv("file_name")` 读取 CSV 格式的文件或目录，返回 DataFrame. 使用 `dataframe.write().csv("path")` 将数据写入到 CSV 文件。

函数 `option()` 用来个性化读写行为，例如控制表头、分隔符、字符集等。

```scala
// A CSV dataset is pointed to by path.
// The path can be either a single CSV file or a directory of CSV files
val path = "examples/src/main/resources/people.csv"

val df = spark.read.csv(path)
df.show()
// +------------------+
// |               _c0|
// +------------------+
// |      name;age;job|
// |Jorge;30;Developer|
// |  Bob;32;Developer|
// +------------------+

// Read a csv with delimiter, the default delimiter is ","
val df2 = spark.read.option("delimiter", ";").csv(path)
df2.show()
// +-----+---+---------+
// |  _c0|_c1|      _c2|
// +-----+---+---------+
// | name|age|      job|
// |Jorge| 30|Developer|
// |  Bob| 32|Developer|
// +-----+---+---------+

// Read a csv with delimiter and a header
val df3 = spark.read.option("delimiter", ";").option("header", "true").csv(path)
df3.show()
// +-----+---+---------+
// | name|age|      job|
// +-----+---+---------+
// |Jorge| 30|Developer|
// |  Bob| 32|Developer|
// +-----+---+---------+

// You can also use options() to use multiple options
val df4 = spark.read.options(Map("delimiter"->";", "header"->"true")).csv(path)

// "output" is a folder which contains multiple csv files and a _SUCCESS file.
df3.write.csv("output")

// Read all files in a folder, please make sure only CSV files should present in the folder.
val folderPath = "examples/src/main/resources";
val df5 = spark.read.csv(folderPath);
df5.show();
// Wrong schema because non-CSV files are read
// +-----------+
// |        _c0|
// +-----------+
// |238val_238|
// |  86val_86|
// |311val_311|
// |  27val_27|
// |165val_165|
// +-----------+
```
> Find full example code at "examples/src/main/scala/org/apache/spark/examples/sql/SQLDataSourceExample.scala" in the Spark repo.

```java
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

// A CSV dataset is pointed to by path.
// The path can be either a single CSV file or a directory of CSV files
String path = "examples/src/main/resources/people.csv";

Dataset<Row> df = spark.read().csv(path);
df.show();
// +------------------+
// |               _c0|
// +------------------+
// |      name;age;job|
// |Jorge;30;Developer|
// |  Bob;32;Developer|
// +------------------+

// Read a csv with delimiter, the default delimiter is ","
Dataset<Row> df2 = spark.read().option("delimiter", ";").csv(path);
df2.show();
// +-----+---+---------+
// |  _c0|_c1|      _c2|
// +-----+---+---------+
// | name|age|      job|
// |Jorge| 30|Developer|
// |  Bob| 32|Developer|
// +-----+---+---------+

// Read a csv with delimiter and a header
Dataset<Row> df3 = spark.read().option("delimiter", ";").option("header", "true").csv(path);
df3.show();
// +-----+---+---------+
// | name|age|      job|
// +-----+---+---------+
// |Jorge| 30|Developer|
// |  Bob| 32|Developer|
// +-----+---+---------+

// You can also use options() to use multiple options
java.util.Map<String, String> optionsMap = new java.util.HashMap<String, String>();
optionsMap.put("delimiter",";");
optionsMap.put("header","true");
Dataset<Row> df4 = spark.read().options(optionsMap).csv(path);

// "output" is a folder which contains multiple csv files and a _SUCCESS file.
df3.write().csv("output");

// Read all files in a folder, please make sure only CSV files should present in the folder.
String folderPath = "examples/src/main/resources";
Dataset<Row> df5 = spark.read().csv(folderPath);
df5.show();
// Wrong schema because non-CSV files are read
// +-----------+
// |        _c0|
// +-----------+
// |238val_238|
// |  86val_86|
// |311val_311|
// |  27val_27|
// |165val_165|
// +-----------+
```
> Find full example code at "examples/src/main/java/org/apache/spark/examples/sql/JavaSQLDataSourceExample.java" in the Spark repo.

```python
# spark is from the previous example
sc = spark.sparkContext

# A CSV dataset is pointed to by path.
# The path can be either a single CSV file or a directory of CSV files
path = "examples/src/main/resources/people.csv"

df = spark.read.csv(path)
df.show()
# +------------------+
# |               _c0|
# +------------------+
# |      name;age;job|
# |Jorge;30;Developer|
# |  Bob;32;Developer|
# +------------------+

# Read a csv with delimiter, the default delimiter is ","
df2 = spark.read.option("delimiter", ";").csv(path)
df2.show()
# +-----+---+---------+
# |  _c0|_c1|      _c2|
# +-----+---+---------+
# | name|age|      job|
# |Jorge| 30|Developer|
# |  Bob| 32|Developer|
# +-----+---+---------+

# Read a csv with delimiter and a header
df3 = spark.read.option("delimiter", ";").option("header", True).csv(path)
df3.show()
# +-----+---+---------+
# | name|age|      job|
# +-----+---+---------+
# |Jorge| 30|Developer|
# |  Bob| 32|Developer|
# +-----+---+---------+

# You can also use options() to use multiple options
df4 = spark.read.options(delimiter=";", header=True).csv(path)

# "output" is a folder which contains multiple csv files and a _SUCCESS file.
df3.write.csv("output")

# Read all files in a folder, please make sure only CSV files should present in the folder.
folderPath = "examples/src/main/resources"
df5 = spark.read.csv(folderPath)
df5.show()
# Wrong schema because non-CSV files are read
# +-----------+
# |        _c0|
# +-----------+
# |238val_238|
# |  86val_86|
# |311val_311|
# |  27val_27|
# |165val_165|
# +-----------+
```
> Find full example code at "examples/src/main/python/sql/datasource.py" in the Spark repo.

## Data Source Option

> Data source options of CSV can be set via:

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
`sep` | `,`	| Sets a separator for each field and value. This separator can be one or more characters. 【字段间和值之间的分隔符。可以是一个字符或多个字符】| read/write
`encoding` | UTF-8 | For reading, decodes the CSV files by the given encoding type. For writing, specifies encoding (charset) of saved CSV files. CSV built-in functions ignore this option. 【编解码格式】 | read/write
`quote`	| `"` | Sets a single character used for escaping quoted values where the separator can be part of the value. For reading, if you would like to turn off quotations, you need to set not `null` but an empty string. For writing, if an empty string is set, it uses `u0000` (null character). 【设置一个单个字符，用来转义带引号的值】| read/write
`quoteAll` | `false` | A flag indicating whether all values should always be enclosed in quotes. Default is to only escape values containing a quote character. 【默认仅转义包含在引号里的值，设为true，把所有值包含在引号里，转义所有】	| write
`escape` | `\` | Sets a single character used for escaping quotes inside an already quoted value. 【用来转义引号的单个字符，这个引号已存在于带引号的值里面】| read/write
`escapeQuotes` | `true`	| A flag indicating whether values containing quotes should always be enclosed in quotes. Default is to escape all values containing a quote character.	【默认转义所有包含引号字符的值】 | write
`comment` | | Sets a single character used for skipping lines beginning with this character. By default, it is disabled.【一个单个字符，用来跳过所有以这个字符开始的行】 | read
`header` | `false` | For reading, uses the first line as names of columns. For writing, writes the names of columns as the first line. Note that if the given path is a RDD of Strings, this header option will remove all lines same with the header if exists. CSV built-in functions ignore this option.	【读数据时，使用第一行作为列名。写数据时，将列名写成第一行。如果给定的路径是字符串RDD，这个header选项将移出所有和header相同的列。】 | read/write
`inferSchema` | `false`	 | Infers the input schema automatically from data. It requires one extra pass over the data. CSV built-in functions ignore this option. 【自动从数据推断输入结构。它需要对数据进行一次额外的传递。】| read
`enforceSchema` | `true` | If it is set to `true`, the specified or inferred schema will be forcibly applied to datasource files, and headers in CSV files will be ignored. If the option is set to `false`, the schema will be validated against all headers in CSV files in the case when the `header` option is set to `true`. Field names in the schema and column names in CSV headers are checked by their positions taking into account `spark.sql.caseSensitive`. Though the default value is `true`, it is recommended to disable the `enforceSchema` option to avoid incorrect results. CSV built-in functions ignore this option.<br/> 【如果设为true，指定或推断出的结构将被强制应用到数据源文件，CSV文件中的header将被忽略。如果设为false,且header设为true时，将需要验证结构。结构中的字段名和CSV header中的列名根据它们的位置检查，同时考虑属性`spark.sql.caseSensitive`。尽管默认值是true，但仍推荐禁用这个选项。】| read
`ignoreLeadingWhiteSpace` | `false` (for reading), `true` (for writing)	|A flag indicating whether or not leading whitespaces from values being read/written should be skipped.	【是否忽略开头的空格】 |read/write
`ignoreTrailingWhiteSpace` | `false` (for reading), `true` (for writing)|A flag indicating whether or not trailing whitespaces from values being read/written should be skipped. 【是否忽略结尾的空格】|read/write
`nullValue`	| |Sets the string representation of a null value. Since 2.0.1, this `nullValue` param applies to all supported types including the string type. 【null值的字符串表示】|read/write
`nanValue`	| NaN | Sets the string representation of a non-number value.【非数字值的字符串表示】|read
`positiveInf` | Inf	| Sets the string representation of a positive infinity value.	【正无穷的字符串表示】|read
`negativeInf` | -Inf | Sets the string representation of a negative infinity value.【副无穷的字符串表示】|read
`dateFormat` | `yyyy-MM-dd`	| Sets the string that indicates a date format. Custom date formats follow the formats at [Datetime Patterns](https://spark.apache.org/docs/latest/sql-ref-datetime-pattern.html). This applies to date type.	【表示日期格式的字符串】| read/write
`timestampFormat` | `yyyy-MM-dd'T'HH:mm:ss[.SSS][XXX]`	| Sets the string that indicates a timestamp format. Custom date formats follow the formats at [Datetime Patterns](https://spark.apache.org/docs/latest/sql-ref-datetime-pattern.html). This applies to timestamp type. 【表示时间戳格式的字符串】| read/write
`timestampNTZFormat` | `yyyy-MM-dd'T'HH:mm:ss[.SSS]` | Sets the string that indicates a timestamp without timezone format. Custom date formats follow the formats at [Datetime Patterns](https://spark.apache.org/docs/latest/sql-ref-datetime-pattern.html). This applies to timestamp without timezone type, note that zone-offset and time-zone components are not supported when writing or reading this data type.  【表示时间戳(不含时区)格式的字符串】| read/write
`maxColumns` | 20480 | Defines a hard limit of how many columns a record can have.【定义一条记录可以有多少列】 |read
`maxCharsPerColumn`| -1	| Defines the maximum number of characters allowed for any given value being read. By default, it is -1 meaning unlimited length 【读取的给定值包含的最大字符数量。默认是-1，表示无限长度】| read
`mode` | `PERMISSIVE` | Allows a mode for dealing with corrupt records during parsing. It supports the following case-insensitive modes. 【在解析记录时，处理损坏记录的模式】 <br/>  Note that Spark tries to parse only required columns in CSV under column pruning. Therefore, corrupt records can be different based on required set of fields. This behavior can be controlled by `spark.sql.csv.parser.columnPruning.enabled` (enabled by default).【注意sprk尝试仅解析要求的列。因此损坏的记录如果在要求的字段集中，情况就不同了，此行为由`spark.sql.csv.parser.columnPruning.enabled`控制】 <br/> `PERMISSIVE`: when it meets a corrupted record, puts the malformed string into a field configured by `columnNameOfCorruptRecord`, and sets malformed fields to null. To keep corrupt records, an user can set a string type field named `columnNameOfCorruptRecord` in an user-defined schema. If a schema does not have the field, it drops corrupt records during parsing. A record with less/more tokens than schema is not a corrupted record to CSV. When it meets a record having fewer tokens than the length of the schema, sets `null` to extra fields. When the record has more tokens than the length of the schema, it drops extra tokens.【将畸形字符串放入到由`columnNameOfCorruptRecord`配置的一个字段里，并设置未null。为了保存损坏的记录，用户可以在结构中设置一个名为`columnNameOfCorruptRecord`的字符串类型的字段。如果结构中没有这个字段，就会在解析时删除损坏的记录。当一条记录具有比结构更少或更多的符号时，对CSV来说，它并不是一条损坏的记录。当一条记录拥有的符号比结构的长度要少时，将null设置为多出的字段。当多时，它会删除多的符号.】 <br/>`DROPMALFORMED`: ignores the whole corrupted records. This mode is unsupported in the CSV built-in functions.【忽略整个损坏的记录】 <br/>`FAILFAST`: throws an exception when it meets corrupted records. 【当遇到损坏记录时，抛异常】 | read
`columnNameOfCorruptRecord` |(value of `spark.sql.columnNameOfCorruptRecord` configuration) | Allows renaming the new field having malformed string created by `PERMISSIVE` mode. This overrides `spark.sql.columnNameOfCorruptRecord`.【在`PERMISSIVE`模式下，创建的具有畸形字符串的新字段。这会覆盖`spark.sql.columnNameOfCorruptRecord`】  | read
`multiLine`	| false	| Parse one record, which may span multiple lines, per file. CSV built-in functions ignore this option.【解析文件中一条跨多行的记录】| read
`charToEscapeQuoteEscaping`	|`escape` or `\0` | Sets a single character used for escaping the escape for the quote character. The default value is escape character when escape and quote characters are different, `\0` otherwise. | read/write
`samplingRatio`	| 1.0 | Defines fraction of rows used for schema inferring. CSV built-in functions ignore this option.【为推断结构，定义行的比例】 | read
`emptyValue` | (for reading), `""` (for writing) | Sets the string representation of an empty value. 【设置空值的字符串表示】| read/write
`locale` | en-US | Sets a locale as language tag in IETF BCP 47 format. For instance, this is used while parsing dates and timestamps.【设置语言】| read
`lineSep` | `\r`, `\r\n` and `\n` (for reading), `\n` (for writing)	| Defines the line separator that should be used for parsing/writing. Maximum length is 1 character. CSV built-in functions ignore this option.	【定义行分隔符】| read/write
`unescapedQuoteHandling` | `STOP_AT_DELIMITER` | Defines how the `CsvParser` will handle values with unescaped quotes.【定义如何处理未转义的引号】<br/> `STOP_AT_CLOSING_QUOTE`: If unescaped quotes are found in the input, accumulate the quote character and proceed parsing the value as a quoted value, until a closing quote is found.【如果是在输入中发现，会积累引号字符，将其作为加引号的值持续处理，直到找到关闭的引号（右侧引号）】<br/> `BACK_TO_DELIMITER`: If unescaped quotes are found in the input, consider the value as an unquoted value. This will make the parser accumulate all characters of the current parsed value until the delimiter is found. If no delimiter is found in the value, the parser will continue accumulating characters from the input until a delimiter or line ending is found.【如果是在输入中发现，将其作为未加引号的值处理。这将使解析器一直积累当前值的字符，直到发现分隔符。如果没有找到分隔符，将会持续积累字符，直到找到分隔符或行结尾。】<br/> `STOP_AT_DELIMITER`: If unescaped quotes are found in the input, consider the value as an unquoted value. This will make the parser accumulate all characters until the delimiter or a line ending is found in the input.【如果是在输入中发现，将其作为未加引号的值处理。这将使解析器持续积累字符，直到找到分隔符或行结尾。】<br/> `SKIP_VALUE`: If unescaped quotes are found in the input, the content parsed for the given value will be skipped and the value set in nullValue will be produced instead.【如果是在输入中发现，那么给定值的内容将被跳过。将使用设置在nullValue的值】<br/>`RAISE_ERROR`: If unescaped quotes are found in the input, a `TextParsingException` will be thrown.【如果是在输入中发现，那么就抛异常】 | read