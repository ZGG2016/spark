# Generator Functions

[TOC]

## Generator Functions

### explode(expr)

Separates the elements of array `expr` into multiple rows, or the elements of map `expr` into multiple rows and columns. Unless specified otherwise, uses the default column name `col` for elements of the array or `key` and `value` for the elements of the map.

将数组 `expr` 的元素划分到多行，或者把 map `expr` 的元素划分到多行和列。

除非指定，否则，对于数组的元素，使用默认列名 `col`，对于 map 的元素，使用 `key` 和 `value`

### explode_outer(expr)

Separates the elements of array `expr` into multiple rows, or the elements of map `expr` into multiple rows and columns. Unless specified otherwise, uses the default column name `col` for elements of the array or `key` and `value` for the elements of the map.

将数组 `expr` 的元素划分到多行，或者把 map `expr` 的元素划分到多行和列。

除非指定，否则，对于数组的元素，使用默认列名 `col`，对于 map 的元素，使用 `key` 和 `value`

### inline(expr)

Explodes an array of structs into a table. Uses column names col1, col2, etc. by default unless specified otherwise.

将元素类型为 struct 的数组爆炸成一张表。

除非指定，否则，使用 col1, col2 等作为列名

### inline_outer(expr)

Explodes an array of structs into a table. Uses column names col1, col2, etc. by default unless specified otherwise.

将元素类型为 struct 的数组爆炸成一张表。

除非指定，否则，使用 col1, col2 等作为列名

### posexplode(expr)

Separates the elements of array `expr` into multiple rows with positions, or the elements of map `expr` into multiple rows and columns with positions. Unless specified otherwise, uses the column name `pos` for position, `col` for elements of the array or `key` and `value` for elements of the map.

将数组 `expr` 的元素划分到具有位置信息的多行，或者把 map `expr` 的元素划分到具有位置信息的多行和列。

除非指定，否则，对于位置，使用列名 `pos`，对于数组的元素，使用列名 `col`，对于 map 的元素，使用 `key` 和 `value`

### posexplode_outer(expr)

Separates the elements of array `expr` into multiple rows with positions, or the elements of map `expr` into multiple rows and columns with positions. Unless specified otherwise, uses the column name `pos` for position, `col` for elements of the array or `key` and `value` for elements of the map.

将数组 `expr` 的元素划分到具有位置信息的多行，或者把 map `expr` 的元素划分到具有位置信息的多行和列。

除非指定，否则，对于位置，使用列名 `pos`，对于数组的元素，使用列名 `col`，对于 map 的元素，使用 `key` 和 `value`

### stack(n, expr1, ..., exprk)

Separates `expr1`, ..., `exprk` into `n` rows. Uses column names col0, col1, etc. by default unless specified otherwise.

把 `expr1`, ..., `exprk` 划分到 `n` 行中。

除非指定，否则，使用 col0, col1 等作为列名

## Examples

```sql
-- explode
SELECT explode(array(10, 20));
+---+
|col|
+---+
| 10|
| 20|
+---+

-- explode_outer
SELECT explode_outer(array(10, 20));
+---+
|col|
+---+
| 10|
| 20|
+---+

-- inline
SELECT inline(array(struct(1, 'a'), struct(2, 'b')));
+----+----+
|col1|col2|
+----+----+
|   1|   a|
|   2|   b|
+----+----+

-- inline_outer
SELECT inline_outer(array(struct(1, 'a'), struct(2, 'b')));
+----+----+
|col1|col2|
+----+----+
|   1|   a|
|   2|   b|
+----+----+

-- posexplode
SELECT posexplode(array(10,20));
+---+---+
|pos|col|
+---+---+
|  0| 10|
|  1| 20|
+---+---+

-- posexplode_outer
SELECT posexplode_outer(array(10,20));
+---+---+
|pos|col|
+---+---+
|  0| 10|
|  1| 20|
+---+---+

-- stack
SELECT stack(2, 1, 2, 3);
+----+----+
|col0|col1|
+----+----+
|   1|   2|
|   3|null|
+----+----+
```