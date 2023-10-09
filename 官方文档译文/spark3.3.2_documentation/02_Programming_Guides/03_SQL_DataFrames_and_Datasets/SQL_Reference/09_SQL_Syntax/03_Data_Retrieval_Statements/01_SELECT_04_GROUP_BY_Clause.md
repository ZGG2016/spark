# GROUP BY Clause

[TOC]

## Description

> The `GROUP BY` clause is used to group the rows based on a set of specified grouping expressions and compute aggregations on the group of rows based on one or more specified aggregate functions. Spark also supports advanced aggregations to do multiple aggregations for the same input record set via `GROUPING SETS`, `CUBE`, `ROLLUP` clauses. The grouping expressions and advanced aggregations can be mixed in the `GROUP BY` clause and nested in a `GROUPING SETS` clause. See more details in the `Mixed/Nested Grouping Analytics` section. When a `FILTER` clause is attached to an aggregate function, only the matching rows are passed to that function.

基于指定的分组表达式，对行进行分组，并基于指定的聚合函数，对行组进行聚合计算。

spark 也支持更高级的聚合，通过 `GROUPING SETS`, `CUBE`, `ROLLUP` 子句，对相同的输入记录执行多种聚合。

分组表达式和高级聚合可以在 `GROUP BY` 子句中混合使用，在 `GROUPING SETS` 子句中嵌套使用。

如果 `FILTER` 子句和聚合函数一起使用，那么仅将匹配的行传给聚合函数。

### Syntax

	GROUP BY group_expression [ , group_expression [ , ... ] ] [ WITH { ROLLUP | CUBE } ]
	
	GROUP BY { group_expression | { ROLLUP | CUBE | GROUPING SETS } (grouping_set [ , ...]) } [ , ... ]

While aggregate functions are defined as

	aggregate_name ( [ DISTINCT ] expression [ , ... ] ) [ FILTER ( WHERE boolean_expression ) ]

### Parameters

- group_expression

    Specifies the criteria based on which the rows are grouped together. The grouping of rows is performed based on result values of the grouping expressions. A grouping expression may be a column name like `GROUP BY a`, a column position like `GROUP BY 0`, or an expression like `GROUP BY a + b`.

    指定一个分组行的准则。基于分组表达式的结果值对行分组。

    一个分组表达式可以是一个列名，例如 `GROUP BY a`, 也可以是列的位置，例如 `GROUP BY 0`, 也可以是表达式，例如 `GROUP BY a + b`.

- grouping_set

    A grouping set is specified by zero or more comma-separated expressions in parentheses. When the grouping set has only one element, parentheses can be omitted. For example, `GROUPING SETS ((a), (b))` is the same as `GROUPING SETS (a, b)`.

    Syntax: `{ ( [ expression [ , ... ] ] ) | expression }`

    一个分组集合可以由0个或多个逗号分隔的表达式指定，由括号括起来。

    当分组集合仅有一个元素时，就可以忽略括号。

- GROUPING SETS

    Groups the rows for each grouping set specified after `GROUPING SETS`. For example, `GROUP BY GROUPING SETS ((warehouse), (product))` is semantically equivalent to union of results of `GROUP BY warehouse` and `GROUP BY product`. This clause is a shorthand for a `UNION ALL` where each leg of the `UNION ALL` operator performs aggregation of each grouping set specified in the `GROUPING SETS` clause. Similarly, `GROUP BY GROUPING SETS ((warehouse, product), (product), ())` is semantically equivalent to the union of results of `GROUP BY warehouse, product`, `GROUP BY product` and global aggregate.

    基于在 `GROUPING SETS` 之后指定的每个分组集合，分组行。例如 `GROUP BY GROUPING SETS ((warehouse), (product))` 在语义上等价于 `GROUP BY warehouse` 和 `GROUP BY product` 结果的合并。

    这个子句是 `UNION ALL` 的简略方式， `UNION ALL` 操作符的每个部分执行每个分组集合的聚合。例如 `GROUP BY GROUPING SETS ((warehouse, product), (product), ())` 在语义上等价于 `GROUP BY warehouse, product`, `GROUP BY product` 和全局聚合结果的合并。

    Note: For Hive compatibility Spark allows `GROUP BY ... GROUPING SETS (...)`. The `GROUP BY` expressions are usually ignored, but if it contains extra expressions than the `GROUPING SETS` expressions, the extra expressions will be included in the grouping expressions and the value is always null. For example, `SELECT a, b, c FROM ... GROUP BY a, b, c GROUPING SETS (a, b)`, the output of column `c` is always null.

    注意：考虑 hive 兼容性，spark 允许 `GROUP BY ... GROUPING SETS (...)`. `GROUP BY` 表达式通常被忽略，但是如果它包含除了 `GROUPING SETS` 的额外表达式，额外表达式将被包含在分组表达式中，并且值总是 null. 例如，`SELECT a, b, c FROM ... GROUP BY a, b, c GROUPING SETS (a, b)`, 列 `c` 的输出总是 null.

