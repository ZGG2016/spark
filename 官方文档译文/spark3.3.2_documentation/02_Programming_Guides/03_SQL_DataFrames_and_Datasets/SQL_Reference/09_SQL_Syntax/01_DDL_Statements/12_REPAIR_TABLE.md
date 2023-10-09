# REPAIR TABLE

[TOC]

## Description

> `MSCK REPAIR TABLE` recovers all the partitions in the directory of a table and updates the Hive metastore. When creating a table using `PARTITIONED BY` clause, partitions are generated and registered in the Hive metastore. However, if the partitioned table is created from existing data, partitions are not registered automatically in the Hive metastore. User needs to run `MSCK REPAIR TABLE` to register the partitions. `MSCK REPAIR TABLE` on a non-existent table or a table without partitions throws an exception. Another way to recover partitions is to use `ALTER TABLE RECOVER PARTITIONS`.

`MSCK REPAIR TABLE` 恢复表目录中的所有分区，更新 Hive metastore.

当使用 `PARTITIONED BY` 子句创建表时，会生成分区，且在 Hive metastore 中注册。

然而，如果分区表根据已存在数据中创建而来，分区并不会自动在 Hive metastore 中注册。用户需要运行 `MSCK REPAIR TABLE`来注册分区。

在不存在的表上或没有分区的表上执行 `MSCK REPAIR TABLE` 会抛一个异常。

另一种恢复分区的方式是 `ALTER TABLE RECOVER PARTITIONS`.

> If the table is cached, the command clears cached data of the table and all its dependents that refer to it. The cache will be lazily filled when the next time the table or the dependents are accessed.

如果表已被缓存，命令会清除表的缓存数据和所有依赖

在下次访问表或依赖时，缓存将被懒填满。

### Syntax

	MSCK REPAIR TABLE table_identifier [{ADD|DROP|SYNC} PARTITIONS]

### Parameters

- table_identifier

	Specifies the name of the table to be repaired. The table name may be optionally qualified with a database name.
	
	Syntax: `[ database_name. ] table_name`

- {ADD|DROP|SYNC} PARTITIONS

	Specifies how to recover partitions. If not specified, ADD is the default.

	- ADD, the command adds new partitions to the session catalog for all sub-folder in the base table folder that don’t belong to any table partitions.

	- DROP, the command drops all partitions from the session catalog that have non-existing locations in the file system.

	- SYNC is the combination of DROP and ADD.

## Examples

```sql
-- create a partitioned table from existing data /tmp/namesAndAges.parquet
CREATE TABLE t1 (name STRING, age INT) USING parquet PARTITIONED BY (age)
    LOCATION "/tmp/namesAndAges.parquet";

-- SELECT * FROM t1 does not return results
SELECT * FROM t1;

-- run MSCK REPAIR TABLE to recovers all the partitions
MSCK REPAIR TABLE t1;

-- SELECT * FROM t1 returns results
SELECT * FROM t1;
+-------+---+
|   name|age|
+-------+---+
|Michael| 20|
+-------+---+
| Justin| 19|
+-------+---+
|   Andy| 30|
+-------+---+
```