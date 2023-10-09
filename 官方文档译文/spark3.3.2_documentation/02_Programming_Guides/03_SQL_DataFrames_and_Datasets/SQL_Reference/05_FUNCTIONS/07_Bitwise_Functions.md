# Bitwise Functions

[TOC]

## Bitwise Functions

### expr1 & expr2

Returns the result of bitwise AND of `expr1` and `expr2`.

### expr1 ^ expr2

Returns the result of bitwise exclusive OR of `expr1` and `expr2`.

### bit_count(expr)

Returns the number of bits that are set in the argument expr as an unsigned 64-bit integer, or NULL if the argument is NULL.

### bit_get(expr, pos)

Returns the value of the bit (0 or 1) at the specified position. The positions are numbered from right to left, starting at zero. The position argument cannot be negative.

### getbit(expr, pos)

Returns the value of the bit (0 or 1) at the specified position. The positions are numbered from right to left, starting at zero. The position argument cannot be negative.

### shiftright(base, expr)

Bitwise (signed) right shift.

### shiftrightunsigned(base, expr)

Bitwise unsigned right shift.

### `expr1 | expr2`

Returns the result of bitwise OR of `expr1` and `expr2`.

### ~ expr

Returns the result of bitwise NOT of `expr`.


## Examples

```sql
-- &
SELECT 3 & 5;
+-------+
|(3 & 5)|
+-------+
|      1|
+-------+

-- ^
SELECT 3 ^ 5;
+-------+
|(3 ^ 5)|
+-------+
|      6|
+-------+

-- bit_count
SELECT bit_count(0);
+------------+
|bit_count(0)|
+------------+
|           0|
+------------+

-- bit_get
SELECT bit_get(11, 0);
+--------------+
|bit_get(11, 0)|
+--------------+
|             1|
+--------------+

SELECT bit_get(11, 2);
+--------------+
|bit_get(11, 2)|
+--------------+
|             0|
+--------------+

-- getbit
SELECT getbit(11, 0);
+-------------+
|getbit(11, 0)|
+-------------+
|            1|
+-------------+

SELECT getbit(11, 2);
+-------------+
|getbit(11, 2)|
+-------------+
|            0|
+-------------+

-- shiftright
SELECT shiftright(4, 1);
+----------------+
|shiftright(4, 1)|
+----------------+
|               2|
+----------------+

-- shiftrightunsigned
SELECT shiftrightunsigned(4, 1);
+------------------------+
|shiftrightunsigned(4, 1)|
+------------------------+
|                       2|
+------------------------+

-- |
SELECT 3 | 5;
+-------+
|(3 | 5)|
+-------+
|      7|
+-------+

-- ~
SELECT ~ 0;
+---+
| ~0|
+---+
| -1|
+---+
```