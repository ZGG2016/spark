# Conversion Functions

[TOC]

## Conversion Functions

### bigint(expr)

Casts the value `expr` to the target data type `bigint`.

### binary(expr)

Casts the value `expr` to the target data type `binary`.

### boolean(expr)

Casts the value `expr` to the target data type `boolean`.

### cast(expr AS type)

Casts the value `expr` to the target data type `type`.

把 expr 转成目标类型 type

### date(expr)

Casts the value `expr` to the target data type `date`.

### decimal(expr)

Casts the value `expr` to the target data type `decimal`.

### double(expr)

Casts the value `expr` to the target data type `double`.

### float(expr)

Casts the value `expr` to the target data type `float`.

### int(expr)

Casts the value `expr` to the target data type `int`.

### smallint(expr)

Casts the value `expr` to the target data type `smallint`.

### string(expr)

Casts the value `expr` to the target data type `string`.

### timestamp(expr)

Casts the value `expr` to the target data type `timestamp`.

### tinyint(expr)

Casts the value `expr` to the target data type `tinyint`.

## Examples

```sql
-- cast
SELECT cast('10' as int);
+---------------+
|CAST(10 AS INT)|
+---------------+
|             10|
+---------------+
```