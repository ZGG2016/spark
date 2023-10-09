# DESCRIBE FUNCTION

[TOC]

## Description

> `DESCRIBE FUNCTION` statement returns the basic metadata information of an existing function. The metadata information includes the function name, implementing class and the usage details. If the optional `EXTENDED` option is specified, the basic metadata information is returned along with the extended usage information.

`DESCRIBE FUNCTION` 语句返回一个已存在函数的元数据。元数据信息包括函数名、实现类和用法细节。

如果指定了可选的 `EXTENDED`, 就返回基本的元数据信息和额外的用法信息。


### Syntax

	{ DESC | DESCRIBE } FUNCTION [ EXTENDED ] function_name

### Parameters

- function_name

	Specifies a name of an existing function in the system. The function name may be optionally qualified with a database name. If `function_name` is qualified with a database then the function is resolved from the user specified database, otherwise it is resolved from the current database.

	Syntax: `[ database_name. ] function_name`

## Examples

```SQL
-- Describe a builtin scalar function.
-- Returns function name, implementing class and usage
DESC FUNCTION abs;
+-------------------------------------------------------------------+
|function_desc                                                      |
+-------------------------------------------------------------------+
|Function: abs                                                      |
|Class: org.apache.spark.sql.catalyst.expressions.Abs               |
|Usage: abs(expr) - Returns the absolute value of the numeric value.|
+-------------------------------------------------------------------+

-- Describe a builtin scalar function.
-- Returns function name, implementing class and usage and examples.
DESC FUNCTION EXTENDED abs;
+-------------------------------------------------------------------+
|function_desc                                                      |
+-------------------------------------------------------------------+
|Function: abs                                                      |
|Class: org.apache.spark.sql.catalyst.expressions.Abs               |
|Usage: abs(expr) - Returns the absolute value of the numeric value.|
|Extended Usage:                                                    |
|    Examples:                                                      |
|      > SELECT abs(-1);                                            |
|       1                                                           |
|                                                                   |
+-------------------------------------------------------------------+

-- Describe a builtin aggregate function
DESC FUNCTION max;
+--------------------------------------------------------------+
|function_desc                                                 |
+--------------------------------------------------------------+
|Function: max                                                 |
|Class: org.apache.spark.sql.catalyst.expressions.aggregate.Max|
|Usage: max(expr) - Returns the maximum value of `expr`.       |
+--------------------------------------------------------------+

-- Describe a builtin user defined aggregate function
-- Returns function name, implementing class and usage and examples.
DESC FUNCTION EXTENDED explode
+---------------------------------------------------------------+
|function_desc                                                  |
+---------------------------------------------------------------+
|Function: explode                                              |
|Class: org.apache.spark.sql.catalyst.expressions.Explode       |
|Usage: explode(expr) - Separates the elements of array `expr`  |
| into multiple rows, or the elements of map `expr` into        |
| multiple rows and columns. Unless specified otherwise, uses   |
| the default column name `col` for elements of the array or    |
| `key` and `value` for the elements of the map.                |
|Extended Usage:                                                |
|    Examples:                                                  |
|      > SELECT explode(array(10, 20));                         |
|       10                                                      |
|       20                                                      |
+---------------------------------------------------------------+
```