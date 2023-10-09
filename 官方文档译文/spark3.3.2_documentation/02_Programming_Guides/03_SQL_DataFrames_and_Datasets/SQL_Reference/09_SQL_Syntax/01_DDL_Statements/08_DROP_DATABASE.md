# DROP DATABASE

[TOC]

## Description

> Drop a database and delete the directory associated with the database from the file system. An exception will be thrown if the database does not exist in the system.

删除数据库，及其相关联的文件系统目录。

如果数据库不存在，就会抛出一个异常。

### Syntax

	DROP { DATABASE | SCHEMA } [ IF EXISTS ] dbname [ RESTRICT | CASCADE ]

### Parameters

- DATABASE | SCHEMA

	DATABASE and SCHEMA mean the same thing, either of them can be used.

- IF EXISTS

	If specified, no exception is thrown when the database does not exist.

- RESTRICT

	If specified, will restrict dropping a non-empty database and is enabled by default.

- CASCADE

	If specified, will drop all the associated tables and functions.

## Examples

```sql
-- Create `inventory_db` Database
CREATE DATABASE inventory_db COMMENT 'This database is used to maintain Inventory';

-- Drop the database and it's tables
DROP DATABASE inventory_db CASCADE;

-- Drop the database using IF EXISTS
DROP DATABASE IF EXISTS inventory_db CASCADE;
```