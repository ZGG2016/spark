# CREATE FUNCTION

[TOC]

## Description

> The `CREATE FUNCTION` statement is used to create a temporary or permanent function in Spark. Temporary functions are scoped at a session level where as permanent functions are created in the persistent catalog and are made available to all sessions. The resources specified in the `USING` clause are made available to all executors when they are executed for the first time. In addition to the SQL interface, spark allows users to create custom user defined scalar and aggregate functions using Scala, Python and Java APIs. Please refer to [Scalar UDFs](https://spark.apache.org/docs/3.3.2/sql-ref-functions-udf-scalar.html) and [UDAFs](https://spark.apache.org/docs/3.3.2/sql-ref-functions-udf-aggregate.html) for more information.

`CREATE FUNCTION` 语句用来创建临时或永久函数。临时函数的作用范围是会话级别，而永久函数创建在持久的 catalog 中，所有会话都可用。

当第一次执行时，在 `USING` 子句中指定的资源对所有的 executors 都可用。


除了 SQL 接口，spark 允许用户使用 Scala、Python 和 Java APIs 创建自定义的标量和聚合函数。

### Syntax

	CREATE [ OR REPLACE ] [ TEMPORARY ] FUNCTION [ IF NOT EXISTS ]
    	function_name AS class_name [ resource_locations ]

### Parameters

- OR REPLACE

	If specified, the resources for the function are reloaded. This is mainly useful to pick up any changes made to the implementation of the function. This parameter is mutually exclusive to IF NOT EXISTS and can not be specified together.

- TEMPORARY

	Indicates the scope of function being created. When TEMPORARY is specified, the created function is valid and visible in the current session. No persistent entry is made in the catalog for these kind of functions.

- IF NOT EXISTS

	If specified, creates the function only when it does not exist. The creation of function succeeds (no error is thrown) if the specified function already exists in the system. This parameter is mutually exclusive to OR REPLACE and can not be specified together.

- function_name

	Specifies a name of function to be created. The function name may be optionally qualified with a database name.
	
	Syntax: `[ database_name. ] function_name`

- class_name

	Specifies the name of the class that provides the implementation for function to be created. The implementing class should extend one of the base classes as follows:

	- Should extend UDF or UDAF in `org.apache.hadoop.hive.ql.exec` package.
	- Should extend AbstractGenericUDAFResolver, GenericUDF, or GenericUDTF in `org.apache.hadoop.hive.ql.udf.generic` package.
	- Should extend UserDefinedAggregateFunction in `org.apache.spark.sql.expressions` package.

- resource_locations

	Specifies the list of resources that contain the implementation of the function along with its dependencies.
	
	Syntax: `USING { { (JAR | FILE | ARCHIVE) resource_uri } , ... }`

## Examples

```sql
-- 1. Create a simple UDF `SimpleUdf` that increments the supplied integral value by 10.
--    import org.apache.hadoop.hive.ql.exec.UDF;
--    public class SimpleUdf extends UDF {
--      public int evaluate(int value) {
--        return value + 10;
--      }
--    }
-- 2. Compile and place it in a JAR file called `SimpleUdf.jar` in /tmp.

-- Create a table called `test` and insert two rows.
CREATE TABLE test(c1 INT);
INSERT INTO test VALUES (1), (2);

-- Create a permanent function called `simple_udf`. 
CREATE FUNCTION simple_udf AS 'SimpleUdf'
    USING JAR '/tmp/SimpleUdf.jar';

-- Verify that the function is in the registry.
SHOW USER FUNCTIONS;
+------------------+
|          function|
+------------------+
|default.simple_udf|
+------------------+

-- Invoke the function. Every selected value should be incremented by 10.
SELECT simple_udf(c1) AS function_return_value FROM test;
+---------------------+
|function_return_value|
+---------------------+
|                   11|
|                   12|
+---------------------+

-- Created a temporary function.
CREATE TEMPORARY FUNCTION simple_temp_udf AS 'SimpleUdf' 
    USING JAR '/tmp/SimpleUdf.jar';

-- Verify that the newly created temporary function is in the registry.
-- Please note that the temporary function does not have a qualified
-- database associated with it.
SHOW USER FUNCTIONS;
+------------------+
|          function|
+------------------+
|default.simple_udf|
|   simple_temp_udf|
+------------------+

-- 1. Modify `SimpleUdf`'s implementation to add supplied integral value by 20.
--    import org.apache.hadoop.hive.ql.exec.UDF;
  
--    public class SimpleUdfR extends UDF {
--      public int evaluate(int value) {
--        return value + 20;
--      }
--    }
-- 2. Compile and place it in a jar file called `SimpleUdfR.jar` in /tmp.

-- Replace the implementation of `simple_udf`
CREATE OR REPLACE FUNCTION simple_udf AS 'SimpleUdfR'
    USING JAR '/tmp/SimpleUdfR.jar';

-- Invoke the function. Every selected value should be incremented by 20.
SELECT simple_udf(c1) AS function_return_value FROM test;
+---------------------+
|function_return_value|
+---------------------+
|                   21|
|                   22|
+---------------------+
```