- ROLLUP

    Specifies multiple levels of aggregations in a single statement. This clause is used to compute aggregations based on multiple grouping sets. `ROLLUP` is a shorthand for `GROUPING SETS`. For example, `GROUP BY warehouse, product WITH ROLLUP` or `GROUP BY ROLLUP(warehouse, product)` is equivalent to `GROUP BY GROUPING SETS((warehouse, product), (warehouse), ())`. `GROUP BY ROLLUP(warehouse, product, (warehouse, location))` is equivalent to `GROUP BY GROUPING SETS((warehouse, product, location), (warehouse, product), (warehouse), ())`. The `N` elements of a `ROLLUP` specification results in N+1 `GROUPING SETS`.

    在单个语句中，指定聚合的多个级别。这个子句用来基于多个分组集合计算聚合。

    `ROLLUP` 是 `GROUPING SETS` 的简略方式。例如：

    `GROUP BY warehouse, product WITH ROLLUP` 或 `GROUP BY ROLLUP(warehouse, product)` 等价于 `GROUP BY GROUPING SETS((warehouse, product), (warehouse), ())`. 

    `GROUP BY ROLLUP(warehouse, product, (warehouse, location))` 等价于 `GROUP BY GROUPING SETS((warehouse, product, location), (warehouse, product), (warehouse), ())`.

    `ROLLUP` 的 N 个元素产生 N+1 个 `GROUPING SETS`.

- CUBE

    `CUBE` clause is used to perform aggregations based on combination of grouping columns specified in the `GROUP BY` clause. `CUBE` is a shorthand for `GROUPING SETS`. For example, `GROUP BY warehouse, product WITH CUBE` or `GROUP BY CUBE(warehouse, product)` is equivalent to `GROUP BY GROUPING SETS((warehouse, product), (warehouse), (product), ())`. `GROUP BY CUBE(warehouse, product, (warehouse, location))` is equivalent to `GROUP BY GROUPING SETS((warehouse, product, location), (warehouse, product), (warehouse, location), (product, warehouse, location), (warehouse), (product), (warehouse, product), ())`. The `N` elements of a `CUBE` specification results in 2^N `GROUPING SETS`.

    基于 `GROUP BY` 中指定的分组列的组合，执行聚合。

    `CUBE` 是 `GROUPING SETS` 的简略方式。例如：

    `GROUP BY warehouse, product WITH CUBE` 或 `GROUP BY CUBE(warehouse, product)` 等价于 `GROUP BY GROUPING SETS((warehouse, product), (warehouse), (product), ())`.

    `GROUP BY CUBE(warehouse, product, (warehouse, location))` 等价于 `GROUP BY GROUPING SETS((warehouse, product, location), (warehouse, product), (warehouse, location), (product, warehouse, location), (warehouse), (product), (warehouse, product), ())`.

    `CUBE` 的 N 个元素产生 2^N 个 `GROUPING SETS`.

