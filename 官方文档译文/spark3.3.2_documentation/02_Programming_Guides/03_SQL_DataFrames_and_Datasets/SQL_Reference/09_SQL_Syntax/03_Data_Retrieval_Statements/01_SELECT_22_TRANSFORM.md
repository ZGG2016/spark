# TRANSFORM

[TOC]

## Description

> The `TRANSFORM` clause is used to specify a Hive-style transform query specification to transform the inputs by running a user-specified command or script.

`TRANSFORM` 用于指定一个 hive 风格的转换查询规范，通过用户指定的命令或脚本转换输入。

> Spark’s script transform supports two modes:

spark 脚本转换支持如下两种模式：

- 禁用 hive 支持

	通过设置 `spark.sql.catalogImplementation=in-memory` 参数或不启用 `SparkSession.builder.enableHiveSupport()` 选项，运行 spark 脚本转换。

	在这种情况下，spark仅使用具有 `ROW FORMAT DELIMITED` 的脚本转换，将传给脚本的所有值当作字符串。

- 启用 hive 支持

	当设置 `spark.sql.catalogImplementation=hive` 参数或启用 `SparkSession.builder.enableHiveSupport()` 选项时，spark 使用脚本转换，同时具有 Hive SerDe 和 `ROW FORMAT DELIMITED`. 

> Hive support disabled: Spark script transform can run with `spark.sql.catalogImplementation=in-memory` or without `SparkSession.builder.enableHiveSupport()`. In this case, now Spark only uses the script transform with `ROW FORMAT DELIMITED` and treats all values passed to the script as strings.
> Hive support enabled: When Spark is run with `spark.sql.catalogImplementation=hive` or Spark SQL is started with `SparkSession.builder.enableHiveSupport()`, Spark can use the script transform with both Hive SerDe and `ROW FORMAT DELIMITED`.

### Syntax

	SELECT TRANSFORM ( expression [ , ... ] )
    	[ ROW FORMAT row_format ]
    	[ RECORDWRITER record_writer_class ]
    	USING command_or_script [ AS ( [ col_name [ col_type ] ] [ , ... ] ) ]
    	[ ROW FORMAT row_format ]
    	[ RECORDREADER record_reader_class ]

### Parameters

- expression

	Specifies a combination of one or more values, operators and SQL functions that results in a value.

	指定一个或多个值、操作符和 SQL 函数的组合

- row_format

	Specifies the row format for input and output. See [HIVE FORMAT](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-hive-format.html) for more syntax details.

	指定输入和输出的 row format

- RECORDWRITER

	Specifies a fully-qualified class name of a custom RecordWriter. The default value is `org.apache.hadoop.hive.ql.exec.TextRecordWriter`.

	指定一个自定义的 RecordWriter 的全限定类名。

	默认值是 `org.apache.hadoop.hive.ql.exec.TextRecordWriter`.

- RECORDREADER

	Specifies a fully-qualified class name of a custom RecordReader. The default value is `org.apache.hadoop.hive.ql.exec.TextRecordReader`.

	指定一个自定义的 RecordReader 的全限定类名。

	默认值是 `org.apache.hadoop.hive.ql.exec.TextRecordReader`.

- command_or_script

	Specifies a command or a path to script to process data.

	指定一个处理数据的命令或指向脚本的路径。

## ROW FORMAT DELIMITED BEHAVIOR

When Spark uses `ROW FORMAT DELIMITED` format:

> Spark uses the character `\u0001` as the default field delimiter and this delimiter can be overridden by `FIELDS TERMINATED BY`.

- spark 使用 `\u0001` 字符作为默认字段分隔符，这个分隔符可以使用 `FIELDS TERMINATED BY` 覆盖。

> Spark uses the character `\n` as the default line delimiter and this delimiter can be overridden by `LINES TERMINATED BY`.

- spark 使用 `\n` 字符作为默认行分隔符，这个分隔符可以使用 `LINES TERMINATED BY` 覆盖。

> Spark uses a string `\N` as the default `NULL` value in order to differentiate `NULL` values from the literal string `NULL`. This delimiter can be overridden by `NULL DEFINED AS`.

- spark 使用字符串 `\N` 作为默认的 `NULL` 值，为了区分 `NULL` 和字面量字符串 `NULL`. 这个分隔符可以使用 `NULL DEFINED AS` 覆盖。 

> Spark casts all columns to `STRING` and combines columns by tabs before feeding to the user script. For complex types such as `ARRAY/MAP/STRUCT`, Spark uses `to_json` casts it to an input `JSON` string and uses `from_json` to convert the result output `JSON` string to `ARRAY/MAP/STRUCT` data.

