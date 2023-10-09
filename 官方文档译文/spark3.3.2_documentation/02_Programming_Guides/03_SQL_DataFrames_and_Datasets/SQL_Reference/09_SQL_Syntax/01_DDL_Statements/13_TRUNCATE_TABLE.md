# TRUNCATE TABLE

[TOC]

## Description

> The `TRUNCATE TABLE` statement removes all the rows from a table or partition(s). The table must not be a view or an external/temporary table. In order to truncate multiple partitions at once, the user can specify the partitions in `partition_spec`. If no `partition_spec` is specified it will remove all partitions in the table.

移除表或分区的所有行。表必须不能是视图或外部表或临时表。

为了一次清空多个分区，用户可以在 `partition_spec` 中指定分区。如果没有指定，就删除表中的所有分区。

> If the table is cached, the command clears cached data of the table and all its dependents that refer to it. The cache will be lazily filled when the next time the table or the dependents are accessed.

如果表已被缓存，命令会清除表的缓存数据和所有依赖

在下次访问表或依赖时，缓存将被懒填满。

### Syntax

	TRUNCATE TABLE table_identifier [ partition_spec ]

### Parameters

- table_identifier

	Specifies a table name, which may be optionally qualified with a database name.
	
	Syntax: `[ database_name. ] table_name`

- partition_spec

	An optional parameter that specifies a comma separated list of key and value pairs for partitions.
	
	Syntax: `PARTITION ( partition_col_name = partition_col_val [ , ... ] )`

## Examples

```sql
-- Create table Student with partition
CREATE TABLE Student (name STRING, rollno INT) PARTITIONED BY (age INT);

SELECT * FROM Student;
+----+------+---+
|name|rollno|age|
+----+------+---+
| ABC|     1| 10|
| DEF|     2| 10|
| XYZ|     3| 12|
+----+------+---+

-- Removes all rows from the table in the partition specified
TRUNCATE TABLE Student partition(age=10);

-- After truncate execution, records belonging to partition age=10 are removed
SELECT * FROM Student;
+----+------+---+
|name|rollno|age|
+----+------+---+
| XYZ|     3| 12|
+----+------+---+

-- Removes all rows from the table from all partitions
TRUNCATE TABLE Student;

SELECT * FROM Student;
+----+------+---+
|name|rollno|age|
+----+------+---+
+----+------+---+
```