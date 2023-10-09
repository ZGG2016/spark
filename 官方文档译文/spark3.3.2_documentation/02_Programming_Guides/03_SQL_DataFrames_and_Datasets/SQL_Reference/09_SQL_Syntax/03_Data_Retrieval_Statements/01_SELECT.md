# SELECT

[TOC]

## Description

> Spark supports a `SELECT` statement and conforms to the ANSI SQL standard. Queries are used to retrieve result sets from one or more tables. The following section describes the overall query syntax and the sub-sections cover different constructs of a query along with examples.

spark 支持 `SELECT` 语句，遵循 ANSI SQL 标准。

查询用来接收从一个或多个表得到的结果集。

### Syntax

	[ WITH with_query [ , ... ] ]
	select_statement [ { UNION | INTERSECT | EXCEPT } [ ALL | DISTINCT ] select_statement, ... ]
    	[ ORDER BY { expression [ ASC | DESC ] [ NULLS { FIRST | LAST } ] [ , ... ] } ]
    	[ SORT BY { expression [ ASC | DESC ] [ NULLS { FIRST | LAST } ] [ , ... ] } ]
    	[ CLUSTER BY { expression [ , ... ] } ]
    	[ DISTRIBUTE BY { expression [, ... ] } ]
    	[ WINDOW { named_window [ , WINDOW named_window, ... ] } ]
    	[ LIMIT { ALL | expression } ]

While `select_statement` is defined as

	SELECT [ hints , ... ] [ ALL | DISTINCT ] { [ [ named_expression | regex_column_names ] [ , ... ] | TRANSFORM (...) ] }
    	FROM { from_item [ , ... ] }
    	[ PIVOT clause ]
    	[ LATERAL VIEW clause ] [ ... ] 
    	[ WHERE boolean_expression ]
    	[ GROUP BY expression [ , ... ] ]
    	[ HAVING boolean_expression ]

### Parameters

- with_query
	
	Specifies the common table expressions (CTEs) before the main query block. These table expressions are allowed to be referenced later in the FROM clause. This is useful to abstract out repeated subquery blocks in the FROM clause and improves readability of the query.

	在主查询块前，指定CTEs. 这些表表达式允许在 FROM 子句之后引用。

	这可以抽象出来 FROM 子句中重复的子查询块，改善查询的可读性。 

- hints

	Hints can be specified to help spark optimizer make better planning decisions. Currently spark supports hints that influence selection of join strategies and repartitioning of the data.

	Hints 用来帮助 spark 优化器做出更好的计划决策。当前支持的 hints 会影响 join 策略的选择和数据的重分区。

- ALL

	Select all matching rows from the relation and is enabled by default.

	选择关系实体的所有匹配的行，默认是启用的。

- DISTINCT

	Select all matching rows from the relation after removing duplicates in results.

	移除结果中的重复行后，选择关系实体的所有匹配的行.

- named_expression

	An expression with an assigned name. In general, it denotes a column expression.
	
	Syntax: `expression [AS] [alias]`

	具有指定名称的表达式。通常，它表示列表达式。

- from_item

	Specifies a source of input for the query. It can be one of the following:

	- Table relation
	- [Join relation](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-qry-select-join.html)
	- [Table-value function](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-qry-select-tvf.html)
	- [Inline table](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-qry-select-inline-table.html)
	- Subquery
	- [File](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-qry-select-file.html)

	指定查询的输入源

- PIVOT

	The `PIVOT` clause is used for data perspective; We can get the aggregated values based on specific column value.

	`PIVOT` 子句用于数据透视。可以基于指定的列值，获得聚合值。	

- LATERAL VIEW

	The `LATERAL VIEW` clause is used in conjunction with generator functions such as `EXPLODE`, which will generate a virtual table containing one or more rows. `LATERAL VIEW` will apply the rows to each original output row.

	`LATERAL VIEW` 子句和生成函数一起使用，例如 `EXPLODE`, 用于生成包含一行或多行的虚拟表。

	`LATERAL VIEW` 将行用于每个原始输出行。

- WHERE

	Filters the result of the `FROM` clause based on the supplied predicates.

	基于指定的谓词，过滤 `FROM` 子句的结果。

