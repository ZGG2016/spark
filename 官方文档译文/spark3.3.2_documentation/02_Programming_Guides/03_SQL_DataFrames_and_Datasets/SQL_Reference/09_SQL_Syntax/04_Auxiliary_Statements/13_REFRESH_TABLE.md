# REFRESH TABLE

[TOC]

## Description

> `REFRESH TABLE` statement invalidates the cached entries, which include data and metadata of the given table or view. The invalidated cache is populated in lazy manner when the cached table or the query associated with it is executed again.

`REFRESH TABLE` 语句使缓存的条目失效，缓存的条目包括给定表或视图的数据和元数据。

当缓存的表或与之相关的查询再次被执行时，失效的缓存以懒加载的方式填充。

### Syntax

	REFRESH [TABLE] table_identifier

### Parameters

- table_identifier

	Specifies a table name, which is either a qualified or unqualified name that designates a table/view. If no database identifier is provided, it refers to a temporary view or a table/view in the current database.
	
	Syntax: `[ database_name. ] table_name`

## Examples

```SQL
-- The cached entries of the table will be refreshed  
-- The table is resolved from the current database as the table name is unqualified.
REFRESH TABLE tbl1;

-- The cached entries of the view will be refreshed or invalidated
-- The view is resolved from tempDB database, as the view name is qualified.
REFRESH TABLE tempDB.view1;   
```