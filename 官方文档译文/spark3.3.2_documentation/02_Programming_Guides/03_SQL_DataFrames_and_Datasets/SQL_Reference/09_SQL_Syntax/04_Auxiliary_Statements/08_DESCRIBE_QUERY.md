# DESCRIBE QUERY

## Description

> The `DESCRIBE QUERY` statement is used to return the metadata of output of a query. A shorthand `DESC` may be used instead of `DESCRIBE` to describe the query output.

`DESCRIBE QUERY` 语句返回一个查询输出的元数据。

### Syntax

	{ DESC | DESCRIBE } [ QUERY ] input_statement

### Parameters

- QUERY 

	This clause is optional and may be omitted.

- input_statement

	Specifies a result set producing statement and may be one of the following:

	- a `SELECT` statement
	- a `CTE(Common table expression)` statement
	- an `INLINE TABLE` statement
	- a `TABLE` statement
	- a `FROM` statement`
	
	Please refer to [select-statement](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-qry-select.html) for a detailed syntax of the query parameter.

## Examples

```sql
-- Create table `person`
CREATE TABLE person (name STRING , age INT COMMENT 'Age column', address STRING);

-- Returns column metadata information for a simple select query
DESCRIBE QUERY SELECT age, sum(age) FROM person GROUP BY age;
+--------+---------+----------+
|col_name|data_type|   comment|
+--------+---------+----------+
|     age|      int|Age column|
|sum(age)|   bigint|      null|
+--------+---------+----------+

-- Returns column metadata information for common table expression (`CTE`).
DESCRIBE QUERY WITH all_names_cte
    AS (SELECT name from person) SELECT * FROM all_names_cte;
+--------+---------+-------+
|col_name|data_type|comment|
+--------+---------+-------+
|    name|   string|   null|
+--------+---------+-------+

-- Returns column metadata information for an inline table.
DESC QUERY VALUES(100, 'John', 10000.20D) AS employee(id, name, salary);
+--------+---------+-------+
|col_name|data_type|comment|
+--------+---------+-------+
|      id|      int|   null|
|    name|   string|   null|
|  salary|   double|   null|
+--------+---------+-------+

-- Returns column metadata information for `TABLE` statement.
DESC QUERY TABLE person;
+--------+---------+----------+
|col_name|data_type|   comment|
+--------+---------+----------+
|    name|   string|      null|
|     age|      int| Agecolumn|
| address|   string|      null|
+--------+---------+----------+

-- Returns column metadata information for a `FROM` statement.
-- `QUERY` clause is optional and can be omitted.
DESCRIBE FROM person SELECT age;
+--------+---------+----------+
|col_name|data_type|   comment|
+--------+---------+----------+
|     age|      int| Agecolumn|
+--------+---------+----------+
```