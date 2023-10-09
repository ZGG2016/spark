# Map Functions

## Map Functions

### element_at(array, index)

Returns element of array at given (1-based) index. If Index is 0, Spark will throw an error. If index < 0, accesses elements from the last to the first. The function returns NULL if the index exceeds the length of the array and `spark.sql.ansi.enabled` is set to false. If `spark.sql.ansi.enabled` is set to true, it throws ArrayIndexOutOfBoundsException for invalid indices.

返回在给定索引位置上的数组元素（索引从1开始）。

如果索引是0，spark将抛出一个错误。如果索引小于0，那么将从末尾到开头访问元素。

如果索引超过了数组长度，且属性 `spark.sql.ansi.enabled` 设为 false，函数返回 NULL.

如果属性 `spark.sql.ansi.enabled` 设为 true，且索引无效，那么就会抛出 ArrayIndexOutOfBoundsException 

### element_at(map, key)

Returns value for given key. The function returns NULL if the key is not contained in the map and `spark.sql.ansi.enabled` is set to false. If `spark.sql.ansi.enabled` is set to true, it throws NoSuchElementException instead.

返回给定 key 对于的值。

如果 map 不包含 key，且属性 `spark.sql.ansi.enabled` 设为 false，函数返回 NULL.

如果属性 `spark.sql.ansi.enabled` 设为 true，那么就会抛出 NoSuchElementException

### map(key0, value0, key1, value1, ...)

Creates a map with the given key/value pairs.

使用给定的键值对创建一个 map

### map_concat(map, ...)

Returns the union of all the given maps

返回所有给定的 map 的合集

### map_contains_key(map, key)

Returns true if the map contains the key.

如果 map 包含 key，返回true

### map_entries(map)

Returns an unordered array of all entries in the given map.

返回给定 map 中的所有项组成的无序数组

### map_from_arrays(keys, values)

Creates a map with a pair of the given key/value arrays. All elements in keys should not be null

使用给定的一对键值对数组创建一个 map

key 中的所有元素不应该是 null

### map_from_entries(arrayOfEntries)

Returns a map created from the given array of entries.

从给定项的数组中创建一个 map

### map_keys(map)

Returns an unordered array containing the keys of the map.

返回一个无序数组，包含了 map 的 keys

### map_values(map)

Returns an unordered array containing the values of the map.

返回一个无序数组，包含了 map 的 values

### str_to_map(text[, pairDelim[, keyValueDelim]])

Creates a map after splitting the text into key/value pairs using delimiters. Default delimiters are ',' for `pairDelim` and ':' for `keyValueDelim`. Both `pairDelim` and `keyValueDelim` are treated as regular expressions.

使用分隔符，将字符串划分成键值对后，创建一个 map.

对于 `pairDelim`, 默认的分隔符是 `,`；对于 `keyValueDelim`，则是 `:`

`pairDelim` 和 `keyValueDelim` 都被当作常规的表达式

### try_element_at(array, index)

Returns element of array at given (1-based) index. If Index is 0, Spark will throw an error. If index < 0, accesses elements from the last to the first. The function always returns NULL if the index exceeds the length of the array.

返回在给定索引位置上的数组元素（索引从1开始）。

如果索引是0，spark将抛出一个错误。如果索引小于0，那么将从末尾到开头访问元素。

如果索引超过了数组长度，函数总是返回 NULL.

### try_element_at(map, key)

Returns value for given key. The function always returns NULL if the key is not contained in the map.

返回给定 key 对于的值。

如果 map 不包含 key，函数返回 NULL.

## Examples

```sql
-- element_at
SELECT element_at(array(1, 2, 3), 2);
+-----------------------------+
|element_at(array(1, 2, 3), 2)|
+-----------------------------+
|                            2|
+-----------------------------+

SELECT element_at(map(1, 'a', 2, 'b'), 2);
+------------------------------+
|element_at(map(1, a, 2, b), 2)|
+------------------------------+
|                             b|
+------------------------------+

-- map
SELECT map(1.0, '2', 3.0, '4');
+--------------------+
| map(1.0, 2, 3.0, 4)|
+--------------------+
|{1.0 -> 2, 3.0 -> 4}|
+--------------------+

-- map_concat
SELECT map_concat(map(1, 'a', 2, 'b'), map(3, 'c'));
+--------------------------------------+
|map_concat(map(1, a, 2, b), map(3, c))|
+--------------------------------------+
|                  {1 -> a, 2 -> b, ...|
+--------------------------------------+

-- map_contains_key
SELECT map_contains_key(map(1, 'a', 2, 'b'), 1);
+------------------------------------+
|map_contains_key(map(1, a, 2, b), 1)|
+------------------------------------+
|                                true|
+------------------------------------+

SELECT map_contains_key(map(1, 'a', 2, 'b'), 3);
+------------------------------------+
|map_contains_key(map(1, a, 2, b), 3)|
+------------------------------------+
|                               false|
+------------------------------------+

-- map_entries
SELECT map_entries(map(1, 'a', 2, 'b'));
+----------------------------+
|map_entries(map(1, a, 2, b))|
+----------------------------+
|            [{1, a}, {2, b}]|
+----------------------------+

-- map_from_arrays
SELECT map_from_arrays(array(1.0, 3.0), array('2', '4'));
+---------------------------------------------+
|map_from_arrays(array(1.0, 3.0), array(2, 4))|
+---------------------------------------------+
|                         {1.0 -> 2, 3.0 -> 4}|
+---------------------------------------------+

-- map_from_entries
SELECT map_from_entries(array(struct(1, 'a'), struct(2, 'b')));
+---------------------------------------------------+
|map_from_entries(array(struct(1, a), struct(2, b)))|
+---------------------------------------------------+
|                                   {1 -> a, 2 -> b}|
+---------------------------------------------------+

-- map_keys
SELECT map_keys(map(1, 'a', 2, 'b'));
+-------------------------+
|map_keys(map(1, a, 2, b))|
+-------------------------+
|                   [1, 2]|
+-------------------------+

-- map_values
SELECT map_values(map(1, 'a', 2, 'b'));
+---------------------------+
|map_values(map(1, a, 2, b))|
+---------------------------+
|                     [a, b]|
+---------------------------+

-- str_to_map
SELECT str_to_map('a:1,b:2,c:3', ',', ':');
+-----------------------------+
|str_to_map(a:1,b:2,c:3, ,, :)|
+-----------------------------+
|         {a -> 1, b -> 2, ...|
+-----------------------------+

SELECT str_to_map('a');
+-------------------+
|str_to_map(a, ,, :)|
+-------------------+
|        {a -> null}|
+-------------------+

-- try_element_at
SELECT try_element_at(array(1, 2, 3), 2);
+---------------------------------+
|try_element_at(array(1, 2, 3), 2)|
+---------------------------------+
|                                2|
+---------------------------------+

SELECT try_element_at(map(1, 'a', 2, 'b'), 2);
+----------------------------------+
|try_element_at(map(1, a, 2, b), 2)|
+----------------------------------+
|                                 b|
+----------------------------------+
```