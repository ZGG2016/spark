# Array Functions

[TOC]

## Array Functions

### array(expr, ...)

Returns an array with the given elements.

基于给定元素，返回一个数组。

### array_contains(array, value)

Returns true if the array contains the value.

如果数组包含这个值，返回true

### array_distinct(array)

Removes duplicate values from the array.

移除数组中的重复元素

### array_except(array1, array2)

Returns an array of the elements in array1 but not in array2, without duplicates.

返回在数组 array1 中，但不在 array2 中的元素组成的数组，不含冗余元素。

### array_intersect(array1, array2)

Returns an array of the elements in the intersection of array1 and array2, without duplicates.

返回数组 array1 和 array2 的交集元组组成的数组，不含冗余元素。

### array_join(array, delimiter[, nullReplacement])

Concatenates the elements of the given array using the delimiter and an optional string to replace nulls. If no value is set for nullReplacement, any null value is filtered.

使用分隔符连接给定数组的元素，并使用可选的字符串代替 null。

如果没有设置nullReplacement，将会过滤掉 null 值。

### array_max(array)

Returns the maximum value in the array. NaN is greater than any non-NaN elements for double/float type. NULL elements are skipped.

返回数组中的最大值。对于 double/float 类型，NaN 大于任意的非 NaN 元素。

将会跳过 NULL 元素。

### array_min(array)

Returns the minimum value in the array. NaN is greater than any non-NaN elements for double/float type. NULL elements are skipped.

返回数组中的最小值。对于 double/float 类型，NaN 大于任意的非 NaN 元素。

将会跳过 NULL 元素。

### array_position(array, element)

Returns the (1-based) index of the first element of the array as long.

返回给定元素在数组中第一次出现的位置。

### array_remove(array, element)

Remove all elements that equal to element from array.

移除数组中所有等于给定值的元素。

### array_repeat(element, count)

Returns the array containing element count times.

返回包含元素个数的数组。

### array_union(array1, array2)

Returns an array of the elements in the union of array1 and array2, without duplicates.

返回一个数组，其元素是数组 array1 和 array2 并集，不含冗余。

### arrays_overlap(a1, a2)

Returns true if a1 contains at least a non-null element present also in a2. If the arrays have no common element and they are both non-empty and either of them contains a null element null is returned, false otherwise.

如果 a1 包含至少一个非 null 元素，且存在于 a2 中，就返回 true.

如果数组没有共同的元素，且都是非空的，且其中一个数组包含一个 null 元素，就返回 null，否则返回 false.

### arrays_zip(a1, a2, ...)

Returns a merged array of structs in which the N-th struct contains all N-th values of input arrays.

返回一个由结构体组成的合并数组，在数组中，第n个结构体包含输入数组所有的第n个元素值

### flatten(arrayOfArrays)

Transforms an array of arrays into a single array.

将由数组组成的数组转换成单一数组。

### sequence(start, stop, step)

Generates an array of elements from start to stop (inclusive), incrementing by step. The type of the returned elements is the same as the type of argument expressions. Supported types are: byte, short, integer, long, date, timestamp. The start and stop expressions must resolve to the same type. If start and stop expressions resolve to the 'date' or 'timestamp' type then the step expression must resolve to the 'interval' or 'year-month interval' or 'day-time interval' type, otherwise to the same type as the start and stop expressions.

生成一个从 start 到 stop （不包含）元素的数组，元素按照 step 增加。返回的元素类型和参数表达式的类型相同。

支持的类型有: byte, short, integer, long, date, timestamp. 

start 和 stop 表达式必须是相同类型。如果 start 和 stop 表达式是 date 或 timestamp 类型，那么 step 表达式必须是 interval 或 year-month interval 或 day-time interval 类型，否则就要和 start 和 stop 表达式类型相同。

### shuffle(array)

Returns a random permutation of the given array.

返回给定数组的随机排列。

### slice(x, start, length)

Subsets array x starting from index start (array indices start at 1, or starting from the end if start is negative) with the specified length.

返回给定数组的子集，从索引 start （数组索引从1开始，如果start是负数，那么就是从数组尾开始）开始，长度为给定的 length

### sort_array(array[, ascendingOrder])

Sorts the input array in ascending or descending order according to the natural ordering of the array elements. NaN is greater than any non-NaN elements for double/float type. Null elements will be placed at the beginning of the returned array in ascending order or at the end of the returned array in descending order.

按照数组元素的自然顺序，以升序或降序排序输入数组。

对于 double/float 类型，NaN 大于任意的非 NaN 元素。

当按照升序排序时，Null 元素将被放在返回数组的开头。当按照降序排序时，Null 元素将被放在返回数组的末尾。


## Examples