- Mixed/Nested Grouping Analytics

    A `GROUP BY` clause can include multiple `group_expressions` and multiple `CUBE|ROLLUP|GROUPING SETSs`. `GROUPING SETS` can also have nested `CUBE|ROLLUP|GROUPING SETS` clauses, e.g. `GROUPING SETS(ROLLUP(warehouse, location), CUBE(warehouse, location))`, `GROUPING SETS(warehouse, GROUPING SETS(location, GROUPING SETS(ROLLUP(warehouse, location), CUBE(warehouse, location))))`. `CUBE|ROLLUP` is just a syntax sugar for `GROUPING SETS`, please refer to the sections above for how to translate `CUBE|ROLLUP` to `GROUPING SETS`. `group_expression` can be treated as a single-group `GROUPING SETS` under this context. For multiple `GROUPING SETS` in the `GROUP BY` clause, we generate a single `GROUPING SETS` by doing a cross-product of the original `GROUPING SETSs`. For nested `GROUPING SETS` in the `GROUPING SETS` clause, we simply take its grouping sets and strip it. For example, `GROUP BY warehouse, GROUPING SETS((product), ()), GROUPING SETS((location, size), (location), (size), ())` and `GROUP BY warehouse, ROLLUP(product), CUBE(location, size)` is equivalent to `GROUP BY GROUPING SETS( (warehouse, product, location, size), (warehouse, product, location), (warehouse, product, size), (warehouse, product), (warehouse, location, size), (warehouse, location), (warehouse, size), (warehouse))`.

    `GROUP BY GROUPING SETS(GROUPING SETS(warehouse), GROUPING SETS((warehouse, product)))` is equivalent to `GROUP BY GROUPING SETS((warehouse), (warehouse, product))`.

    `GROUP BY` 子句可以包含多个 `group_expressions` 和多个 `CUBE|ROLLUP|GROUPING SETSs`.

    `GROUPING SETS` 也可以有嵌套的 `CUBE|ROLLUP|GROUPING SETSs` 子句。例如：

    `GROUPING SETS(ROLLUP(warehouse, location), CUBE(warehouse, location))`, `GROUPING SETS(warehouse, GROUPING SETS(location, GROUPING SETS(ROLLUP(warehouse, location), CUBE(warehouse, location))))`

    `CUBE|ROLLUP` 仅是 `GROUPING SETS` 的语法糖。

    `group_expression` 可以被当作这个上下文中的单组 `GROUPING SETS`. 对于 `GROUP BY` 子句中的多个 `GROUPING SETS`，通过对原始 `GROUPING SETSs` 求笛卡尔积，我们生成一个 `GROUPING SETS`. 对于 `GROUP BY` 子句中嵌套的的 `GROUPING SETS`, 我们仅接收它的分组集合，并分离它。例如：

    `GROUP BY warehouse, GROUPING SETS((product), ()), GROUPING SETS((location, size), (location), (size), ())` 和 `GROUP BY warehouse, ROLLUP(product), CUBE(location, size)` 等价于 `GROUP BY GROUPING SETS( (warehouse, product, location, size), (warehouse, product, location), (warehouse, product, size), (warehouse, product), (warehouse, location, size), (warehouse, location), (warehouse, size), (warehouse))`.

- aggregate_name

    Specifies an aggregate function name (MIN, MAX, COUNT, SUM, AVG, etc.).

    指定一个聚合函数名称。

- DISTINCT

    Removes duplicates in input rows before they are passed to aggregate functions.

    在传给聚合函数前，对输入行去重。

- FILTER

    Filters the input rows for which the `boolean_expression` in the `WHERE` clause evaluates to true are passed to the aggregate function; other rows are discarded.

    过滤出 `boolean_expression` 为 true 的行，将其传给聚合函数。其他行被丢弃。  【组内过滤】

## Examples

