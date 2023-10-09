# Identifiers

[TOC]

## Description

> An identifier is a string used to identify a database object such as a table, view, schema, column, etc. Spark SQL has regular identifiers and delimited identifiers, which are enclosed within backticks. Both regular identifiers and delimited identifiers are case-insensitive.

标识符是用于标识一个数据库对象的字符串，例如表、视图、结构、列等。

Spark SQL 具有常规的标识符和使用反引号包裹的标识符，这两类都是大小写不敏感的。

## Syntax
### Regular Identifier

	{ letter | digit | '_' } [ , ... ]

> Note: If `spark.sql.ansi.enabled` is set to true, ANSI SQL reserved keywords cannot be used as identifiers. For more details, please refer to [ANSI Compliance](https://spark.apache.org/docs/3.3.2/sql-ref-ansi-compliance.html).

注意：如果 `spark.sql.ansi.enabled` 设为 true, ANSI SQL 保留关键字不能用作标识符。

### Delimited Identifier

	`c [ ... ]`

### Parameters

```
- letter

	Any letter from A-Z or a-z.

- digit

	Any numeral from 0 to 9.

- c

	Any character from the character set. Use ` to escape special characters (e.g., `).
```

## Examples

```sql
-- This CREATE TABLE fails with ParseException because of the illegal identifier name a.b
CREATE TABLE test (a.b int);
org.apache.spark.sql.catalyst.parser.ParseException:
Syntax error at or near '.': extra input '.'(line 1, pos 20)

-- This CREATE TABLE works
CREATE TABLE test (`a.b` int);

-- This CREATE TABLE fails with ParseException because special character ` is not escaped
CREATE TABLE test1 (`a`b` int);
org.apache.spark.sql.catalyst.parser.ParseException:
Syntax error at or near '`'(line 1, pos 23)

-- This CREATE TABLE works
CREATE TABLE test (`a``b` int);
```