- GROUP BY

	Specifies the expressions that are used to group the rows. This is used in conjunction with aggregate functions (MIN, MAX, COUNT, SUM, AVG, etc.) to group rows based on the grouping expressions and aggregate values in each group. When a `FILTER` clause is attached to an aggregate function, only the matching rows are passed to that function.

	指定用于分组行的表达式。这和聚合函数一起使用，基于分组表达式对行进行分组，并在每组中聚合值。

	如果 `FILTER` 子句和聚合函数一起使用，那么仅将匹配的行传给这个聚合函数。

- HAVING

	Specifies the predicates by which the rows produced by `GROUP BY` are filtered. The `HAVING` clause is used to filter rows after the grouping is performed. If `HAVING` is specified without `GROUP BY`, it indicates a `GROUP BY` without grouping expressions (global aggregate).

	指定谓词，用来过滤由 `GROUP BY` 产生的行。

	`HAVING` 子句是在分组执行完成后，再过滤行。如果在使用 `HAVING` 子句没有 `GROUP BY` 子句，就表示没有分组表达式的 `GROUP BY`(全局聚合)。

- ORDER BY

	Specifies an ordering of the rows of the complete result set of the query. The output rows are ordered across the partitions. This parameter is mutually exclusive with `SORT BY`, `CLUSTER BY` and `DISTRIBUTE BY` and can not be specified together.

	指定查询结果的行的顺序。输出行在分区间排序。

	这个参数不能和 `SORT BY`, `CLUSTER BY` and `DISTRIBUTE BY` 一起使用。

- SORT BY

	Specifies an ordering by which the rows are ordered within each partition. This parameter is mutually exclusive with `ORDER BY` and `CLUSTER BY` and can not be specified together.

	指定每个分区中的行的顺序。

	这个参数不能和 `ORDER BY`, `CLUSTER BY` 一起使用。

- CLUSTER BY

	Specifies a set of expressions that is used to repartition and sort the rows. Using this clause has the same effect of using `DISTRIBUTE BY` and `SORT BY` together.

	指定用来重分区和排序行的表达式。

	作用等价于 `DISTRIBUTE BY` and `SORT BY` 一起使用。

- DISTRIBUTE BY

	Specifies a set of expressions by which the result rows are repartitioned. This parameter is mutually exclusive with `ORDER BY` and `CLUSTER BY` and can not be specified together.

	指定对结果行进行重分区的表达式。

	这个参数不能和 `ORDER BY`, `CLUSTER BY` 一起使用。

- LIMIT

	Specifies the maximum number of rows that can be returned by a statement or subquery. This clause is mostly used in the conjunction with `ORDER BY` to produce a deterministic result.

	指定一个语句或子查询返回的行的最大数量。

	这个子句主要和 `ORDER BY` 一起使用。

- boolean_expression

	Specifies any expression that evaluates to a result type boolean. Two or more expressions may be combined together using the logical operators ( AND, OR ).

	指定任意能计算出布尔结果类型的表达式。

	两个或更多表达式可以通过逻辑操作符一起使用。

- expression

	Specifies a combination of one or more values, operators, and SQL functions that evaluates to a value.

	指定一个或多个值、操作符、sql函数的组合。

- named_window

	Specifies aliases for one or more source window specifications. The source window specifications can be referenced in the widow definitions in the query.

	为一个或多个源窗口描述指定别名。源窗口描述可以在查询的窗口定义中引用。

- regex_column_names

	When `spark.sql.parser.quotedRegexColumnNames` is true, quoted identifiers (using backticks) in `SELECT` statement are interpreted as regular expressions and `SELECT` statement can take regex-based column specification. For example, below SQL will only take column c:
	
	当 `spark.sql.parser.quotedRegexColumnNames` 是 true 时，`SELECT` 语句中的引号标识符（使用反引号）被解释成常规表达式，`SELECT` 语句可以接收基于正则的列描述。

	```sql
   	SELECT `(a|b)?+.+` FROM (
     	SELECT 1 as a, 2 as b, 3 as c
   	)
   	```

- TRANSFORM

	Specifies a hive-style transform query specification to transform the input by forking and running user-specified command or script.

	指定 hive 风格转换查询描述，用来通过 forking 并运行用户指定的命令或脚本来转换输入。