# SHOW FUNCTIONS

[TOC]

## Description

> Returns the list of functions after applying an optional regex pattern. Given number of functions supported by Spark is quite large, this statement in conjunction with [describe function](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-aux-describe-function.html) may be used to quickly find the function and understand its usage. The `LIKE` clause is optional and supported only for compatibility with other systems.

使用可选的正则模板，匹配函数名称，返回函数列名。

### Syntax

	SHOW [ function_kind ] FUNCTIONS [ { FROM | IN } database_name ] [ LIKE regex_pattern ]

### Parameters

- function_kind

	Specifies the name space of the function to be searched upon. The valid name spaces are :

	- USER - Looks up the function(s) among the user defined functions.
	- SYSTEM - Looks up the function(s) among the system defined functions.
	- ALL - Looks up the function(s) among both user and system defined functions.

- `{ FROM | IN } database_name`

	Specifies the database name from which functions are listed.

- regex_pattern

	Specifies a regular expression pattern that is used to filter the results of the statement.

	- Except for `*` and `|` character, the pattern works like a regular expression.
	- `*` alone matches 0 or more characters and `|` is used to separate multiple different regular expressions, any of which can match.
	- The leading and trailing blanks are trimmed in the input pattern before processing. The pattern match is case-insensitive.

## Examples

```SQL
-- List a system function `trim` by searching both user defined and system
-- defined functions.
SHOW FUNCTIONS trim;
+--------+
|function|
+--------+
|    trim|
+--------+

-- List a system function `concat` by searching system defined functions.
SHOW SYSTEM FUNCTIONS concat;
+--------+
|function|
+--------+
|  concat|
+--------+

-- List a qualified function `max` from database `salesdb`. 
SHOW SYSTEM FUNCTIONS FROM salesdb LIKE 'max';
+--------+
|function|
+--------+
|     max|
+--------+

-- List all functions starting with `t`
SHOW FUNCTIONS LIKE 't*';
+-----------------+
|         function|
+-----------------+
|              tan|
|             tanh|
|        timestamp|
|          tinyint|
|           to_csv|
|          to_date|
|          to_json|
|     to_timestamp|
|to_unix_timestamp|
| to_utc_timestamp|
|        transform|
|   transform_keys|
| transform_values|
|        translate|
|             trim|
|            trunc|
|           typeof|
+-----------------+

-- List all functions starting with `yea` or `windo`
SHOW FUNCTIONS LIKE 'yea*|windo*';
+--------+
|function|
+--------+
|  window|
|    year|
+--------+

-- Use normal regex pattern to list function names that has 4 characters
-- with `t` as the starting character.
SHOW FUNCTIONS LIKE 't[a-z][a-z][a-z]';
+--------+
|function|
+--------+
|    tanh|
|    trim|
+--------+
```