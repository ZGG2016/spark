# SHOW PARTITIONS

[TOC]

## Description

> The `SHOW PARTITIONS` statement is used to list partitions of a table. An optional partition spec may be specified to return the partitions matching the supplied partition spec.

`SHOW PARTITIONS` 语句用来列出一张表的分区。

可以指定可选的分区描述，来返回与提供的分区描述匹配的分区。

### Syntax

	SHOW PARTITIONS table_identifier [ partition_spec ]

### Parameters

- table_identifier

	Specifies a table name, which may be optionally qualified with a database name.
	
	Syntax: `[ database_name. ] table_name`

- partition_spec

	An optional parameter that specifies a comma separated list of key and value pairs for partitions. When specified, the partitions that match the partition specification are returned.
	
	Syntax: `PARTITION ( partition_col_name = partition_col_val [ , ... ] )`

## Examples

```sql
-- create a partitioned table and insert a few rows.
USE salesdb;
CREATE TABLE customer(id INT, name STRING) PARTITIONED BY (state STRING, city STRING);
INSERT INTO customer PARTITION (state = 'CA', city = 'Fremont') VALUES (100, 'John');
INSERT INTO customer PARTITION (state = 'CA', city = 'San Jose') VALUES (200, 'Marry');
INSERT INTO customer PARTITION (state = 'AZ', city = 'Peoria') VALUES (300, 'Daniel');

-- Lists all partitions for table `customer`
SHOW PARTITIONS customer;
+----------------------+
|             partition|
+----------------------+
|  state=AZ/city=Peoria|
| state=CA/city=Fremont|
|state=CA/city=San Jose|
+----------------------+

-- Lists all partitions for the qualified table `customer`
SHOW PARTITIONS salesdb.customer;
+----------------------+
|             partition|
+----------------------+
|  state=AZ/city=Peoria|
| state=CA/city=Fremont|
|state=CA/city=San Jose|
+----------------------+

-- Specify a full partition spec to list specific partition
SHOW PARTITIONS customer PARTITION (state = 'CA', city = 'Fremont');
+---------------------+
|            partition|
+---------------------+
|state=CA/city=Fremont|
+---------------------+

-- Specify a partial partition spec to list the specific partitions
SHOW PARTITIONS customer PARTITION (state = 'CA');
+----------------------+
|             partition|
+----------------------+
| state=CA/city=Fremont|
|state=CA/city=San Jose|
+----------------------+

-- Specify a partial spec to list specific partition
SHOW PARTITIONS customer PARTITION (city =  'San Jose');
+----------------------+
|             partition|
+----------------------+
|state=CA/city=San Jose|
+----------------------+
```