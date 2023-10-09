# CREATE DATASOURCE TABLE

[TOC]

## Description

> The `CREATE TABLE` statement defines a new table using a Data Source.

使用 Data Source 创建新表

### Syntax

	CREATE TABLE [ IF NOT EXISTS ] table_identifier
    	[ ( col_name1 col_type1 [ COMMENT col_comment1 ], ... ) ]
    	USING data_source
    	[ OPTIONS ( key1=val1, key2=val2, ... ) ]
    	[ PARTITIONED BY ( col_name1, col_name2, ... ) ]
    	[ CLUSTERED BY ( col_name3, col_name4, ... ) 
        	[ SORTED BY ( col_name [ ASC | DESC ], ... ) ] 
        	INTO num_buckets BUCKETS ]
    	[ LOCATION path ]
    	[ COMMENT table_comment ]
    	[ TBLPROPERTIES ( key1=val1, key2=val2, ... ) ]
    	[ AS select_statement ]

> Note that, the clauses between the USING clause and the AS SELECT clause can come in as any order. For example, you can write COMMENT table_comment after TBLPROPERTIES.

注意，在 USING 子句和 AS SELECT 子句之间的子句可以以任意顺序出现。例如，你可以先写 TBLPROPERTIES 再写 COMMENT table_comment.

### Parameters

- table_identifier

	Specifies a table name, which may be optionally qualified with a database name.
	
	Syntax: `[ database_name. ] table_name`

- USING data_source

	Data Source is the input format used to create the table. Data source can be CSV, TXT, ORC, JDBC, PARQUET, etc.

- PARTITIONED BY

	Partitions are created on the table, based on the columns specified.

- CLUSTERED BY

	Partitions created on the table will be bucketed into fixed buckets based on the column specified for bucketing.
	
	NOTE: Bucketing is an optimization technique that uses buckets (and bucketing columns) to determine data partitioning and avoid data shuffle.

- SORTED BY

	Specifies an ordering of bucket columns. Optionally, one can use ASC for an ascending order or DESC for a descending order after any column names in the SORTED BY clause. If not specified, ASC is assumed by default.

- INTO num_buckets BUCKETS

	Specifies buckets numbers, which is used in CLUSTERED BY clause.

- LOCATION

	Path to the directory where table data is stored, which could be a path on distributed storage like HDFS, etc.

- COMMENT

	A string literal to describe the table.

- TBLPROPERTIES

	A list of key-value pairs that is used to tag the table definition.

- AS select_statement

	The table is populated using the data from the select statement.

## Data Source Interaction

> A Data Source table acts like a pointer to the underlying data source. For example, you can create a table “foo” in Spark which points to a table “bar” in MySQL using JDBC Data Source. When you read/write table “foo”, you actually read/write table “bar”.

一个 Data Source 表就像是指向了底层数据源的指针。

例如，你可以在 spark 中创建 foo 表，它使用 JDBC Data Source 指向 MySQL 中的 bar 表。当你读写 foo 表时，实际上是读写 bar 表。

> In general `CREATE TABLE` is creating a “pointer”, and you need to make sure it points to something existing. An exception is file source such as parquet, json. If you don’t specify the `LOCATION`, Spark will create a default table location for you.

一般情况下，语句 `CREATE TABLE` 创建一个指针，你需要确保它指向已存在的某物。一个例外就是文件源，例如 parquet、json.

如果你没有指定 `LOCATION`, spark 将为你创建默认表路径。

> For `CREATE TABLE AS SELECT`, Spark will overwrite the underlying data source with the data of the input query, to make sure the table gets created contains exactly the same data as the input query.

对于 `CREATE TABLE AS SELECT`, spark 将使用输入查询的数据覆盖底层数据源，以确保创建的表精确包含输入查询的数据。

## Examples

```sql
--Use data source
CREATE TABLE student (id INT, name STRING, age INT) USING CSV;

--Use data from another table
CREATE TABLE student_copy USING CSV
    AS SELECT * FROM student;
  
--Omit the USING clause, which uses the default data source (parquet by default)
CREATE TABLE student (id INT, name STRING, age INT);

--Specify table comment and properties
CREATE TABLE student (id INT, name STRING, age INT) USING CSV
    COMMENT 'this is a comment'
    TBLPROPERTIES ('foo'='bar');

--Specify table comment and properties with different clauses order
CREATE TABLE student (id INT, name STRING, age INT) USING CSV
    TBLPROPERTIES ('foo'='bar')
    COMMENT 'this is a comment';

--Create partitioned and bucketed table
CREATE TABLE student (id INT, name STRING, age INT)
    USING CSV
    PARTITIONED BY (age)
    CLUSTERED BY (Id) INTO 4 buckets;

--Create partitioned and bucketed table through CTAS
CREATE TABLE student_partition_bucket
    USING parquet
    PARTITIONED BY (age)
    CLUSTERED BY (id) INTO 4 buckets
    AS SELECT * FROM student;

--Create bucketed table through CTAS and CTE
CREATE TABLE student_bucket
    USING parquet
    CLUSTERED BY (id) INTO 4 buckets (
    WITH tmpTable AS (
        SELECT * FROM student WHERE id > 100
    )
    SELECT * FROM tmpTable
);
```