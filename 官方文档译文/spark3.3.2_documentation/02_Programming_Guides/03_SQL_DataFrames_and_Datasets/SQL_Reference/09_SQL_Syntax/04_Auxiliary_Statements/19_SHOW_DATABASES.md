# SHOW DATABASES

[TOC]

## Description

> Lists the databases that match an optionally supplied regular expression pattern. If no pattern is supplied then the command lists all the databases in the system. Please note that the usage of `SCHEMAS` and `DATABASES` are interchangeable and mean the same thing.

列出匹配到的数据库，根据根据正则表达式模板匹配。

如果没有提供正则表达式模板，那么命令就会列出系统中的所有数据库。

`SCHEMAS` 和 `DATABASES` 是可互换的，表示相同的含义。

### Syntax

	SHOW { DATABASES | SCHEMAS } [ LIKE regex_pattern ]

### Parameters

- regex_pattern

	Specifies a regular expression pattern that is used to filter the results of the statement.

	- Except for `*` and `|` character, the pattern works like a regular expression.
	
	- `*` alone matches 0 or more characters and `|` is used to separate multiple different regular expressions, any of which can match.
	
	- The leading and trailing blanks are trimmed in the input pattern before processing. The pattern match is case-insensitive.

## Examples

```SQL
-- Create database. Assumes a database named `default` already exists in
-- the system. 
CREATE DATABASE payroll_db;
CREATE DATABASE payments_db;

-- Lists all the databases. 
SHOW DATABASES;
+------------+
|databaseName|
+------------+
|     default|
| payments_db|
|  payroll_db|
+------------+
  
-- Lists databases with name starting with string pattern `pay`
SHOW DATABASES LIKE 'pay*';
+------------+
|databaseName|
+------------+
| payments_db|
|  payroll_db|
+------------+
  
-- Lists all databases. Keywords SCHEMAS and DATABASES are interchangeable. 
SHOW SCHEMAS;
+------------+
|databaseName|
+------------+
|     default|
| payments_db|
|  payroll_db|
+------------+
```