- spark 将所有列转换为 `STRING`，在传给用户脚本前，按制表符组合列。对于复杂类型，spark 使用 `to_json` 将其转成输入 `JSON` 字符串，使用 `from_json` 将结果输出 `JSON` 字符串转成 `ARRAY/MAP/STRUCT` 数据。

> `COLLECTION ITEMS TERMINATED BY` and `MAP KEYS TERMINATED BY` are delimiters to split complex data such as `ARRAY/MAP/STRUCT`, Spark uses `to_json` and `from_json` to handle complex data types with `JSON` format. So `COLLECTION ITEMS TERMINATED BY` and `MAP KEYS TERMINATED BY` won’t work in default row format.

- `COLLECTION ITEMS TERMINATED BY` 和 `MAP KEYS TERMINATED BY` 用来划分诸如 `ARRAY/MAP/STRUCT` 复杂数据类型的数据。使用 `to_json` 和 `from_json` 处理 `JSON` 格式的数据。所以，`COLLECTION ITEMS TERMINATED BY` 和 `MAP KEYS TERMINATED BY` 不会和默认行格式一起使用。

> The standard output of the user script is treated as tab-separated `STRING` columns. Any cell containing only a string `\N` is re-interpreted as a literal `NULL` value, and then the resulting `STRING` column will be cast to the data types specified in `col_type`.

- 用户脚本的标准输出被当作制表符划分的 `STRING` 列。任意包含字符串 `\N` 的单元格被重新解释为字面量 `NULL` 值，然后 `STRING` 列将被转换成由 `col_type` 指定的数据类型。

> If the actual number of output columns is less than the number of specified output columns, additional output columns will be filled with `NULL`. For example:

- 如果输出列的实际数量小于指定的输出列的数量，额外的输出列将使用 `NULL` 填充。

```
  output tabs: 1, 2
  output columns: A: INT, B INT, C: INT
  result: 
    +---+---+------+
    |  a|  b|     c|
    +---+---+------+
    |  1|  2|  NULL|
    +---+---+------+
```

> If the actual number of output columns is more than the number of specified output columns, the output columns only select the corresponding columns, and the remaining part will be discarded. For example, if the output has three tabs and there are only two output columns:

- 如果输出列的实际数量大于指定的输出列的数量，那么输出列仅选择对应的列，剩余的部分将被丢弃。

```
  output tabs: 1, 2, 3
  output columns: A: INT, B INT
  result: 
    +---+---+
    |  a|  b|
    +---+---+
    |  1|  2|
    +---+---+
```

> If there is no `AS` clause after `USING my_script`, the output schema is `key: STRING, value: STRING`. The key column contains all the characters before the first tab and the value column contains the remaining characters after the first tab. If there are no tabs, Spark returns the `NULL` value. For example:

- 如果在 `USING my_script` 之后没有 `AS` 子句，输出结构就是 `key: STRING, value: STRING`. key列包含第一个制表符前的所有字符，value列包含第一个制表符之后剩余的字符。如果没有制表符，spark 就返回 `NULL` 值。

```
   output tabs: 1, 2, 3
   output columns: 
   result: 
     +-----+-------+
     |  key|  value|
     +-----+-------+
     |    1|      2|
     +-----+-------+
   
   output tabs: 1, 2
   output columns: 
   result: 
     +-----+-------+
     |  key|  value|
     +-----+-------+
     |    1|   NULL|
     +-----+-------+
```

## Hive SerDe behavior

> When Hive support is enabled and Hive SerDe mode is used:

当启用了 hive 支持，同时使用了 hive SerDe 模式：

> Spark uses the Hive SerDe `org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe` by default, so columns are cast to `STRING` and combined by tabs before feeding to the user script.

- 默认情况下，spark 使用 Hive SerDe `org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe`，所以列被转成 `STRING`，并且在传给用户脚本前，使用制表符组合列。

> All literal `NULL` values are converted to a string `\N` in order to differentiate literal `NULL` values from the literal string `NULL`.

- 所有字面量 `NULL` 值被转成字符串 `\N`, 为了区分字面量 `NULL` 值和字面量字符串 `NULL` 

> The standard output of the user script is treated as tab-separated `STRING` columns, any cell containing only a string `\N` is re-interpreted as a `NULL` value, and then the resulting `STRING` column will be cast to the data type specified in `col_type`.

- 用户脚本的标准输出被当作制表符划分的 `STRING` 列，任意包含字符串 `\N` 的单元格被重新解释为 `NULL` 值，然后 `STRING` 列将被转换成由 `col_type` 指定的数据类型。

