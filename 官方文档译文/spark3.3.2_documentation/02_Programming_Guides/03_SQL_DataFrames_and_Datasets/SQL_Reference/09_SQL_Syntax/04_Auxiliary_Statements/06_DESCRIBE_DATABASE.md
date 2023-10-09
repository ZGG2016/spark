# DESCRIBE DATABASE

[TOC]

## Description

> `DESCRIBE DATABASE` statement returns the metadata of an existing database. The metadata information includes database name, database comment, and database location on the filesystem. If the optional `EXTENDED` option is specified, it returns the basic metadata information along with the database properties. The `DATABASE` and `SCHEMA` are interchangeable.

`DESCRIBE DATABASE` 语句返回一个已存在数据库的元数据。元数据信息包括数据库名、数据库评论和数据库在文件系统中的位置。

如果指定了可选的 `EXTENDED`, 就返回基本的元数据信息和数据库属性。

`DATABASE` 和 `SCHEMA` 是可互换的。

### Syntax

	{ DESC | DESCRIBE } DATABASE [ EXTENDED ] db_name

### Parameters

- db_name

	Specifies a name of an existing database or an existing schema in the system. If the name does not exist, an exception is thrown.

## Examples

```SQL
-- Create employees DATABASE
CREATE DATABASE employees COMMENT 'For software companies';

-- Describe employees DATABASE.
-- Returns Database Name, Description and Root location of the filesystem
-- for the employees DATABASE.
DESCRIBE DATABASE employees;
+-------------------------+-----------------------------+
|database_description_item|   database_description_value|
+-------------------------+-----------------------------+
|            Database Name|                    employees|
|              Description|       For software companies|
|                 Location|file:/Users/Temp/employees.db|
+-------------------------+-----------------------------+

-- Create employees DATABASE
CREATE DATABASE employees COMMENT 'For software companies';

-- Alter employees database to set DBPROPERTIES
ALTER DATABASE employees SET DBPROPERTIES ('Create-by' = 'Kevin', 'Create-date' = '09/01/2019');

-- Describe employees DATABASE with EXTENDED option to return additional database properties
DESCRIBE DATABASE EXTENDED employees;
+-------------------------+---------------------------------------------+
|database_description_item|                   database_description_value|
+-------------------------+---------------------------------------------+
|            Database Name|                                    employees|
|              Description|                       For software companies|
|                 Location|                file:/Users/Temp/employees.db|
|               Properties|((Create-by,kevin), (Create-date,09/01/2019))|
+-------------------------+---------------------------------------------+

-- Create deployment SCHEMA
CREATE SCHEMA deployment COMMENT 'Deployment environment';

-- Describe deployment, the DATABASE and SCHEMA are interchangeable, their meaning are the same.
DESC DATABASE deployment;
+-------------------------+------------------------------+
|database_description_item|database_description_value    |
+-------------------------+------------------------------+
|            Database Name|                    deployment|
|              Description|        Deployment environment|
|                 Location|file:/Users/Temp/deployment.db|
+-------------------------+------------------------------+
```