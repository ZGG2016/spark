# REFRESH

[TOC]

## Description

> `REFRESH` is used to invalidate and refresh all the cached data (and the associated metadata) for all Datasets that contains the given data source path. Path matching is by prefix, i.e. “/” would invalidate everything that is cached.

对于包含给定数据源路径的所有 Datasets，`REFRESH` 用来使所有缓存的数据（和相关的元数据）失效，并重新刷新。

由前缀（例如`/`）匹配的路径将使缓存的所有失效。

### Syntax

	REFRESH resource_path

### Parameters

- resource_path

	The path of the resource that is to be refreshed.

## Examples

```SQL
-- The Path is resolved using the datasource's File Index.
CREATE TABLE test(ID INT) using parquet;
INSERT INTO test SELECT 1000;
CACHE TABLE test;
INSERT INTO test SELECT 100;
REFRESH "hdfs://path/to/table";
```