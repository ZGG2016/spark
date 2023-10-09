# Mathematical Functions

[TOC]

## Mathematical Functions

### expr1 % expr2

Returns the remainder after `expr1`/`expr2`.

### expr1 * expr2

	Returns `expr1`*`expr2`.

### expr1 + expr2

Returns `expr1`+`expr2`.

### expr1 - expr2

Returns `expr1`-`expr2`.

### expr1 / expr2

Returns `expr1`/`expr2`. It always performs floating point division.

### abs(expr)

Returns the absolute value of the numeric or interval value.

### acos(expr)

Returns the inverse cosine (a.k.a. arc cosine) of `expr`, as if computed by `java.lang.Math.acos`.

### acosh(expr)

Returns inverse hyperbolic cosine of `expr`.

### asin(expr)

Returns the inverse sine (a.k.a. arc sine) the arc sin of `expr`, as if computed by `java.lang.Math.asin`.

### asinh(expr)

Returns inverse hyperbolic sine of `expr`.

### atan(expr)

Returns the inverse tangent (a.k.a. arc tangent) of `expr`, as if computed by `java.lang.Math.atan`
### atan2(exprY, exprX)

Returns the angle in radians between the positive x-axis of a plane and the point given by the coordinates (`exprX`, `exprY`), as if computed by `java.lang.Math.atan2`.

### atanh(expr)

Returns inverse hyperbolic tangent of `expr`.

### bin(expr)

Returns the string representation of the long value `expr` represented in binary.

### bround(expr, d)

Returns `expr` rounded to `d` decimal places using HALF_EVEN rounding mode.

### cbrt(expr)

Returns the cube root of `expr`.

### ceil(expr[, scale])

Returns the smallest number after rounding up that is not smaller than `expr`. An optional `scale` parameter can be specified to control the rounding behavior.

### ceiling(expr[, scale])

Returns the smallest number after rounding up that is not smaller than `expr`. An optional `scale` parameter can be specified to control the rounding behavior.

### conv(num, from_base, to_base)

Convert `num` from `from_base` to `to_base`.

### cos(expr)

Returns the cosine of `expr`, as if computed by `java.lang.Math.cos`.

### cosh(expr)

Returns the hyperbolic cosine of `expr`, as if computed by `java.lang.Math.cosh`.

### cot(expr)

Returns the cotangent of `expr`, as if computed by `1/java.lang.Math.tan`.

### csc(expr)

Returns the cosecant of `expr`, as if computed by `1/java.lang.Math.sin`.

### degrees(expr)

Converts radians to degrees.

### expr1 div expr2

Divide `expr1` by `expr2`. It returns NULL if an operand is NULL or `expr2` is 0. The result is casted to long.

### e()

Returns Euler's number, e.

### exp(expr)

Returns e to the power of `expr`.

### expm1(expr)

Returns exp(`expr`)	

### factorial(expr)

Returns the factorial of `expr`. `expr` is [0..20]. Otherwise, null.

### floor(expr[, scale])

Returns the largest number after rounding down that is not greater than `expr`. An optional `scale` parameter can be specified to control the rounding behavior.

### greatest(expr, ...)

Returns the greatest value of all parameters, skipping null values.

### hex(expr)

Converts `expr` to hexadecimal.

### hypot(expr1, expr2)

Returns sqrt(`expr1`**2 + `expr2`**2).

### least(expr, ...)

Returns the least value of all parameters, skipping null values.

### ln(expr)

Returns the natural logarithm (base e) of `expr`.

### log(base, expr)

Returns the logarithm of `expr` with `base`.

### log10(expr)

Returns the logarithm of `expr` with base 10.

### log1p(expr)

Returns log(1 + `expr`).

### log2(expr)

Returns the logarithm of `expr` with base 2.

### expr1 mod expr2

Returns the remainder after `expr1`/`expr2`.

### negative(expr)

Returns the negated value of `expr`.

### pi()

Returns pi.

### pmod(expr1, expr2)

Returns the positive value of `expr1` mod `expr2`.

### positive(expr)

Returns the value of `expr`.

### pow(expr1, expr2)

Raises `expr1` to the power of `expr2`.

### power(expr1, expr2)

Raises `expr1` to the power of `expr2`.

### radians(expr)

Converts degrees to radians.

### rand([seed])

Returns a random value with independent and identically distributed (i.i.d.) uniformly distributed values in [0, 1).

