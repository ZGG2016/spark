# Aggregate Functions

[TOC]

## Aggregate Functions

### any(expr)

Returns true if at least one value of `expr` is true.

只要 `expr` 有一个值是 true, 就返回 true

### approx_count_distinct(expr[, relativeSD])

Returns the estimated cardinality by HyperLogLog++. `relativeSD` defines the maximum relative standard deviation allowed.

返回由 HyperLogLog++ 估计的数量。

`relativeSD` 定义了允许的最大相对标准差

### approx_percentile(col, percentage [, accuracy])

Returns the approximate `percentile` of the numeric or ansi interval column `col` which is the smallest value in the ordered `col` values (sorted from least to greatest) such that no more than `percentage` of `col` values is less than the value or equal to that value. The value of percentage must be between 0.0 and 1.0. The `accuracy` parameter (default: 10000) is a positive numeric literal which controls approximation accuracy at the cost of memory. Higher value of `accuracy` yields better accuracy, `1.0/accuracy` is the relative error of the approximation. When `percentage` is an array, each value of the percentage array must be between 0.0 and 1.0. In this case, returns the approximate percentile array of column `col` at the given percentage array.

### array_agg(expr)

Collects and returns a list of non-unique elements.

收集并返回非唯一元素的列表（不去重）

### avg(expr)

Returns the mean calculated from values of a group.

返回一组值的平均值

### bit_and(expr)

Returns the bitwise AND of all non-null input values, or null if none.

返回所有非 null 输入值的位与，如果是 none，返回 null

### bit_or(expr)

Returns the bitwise OR of all non-null input values, or null if none.

返回所有非 null 输入值的位或，如果是 none，返回 null

### bit_xor(expr)

Returns the bitwise XOR of all non-null input values, or null if none.

返回所有非 null 输入值的异或，如果是 none，返回 null

### bool_and(expr)

Returns true if all values of `expr` are true.

如果 `expr` 的所有值是 true, 返回 true

### bool_or(expr)

Returns true if at least one value of `expr` is true.

如果 `expr` 有一个值是 true, 返回 true

### collect_list(expr)

Collects and returns a list of non-unique elements.

收集并返回非唯一元素的列表（不去重）

### collect_set(expr)

Collects and returns a set of unique elements.

收集并返回唯一元素的集合（去重）

### corr(expr1, expr2)

Returns Pearson coefficient of correlation between a set of number pairs.

返回一组数字对的 Pearson 相关系数

### `count(*)`

Returns the total number of retrieved rows, including rows containing null.

返回检索行的总数量，包含有null的行

### count(expr[, expr...])

Returns the number of rows for which the supplied expression(s) are all non-null.

返回提供表达式的值是所有非 null 的行的数量

### count(DISTINCT expr[, expr...])

Returns the number of rows for which the supplied expression(s) are unique and non-null.

返回提供表达式的值是唯一的且非 null 的行的数量

### count_if(expr)

Returns the number of `TRUE` values for the expression.

返回表达式是 `TRUE` 的行的数量

### count_min_sketch(col, eps, confidence, seed)

Returns a count-min sketch of a column with the given esp, confidence and seed. The result is an array of bytes, which can be deserialized to a `CountMinSketch` before usage. Count-min sketch is a probabilistic data structure used for cardinality estimation using sub-linear space.

### covar_pop(expr1, expr2)

Returns the population covariance of a set of number pairs.

返回一组数字对的总体协方差。

### covar_samp(expr1, expr2)

Returns the sample covariance of a set of number pairs.

返回一组数字对的样本协方差。

### every(expr)

Returns true if all values of `expr` are true.

如果 `expr` 的所有值都是 true, 返回 true

### first(expr[, isIgnoreNull])

Returns the first value of `expr` for a group of rows. If `isIgnoreNull` is true, returns only non-null values.

对于一组行，返回 `expr` 的第一个值。

如果 `isIgnoreNull` 是 true, 仅返回非 null 值。

### first_value(expr[, isIgnoreNull])

Returns the first value of `expr` for a group of rows. If `isIgnoreNull` is true, returns only non-null values.

对于一组行，返回 `expr` 的第一个值。

