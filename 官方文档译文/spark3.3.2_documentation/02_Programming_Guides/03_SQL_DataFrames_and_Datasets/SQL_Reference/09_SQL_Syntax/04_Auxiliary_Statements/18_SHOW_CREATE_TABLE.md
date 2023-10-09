# SHOW CREATE TABLE

[TOC]

## Description

> `SHOW CREATE TABLE` returns the `CREATE TABLE` statement or `CREATE VIEW` statement that was used to create a given table or view. `SHOW CREATE TABLE` on a non-existent table or a temporary view throws an exception.

返回 `CREATE TABLE` 语句和 `CREATE VIEW` 语句。

如果使用在不存在的表或临时视图，将抛异常。

### Syntax

	SHOW CREATE TABLE table_identifier [ AS SERDE ]

### Parameters

- table_identifier

	Specifies a table or view name, which may be optionally qualified with a database name.
	
	Syntax: `[ database_name. ] table_name`

- AS SERDE

	Generates Hive DDL for a Hive SerDe table.

## Examples

```SQL
CREATE TABLE test (c INT) ROW FORMAT DELIMITED FIELDS TERMINATED BY ','
    STORED AS TEXTFILE
    TBLPROPERTIES ('prop1' = 'value1', 'prop2' = 'value2');

SHOW CREATE TABLE test;
+----------------------------------------------------+
|                                      createtab_stmt|
+----------------------------------------------------+
|CREATE TABLE `default`.`test` (`c` INT)
 USING text
 TBLPROPERTIES (
   'transient_lastDdlTime' = '1586269021',
   'prop1' = 'value1',
   'prop2' = 'value2')
+----------------------------------------------------+

SHOW CREATE TABLE test AS SERDE;
+------------------------------------------------------------------------------+
|                                                                createtab_stmt|
+------------------------------------------------------------------------------+
|CREATE TABLE `default`.`test`(
  `c` INT)
 ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe'
 WITH SERDEPROPERTIES (
   'serialization.format' = ',',
   'field.delim' = ',')
 STORED AS
   INPUTFORMAT 'org.apache.hadoop.mapred.TextInputFormat'
   OUTPUTFORMAT 'org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat'
 TBLPROPERTIES (
   'prop1' = 'value1',
   'prop2' = 'value2',
   'transient_lastDdlTime' = '1641800515')
+------------------------------------------------------------------------------+
```