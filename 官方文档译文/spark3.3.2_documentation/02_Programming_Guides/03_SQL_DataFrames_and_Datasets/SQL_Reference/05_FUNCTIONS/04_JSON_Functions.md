# JSON Functions

[TOC]

## JSON Functions

### from_json(jsonStr, schema[, options])

Returns a struct value with the given `jsonStr` and `schema`.

根据给定的 `jsonStr` 和 `schema`，返回结构体值。

### get_json_object(json_txt, path)

Extracts a json object from `path`.

从 `path` 中抽取 json 对象。

### json_array_length(jsonArray)

Returns the number of elements in the outermost JSON array.

返回最外层 JSON 数组中的元素数量

### json_object_keys(json_object)

Returns all the keys of the outermost JSON object as an array.

返回最外层 JSON 数组中的所有 keys，以数组形式返回

### json_tuple(jsonStr, p1, p2, ..., pn)

Returns a tuple like the function get_json_object, but it takes multiple names. All the input parameters and output column types are string.

返回一个元组，就像是 get_json_object 函数，但是它接收多个名字。

所有输入参数和输出列类型都是字符串。

### schema_of_json(json[, options])

Returns schema in the DDL format of JSON string.

返回 JSON 字符串的 DDL 格式的结构

### to_json(expr[, options])

Returns a JSON string with a given struct value

使用给定的结构体值，返回 JSON 字符串。

## Examples

```sql
-- from_json
SELECT from_json('{"a":1, "b":0.8}', 'a INT, b DOUBLE');
+---------------------------+
|from_json({"a":1, "b":0.8})|
+---------------------------+
|                   {1, 0.8}|
+---------------------------+

SELECT from_json('{"time":"26/08/2015"}', 'time Timestamp', map('timestampFormat', 'dd/MM/yyyy'));
+--------------------------------+
|from_json({"time":"26/08/2015"})|
+--------------------------------+
|            {2015-08-26 00:00...|
+--------------------------------+

SELECT from_json('{"teacher": "Alice", "student": [{"name": "Bob", "rank": 1}, {"name": "Charlie", "rank": 2}]}', 'STRUCT<teacher: STRING, student: ARRAY<STRUCT<name: STRING, rank: INT>>>');
+--------------------------------------------------------------------------------------------------------+
|from_json({"teacher": "Alice", "student": [{"name": "Bob", "rank": 1}, {"name": "Charlie", "rank": 2}]})|
+--------------------------------------------------------------------------------------------------------+
|                                                                                    {Alice, [{Bob, 1}...|
+--------------------------------------------------------------------------------------------------------+

-- get_json_object
SELECT get_json_object('{"a":"b"}', '$.a');
+-------------------------------+
|get_json_object({"a":"b"}, $.a)|
+-------------------------------+
|                              b|
+-------------------------------+

-- json_array_length
SELECT json_array_length('[1,2,3,4]');
+----------------------------+
|json_array_length([1,2,3,4])|
+----------------------------+
|                           4|
+----------------------------+

SELECT json_array_length('[1,2,3,{"f1":1,"f2":[5,6]},4]');
+------------------------------------------------+
|json_array_length([1,2,3,{"f1":1,"f2":[5,6]},4])|
+------------------------------------------------+
|                                               5|
+------------------------------------------------+

SELECT json_array_length('[1,2');
+-----------------------+
|json_array_length([1,2)|
+-----------------------+
|                   null|
+-----------------------+

-- json_object_keys
SELECT json_object_keys('{}');
+--------------------+
|json_object_keys({})|
+--------------------+
|                  []|
+--------------------+

SELECT json_object_keys('{"key": "value"}');
+----------------------------------+
|json_object_keys({"key": "value"})|
+----------------------------------+
|                             [key]|
+----------------------------------+

SELECT json_object_keys('{"f1":"abc","f2":{"f3":"a", "f4":"b"}}');
+--------------------------------------------------------+
|json_object_keys({"f1":"abc","f2":{"f3":"a", "f4":"b"}})|
+--------------------------------------------------------+
|                                                [f1, f2]|
+--------------------------------------------------------+

-- json_tuple
SELECT json_tuple('{"a":1, "b":2}', 'a', 'b');
+---+---+
| c0| c1|
+---+---+
|  1|  2|
+---+---+

-- schema_of_json
SELECT schema_of_json('[{"col":0}]');
+---------------------------+
|schema_of_json([{"col":0}])|
+---------------------------+
|       ARRAY<STRUCT<col:...|
+---------------------------+

SELECT schema_of_json('[{"col":01}]', map('allowNumericLeadingZeros', 'true'));
+----------------------------+
|schema_of_json([{"col":01}])|
+----------------------------+
|        ARRAY<STRUCT<col:...|
+----------------------------+

-- to_json
SELECT to_json(named_struct('a', 1, 'b', 2));
+---------------------------------+
|to_json(named_struct(a, 1, b, 2))|
+---------------------------------+
|                    {"a":1,"b":2}|
+---------------------------------+

SELECT to_json(named_struct('time', to_timestamp('2015-08-26', 'yyyy-MM-dd')), map('timestampFormat', 'dd/MM/yyyy'));
+-----------------------------------------------------------------+
|to_json(named_struct(time, to_timestamp(2015-08-26, yyyy-MM-dd)))|
+-----------------------------------------------------------------+
|                                             {"time":"26/08/20...|
+-----------------------------------------------------------------+

SELECT to_json(array(named_struct('a', 1, 'b', 2)));
+----------------------------------------+
|to_json(array(named_struct(a, 1, b, 2)))|
+----------------------------------------+
|                         [{"a":1,"b":2}]|
+----------------------------------------+

SELECT to_json(map('a', named_struct('b', 1)));
+-----------------------------------+
|to_json(map(a, named_struct(b, 1)))|
+-----------------------------------+
|                      {"a":{"b":1}}|
+-----------------------------------+

SELECT to_json(map(named_struct('a', 1),named_struct('b', 2)));
+----------------------------------------------------+
|to_json(map(named_struct(a, 1), named_struct(b, 2)))|
+----------------------------------------------------+
|                                     {"[1]":{"b":2}}|
+----------------------------------------------------+

SELECT to_json(map('a', 1));
+------------------+
|to_json(map(a, 1))|
+------------------+
|           {"a":1}|
+------------------+

SELECT to_json(array((map('a', 1))));
+-------------------------+
|to_json(array(map(a, 1)))|
+-------------------------+
|                [{"a":1}]|
+-------------------------+
```