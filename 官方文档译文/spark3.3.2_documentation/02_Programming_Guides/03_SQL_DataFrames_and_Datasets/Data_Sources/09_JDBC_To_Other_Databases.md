# JDBC To Other Databases

> Spark SQL also includes a data source that can read data from other databases using JDBC. This functionality should be preferred over using [JdbcRDD](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/rdd/JdbcRDD.html). This is because the results are returned as a DataFrame and they can easily be processed in Spark SQL or joined with other data sources. The JDBC data source is also easier to use from Java or Python as it does not require the user to provide a ClassTag. (Note that this is different than the Spark SQL JDBC server, which allows other applications to run queries using Spark SQL).

Spark SQL 还包含一个使用 JDBC 从其他数据库读取数据的数据源。这个功能更倾向使用的是 JdbcRDD, 因为返回的是 DataFrame, 可以很容易地使用 Spark SQL 处理，或者和其他数据源 join.

JDBC 数据源也可以在 Java\Python 里使用，不需要用户提供 ClassTag. 这和 Spark SQL JDBC server 不同, JDBC 数据源允许其他程序使用 Spark SQL 执行查询。

> To get started you will need to include the JDBC driver for your particular database on the spark classpath. For example, to connect to postgres from the Spark Shell you would run the following command:

首先需要将你的数据的 JDBC driver 添加到你的 spark 类路径中。例如，从 Spark shell 连接 postgres, 需要执行如下命令：

```
./bin/spark-shell --driver-class-path postgresql-9.4.1207.jar --jars postgresql-9.4.1207.jar
```

## Data Source Option

> Spark supports the following case-insensitive options for JDBC. The Data source options of JDBC can be set via:

spark 支持如下大小写不敏感的选项。

- the `.option/.options` methods of
	- DataFrameReader
	- DataFrameWriter