> If the actual number of output columns is less than the number of specified output columns, additional output columns will be filled with NULL.

- 如果输出列的实际数量小于指定的输出列的数量，额外的输出列将使用 `NULL` 填充。

> If the actual number of output columns is more than the number of specified output columns, the output columns only select the corresponding columns, and the remaining part will be discarded.

- 如果输出列的实际数量大于指定的输出列的数量，那么输出列仅选择对应的列，剩余的部分将被丢弃。

> If there is no `AS` clause after `USING my_script`, the output schema is `key: STRING, value: STRING`. The key column contains all the characters before the first tab and the value column contains the remaining characters after the first tab. If there is no tab, Spark returns the `NULL` value.

- 如果在 `USING my_script` 之后没有 `AS` 子句，输出结构就是 `key: STRING, value: STRING`. key列包含第一个制表符前的所有字符，value列包含第一个制表符之后剩余的字符。如果没有制表符，spark 就返回 `NULL` 值。

> These defaults can be overridden with `ROW FORMAT SERDE` or `ROW FORMAT DELIMITED`.

- 这些默认值可以使用 `ROW FORMAT SERDE` 或 `ROW FORMAT DELIMITED` 覆盖。

## Examples

```sql
CREATE TABLE person (zip_code INT, name STRING, age INT);
INSERT INTO person VALUES
    (94588, 'Zen Hui', 50),
    (94588, 'Dan Li', 18),
    (94588, 'Anil K', 27),
    (94588, 'John V', NULL),
    (94511, 'David K', 42),
    (94511, 'Aryan B.', 18),
    (94511, 'Lalit B.', NULL);

-- With specified output without data type
SELECT TRANSFORM(zip_code, name, age)
   USING 'cat' AS (a, b, c)
FROM person
WHERE zip_code > 94511;
+-------+---------+-----+
|    a  |        b|    c|
+-------+---------+-----+
|  94588|   Anil K|   27|
|  94588|   John V| NULL|
|  94588|  Zen Hui|   50|
|  94588|   Dan Li|   18|
+-------+---------+-----+

-- With specified output with data type
SELECT TRANSFORM(zip_code, name, age)
   USING 'cat' AS (a STRING, b STRING, c STRING)
FROM person
WHERE zip_code > 94511;
+-------+---------+-----+
|    a  |        b|    c|
+-------+---------+-----+
|  94588|   Anil K|   27|
|  94588|   John V| NULL|
|  94588|  Zen Hui|   50|
|  94588|   Dan Li|   18|
+-------+---------+-----+

-- Using ROW FORMAT DELIMITED
SELECT TRANSFORM(name, age)
    ROW FORMAT DELIMITED
    FIELDS TERMINATED BY ','
    LINES TERMINATED BY '\n'
    NULL DEFINED AS 'NULL'
    USING 'cat' AS (name_age string)
    ROW FORMAT DELIMITED
    FIELDS TERMINATED BY '@'
    LINES TERMINATED BY '\n'
    NULL DEFINED AS 'NULL'
FROM person;
+---------------+
|       name_age|
+---------------+
|      Anil K,27|
|    John V,null|
|     ryan B.,18|
|     David K,42|
|     Zen Hui,50|
|      Dan Li,18|
|  Lalit B.,null|
+---------------+

-- Using Hive Serde
SELECT TRANSFORM(zip_code, name, age)
    ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe'
    WITH SERDEPROPERTIES (
      'field.delim' = '\t'
    )
    USING 'cat' AS (a STRING, b STRING, c STRING)
    ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe'
    WITH SERDEPROPERTIES (
      'field.delim' = '\t'
    )
FROM person
WHERE zip_code > 94511;
+-------+---------+-----+
|    a  |        b|    c|
+-------+---------+-----+
|  94588|   Anil K|   27|
|  94588|   John V| NULL|
|  94588|  Zen Hui|   50|
|  94588|   Dan Li|   18|
+-------+---------+-----+

-- Schema-less mode
SELECT TRANSFORM(zip_code, name, age)
    USING 'cat'
FROM person
WHERE zip_code > 94500;
+-------+---------------------+
|    key|                value|
+-------+---------------------+
|  94588|	  Anil K    27|
|  94588|	  John V    \N|
|  94511|	Aryan B.    18|
|  94511|	 David K    42|
|  94588|	 Zen Hui    50|
|  94588|	  Dan Li    18|
|  94511|	Lalit B.    \N|
+-------+---------------------+
```