### randn([seed])

Returns a random value with independent and identically distributed (i.i.d.) values drawn from the standard normal distribution.

### random([seed])

Returns a random value with independent and identically distributed (i.i.d.) uniformly distributed values in [0, 1).

### rint(expr)

Returns the double value that is closest in value to the argument and is equal to a mathematical integer.

### round(expr, d)

Returns `expr` rounded to `d` decimal places using HALF_UP rounding mode.

### sec(expr)

Returns the secant of `expr`, as if computed by `1/java.lang.Math.cos`.

### shiftleft(base, expr)

Bitwise left shift.

### sign(expr)

Returns -1.0, 0.0 or 1.0 as `expr` is negative, 0 or positive.

### signum(expr)

Returns -1.0, 0.0 or 1.0 as `expr` is negative, 0 or positive.

### sin(expr)

Returns the sine of `expr`, as if computed by `java.lang.Math.sin`.

### sinh(expr)

Returns hyperbolic sine of `expr`, as if computed by `java.lang.Math.sinh`.

### sqrt(expr)

Returns the square root of `expr`.

### tan(expr)

Returns the tangent of `expr`, as if computed by `java.lang.Math.tan`.

### tanh(expr)

Returns the hyperbolic tangent of `expr`, as if computed by `java.lang.Math.tanh`.

### try_add(expr1, expr2)

Returns the sum of `expr1`and `expr2` and the result is null on overflow. The acceptable input types are the same with the `+` operator.

### try_divide(dividend, divisor)

Returns `dividend`/`divisor`. It always performs floating point division. Its result is always null if `expr2` is 0. `dividend` must be a numeric or an interval. `divisor` must be a numeric.

### try_multiply(expr1, expr2)

	Returns `expr1`*`expr2` and the result is null on overflow. The acceptable input types are the same with the `*` operator.

### try_subtract(expr1, expr2)

Returns `expr1`-`expr2` and the result is null on overflow. The acceptable input types are the same with the `-` operator.

### unhex(expr)

Converts hexadecimal `expr` to binary.

### width_bucket(value, min_value, max_value, num_bucket)

Returns the bucket number to which `value` would be assigned in an equiwidth histogram with `num_bucket` buckets, in the range `min_value` to `max_value`."

## Examples