- OPTIONS clause at [`CREATE TABLE USING DATA_SOURCE`](https://spark.apache.org/docs/3.3.2/sql-ref-syntax-ddl-create-table-datasource.html)

> For connection properties, users can specify the JDBC connection properties in the data source options. `user` and `password` are normally provided as connection properties for logging into the data sources.

用户可以在数据源选项中指定 JDBC 连接属性。

用户账号和密码通常作为连接属性提供，为登入数据源。

Property Name | Default | Meaning | Since Version
---|:---|:---|:---
`url` | (none) | The JDBC URL of the form `jdbc:subprotocol:subname` to connect to. The source-specific connection properties may be specified in the URL. e.g., `jdbc:postgresql://localhost/test?user=fred&password=secret` 【JDBC url.可以在这个url中指定连接属性】| read/write
`dbtable` | (none) | The JDBC table that should be read from or written into. Note that when using it in the read path anything that is valid in a `FROM` clause of a SQL query can be used. For example, instead of a full table you could also use a subquery in parentheses. It is not allowed to specify `dbtable` and `query` options at the same time. 【读写数据的jdbc表。当在读取路径中使用它时，sql查询的from子句中任意有效的内容都被使用。例如不用使用完整的表，而是使用一个子查询。不能同时使用`dbtable` 和`query`属性】| read/write
`query`	| (none) | A query that will be used to read data into Spark. The specified query will be parenthesized and used as a subquery in the `FROM` clause. Spark will also assign an alias to the subquery clause. As an example, spark will issue a query of the following form to the JDBC Source. `SELECT <columns> FROM (<user_specified_query>) spark_gen_alias`【spark中用来读取数据的查询。指定的查询将被括起来，作为子查询用在from子句中。也会给它分配一个别名】<br/> Below are a couple of restrictions while using this option.【下面是使用这个选项的限制】<br/> 1.It is not allowed to specify `dbtable` and `query` options at the same time.【1.不允许同时使用`dbtable` 和`query`属性】<br/> 2.It is not allowed to specify `query` and `partitionColumn` options at the same time. When specifying partitionColumn option is required, the subquery can be specified using `dbtable` option instead and partition columns can be qualified using the subquery alias provided as part of `dbtable`.【2.不允许同时指定`query`和`partitionColumn`选项。子查询可以使用`dbtable`选项指定，可以使用子查询的别名来限定分区列】<br/>Example: `spark.read.format("jdbc").option("url", jdbcUrl).option("query", "select c1, c2 from t1").load()` | read/write
`driver` | (none) | The class name of the JDBC driver to use to connect to this URL. 【driver类名】| read/write
`partitionColumn`, `lowerBound`, `upperBound` | (none) | These options must all be specified if any of them is specified. In addition, `numPartitions` must be specified. They describe how to partition the table when reading in parallel from multiple workers. `partitionColumn` must be a numeric, date, or timestamp column from the table in question. Notice that `lowerBound` and `upperBound` are just used to decide the partition stride, not for filtering the rows in table. So all rows in the table will be partitioned and returned. This option applies only to reading.【如果指定了其中一个选项，那么其他选项都要指定。另外`numPartitions`必须指定。这些选项描述了从多个workers并行读取数据时，如何分区表。`partitionColumn`必须是数值、日期或时间戳列。`lowerBound`和`upperBound`决定了partition stride，并不是用来过滤行。所以表中的所有行将被分区、返回。这个选项仅用于读数据。】| read	
`numPartitions`	| (none) | The maximum number of partitions that can be used for parallelism in table reading and writing. This also determines the maximum number of concurrent JDBC connections. If the number of partitions to write exceeds this limit, we decrease it to this limit by calling coalesce(`numPartitions`) before writing. 【分区的最大数量。决定了并发jdbc连接数量。如果超过了数量，就调用coalesce(`numPartitions`)减少分区数】| read/write
`queryTimeout` | 0 | The number of seconds the driver will wait for a Statement object to execute to the given number of seconds. Zero means there is no limit. In the write path, this option depends on how JDBC drivers implement the API `setQueryTimeout`, e.g., the h2 JDBC driver checks the timeout of each query instead of an entire JDBC batch.【driver等待Statement对象执行的秒数。0表示没有限制。在读路径中，这个选项jdbc drvier如何执行`setQueryTimeout`】 | read/write
`fetchsize`	| 0 | The JDBC fetch size, which determines how many rows to fetch per round trip. This can help performance on JDBC drivers which default to low fetch size (e.g. Oracle with 10 rows).【获取大小，决定了每次轮询获取多少行。】 | read
`batchsize`	| 1000 | The JDBC batch size, which determines how many rows to insert per round trip. This can help performance on JDBC drivers. This option applies only to writing.【决定了每次轮询插入多少行】 | write
`isolationLevel` | `READ_UNCOMMITTED` | The transaction isolation level, which applies to current connection. It can be one of `NONE`, `READ_COMMITTED`, `READ_UNCOMMITTED`, `REPEATABLE_READ`, or `SERIALIZABLE`, corresponding to standard transaction isolation levels defined by JDBC's Connection object, with default of `READ_UNCOMMITTED`. Please refer the documentation in `java.sql.Connection`.	【事务隔离级别，应用当前连接。】|write
`sessionInitStatement` | (none)	| After each database session is opened to the remote DB and before starting to read data, this option executes a custom SQL statement (or a PL/SQL block). Use this to implement session initialization code. Example: `option("sessionInitStatement", """BEGIN execute immediate 'alter session set "_serial_direct_read"=true'; END;""")` 【在每次打开远程db的数据库会话之后，开始读取数据之前，这个选项执行一个个性化sql语句（或pl/sql块）。使用这个来执行会话初始化】| read
`truncate`	| `false` | This is a JDBC writer related option. When `SaveMode.Overwrite` is enabled, this option causes Spark to truncate an existing table instead of dropping and recreating it. This can be more efficient, and prevents the table metadata (e.g., indices) from being removed. However, it will not work in some cases, such as when the new data has a different schema. In case of failures, users should turn off `truncate` option to use `DROP TABLE` again. Also, due to the different behavior of `TRUNCATE TABLE` among DBMS, it's not always safe to use this. MySQLDialect, DB2Dialect, MsSqlServerDialect, DerbyDialect, and OracleDialect supports this while PostgresDialect and default JDBCDirect doesn't. For unknown and unsupported JDBCDirect, the user option `truncate` is ignored.【jdbc写入器相关的选项。当启用`SaveMode.Overwrite`，这个选项会让spark清空已存在的表，而不是删除或重建它。这就更高效，保护了表元数据不被删除。然而在某些情况下并不起作用，例如当新数据具有不同的结构时。当失败时，用户应该关闭这个选项，使用DROP TABLE。由于在不同关系数据库中`TRUNCATE TABLE`存在不同的行为，使用它并不总是安全的。】 | write
`cascadeTruncate` | the default cascading truncate behaviour of the JDBC database in question, specified in the `isCascadeTruncate` in each JDBCDialect | This is a JDBC writer related option. If enabled and supported by the JDBC database (PostgreSQL and Oracle at the moment), this options allows execution of a `TRUNCATE TABLE t CASCADE` (in the case of PostgreSQL a `TRUNCATE TABLE ONLY t CASCADE` is executed to prevent inadvertently truncating descendant tables). This will affect other tables, and thus should be used with care.【jdbc写入器相关的选项。如果启用它，且jdbc数据库也支持，那么这个选项允许执行`TRUNCATE TABLE t CASCADE`】 | write
`createTableOptions` | | This is a JDBC writer related option. If specified, this option allows setting of database-specific table and partition options when creating a table (e.g., `CREATE TABLE t (name string) ENGINE=InnoDB`.).【jdbc写入器相关的选项。如果指定，此选项允许在创建表时设置特定于数据库的表和分区选项】 | write
`createTableColumnTypes` | (none) | The database column data types to use instead of the defaults, when creating the table. Data type information should be specified in the same format as CREATE TABLE columns syntax (e.g: `"name CHAR(64), comments VARCHAR(1024)"`). The specified types should be valid spark sql data types. 【当建表时，指定数据库列数据类型，而不是使用默认的。指定的数据类型信息的格式应该和使用CREATE TABLE语句创建时一致。类型也要是有效的spark sql数据类型】 |	write
`customSchema`|(none) | The custom schema to use for reading data from JDBC connectors. For example, `"id DECIMAL(38, 0), name STRING"`. You can also specify partial fields, and the others use the default type mapping. For example, `"id DECIMAL(38, 0)"`. The column names should be identical to the corresponding column names of JDBC table. Users can specify the corresponding data types of Spark SQL instead of using the defaults.	【从jdbc连接器读取数据时，使用的自定义的结构。你可以指定部分字段，其他的使用默认类型。列名应该和jdbc表中的列名相同。用户可以指定对应的spark sql数据类型，而不是使用默认类型】| read
`pushDownPredicate`	| `true` | The option to enable or disable predicate push-down into the JDBC data source. The default value is true, in which case Spark will push down filters to the JDBC data source as much as possible. Otherwise, if set to false, no filter will be pushed down to the JDBC data source and thus all filters will be handled by Spark. Predicate push-down is usually turned off when the predicate filtering is performed faster by Spark than by the JDBC data source.	【启用或禁用jdbc数据源的谓词下推。默认是true，为了能让spark尽量下推谓词到jdbc数据源。如果设为false，就没有谓词下推到jdbc数据源，因此所有的过滤器都被spark处理。如果spark执行谓词过滤的效率比jdbc数据源高，那么就关闭谓词下推】| read
`pushDownAggregate`	| `false` | The option to enable or disable aggregate push-down in V2 JDBC data source. The default value is false, in which case Spark will not push down aggregates to the JDBC data source. Otherwise, if sets to true, aggregates will be pushed down to the JDBC data source. Aggregate push-down is usually turned off when the aggregate is performed faster by Spark than by the JDBC data source. Please note that aggregates can be pushed down if and only if all the aggregate functions and the related filters can be pushed down. If `numPartitions` equals to 1 or the group by key is the same as `partitionColumn`, Spark will push down aggregate to data source completely and not apply a final aggregate over the data source output. Otherwise, Spark will apply a final aggregate over the data source output. 【在V2 JDBC数据源中，启用或禁用聚合下推。默认是false,spark不会将聚合下推到jdbc数据源。否则如果设为true，聚合被下推到jdbc数据源。如果spark执行聚合的效率比jdbc数据源高，那么就关闭聚合下推。注意，仅在所有的聚合函数和相关过滤器能被下推时，聚合才能被下推。如果numPartitions等于1，或分组key和partitionColumn相同，spark完全地将聚合下推到数据源，并不会在数据源输出上执行最终的聚合。否则spark将在数据源输出上执行最终的聚合】| read
`pushDownLimit`	| `false`| The option to enable or disable LIMIT push-down into V2 JDBC data source. The LIMIT push-down also includes LIMIT + SORT , a.k.a. the Top N operator. The default value is false, in which case Spark does not push down LIMIT or LIMIT with SORT to the JDBC data source. Otherwise, if sets to true, LIMIT or LIMIT with SORT is pushed down to the JDBC data source. If `numPartitions` is greater than 1, SPARK still applies LIMIT or LIMIT with SORT on the result from data source even if LIMIT or LIMIT with SORT is pushed down. Otherwise, if LIMIT or LIMIT with SORT is pushed down and `numPartitions` equals to 1, SPARK will not apply LIMIT or LIMIT with SORT on the result from data source. 【在V2 JDBC数据源中，启用或禁用LIMIT下推。LIMIT下推也包括了LIMIT + SORT,也就是top n操作。默认值是false,spark不会将LIMIT或LIMIT + SORT下推到JDBC数据源。如果设为true,LIMIT或LIMIT + SORT下推到JDBC数据源。如果numPartitions大于1，spark仍会将LIMIT或LIMIT + SORT应用到数据源结果上，即使下推了LIMIT或LIMIT + SORT。否则如果LIMIT或LIMIT + SORT被下推，并且numPartitions等于1，spark不会将LIMIT或LIMIT + SORT应用到数据源结果上】 | read
`pushDownTableSample` | `false`	| The option to enable or disable TABLESAMPLE push-down into V2 JDBC data source. The default value is false, in which case Spark does not push down TABLESAMPLE to the JDBC data source. Otherwise, if value sets to true, TABLESAMPLE is pushed down to the JDBC data source.	【在V2 JDBC数据源中，启用或禁用TABLESAMPLE下推。默认值是false,spark不会将TABLESAMPLE下推到JDBC数据源。如果设为true,TABLESAMPLE下推到JDBC数据源。】| read
`keytab` | (none) | Location of the kerberos keytab file (which must be pre-uploaded to all nodes either by `--files` option of spark-submit or manually) for the JDBC client. When path information found then Spark considers the keytab distributed manually, otherwise `--files` assumed. If both keytab and principal are defined then Spark tries to do kerberos authentication.	| read/write
`principal`	| (none) | Specifies kerberos principal name for the JDBC client. If both `keytab` and `principal` are defined then Spark tries to do kerberos authentication.	| read/write
`refreshKrb5Config`	| `false` | This option controls whether the kerberos configuration is to be refreshed or not for the JDBC client before establishing a new connection. Set to true if you want to refresh the configuration, otherwise set to false. The default value is false. Note that if you set this option to true and try to establish multiple connections, a race condition can occur. One possble situation would be like as follows. <br/>1.refreshKrb5Config flag is set with security context 1 <br/>2.A JDBC connection provider is used for the corresponding DBMS <br/>3.The krb5.conf is modified but the JVM not yet realized that it must be reloaded <br/>4.Spark authenticates successfully for security context 1 <br/>5.The JVM loads security context 2 from the modified krb5.conf <br/>6.Spark restores the previously saved security context 1 <br/>7.The modified krb5.conf content just gone | read/write
`connectionProvider` | (none)	| The name of the JDBC connection provider to use to connect to this URL, e.g. db2, mssql. Must be one of the providers loaded with the JDBC data source. Used to disambiguate when more than one provider can handle the specified driver and options. The selected provider must not be disabled by `spark.sql.sources.disabledJdbcConnProviderList`.	| read/write

> Note that kerberos authentication with keytab is not always supported by the JDBC driver.
Before using keytab and principal configuration options, please make sure the following requirements are met:

> The included JDBC driver version supports kerberos authentication with keytab.
> There is a built-in connection provider which supports the used database.

JDBC 驱动并不总是支持使用 keytab 的 kerberos 认证。在使用 keytab 和主要配置选项前，确保满足下列要求：

- JDBC 驱动版本支持使用 keytab 的 kerberos 认证。
- 内建连接提供者支持使用的数据库。

> There is a built-in connection providers for the following databases:

以下数据库有一个内建连接提供者：

- DB2
- MariaDB
- MS Sql
- Oracle
- PostgreSQL

> If the requirements are not met, please consider using the `JdbcConnectionProvider` developer API to handle custom authentication.

如果不满足要求，请考虑使用 `JdbcConnectionProvider` 开发者 API, 来处理自定义认证。

```scala
// Note: JDBC loading and saving can be achieved via either the load/save or jdbc methods
// Loading data from a JDBC source
val jdbcDF = spark.read
  .format("jdbc")
  .option("url", "jdbc:postgresql:dbserver")
  .option("dbtable", "schema.tablename")
  .option("user", "username")
  .option("password", "password")
  .load()

val connectionProperties = new Properties()
connectionProperties.put("user", "username")
connectionProperties.put("password", "password")
val jdbcDF2 = spark.read
  .jdbc("jdbc:postgresql:dbserver", "schema.tablename", connectionProperties)
// Specifying the custom data types of the read schema
connectionProperties.put("customSchema", "id DECIMAL(38, 0), name STRING")
val jdbcDF3 = spark.read
  .jdbc("jdbc:postgresql:dbserver", "schema.tablename", connectionProperties)

// Saving data to a JDBC source
jdbcDF.write
  .format("jdbc")
  .option("url", "jdbc:postgresql:dbserver")
  .option("dbtable", "schema.tablename")
  .option("user", "username")
  .option("password", "password")
  .save()

jdbcDF2.write
  .jdbc("jdbc:postgresql:dbserver", "schema.tablename", connectionProperties)

// Specifying create table column data types on write
jdbcDF.write
  .option("createTableColumnTypes", "name CHAR(64), comments VARCHAR(1024)")
  .jdbc("jdbc:postgresql:dbserver", "schema.tablename", connectionProperties)
```
> Find full example code at "examples/src/main/scala/org/apache/spark/examples/sql/SQLDataSourceExample.scala" in the Spark repo.

```java
// Note: JDBC loading and saving can be achieved via either the load/save or jdbc methods
// Loading data from a JDBC source
Dataset<Row> jdbcDF = spark.read()
  .format("jdbc")
  .option("url", "jdbc:postgresql:dbserver")
  .option("dbtable", "schema.tablename")
  .option("user", "username")
  .option("password", "password")
  .load();

Properties connectionProperties = new Properties();
connectionProperties.put("user", "username");
connectionProperties.put("password", "password");
Dataset<Row> jdbcDF2 = spark.read()
  .jdbc("jdbc:postgresql:dbserver", "schema.tablename", connectionProperties);

// Saving data to a JDBC source
jdbcDF.write()
  .format("jdbc")
  .option("url", "jdbc:postgresql:dbserver")
  .option("dbtable", "schema.tablename")
  .option("user", "username")
  .option("password", "password")
  .save();

jdbcDF2.write()
  .jdbc("jdbc:postgresql:dbserver", "schema.tablename", connectionProperties);

// Specifying create table column data types on write
jdbcDF.write()
  .option("createTableColumnTypes", "name CHAR(64), comments VARCHAR(1024)")
  .jdbc("jdbc:postgresql:dbserver", "schema.tablename", connectionProperties);
```
> Find full example code at "examples/src/main/java/org/apache/spark/examples/sql/JavaSQLDataSourceExample.java" in the Spark repo.

```python
# Note: JDBC loading and saving can be achieved via either the load/save or jdbc methods
# Loading data from a JDBC source
jdbcDF = spark.read \
    .format("jdbc") \
    .option("url", "jdbc:postgresql:dbserver") \
    .option("dbtable", "schema.tablename") \
    .option("user", "username") \
    .option("password", "password") \
    .load()

jdbcDF2 = spark.read \
    .jdbc("jdbc:postgresql:dbserver", "schema.tablename",
          properties={"user": "username", "password": "password"})

# Specifying dataframe column data types on read
jdbcDF3 = spark.read \
    .format("jdbc") \
    .option("url", "jdbc:postgresql:dbserver") \
    .option("dbtable", "schema.tablename") \
    .option("user", "username") \
    .option("password", "password") \
    .option("customSchema", "id DECIMAL(38, 0), name STRING") \
    .load()

# Saving data to a JDBC source
jdbcDF.write \
    .format("jdbc") \
    .option("url", "jdbc:postgresql:dbserver") \
    .option("dbtable", "schema.tablename") \
    .option("user", "username") \
    .option("password", "password") \
    .save()

jdbcDF2.write \
    .jdbc("jdbc:postgresql:dbserver", "schema.tablename",
          properties={"user": "username", "password": "password"})

# Specifying create table column data types on write
jdbcDF.write \
    .option("createTableColumnTypes", "name CHAR(64), comments VARCHAR(1024)") \
    .jdbc("jdbc:postgresql:dbserver", "schema.tablename",
          properties={"user": "username", "password": "password"})
```
> Find full example code at "examples/src/main/python/sql/datasource.py" in the Spark repo.

```r
# Loading data from a JDBC source
df <- read.jdbc("jdbc:postgresql:dbserver", "schema.tablename", user = "username", password = "password")

# Saving data to a JDBC source
write.jdbc(df, "jdbc:postgresql:dbserver", "schema.tablename", user = "username", password = "password")
```
> Find full example code at "examples/src/main/r/RSparkSQLExample.R" in the Spark repo.


```sql
CREATE TEMPORARY VIEW jdbcTable
USING org.apache.spark.sql.jdbc
OPTIONS (
  url "jdbc:postgresql:dbserver",
  dbtable "schema.tablename",
  user 'username',
  password 'password'
)

INSERT INTO TABLE jdbcTable
SELECT * FROM resultTable
```