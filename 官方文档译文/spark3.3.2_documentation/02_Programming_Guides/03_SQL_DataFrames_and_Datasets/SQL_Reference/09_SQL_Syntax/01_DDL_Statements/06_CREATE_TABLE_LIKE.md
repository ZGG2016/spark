# CREATE TABLE LIKE

[TOC]

## Description

> The `CREATE TABLE` statement defines a new table using the definition/metadata of an existing table or view.

使用已存在表或视图的定义/元数据创建新表

### Syntax

	CREATE TABLE [IF NOT EXISTS] table_identifier LIKE source_table_identifier
    	USING data_source
    	[ ROW FORMAT row_format ]
    	[ STORED AS file_format ]
    	[ TBLPROPERTIES ( key1=val1, key2=val2, ... ) ]
    	[ LOCATION path ]

### Parameters

- table_identifier

	Specifies a table name, which may be optionally qualified with a database name.
	
	Syntax: `[ database_name. ] table_name`

- USING data_source

	Data Source is the input format used to create the table. Data source can be CSV, TXT, ORC, JDBC, PARQUET, etc.

- ROW FORMAT

	SERDE is used to specify a custom SerDe or the DELIMITED clause in order to use the native SerDe.

- STORED AS

	File format for table storage, could be TEXTFILE, ORC, PARQUET, etc.

- TBLPROPERTIES

	Table properties that have to be set are specified, such as created.by.user, owner, etc.

- LOCATION

	Path to the directory where table data is stored, which could be a path on distributed storage like HDFS, etc. Location to create an external table.

## Examples

```sql
-- Create table using an existing table
CREATE TABLE Student_Dupli like Student;

-- Create table like using a data source
CREATE TABLE Student_Dupli like Student USING CSV;

-- Table is created as external table at the location specified
CREATE TABLE Student_Dupli like Student location  '/root1/home';

-- Create table like using a rowformat
CREATE TABLE Student_Dupli like Student
    ROW FORMAT DELIMITED FIELDS TERMINATED BY ','
    STORED AS TEXTFILE
    TBLPROPERTIES ('owner'='xxxx');
```