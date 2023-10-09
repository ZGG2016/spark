# ALTER TABLE

[TOC]

## Description

> `ALTER TABLE` statement changes the schema or properties of a table.

`ALTER TABLE` 语句改变表的结构或属性。

## RENAME

> `ALTER TABLE RENAME TO` statement changes the table name of an existing table in the database. The table rename command cannot be used to move a table between databases, only to rename a table within the same database.

`ALTER TABLE RENAME TO` 语句改变数据库中已存在表的表名。

表的重命名命令不能用于在数据库间移动表，仅能在相同数据库下重命名表。

> If the table is cached, the commands clear cached data of the table. The cache will be lazily filled when the next time the table is accessed. Additionally:

如果表已被缓存，此命令会清除已缓存的数据。在下次访问表的时候，再将缓存懒填满。此外：

- 表重命名命令取消缓存所有表的依赖，例如指向表的视图。应该明确地再次缓存依赖。
- 如果表的所有依赖已被缓存，分区重命名命令清除这些缓存。所以，在下次访问它们的时候，再将缓存懒填满。

> the table rename command uncaches all table’s dependents such as views that refer to the table. The dependents should be cached again explicitly.
> the partition rename command clears caches of all table dependents while keeping them as cached. So, their caches will be lazily filled when the next time they are accessed.

### Syntax

	ALTER TABLE table_identifier RENAME TO table_identifier

	ALTER TABLE table_identifier partition_spec RENAME TO partition_spec

### Parameters

- table_identifier

	Specifies a table name, which may be optionally qualified with a database name.

	Syntax: `[ database_name. ] table_name`

- partition_spec

	Partition to be renamed. Note that one can use a typed literal (e.g., date’2019-01-02’) in the partition spec.

	Syntax: `PARTITION ( partition_col_name = partition_col_val [ , ... ] )`

## ADD COLUMNS

> `ALTER TABLE ADD COLUMNS` statement adds mentioned columns to an existing table.

`ALTER TABLE ADD COLUMNS` 语句将列添加到已存在的表中。

### Syntax

	ALTER TABLE table_identifier ADD COLUMNS ( col_spec [ , ... ] )

### Parameters

- table_identifier

	Specifies a table name, which may be optionally qualified with a database name.

	Syntax: `[ database_name. ] table_name`

- COLUMNS ( col_spec )

	Specifies the columns to be added.

## DROP COLUMNS

> `ALTER TABLE DROP COLUMNS` statement drops mentioned columns from an existing table. Note that this statement is only supported with v2 tables.

`ALTER TABLE DROP COLUMNS` 语句删除已存在表中的列。

此语句仅支持 v2 表。

### Syntax

	ALTER TABLE table_identifier DROP { COLUMN | COLUMNS } [ ( ] col_name [ , ... ] [ ) ]

### Parameters

- table_identifier

	Specifies a table name, which may be optionally qualified with a database name.
	
	Syntax: `[ database_name. ] table_name`

- col_name

	Specifies the name of the column.

## RENAME COLUMN

> `ALTER TABLE RENAME COLUMN` statement changes the column name of an existing table. Note that this statement is only supported with v2 tables.

`ALTER TABLE RENAME COLUMN` 语句改变已存在表中的列的名称。

此语句仅支持 v2 表。

### Syntax

	ALTER TABLE table_identifier RENAME COLUMN col_name TO col_name

### Parameters

- table_identifier

	Specifies a table name, which may be optionally qualified with a database name.

	Syntax: `[ database_name. ] table_name`

- col_name

	Specifies the name of the column.


## ALTER OR CHANGE COLUMN

> `ALTER TABLE ALTER COLUMN` or `ALTER TABLE CHANGE COLUMN` statement changes column’s definition.

`ALTER TABLE ALTER COLUMN` or `ALTER TABLE CHANGE COLUMN` 语句改变列的定义

### Syntax

	ALTER TABLE table_identifier { ALTER | CHANGE } [ COLUMN ] col_name alterColumnAction

