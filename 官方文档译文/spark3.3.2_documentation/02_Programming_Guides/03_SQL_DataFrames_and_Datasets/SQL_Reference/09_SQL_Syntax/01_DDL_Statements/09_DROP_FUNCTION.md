# DROP FUNCTION

[TOC]

## Description

> The DROP FUNCTION statement drops a temporary or user defined function (UDF). An exception will be thrown if the function does not exist.

### Syntax

	DROP [ TEMPORARY ] FUNCTION [ IF EXISTS ] function_name

### Parameters

- function_name

	Specifies the name of an existing function. The function name may be optionally qualified with a database name.
	
	Syntax: `[ database_name. ] function_name`

- TEMPORARY

	Should be used to delete the TEMPORARY function.

- IF EXISTS

	If specified, no exception is thrown when the function does not exist.

## Examples

```sql
-- Create a permanent function `test_avg`
CREATE FUNCTION test_avg AS 'org.apache.hadoop.hive.ql.udf.generic.GenericUDAFAverage';

-- List user functions
SHOW USER FUNCTIONS;
+----------------+
|        function|
+----------------+
|default.test_avg|
+----------------+

-- Create Temporary function `test_avg`
CREATE TEMPORARY FUNCTION test_avg AS
    'org.apache.hadoop.hive.ql.udf.generic.GenericUDAFAverage';

-- List user functions
SHOW USER FUNCTIONS;
+----------------+
|        function|
+----------------+
|default.test_avg|
|        test_avg|
+----------------+

-- Drop Permanent function
DROP FUNCTION test_avg;

-- Try to drop Permanent function which is not present
DROP FUNCTION test_avg;
Error: Error running query:
org.apache.spark.sql.catalyst.analysis.NoSuchPermanentFunctionException:
Function 'default.test_avg' not found in database 'default'; (state=,code=0)

-- List the functions after dropping, it should list only temporary function
SHOW USER FUNCTIONS;
+--------+
|function|
+--------+
|test_avg|
+--------+
  
-- Drop Temporary function
DROP TEMPORARY FUNCTION IF EXISTS test_avg;
```