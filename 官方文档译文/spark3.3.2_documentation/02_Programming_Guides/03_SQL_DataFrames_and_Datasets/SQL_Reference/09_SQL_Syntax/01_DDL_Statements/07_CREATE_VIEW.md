# CREATE VIEW

[TOC]

## Description

> Views are based on the result-set of an SQL query. `CREATE VIEW` constructs a virtual table that has no physical data therefore other operations like `ALTER VIEW` and `DROP VIEW` only change metadata.

视图可以基于一个 SQL查询的结果集创建。

`CREATE VIEW` 创建一个虚拟表，没有物理数据，因此像 `ALTER VIEW` and `DROP VIEW` 的其他操作仅改变元数据。

### Syntax

	CREATE [ OR REPLACE ] [ [ GLOBAL ] TEMPORARY ] VIEW [ IF NOT EXISTS ] view_identifier
    	create_view_clauses AS query

### Parameters

- OR REPLACE

	If a view of same name already exists, it will be replaced.

- [ GLOBAL ] TEMPORARY

	TEMPORARY views are session-scoped and will be dropped when session ends because it skips persisting the definition in the underlying metastore, if any. GLOBAL TEMPORARY views are tied to a system preserved temporary database `global_temp`.

- IF NOT EXISTS

	Creates a view if it does not exist.

- view_identifier

	Specifies a view name, which may be optionally qualified with a database name.
	
	Syntax: `[ database_name. ] view_name`

- create_view_clauses

	These clauses are optional and order insensitive. It can be of following formats.

	- `[ ( column_name [ COMMENT column_comment ], ... ) ]` to specify column-level comments.
	- `[ COMMENT view_comment ]` to specify view-level comments.
	- `[ TBLPROPERTIES ( property_name = property_value [ , ... ] ) ]` to add metadata key-value pairs.

- query 

	A [SELECT](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-qry-select.html) statement that constructs the view from base tables or other views.

## Examples

```sql
-- Create or replace view for `experienced_employee` with comments.
CREATE OR REPLACE VIEW experienced_employee
    (ID COMMENT 'Unique identification number', Name) 
    COMMENT 'View for experienced employees'
    AS SELECT id, name FROM all_employee
        WHERE working_years > 5;

-- Create a global temporary view `subscribed_movies` if it does not exist.
CREATE GLOBAL TEMPORARY VIEW IF NOT EXISTS subscribed_movies 
    AS SELECT mo.member_id, mb.full_name, mo.movie_title
        FROM movies AS mo INNER JOIN members AS mb 
        ON mo.member_id = mb.id;
```