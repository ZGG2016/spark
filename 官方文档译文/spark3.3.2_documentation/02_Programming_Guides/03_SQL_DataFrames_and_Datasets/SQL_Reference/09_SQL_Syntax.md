# SQL Syntax

> Spark SQL is Apache Spark’s module for working with structured data. The SQL Syntax section describes the SQL syntax in detail along with usage examples when applicable. This document provides a list of Data Definition and Data Manipulation Statements, as well as Data Retrieval and Auxiliary Statements.

## DDL Statements

> Data Definition Statements are used to create or modify the structure of database objects in a database. Spark SQL supports the following Data Definition Statements:

- [ALTER DATABASE](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-ddl-alter-database.html)
- [ALTER TABLE](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-ddl-alter-table.html)
- [ALTER VIEW](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-ddl-alter-view.html)
- [CREATE DATABASE](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-ddl-create-database.html)
- [CREATE FUNCTION](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-ddl-create-function.html)
- [CREATE TABLE](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-ddl-create-table.html)
- [CREATE VIEW](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-ddl-create-view.html)
- [DROP DATABASE](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-ddl-drop-database.html)
- [DROP FUNCTION](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-ddl-drop-function.html)
- [DROP TABLE](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-ddl-repair-table.html)
- [DROP VIEW](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-ddl-drop-view.html)
- [REPAIR TABLE](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-ddl-repair-table.html)
- [TRUNCATE TABLE](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-ddl-truncate-table.html)
- [USE DATABASE](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-ddl-usedb.html)

------------------------------

- [ALTER DATABASE]()
- [ALTER TABLE]()
- [ALTER VIEW]()
- [CREATE DATABASE]()
- [CREATE FUNCTION]()
- [CREATE TABLE]()
- [CREATE VIEW]()
- [DROP DATABASE]()
- [DROP FUNCTION]()
- [DROP TABLE]()
- [DROP VIEW]()
- [REPAIR TABLE]()
- [TRUNCATE TABLE]()
- [USE DATABASE]()

## DML Statements

> Data Manipulation Statements are used to add, change, or delete data. Spark SQL supports the following Data Manipulation Statements:

- [INSERT TABLE](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-dml-insert-table.html)
- [INSERT OVERWRITE DIRECTORY](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-dml-insert-overwrite-directory.html)
- [LOAD](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-dml-load.html)

------------------------

- [INSERT TABLE]()
- [INSERT OVERWRITE DIRECTORY]()
- [LOAD]()


## Data Retrieval Statements

> Spark supports SELECT statement that is used to retrieve rows from one or more tables according to the specified clauses. The full syntax and brief description of supported clauses are explained in SELECT section. The SQL statements related to SELECT are also included in this section. Spark also provides the ability to generate logical and physical plan for a given query using EXPLAIN statement.

- [SELECT Statement](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-qry-select.html)
	+ [Common Table Expression](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-qry-select-cte.html)
	+ [CLUSTER BY Clause](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-qry-select-clusterby.html)
	+ [DISTRIBUTE BY Clause](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-qry-select-distribute-by.html)
	+ [GROUP BY Clause](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-qry-select-groupby.html)
	+ [HAVING Clause](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-qry-select-having.html)
	+ [Hints](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-qry-select-hints.html)
	+ [Inline Table](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-qry-select-inline-table.html)
	+ [File](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-qry-select-file.html)
	+ [JOIN](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-qry-select-join.html)
	+ [LIKE Predicate](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-qry-select-like.html)
	+ [LIMIT Clause](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-qry-select-limit.html)
	+ [ORDER BY Clause](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-qry-select-orderby.html)
	+ [Set Operators](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-qry-select-setops.html)
	+ [SORT BY Clause](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-qry-select-sortby.html)
	+ [TABLESAMPLE](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-qry-select-sampling.html)
	+ [Table-valued Function](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-qry-select-tvf.html)
	+ [WHERE Clause](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-qry-select-where.html)
	+ [Window Function](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-qry-select-window.html)
	+ [CASE Clause](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-qry-select-case.html)
	+ [PIVOT Clause](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-qry-select-pivot.html)
	+ [LATERAL VIEW Clause](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-qry-select-lateral-view.html)
	+ [TRANSFORM Clause](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-qry-select-transform.html)

