# CREATE DATABASE

[TOC]

## Description

> Creates a database with the specified name. If database with the same name already exists, an exception will be thrown.

使用指定名称创建一个数据库。

如果同名数据库已存在，将抛出异常。

### Syntax

	CREATE { DATABASE | SCHEMA } [ IF NOT EXISTS ] database_name
	    [ COMMENT database_comment ]
	    [ LOCATION database_directory ]
	    [ WITH DBPROPERTIES ( property_name = property_value [ , ... ] ) ]

### Parameters

- database_name

	Specifies the name of the database to be created.

- IF NOT EXISTS

	Creates a database with the given name if it does not exist. If a database with the same name already exists, nothing will happen.

- database_directory

	Path of the file system in which the specified database is to be created. If the specified path does not exist in the underlying file system, this command creates a directory with the path. If the location is not specified, the database will be created in the default warehouse directory, whose path is configured by the static configuration `spark.sql.warehouse.dir`.

- database_comment

	Specifies the description for the database.

- WITH DBPROPERTIES ( property_name=property_value [ , … ] )

	Specifies the properties for the database in key-value pairs.

## Examples

```sql
-- Create database `customer_db`. This throws exception if database with name customer_db
-- already exists.
CREATE DATABASE customer_db;

-- Create database `customer_db` only if database with same name doesn't exist.
CREATE DATABASE IF NOT EXISTS customer_db;

-- Create database `customer_db` only if database with same name doesn't exist with 
-- `Comments`,`Specific Location` and `Database properties`.
CREATE DATABASE IF NOT EXISTS customer_db COMMENT 'This is customer database' LOCATION '/user'
    WITH DBPROPERTIES (ID=001, Name='John');

-- Verify that properties are set.
DESCRIBE DATABASE EXTENDED customer_db;
+-------------------------+--------------------------+
|database_description_item|database_description_value|
+-------------------------+--------------------------+
|            Database Name|               customer_db|
|              Description| This is customer database|
|                 Location|     hdfs://hacluster/user|
|               Properties|   ((ID,001), (Name,John))|
+-------------------------+--------------------------+
```