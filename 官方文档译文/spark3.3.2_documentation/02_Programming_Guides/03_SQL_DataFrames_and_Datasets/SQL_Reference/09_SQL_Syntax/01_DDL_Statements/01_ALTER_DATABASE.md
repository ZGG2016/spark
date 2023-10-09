# ALTER DATABASE

[TOC]

## Description

> `ALTER DATABASE` statement changes the properties or location of a database. Please note that the usage of `DATABASE`, `SCHEMA` and `NAMESPACE` are interchangeable and one can be used in place of the others. An error message is issued if the database is not found in the system.

`ALTER DATABASE` 语句改变数据库的属性或路径。

`DATABASE`, `SCHEMA` 和 `NAMESPACE` 是可互换的，其中一个可以放在其他的位置上。

如果数据库没有在系统中找到，就会发出一个错误信息。

## ALTER PROPERTIES

> `ALTER DATABASE SET DBPROPERTIES` statement changes the properties associated with a database. The specified property values override any existing value with the same property name. This command is mostly used to record the metadata for a database and may be used for auditing purposes.

`ALTER DATABASE SET DBPROPERTIES` 语句改变数据库的属性。

对于相同的属性名称，指定的属性值覆盖已存在的值。

这条命令主要用来记录数据库的元数据，和用作审计。

### Syntax

	ALTER { DATABASE | SCHEMA | NAMESPACE } database_name
	    SET { DBPROPERTIES | PROPERTIES } ( property_name = property_value [ , ... ] )

### Parameters

- database_name

	Specifies the name of the database to be altered.

## ALTER LOCATION

> `ALTER DATABASE SET LOCATION` statement changes the default parent-directory where new tables will be added for a database. Please note that it does not move the contents of the database’s current directory to the newly specified location or change the locations associated with any tables/partitions under the specified database (available since Spark 3.0.0 with the Hive metastore version 3.0.0 and later).

`ALTER DATABASE SET LOCATION` 语句改变将被添加到数据库的新表的默认父路径。

注意：它并不会将数据库当前目录的内容移动到新指定的路径下，或者改变数据库下任意相关的表/分区的路径。（Spark 3.0.0 和 Hive metastore 3.0.0之后可用）

### Syntax

	ALTER { DATABASE | SCHEMA | NAMESPACE } database_name
	    SET LOCATION 'new_location'

### Parameters

- database_name

	Specifies the name of the database to be altered.

## Examples

```sql
-- Creates a database named `inventory`.
CREATE DATABASE inventory;

-- Alters the database to set properties `Edited-by` and `Edit-date`.
ALTER DATABASE inventory SET DBPROPERTIES ('Edited-by' = 'John', 'Edit-date' = '01/01/2001');

-- Verify that properties are set.
DESCRIBE DATABASE EXTENDED inventory;
+-------------------------+------------------------------------------+
|database_description_item|                database_description_value|
+-------------------------+------------------------------------------+
|            Database Name|                                 inventory|
|              Description|                                          |
|                 Location|   file:/temp/spark-warehouse/inventory.db|
|               Properties|((Edit-date,01/01/2001), (Edited-by,John))|
+-------------------------+------------------------------------------+

-- Alters the database to set a new location.
ALTER DATABASE inventory SET LOCATION 'file:/temp/spark-warehouse/new_inventory.db';

-- Verify that a new location is set.
DESCRIBE DATABASE EXTENDED inventory;
+-------------------------+-------------------------------------------+
|database_description_item|                 database_description_value|
+-------------------------+-------------------------------------------+
|            Database Name|                                  inventory|
|              Description|                                           |
|                 Location|file:/temp/spark-warehouse/new_inventory.db|
|               Properties| ((Edit-date,01/01/2001), (Edited-by,John))|
+-------------------------+-------------------------------------------+
```