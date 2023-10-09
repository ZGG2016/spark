# Conditional Functions

[TOC]

## Conditional Functions

### coalesce(expr1, expr2, ...)

Returns the first non-null argument if exists. Otherwise, null.

返回第一个非 null 的参数.否则，返回 null.

### if(expr1, expr2, expr3)

If `expr1` evaluates to true, then returns `expr2`; otherwise returns `expr3`.

如果 `expr1` 为 true, 就返回 `expr2`，否则返回 `expr3`

### ifnull(expr1, expr2)

Returns `expr2` if `expr1` is null, or `expr1` otherwise.

如果 `expr1` 为 null, 返回 `expr2`，否则返回 `expr1`

### nanvl(expr1, expr2)

Returns `expr1` if it's not NaN, or `expr2` otherwise.

如果 `expr1` 不是 NaN, 返回 `expr1`，否则返回 `expr2`

### nullif(expr1, expr2)

Returns null if `expr1` equals to `expr2`, or `expr1` otherwise.

如果 `expr1` 等于 `expr2`，返回 null，否则返回 `expr1`

### nvl(expr1, expr2)

Returns `expr2` if `expr1` is null, or `expr1` otherwise.

如果 `expr1` 为 null, 返回 `expr2`，否则返回 `expr1`

### nvl2(expr1, expr2, expr3)

Returns `expr2` if `expr1` is not null, or `expr3` otherwise.

如果 `expr1` 不为 null, 返回 `expr2`，否则返回 `expr3`

### CASE WHEN expr1 THEN expr2 [WHEN expr3 THEN expr4]* [ELSE expr5] END

When `expr1` = true, returns `expr2`; else when `expr3` = true, returns `expr4`; else returns `expr5`.

## Examples

```sql
-- coalesce
SELECT coalesce(NULL, 1, NULL);
+-----------------------+
|coalesce(NULL, 1, NULL)|
+-----------------------+
|                      1|
+-----------------------+

-- if
SELECT if(1 < 2, 'a', 'b');
+-------------------+
|(IF((1 < 2), a, b))|
+-------------------+
|                  a|
+-------------------+

-- ifnull
SELECT ifnull(NULL, array('2'));
+----------------------+
|ifnull(NULL, array(2))|
+----------------------+
|                   [2]|
+----------------------+

-- nanvl
SELECT nanvl(cast('NaN' as double), 123);
+-------------------------------+
|nanvl(CAST(NaN AS DOUBLE), 123)|
+-------------------------------+
|                          123.0|
+-------------------------------+

-- nullif
SELECT nullif(2, 2);
+------------+
|nullif(2, 2)|
+------------+
|        null|
+------------+

-- nvl
SELECT nvl(NULL, array('2'));
+-------------------+
|nvl(NULL, array(2))|
+-------------------+
|                [2]|
+-------------------+

-- nvl2
SELECT nvl2(NULL, 2, 1);
+----------------+
|nvl2(NULL, 2, 1)|
+----------------+
|               1|
+----------------+

-- when
SELECT CASE WHEN 1 > 0 THEN 1 WHEN 2 > 0 THEN 2.0 ELSE 1.2 END;
+-----------------------------------------------------------+
|CASE WHEN (1 > 0) THEN 1 WHEN (2 > 0) THEN 2.0 ELSE 1.2 END|
+-----------------------------------------------------------+
|                                                        1.0|
+-----------------------------------------------------------+

SELECT CASE WHEN 1 < 0 THEN 1 WHEN 2 > 0 THEN 2.0 ELSE 1.2 END;
+-----------------------------------------------------------+
|CASE WHEN (1 < 0) THEN 1 WHEN (2 > 0) THEN 2.0 ELSE 1.2 END|
+-----------------------------------------------------------+
|                                                        2.0|
+-----------------------------------------------------------+

SELECT CASE WHEN 1 < 0 THEN 1 WHEN 2 < 0 THEN 2.0 END;
+--------------------------------------------------+
|CASE WHEN (1 < 0) THEN 1 WHEN (2 < 0) THEN 2.0 END|
+--------------------------------------------------+
|                                              null|
+--------------------------------------------------+
```