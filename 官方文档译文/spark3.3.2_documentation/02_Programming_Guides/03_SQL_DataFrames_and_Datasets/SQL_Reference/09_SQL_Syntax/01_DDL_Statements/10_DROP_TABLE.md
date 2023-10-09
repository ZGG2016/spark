# DROP TABLE

[TOC]

## Description

> `DROP TABLE` deletes the table and removes the directory associated with the table from the file system if the table is not EXTERNAL table. If the table is not present it throws an exception.

如果表不是外部表，就删除表，及其和该表相关的目录。

如果表不存在，就抛异常。

> In case of an external table, only the associated metadata information is removed from the metastore database.

对于外部表，仅会删除元数据数据库中的相关元数据信息。

> If the table is cached, the command uncaches the table and all its dependents.

如果表已被缓存，命令会取消缓存表及其所有依赖。

## Syntax

	DROP TABLE [ IF EXISTS ] table_identifier [ PURGE ]

### Parameter

- IF EXISTS

	If specified, no exception is thrown when the table does not exist.

- table_identifier

	Specifies the table name to be dropped. The table name may be optionally qualified with a database name.
	
	Syntax: `[ database_name. ] table_name`

- PURGE

	If specified, completely purge the table skipping trash while dropping table(Note: PURGE available in Hive Metastore 0.14.0 and later).

## Examples

```sql
-- Assumes a table named `employeetable` exists.
DROP TABLE employeetable;

-- Assumes a table named `employeetable` exists in the `userdb` database
DROP TABLE userdb.employeetable;

-- Assumes a table named `employeetable` does not exist.
-- Throws exception
DROP TABLE employeetable;
Error: org.apache.spark.sql.AnalysisException: Table or view not found: employeetable;
(state=,code=0)

-- Assumes a table named `employeetable` does not exist,Try with IF EXISTS
-- this time it will not throw exception
DROP TABLE IF EXISTS employeetable;

-- Completely purge the table skipping trash.
DROP TABLE employeetable PURGE;
```