### Parameters

- table_identifier

	Specifies a table name, which may be optionally qualified with a database name.
	
	Syntax: `[ database_name. ] table_name`

- col_name

	Specifies the name of the column.

- alterColumnAction

	Change column’s definition.

## REPLACE COLUMNS

> `ALTER TABLE REPLACE COLUMNS` statement removes all existing columns and adds the new set of columns. Note that this statement is only supported with v2 tables.

`ALTER TABLE REPLACE COLUMNS` 语句移除所有已存在的列，并添加新的列。

此语句仅支持 v2 表。

### Syntax

	ALTER TABLE table_identifier [ partition_spec ] REPLACE COLUMNS  
  		[ ( ] qualified_col_type_with_position_list [ ) ]

### Parameters

- table_identifier

	Specifies a table name, which may be optionally qualified with a database name.
	
	Syntax: `[ database_name. ] table_name`

- partition_spec

	Partition to be replaced. Note that one can use a typed literal (e.g., date’2019-01-02’) in the partition spec.
	
	Syntax: `PARTITION ( partition_col_name = partition_col_val [ , ... ] )`

- qualified_col_type_with_position_list

	The list of the column(s) to be added
	
	Syntax: `col_name col_type [ col_comment ] [ col_position ] [ , ... ]`

## ADD AND DROP PARTITION

### ADD PARTITION

> `ALTER TABLE ADD` statement adds partition to the partitioned table.

`ALTER TABLE ADD` 语句往分区表中添加分区。

> If the table is cached, the command clears cached data of the table and all its dependents that refer to it. The cache will be lazily filled when the next time the table or the dependents are accessed.

如果表已被缓存，命令会清除表的缓存数据和所有依赖

在下次访问表或依赖时，缓存将被懒填满。

#### Syntax

	ALTER TABLE table_identifier ADD [IF NOT EXISTS] 
    	( partition_spec [ partition_spec ... ] )

#### Parameters

- table_identifier

	Specifies a table name, which may be optionally qualified with a database name.
	
	Syntax: `[ database_name. ] table_name`

- partition_spec

	Partition to be added. Note that one can use a typed literal (e.g., date’2019-01-02’) in the partition spec.
	
	Syntax: `PARTITION ( partition_col_name = partition_col_val [ , ... ] )`

### DROP PARTITION

> `ALTER TABLE DROP` statement drops the partition of the table.

`ALTER TABLE DROP` 语句往分区表中删除分区。

> If the table is cached, the command clears cached data of the table and all its dependents that refer to it. The cache will be lazily filled when the next time the table or the dependents are accessed.

如果表已被缓存，命令会清除表的缓存数据和所有依赖

在下次访问表或依赖时，缓存将被懒填满。

#### Syntax

	ALTER TABLE table_identifier DROP [ IF EXISTS ] partition_spec [PURGE]

#### Parameters

- table_identifier

	Specifies a table name, which may be optionally qualified with a database name.
	
	Syntax: `[ database_name. ] table_name`

- partition_spec

	Partition to be dropped. Note that one can use a typed literal (e.g., date’2019-01-02’) in the partition spec.
	
	Syntax: `PARTITION ( partition_col_name = partition_col_val [ , ... ] )`

## SET AND UNSET

### SET TABLE PROPERTIES

> `ALTER TABLE SET` command is used for setting the table properties. If a particular property was already set, this overrides the old value with the new one.

`ALTER TABLE SET` 命令用来设置表的属性

> `ALTER TABLE UNSET` is used to drop the table property.

`ALTER TABLE UNSET` 用来删除表的属性

#### Syntax

	-- Set Table Properties 
	ALTER TABLE table_identifier SET TBLPROPERTIES ( key1 = val1, key2 = val2, ... )
	
	-- Unset Table Properties
	ALTER TABLE table_identifier UNSET TBLPROPERTIES [ IF EXISTS ] ( key1, key2, ... )

