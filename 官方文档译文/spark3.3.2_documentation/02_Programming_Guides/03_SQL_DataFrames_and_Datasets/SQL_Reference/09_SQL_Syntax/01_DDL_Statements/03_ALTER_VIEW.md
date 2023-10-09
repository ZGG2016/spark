# ALTER VIEW

[TOC]

## Description

> The `ALTER VIEW` statement can alter metadata associated with the view. It can change the definition of the view, change the name of a view to a different name, set and unset the metadata of the view by setting `TBLPROPERTIES`.

`ALTER VIEW` 语句可以改变视图的元数据。

它可以改变视图的定义，改变视图的名称，设置和取消设置由 `TBLPROPERTIES` 设置的视图元数据。

## RENAME View

> Renames the existing view. If the new view name already exists in the source database, a TableAlreadyExistsException is thrown. This operation does not support moving the views across databases.

重命名已存在的视图。

如果新视图名在源数据库中已存在，就会抛出 TableAlreadyExistsException 异常。

这个操作不支持跨数据库移动视图。

> If the view is cached, the command clears cached data of the view and all its dependents that refer to it. View’s cache will be lazily filled when the next time the view is accessed. The command leaves view’s dependents as uncached.

如果视图已被缓存，命令会清除视图的缓存数据和所有依赖

在下次访问视图时，缓存将被懒填满。命令不会缓存视图的依赖。

### Syntax

	ALTER VIEW view_identifier RENAME TO view_identifier

### Parameters

- view_identifier

	Specifies a view name, which may be optionally qualified with a database name.
	
	Syntax: [ database_name. ] view_name

## SET View Properties

> Set one or more properties of an existing view. The properties are the key value pairs. If the properties’ keys exist, the values are replaced with the new values. If the properties’ keys do not exist, the key value pairs are added into the properties.

设置已存在视图的一个或多个属性。

属性是键值对形式。如果属性的键存在，值将被新值取代。如果属性的键不存在，键值对将被添加到属性中。

### Syntax

	ALTER VIEW view_identifier SET TBLPROPERTIES ( property_key = property_val [ , ... ] )

### Parameters

- view_identifier

	Specifies a view name, which may be optionally qualified with a database name.
	
	Syntax: `[ database_name. ] view_name`

- property_key

	Specifies the property key. The key may consists of multiple parts separated by dot.
	
	Syntax: `[ key_part1 ] [ .key_part2 ] [ ... ]`

## UNSET View Properties

> Drop one or more properties of an existing view. If the specified keys do not exist, an exception is thrown. Use `IF EXISTS` to avoid the exception.

删除已存在视图的一个或多个属性。

如果指定的键不存在，就会抛出异常。可以使用 `IF EXIST` 避免异常。

### Syntax

	ALTER VIEW view_identifier UNSET TBLPROPERTIES [ IF EXISTS ]  ( property_key [ , ... ] )

### Parameters

- view_identifier

	Specifies a view name, which may be optionally qualified with a database name.
	
	Syntax: `[ database_name. ] view_name`

- property_key

	Specifies the property key. The key may consists of multiple parts separated by dot.
	
	Syntax: `[ key_part1 ] [ .key_part2 ] [ ... ]`

## ALTER View AS SELECT

> `ALTER VIEW view_identifier AS SELECT` statement changes the definition of a view. The `SELECT` statement must be valid, and the `view_identifier` must exist.

`ALTER VIEW view_identifier AS SELECT` 语句改变视图的定义。

`SELECT` 语句必须是有效的，且 `view_identifier` 必须存在。

### Syntax

	ALTER VIEW view_identifier AS select_statement

> Note that ALTER VIEW statement does not support SET SERDE or SET SERDEPROPERTIES properties.

### Parameters

- view_identifier

	Specifies a view name, which may be optionally qualified with a database name.
	
	Syntax: `[ database_name. ] view_name`

- select_statement

	Specifies the definition of the view. Check select_statement for details.

## Examples

```sql
-- Rename only changes the view name.
-- The source and target databases of the view have to be the same.
-- Use qualified or unqualified name for the source and target view.
ALTER VIEW tempdb1.v1 RENAME TO tempdb1.v2;

-- Verify that the new view is created.
DESCRIBE TABLE EXTENDED tempdb1.v2;
+----------------------------+----------+-------+
|                    col_name|data_type |comment|
+----------------------------+----------+-------+
|                          c1|       int|   null|
|                          c2|    string|   null|
|                            |          |       |
|# Detailed Table Information|          |       |
|                    Database|   tempdb1|       |
|                       Table|        v2|       |
+----------------------------+----------+-------+

-- Before ALTER VIEW SET TBLPROPERTIES
DESC TABLE EXTENDED tempdb1.v2;
+----------------------------+----------+-------+
|                    col_name| data_type|comment|
+----------------------------+----------+-------+
|                          c1|       int|   null|
|                          c2|    string|   null|
|                            |          |       |
|# Detailed Table Information|          |       |
|                    Database|   tempdb1|       |
|                       Table|        v2|       |
|            Table Properties|    [....]|       |
+----------------------------+----------+-------+

-- Set properties in TBLPROPERTIES
ALTER VIEW tempdb1.v2 SET TBLPROPERTIES ('created.by.user' = "John", 'created.date' = '01-01-2001' );

-- Use `DESCRIBE TABLE EXTENDED tempdb1.v2` to verify
DESC TABLE EXTENDED tempdb1.v2;
+----------------------------+-----------------------------------------------------+-------+
|                    col_name|                                            data_type|comment|
+----------------------------+-----------------------------------------------------+-------+
|                          c1|                                                  int|   null|
|                          c2|                                               string|   null|
|                            |                                                     |       |
|# Detailed Table Information|                                                     |       |
|                    Database|                                              tempdb1|       |
|                       Table|                                                   v2|       |
|            Table Properties|[created.by.user=John, created.date=01-01-2001, ....]|       |
+----------------------------+-----------------------------------------------------+-------+

-- Remove the key `created.by.user` and `created.date` from `TBLPROPERTIES`
ALTER VIEW tempdb1.v2 UNSET TBLPROPERTIES ('created.by.user', 'created.date');

--Use `DESC TABLE EXTENDED tempdb1.v2` to verify the changes
DESC TABLE EXTENDED tempdb1.v2;
+----------------------------+----------+-------+
|                    col_name| data_type|comment|
+----------------------------+----------+-------+
|                          c1|       int|   null|
|                          c2|    string|   null|
|                            |          |       |
|# Detailed Table Information|          |       |
|                    Database|   tempdb1|       |
|                       Table|        v2|       |
|            Table Properties|    [....]|       |
+----------------------------+----------+-------+

-- Change the view definition
ALTER VIEW tempdb1.v2 AS SELECT * FROM tempdb1.v1;

-- Use `DESC TABLE EXTENDED` to verify
DESC TABLE EXTENDED tempdb1.v2;
+----------------------------+---------------------------+-------+
|                    col_name|                  data_type|comment|
+----------------------------+---------------------------+-------+
|                          c1|                        int|   null|
|                          c2|                     string|   null|
|                            |                           |       |
|# Detailed Table Information|                           |       |
|                    Database|                    tempdb1|       |
|                       Table|                         v2|       |
|                        Type|                       VIEW|       |
|                   View Text|   select * from tempdb1.v1|       |
|          View Original Text|   select * from tempdb1.v1|       |
+----------------------------+---------------------------+-------+
```