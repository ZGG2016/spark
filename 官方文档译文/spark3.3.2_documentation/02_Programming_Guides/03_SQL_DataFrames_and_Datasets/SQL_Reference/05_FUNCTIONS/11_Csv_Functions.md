# Csv Functions

[TOC]

## Csv Functions

### from_csv(csvStr, schema[, options])

Returns a struct value with the given `csvStr` and `schema`.

根据  `csvStr` 和 `schema`，返回结构体值。

### schema_of_csv(csv[, options])

Returns schema in the DDL format of CSV string.

返回 CSV 字符串的 DDL 的结构

### to_csv(expr[, options])

Returns a CSV string with a given struct value

根据结构体值，返回 CSV 字符串

## Examples

```sql
-- from_csv
SELECT from_csv('1, 0.8', 'a INT, b DOUBLE');
+----------------+
|from_csv(1, 0.8)|
+----------------+
|        {1, 0.8}|
+----------------+

SELECT from_csv('26/08/2015', 'time Timestamp', map('timestampFormat', 'dd/MM/yyyy'));
+--------------------+
|from_csv(26/08/2015)|
+--------------------+
|{2015-08-26 00:00...|
+--------------------+

-- schema_of_csv
SELECT schema_of_csv('1,abc');
+--------------------+
|schema_of_csv(1,abc)|
+--------------------+
|STRUCT<_c0: INT, ...|
+--------------------+

-- to_csv
SELECT to_csv(named_struct('a', 1, 'b', 2));
+--------------------------------+
|to_csv(named_struct(a, 1, b, 2))|
+--------------------------------+
|                             1,2|
+--------------------------------+

SELECT to_csv(named_struct('time', to_timestamp('2015-08-26', 'yyyy-MM-dd')), map('timestampFormat', 'dd/MM/yyyy'));
+----------------------------------------------------------------+
|to_csv(named_struct(time, to_timestamp(2015-08-26, yyyy-MM-dd)))|
+----------------------------------------------------------------+
|                                                      26/08/2015|
+----------------------------------------------------------------+
```