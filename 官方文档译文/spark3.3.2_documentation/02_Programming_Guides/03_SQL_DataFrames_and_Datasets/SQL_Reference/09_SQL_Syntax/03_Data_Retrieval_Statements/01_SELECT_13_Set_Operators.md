# Set Operators

[TOC]

## Description

> Set operators are used to combine two input relations into a single one. Spark SQL supports three types of set operators:

集合操作符用于将两个输入关系实体合并成一个。支持如下操作符：

- EXCEPT or MINUS
- INTERSECT
- UNION

> Note that input relations must have the same number of columns and compatible data types for the respective columns.

注意：输入关系实体必须具有相同的列数量，和对于对应列，具有兼容的数据类型。

### EXCEPT

> `EXCEPT` and `EXCEPT ALL` return the rows that are found in one relation but not the other. `EXCEPT` (alternatively, `EXCEPT DISTINCT`) takes only distinct rows while `EXCEPT ALL` does not remove duplicates from the result rows. Note that `MINUS` is an alias for `EXCEPT`.

`EXCEPT` 和 `EXCEPT ALL` 返回在一个实体中，但不在另一个实体中的行。

`EXCEPT`(`EXCEPT DISTINCT`)去重行，而 `EXCEPT ALL` 不会去重。

`MINUS` 是 `EXCEPT` 的别名。

#### Syntax

	[ ( ] relation [ ) ] EXCEPT | MINUS [ ALL | DISTINCT ] [ ( ] relation [ ) ]

#### Examples

```sql
-- Use number1 and number2 tables to demonstrate set operators in this page.
SELECT * FROM number1;
+---+
|  c|
+---+
|  3|
|  1|
|  2|
|  2|
|  3|
|  4|
+---+
  
SELECT * FROM number2;
+---+
|  c|
+---+
|  5|
|  1|
|  2|
|  2|
+---+

SELECT c FROM number1 EXCEPT SELECT c FROM number2;
+---+
|  c|
+---+
|  3|
|  4|
+---+

SELECT c FROM number1 MINUS SELECT c FROM number2;
+---+
|  c|
+---+
|  3|
|  4|
+---+

SELECT c FROM number1 EXCEPT ALL (SELECT c FROM number2);
+---+
|  c|
+---+
|  3|
|  3|
|  4|
+---+

SELECT c FROM number1 MINUS ALL (SELECT c FROM number2);
+---+
|  c|
+---+
|  3|
|  3|
|  4|
+---+
```

### INTERSECT

> `INTERSECT` and `INTERSECT ALL` return the rows that are found in both relations. `INTERSECT` (alternatively, `INTERSECT DISTINCT`) takes only distinct rows while `INTERSECT ALL` does not remove duplicates from the result rows.

`INTERSECT` 和 `INTERSECT ALL` 返回同时在两个实体中的行。

`INTERSECT`(`INTERSECT DISTINCT`)去重行，而 `INTERSECT ALL` 不会去重。

#### Syntax

	[ ( ] relation [ ) ] INTERSECT [ ALL | DISTINCT ] [ ( ] relation [ ) ]

#### Examples

```sql
(SELECT c FROM number1) INTERSECT (SELECT c FROM number2);
+---+
|  c|
+---+
|  1|
|  2|
+---+

(SELECT c FROM number1) INTERSECT DISTINCT (SELECT c FROM number2);
+---+
|  c|
+---+
|  1|
|  2|
+---+

(SELECT c FROM number1) INTERSECT ALL (SELECT c FROM number2);
+---+
|  c|
+---+
|  1|
|  2|
|  2|
+---+
```

### UNION

> UNION and UNION ALL return the rows that are found in either relation. UNION (alternatively, UNION DISTINCT) takes only distinct rows while UNION ALL does not remove duplicates from the result rows.

`UNION` 和 `UNION ALL` 返回在其中一个实体中的行。

`UNION`(`UNION DISTINCT`)去重行，而 `UNION ALL` 不会去重。

#### Syntax

	[ ( ] relation [ ) ] UNION [ ALL | DISTINCT ] [ ( ] relation [ ) ]

#### Examples

```sql
(SELECT c FROM number1) UNION (SELECT c FROM number2);
+---+
|  c|
+---+
|  1|
|  3|
|  5|
|  4|
|  2|
+---+

(SELECT c FROM number1) UNION DISTINCT (SELECT c FROM number2);
+---+
|  c|
+---+
|  1|
|  3|
|  5|
|  4|
|  2|
+---+

SELECT c FROM number1 UNION ALL (SELECT c FROM number2);
+---+
|  c|
+---+
|  3|
|  1|
|  2|
|  2|
|  3|
|  4|
|  5|
|  1|
|  2|
|  2|
+---+
```