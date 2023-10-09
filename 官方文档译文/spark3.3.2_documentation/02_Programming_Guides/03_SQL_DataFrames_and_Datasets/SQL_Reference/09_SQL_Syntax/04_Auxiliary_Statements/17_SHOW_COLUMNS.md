# SHOW COLUMNS

[TOC]

## Description

> Returns the list of columns in a table. If the table does not exist, an exception is thrown.

返回一张表的列的列表。如果表不存在，抛异常。

### Syntax

	SHOW COLUMNS table_identifier [ database ]

### Parameters

- table_identifier

	Specifies the table name of an existing table. The table may be optionally qualified with a database name.
	
	Syntax: `{ IN | FROM } [ database_name . ] table_name`

	Note: Keywords `IN` and `FROM` are interchangeable.

- database

	Specifies an optional database name. The table is resolved from this database when it is specified. When this parameter is specified then table name should not be qualified with a different database name.
	
	Syntax: { IN | FROM } database_name
	
	Note: Keywords `IN` and `FROM` are interchangeable.

## Examples

```SQL
-- Create `customer` table in `salesdb` database;
USE salesdb;
CREATE TABLE customer(
    cust_cd INT,
    name VARCHAR(100),
    cust_addr STRING);

-- List the columns of `customer` table in current database.
SHOW COLUMNS IN customer;
+---------+
| col_name|
+---------+
|  cust_cd|
|     name|
|cust_addr|
+---------+

-- List the columns of `customer` table in `salesdb` database.
SHOW COLUMNS IN salesdb.customer;
+---------+
| col_name|
+---------+
|  cust_cd|
|     name|
|cust_addr|
+---------+

-- List the columns of `customer` table in `salesdb` database
SHOW COLUMNS IN customer IN salesdb;
+---------+
| col_name|
+---------+
|  cust_cd|
|     name|
|cust_addr|
+---------+
```