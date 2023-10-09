# Table-valued Functions (TVF)

[TOC]

## Description

> A table-valued function (TVF) is a function that returns a relation or a set of rows. There are two types of TVFs in Spark SQL:

一个 TVF 是返回一个关系实体或行集的函数。有以下两种类型：

- a TVF that can be specified in a `FROM` clause, e.g. range;
- a TVF that can be specified in `SELECT/LATERAL VIEW` clauses, e.g. explode.

## Supported Table-valued Functions

### TVFs that can be specified in a FROM clause:

Function | Argument Type(s) |  Description
---|:---|:---
range ( end )  |  Long  |  Creates a table with a single LongType column named id,containing rows in a range from 0 to end (exclusive) with step value 1.<br/>【创建一张表，其列名为id,类型是LongType，包含的行的范围是0到end(不包含)，步长为1】
range ( start, end )  |  Long, Long  |  Creates a table with a single LongType column named id,containing rows in a range from start to end (exclusive) with step value 1.<br/>【创建一张表，其列名为id,类型是LongType，包含的行的范围是start到end(不包含)，步长为1】
range ( start, end, step )  |  Long, Long, Long  |  Creates a table with a single LongType column named id,containing rows in a range from start to end (exclusive) with step value.<br/>【创建一张表，其列名为id,类型是LongType，包含的行的范围是start到end(不包含)，步长为step】
range ( start, end, step, numPartitions )  |  Long, Long, Long, Int  |  Creates a table with a single LongType column named id,containing rows in a range from start to end (exclusive) with step value, with partition number numPartitions specified.<br/>【创建一张表，其列名为id,类型是LongType，包含的行的范围是start到end(不包含)，步长为step，具有numPartitions分区数】

### TVFs that can be specified in SELECT/LATERAL VIEW clauses:

Function | Argument Type(s) |  Description
---|:---|:---
explode ( expr ) | Array/Map | Separates the elements of array `expr` into multiple rows, or the elements of map `expr` into multiple rows and columns. Unless specified otherwise, uses the default column name `col` for elements of the array or `key` and `value` for the elements of the map.<br/>【将数组expr中的元素划分到多行，或map expr的元素划分成多行和多列。除非指定，否则，对于数组，使用名为col的列，对于map,使用key和value】
explode_outer ( expr ) | Array/Map | Separates the elements of array `expr` into multiple rows, or the elements of map `expr` into multiple rows and columns. Unless specified otherwise, uses the default column name `col` for elements of the array or `key` and `value` for the elements of the map.<br/>【将数组expr中的元素划分到多行，或map expr的元素划分成多行和多列。除非指定，否则，对于数组，使用名为col的列，对于map,使用key和value】
inline ( expr ) | Expression | Explodes an array of structs into a table. Uses column names `col1`, `col2`, etc. by default unless specified otherwise.<br/>【将元素类型为 struct 的数组爆炸成一张表。除非指定，否则，使用 col1, col2 等作为列名】
inline_outer ( expr ) | Expression | Explodes an array of structs into a table. Uses column names `col1`, `col2`, etc. by default unless specified otherwise.<br/>【将元素类型为 struct 的数组爆炸成一张表。除非指定，否则，使用 col1, col2 等作为列名】
posexplode( expr )  | Array/Map | Separates the elements of array `expr` into multiple rows with positions, or the elements of map expr into multiple rows and columns with positions. Unless specified otherwise, uses the column name `pos` for position, `col` for elements of the array or `key` and `value` for elements of the map.<br/>【将数组expr的元素划分到具有位置信息的多行，或者把map expr的元素划分到具有位置信息的多行和列。除非指定，否则，对于位置，使用列名 pos，对于数组的元素，使用列名 col，对于 map 的元素，使用 key 和 value】
posexplode_outer ( expr ) | Array/Map | Separates the elements of array `expr` into multiple rows with positions, or the elements of map `expr` into multiple rows and columns with positions. Unless specified otherwise, uses the column name `pos` for position, `col` for elements of the array or `key` and `value` for elements of the map.<br/>【将数组expr的元素划分到具有位置信息的多行，或者把map expr的元素划分到具有位置信息的多行和列。除非指定，否则，对于位置，使用列名 pos，对于数组的元素，使用列名 col，对于 map 的元素，使用 key 和 value】
stack ( n, expr1, …, exprk ) | Seq[Expression] | Separates `expr1, …, exprk` into n rows. Uses column names `col0`, `col1`, etc. by default unless specified otherwise. <br/>【把 expr1, …, exprk 划分到 n 行中。除非指定，否则，使用 col0, col1 等作为列名】
json_tuple ( jsonStr, p1, p2, …, pn ) | Seq[Expression]  | Returns a tuple like the function `get_json_object`, but it takes multiple names. All the input parameters and output column types are string. <br/>【返回一个元组，类似get_json_object函数，但它接收多个名称。所有的输入参数和输出列类型都是字符串】
parse_url( url, partToExtract[, key] ) | Seq[Expression] | Extracts a part from a URL. <br/>【从url中抽取一部分】

## Examples

```sql
-- range call with end
SELECT * FROM range(6 + cos(3));
+---+
| id|
+---+
|  0|
|  1|
|  2|
|  3|
|  4|
+---+

-- range call with start and end
SELECT * FROM range(5, 10);
+---+
| id|
+---+
|  5|
|  6|
|  7|
|  8|
|  9|
+---+

-- range call with numPartitions
SELECT * FROM range(0, 10, 2, 200);
+---+
| id|
+---+
|  0|
|  2|
|  4|
|  6|
|  8|
+---+

-- range call with a table alias
SELECT * FROM range(5, 8) AS test;
+---+
| id|
+---+
|  5|
|  6|
|  7|
+---+

SELECT explode(array(10, 20));
+---+
|col|
+---+
| 10|
| 20|
+---+

SELECT inline(array(struct(1, 'a'), struct(2, 'b')));
+----+----+
|col1|col2|
+----+----+
|   1|   a|
|   2|   b|
+----+----+

SELECT posexplode(array(10,20));
+---+---+
|pos|col|
+---+---+
|  0| 10|
|  1| 20|
+---+---+

SELECT stack(2, 1, 2, 3);
+----+----+
|col0|col1|
+----+----+
|   1|   2|
|   3|null|
+----+----+

SELECT json_tuple('{"a":1, "b":2}', 'a', 'b');
+---+---+
| c0| c1|
+---+---+
|  1|  2|
+---+---+

SELECT parse_url('http://spark.apache.org/path?query=1', 'HOST');
+-----------------------------------------------------+
|parse_url(http://spark.apache.org/path?query=1, HOST)|
+-----------------------------------------------------+
|                                     spark.apache.org|
+-----------------------------------------------------+

-- Use explode in a LATERAL VIEW clause
CREATE TABLE test (c1 INT);
INSERT INTO test VALUES (1);
INSERT INTO test VALUES (2);
SELECT * FROM test LATERAL VIEW explode (ARRAY(3,4)) AS c2;
+--+--+
|c1|c2|
+--+--+
| 1| 3|
| 1| 4|
| 2| 3|
| 2| 4|
+--+--+
```