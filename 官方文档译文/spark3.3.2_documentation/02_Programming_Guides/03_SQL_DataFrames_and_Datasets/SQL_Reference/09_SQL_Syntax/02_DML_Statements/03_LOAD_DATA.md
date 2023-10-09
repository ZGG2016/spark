# LOAD DATA

## Description

> `LOAD DATA` statement loads the data into a Hive serde table from the user specified directory or file. If a directory is specified then all the files from the directory are loaded. If a file is specified then only the single file is loaded. Additionally the `LOAD DATA` statement takes an optional partition specification. When a partition is specified, the data files (when input source is a directory) or the single file (when input source is a file) are loaded into the partition of the target table.

`LOAD DATA` 语句将数据从用户指定的目录或文件载入到 Hive serde 表。

如果指定的是目录，那么目录下的所有文件都被载入。如果指定的是文件，仅单个文件被载入。

另外，`LOAD DATA` 语句接收可选的分区描述。当指定分区时，数据文件（输入源是目录）或单个文件（输入源是文件）被载入到目标表的分区。

> If the table is cached, the command clears cached data of the table and all its dependents that refer to it. The cache will be lazily filled when the next time the table or the dependents are accessed.

如果表已被缓存，命令会清除表的缓存数据和所有依赖

在下次访问表或依赖时，缓存将被懒填满。

### Syntax

	LOAD DATA [ LOCAL ] INPATH path [ OVERWRITE ] INTO TABLE table_identifier [ partition_spec ]

### Parameters

- path

	Path of the file system. It can be either an absolute or a relative path.

- table_identifier

	Specifies a table name, which may be optionally qualified with a database name.
	
	Syntax: `[ database_name. ] table_name`

- partition_spec

	An optional parameter that specifies a comma separated list of key and value pairs for partitions.
	
	Syntax: `PARTITION ( partition_col_name = partition_col_val [ , ... ] )`

- LOCAL

	If specified, it causes the `INPATH` to be resolved against the local file system, instead of the default file system, which is typically a distributed storage.

- OVERWRITE

	By default, new data is appended to the table. If `OVERWRITE` is used, the table is instead overwritten with new data.

## Examples

```sql
-- Example without partition specification.
-- Assuming the students table has already been created and populated.
SELECT * FROM students;
+---------+----------------------+----------+
|     name|               address|student_id|
+---------+----------------------+----------+
|Amy Smith|123 Park Ave, San Jose|    111111|
+---------+----------------------+----------+

CREATE TABLE test_load (name VARCHAR(64), address VARCHAR(64), student_id INT) USING HIVE;

-- Assuming the students table is in '/user/hive/warehouse/'
LOAD DATA LOCAL INPATH '/user/hive/warehouse/students' OVERWRITE INTO TABLE test_load;

SELECT * FROM test_load;
+---------+----------------------+----------+
|     name|               address|student_id|
+---------+----------------------+----------+
|Amy Smith|123 Park Ave, San Jose|    111111|
+---------+----------------------+----------+

-- Example with partition specification.
CREATE TABLE test_partition (c1 INT, c2 INT, c3 INT) PARTITIONED BY (c2, c3);

INSERT INTO test_partition PARTITION (c2 = 2, c3 = 3) VALUES (1);

INSERT INTO test_partition PARTITION (c2 = 5, c3 = 6) VALUES (4);

INSERT INTO test_partition PARTITION (c2 = 8, c3 = 9) VALUES (7);

SELECT * FROM test_partition;
+---+---+---+
| c1| c2| c3|
+---+---+---+
|  1|  2|  3|
|  4|  5|  6|
|  7|  8|  9|
+---+---+---+

CREATE TABLE test_load_partition (c1 INT, c2 INT, c3 INT) USING HIVE PARTITIONED BY (c2, c3);

-- Assuming the test_partition table is in '/user/hive/warehouse/'
LOAD DATA LOCAL INPATH '/user/hive/warehouse/test_partition/c2=2/c3=3'
    OVERWRITE INTO TABLE test_load_partition PARTITION (c2=2, c3=3);

SELECT * FROM test_load_partition;
+---+---+---+
| c1| c2| c3|
+---+---+---+
|  1|  2|  3|
+---+---+---+
```