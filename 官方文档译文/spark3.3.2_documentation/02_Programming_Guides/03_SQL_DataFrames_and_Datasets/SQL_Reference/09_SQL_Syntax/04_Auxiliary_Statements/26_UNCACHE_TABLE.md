# UNCACHE TABLE

[TOC]

## Description

> `UNCACHE TABLE` removes the entries and associated data from the in-memory and/or on-disk cache for a given table or view. The underlying entries should already have been brought to cache by previous `CACHE TABLE` operation. `UNCACHE TABLE` on a non-existent table throws an exception if `IF EXISTS` is not specified.

基于给定的存储级别，`UNCACHE TABLE` 语句会移除一张表或一个视图缓存在内存和/或磁盘中的条目和相关的数据。

底层的条目应该早已通过 `CACHE TABLE` 操作缓存。

如果在一张不存在的表上使用此命令，且没有指定 `IF EXISTS` 语句，那么就抛异常。

### Syntax

	UNCACHE TABLE [ IF EXISTS ] table_identifier

### Parameters

- table_identifier

	Specifies the table or view name to be uncached. The table or view name may be optionally qualified with a database name.
	
	Syntax: `[ database_name. ] table_name`

## Examples

```sql
UNCACHE TABLE t1;
```