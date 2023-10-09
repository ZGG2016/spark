# INSERT OVERWRITE DIRECTORY

[TOC]

## Description

> The `INSERT OVERWRITE DIRECTORY` statement overwrites the existing data in the directory with the new values using either spark file format or Hive Serde. Hive support must be enabled to use Hive Serde. The inserted rows can be specified by value expressions or result from a query.

`INSERT OVERWRITE DIRECTORY` 语句会使用新值覆盖目录中的已存在数据，要么是 spark 文件格式，要么是 Hive Serde.

必须启用 hive 支持来使用 Hive Serde.

插入的行可以由表达式或一个查询指定。

### Syntax

	INSERT OVERWRITE [ LOCAL ] DIRECTORY [ directory_path ]
    	{ spark_format | hive_format }
    	{ VALUES ( { value | NULL } [ , ... ] ) [ , ( ... ) ] | query }

While `spark_format` is defined as

	USING file_format [ OPTIONS ( key = val [ , ... ] ) ]

`hive_format` is defined as

	[ ROW FORMAT row_format ] [ STORED AS hive_serde ]

### Parameters

- directory_path

	Specifies the destination directory. The `LOCAL` keyword is used to specify that the directory is on the local file system. In spark file format, it can also be specified in `OPTIONS` using `path`, but `directory_path` and `path` option can not be both specified.

- file_format

	Specifies the file format to use for the insert. Valid options are TEXT, CSV, JSON, JDBC, PARQUET, ORC, HIVE, LIBSVM, or a fully qualified class name of a custom implementation of `org.apache.spark.sql.execution.datasources.FileFormat`.

- OPTIONS ( key = val [ , … ] )

	Specifies one or more options for the writing of the file format.

- hive_format

	Specifies the file format to use for the insert. Both `row_format` and `hive_serde` are optional. `ROW FORMAT SERDE` can only be used with TEXTFILE, SEQUENCEFILE, or RCFILE, while `ROW FORMAT DELIMITED` can only be used with TEXTFILE. If both are not defined, spark uses TEXTFILE.

- row_format

	Specifies the row format for this insert. Valid options are `SERDE` clause and `DELIMITED` clause. `SERDE` clause can be used to specify a custom `SerDe` for this insert. Alternatively, `DELIMITED` clause can be used to specify the native `SerDe` and state the delimiter, escape character, null character, and so on.

- hive_serde

	Specifies the file format for this insert. Valid options are TEXTFILE, SEQUENCEFILE, RCFILE, ORC, PARQUET, and AVRO. You can also specify your own input and output format using `INPUTFORMAT` and `OUTPUTFORMAT`.

- VALUES ( { value | NULL } [ , … ] ) [ , ( … ) ]

	Specifies the values to be inserted. Either an explicitly specified value or a NULL can be inserted. A comma must be used to separate each value in the clause. More than one set of values can be specified to insert multiple rows.

- query

	A query that produces the rows to be inserted. It can be in one of following formats:
	
	- [SELECT](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-qry-select.html) statement
	- [Inline Table](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-qry-select-inline-table.html) statement
	- FROM statement
	
## Examples

### Spark format

```sql
INSERT OVERWRITE DIRECTORY '/tmp/destination'
    USING parquet
    OPTIONS (col1 1, col2 2, col3 'test')
    SELECT * FROM test_table;

INSERT OVERWRITE DIRECTORY
    USING parquet
    OPTIONS ('path' '/tmp/destination', col1 1, col2 2, col3 'test')
    SELECT * FROM test_table;
```

### Hive format

```sql
INSERT OVERWRITE LOCAL DIRECTORY '/tmp/destination'
    STORED AS orc
    SELECT * FROM test_table;

INSERT OVERWRITE LOCAL DIRECTORY '/tmp/destination'
    ROW FORMAT DELIMITED FIELDS TERMINATED BY ','
    SELECT * FROM test_table;
```