- [EXPLAIN](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-qry-explain.html)

----------------------------------------

- [SELECT Statement]()
	+ [Common Table Expression]()
	+ [CLUSTER BY Clause]()
	+ [DISTRIBUTE BY Clause]()
	+ [GROUP BY Clause]()
	+ [HAVING Clause]()
	+ [Hints]()
	+ [Inline Table]()
	+ [File]()
	+ [JOIN]()
	+ [LIKE Predicate]()
	+ [LIMIT Clause]()
	+ [ORDER BY Clause]()
	+ [Set Operators]()
	+ [SORT BY Clause]()
	+ [TABLESAMPLE]()
	+ [Table-valued Function]()
	+ [WHERE Clause]()
	+ [Window Function]()
	+ [CASE Clause]()
	+ [PIVOT Clause]()
	+ [LATERAL VIEW Clause]()
	+ [TRANSFORM Clause]()

- [EXPLAIN]()

## Auxiliary Statements

- [ADD FILE](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-aux-resource-mgmt-add-file.html)
- [ADD JAR](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-aux-resource-mgmt-add-jar.html)
- [ANALYZE TABLE](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-aux-analyze-table.html)
- [CACHE TABLE](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-aux-cache-cache-table.html)
- [CLEAR CACHE](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-aux-cache-clear-cache.html)
- [DESCRIBE DATABASE](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-aux-describe-database.html)
- [DESCRIBE FUNCTION](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-aux-describe-function.html)
- [DESCRIBE QUERY](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-aux-describe-query.html)
- [DESCRIBE TABLE](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-aux-describe-table.html)
- [LIST FILE](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-aux-resource-mgmt-list-file.html)
- [LIST JAR](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-aux-resource-mgmt-list-jar.html)
- [REFRESH](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-aux-cache-refresh.html)
- [REFRESH TABLE](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-aux-cache-refresh-table.html)
- [REFRESH FUNCTION](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-aux-cache-refresh-function.html)
- [RESET](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-aux-conf-mgmt-reset.html)
- [SET](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-aux-conf-mgmt-set.html)
- [SHOW COLUMNS](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-aux-show-columns.html)
- [SHOW CREATE TABLE](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-aux-show-create-table.html)
- [SHOW DATABASES](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-aux-show-databases.html)
- [SHOW FUNCTIONS](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-aux-show-functions.html)
- [SHOW PARTITIONS](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-aux-show-partitions.html)
- [SHOW TABLE EXTENDED](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-aux-show-table.html)
- [SHOW TABLES](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-aux-show-tables.html)
- [SHOW TBLPROPERTIES](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-aux-show-tblproperties.html)
- [SHOW VIEWS](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-aux-show-views.html)
- [UNCACHE TABLE](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-aux-cache-uncache-table.html)

---------------------------

- [ADD FILE]()
- [ADD JAR]()
- [ANALYZE TABLE]()
- [CACHE TABLE]()
- [CLEAR CACHE]()
- [DESCRIBE DATABASE]()
- [DESCRIBE FUNCTION]()
- [DESCRIBE QUERY]()
- [DESCRIBE TABLE]()
- [LIST FILE]()
- [LIST JAR]()
- [REFRESH]()
- [REFRESH TABLE]()
- [REFRESH FUNCTION]()
- [RESET]()
- [SET]()
- [SHOW COLUMNS]()
- [SHOW CREATE TABLE]()
- [SHOW DATABASES]()
- [SHOW FUNCTIONS]()
- [SHOW PARTITIONS]()
- [SHOW TABLE EXTENDED]()
- [SHOW TABLES]()
- [SHOW TBLPROPERTIES]()
- [SHOW VIEWS]()
- [UNCACHE TABLE]()