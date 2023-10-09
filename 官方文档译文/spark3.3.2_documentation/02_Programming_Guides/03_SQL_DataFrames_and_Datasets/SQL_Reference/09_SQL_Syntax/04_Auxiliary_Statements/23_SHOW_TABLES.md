# SHOW TABLES

## Description

> The `SHOW TABLES` statement returns all the tables for an optionally specified database. Additionally, the output of this statement may be filtered by an optional matching pattern. If no database is specified then the tables are returned from the current database.

返回指定数据库下的所有表。

这个语句的输出可以通过可选的匹配模式过滤。

如果没有指定数据库，那么返回当前数据库下的表。

### Syntax

	SHOW TABLES [ { FROM | IN } database_name ] [ LIKE regex_pattern ]

### Parameters

- { FROM | IN } database_name

	Specifies the database name from which tables are listed.

- regex_pattern

	Specifies the regular expression pattern that is used to filter out unwanted tables.

	+ Except for `*` and `|` character, the pattern works like a regular expression.
	+ `*` alone matches 0 or more characters and `|` is used to separate multiple different regular expressions, any of which can match.
	+ The leading and trailing blanks are trimmed in the input pattern before processing. The pattern match is case-insensitive.

## Examples

```sql
-- List all tables in default database
SHOW TABLES;
+--------+---------+-----------+
|database|tableName|isTemporary|
+--------+---------+-----------+
| default|      sam|      false|
| default|     sam1|      false|
| default|      suj|      false|
+--------+---------+-----------+

-- List all tables from userdb database 
SHOW TABLES FROM userdb;
+--------+---------+-----------+
|database|tableName|isTemporary|
+--------+---------+-----------+
|  userdb|    user1|      false|
|  userdb|    user2|      false|
+--------+---------+-----------+

-- List all tables in userdb database
SHOW TABLES IN userdb;
+--------+---------+-----------+
|database|tableName|isTemporary|
+--------+---------+-----------+
|  userdb|    user1|      false|
|  userdb|    user2|      false|
+--------+---------+-----------+

-- List all tables from default database matching the pattern `sam*`
SHOW TABLES FROM default LIKE 'sam*';
+--------+---------+-----------+
|database|tableName|isTemporary|
+--------+---------+-----------+
| default|      sam|      false|
| default|     sam1|      false|
+--------+---------+-----------+
  
-- List all tables matching the pattern `sam*|suj`
SHOW TABLES LIKE 'sam*|suj';
+--------+---------+-----------+
|database|tableName|isTemporary|
+--------+---------+-----------+
| default|      sam|      false|
| default|     sam1|      false|
| default|      suj|      false|
+--------+---------+-----------+
```