# CACHE TABLE

[TOC]

## Description

> `CACHE TABLE` statement caches contents of a table or output of a query with the given storage level. If a query is cached, then a temp view will be created for this query. This reduces scanning of the original files in future queries.

基于给定的存储级别，`CACHE TABLE` 语句会缓存一张表或一个查询输出的内容。

如果缓存的是一个查询，那么将会为这个查询创建一个临时视图。这会在未来的查询中，减少对原始文件的扫描。

### Syntax

	CACHE [ LAZY ] TABLE table_identifier
    	[ OPTIONS ( 'storageLevel' [ = ] value ) ] [ [ AS ] query ]

### Parameters

- LAZY

	Only cache the table when it is first used, instead of immediately.

	仅在第一个使用表时，缓存表，而不是立即缓存它。

- table_identifier

	Specifies the table or view name to be cached. The table or view name may be optionally qualified with a database name.

	Syntax: `[ database_name. ] table_name`

- OPTIONS ( ‘storageLevel’ [ = ] value )

	`OPTIONS` clause with `storageLevel` key and value pair. A Warning is issued when a key other than `storageLevel` is used. The valid options for `storageLevel` are:

	+ NONE
	+ DISK_ONLY
	+ DISK_ONLY_2
	+ DISK_ONLY_3
	+ MEMORY_ONLY
	+ MEMORY_ONLY_2
	+ MEMORY_ONLY_SER
	+ MEMORY_ONLY_SER_2
	+ MEMORY_AND_DISK
	+ MEMORY_AND_DISK_2
	+ MEMORY_AND_DISK_SER
	+ MEMORY_AND_DISK_SER_2
	+ OFF_HEAP

	An Exception is thrown when an invalid value is set for `storageLevel`. If `storageLevel` is not explicitly set using `OPTIONS` clause, the default `storageLevel` is set to `MEMORY_AND_DISK`.

- query

	A query that produces the rows to be cached. It can be in one of following formats:

	+ a `SELECT` statement
	+ a `TABLE` statement
	+ a `FROM` statement

## Examples

```SQL
CACHE TABLE testCache OPTIONS ('storageLevel' 'DISK_ONLY') SELECT * FROM testData;
```