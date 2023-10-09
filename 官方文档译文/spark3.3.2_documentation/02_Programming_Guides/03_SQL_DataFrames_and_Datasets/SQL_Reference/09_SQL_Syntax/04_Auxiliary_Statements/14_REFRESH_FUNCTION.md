# REFRESH FUNCTION

[TOC]

## Description

> `REFRESH FUNCTION` statement invalidates the cached function entry, which includes a class name and resource location of the given function. The invalidated cache is populated right away. Note that `REFRESH FUNCTION` only works for permanent functions. Refreshing native functions or temporary functions will cause an exception.

`REFRESH FUNCTION` 语句使缓存的函数条目失效，其包含了类名和给定函数的资源路径。

失效的缓存立刻被填充。

注意：此语句仅对持久函数有效。重新刷新原生函数或临时函数将造成异常。

### Syntax

	REFRESH FUNCTION function_identifier

### Parameters

- function_identifier

	Specifies a function name, which is either a qualified or unqualified name. If no database identifier is provided, uses the current database.
	
	Syntax: `[ database_name. ] function_name`

## Examples

```SQL
-- The cached entry of the function will be refreshed
-- The function is resolved from the current database as the function name is unqualified.
REFRESH FUNCTION func1;

-- The cached entry of the function will be refreshed
-- The function is resolved from tempDB database as the function name is qualified.
REFRESH FUNCTION db1.func1;   
```