```sql
CREATE TABLE dealer (id INT, city STRING, car_model STRING, quantity INT);
INSERT INTO dealer VALUES
    (100, 'Fremont', 'Honda Civic', 10),
    (100, 'Fremont', 'Honda Accord', 15),
    (100, 'Fremont', 'Honda CRV', 7),
    (200, 'Dublin', 'Honda Civic', 20),
    (200, 'Dublin', 'Honda Accord', 10),
    (200, 'Dublin', 'Honda CRV', 3),
    (300, 'San Jose', 'Honda Civic', 5),
    (300, 'San Jose', 'Honda Accord', 8);

-- Sum of quantity per dealership. Group by `id`.
SELECT id, sum(quantity) FROM dealer GROUP BY id ORDER BY id;
+---+-------------+
| id|sum(quantity)|
+---+-------------+
|100|           32|
|200|           33|
|300|           13|
+---+-------------+

-- Use column position in GROUP by clause.
SELECT id, sum(quantity) FROM dealer GROUP BY 1 ORDER BY 1;
+---+-------------+
| id|sum(quantity)|
+---+-------------+
|100|           32|
|200|           33|
|300|           13|
+---+-------------+

-- Multiple aggregations.
-- 1. Sum of quantity per dealership.
-- 2. Max quantity per dealership.
SELECT id, sum(quantity) AS sum, max(quantity) AS max FROM dealer GROUP BY id ORDER BY id;
+---+---+---+
| id|sum|max|
+---+---+---+
|100| 32| 15|
|200| 33| 20|
|300| 13|  8|
+---+---+---+

-- Count the number of distinct dealer cities per car_model.
SELECT car_model, count(DISTINCT city) AS count FROM dealer GROUP BY car_model;
+------------+-----+
|   car_model|count|
+------------+-----+
| Honda Civic|    3|
|   Honda CRV|    2|
|Honda Accord|    3|
+------------+-----+

-- Sum of only 'Honda Civic' and 'Honda CRV' quantities per dealership.
SELECT id, sum(quantity) FILTER (
            WHERE car_model IN ('Honda Civic', 'Honda CRV')
        ) AS `sum(quantity)` FROM dealer
    GROUP BY id ORDER BY id;
+---+-------------+
| id|sum(quantity)|
+---+-------------+
|100|           17|
|200|           23|
|300|            5|
+---+-------------+

-- Aggregations using multiple sets of grouping columns in a single statement.
-- Following performs aggregations based on four sets of grouping columns.
-- 1. city, car_model
-- 2. city
-- 3. car_model
-- 4. Empty grouping set. Returns quantities for all city and car models.
SELECT city, car_model, sum(quantity) AS sum FROM dealer
    GROUP BY GROUPING SETS ((city, car_model), (city), (car_model), ())
    ORDER BY city;
+---------+------------+---+
|     city|   car_model|sum|
+---------+------------+---+
|     null|        null| 78|
|     null| HondaAccord| 33|
|     null|    HondaCRV| 10|
|     null|  HondaCivic| 35|
|   Dublin|        null| 33|
|   Dublin| HondaAccord| 10|
|   Dublin|    HondaCRV|  3|
|   Dublin|  HondaCivic| 20|
|  Fremont|        null| 32|
|  Fremont| HondaAccord| 15|
|  Fremont|    HondaCRV|  7|
|  Fremont|  HondaCivic| 10|
| San Jose|        null| 13|
| San Jose| HondaAccord|  8|
| San Jose|  HondaCivic|  5|
+---------+------------+---+

-- Group by processing with `ROLLUP` clause.
-- Equivalent GROUP BY GROUPING SETS ((city, car_model), (city), ())
SELECT city, car_model, sum(quantity) AS sum FROM dealer
    GROUP BY city, car_model WITH ROLLUP
    ORDER BY city, car_model;
+---------+------------+---+
|     city|   car_model|sum|
+---------+------------+---+
|     null|        null| 78|
|   Dublin|        null| 33|
|   Dublin| HondaAccord| 10|
|   Dublin|    HondaCRV|  3|
|   Dublin|  HondaCivic| 20|
|  Fremont|        null| 32|
|  Fremont| HondaAccord| 15|
|  Fremont|    HondaCRV|  7|
|  Fremont|  HondaCivic| 10|
| San Jose|        null| 13|
| San Jose| HondaAccord|  8|
| San Jose|  HondaCivic|  5|
+---------+------------+---+

-- Group by processing with `CUBE` clause.
-- Equivalent GROUP BY GROUPING SETS ((city, car_model), (city), (car_model), ())
SELECT city, car_model, sum(quantity) AS sum FROM dealer
    GROUP BY city, car_model WITH CUBE
    ORDER BY city, car_model;
+---------+------------+---+
|     city|   car_model|sum|
+---------+------------+---+
|     null|        null| 78|
|     null| HondaAccord| 33|
|     null|    HondaCRV| 10|
|     null|  HondaCivic| 35|
|   Dublin|        null| 33|
|   Dublin| HondaAccord| 10|
|   Dublin|    HondaCRV|  3|
|   Dublin|  HondaCivic| 20|
|  Fremont|        null| 32|
|  Fremont| HondaAccord| 15|
|  Fremont|    HondaCRV|  7|
|  Fremont|  HondaCivic| 10|
| San Jose|        null| 13|
| San Jose| HondaAccord|  8|
| San Jose|  HondaCivic|  5|
+---------+------------+---+

--Prepare data for ignore nulls example
CREATE TABLE person (id INT, name STRING, age INT);
INSERT INTO person VALUES
    (100, 'Mary', NULL),
    (200, 'John', 30),
    (300, 'Mike', 80),
    (400, 'Dan', 50);

--Select the first row in column age
SELECT FIRST(age) FROM person;
+--------------------+
| first(age, false)  |
+--------------------+
| NULL               |
+--------------------+

--Get the first row in column `age` ignore nulls,last row in column `id` and sum of column `id`.
SELECT FIRST(age IGNORE NULLS), LAST(id), SUM(id) FROM person;
+-------------------+------------------+----------+
| first(age, true)  | last(id, false)  | sum(id)  |
+-------------------+------------------+----------+
| 30                | 400              | 1000     |
+-------------------+------------------+----------+
```