```sql
-- array
SELECT array(1, 2, 3);
+--------------+
|array(1, 2, 3)|
+--------------+
|     [1, 2, 3]|
+--------------+

-- array_contains
SELECT array_contains(array(1, 2, 3), 2);
+---------------------------------+
|array_contains(array(1, 2, 3), 2)|
+---------------------------------+
|                             true|
+---------------------------------+

-- array_distinct
SELECT array_distinct(array(1, 2, 3, null, 3));
+---------------------------------------+
|array_distinct(array(1, 2, 3, NULL, 3))|
+---------------------------------------+
|                        [1, 2, 3, null]|
+---------------------------------------+

-- array_except
SELECT array_except(array(1, 2, 3), array(1, 3, 5));
+--------------------------------------------+
|array_except(array(1, 2, 3), array(1, 3, 5))|
+--------------------------------------------+
|                                         [2]|
+--------------------------------------------+

-- array_intersect
SELECT array_intersect(array(1, 2, 3), array(1, 3, 5));
+-----------------------------------------------+
|array_intersect(array(1, 2, 3), array(1, 3, 5))|
+-----------------------------------------------+
|                                         [1, 3]|
+-----------------------------------------------+

-- array_join
SELECT array_join(array('hello', 'world'), ' ');
+----------------------------------+
|array_join(array(hello, world),  )|
+----------------------------------+
|                       hello world|
+----------------------------------+

SELECT array_join(array('hello', null ,'world'), ' ');
+----------------------------------------+
|array_join(array(hello, NULL, world),  )|
+----------------------------------------+
|                             hello world|
+----------------------------------------+

SELECT array_join(array('hello', null ,'world'), ' ', ',');
+-------------------------------------------+
|array_join(array(hello, NULL, world),  , ,)|
+-------------------------------------------+
|                              hello , world|
+-------------------------------------------+

-- array_max
SELECT array_max(array(1, 20, null, 3));
+--------------------------------+
|array_max(array(1, 20, NULL, 3))|
+--------------------------------+
|                              20|
+--------------------------------+

-- array_min
SELECT array_min(array(1, 20, null, 3));
+--------------------------------+
|array_min(array(1, 20, NULL, 3))|
+--------------------------------+
|                               1|
+--------------------------------+

-- array_position
SELECT array_position(array(3, 2, 1), 1);
+---------------------------------+
|array_position(array(3, 2, 1), 1)|
+---------------------------------+
|                                3|
+---------------------------------+

-- array_remove
SELECT array_remove(array(1, 2, 3, null, 3), 3);
+----------------------------------------+
|array_remove(array(1, 2, 3, NULL, 3), 3)|
+----------------------------------------+
|                            [1, 2, null]|
+----------------------------------------+

-- array_repeat
SELECT array_repeat('123', 2);
+--------------------+
|array_repeat(123, 2)|
+--------------------+
|          [123, 123]|
+--------------------+

-- array_union
SELECT array_union(array(1, 2, 3), array(1, 3, 5));
+-------------------------------------------+
|array_union(array(1, 2, 3), array(1, 3, 5))|
+-------------------------------------------+
|                               [1, 2, 3, 5]|
+-------------------------------------------+

-- arrays_overlap
SELECT arrays_overlap(array(1, 2, 3), array(3, 4, 5));
+----------------------------------------------+
|arrays_overlap(array(1, 2, 3), array(3, 4, 5))|
+----------------------------------------------+
|                                          true|
+----------------------------------------------+

-- arrays_zip
SELECT arrays_zip(array(1, 2, 3), array(2, 3, 4));
+------------------------------------------+
|arrays_zip(array(1, 2, 3), array(2, 3, 4))|
+------------------------------------------+
|                      [{1, 2}, {2, 3}, ...|
+------------------------------------------+

SELECT arrays_zip(array(1, 2), array(2, 3), array(3, 4));
+-------------------------------------------------+
|arrays_zip(array(1, 2), array(2, 3), array(3, 4))|
+-------------------------------------------------+
|                             [{1, 2, 3}, {2, 3...|
+-------------------------------------------------+

-- flatten
SELECT flatten(array(array(1, 2), array(3, 4)));
+----------------------------------------+
|flatten(array(array(1, 2), array(3, 4)))|
+----------------------------------------+
|                            [1, 2, 3, 4]|
+----------------------------------------+

-- sequence
SELECT sequence(1, 5);
+---------------+
| sequence(1, 5)|
+---------------+
|[1, 2, 3, 4, 5]|
+---------------+

SELECT sequence(5, 1);
+---------------+
| sequence(5, 1)|
+---------------+
|[5, 4, 3, 2, 1]|
+---------------+

SELECT sequence(to_date('2018-01-01'), to_date('2018-03-01'), interval 1 month);
+----------------------------------------------------------------------+
|sequence(to_date(2018-01-01), to_date(2018-03-01), INTERVAL '1' MONTH)|
+----------------------------------------------------------------------+
|                                                  [2018-01-01, 2018...|
+----------------------------------------------------------------------+

SELECT sequence(to_date('2018-01-01'), to_date('2018-03-01'), interval '0-1' year to month);
+--------------------------------------------------------------------------------+
|sequence(to_date(2018-01-01), to_date(2018-03-01), INTERVAL '0-1' YEAR TO MONTH)|
+--------------------------------------------------------------------------------+
|                                                            [2018-01-01, 2018...|
+--------------------------------------------------------------------------------+

-- shuffle
SELECT shuffle(array(1, 20, 3, 5));
+---------------------------+
|shuffle(array(1, 20, 3, 5))|
+---------------------------+
|              [5, 20, 1, 3]|
+---------------------------+

SELECT shuffle(array(1, 20, null, 3));
+------------------------------+
|shuffle(array(1, 20, NULL, 3))|
+------------------------------+
|              [1, 20, 3, null]|
+------------------------------+

-- slice
SELECT slice(array(1, 2, 3, 4), 2, 2);
+------------------------------+
|slice(array(1, 2, 3, 4), 2, 2)|
+------------------------------+
|                        [2, 3]|
+------------------------------+

SELECT slice(array(1, 2, 3, 4), -2, 2);
+-------------------------------+
|slice(array(1, 2, 3, 4), -2, 2)|
+-------------------------------+
|                         [3, 4]|
+-------------------------------+

-- sort_array
SELECT sort_array(array('b', 'd', null, 'c', 'a'), true);
+-----------------------------------------+
|sort_array(array(b, d, NULL, c, a), true)|
+-----------------------------------------+
|                       [null, a, b, c, d]|
+-----------------------------------------+
```