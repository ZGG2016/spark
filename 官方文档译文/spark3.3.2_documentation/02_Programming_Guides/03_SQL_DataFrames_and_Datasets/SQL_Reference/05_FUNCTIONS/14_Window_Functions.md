# Window Functions

[TOC]

## Window Functions

### cume_dist()

Computes the position of a value relative to all values in the partition.

计算一个值相对分区中的所有值的位置

### dense_rank()

Computes the rank of a value in a group of values. The result is one plus the previously assigned rank value. Unlike the function rank, dense_rank will not produce gaps in the ranking sequence.

计算一个值在一组值中的等级，其结果是前面分配的等级值加1

和 rank 函数不同，dense_rank 在等级序列中不会产生间隔。

### lag(input[, offset[, default]])

Returns the value of `input` at the `offset`th row before the current row in the window. The default value of `offset` is 1 and the default value of `default` is null. If the value of `input` at the `offset`th row is null, null is returned. If there is no such offset row (e.g., when the offset is 1, the first row of the window does not have any previous row), `default` is returned.

返回窗口中，在当前行之前，第 `offset` 行的 `input` 的值。 

`offset` 的默认值是1, `default` 的默认值是 null.

如果第 `offset` 行的 `input` 的值是 null, 就返回 null

如果没有指定的 `offset` 行（例如，`offset`是1，但是窗口的第一行前面并没有行），就返回 `default` 

### lead(input[, offset[, default]])

Returns the value of `input` at the `offset`th row after the current row in the window. The default value of `offset` is 1 and the default value of `default` is null. If the value of `input` at the `offset`th row is null, null is returned. If there is no such an offset row (e.g., when the offset is 1, the last row of the window does not have any subsequent row), `default` is returned.

返回窗口中，在当前行之后，第 `offset` 行的 `input` 的值。 

`offset` 的默认值是1, `default` 的默认值是 null.

如果第 `offset` 行的 `input` 的值是 null, 就返回 null

如果没有指定的 `offset` 行（例如，`offset`是1，但是窗口的最后一行后面并没有行），就返回 `default` 

### nth_value(input[, offset])

Returns the value of `input` at the row that is the `offset`th row from beginning of the window frame. Offset starts at 1. If ignoreNulls=true, we will skip nulls when finding the `offset`th row. Otherwise, every row counts for the `offset`. If there is no such an `offset`th row (e.g., when the offset is 10, size of the window frame is less than 10), null is returned.

从窗口的开始计数，返回第 `offset` 行的 `input` 的值。 

`offset` 从1开始。如果 ignoreNulls=true, 在查找第 `offset` 行的值时，会跳过 null. 否则，每行使用 `offset` 统计。

如果没有找到第 `offset` 行的值（例如，如果`offset`是10，窗口大小小于10），就返回 null 

### ntile(n)

Divides the rows for each window partition into `n` buckets ranging from 1 to at most `n`.

把每个窗口分区的行划分到 `n` 桶中，桶编号的范围从1到 `n`

### percent_rank()

Computes the percentage ranking of a value in a group of values.

计算一组值中一个值的百分比等级

### rank()

Computes the rank of a value in a group of values. The result is one plus the number of rows preceding or equal to the current row in the ordering of the partition. The values will produce gaps in the sequence.

计算一个值在一组值中的等级，其结果是分区中当前行之前或等于当前行的行数加1

这会在序列中产生间隔。

### row_number()

Assigns a unique, sequential number to each row, starting with one, according to the ordering of rows within the window partition.

根据窗口分区中行的顺序，为每行分配一个唯一的连续数字，从1开始。

## Examples