如果 `isIgnoreNull` 是 true, 仅返回非 null 值。

### grouping(col)

indicates whether a specified column in a GROUP BY is aggregated or not, returns 1 for aggregated or 0 for not aggregated in the result set.",

表示 GROUP BY 中指定的列是否被聚合，聚合的话返回1，不聚合的话返回0

### grouping_id([col1[, col2 ..]])

returns the level of grouping, equals to `(grouping(c1) << (n-1)) + (grouping(c2) << (n-2)) + ... + grouping(cn)`

返回分组的级别。

### histogram_numeric(expr, nb)

Computes a histogram on numeric 'expr' using nb bins. The return value is an array of (x,y) pairs representing the centers of the histogram's bins. As the value of 'nb' is increased, the histogram approximation gets finer-grained, but may yield artifacts around outliers. In practice, 20-40 histogram bins appear to work well, with more bins being required for skewed or smaller datasets. Note that this function creates a histogram with non-uniform bin widths. It offers no guarantees in terms of the mean-squared-error of the histogram, but in practice is comparable to the histograms produced by the R/S-Plus statistical computing packages. Note: the output type of the 'x' field in the return value is propagated from the input value consumed in the aggregate function.

基于数值的 'expr', 使用 nb 个箱计算直方图。

返回值是 (x,y) 对的数组，表示直方图箱的中心。

随着 nb 值的增加，直方图近似值成为细粒度，但是可能在异常值周边产生 artifacts.

在实践中，20到40个直方图箱能工作的很好，对于数据倾斜或更小的数据集，使用更多的箱。

注意，这个函数创建的直方图具有不均匀的箱宽。它不能保证直方图的均方误差，但在实践中，与 R/S-Plus 统计计算包产生的直方图相当。

注意: 返回值中'x'字段的输出类型是从聚合函数中消耗的输入值传播的。

### kurtosis(expr)

Returns the kurtosis value calculated from values of a group.

返回根据一组值计算的峰度值。

### last(expr[, isIgnoreNull])

Returns the last value of `expr` for a group of rows. If `isIgnoreNull` is true, returns only non-null values

对于一组行，返回 `expr` 的最后一个值。

如果 `isIgnoreNull` 是 true, 仅返回非 null 值。

### last_value(expr[, isIgnoreNull])

Returns the last value of `expr` for a group of rows. If `isIgnoreNull` is true, returns only non-null values

对于一组行，返回 `expr` 的最后一个值。

如果 `isIgnoreNull` 是 true, 仅返回非 null 值。

### max(expr)

Returns the maximum value of `expr`.

返回 `expr` 的最大值

### max_by(x, y)

Returns the value of `x` associated with the maximum value of `y`.

返回 `y` 的最大值对应的 `x` 的值

### mean(expr)

Returns the mean calculated from values of a group.

返回一组值的均值

### min(expr)

Returns the minimum value of `expr`.

返回 `expr` 的最小值

### min_by(x, y)

Returns the value of `x` associated with the minimum value of `y`.

返回 `y` 的最小值对应的 `x` 的值

### percentile(col, percentage [, frequency])

Returns the exact percentile value of numeric column `col` at the given percentage. The value of percentage must be between 0.0 and 1.0. The value of frequency should be positive integral

根据给定的 percentage, 返回数值列 `col` 的精确的百分位值。

percentage 的值必须在 0.0 和 1.0 之间。

frequency 的值应该是正整数

### percentile(col, array(percentage1 [, percentage2]...) [, frequency])

Returns the exact percentile value array of numeric column `col` at the given percentage(s). Each value of the percentage array must be between 0.0 and 1.0. The value of frequency should be positive integral