```sql
-- %
SELECT 2 % 1.8;
+---------+
|(2 % 1.8)|
+---------+
|      0.2|
+---------+

SELECT MOD(2, 1.8);
+---------+
|(2 % 1.8)|
+---------+
|      0.2|
+---------+

-- *
SELECT 2 * 3;
+-------+
|(2 * 3)|
+-------+
|      6|
+-------+

-- +
SELECT 1 + 2;
+-------+
|(1 + 2)|
+-------+
|      3|
+-------+

-- -
SELECT 2 - 1;
+-------+
|(2 - 1)|
+-------+
|      1|
+-------+

-- /
SELECT 3 / 2;
+-------+
|(3 / 2)|
+-------+
|    1.5|
+-------+

SELECT 2L / 2L;
+-------+
|(2 / 2)|
+-------+
|    1.0|
+-------+

-- abs
SELECT abs(-1);
+-------+
|abs(-1)|
+-------+
|      1|
+-------+

SELECT abs(INTERVAL -'1-1' YEAR TO MONTH);
+----------------------------------+
|abs(INTERVAL '-1-1' YEAR TO MONTH)|
+----------------------------------+
|              INTERVAL '1-1' YE...|
+----------------------------------+

-- acos
SELECT acos(1);
+-------+
|ACOS(1)|
+-------+
|    0.0|
+-------+

SELECT acos(2);
+-------+
|ACOS(2)|
+-------+
|    NaN|
+-------+

-- acosh
SELECT acosh(1);
+--------+
|ACOSH(1)|
+--------+
|     0.0|
+--------+

SELECT acosh(0);
+--------+
|ACOSH(0)|
+--------+
|     NaN|
+--------+

-- asin
SELECT asin(0);
+-------+
|ASIN(0)|
+-------+
|    0.0|
+-------+

SELECT asin(2);
+-------+
|ASIN(2)|
+-------+
|    NaN|
+-------+

-- asinh
SELECT asinh(0);
+--------+
|ASINH(0)|
+--------+
|     0.0|
+--------+

-- atan
SELECT atan(0);
+-------+
|ATAN(0)|
+-------+
|    0.0|
+-------+

-- atan2
SELECT atan2(0, 0);
+-----------+
|ATAN2(0, 0)|
+-----------+
|        0.0|
+-----------+

-- atanh
SELECT atanh(0);
+--------+
|ATANH(0)|
+--------+
|     0.0|
+--------+

SELECT atanh(2);
+--------+
|ATANH(2)|
+--------+
|     NaN|
+--------+

-- bin
SELECT bin(13);
+-------+
|bin(13)|
+-------+
|   1101|
+-------+

SELECT bin(-13);
+--------------------+
|            bin(-13)|
+--------------------+
|11111111111111111...|
+--------------------+

SELECT bin(13.3);
+---------+
|bin(13.3)|
+---------+
|     1101|
+---------+

-- bround
SELECT bround(2.5, 0);
+--------------+
|bround(2.5, 0)|
+--------------+
|             2|
+--------------+

SELECT bround(25, -1);
+--------------+
|bround(25, -1)|
+--------------+
|            20|
+--------------+

-- cbrt
SELECT cbrt(27.0);
+----------+
|CBRT(27.0)|
+----------+
|       3.0|
+----------+

-- ceil
SELECT ceil(-0.1);
+----------+
|CEIL(-0.1)|
+----------+
|         0|
+----------+

SELECT ceil(5);
+-------+
|CEIL(5)|
+-------+
|      5|
+-------+

SELECT ceil(3.1411, 3);
+---------------+
|ceil(3.1411, 3)|
+---------------+
|          3.142|
+---------------+

SELECT ceil(3.1411, -3);
+----------------+
|ceil(3.1411, -3)|
+----------------+
|            1000|
+----------------+

-- ceiling
SELECT ceiling(-0.1);
+-------------+
|ceiling(-0.1)|
+-------------+
|            0|
+-------------+

SELECT ceiling(5);
+----------+
|ceiling(5)|
+----------+
|         5|
+----------+

SELECT ceiling(3.1411, 3);
+------------------+
|ceiling(3.1411, 3)|
+------------------+
|             3.142|
+------------------+

SELECT ceiling(3.1411, -3);
+-------------------+
|ceiling(3.1411, -3)|
+-------------------+
|               1000|
+-------------------+

-- conv
SELECT conv('100', 2, 10);
+----------------+
|conv(100, 2, 10)|
+----------------+
|               4|
+----------------+

SELECT conv(-10, 16, -10);
+------------------+
|conv(-10, 16, -10)|
+------------------+
|               -16|
+------------------+

-- cos
SELECT cos(0);
+------+
|COS(0)|
+------+
|   1.0|
+------+

-- cosh
SELECT cosh(0);
+-------+
|COSH(0)|
+-------+
|    1.0|
+-------+

-- cot
SELECT cot(1);
+------------------+
|            COT(1)|
+------------------+
|0.6420926159343306|
+------------------+

-- csc
SELECT csc(1);
+------------------+
|            CSC(1)|
+------------------+
|1.1883951057781212|
+------------------+

-- degrees
SELECT degrees(3.141592653589793);
+--------------------------+
|DEGREES(3.141592653589793)|
+--------------------------+
|                     180.0|
+--------------------------+

-- div
SELECT 3 div 2;
+---------+
|(3 div 2)|
+---------+
|        1|
+---------+

SELECT INTERVAL '1-1' YEAR TO MONTH div INTERVAL '-1' MONTH;
+------------------------------------------------------+
|(INTERVAL '1-1' YEAR TO MONTH div INTERVAL '-1' MONTH)|
+------------------------------------------------------+
|                                                   -13|
+------------------------------------------------------+

-- e
SELECT e();
+-----------------+
|              E()|
+-----------------+
|2.718281828459045|
+-----------------+

-- exp
SELECT exp(0);
+------+
|EXP(0)|
+------+
|   1.0|
+------+

-- expm1
SELECT expm1(0);
+--------+
|EXPM1(0)|
+--------+
|     0.0|
+--------+

-- factorial
SELECT factorial(5);
+------------+
|factorial(5)|
+------------+
|         120|
+------------+

-- floor
SELECT floor(-0.1);
+-----------+
|FLOOR(-0.1)|
+-----------+
|         -1|
+-----------+

SELECT floor(5);
+--------+
|FLOOR(5)|
+--------+
|       5|
+--------+

SELECT floor(3.1411, 3);
+----------------+
|floor(3.1411, 3)|
+----------------+
|           3.141|
+----------------+

SELECT floor(3.1411, -3);
+-----------------+
|floor(3.1411, -3)|
+-----------------+
|                0|
+-----------------+

-- greatest
SELECT greatest(10, 9, 2, 4, 3);
+------------------------+
|greatest(10, 9, 2, 4, 3)|
+------------------------+
|                      10|
+------------------------+

-- hex
SELECT hex(17);
+-------+
|hex(17)|
+-------+
|     11|
+-------+

SELECT hex('Spark SQL');
+------------------+
|    hex(Spark SQL)|
+------------------+
|537061726B2053514C|
+------------------+

-- hypot
SELECT hypot(3, 4);
+-----------+
|HYPOT(3, 4)|
+-----------+
|        5.0|
+-----------+

-- least
SELECT least(10, 9, 2, 4, 3);
+---------------------+
|least(10, 9, 2, 4, 3)|
+---------------------+
|                    2|
+---------------------+

-- ln
SELECT ln(1);
+-----+
|ln(1)|
+-----+
|  0.0|
+-----+

-- log
SELECT log(10, 100);
+------------+
|LOG(10, 100)|
+------------+
|         2.0|
+------------+

-- log10
SELECT log10(10);
+---------+
|LOG10(10)|
+---------+
|      1.0|
+---------+

-- log1p
SELECT log1p(0);
+--------+
|LOG1P(0)|
+--------+
|     0.0|
+--------+

-- log2
SELECT log2(2);
+-------+
|LOG2(2)|
+-------+
|    1.0|
+-------+

-- mod
SELECT 2 % 1.8;
+---------+
|(2 % 1.8)|
+---------+
|      0.2|
+---------+

SELECT MOD(2, 1.8);
+---------+
|(2 % 1.8)|
+---------+
|      0.2|
+---------+

-- negative
SELECT negative(1);
+-----------+
|negative(1)|
+-----------+
|         -1|
+-----------+

-- pi
SELECT pi();
+-----------------+
|             PI()|
+-----------------+
|3.141592653589793|
+-----------------+

-- pmod
SELECT pmod(10, 3);
+-----------+
|pmod(10, 3)|
+-----------+
|          1|
+-----------+

SELECT pmod(-10, 3);
+------------+
|pmod(-10, 3)|
+------------+
|           2|
+------------+

-- positive
SELECT positive(1);
+-----+
|(+ 1)|
+-----+
|    1|
+-----+

-- pow
SELECT pow(2, 3);
+---------+
|pow(2, 3)|
+---------+
|      8.0|
+---------+

-- power
SELECT power(2, 3);
+-----------+
|POWER(2, 3)|
+-----------+
|        8.0|
+-----------+

-- radians
SELECT radians(180);
+-----------------+
|     RADIANS(180)|
+-----------------+
|3.141592653589793|
+-----------------+

-- rand
SELECT rand();
+-------------------+
|             rand()|
+-------------------+
|0.09109379190094202|
+-------------------+

SELECT rand(0);
+------------------+
|           rand(0)|
+------------------+
|0.7604953758285915|
+------------------+

SELECT rand(null);
+------------------+
|        rand(NULL)|
+------------------+
|0.7604953758285915|
+------------------+

-- randn
SELECT randn();
+-------------------+
|            randn()|
+-------------------+
|-1.0673051133316651|
+-------------------+

SELECT randn(0);
+------------------+
|          randn(0)|
+------------------+
|1.6034991609278433|
+------------------+

SELECT randn(null);
+------------------+
|       randn(NULL)|
+------------------+
|1.6034991609278433|
+------------------+

-- random
SELECT random();
+------------------+
|            rand()|
+------------------+
|0.4731149010070248|
+------------------+

SELECT random(0);
+------------------+
|           rand(0)|
+------------------+
|0.7604953758285915|
+------------------+

SELECT random(null);
+------------------+
|        rand(NULL)|
+------------------+
|0.7604953758285915|
+------------------+

-- rint
SELECT rint(12.3456);
+-------------+
|rint(12.3456)|
+-------------+
|         12.0|
+-------------+

-- round
SELECT round(2.5, 0);
+-------------+
|round(2.5, 0)|
+-------------+
|            3|
+-------------+

SELECT round(25, -1);
+-------------+
|round(25, -1)|
+-------------+
|           30|
+-------------+

-- sec
SELECT sec(0);
+------+
|SEC(0)|
+------+
|   1.0|
+------+

-- shiftleft
SELECT shiftleft(2, 1);
+---------------+
|shiftleft(2, 1)|
+---------------+
|              4|
+---------------+

-- sign
SELECT sign(40);
+--------+
|sign(40)|
+--------+
|     1.0|
+--------+

SELECT sign(INTERVAL -'100' YEAR);
+--------------------------+
|sign(INTERVAL '-100' YEAR)|
+--------------------------+
|                      -1.0|
+--------------------------+

-- signum
SELECT signum(40);
+----------+
|SIGNUM(40)|
+----------+
|       1.0|
+----------+

SELECT signum(INTERVAL -'100' YEAR);
+----------------------------+
|SIGNUM(INTERVAL '-100' YEAR)|
+----------------------------+
|                        -1.0|
+----------------------------+

-- sin
SELECT sin(0);
+------+
|SIN(0)|
+------+
|   0.0|
+------+

-- sinh
SELECT sinh(0);
+-------+
|SINH(0)|
+-------+
|    0.0|
+-------+

-- sqrt
SELECT sqrt(4);
+-------+
|SQRT(4)|
+-------+
|    2.0|
+-------+

-- tan
SELECT tan(0);
+------+
|TAN(0)|
+------+
|   0.0|
+------+

-- tanh
SELECT tanh(0);
+-------+
|TANH(0)|
+-------+
|    0.0|
+-------+

-- try_add
SELECT try_add(1, 2);
+-------------+
|try_add(1, 2)|
+-------------+
|            3|
+-------------+

SELECT try_add(2147483647, 1);
+----------------------+
|try_add(2147483647, 1)|
+----------------------+
|                  null|
+----------------------+

SELECT try_add(date'2021-01-01', 1);
+-----------------------------+
|try_add(DATE '2021-01-01', 1)|
+-----------------------------+
|                   2021-01-02|
+-----------------------------+

SELECT try_add(date'2021-01-01', interval 1 year);
+---------------------------------------------+
|try_add(DATE '2021-01-01', INTERVAL '1' YEAR)|
+---------------------------------------------+
|                                   2022-01-01|
+---------------------------------------------+

SELECT try_add(timestamp'2021-01-01 00:00:00', interval 1 day);
+----------------------------------------------------------+
|try_add(TIMESTAMP '2021-01-01 00:00:00', INTERVAL '1' DAY)|
+----------------------------------------------------------+
|                                       2021-01-02 00:00:00|
+----------------------------------------------------------+

SELECT try_add(interval 1 year, interval 2 year);
+---------------------------------------------+
|try_add(INTERVAL '1' YEAR, INTERVAL '2' YEAR)|
+---------------------------------------------+
|                            INTERVAL '3' YEAR|
+---------------------------------------------+

-- try_divide
SELECT try_divide(3, 2);
+----------------+
|try_divide(3, 2)|
+----------------+
|             1.5|
+----------------+

SELECT try_divide(2L, 2L);
+----------------+
|try_divide(2, 2)|
+----------------+
|             1.0|
+----------------+

SELECT try_divide(1, 0);
+----------------+
|try_divide(1, 0)|
+----------------+
|            null|
+----------------+

SELECT try_divide(interval 2 month, 2);
+---------------------------------+
|try_divide(INTERVAL '2' MONTH, 2)|
+---------------------------------+
|             INTERVAL '0-1' YE...|
+---------------------------------+

SELECT try_divide(interval 2 month, 0);
+---------------------------------+
|try_divide(INTERVAL '2' MONTH, 0)|
+---------------------------------+
|                             null|
+---------------------------------+

-- try_multiply
SELECT try_multiply(2, 3);
+------------------+
|try_multiply(2, 3)|
+------------------+
|                 6|
+------------------+

SELECT try_multiply(-2147483648, 10);
+-----------------------------+
|try_multiply(-2147483648, 10)|
+-----------------------------+
|                         null|
+-----------------------------+

SELECT try_multiply(interval 2 year, 3);
+----------------------------------+
|try_multiply(INTERVAL '2' YEAR, 3)|
+----------------------------------+
|              INTERVAL '6-0' YE...|
+----------------------------------+

-- try_subtract
SELECT try_subtract(2, 1);
+------------------+
|try_subtract(2, 1)|
+------------------+
|                 1|
+------------------+

SELECT try_subtract(-2147483648, 1);
+----------------------------+
|try_subtract(-2147483648, 1)|
+----------------------------+
|                        null|
+----------------------------+

SELECT try_subtract(date'2021-01-02', 1);
+----------------------------------+
|try_subtract(DATE '2021-01-02', 1)|
+----------------------------------+
|                        2021-01-01|
+----------------------------------+

SELECT try_subtract(date'2021-01-01', interval 1 year);
+--------------------------------------------------+
|try_subtract(DATE '2021-01-01', INTERVAL '1' YEAR)|
+--------------------------------------------------+
|                                        2020-01-01|
+--------------------------------------------------+

SELECT try_subtract(timestamp'2021-01-02 00:00:00', interval 1 day);
+---------------------------------------------------------------+
|try_subtract(TIMESTAMP '2021-01-02 00:00:00', INTERVAL '1' DAY)|
+---------------------------------------------------------------+
|                                            2021-01-01 00:00:00|
+---------------------------------------------------------------+

SELECT try_subtract(interval 2 year, interval 1 year);
+--------------------------------------------------+
|try_subtract(INTERVAL '2' YEAR, INTERVAL '1' YEAR)|
+--------------------------------------------------+
|                                 INTERVAL '1' YEAR|
+--------------------------------------------------+

-- unhex
SELECT decode(unhex('537061726B2053514C'), 'UTF-8');
+----------------------------------------+
|decode(unhex(537061726B2053514C), UTF-8)|
+----------------------------------------+
|                               Spark SQL|
+----------------------------------------+

-- width_bucket
SELECT width_bucket(5.3, 0.2, 10.6, 5);
+-------------------------------+
|width_bucket(5.3, 0.2, 10.6, 5)|
+-------------------------------+
|                              3|
+-------------------------------+

SELECT width_bucket(-2.1, 1.3, 3.4, 3);
+-------------------------------+
|width_bucket(-2.1, 1.3, 3.4, 3)|
+-------------------------------+
|                              0|
+-------------------------------+

SELECT width_bucket(8.1, 0.0, 5.7, 4);
+------------------------------+
|width_bucket(8.1, 0.0, 5.7, 4)|
+------------------------------+
|                             5|
+------------------------------+

SELECT width_bucket(-0.9, 5.2, 0.5, 2);
+-------------------------------+
|width_bucket(-0.9, 5.2, 0.5, 2)|
+-------------------------------+
|                              3|
+-------------------------------+

SELECT width_bucket(INTERVAL '0' YEAR, INTERVAL '0' YEAR, INTERVAL '10' YEAR, 10);
+--------------------------------------------------------------------------+
|width_bucket(INTERVAL '0' YEAR, INTERVAL '0' YEAR, INTERVAL '10' YEAR, 10)|
+--------------------------------------------------------------------------+
|                                                                         1|
+--------------------------------------------------------------------------+

SELECT width_bucket(INTERVAL '1' YEAR, INTERVAL '0' YEAR, INTERVAL '10' YEAR, 10);
+--------------------------------------------------------------------------+
|width_bucket(INTERVAL '1' YEAR, INTERVAL '0' YEAR, INTERVAL '10' YEAR, 10)|
+--------------------------------------------------------------------------+
|                                                                         2|
+--------------------------------------------------------------------------+

SELECT width_bucket(INTERVAL '0' DAY, INTERVAL '0' DAY, INTERVAL '10' DAY, 10);
+-----------------------------------------------------------------------+
|width_bucket(INTERVAL '0' DAY, INTERVAL '0' DAY, INTERVAL '10' DAY, 10)|
+-----------------------------------------------------------------------+
|                                                                      1|
+-----------------------------------------------------------------------+

SELECT width_bucket(INTERVAL '1' DAY, INTERVAL '0' DAY, INTERVAL '10' DAY, 10);
+-----------------------------------------------------------------------+
|width_bucket(INTERVAL '1' DAY, INTERVAL '0' DAY, INTERVAL '10' DAY, 10)|
+-----------------------------------------------------------------------+
|                                                                      2|
+-----------------------------------------------------------------------+
```