```sql
-- cume_dist
SELECT a, b, cume_dist() OVER (PARTITION BY a ORDER BY b) FROM VALUES ('A1', 2), ('A1', 1), ('A2', 3), ('A1', 1) tab(a, b);
+---+---+--------------------------------------------------------------------------------------------------------------+
|  a|  b|cume_dist() OVER (PARTITION BY a ORDER BY b ASC NULLS FIRST RANGE BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW)|
+---+---+--------------------------------------------------------------------------------------------------------------+
| A1|  1|                                                                                            0.6666666666666666|
| A1|  1|                                                                                            0.6666666666666666|
| A1|  2|                                                                                                           1.0|
| A2|  3|                                                                                                           1.0|
+---+---+--------------------------------------------------------------------------------------------------------------+

-- dense_rank
SELECT a, b, dense_rank(b) OVER (PARTITION BY a ORDER BY b) FROM VALUES ('A1', 2), ('A1', 1), ('A2', 3), ('A1', 1) tab(a, b);
+---+---+--------------------------------------------------------------------------------------------------------------+
|  a|  b|DENSE_RANK() OVER (PARTITION BY a ORDER BY b ASC NULLS FIRST ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW)|
+---+---+--------------------------------------------------------------------------------------------------------------+
| A1|  1|                                                                                                             1|
| A1|  1|                                                                                                             1|
| A1|  2|                                                                                                             2|
| A2|  3|                                                                                                             1|
+---+---+--------------------------------------------------------------------------------------------------------------+

-- lag
SELECT a, b, lag(b) OVER (PARTITION BY a ORDER BY b) FROM VALUES ('A1', 2), ('A1', 1), ('A2', 3), ('A1', 1) tab(a, b);
+---+---+-----------------------------------------------------------------------------------------------------------+
|  a|  b|lag(b, 1, NULL) OVER (PARTITION BY a ORDER BY b ASC NULLS FIRST ROWS BETWEEN -1 FOLLOWING AND -1 FOLLOWING)|
+---+---+-----------------------------------------------------------------------------------------------------------+
| A1|  1|                                                                                                       null|
| A1|  1|                                                                                                          1|
| A1|  2|                                                                                                          1|
| A2|  3|                                                                                                       null|
+---+---+-----------------------------------------------------------------------------------------------------------+

-- lead
SELECT a, b, lead(b) OVER (PARTITION BY a ORDER BY b) FROM VALUES ('A1', 2), ('A1', 1), ('A2', 3), ('A1', 1) tab(a, b);
+---+---+----------------------------------------------------------------------------------------------------------+
|  a|  b|lead(b, 1, NULL) OVER (PARTITION BY a ORDER BY b ASC NULLS FIRST ROWS BETWEEN 1 FOLLOWING AND 1 FOLLOWING)|
+---+---+----------------------------------------------------------------------------------------------------------+
| A1|  1|                                                                                                         1|
| A1|  1|                                                                                                         2|
| A1|  2|                                                                                                      null|
| A2|  3|                                                                                                      null|
+---+---+----------------------------------------------------------------------------------------------------------+

-- nth_value
SELECT a, b, nth_value(b, 2) OVER (PARTITION BY a ORDER BY b) FROM VALUES ('A1', 2), ('A1', 1), ('A2', 3), ('A1', 1) tab(a, b);
+---+---+------------------------------------------------------------------------------------------------------------------+
|  a|  b|nth_value(b, 2) OVER (PARTITION BY a ORDER BY b ASC NULLS FIRST RANGE BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW)|
+---+---+------------------------------------------------------------------------------------------------------------------+
| A1|  1|                                                                                                                 1|
| A1|  1|                                                                                                                 1|
| A1|  2|                                                                                                                 1|
| A2|  3|                                                                                                              null|
+---+---+------------------------------------------------------------------------------------------------------------------+

-- ntile
SELECT a, b, ntile(2) OVER (PARTITION BY a ORDER BY b) FROM VALUES ('A1', 2), ('A1', 1), ('A2', 3), ('A1', 1) tab(a, b);
+---+---+----------------------------------------------------------------------------------------------------------+
|  a|  b|ntile(2) OVER (PARTITION BY a ORDER BY b ASC NULLS FIRST ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW)|
+---+---+----------------------------------------------------------------------------------------------------------+
| A1|  1|                                                                                                         1|
| A1|  1|                                                                                                         1|
| A1|  2|                                                                                                         2|
| A2|  3|                                                                                                         1|
+---+---+----------------------------------------------------------------------------------------------------------+

-- percent_rank
SELECT a, b, percent_rank(b) OVER (PARTITION BY a ORDER BY b) FROM VALUES ('A1', 2), ('A1', 1), ('A2', 3), ('A1', 1) tab(a, b);
+---+---+----------------------------------------------------------------------------------------------------------------+
|  a|  b|PERCENT_RANK() OVER (PARTITION BY a ORDER BY b ASC NULLS FIRST ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW)|
+---+---+----------------------------------------------------------------------------------------------------------------+
| A1|  1|                                                                                                             0.0|
| A1|  1|                                                                                                             0.0|
| A1|  2|                                                                                                             1.0|
| A2|  3|                                                                                                             0.0|
+---+---+----------------------------------------------------------------------------------------------------------------+

-- rank
SELECT a, b, rank(b) OVER (PARTITION BY a ORDER BY b) FROM VALUES ('A1', 2), ('A1', 1), ('A2', 3), ('A1', 1) tab(a, b);
+---+---+--------------------------------------------------------------------------------------------------------+
|  a|  b|RANK() OVER (PARTITION BY a ORDER BY b ASC NULLS FIRST ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW)|
+---+---+--------------------------------------------------------------------------------------------------------+
| A1|  1|                                                                                                       1|
| A1|  1|                                                                                                       1|
| A1|  2|                                                                                                       3|
| A2|  3|                                                                                                       1|
+---+---+--------------------------------------------------------------------------------------------------------+

-- row_number
SELECT a, b, row_number() OVER (PARTITION BY a ORDER BY b) FROM VALUES ('A1', 2), ('A1', 1), ('A2', 3), ('A1', 1) tab(a, b);
+---+---+--------------------------------------------------------------------------------------------------------------+
|  a|  b|row_number() OVER (PARTITION BY a ORDER BY b ASC NULLS FIRST ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW)|
+---+---+--------------------------------------------------------------------------------------------------------------+
| A1|  1|                                                                                                             1|
| A1|  1|                                                                                                             2|
| A1|  2|                                                                                                             3|
| A2|  3|                                                                                                             1|
+---+---+--------------------------------------------------------------------------------------------------------------+
```