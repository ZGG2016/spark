# 02_Getting_Started

[TOC]

## Starting Point: SparkSession {.tabset}

### Scala

> The entry point into all functionality in Spark is the [SparkSession](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/sql/SparkSession.html) class. To create a basic SparkSession, just use `SparkSession.builder()`:

Spark 功能的入口点就是 SparkSession 类。为了创建一个 SparkSession，只需要使用 `SparkSession.builder()`

```scala
import org.apache.spark.sql.SparkSession

val spark = SparkSession
  .build()
  .appName("Spark SQL basic example")
  .config("spark.some.config.option","some-value")
  .getOrCreate()
```

`Find full example code at "examples/src/main/scala/org/apache/spark/examples/sql/SparkSQLExample.scala" in the Spark repo.`

### Java

> The entry point into all functionality in Spark is the [SparkSession](https://spark.apache.org/docs/3.3.2/api/java/index.html#org.apache.spark.sql.SparkSession) class. To create a basic SparkSession, just use `SparkSession.builder()`:

```java
import org.apache.spark.sql.SparkSession;

SparkSession spark = SparkSession
  .builder()
  .appName("Java Spark SQL basic example")
  .config("spark.some.config.option", "some-value")
  .getOrCreate();
```

`Find full example code at "examples/src/main/java/org/apache/spark/examples/sql/JavaSparkSQLExample.java" in the Spark repo.`

### Python

> The entry point into all functionality in Spark is the [SparkSession](https://spark.apache.org/docs/3.3.2/api/python/reference/pyspark.sql/api/pyspark.sql.SparkSession.html) class. To create a basic SparkSession, just use `SparkSession.builder`:

```python
from pyspark.sql import SparkSession

spark = SparkSession \
    .builder \
    .appName("Python Spark SQL basic example") \
    .config("spark.some.config.option", "some-value") \
    .getOrCreate()
```

`Find full example code at "examples/src/main/python/sql/basic.py" in the Spark repo.`

### R

> The entry point into all functionality in Spark is the [SparkSession](https://spark.apache.org/docs/3.3.2/api/R/reference/sparkR.session.html) class. To initialize a basic SparkSession, just call `sparkR.session()`:

```r
sparkR.session(appName = "R Spark SQL basic example", sparkConfig = list(spark.some.config.option = "some-value"))
```

`Find full example code at "examples/src/main/r/RSparkSQLExample.R" in the Spark repo.`

> Note that when invoked for the first time, `sparkR.session()` initializes a global SparkSession singleton instance, and always returns a reference to this instance for successive invocations. In this way, users only need to initialize the SparkSession once, then SparkR functions like read.df will be able to access this global instance implicitly, and users don’t need to pass the SparkSession instance around.

> SparkSession in Spark 2.0 provides builtin support for Hive features including the ability to write queries using HiveQL, access to Hive UDFs, and the ability to read data from Hive tables. To use these features, you do not need to have an existing Hive setup.

Spark 2.0 的 SparkSession 提供了对 Hive 特性的内建支持，包括使用 HiveQL 的写查询、访问 Hive UDFs 、从 Hive 表中读数据的能力。 使用这些特性的话，需要先安装 Hive。

## Creating DataFrames

### Scala

> With a SparkSession, applications can create DataFrames from an [existing RDD](https://spark.apache.org/docs/3.3.2/sql-getting-started.html#interoperating-with-rdds), from a Hive table, or from [Spark data sources](https://spark.apache.org/docs/3.3.2/sql-data-sources.html).

创建 SparkSession 后，可以从已存在的 RDD、Hive 表、Spark 数据源中创建 DataFrames。

> As an example, the following creates a DataFrame based on the content of a JSON file:

下例是从 JSON 文件中创建：

```scala
val df = SparkSession.read.json("examples/src/main/resources/people.json")

// Displays the content of the DataFrame to stdout
df.show()
// +----+-------+
// | age|   name|
// +----+-------+
// |null|Michael|
// |  30|   Andy|
// |  19| Justin|
// +----+-------+
```

`Find full example code at "examples/src/main/scala/org/apache/spark/examples/sql/SparkSQLExample.scala" in the Spark repo.`

### Java

> With a SparkSession, applications can create DataFrames from an [existing RDD](https://spark.apache.org/docs/3.3.2/sql-getting-started.html#interoperating-with-rdds), from a Hive table, or from [Spark data sources](https://spark.apache.org/docs/3.3.2/sql-data-sources.html).

> As an example, the following creates a DataFrame based on the content of a JSON file:

```java
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

Dataset<Row> df = spark.read().json("examples/src/main/resources/people.json");

// Displays the content of the DataFrame to stdout
df.show();
// +----+-------+
// | age|   name|
// +----+-------+
// |null|Michael|
// |  30|   Andy|
// |  19| Justin|
// +----+-------+
```

`Find full example code at "examples/src/main/java/org/apache/spark/examples/sql/JavaSparkSQLExample.java" in the Spark repo.`

### Python

> With a SparkSession, applications can create DataFrames from an [existing RDD](https://spark.apache.org/docs/3.3.2/sql-getting-started.html#interoperating-with-rdds), from a Hive table, or from [Spark data sources](https://spark.apache.org/docs/3.3.2/sql-data-sources.html).

> As an example, the following creates a DataFrame based on the content of a JSON file:

```python
# spark is an existing SparkSession
df = spark.read.json("examples/src/main/resources/people.json")
# Displays the content of the DataFrame to stdout
df.show()
# +----+-------+
# | age|   name|
# +----+-------+
# |null|Michael|
# |  30|   Andy|
# |  19| Justin|
# +----+-------+
```

`Find full example code at "examples/src/main/python/sql/basic.py" in the Spark repo.`

### R

> With a SparkSession, applications can create DataFrames from a local R data.frame, from a Hive table, or from [Spark data sources](https://spark.apache.org/docs/3.3.2/sql-data-sources.html).

> As an example, the following creates a DataFrame based on the content of a JSON file:

```python
df <- read.json("examples/src/main/resources/people.json")

# Displays the content of the DataFrame
head(df)
##   age    name
## 1  NA Michael
## 2  30    Andy
## 3  19  Justin

# Another method to print the first few rows and optionally truncate the printing of long values
showDF(df)
## +----+-------+
## | age|   name|
## +----+-------+
## |null|Michael|
## |  30|   Andy|
## |  19| Justin|
## +----+-------+
```

`Find full example code at "examples/src/main/r/RSparkSQLExample.R" in the Spark repo.`

## Untyped Dataset Operations (aka DataFrame Operations)

> DataFrames provide a domain-specific language for structured data manipulation in [Scala](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/sql/Dataset.html), [Java](https://spark.apache.org/docs/3.3.2/api/java/index.html?org/apache/spark/sql/Dataset.html), [Python](https://spark.apache.org/docs/3.3.2/api/python/reference/pyspark.sql/api/pyspark.sql.DataFrame.html) and [R](https://spark.apache.org/docs/3.3.2/api/R/reference/SparkDataFrame.html).

在 Scala、Java、Python 和 R 中，DataFrames 提供了一个特定的语法用在结构化数据的操作。

> As mentioned above, in Spark 2.0, DataFrames are just Dataset of `Rows` in Scala and Java API. These operations are also referred as “untyped transformations” in contrast to “typed transformations” come with strongly typed Scala/Java Datasets.

正如上面提到的一样，在 Spark 2.0 的 Scala 和 Java API 中，DataFrames 仅仅是 Dataset of Rows。

这些操作称为“无类型转换”，与强类型的 Scala/Java Datasets 中的 “类型转换” 对应的。

> Here we include some basic examples of structured data processing using Datasets:

这里包括一些使用 Dataset 处理结构化数据的示例 :

### Scala

```scala
// This import is needed to use the $-notation
import spark.implicits._
// Print the schema in a tree format
df.printSchema()
// root
// |-- age: long (nullable = true)
// |-- name: string (nullable = true)

// Select only the "name" column
df.select("name").show()
// +-------+
// |   name|
// +-------+
// |Michael|
// |   Andy|
// | Justin|
// +-------+

// Select everybody, but increment the age by 1
df.select($"name", $"age" + 1).show()
// +-------+---------+
// |   name|(age + 1)|
// +-------+---------+
// |Michael|     null|
// |   Andy|       31|
// | Justin|       20|
// +-------+---------+

// Select people older than 21
df.filter($"age" > 21).show()
// +---+----+
// |age|name|
// +---+----+
// | 30|Andy|
// +---+----+

// Count people by age
df.groupBy("age").count().show()
// +----+-----+
// | age|count|
// +----+-----+
// |  19|    1|
// |null|    1|
// |  30|    1|
// +----+-----+
```

`Find full example code at "examples/src/main/scala/org/apache/spark/examples/sql/SparkSQLExample.scala" in the Spark repo.`

> For a complete list of the types of operations that can be performed on a Dataset, refer to the [API Documentation](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/sql/Dataset.html).

关于 DataFrame 更多的操作见 API Documentation

### Java

```java
// col("...") is preferable to df.col("...")
import static org.apache.spark.sql.functions.col;

// Print the schema in a tree format
df.printSchema();
// root
// |-- age: long (nullable = true)
// |-- name: string (nullable = true)

// Select only the "name" column
df.select("name").show();
// +-------+
// |   name|
// +-------+
// |Michael|
// |   Andy|
// | Justin|
// +-------+

// Select everybody, but increment the age by 1
df.select(col("name"), col("age").plus(1)).show();
// +-------+---------+
// |   name|(age + 1)|
// +-------+---------+
// |Michael|     null|
// |   Andy|       31|
// | Justin|       20|
// +-------+---------+

// Select people older than 21
df.filter(col("age").gt(21)).show();
// +---+----+
// |age|name|
// +---+----+
// | 30|Andy|
// +---+----+

// Count people by age
df.groupBy("age").count().show();
// +----+-----+
// | age|count|
// +----+-----+
// |  19|    1|
// |null|    1|
// |  30|    1|
// +----+-----+
```

`Find full example code at "examples/src/main/java/org/apache/spark/examples/sql/JavaSparkSQLExample.java" in the Spark repo.`

> For a complete list of the types of operations that can be performed on a Dataset refer to the [API Documentation](https://spark.apache.org/docs/3.3.2/api/java/org/apache/spark/sql/Dataset.html).

### Python

> In Python, it’s possible to access a DataFrame’s columns either by attribute (df.age) or by indexing (df['age']). While the former is convenient for interactive data exploration, users are highly encouraged to use the latter form, which is future proof and won’t break with column names that are also attributes on the DataFrame class.

在 Python 中，可以通过属性访问`df.age`，也可以通过索引访问`df['age']`。

```python
# spark, df are from the previous example
# Print the schema in a tree format
df.printSchema()
# root
# |-- age: long (nullable = true)
# |-- name: string (nullable = true)

# Select only the "name" column
df.select("name").show()
# +-------+
# |   name|
# +-------+
# |Michael|
# |   Andy|
# | Justin|
# +-------+

# Select everybody, but increment the age by 1
df.select(df['name'], df['age'] + 1).show()
# +-------+---------+
# |   name|(age + 1)|
# +-------+---------+
# |Michael|     null|
# |   Andy|       31|
# | Justin|       20|
# +-------+---------+

# Select people older than 21
df.filter(df['age'] > 21).show()
# +---+----+
# |age|name|
# +---+----+
# | 30|Andy|
# +---+----+

# Count people by age
df.groupBy("age").count().show()
# +----+-----+
# | age|count|
# +----+-----+
# |  19|    1|
# |null|    1|
# |  30|    1|
# +----+-----+
```

`Find full example code at "examples/src/main/python/sql/basic.py" in the Spark repo.`

> For a complete list of the types of operations that can be performed on a DataFrame refer to the [API Documentation](https://spark.apache.org/docs/3.3.2/api/python/reference/pyspark.sql.html#dataframe-apis).

### R

```r
# Create the DataFrame
df <- read.json("examples/src/main/resources/people.json")

# Show the content of the DataFrame
head(df)
##   age    name
## 1  NA Michael
## 2  30    Andy
## 3  19  Justin


# Print the schema in a tree format
printSchema(df)
## root
## |-- age: long (nullable = true)
## |-- name: string (nullable = true)

# Select only the "name" column
head(select(df, "name"))
##      name
## 1 Michael
## 2    Andy
## 3  Justin

# Select everybody, but increment the age by 1
head(select(df, df$name, df$age + 1))
##      name (age + 1.0)
## 1 Michael          NA
## 2    Andy          31
## 3  Justin          20

# Select people older than 21
head(where(df, df$age > 21))
##   age name
## 1  30 Andy

# Count people by age
head(count(groupBy(df, "age")))
##   age count
## 1  19     1
## 2  NA     1
## 3  30     1
```

`Find full example code at "examples/src/main/r/RSparkSQLExample.R" in the Spark repo.`

> For a complete list of the types of operations that can be performed on a DataFrame refer to the [API Documentation](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/sql/Dataset.html).

> In addition to simple column references and expressions, Datasets also have a rich library of functions including string manipulation, date arithmetic, common math operations and more. The complete list is available in the [DataFrame Function Reference](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/sql/functions$.html).

除了简单的列引用和表达式之外，DataFrame 还有丰富的函数库，包含：字符串操作、日期计算、数学运算等。更多请见 DataFrame Function Reference.

## Running SQL Queries Programmatically

### Scala

> The `sql` function on a `SparkSession` enables applications to run SQL queries programmatically and returns the result as a `DataFrame`.

SparkSession 中的 sql 函数能以编程的方式运行 SQL 查询，以 DataFrame 的形式返回结果。

```scala
// Register the DataFrame as a SQL temporary view
df.createOrReplaceTempView("people")

val sqlDF = spark.sql("SELECT * FROM people")
sqlDF.show()
// +----+-------+
// | age|   name|
// +----+-------+
// |null|Michael|
// |  30|   Andy|
// |  19| Justin|
// +----+-------+
```

> Find full example code at "examples/src/main/scala/org/apache/spark/examples/sql/SparkSQLExample.scala" in the Spark repo.

### Java

> The `sql` function on a `SparkSession` enables applications to run SQL queries programmatically and returns the result as a `Dataset<Row>`.

```java
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

// Register the DataFrame as a SQL temporary view
df.createOrReplaceTempView("people");

Dataset<Row> sqlDF = spark.sql("SELECT * FROM people");
sqlDF.show();
// +----+-------+
// | age|   name|
// +----+-------+
// |null|Michael|
// |  30|   Andy|
// |  19| Justin|
// +----+-------+
```

> Find full example code at "examples/src/main/java/org/apache/spark/examples/sql/JavaSparkSQLExample.java" in the Spark repo.

### Python

> The `sql` function on a `SparkSession` enables applications to run SQL queries programmatically and returns the result as a `DataFrame`.

```python
# Register the DataFrame as a SQL temporary view
df.createOrReplaceTempView("people")

sqlDF = spark.sql("SELECT * FROM people")
sqlDF.show()
# +----+-------+
# | age|   name|
# +----+-------+
# |null|Michael|
# |  30|   Andy|
# |  19| Justin|
# +----+-------+
```

> Find full example code at "examples/src/main/python/sql/basic.py" in the Spark repo.

### R

> The `sql` function enables applications to run SQL queries programmatically and returns the result as a `SparkDataFrame`.

```r
df <- sql("SELECT * FROM table")
```

> Find full example code at "examples/src/main/r/RSparkSQLExample.R" in the Spark repo.

## Global Temporary View

> Temporary views in Spark SQL are session-scoped and will disappear if the session that creates it terminates. If you want to have a temporary view that is shared among all sessions and keep alive until the Spark application terminates, you can create a global temporary view. Global temporary view is tied to a system preserved database `global_temp`, and we must use the qualified name to refer it, e.g. `SELECT * FROM global_temp.view1`.

Spark SQL 中临时视图的作用范围是在一个会话内，此会话结束，此临时消失。

如果想在多个会话间共享临时视图，直至应用程序结束，你可以创建全局的临时视图。

全局临时视图和系统数据库 `global_temp` 相关联，且必须起个合法的名字。如 `SELECT * FROM global_temp.view1`.

### Scala

```scala
// Register the DataFrame as a global temporary view
df.createGlobalTempView("people")

// Global temporary view is tied to a system preserved database `global_temp`
spark.sql("SELECT * FROM global_temp.people").show()
// +----+-------+
// | age|   name|
// +----+-------+
// |null|Michael|
// |  30|   Andy|
// |  19| Justin|
// +----+-------+

// Global temporary view is cross-session
spark.newSession().sql("SELECT * FROM global_temp.people").show()
// +----+-------+
// | age|   name|
// +----+-------+
// |null|Michael|
// |  30|   Andy|
// |  19| Justin|
// +----+-------+
```

> Find full example code at "examples/src/main/scala/org/apache/spark/examples/sql/SparkSQLExample.scala" in the Spark repo.

### Java

```java
// Register the DataFrame as a global temporary view
df.createGlobalTempView("people");

// Global temporary view is tied to a system preserved database `global_temp`
spark.sql("SELECT * FROM global_temp.people").show();
// +----+-------+
// | age|   name|
// +----+-------+
// |null|Michael|
// |  30|   Andy|
// |  19| Justin|
// +----+-------+

// Global temporary view is cross-session
spark.newSession().sql("SELECT * FROM global_temp.people").show();
// +----+-------+
// | age|   name|
// +----+-------+
// |null|Michael|
// |  30|   Andy|
// |  19| Justin|
// +----+-------+
```

> Find full example code at "examples/src/main/java/org/apache/spark/examples/sql/JavaSparkSQLExample.java" in the Spark repo.

### Python

```python
# Register the DataFrame as a global temporary view
df.createGlobalTempView("people")

# Global temporary view is tied to a system preserved database `global_temp`
spark.sql("SELECT * FROM global_temp.people").show()
# +----+-------+
# | age|   name|
# +----+-------+
# |null|Michael|
# |  30|   Andy|
# |  19| Justin|
# +----+-------+

# Global temporary view is cross-session
spark.newSession().sql("SELECT * FROM global_temp.people").show()
# +----+-------+
# | age|   name|
# +----+-------+
# |null|Michael|
# |  30|   Andy|
# |  19| Justin|
# +----+-------+
```

> Find full example code at "examples/src/main/python/sql/basic.py" in the Spark repo.

### SQL

```sql
CREATE GLOBAL TEMPORARY VIEW temp_view AS SELECT a + 1, b * 2 FROM tbl

SELECT * FROM global_temp.temp_view
```

## Creating Datasets

> Datasets are similar to RDDs, however, instead of using Java serialization or Kryo they use a specialized [Encoder](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/sql/Encoder.html) to serialize the objects for processing or transmitting over the network. While both encoders and standard serialization are responsible for turning an object into bytes, encoders are code generated dynamically and use a format that allows Spark to perform many operations like filtering, sorting and hashing without deserializing the bytes back into an object.

Datasets 和 RDDs 类似，但是不使用 Java 序列化器或 Kryo, 而是使用了一个特殊的 Encoder 序列化对象，为处理或在网络间传输对象。

虽然 Encoder 和标准序列化都负责将一个对象序列化成字节，但是 Encoder 是动态生成的代码，并且使用了一种允许 Spark 去执行许多像过滤、排序以及哈希这样的操作，而不需要再将字节反序列化成对象。

### Scala

```scala
case class Person(name: String, age: Long)

// Encoders are created for case classes
val caseClassDS = Seq(Person("Andy", 32)).toDS()
caseClassDS.show()
// +----+---+
// |name|age|
// +----+---+
// |Andy| 32|
// +----+---+

// Encoders for most common types are automatically provided by importing spark.implicits._
val primitiveDS = Seq(1, 2, 3).toDS()
primitiveDS.map(_ + 1).collect() // Returns: Array(2, 3, 4)

// DataFrames can be converted to a Dataset by providing a class. Mapping will be done by name
val path = "examples/src/main/resources/people.json"
val peopleDS = spark.read.json(path).as[Person]
peopleDS.show()
// +----+-------+
// | age|   name|
// +----+-------+
// |null|Michael|
// |  30|   Andy|
// |  19| Justin|
// +----+-------+
```

> Find full example code at "examples/src/main/scala/org/apache/spark/examples/sql/SparkSQLExample.scala" in the Spark repo.

### Java

```java
import java.util.Arrays;
import java.util.Collections;
import java.io.Serializable;

import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Encoders;

public static class Person implements Serializable {
  private String name;
  private long age;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public long getAge() {
    return age;
  }

  public void setAge(long age) {
    this.age = age;
  }
}

// Create an instance of a Bean class
Person person = new Person();
person.setName("Andy");
person.setAge(32);

// Encoders are created for Java beans
Encoder<Person> personEncoder = Encoders.bean(Person.class);
Dataset<Person> javaBeanDS = spark.createDataset(
  Collections.singletonList(person),
  personEncoder
);
javaBeanDS.show();
// +---+----+
// |age|name|
// +---+----+
// | 32|Andy|
// +---+----+

// Encoders for most common types are provided in class Encoders
Encoder<Long> longEncoder = Encoders.LONG();
Dataset<Long> primitiveDS = spark.createDataset(Arrays.asList(1L, 2L, 3L), longEncoder);
Dataset<Long> transformedDS = primitiveDS.map(
    (MapFunction<Long, Long>) value -> value + 1L,
    longEncoder);
transformedDS.collect(); // Returns [2, 3, 4]

// DataFrames can be converted to a Dataset by providing a class. Mapping based on name
String path = "examples/src/main/resources/people.json";
Dataset<Person> peopleDS = spark.read().json(path).as(personEncoder);
peopleDS.show();
// +----+-------+
// | age|   name|
// +----+-------+
// |null|Michael|
// |  30|   Andy|
// |  19| Justin|
// +----+-------+
```

> Find full example code at "examples/src/main/java/org/apache/spark/examples/sql/JavaSparkSQLExample.java" in the Spark repo.

## Interoperating with RDDs

> Spark SQL supports two different methods for converting existing RDDs into Datasets. The first method uses reflection to infer the schema of an RDD that contains specific types of objects. This reflection-based approach leads to more concise code and works well when you already know the schema while writing your Spark application.

Spark SQL 支持两种将 RDD 转换成 Datasets 的方法：

第一种方法就是使用反射去推断一个包含指定对象类型的 RDD 的 Schema。 

在你的 Spark 应用程序中，当你已知 Schema 时，这个方法可以让你的代码更简洁、性能更好。

> The second method for creating Datasets is through a programmatic interface that allows you to construct a schema and then apply it to an existing RDD. 【While this method is more verbose, it allows you to construct Datasets when the columns and their types are not known until runtime.】

第二种方法就是先构造 Schema 然后把它应用到一个已存在的 RDD。


### Inferring the Schema Using Reflection

#### Scala

> The Scala interface for Spark SQL supports automatically converting an RDD containing case classes to a DataFrame. The case class defines the schema of the table. The names of the arguments to the case class are read using reflection and become the names of the columns. Case classes can also be nested or contain complex types such as `Seqs` or `Arrays`. This RDD can be implicitly converted to a DataFrame and then be registered as a table. Tables can be used in subsequent SQL statements.

Spark SQL 支持将包含样例类的 RDD 自动转换为一个 DataFrame.

样例类定义了表的 schema. 使用反射来读取样例类的参数名称，成为列的名称。样例类也可以嵌套，或包含复杂的类型，如 Seqs 或 Arrays.

这个 RDD 能隐式转换成一个 DataFrame, 然后注册成一个表。表可以用在后续的 SQL 语句中。

```scala
// For implicit conversions from RDDs to DataFrames
import spark.implicits._

// Create an RDD of Person objects from a text file, convert it to a Dataframe
val peopleDF = spark.sparkContext
  .textFile("examples/src/main/resources/people.txt")
  .map(_.split(","))
  .map(attributes => Person(attributes(0), attributes(1).trim.toInt))
  .toDF()
// Register the DataFrame as a temporary view
peopleDF.createOrReplaceTempView("people")

// SQL statements can be run by using the sql methods provided by Spark
val teenagersDF = spark.sql("SELECT name, age FROM people WHERE age BETWEEN 13 AND 19")

// The columns of a row in the result can be accessed by field index
teenagersDF.map(teenager => "Name: " + teenager(0)).show()
// +------------+
// |       value|
// +------------+
// |Name: Justin|
// +------------+

// or by field name
teenagersDF.map(teenager => "Name: " + teenager.getAs[String]("name")).show()
// +------------+
// |       value|
// +------------+
// |Name: Justin|
// +------------+

// No pre-defined encoders for Dataset[Map[K,V]], define explicitly
implicit val mapEncoder = org.apache.spark.sql.Encoders.kryo[Map[String, Any]]
// Primitive types and case classes can be also defined as
// implicit val stringIntMapEncoder: Encoder[Map[String, Any]] = ExpressionEncoder()

// row.getValuesMap[T] retrieves multiple columns at once into a Map[String, T]
teenagersDF.map(teenager => teenager.getValuesMap[Any](List("name", "age"))).collect()
// Array(Map("name" -> "Justin", "age" -> 19))
```

> Find full example code at "examples/src/main/scala/org/apache/spark/examples/sql/SparkSQLExample.scala" in the Spark repo.

#### Java

> Spark SQL supports automatically converting an RDD of [JavaBeans](http://stackoverflow.com/questions/3295496/what-is-a-javabean-exactly) into a DataFrame. The BeanInfo, obtained using reflection, defines the schema of the table. Currently, Spark SQL does not support JavaBeans that contain Map field(s). Nested JavaBeans and `List` or `Array` fields are supported though. You can create a JavaBean by creating a class that implements Serializable and has getters and setters for all of its fields.

Spark SQL 支持将 JavaBeans 的 RDD 自动转换为 DataFrame. 使用反射获得的 BeanInfo 定义了表的 schema. 

目前 Spark SQL 不支持含有 Map 字段的 JavaBeans. 但是支持 JavaBeans 和 List 或 Array 的嵌套。

你可以通过创建一个有 getters 和 setters 的序列化的类来创建一个 JavaBean。

```java
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Encoders;

// Create an RDD of Person objects from a text file
JavaRDD<Person> peopleRDD = spark.read()
  .textFile("examples/src/main/resources/people.txt")
  .javaRDD()
  .map(line -> {
    String[] parts = line.split(",");
    Person person = new Person();
    person.setName(parts[0]);
    person.setAge(Integer.parseInt(parts[1].trim()));
    return person;
  });

// Apply a schema to an RDD of JavaBeans to get a DataFrame
Dataset<Row> peopleDF = spark.createDataFrame(peopleRDD, Person.class);
// Register the DataFrame as a temporary view
peopleDF.createOrReplaceTempView("people");

// SQL statements can be run by using the sql methods provided by spark
Dataset<Row> teenagersDF = spark.sql("SELECT name FROM people WHERE age BETWEEN 13 AND 19");

// The columns of a row in the result can be accessed by field index
Encoder<String> stringEncoder = Encoders.STRING();
Dataset<String> teenagerNamesByIndexDF = teenagersDF.map(
    (MapFunction<Row, String>) row -> "Name: " + row.getString(0),
    stringEncoder);
teenagerNamesByIndexDF.show();
// +------------+
// |       value|
// +------------+
// |Name: Justin|
// +------------+

// or by field name
Dataset<String> teenagerNamesByFieldDF = teenagersDF.map(
    (MapFunction<Row, String>) row -> "Name: " + row.<String>getAs("name"),
    stringEncoder);
teenagerNamesByFieldDF.show();
// +------------+
// |       value|
// +------------+
// |Name: Justin|
// +------------+
```

> Find full example code at "examples/src/main/java/org/apache/spark/examples/sql/JavaSparkSQLExample.java" in the Spark repo.

#### Python

> Spark SQL can convert an RDD of Row objects to a DataFrame, inferring the datatypes. Rows are constructed by passing a list of key/value pairs as kwargs to the Row class. The keys of this list define the column names of the table, and the types are inferred by sampling the whole dataset, similar to the inference that is performed on JSON files.

Spark SQL 可以将 Row 对象的 RDD 转换成一个 DataFrame, DataFrame 会推断数据类型。

Rows 对象的创建是通过传一个键值对的列表作为关键字参数。 列表的 keys 是表的列名，类型通过抽样整个 dataset 来推断。

同样的推断也适用于 JSON 文件。

```python
from pyspark.sql import Row

sc = spark.sparkContext

# Load a text file and convert each line to a Row.
lines = sc.textFile("examples/src/main/resources/people.txt")
parts = lines.map(lambda l: l.split(","))
people = parts.map(lambda p: Row(name=p[0], age=int(p[1])))

# Infer the schema, and register the DataFrame as a table.
schemaPeople = spark.createDataFrame(people)
schemaPeople.createOrReplaceTempView("people")

# SQL can be run over DataFrames that have been registered as a table.
teenagers = spark.sql("SELECT name FROM people WHERE age >= 13 AND age <= 19")

# The results of SQL queries are Dataframe objects.
# rdd returns the content as an :class:`pyspark.RDD` of :class:`Row`.
teenNames = teenagers.rdd.map(lambda p: "Name: " + p.name).collect()
for name in teenNames:
    print(name)
# Name: Justin
```

> Find full example code at "examples/src/main/python/sql/basic.py" in the Spark repo.

## Programmatically Specifying the Schema

### Scala

> When case classes cannot be defined ahead of time (for example, the structure of records is encoded in a string, or a text dataset will be parsed and fields will be projected differently for different users), a DataFrame can be created programmatically with three steps.

当不能提前定义样例类（例如，记录的结构被编码在一个字符串中，或解析一个文本数据集后，字段被不同的用户所拥有），那么一个 DataFrame 可以通过以下三步来创建。

> 1.Create an RDD of `Rows` from the original RDD;
> 2.Create the schema represented by a `StructType` matching the structure of `Rows` in the RDD created in Step 1.
> 3.Apply the schema to the RDD of Rows via `createDataFrame` method provided by SparkSession.

1. 从原始 RDD 创建一个 Rows RDD
2. 创建一个由 StructType 表示的 Schema, StructType 匹配了步骤1创建的 RDD 中的元组或列表结构
3. 通过 SparkSession.createDataFrame 方法将 Schema 应用到 RDD

For example:

```scala
import org.apache.spark.sql.Row

import org.apache.spark.sql.types._

// Create an RDD
val peopleRDD = spark.sparkContext.textFile("examples/src/main/resources/people.txt")

// The schema is encoded in a string
val schemaString = "name age"

// Generate the schema based on the string of schema
val fields = schemaString.split(" ")
  .map(fieldName => StructField(fieldName, StringType, nullable = true))
val schema = StructType(fields)

// Convert records of the RDD (people) to Rows
val rowRDD = peopleRDD
  .map(_.split(","))
  .map(attributes => Row(attributes(0), attributes(1).trim))

// Apply the schema to the RDD
val peopleDF = spark.createDataFrame(rowRDD, schema)

// Creates a temporary view using the DataFrame
peopleDF.createOrReplaceTempView("people")

// SQL can be run over a temporary view created using DataFrames
val results = spark.sql("SELECT name FROM people")

// The results of SQL queries are DataFrames and support all the normal RDD operations
// The columns of a row in the result can be accessed by field index or by field name
results.map(attributes => "Name: " + attributes(0)).show()
// +-------------+
// |        value|
// +-------------+
// |Name: Michael|
// |   Name: Andy|
// | Name: Justin|
// +-------------+
```

> Find full example code at "examples/src/main/scala/org/apache/spark/examples/sql/SparkSQLExample.scala" in the Spark repo.

### Java

> When JavaBean classes cannot be defined ahead of time (for example, the structure of records is encoded in a string, or a text dataset will be parsed and fields will be projected differently for different users), a `Dataset<Row>` can be created programmatically with three steps.

> 1.Create an RDD of Rows from the original RDD;
> 2.Create the schema represented by a `StructType` matching the structure of Rows in the RDD created in Step 1.
> 3.Apply the schema to the RDD of Rows via `createDataFrame` method provided by `SparkSession`.

For example:

```java
import java.util.ArrayList;
import java.util.List;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

// Create an RDD
JavaRDD<String> peopleRDD = spark.sparkContext()
  .textFile("examples/src/main/resources/people.txt", 1)
  .toJavaRDD();

// The schema is encoded in a string
String schemaString = "name age";

// Generate the schema based on the string of schema
List<StructField> fields = new ArrayList<>();
for (String fieldName : schemaString.split(" ")) {
  StructField field = DataTypes.createStructField(fieldName, DataTypes.StringType, true);
  fields.add(field);
}
StructType schema = DataTypes.createStructType(fields);

// Convert records of the RDD (people) to Rows
JavaRDD<Row> rowRDD = peopleRDD.map((Function<String, Row>) record -> {
  String[] attributes = record.split(",");
  return RowFactory.create(attributes[0], attributes[1].trim());
});

// Apply the schema to the RDD
Dataset<Row> peopleDataFrame = spark.createDataFrame(rowRDD, schema);

// Creates a temporary view using the DataFrame
peopleDataFrame.createOrReplaceTempView("people");

// SQL can be run over a temporary view created using DataFrames
Dataset<Row> results = spark.sql("SELECT name FROM people");

// The results of SQL queries are DataFrames and support all the normal RDD operations
// The columns of a row in the result can be accessed by field index or by field name
Dataset<String> namesDS = results.map(
    (MapFunction<Row, String>) row -> "Name: " + row.getString(0),
    Encoders.STRING());
namesDS.show();
// +-------------+
// |        value|
// +-------------+
// |Name: Michael|
// |   Name: Andy|
// | Name: Justin|
// +-------------+
```

> Find full example code at "examples/src/main/java/org/apache/spark/examples/sql/JavaSparkSQLExample.java" in the Spark repo.

### Python

> When a dictionary of kwargs cannot be defined ahead of time (for example, the structure of records is encoded in a string, or a text dataset will be parsed and fields will be projected differently for different users), a DataFrame can be created programmatically with three steps.

当一个参数字典不能被提前定义（例如，记录的结构被编码在一个字符串中，或解析一个文本数据集后，字段被不同的用户所拥有），那么 DataFrame 可以通过以下三步来创建。

> Create an RDD of tuples or lists from the original RDD;
> Create the schema represented by a StructType matching the structure of tuples or lists in the RDD created in the step 1.
> Apply the schema to the RDD via createDataFrame method provided by SparkSession.

1. 从原始 RDD 创建一个元素为元组或列表的 RDD
2. 创建一个由 StructType 表示的 Schema, StructType 匹配了 RDD 中的元组或列表结构
3. 通过 SparkSession.createDataFrame 方法应用 Schema 到 RDD

For example:

```
# Import data types
from pyspark.sql.types import StringType, StructType, StructField

sc = spark.sparkContext

# Load a text file and convert each line to a Row.
lines = sc.textFile("examples/src/main/resources/people.txt")
parts = lines.map(lambda l: l.split(","))
# Each line is converted to a tuple.
people = parts.map(lambda p: (p[0], p[1].strip()))

# The schema is encoded in a string.
schemaString = "name age"

fields = [StructField(field_name, StringType(), True) for field_name in schemaString.split()]
schema = StructType(fields)

# Apply the schema to the RDD.
schemaPeople = spark.createDataFrame(people, schema)

# Creates a temporary view using the DataFrame
schemaPeople.createOrReplaceTempView("people")

# SQL can be run over DataFrames that have been registered as a table.
results = spark.sql("SELECT name FROM people")

results.show()
# +-------+
# |   name|
# +-------+
# |Michael|
# |   Andy|
# | Justin|
# +-------+
```

> Find full example code at "examples/src/main/python/sql/basic.py" in the Spark repo.

## Scalar Functions

> Scalar functions are functions that return a single value per row, as opposed to aggregation functions, which return a value for a group of rows. Spark SQL supports a variety of [Built-in Scalar Functions](https://spark.apache.org/docs/3.3.2/sql-ref-functions.html#scalar-functions). It also supports [User Defined Scalar Functions](https://spark.apache.org/docs/3.3.2/sql-ref-functions-udf-scalar.html).

Scalar functions 是每个输入行返回一个值。

Spark SQL 支持多种内建标量函数和用户自定义标量函数。

## Aggregate Functions

> Aggregate functions are functions that return a single value on a group of rows. The [Built-in Aggregation Functions](https://spark.apache.org/docs/3.3.2/sql-ref-functions-builtin.html#aggregate-functions) provide common aggregations such as count(), count_distinct(), avg(), max(), min(), etc. Users are not limited to the predefined aggregate functions and can create their own. For more details about user defined aggregate functions, please refer to the documentation of [User Defined Aggregate Functions](https://spark.apache.org/docs/3.3.2/sql-ref-functions-udf-aggregate.html).

Aggregate functions 是每个输入组上返回一个值。

内建聚合函数提供了诸如 `count()`、`count_distinct()`、`avg()`、`max()`和`min()`等通用聚合函数。

也支持用户自定义聚合函数。