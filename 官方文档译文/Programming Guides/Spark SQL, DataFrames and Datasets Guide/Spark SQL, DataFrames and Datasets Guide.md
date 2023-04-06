# Spark SQL, DataFrames and Datasets Guide

[TOC]

> Spark SQL is a Spark module for structured data processing. Unlike the basic Spark RDD API, the interfaces provided by Spark SQL provide Spark with more information about the structure of both the data and the computation being performed. Internally, Spark SQL uses this extra information to perform extra optimizations. There are several ways to interact with Spark SQL including SQL and the Dataset API. When computing a result, the same execution engine is used, independent of which API/language you are using to express the computation. This unification means that developers can easily switch back and forth between different APIs based on which provides the most natural way to express a given transformation.

Spark SQL 是**处理结构化数据**的模块。和基本的 Spark RDD API 不同，Spark SQL 为 Spark 提供了关于数据和执行计算的更多结构化信息。在内部，Spark SQL 使用这个额外的信息去执行额外的优化。 可以使用 SQL 和 Dataset API 跟 Spark SQL 交互。无论你使用哪种 API/语言，都会使用同一个引擎执行计算。这就意味着开发者可以在不同 APIs 间来回切换。

> All of the examples on this page use sample data included in the Spark distribution and can be run in the spark-shell, pyspark shell, or sparkR shell.

本页所有的样例数据都在 Spark 发行版里。可以使用 spark-shell、pyspark shell、sparkR shell 来运行。


## 1、SQL

> One use of Spark SQL is to execute SQL queries. Spark SQL can also be used to read data from an existing Hive installation. For more on how to configure this feature, please refer to the [Hive Tables](https://spark.apache.org/docs/3.0.1/sql-data-sources-hive-tables.html) section. When running SQL from within another programming language the results will be returned as a [Dataset/DataFrame](https://spark.apache.org/docs/3.0.1/sql-programming-guide.html#datasets-and-dataframes). You can also interact with the SQL interface using the [command-line](https://spark.apache.org/docs/3.0.1/sql-distributed-sql-engine.html#running-the-spark-sql-cli) or over [JDBC/ODBC](https://spark.apache.org/docs/3.0.1/sql-distributed-sql-engine.html#running-the-thrift-jdbcodbc-server).

Spark SQL 的作用之一就是**执行 SQL 查询**。也可以**从 Hive 中读取数据**。更详细信息参见 Hive Tables 。当在其他编程语言运行 SQL 时，会返回一个 Dataset/DataFrame 。你也可以使用命令行 或通过 JDBC/ODBC 来和 SQL 接口交互。

## 2、Datasets and DataFrames

> A Dataset is a distributed collection of data. Dataset is a new interface added in Spark 1.6 that provides the benefits of RDDs (strong typing, ability to use powerful lambda functions) with the benefits of Spark SQL’s optimized execution engine. A Dataset can be [constructed](https://spark.apache.org/docs/3.0.1/sql-getting-started.html#creating-datasets) from JVM objects and then manipulated using functional transformations (map, flatMap, filter, etc.). The Dataset API is available in [Scala](https://spark.apache.org/docs/3.0.1/api/scala/org/apache/spark/sql/Dataset.html) and [Java](https://spark.apache.org/docs/3.0.1/api/java/index.html?org/apache/spark/sql/Dataset.html). Python does not have the support for the Dataset API. But due to Python’s dynamic nature, many of the benefits of the Dataset API are already available (i.e. you can access the field of a row by name naturally row.columnName). The case for R is similar.

**Dataset 是一个分布式的数据集合**。 Dataset 是在 Spark 1.6 添加的新的接口，同时具备 RDDs 的优点(强类型化、lambda 函数) 和 Spark SQL 优化了的执行引擎的优点。 Dataset 可以从 JVM 对象中构造，然后可以使用 transformations 函数(map, flatMap, filter, etc.)。Dataset API在 Scala 和 Java 中均可用，Python 不支持。但是由于 Python 的动态特性，Dataset API 的优点早已具备。(如，通过 row.columnName 来访问一行的字段)。 R 也具有相似情况。

> A DataFrame is a Dataset organized into named columns. It is conceptually equivalent to a table in a relational database or a data frame in R/Python, but with richer optimizations under the hood. DataFrames can be constructed from a wide array of [sources](https://spark.apache.org/docs/3.0.1/sql-data-sources.html) such as: structured data files, tables in Hive, external databases, or existing RDDs. The DataFrame API is available in Scala, Java, [Python](https://spark.apache.org/docs/3.0.1/api/python/pyspark.sql.html#pyspark.sql.DataFrame), and [R](https://spark.apache.org/docs/3.0.1/api/R/index.html). In Scala and Java, a DataFrame is represented by a Dataset of Rows. In the [Scala API](https://spark.apache.org/docs/3.0.1/api/scala/org/apache/spark/sql/Dataset.html), DataFrame is simply a type alias of Dataset[Row]. While, in [Java API](https://spark.apache.org/docs/3.0.1/api/java/index.html?org/apache/spark/sql/Dataset.html), users need to use Dataset<Row> to represent a DataFrame.

**DataFrame 是组织成指定列的 Dataset**。和关系数据库中的表、R/Python 中的 data frame 的概念相似，但底层进行了优化。 **DataFrames 可以从各种各样的数据源中构造，如结构化数据文件、Hive 表、外部数据库、已存在的 RDDs**。Dataset API在 Scala 、Java 、Python 、R 中均可用。 在 Scala 和 Java 中，**a DataFrame is represented by a Dataset of Rows.** 在 Scala API 中，DataFrame 仅仅是一个 Dataset[Row]类型的别名。在 Java API 中，可以使用 Dataset<Row> 表示一个 DataFrame。

> Throughout this document, we will often refer to Scala/Java Datasets of Rows as DataFrames.

在此文档中，我们将常常会引用 Scala/Java Datasets 的 Rows 作为 DataFrames。


```java
/**
 * vi people.json
 *
 * {"name":"Michael"}
 * {"name":"Andy", "age":30}
 * {"name":"Justin", "age":19}
 *
 */

// 创建 DataFrame
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

// a DataFrame is represented by a Dataset of Rows 
Dataset<Row> df = spark.read().json("examples/src/main/resources/people.json");

// Displays the content of the DataFrame to stdout
df.show(); // DataFrame 是组织成指定列的 Dataset
// +----+-------+
// | age|   name|
// +----+-------+
// |null|Michael|
// |  30|   Andy|
// |  19| Justin|
// +----+-------+
```