### SET SERDE

> `ALTER TABLE SET` command is used for setting the SERDE or SERDE properties in Hive tables. If a particular property was already set, this overrides the old value with the new one.

`ALTER TABLE SET` 命令用来设置 Hive 表的 SERDE 属性。

如果已设置了一个属性，这就会使用新值覆盖旧值。

#### Syntax

	-- Set SERDE Properties
	ALTER TABLE table_identifier [ partition_spec ]
    	SET SERDEPROPERTIES ( key1 = val1, key2 = val2, ... )
	
	ALTER TABLE table_identifier [ partition_spec ] SET SERDE serde_class_name
    	[ WITH SERDEPROPERTIES ( key1 = val1, key2 = val2, ... ) ]

### SET LOCATION And SET FILE FORMAT

> `ALTER TABLE SET` command can also be used for changing the file location and file format for existing tables.

`ALTER TABLE SET` 命令也可用来改变已存在表的文件路径和文件格式。

> If the table is cached, the `ALTER TABLE .. SET LOCATION` command clears cached data of the table and all its dependents that refer to it. The cache will be lazily filled when the next time the table or the dependents are accessed.

如果表已被缓存，命令 `ALTER TABLE .. SET LOCATION` 会清除表的缓存数据和所有依赖。

在下次访问表或依赖时，缓存将被懒填满。

#### Syntax

	-- Changing File Format
	ALTER TABLE table_identifier [ partition_spec ] SET FILEFORMAT file_format
	
	-- Changing File Location
	ALTER TABLE table_identifier [ partition_spec ] SET LOCATION 'new_location'

#### Parameters

- table_identifier

	Specifies a table name, which may be optionally qualified with a database name.
	
	Syntax: `[ database_name. ] table_name`

- partition_spec

	Specifies the partition on which the property has to be set. Note that one can use a typed literal (e.g., date’2019-01-02’) in the partition spec.
	
	Syntax: `PARTITION ( partition_col_name = partition_col_val [ , ... ] )`

- SERDEPROPERTIES ( key1 = val1, key2 = val2, … )

	Specifies the SERDE properties to be set.

## RECOVER PARTITIONS

> `ALTER TABLE RECOVER PARTITIONS` statement recovers all the partitions in the directory of a table and updates the Hive metastore. Another way to recover partitions is to use `MSCK REPAIR TABLE`.

`ALTER TABLE RECOVER PARTITIONS` 语句恢复表目录中的所有分区，并更新 Hive metastore.

另一个恢复分区的方法是 `MSCK REPAIR TABLE`.

### Syntax

	ALTER TABLE table_identifier RECOVER PARTITIONS

### Parameters

- table_identifier
	
	Specifies a table name, which may be optionally qualified with a database name.
	
	Syntax: `[ database_name. ] table_name`

## Examples

