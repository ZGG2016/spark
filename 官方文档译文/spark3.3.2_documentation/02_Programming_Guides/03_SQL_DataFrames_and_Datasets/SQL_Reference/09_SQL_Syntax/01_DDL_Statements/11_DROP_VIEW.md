# DROP VIEW

[TOC]

## Description

> `DROP VIEW` removes the metadata associated with a specified view from the catalog.

删除此视图相关的元数据

### Syntax

	DROP VIEW [ IF EXISTS ] view_identifier

### Parameter

- IF EXISTS

	If specified, no exception is thrown when the view does not exist.

- view_identifier

	Specifies the view name to be dropped. The view name may be optionally qualified with a database name.
	
	Syntax: `[ database_name. ] view_name`

## Examples

```sql
-- Assumes a view named `employeeView` exists.
DROP VIEW employeeView;

-- Assumes a view named `employeeView` exists in the `userdb` database
DROP VIEW userdb.employeeView;

-- Assumes a view named `employeeView` does not exist.
-- Throws exception
DROP VIEW employeeView;
Error: org.apache.spark.sql.AnalysisException: Table or view not found: employeeView;
(state=,code=0)

-- Assumes a view named `employeeView` does not exist,Try with IF EXISTS
-- this time it will not throw exception
DROP VIEW IF EXISTS employeeView;
```