# USE Database

[TOC]

## Description

> `USE` statement is used to set the current database. After the current database is set, the unqualified database artifacts such as tables, functions and views that are referenced by SQLs are resolved from the current database. The default database name is ‘default’.

`USE` 语句用于设置当前数据库

### Syntax

	USE database_name

### Parameter

- database_name

	Name of the database will be used. If the database does not exist, an exception will be thrown.

## Examples

```sql
-- Use the 'userdb' which exists.
USE userdb;

-- Use the 'userdb1' which doesn't exist
USE userdb1;
Error: org.apache.spark.sql.catalyst.analysis.NoSuchDatabaseException: Database 'userdb1' not found;
(state=,code=0)
```