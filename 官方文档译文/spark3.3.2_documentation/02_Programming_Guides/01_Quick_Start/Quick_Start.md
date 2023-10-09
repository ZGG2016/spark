# Quick Start

[TOC]

> This tutorial provides a quick introduction to using Spark. We will first introduce the API through Spark’s interactive shell (in Python or Scala), then show how to write applications in Java, Scala, and Python.

本教程是使用 Spark 的快速入门。首先通过 Spark 交互式的 shell(在 Python 或 Scala 中)来介绍 API, 然后展示如何使用 Java, Scala 和 Python 来编写应用程序。

> To follow along with this guide, first, download a packaged release of Spark from the [Spark website](https://spark.apache.org/downloads.html). Since we won’t be using HDFS, you can download a package for any version of Hadoop.

为了继续阅读本指南，首先从 Spark 官网下载 Spark 的发行包。因为我们不使用 HDFS, 所以你可以下载任意一个 Hadoop 版本的软件包。

> Note that, before Spark 2.0, the main programming interface of Spark was the Resilient Distributed Dataset (RDD). After Spark 2.0, RDDs are replaced by Dataset, which is strongly-typed like an RDD, but with richer optimizations under the hood. The RDD interface is still supported, and you can get a more detailed reference at the [RDD programming guide](https://spark.apache.org/docs/3.3.2/rdd-programming-guide.html). However, we highly recommend you to switch to use Dataset, which has better performance than RDD. See the [SQL programming guide](https://spark.apache.org/docs/3.3.2/sql-programming-guide.html) to get more information about Dataset.

请注意，在 Spark 2.0 之前，Spark 的主要编程接口是弹性分布式数据集(RDD)。 在 Spark 2.0 之后, RDD 被 Dataset 替换 ，它是像 RDD 一样的 强类型，但是更加优化。 

RDD 接口仍然受支持，可以在 RDD programming guide 中获得更完整的参考。 但是，我们强烈建议您切换到使用 Dataset, 其性能要更优于 RDD. 请参阅 SQL programming guide 获取更多有关 Dataset 的信息。

	备注：强类型语言中，所有基本数据类型（整型，字符型，浮点型等等）都作为该语言的一部分被预定义。程序中的所有变量和常量都必须借助这些数据类型来表示。数据和数据类型绑定，**在定义变量/常量时必须同时指出它的数据类型**。对数据可执行的操作也因数据类型的不同而异。

## Interactive Analysis with the Spark Shell

### Basics

> Spark’s shell provides a simple way to learn the API, as well as a powerful tool to analyze data interactively. It is available in either Scala (which runs on the Java VM and is thus a good way to use existing Java libraries) or Python. Start it by running the following in the Spark directory:

Spark shell 提供了一种比较简单的方式和一个强大的交互式数据分析工具，学习 API.

可以运行在 Scala(运行于 Java 虚拟机之上，并能很好的调用已有的 Java 类库)或者 Python 中。

可以通过在 Spark 目录中运行以下的命令来启动它:

#### Scala

```
./bin/spark-shell
```

> Spark’s primary abstraction is a distributed collection of items called a Dataset. Datasets can be created from Hadoop InputFormats (such as HDFS files) or by transforming other Datasets. Let’s make a new Dataset from the text of the README file in the Spark source directory:

Spark 的主要抽象是一个称为 Dataset 的分布式的项集合。 

Datasets 可以从 Hadoop 的 InputFormats(例如 HDFS 文件)或者通过其它的 Datasets 转换来创建。让我们从 Spark 源目录中的 README 文件来创建一个新的 Dataset:

```
scala> val textFile = spark.read.textFile("README.md")
textFile: org.apache.spark.sql.Dataset[String] = [value: string]
```

> You can get values from Dataset directly, by calling some actions, or transform the Dataset to get a new one. For more details, please read the [API doc](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/sql/Dataset.html).

可以通过使用一些 actions 直接从 Dataset 中获取值， 或者通过 transform 获得一个新的 Dataset

```
scala> textFile.count() // Number of items in this Dataset
res0: Long = 126 // May be different from yours as README.md will change over time, similar to other outputs

scala> textFile.first() // First item in this Dataset
res1: String = # Apache Spark
```

> Now let’s transform this Dataset into a new one. We call filter to return a new Dataset with a subset of the items in the file.

由这个 Dataset 转换获得一个新的 Dataset

我们调用 filter 来返回一个新的 Dataset, 这个 Dataset 是文件中所有行的一个子集。

```
scala> val linesWithSpark = textFile.filter(line => line.contains("Spark"))
linesWithSpark: org.apache.spark.sql.Dataset[String] = [value: string]
```

> We can chain together transformations and actions:

可以将转换和行动算子里链接起来

```
scala> textFile.filter(line => line.contains("Spark")).count() // How many lines contain "Spark"?
res3: Long = 15
```

#### Python

```
./bin/pyspark
```

> Or if PySpark is installed with pip in your current environment:

如果在当前环境下使用 pip 安装了 PySpark, 可以这么启动：

```
pyspark
```

> Spark’s primary abstraction is a distributed collection of items called a Dataset. Datasets can be created from Hadoop InputFormats (such as HDFS files) or by transforming other Datasets. Due to Python’s dynamic nature, we don’t need the Dataset to be strongly-typed in Python. As a result, all Datasets in Python are `Dataset[Row]`, and we call it DataFrame to be consistent with the data frame concept in Pandas and R. Let’s make a new DataFrame from the text of the README file in the Spark source directory:

Spark 的主要抽象是一个称为 Dataset 的分布式的项的集合。

Datasets 可以从 Hadoop 的 InputFormats(例如 HDFS 文件)或者通过其它的 Datasets 转换来创建。

由于 Python 具有动态特性，所以在 Python 中，不需要 Datasets 是强类型的。

在 Python 中，所有的 Datasets 都是 `Dataset[Row]` ，我们称它为 DataFrame, 以和 Pandas 和 R 中的 data frame 概念一致。

让我们从 Spark 源目录中的 README 文件来创建一个新的 DataFrame:


```
>>> textFile = spark.read.text("README.md")
```

> You can get values from DataFrame directly, by calling some actions, or transform the DataFrame to get a new one. For more details, please read the [API doc](https://spark.apache.org/docs/3.3.2/api/python/index.html#pyspark.sql.DataFrame).

可以通过使用一些 actions 直接从 DataFrame 中获取值，或者 transform 获得一个新的 DataFrame

```
>>> textFile.count()  # Number of rows in this DataFrame
126

>>> textFile.first()  # First row in this DataFrame
Row(value=u'# Apache Spark')
```

> Now let’s transform this DataFrame to a new one. We call filter to return a new DataFrame with a subset of the lines in the file.

由这个 DataFrame 转换获得一个新的 DataFrame

调用 filter 来返回一个新的 DataFrame, 这个 DataFrame 是文件中所有行的一个子集。

```
>>> linesWithSpark = textFile.filter(textFile.value.contains("Spark"))
```

> We can chain together transformations and actions:

可以将转换和行动算子里链接起来

```
>>> textFile.filter(textFile.value.contains("Spark")).count()  # How many lines contain "Spark"?
15
```

### More on Dataset Operations

> Dataset actions and transformations can be used for more complex computations. Let’s say we want to find the line with the most words:

Dataset 的 actions 和 transformations 可以用于更复杂的计算。例如，统计字数最多的行 :

#### Scala

```
scala> textFile.map(line => line.split(" ").size).reduce((a, b) => if (a > b) a else b)
res4: Long = 15
```

> This first maps a line to an integer value, creating a new Dataset. reduce is called on that Dataset to find the largest word count. The arguments to map and reduce are Scala function literals (closures), and can use any language feature or Scala/Java library. For example, we can easily call functions declared elsewhere. We’ll use Math.max() function to make this code easier to understand:

第一个 map 操作将一行数据映射为一个整型值，并创建一个新的 Dataset. 在新的 Dataset 上调用 reduce 来找到字数最多的行。

map 与 reduce 的参数是 Scala 函数的字面量闭包，并可以使用任何语言特性或者 Scala/Java 库。

例如，我们可以很容易地调用函数声明，我们将定义一个 max 函数来使代码更易于理解 :


```
scala> import java.lang.Math
import java.lang.Math

scala> textFile.map(line => line.split(" ").size).reduce((a, b) => Math.max(a, b))
res5: Int = 15
```

> One common data flow pattern is MapReduce, as popularized by Hadoop. Spark can implement MapReduce flows easily:

一种常见的数据流模式是被 Hadoop 所推广的 MapReduce

Spark 可以很容易实现 MapReduce:

```
scala> val wordCounts = textFile.flatMap(line => line.split(" ")).groupByKey(identity).count()
wordCounts: org.apache.spark.sql.Dataset[(String, Long)] = [value: string, count(1): bigint]
```

> Here, we call flatMap to transform a Dataset of lines to a Dataset of words, and then combine groupByKey and count to compute the per-word counts in the file as a Dataset of (String, Long) pairs. To collect the word counts in our shell, we can call collect:

在这里，我们调用了 flatMap 转换一个 lines 的 Dataset 为一个 words 的 Dataset, 然后结合 groupByKey 和 count 来计算文件中每个单词的计数，结果以一个 (String, Long) 的 Dataset 对的形式输出。

要在 shell 中收集 word counts，我们可以调用 collect:

```
scala> wordCounts.collect()
res6: Array[(String, Int)] = Array((means,1), (under,2), (this,3), (Because,1), (Python,2), (agree,1), (cluster.,1), ...)
```

#### Python

```
>>> from pyspark.sql.functions import *
>>> textFile.select(size(split(textFile.value, "\s+")).name("numWords")).agg(max(col("numWords"))).collect()
[Row(max(numWords)=15)]
```

> This first maps a line to an integer value and aliases it as “numWords”, creating a new DataFrame. agg is called on that DataFrame to find the largest word count. The arguments to `select` and `agg` are both [Column](https://spark.apache.org/docs/3.3.2/api/python/index.html#pyspark.sql.Column), we can use `df.colName` to get a column from a DataFrame. We can also import `pyspark.sql.functions`, which provides a lot of convenient functions to build a new Column from an old one.

首先将一行数据映射为一个名为 numWords 的整型值，并创建一个新的 DataFrame. 在新的 DataFrame 上调用 agg 方法找到字数最多的行。

select 和 agg 的参数都是 Column. 我们可以使用 `df.colName` 来获取一行。我们也可以导入 `pyspark.sql.functions`, 它提供了很多从旧的 Column 构建新 Column 的方法。

> One common data flow pattern is MapReduce, as popularized by Hadoop. Spark can implement MapReduce flows easily:

```
>>> wordCounts = textFile.select(explode(split(textFile.value, "\s+")).alias("word")).groupBy("word").count()
```

> Here, we use the `explode` function in `select`, to transform a Dataset of lines to a Dataset of words, and then combine `groupBy` and `count` to compute the per-word counts in the file as a DataFrame of 2 columns: “word” and “count”. To collect the word counts in our shell, we can call `collect`:

这里，我们在 select 使用了 explode 方法，来转换一个 lines 的 Dataset 到 words 的 Dataset。然后结合 groupBy 和 count，计算文件中每个单词的计数，生成的结果为两列的 DataFrame： word and count. 

要在 shell 中收集 word 的计数，我们可以调用 collect:

```
>>> wordCounts.collect()
[Row(word=u'online', count=1), Row(word=u'graphs', count=1), ...]
```

### Caching

> Spark also supports pulling data sets into a cluster-wide in-memory cache. This is very useful when data is accessed repeatedly, such as when querying a small “hot” dataset or when running an iterative algorithm like PageRank. As a simple example, let’s mark our `linesWithSpark` dataset to be cached:

Spark 支持将数据集拉取到一个集群范围内的内存缓存中。

在数据被重复访问时是非常高效的，例如当查询一个小的 hot 数据集或运行一个像 PageRANK 这样的迭代算法时。

举一个简单的例子，让我们拉取 linesWithSpark 数据集到缓存中:

#### Scala

```
scala> linesWithSpark.cache()
res7: linesWithSpark.type = [value: string]

scala> linesWithSpark.count()
res8: Long = 15

scala> linesWithSpark.count()
res9: Long = 15
```

> It may seem silly to use Spark to explore and cache a 100-line text file. The interesting part is that these same functions can be used on very large data sets, even when they are striped across tens or hundreds of nodes. You can also do this interactively by connecting `bin/spark-shell` to a cluster, as described in the [RDD programming guide](https://spark.apache.org/docs/3.3.2/rdd-programming-guide.html#using-the-shell).

使用 Spark 来探索和缓存一个 100 行的文本文件看起来比较愚蠢。

有趣的是，即使在他们跨越几十或者几百个节点时，这些相同的函数也可以用于非常大的数据集。也可以通过 `bin/spark-shell` 连接集群，使用交互式的方式来做这件事情。

#### Python

```
>>> linesWithSpark.cache()

>>> linesWithSpark.count()
15

>>> linesWithSpark.count()
15
```

> It may seem silly to use Spark to explore and cache a 100-line text file. The interesting part is that these same functions can be used on very large data sets, even when they are striped across tens or hundreds of nodes. You can also do this interactively by connecting `bin/pyspark` to a cluster, as described in the [RDD programming guide](https://spark.apache.org/docs/3.3.2/rdd-programming-guide.html#using-the-shell).

使用 Spark 来探索和缓存一个 100 行的文本文件看起来比较愚蠢。

有趣的是，即使在他们跨越几十或者几百个节点时，这些相同的函数也可以用于非常大的数据集。也可以通过 `bin/pyspark` 连接集群，使用交互式的方式来做这件事情。

## Self-Contained Applications

> Suppose we wish to write a self-contained application using the Spark API. We will walk through a simple application in Scala (with sbt), Java (with Maven), and Python (pip).

假设我们希望使用 Spark API 来创建一个独立的应用程序。

### Scala

We’ll create a very simple Spark application in Scala–so simple, in fact, that it’s named SimpleApp.scala:

```scala
/* SimpleApp.scala */
import org.apache.spark.sql.SparkSession

object SimpleApp {
  def main(args: Array[String]) {
    val logFile = "YOUR_SPARK_HOME/README.md" // Should be some file on your system
    val spark = SparkSession.builder.appName("Simple Application").getOrCreate()
    val logData = spark.read.textFile(logFile).cache()
    val numAs = logData.filter(line => line.contains("a")).count()
    val numBs = logData.filter(line => line.contains("b")).count()
    println(s"Lines with a: $numAs, Lines with b: $numBs")
    spark.stop()
  }
}
```

> Note that applications should define a `main()` method instead of extending `scala.App`. Subclasses of `scala.App` may not work correctly.

注意，应用程序应该定义一个 main() 方法，而不是去扩展 scala.App. 使用 scala.App 的子类可能不会正常运行。

> This program just counts the number of lines containing ‘a’ and the number containing ‘b’ in the Spark README. Note that you’ll need to replace YOUR_SPARK_HOME with the location where Spark is installed. Unlike the earlier examples with the Spark shell, which initializes its own SparkSession, we initialize a SparkSession as part of the program.

该程序仅仅统计了 Spark README 文件中包含 a 的数量和包含 b 的行数。

注意，需要将 YOUR_SPARK_HOME 替换为你安装 Spark 的位置。不像先前使用 spark shell 操作的示例，它们初始化了自己的 SparkContext, 而这里在应用程序里初始化了一个 SparkContext.

> We call `SparkSession.builder` to construct a `SparkSession`, then set the application name, and finally call `getOrCreate` to get the `SparkSession` instance.

我们调用 SparkSession.builder 构造一个 SparkSession, 然后设置应用名称，最后调用 getOrCreate 获得 SparkSession 实例。

> Our application depends on the Spark API, so we’ll also include an sbt configuration file, `build.sbt`, which explains that Spark is a dependency. This file also adds a repository that Spark depends on:

我们的应用依赖于 Spark API, 所以我们将包含一个名为 build.sbt 的 sbt 配置文件，它描述了 Spark 的依赖。

该文件也会添加一个 Spark 依赖的仓库

```
name := "Simple Project"

version := "1.0"

scalaVersion := "2.12.15"

libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.3.2"
```

> For sbt to work correctly, we’ll need to layout `SimpleApp.scala` and `build.sbt` according to the typical directory structure. Once that is in place, we can create a JAR package containing the application’s code, then use the `spark-submit` script to run our program.

为了让 sbt 正常的运行，我们需要根据典型的目录结构来布局 SimpleApp.scala 和 build.sbt 文件。

在完成后，我们可以创建一个包含应用程序代码的 JAR 包，然后使用 spark-submit 脚本来运行我们的程序。

```
# Your directory layout should look like this
$ find .
.
./build.sbt
./src
./src/main
./src/main/scala
./src/main/scala/SimpleApp.scala

# Package a jar containing your application
$ sbt package
...
[info] Packaging {..}/{..}/target/scala-2.12/simple-project_2.12-1.0.jar

# Use spark-submit to run your application
$ YOUR_SPARK_HOME/bin/spark-submit \
  --class "SimpleApp" \
  --master local[4] \
  target/scala-2.12/simple-project_2.12-1.0.jar
...
Lines with a: 46, Lines with b: 23
```

> Other dependency management tools such as Conda and pip can be also used for custom classes or third-party libraries. See also [Python Package Management](https://spark.apache.org/docs/3.3.2/api/python/user_guide/python_packaging.html).

### Java

> This example will use Maven to compile an application JAR, but any similar build system will work.

这个例子使用 Maven 编译一个 application JAR, 使用其他的构建系统也可以。

> We’ll create a very simple Spark application, SimpleApp.java:

创建一个非常简单的 Spark 应用 SimpleApp.java

```java
/* SimpleApp.java */
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.Dataset;

public class SimpleApp {
  public static void main(String[] args) {
    String logFile = "YOUR_SPARK_HOME/README.md"; // Should be some file on your system
    SparkSession spark = SparkSession.builder().appName("Simple Application").getOrCreate();
    Dataset<String> logData = spark.read().textFile(logFile).cache();

    long numAs = logData.filter(s -> s.contains("a")).count();
    long numBs = logData.filter(s -> s.contains("b")).count();

    System.out.println("Lines with a: " + numAs + ", lines with b: " + numBs);

    spark.stop();
  }
}
```

> This program just counts the number of lines containing ‘a’ and the number containing ‘b’ in the Spark README. Note that you’ll need to replace YOUR_SPARK_HOME with the location where Spark is installed. Unlike the earlier examples with the Spark shell, which initializes its own SparkSession, we initialize a SparkSession as part of the program.

这个程序计算 Spark README 文档中包含字母 a 和字母 b 的行数。

注意把 YOUR_SPARK_HOME 修改成你的 Spark 的安装目录。

和前面使用 Spark shell 运行的例子不同，我们需要在程序中初始化 SparkSession.

> To build the program, we also write a Maven `pom.xml` file that lists Spark as a dependency. Note that Spark artifacts are tagged with a Scala version.

为构建程序，把 Spark 依赖添加到 Maven 的 pom.xml 文件里。 注意 Spark 的 artifacts 使用 Scala 版本进行标记。

```xml
<project>
  <groupId>edu.berkeley</groupId>
  <artifactId>simple-project</artifactId>
  <modelVersion>4.0.0</modelVersion>
  <name>Simple Project</name>
  <packaging>jar</packaging>
  <version>1.0</version>
  <dependencies>
    <dependency> <!-- Spark dependency -->
      <groupId>org.apache.spark</groupId>
      <artifactId>spark-sql_2.12</artifactId>
      <version>3.3.2</version>
      <scope>provided</scope>
    </dependency>
  </dependencies>
</project>
```

> We lay out these files according to the canonical Maven directory structure:

按照 Maven 经典的目录结构组织这些文件：

```
$ find .
./pom.xml
./src
./src/main
./src/main/java
./src/main/java/SimpleApp.java
```

> Now, we can package the application using Maven and execute it with `./bin/spark-submit`.

现在可以用 Maven 打包这个应用，然后用 ./bin/spark-submit 执行。

```
# Package a JAR containing your application
$ mvn package
...
[INFO] Building jar: {..}/{..}/target/simple-project-1.0.jar

# Use spark-submit to run your application
$ YOUR_SPARK_HOME/bin/spark-submit \
  --class "SimpleApp" \
  --master local[4] \
  target/simple-project-1.0.jar
...
Lines with a: 46, Lines with b: 23
```

### Python

> Now we will show how to write an application using the Python API (PySpark).

现在展示如何用 python API(pyspark) 写一个应用。

> If you are building a packaged PySpark application or library you can add it to your `setup.py` file as:

如果要打包的 PySpark 应用程序或库，则可以添加以下内容到 setup.py 文件中：

    install_requires=[
        'pyspark==3.3.2'
    ]

> As an example, we’ll create a simple Spark application, SimpleApp.py:

创建一个简单的 Spark 应用 SimpleApp.py

```python
"""SimpleApp.py"""
from pyspark.sql import SparkSession

logFile = "YOUR_SPARK_HOME/README.md"  # Should be some file on your system
spark = SparkSession.builder.appName("SimpleApp").getOrCreate()
logData = spark.read.text(logFile).cache()

numAs = logData.filter(logData.value.contains('a')).count()
numBs = logData.filter(logData.value.contains('b')).count()

print("Lines with a: %i, lines with b: %i" % (numAs, numBs))

spark.stop()
```

> This program just counts the number of lines containing ‘a’ and the number containing ‘b’ in a text file. Note that you’ll need to replace YOUR_SPARK_HOME with the location where Spark is installed. As with the Scala and Java examples, we use a SparkSession to create Datasets. For applications that use custom classes or third-party libraries, we can also add code dependencies to spark-submit through its `--py-files` argument by packaging them into a `.zip` file (see `spark-submit --help` for details). SimpleApp is simple enough that we do not need to specify any code dependencies.

该程序统计该文本中包含字母 a 和包含字母 b 的行数。

注意你需要将 YOUR_SPARK_HOME 替换成你的 spark 安装路径。

就像 scala 和 java 示例一样，我们使用 SparkSession 创建数据集。

对于使用自定义类或者第三方库的应用程序，我们还可以通过带有 `--py-files` 选项为 spark-submit 命令添加代码依赖，具体就是通过把代码打成 zip 包添加。

SimpleApp 是个简单的例子，我们不需要添加特别的代码或自定义类.

> We can run this application using the `bin/spark-submit` script:

可以通过 `bin/spark-submit` 脚本来运行应用

```
# Use spark-submit to run your application
$ YOUR_SPARK_HOME/bin/spark-submit \
  --master local[4] \
  SimpleApp.py
...
Lines with a: 46, Lines with b: 23
```

> If you have PySpark pip installed into your environment (e.g., `pip install pyspark`), you can run your application with the regular Python interpreter or use the provided ‘spark-submit’ as you prefer.

如果在环境中通过 pip 安装  pyspark, 则可以使用常规 Python 解释器运行应用程序，也可以根据需要使用前面的 spark-submit 

```
# Use the Python interpreter to run your application
$ python SimpleApp.py
...
Lines with a: 46, Lines with b: 23
```

## Where to Go from Here

Congratulations on running your first Spark application!

- For an in-depth overview of the API, start with the [RDD programming guide](https://spark.apache.org/docs/3.3.2/rdd-programming-guide.html) and the [SQL programming guide](https://spark.apache.org/docs/3.3.2/sql-programming-guide.html), or see “Programming Guides” menu for other components.

- For running applications on a cluster, head to the [deployment overview](https://spark.apache.org/docs/3.3.2/cluster-overview.html).

- Finally, Spark includes several samples in the examples directory ([Scala](https://github.com/apache/spark/tree/master/examples/src/main/scala/org/apache/spark/examples), [Java](https://github.com/apache/spark/tree/master/examples/src/main/java/org/apache/spark/examples), [Python](https://github.com/apache/spark/tree/master/examples/src/main/python), [R](https://github.com/apache/spark/tree/master/examples/src/main/r)). You can run them as follows:

```
# For Scala and Java, use run-example:
./bin/run-example SparkPi

# For Python examples, use spark-submit directly:
./bin/spark-submit examples/src/main/python/pi.py

# For R examples, use spark-submit directly:
./bin/spark-submit examples/src/main/r/dataframe.R
```