```
-- RENAME table 
DESC student;
+-----------------------+---------+-------+
|               col_name|data_type|comment|
+-----------------------+---------+-------+
|                   name|   string|   NULL|
|                 rollno|      int|   NULL|
|                    age|      int|   NULL|
|# Partition Information|         |       |
|             # col_name|data_type|comment|
|                    age|      int|   NULL|
+-----------------------+---------+-------+

ALTER TABLE Student RENAME TO StudentInfo;

-- After Renaming the table
DESC StudentInfo;
+-----------------------+---------+-------+
|               col_name|data_type|comment|
+-----------------------+---------+-------+
|                   name|   string|   NULL|
|                 rollno|      int|   NULL|
|                    age|      int|   NULL|
|# Partition Information|         |       |
|             # col_name|data_type|comment|
|                    age|      int|   NULL|
+-----------------------+---------+-------+

-- RENAME partition

SHOW PARTITIONS StudentInfo;
+---------+
|partition|
+---------+
|   age=10|
|   age=11|
|   age=12|
+---------+

ALTER TABLE default.StudentInfo PARTITION (age='10') RENAME TO PARTITION (age='15');

-- After renaming Partition
SHOW PARTITIONS StudentInfo;
+---------+
|partition|
+---------+
|   age=11|
|   age=12|
|   age=15|
+---------+

-- Add new columns to a table
DESC StudentInfo;
+-----------------------+---------+-------+
|               col_name|data_type|comment|
+-----------------------+---------+-------+
|                   name|   string|   NULL|
|                 rollno|      int|   NULL|
|                    age|      int|   NULL|
|# Partition Information|         |       |
|             # col_name|data_type|comment|
|                    age|      int|   NULL|
+-----------------------+---------+-------+

ALTER TABLE StudentInfo ADD columns (LastName string, DOB timestamp);

-- After Adding New columns to the table
DESC StudentInfo;
+-----------------------+---------+-------+
|               col_name|data_type|comment|
+-----------------------+---------+-------+
|                   name|   string|   NULL|
|                 rollno|      int|   NULL|
|               LastName|   string|   NULL|
|                    DOB|timestamp|   NULL|
|                    age|      int|   NULL|
|# Partition Information|         |       |
|             # col_name|data_type|comment|
|                    age|      int|   NULL|
+-----------------------+---------+-------+

-- Drop columns of a table
DESC StudentInfo;
+-----------------------+---------+-------+
|               col_name|data_type|comment|
+-----------------------+---------+-------+
|                   name|   string|   NULL|
|                 rollno|      int|   NULL|
|               LastName|   string|   NULL|
|                    DOB|timestamp|   NULL|
|                    age|      int|   NULL|
|# Partition Information|         |       |
|             # col_name|data_type|comment|
|                    age|      int|   NULL|
+-----------------------+---------+-------+

ALTER TABLE StudentInfo DROP columns (LastName, DOB);

-- After dropping columns of the table
DESC StudentInfo;
+-----------------------+---------+-------+
|               col_name|data_type|comment|
+-----------------------+---------+-------+
|                   name|   string|   NULL|
|                 rollno|      int|   NULL|
|                    age|      int|   NULL|
|# Partition Information|         |       |
|             # col_name|data_type|comment|
|                    age|      int|   NULL|
+-----------------------+---------+-------+

-- Rename a column of a table
DESC StudentInfo;
+-----------------------+---------+-------+
|               col_name|data_type|comment|
+-----------------------+---------+-------+
|                   name|   string|   NULL|
|                 rollno|      int|   NULL|
|                    age|      int|   NULL|
|# Partition Information|         |       |
|             # col_name|data_type|comment|
|                    age|      int|   NULL|
+-----------------------+---------+-------+

ALTER TABLE StudentInfo RENAME COLUMN name TO FirstName;

-- After renaming a column of the table
DESC StudentInfo;
+-----------------------+---------+-------+
|               col_name|data_type|comment|
+-----------------------+---------+-------+
|              FirstName|   string|   NULL|
|                 rollno|      int|   NULL|
|                    age|      int|   NULL|
|# Partition Information|         |       |
|             # col_name|data_type|comment|
|                    age|      int|   NULL|
+-----------------------+---------+-------+

-- ALTER OR CHANGE COLUMNS
DESC StudentInfo;
+-----------------------+---------+-------+
|               col_name|data_type|comment|
+-----------------------+---------+-------+
|              FirstName|   string|   NULL|
|                 rollno|      int|   NULL|
|                    age|      int|   NULL|
|# Partition Information|         |       |
|             # col_name|data_type|comment|
|                    age|      int|   NULL|
+-----------------------+---------+-------+

ALTER TABLE StudentInfo ALTER COLUMN FirstName COMMENT "new comment";

-- After ALTER or CHANGE COLUMNS
DESC StudentInfo;
+-----------------------+---------+-----------+
|               col_name|data_type|    comment|
+-----------------------+---------+-----------+
|              FirstName|   string|new comment|
|                 rollno|      int|       NULL|
|                    age|      int|       NULL|
|# Partition Information|         |           |
|             # col_name|data_type|    comment|
|                    age|      int|       NULL|
+-----------------------+---------+-----------+

-- REPLACE COLUMNS
DESC StudentInfo;
+-----------------------+---------+-----------+
|               col_name|data_type|    comment|
+-----------------------+---------+-----------+
|              FirstName|   string|new comment|
|                 rollno|      int|       NULL|
|                    age|      int|       NULL|
|# Partition Information|         |           |
|             # col_name|data_type|    comment|
|                    age|      int|       NULL|
+-----------------------+---------+-----------+

ALTER TABLE StudentInfo REPLACE COLUMNS (name string, ID int COMMENT 'new comment');

-- After replacing COLUMNS
DESC StudentInfo;
+-----=---------+---------+-----------+
|       col_name|data_type|    comment|
+---------------+---------+-----------+
|           name|   string|       NULL|
|             ID|      int|new comment|
| # Partitioning|         |           |
|Not partitioned|         |           |
+---------------+---------+-----------+

-- Add a new partition to a table 
SHOW PARTITIONS StudentInfo;
+---------+
|partition|
+---------+
|   age=11|
|   age=12|
|   age=15|
+---------+

ALTER TABLE StudentInfo ADD IF NOT EXISTS PARTITION (age=18);

-- After adding a new partition to the table
SHOW PARTITIONS StudentInfo;
+---------+
|partition|
+---------+
|   age=11|
|   age=12|
|   age=15|
|   age=18|
+---------+

-- Drop a partition from the table 
SHOW PARTITIONS StudentInfo;
+---------+
|partition|
+---------+
|   age=11|
|   age=12|
|   age=15|
|   age=18|
+---------+

ALTER TABLE StudentInfo DROP IF EXISTS PARTITION (age=18);

-- After dropping the partition of the table
SHOW PARTITIONS StudentInfo;
+---------+
|partition|
+---------+
|   age=11|
|   age=12|
|   age=15|
+---------+

-- Adding multiple partitions to the table
SHOW PARTITIONS StudentInfo;
+---------+
|partition|
+---------+
|   age=11|
|   age=12|
|   age=15|
+---------+

ALTER TABLE StudentInfo ADD IF NOT EXISTS PARTITION (age=18) PARTITION (age=20);

-- After adding multiple partitions to the table
SHOW PARTITIONS StudentInfo;
+---------+
|partition|
+---------+
|   age=11|
|   age=12|
|   age=15|
|   age=18|
|   age=20|
+---------+

-- Change the fileformat
ALTER TABLE loc_orc SET fileformat orc;

ALTER TABLE p1 partition (month=2, day=2) SET fileformat parquet;

-- Change the file Location
ALTER TABLE dbx.tab1 PARTITION (a='1', b='2') SET LOCATION '/path/to/part/ways'

-- SET SERDE/ SERDE Properties
ALTER TABLE test_tab SET SERDE 'org.apache.hadoop.hive.serde2.columnar.LazyBinaryColumnarSerDe';

ALTER TABLE dbx.tab1 SET SERDE 'org.apache.hadoop' WITH SERDEPROPERTIES ('k' = 'v', 'kay' = 'vee')

-- SET TABLE PROPERTIES
ALTER TABLE dbx.tab1 SET TBLPROPERTIES ('winner' = 'loser');

-- SET TABLE COMMENT Using SET PROPERTIES
ALTER TABLE dbx.tab1 SET TBLPROPERTIES ('comment' = 'A table comment.');

-- Alter TABLE COMMENT Using SET PROPERTIES
ALTER TABLE dbx.tab1 SET TBLPROPERTIES ('comment' = 'This is a new comment.');

-- DROP TABLE PROPERTIES
ALTER TABLE dbx.tab1 UNSET TBLPROPERTIES ('winner');

-- RECOVER PARTITIONS
ALTER TABLE dbx.tab1 RECOVER PARTITIONS;
```