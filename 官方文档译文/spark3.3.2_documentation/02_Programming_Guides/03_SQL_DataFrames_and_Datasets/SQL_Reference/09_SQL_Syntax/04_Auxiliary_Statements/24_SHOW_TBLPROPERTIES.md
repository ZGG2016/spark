# SHOW TBLPROPERTIES

[TOC]

## Description

> This statement returns the value of a table property given an optional value for a property key. If no key is specified then all the properties are returned.

返回一张表给定的属性键对应的值。

如果没有指定键，就返回所有的属性

### Syntax

	SHOW TBLPROPERTIES table_identifier 
   	[ ( unquoted_property_key | property_key_as_string_literal ) ]

### Parameters

- table_identifier

	Specifies the table name of an existing table. The table may be optionally qualified with a database name.
	
	Syntax: `[ database_name. ] table_name`

- unquoted_property_key

	Specifies the property key in unquoted form. The key may consists of multiple parts separated by dot.
	
	Syntax: `[ key_part1 ] [ .key_part2 ] [ ... ]`

- property_key_as_string_literal

	Specifies a property key value as a string literal.

Note

- Property value returned by this statement excludes some properties that are internal to spark and hive. The excluded properties are :
	+ All the properties that start with prefix `spark.sql`
	+ Property keys such as: `EXTERNAL`, `comment`
	+ All the properties generated internally by hive to store statistics. Some of these properties are: `numFiles`, `numPartitions`, `numRows`.

## Examples

```sql
-- create a table `customer` in database `salesdb`
USE salesdb;
CREATE TABLE customer(cust_code INT, name VARCHAR(100), cust_addr STRING)
    TBLPROPERTIES ('created.by.user' = 'John', 'created.date' = '01-01-2001');

-- show all the user specified properties for table `customer`
SHOW TBLPROPERTIES customer;
+---------------------+----------+
|                  key|     value|
+---------------------+----------+
|      created.by.user|      John|
|         created.date|01-01-2001|
|transient_lastDdlTime|1567554931|
+---------------------+----------+

-- show all the user specified properties for a qualified table `customer`
-- in database `salesdb`
SHOW TBLPROPERTIES salesdb.customer;
+---------------------+----------+
|                  key|     value|
+---------------------+----------+
|      created.by.user|      John|
|         created.date|01-01-2001|
|transient_lastDdlTime|1567554931|
+---------------------+----------+

-- show value for unquoted property key `created.by.user`
SHOW TBLPROPERTIES customer (created.by.user);
+-----+
|value|
+-----+
| John|
+-----+

-- show value for property `created.date`` specified as string literal
SHOW TBLPROPERTIES customer ('created.date');
+----------+
|     value|
+----------+
|01-01-2001|
+----------+