根据给定的 percentage(s, 返回数值列 `col` 的精确的百分位值数组。

percentage 数组的每个值必须在 0.0 和 1.0 之间。

frequency 的值应该是正整数

### percentile_approx(col, percentage [, accuracy])

Returns the approximate `percentile` of the numeric or ansi interval column `col` which is the smallest value in the ordered `col` values (sorted from least to greatest) such that no more than `percentage` of `col` values is less than the value or equal to that value. The value of percentage must be between 0.0 and 1.0. The `accuracy` parameter (default: 10000) is a positive numeric literal which controls approximation accuracy at the cost of memory. Higher value of `accuracy` yields better accuracy, `1.0/accuracy` is the relative error of the approximation. When `percentage` is an array, each value of the percentage array must be between 0.0 and 1.0. In this case, returns the approximate percentile array of column `col` at the given percentage array.

### regr_avgx(y, x)

Returns the average of the independent variable for non-null pairs in a group, where `y` is the dependent variable and `x` is the independent variable.

返回组中非 null 对的自变量的平均值，其中 y 是因变量，x 是自变量。

### regr_avgy(y, x)

Returns the average of the dependent variable for non-null pairs in a group, where `y` is the dependent variable and `x` is the independent variable.

返回组中非 null 对的因变量的平均值，其中 y 是因变量，x 是自变量。

### regr_count(y, x)

Returns the number of non-null number pairs in a group, where `y` is the dependent variable and `x` is the independent variable.

返回组中非 null 数字对的数量，其中 y 是因变量，x 是自变量。

### regr_r2(y, x)

Returns the coefficient of determination for non-null pairs in a group, where `y` is the dependent variable and `x` is the independent variable.

返回组中非 null 对的可决系数，其中 y 是因变量，x 是自变量。

### skewness(expr)

Returns the skewness value calculated from values of a group.

从一组值中计算倾斜值

### some(expr)

Returns true if at least one value of `expr` is true.

只要 `expr` 有一个值是 true, 就返回 true

### std(expr)

Returns the sample standard deviation calculated from values of a group.

计算一组值的样本标准差

### stddev(expr)

Returns the sample standard deviation calculated from values of a group.

计算一组值的样本标准差

### stddev_pop(expr)

Returns the population standard deviation calculated from values of a group.

计算一组值的总体标准差

### stddev_samp(expr)

Returns the sample standard deviation calculated from values of a group.

计算一组值的样本标准差

### sum(expr)

Returns the sum calculated from values of a group.

计算一组值的和

### try_avg(expr)

Returns the mean calculated from values of a group and the result is null on overflow.

计算一组值的平均值，溢出时结果为空。

### try_sum(expr)

Returns the sum calculated from values of a group and the result is null on overflow.

计算一组值的和，溢出时结果为空。

### var_pop(expr)

Returns the population variance calculated from values of a group.

计算一组值的总体标准差

### var_samp(expr)

Returns the sample variance calculated from values of a group.

计算一组值的样本标准差

### variance(expr)

Returns the sample variance calculated from values of a group.

计算一组值的样本标准差

## Examples

```sql
-- any
SELECT any(col) FROM VALUES (true), (false), (false) AS tab(col);
+--------+
|any(col)|
+--------+
|    true|
+--------+

SELECT any(col) FROM VALUES (NULL), (true), (false) AS tab(col);
+--------+
|any(col)|
+--------+
|    true|
+--------+

SELECT any(col) FROM VALUES (false), (false), (NULL) AS tab(col);
+--------+
|any(col)|
+--------+
|   false|
+--------+

-- approx_count_distinct
SELECT approx_count_distinct(col1) FROM VALUES (1), (1), (2), (2), (3) tab(col1);
+---------------------------+
|approx_count_distinct(col1)|
+---------------------------+
|                          3|
+---------------------------+

-- approx_percentile
SELECT approx_percentile(col, array(0.5, 0.4, 0.1), 100) FROM VALUES (0), (1), (2), (10) AS tab(col);
+-------------------------------------------------+
|approx_percentile(col, array(0.5, 0.4, 0.1), 100)|
+-------------------------------------------------+
|                                        [1, 1, 0]|
+-------------------------------------------------+

SELECT approx_percentile(col, 0.5, 100) FROM VALUES (0), (6), (7), (9), (10) AS tab(col);
+--------------------------------+
|approx_percentile(col, 0.5, 100)|
+--------------------------------+
|                               7|
+--------------------------------+

SELECT approx_percentile(col, 0.5, 100) FROM VALUES (INTERVAL '0' MONTH), (INTERVAL '1' MONTH), (INTERVAL '2' MONTH), (INTERVAL '10' MONTH) AS tab(col);
+--------------------------------+
|approx_percentile(col, 0.5, 100)|
+--------------------------------+
|              INTERVAL '1' MONTH|
+--------------------------------+

SELECT approx_percentile(col, array(0.5, 0.7), 100) FROM VALUES (INTERVAL '0' SECOND), (INTERVAL '1' SECOND), (INTERVAL '2' SECOND), (INTERVAL '10' SECOND) AS tab(col);
+--------------------------------------------+
|approx_percentile(col, array(0.5, 0.7), 100)|
+--------------------------------------------+
|                        [INTERVAL '01' SE...|
+--------------------------------------------+

-- array_agg
SELECT array_agg(col) FROM VALUES (1), (2), (1) AS tab(col);
+-----------------+
|collect_list(col)|
+-----------------+
|        [1, 2, 1]|
+-----------------+

-- avg
SELECT avg(col) FROM VALUES (1), (2), (3) AS tab(col);
+--------+
|avg(col)|
+--------+
|     2.0|
+--------+

SELECT avg(col) FROM VALUES (1), (2), (NULL) AS tab(col);
+--------+
|avg(col)|
+--------+
|     1.5|
+--------+

-- bit_and
SELECT bit_and(col) FROM VALUES (3), (5) AS tab(col);
+------------+
|bit_and(col)|
+------------+
|           1|
+------------+

-- bit_or
SELECT bit_or(col) FROM VALUES (3), (5) AS tab(col);
+-----------+
|bit_or(col)|
+-----------+
|          7|
+-----------+

-- bit_xor
SELECT bit_xor(col) FROM VALUES (3), (5) AS tab(col);
+------------+
|bit_xor(col)|
+------------+
|           6|
+------------+

-- bool_and
SELECT bool_and(col) FROM VALUES (true), (true), (true) AS tab(col);
+-------------+
|bool_and(col)|
+-------------+
|         true|
+-------------+

SELECT bool_and(col) FROM VALUES (NULL), (true), (true) AS tab(col);
+-------------+
|bool_and(col)|
+-------------+
|         true|
+-------------+

SELECT bool_and(col) FROM VALUES (true), (false), (true) AS tab(col);
+-------------+
|bool_and(col)|
+-------------+
|        false|
+-------------+

-- bool_or
SELECT bool_or(col) FROM VALUES (true), (false), (false) AS tab(col);
+------------+
|bool_or(col)|
+------------+
|        true|
+------------+

SELECT bool_or(col) FROM VALUES (NULL), (true), (false) AS tab(col);
+------------+
|bool_or(col)|
+------------+
|        true|
+------------+

SELECT bool_or(col) FROM VALUES (false), (false), (NULL) AS tab(col);
+------------+
|bool_or(col)|
+------------+
|       false|
+------------+

-- collect_list
SELECT collect_list(col) FROM VALUES (1), (2), (1) AS tab(col);
+-----------------+
|collect_list(col)|
+-----------------+
|        [1, 2, 1]|
+-----------------+

-- collect_set
SELECT collect_set(col) FROM VALUES (1), (2), (1) AS tab(col);
+----------------+
|collect_set(col)|
+----------------+
|          [1, 2]|
+----------------+

-- corr
SELECT corr(c1, c2) FROM VALUES (3, 2), (3, 3), (6, 4) as tab(c1, c2);
+------------------+
|      corr(c1, c2)|
+------------------+
|0.8660254037844387|
+------------------+

-- count
SELECT count(*) FROM VALUES (NULL), (5), (5), (20) AS tab(col);
+--------+
|count(1)|
+--------+
|       4|
+--------+

SELECT count(col) FROM VALUES (NULL), (5), (5), (20) AS tab(col);
+----------+
|count(col)|
+----------+
|         3|
+----------+

SELECT count(DISTINCT col) FROM VALUES (NULL), (5), (5), (10) AS tab(col);
+-------------------+
|count(DISTINCT col)|
+-------------------+
|                  2|
+-------------------+

-- count_if
SELECT count_if(col % 2 = 0) FROM VALUES (NULL), (0), (1), (2), (3) AS tab(col);
+-------------------------+
|count_if(((col % 2) = 0))|
+-------------------------+
|                        2|
+-------------------------+

SELECT count_if(col IS NULL) FROM VALUES (NULL), (0), (1), (2), (3) AS tab(col);
+-----------------------+
|count_if((col IS NULL))|
+-----------------------+
|                      1|
+-----------------------+

-- count_min_sketch
SELECT hex(count_min_sketch(col, 0.5d, 0.5d, 1)) FROM VALUES (1), (2), (1) AS tab(col);
+---------------------------------------+
|hex(count_min_sketch(col, 0.5, 0.5, 1))|
+---------------------------------------+
|                   00000001000000000...|
+---------------------------------------+

-- covar_pop
SELECT covar_pop(c1, c2) FROM VALUES (1,1), (2,2), (3,3) AS tab(c1, c2);
+------------------+
| covar_pop(c1, c2)|
+------------------+
|0.6666666666666666|
+------------------+

-- covar_samp
SELECT covar_samp(c1, c2) FROM VALUES (1,1), (2,2), (3,3) AS tab(c1, c2);
+------------------+
|covar_samp(c1, c2)|
+------------------+
|               1.0|
+------------------+

-- every
SELECT every(col) FROM VALUES (true), (true), (true) AS tab(col);
+----------+
|every(col)|
+----------+
|      true|
+----------+

SELECT every(col) FROM VALUES (NULL), (true), (true) AS tab(col);
+----------+
|every(col)|
+----------+
|      true|
+----------+

SELECT every(col) FROM VALUES (true), (false), (true) AS tab(col);
+----------+
|every(col)|
+----------+
|     false|
+----------+

-- first
SELECT first(col) FROM VALUES (10), (5), (20) AS tab(col);
+----------+
|first(col)|
+----------+
|        10|
+----------+

SELECT first(col) FROM VALUES (NULL), (5), (20) AS tab(col);
+----------+
|first(col)|
+----------+
|      null|
+----------+

SELECT first(col, true) FROM VALUES (NULL), (5), (20) AS tab(col);
+----------+
|first(col)|
+----------+
|         5|
+----------+

-- first_value
SELECT first_value(col) FROM VALUES (10), (5), (20) AS tab(col);
+----------------+
|first_value(col)|
+----------------+
|              10|
+----------------+

SELECT first_value(col) FROM VALUES (NULL), (5), (20) AS tab(col);
+----------------+
|first_value(col)|
+----------------+
|            null|
+----------------+

SELECT first_value(col, true) FROM VALUES (NULL), (5), (20) AS tab(col);
+----------------+
|first_value(col)|
+----------------+
|               5|
+----------------+

-- grouping
SELECT name, grouping(name), sum(age) FROM VALUES (2, 'Alice'), (5, 'Bob') people(age, name) GROUP BY cube(name);
+-----+--------------+--------+
| name|grouping(name)|sum(age)|
+-----+--------------+--------+
| null|             1|       7|
|Alice|             0|       2|
|  Bob|             0|       5|
+-----+--------------+--------+

-- grouping_id
SELECT name, grouping_id(), sum(age), avg(height) FROM VALUES (2, 'Alice', 165), (5, 'Bob', 180) people(age, name, height) GROUP BY cube(name, height);
+-----+-------------+--------+-----------+
| name|grouping_id()|sum(age)|avg(height)|
+-----+-------------+--------+-----------+
| null|            2|       2|      165.0|
|Alice|            0|       2|      165.0|
|Alice|            1|       2|      165.0|
| null|            3|       7|      172.5|
|  Bob|            1|       5|      180.0|
|  Bob|            0|       5|      180.0|
| null|            2|       5|      180.0|
+-----+-------------+--------+-----------+

-- histogram_numeric
SELECT histogram_numeric(col, 5) FROM VALUES (0), (1), (2), (10) AS tab(col);
+-------------------------+
|histogram_numeric(col, 5)|
+-------------------------+
|     [{0, 1.0}, {1, 1....|
+-------------------------+

-- kurtosis
SELECT kurtosis(col) FROM VALUES (-10), (-20), (100), (1000) AS tab(col);
+-------------------+
|      kurtosis(col)|
+-------------------+
|-0.7014368047529618|
+-------------------+

SELECT kurtosis(col) FROM VALUES (1), (10), (100), (10), (1) as tab(col);
+-------------------+
|      kurtosis(col)|
+-------------------+
|0.19432323191699075|
+-------------------+

-- last
SELECT last(col) FROM VALUES (10), (5), (20) AS tab(col);
+---------+
|last(col)|
+---------+
|       20|
+---------+

SELECT last(col) FROM VALUES (10), (5), (NULL) AS tab(col);
+---------+
|last(col)|
+---------+
|     null|
+---------+

SELECT last(col, true) FROM VALUES (10), (5), (NULL) AS tab(col);
+---------+
|last(col)|
+---------+
|        5|
+---------+

-- last_value
SELECT last_value(col) FROM VALUES (10), (5), (20) AS tab(col);
+---------------+
|last_value(col)|
+---------------+
|             20|
+---------------+

SELECT last_value(col) FROM VALUES (10), (5), (NULL) AS tab(col);
+---------------+
|last_value(col)|
+---------------+
|           null|
+---------------+

SELECT last_value(col, true) FROM VALUES (10), (5), (NULL) AS tab(col);
+---------------+
|last_value(col)|
+---------------+
|              5|
+---------------+

-- max
SELECT max(col) FROM VALUES (10), (50), (20) AS tab(col);
+--------+
|max(col)|
+--------+
|      50|
+--------+

-- max_by
SELECT max_by(x, y) FROM VALUES (('a', 10)), (('b', 50)), (('c', 20)) AS tab(x, y);
+------------+
|max_by(x, y)|
+------------+
|           b|
+------------+

-- mean
SELECT mean(col) FROM VALUES (1), (2), (3) AS tab(col);
+---------+
|mean(col)|
+---------+
|      2.0|
+---------+

SELECT mean(col) FROM VALUES (1), (2), (NULL) AS tab(col);
+---------+
|mean(col)|
+---------+
|      1.5|
+---------+

-- min
SELECT min(col) FROM VALUES (10), (-1), (20) AS tab(col);
+--------+
|min(col)|
+--------+
|      -1|
+--------+

-- min_by
SELECT min_by(x, y) FROM VALUES (('a', 10)), (('b', 50)), (('c', 20)) AS tab(x, y);
+------------+
|min_by(x, y)|
+------------+
|           a|
+------------+

-- percentile
SELECT percentile(col, 0.3) FROM VALUES (0), (10) AS tab(col);
+-----------------------+
|percentile(col, 0.3, 1)|
+-----------------------+
|                    3.0|
+-----------------------+

SELECT percentile(col, array(0.25, 0.75)) FROM VALUES (0), (10) AS tab(col);
+-------------------------------------+
|percentile(col, array(0.25, 0.75), 1)|
+-------------------------------------+
|                           [2.5, 7.5]|
+-------------------------------------+

-- percentile_approx
SELECT percentile_approx(col, array(0.5, 0.4, 0.1), 100) FROM VALUES (0), (1), (2), (10) AS tab(col);
+-------------------------------------------------+
|percentile_approx(col, array(0.5, 0.4, 0.1), 100)|
+-------------------------------------------------+
|                                        [1, 1, 0]|
+-------------------------------------------------+

SELECT percentile_approx(col, 0.5, 100) FROM VALUES (0), (6), (7), (9), (10) AS tab(col);
+--------------------------------+
|percentile_approx(col, 0.5, 100)|
+--------------------------------+
|                               7|
+--------------------------------+

SELECT percentile_approx(col, 0.5, 100) FROM VALUES (INTERVAL '0' MONTH), (INTERVAL '1' MONTH), (INTERVAL '2' MONTH), (INTERVAL '10' MONTH) AS tab(col);
+--------------------------------+
|percentile_approx(col, 0.5, 100)|
+--------------------------------+
|              INTERVAL '1' MONTH|
+--------------------------------+

SELECT percentile_approx(col, array(0.5, 0.7), 100) FROM VALUES (INTERVAL '0' SECOND), (INTERVAL '1' SECOND), (INTERVAL '2' SECOND), (INTERVAL '10' SECOND) AS tab(col);
+--------------------------------------------+
|percentile_approx(col, array(0.5, 0.7), 100)|
+--------------------------------------------+
|                        [INTERVAL '01' SE...|
+--------------------------------------------+

-- regr_avgx
SELECT regr_avgx(y, x) FROM VALUES (1, 2), (2, 2), (2, 3), (2, 4) AS tab(y, x);
+---------------+
|regr_avgx(y, x)|
+---------------+
|           2.75|
+---------------+

SELECT regr_avgx(y, x) FROM VALUES (1, null) AS tab(y, x);
+---------------+
|regr_avgx(y, x)|
+---------------+
|           null|
+---------------+

SELECT regr_avgx(y, x) FROM VALUES (null, 1) AS tab(y, x);
+---------------+
|regr_avgx(y, x)|
+---------------+
|           null|
+---------------+

SELECT regr_avgx(y, x) FROM VALUES (1, 2), (2, null), (2, 3), (2, 4) AS tab(y, x);
+---------------+
|regr_avgx(y, x)|
+---------------+
|            3.0|
+---------------+

SELECT regr_avgx(y, x) FROM VALUES (1, 2), (2, null), (null, 3), (2, 4) AS tab(y, x);
+---------------+
|regr_avgx(y, x)|
+---------------+
|            3.0|
+---------------+

-- regr_avgy
SELECT regr_avgy(y, x) FROM VALUES (1, 2), (2, 2), (2, 3), (2, 4) AS tab(y, x);
+---------------+
|regr_avgy(y, x)|
+---------------+
|           1.75|
+---------------+

SELECT regr_avgy(y, x) FROM VALUES (1, null) AS tab(y, x);
+---------------+
|regr_avgy(y, x)|
+---------------+
|           null|
+---------------+

SELECT regr_avgy(y, x) FROM VALUES (null, 1) AS tab(y, x);
+---------------+
|regr_avgy(y, x)|
+---------------+
|           null|
+---------------+

SELECT regr_avgy(y, x) FROM VALUES (1, 2), (2, null), (2, 3), (2, 4) AS tab(y, x);
+------------------+
|   regr_avgy(y, x)|
+------------------+
|1.6666666666666667|
+------------------+

SELECT regr_avgy(y, x) FROM VALUES (1, 2), (2, null), (null, 3), (2, 4) AS tab(y, x);
+---------------+
|regr_avgy(y, x)|
+---------------+
|            1.5|
+---------------+

-- regr_count
SELECT regr_count(y, x) FROM VALUES (1, 2), (2, 2), (2, 3), (2, 4) AS tab(y, x);
+----------------+
|regr_count(y, x)|
+----------------+
|               4|
+----------------+

SELECT regr_count(y, x) FROM VALUES (1, 2), (2, null), (2, 3), (2, 4) AS tab(y, x);
+----------------+
|regr_count(y, x)|
+----------------+
|               3|
+----------------+

SELECT regr_count(y, x) FROM VALUES (1, 2), (2, null), (null, 3), (2, 4) AS tab(y, x);
+----------------+
|regr_count(y, x)|
+----------------+
|               2|
+----------------+

-- regr_r2
SELECT regr_r2(y, x) FROM VALUES (1, 2), (2, 2), (2, 3), (2, 4) AS tab(y, x);
+------------------+
|     regr_r2(y, x)|
+------------------+
|0.2727272727272726|
+------------------+

SELECT regr_r2(y, x) FROM VALUES (1, null) AS tab(y, x);
+-------------+
|regr_r2(y, x)|
+-------------+
|         null|
+-------------+

SELECT regr_r2(y, x) FROM VALUES (null, 1) AS tab(y, x);
+-------------+
|regr_r2(y, x)|
+-------------+
|         null|
+-------------+

SELECT regr_r2(y, x) FROM VALUES (1, 2), (2, null), (2, 3), (2, 4) AS tab(y, x);
+------------------+
|     regr_r2(y, x)|
+------------------+
|0.7500000000000001|
+------------------+

SELECT regr_r2(y, x) FROM VALUES (1, 2), (2, null), (null, 3), (2, 4) AS tab(y, x);
+-------------+
|regr_r2(y, x)|
+-------------+
|          1.0|
+-------------+

-- skewness
SELECT skewness(col) FROM VALUES (-10), (-20), (100), (1000) AS tab(col);
+------------------+
|     skewness(col)|
+------------------+
|1.1135657469022013|
+------------------+

SELECT skewness(col) FROM VALUES (-1000), (-100), (10), (20) AS tab(col);
+-------------------+
|      skewness(col)|
+-------------------+
|-1.1135657469022011|
+-------------------+

-- some
SELECT some(col) FROM VALUES (true), (false), (false) AS tab(col);
+---------+
|some(col)|
+---------+
|     true|
+---------+

SELECT some(col) FROM VALUES (NULL), (true), (false) AS tab(col);
+---------+
|some(col)|
+---------+
|     true|
+---------+

SELECT some(col) FROM VALUES (false), (false), (NULL) AS tab(col);
+---------+
|some(col)|
+---------+
|    false|
+---------+

-- std
SELECT std(col) FROM VALUES (1), (2), (3) AS tab(col);
+--------+
|std(col)|
+--------+
|     1.0|
+--------+

-- stddev
SELECT stddev(col) FROM VALUES (1), (2), (3) AS tab(col);
+-----------+
|stddev(col)|
+-----------+
|        1.0|
+-----------+

-- stddev_pop
SELECT stddev_pop(col) FROM VALUES (1), (2), (3) AS tab(col);
+-----------------+
|  stddev_pop(col)|
+-----------------+
|0.816496580927726|
+-----------------+

-- stddev_samp
SELECT stddev_samp(col) FROM VALUES (1), (2), (3) AS tab(col);
+----------------+
|stddev_samp(col)|
+----------------+
|             1.0|
+----------------+

-- sum
SELECT sum(col) FROM VALUES (5), (10), (15) AS tab(col);
+--------+
|sum(col)|
+--------+
|      30|
+--------+

SELECT sum(col) FROM VALUES (NULL), (10), (15) AS tab(col);
+--------+
|sum(col)|
+--------+
|      25|
+--------+

SELECT sum(col) FROM VALUES (NULL), (NULL) AS tab(col);
+--------+
|sum(col)|
+--------+
|    null|
+--------+

-- try_avg
SELECT try_avg(col) FROM VALUES (1), (2), (3) AS tab(col);
+------------+
|try_avg(col)|
+------------+
|         2.0|
+------------+

SELECT try_avg(col) FROM VALUES (1), (2), (NULL) AS tab(col);
+------------+
|try_avg(col)|
+------------+
|         1.5|
+------------+

SELECT try_avg(col) FROM VALUES (interval '2147483647 months'), (interval '1 months') AS tab(col);
+------------+
|try_avg(col)|
+------------+
|        null|
+------------+

-- try_sum
SELECT try_sum(col) FROM VALUES (5), (10), (15) AS tab(col);
+------------+
|try_sum(col)|
+------------+
|          30|
+------------+

SELECT try_sum(col) FROM VALUES (NULL), (10), (15) AS tab(col);
+------------+
|try_sum(col)|
+------------+
|          25|
+------------+

SELECT try_sum(col) FROM VALUES (NULL), (NULL) AS tab(col);
+------------+
|try_sum(col)|
+------------+
|        null|
+------------+

SELECT try_sum(col) FROM VALUES (9223372036854775807L), (1L) AS tab(col);
+------------+
|try_sum(col)|
+------------+
|        null|
+------------+

-- var_pop
SELECT var_pop(col) FROM VALUES (1), (2), (3) AS tab(col);
+------------------+
|      var_pop(col)|
+------------------+
|0.6666666666666666|
+------------------+

-- var_samp
SELECT var_samp(col) FROM VALUES (1), (2), (3) AS tab(col);
+-------------+
|var_samp(col)|
+-------------+
|          1.0|
+-------------+

-- variance
SELECT variance(col) FROM VALUES (1), (2), (3) AS tab(col);
+-------------+
|variance(col)|
+-------------+
|          1.0|
+-------------+
```