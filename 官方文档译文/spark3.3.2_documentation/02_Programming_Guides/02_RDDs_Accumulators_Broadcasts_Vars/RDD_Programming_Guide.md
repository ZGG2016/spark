# RDD Programming Guide

[TOC]

## Overview

> At a high level, every Spark application consists of a driver program that runs the user’s main function and executes various parallel operations on a cluster. The main abstraction Spark provides is a resilient distributed dataset (RDD), which is a collection of elements partitioned across the nodes of the cluster that can be operated on in parallel. RDDs are created by starting with a file in the Hadoop file system (or any other Hadoop-supported file system), or an existing Scala collection in the driver program, and transforming it. Users may also ask Spark to persist an RDD in memory, allowing it to be reused efficiently across parallel operations. Finally, RDDs automatically recover from node failures.

从一个高层次的角度来看，每个 Spark 应用程序都由一个驱动程序组成，它在集群上运行着用户的 main 函数和执行着各种并行操作。

Spark 提供的主要抽象是一个弹性分布式数据集 RDD, 它是在集群节点间分区的、执行并行操作的元素集合。

RDD 可以根据 Hadoop 文件系统（或者任何其它 Hadoop 支持的文件系统）中的一个文件创建，也可以通过转换已存在的 Scala 集合创建。

为了让 RDD 在整个并行操作中能更高效的重用，Spark 能够将一个 RDD 持久化到内存中。

最后，RDD 会自动的从节点故障中恢复。

> A second abstraction in Spark is shared variables that can be used in parallel operations. By default, when Spark runs a function in parallel as a set of tasks on different nodes, it ships a copy of each variable used in the function to each task. Sometimes, a variable needs to be shared across tasks, or between tasks and the driver program. Spark supports two types of shared variables: broadcast variables, which can be used to cache a value in memory on all nodes, and accumulators, which are variables that are only “added” to, such as counters and sums.

在 Spark 中，第二个抽象是能够用于并行操作的共享变量，默认情况下，当 Spark 在不同节点上作为任务集并行运行一个函数，它会将函数中使用的每个变量的副本应用到每个任务中。有时候，一个变量需要任务间共享，或者在任务和驱动程序间共享。

Spark 支持两种类型的共享变量：广播变量和累加器。广播变量用于在所有节点上的内存中缓存一个值。累加器是一个只能被增加的变量，例如 counters 和 sums。

> This guide shows each of these features in each of Spark’s supported languages. It is easiest to follow along with if you launch Spark’s interactive shell – either `bin/spark-shell` for the Scala shell or `bin/pyspark` for the Python one.

本指南介绍了每一种 Spark 所支持的语言的特性。启动 Spark 的交互式 shell 来学习是很容易的，要么通过 `bin/spark-shell` 启动 Scala shell ，要么通过 `bin/pyspark` 启动 Python shell.

## Linking with Spark

### Scala

> Spark 3.3.2 is built and distributed to work with Scala 2.12 by default. (Spark can be built to work with other versions of Scala, too.) To write applications in Scala, you will need to use a compatible Scala version (e.g. 2.12.X).

Spark 3.3.2 默认支持的是 Scala 2.12. （也适用于其他的 Scala 版本）。为了能编写 Scala 应用程序，你需要使用一个兼容的 Scala 版本（如2.12.X）。

> To write a Spark application, you need to add a Maven dependency on Spark. Spark is available through Maven Central at:

首先添加 Spark Maven 依赖

```
groupId = org.apache.spark
artifactId = spark-core_2.12
version = 3.3.2
```

> In addition, if you wish to access an HDFS cluster, you need to add a dependency on hadoop-client for your version of HDFS.

然后，如果想访问 HDFS 集群，还需要添加对于你的 HDFS 版本的 hadoop-client 依赖

```
groupId = org.apache.hadoop
artifactId = hadoop-client
version = <your-hdfs-version>
```

> Finally, you need to import some Spark classes into your program. Add the following lines:

最后，在程序里导入 Spark 类

```
import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
```

> (Before Spark 1.3.0, you need to explicitly `import org.apache.spark.SparkContext._` to enable essential implicit conversions.)

在 Spark 1.3.0 之前，你需要明确地导入 `org.apache.spark.SparkContext._` 以启用必要的隐式转换。

### Java

> Spark 3.3.2 supports [lambda expressions](http://docs.oracle.com/javase/tutorial/java/javaOO/lambdaexpressions.html) for concisely writing functions, otherwise you can use the classes in the [org.apache.spark.api.java.function](https://spark.apache.org/docs/3.3.2/api/java/index.html?org/apache/spark/api/java/function/package-summary.html) package.

Spark 3.3.2 支持使用 lambda 表达式写函数，也可以使用 `org.apache.spark.api.java.function` 包中的类。 

> Note that support for Java 7 was removed in Spark 2.2.0.

注意： 对 Java 7 的支持已在 Spark 2.2.0 中已移除。

> To write a Spark application in Java, you need to add a dependency on Spark. Spark is available through Maven Central at:

首先添加 Spark Maven 依赖

```
groupId = org.apache.spark
artifactId = spark-core_2.12
version = 3.3.2
```

> In addition, if you wish to access an HDFS cluster, you need to add a dependency on hadoop-client for your version of HDFS.

然后，如果想访问 HDFS 集群，还需要添加对于你的 HDFS 版本的 hadoop-client 依赖

```
groupId = org.apache.hadoop
artifactId = hadoop-client
version = <your-hdfs-version>
```

> Finally, you need to import some Spark classes into your program. Add the following lines:

最后，在程序里导入 Spark 类

```
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.SparkConf;
```

### Python

> Spark 3.3.2 works with Python 3.7+. It can use the standard CPython interpreter, so C libraries like NumPy can be used. It also works with PyPy 7.3.6+.

Spark 3.3.2 支持 Python 3.7+. 可以使用标准的 CPython 解释器，那么像 NumPy 一样的 C 库就可以使用了。同时也支持 PyPy 2.3+.

> Python 2, 3.4 and 3.5 supports were removed in Spark 3.1.0. Python 3.6 support was removed in Spark 3.3.0.

在 Spark 3.1.0 版本中，Python 2, 3.4 and 3.5 被弃用了。在 Spark 3.3.0 中将移除对 Python 3.6 的支持。

> Spark applications in Python can either be run with the `bin/spark-submit` script which includes Spark at runtime, or by including it in your `setup.py` as:

在 Python 中，配置运行 Spark 应用程序所需信息既可以在 `bin/spark-submit` 脚本中添加，也可以在 `setup.py` 中添加如下内容：

```
    install_requires=[
        'pyspark=={site.SPARK_VERSION}'
    ]
```

> To run Spark applications in Python without pip installing PySpark, use the `bin/spark-submit` script located in the Spark directory. This script will load Spark’s Java/Scala libraries and allow you to submit applications to a cluster. You can also use `bin/pyspark` to launch an interactive Python shell.

如果不是通过 pip 安装的 PySpark, 可以使用 Spark 目录下的 `bin/spark-submit` 脚本运行 Spark 应用程序。这个脚本会载入 Spark 的 Java/Scala 库，并向集群提交应用程序。你也可以使用 `bin/pyspark` 启动一个 Python shell.

> If you wish to access HDFS data, you need to use a build of PySpark linking to your version of HDFS. [Prebuilt packages](https://spark.apache.org/downloads.html) are also available on the Spark homepage for common HDFS versions.

如果你想访问 HDFS 中的数据，需要 PySpark 的版本和你的 HDFS 版本相同。

> Finally, you need to import some Spark classes into your program. Add the following line:

最后，在程序里导入 Spark 类

```
from pyspark import SparkContext, SparkConf
```

> PySpark requires the same minor version of Python in both driver and workers. It uses the default python version in PATH, you can specify which version of Python you want to use by `PYSPARK_PYTHON`, for example:

在 driver 和 workers 中，PySpark 都需要使用相同的 Python 版本。 它使用 PATH 中的默认版本，但你可以通过设置 `PYSPARK_PYTHON` 指定一个版本。

```
$ PYSPARK_PYTHON=python3.8 bin/pyspark
$ PYSPARK_PYTHON=/path-to-your-pypy/pypy bin/spark-submit examples/src/main/python/pi.py
```

## Initializing Spark

### Scala

> The first thing a Spark program must do is to create a [SparkContext](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/SparkContext.html) object, which tells Spark how to access a cluster. To create a SparkContext you first need to build a [SparkConf](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/SparkConf.html) object that contains information about your application.

Spark 程序首先要做的事情是创建一个 `SparkContext` 对象，它会告诉 Spark 如何访问集群。要创建一个 `SparkContext`，首先需要构建一个包含应用程序的信息的 `SparkConf` 对象。

> Only one `SparkContext` should be active per JVM. You must `stop()` the active `SparkContext` before creating a new one.

在每个 JVM 中，仅需要一个 `SparkContext` 是活跃的。你需要在创建一个新的 `SparkContext` 前，停止现在活跃的。

```scala
val conf = new SparkConf().setAppName(appName).setMaster(master)
new SparkContext(conf)
```

> The `appName` parameter is a name for your application to show on the cluster UI. `master` is a [Spark, Mesos or YARN cluster URL](https://spark.apache.org/docs/3.3.2/submitting-applications.html#master-urls), or a special “local” string to run in local mode. In practice, when running on a cluster, you will not want to hardcode `master` in the program, but rather [launch the application with spark-submit](https://spark.apache.org/docs/3.3.2/submitting-applications.html) and receive it there. However, for local testing and unit tests, you can pass “local” to run Spark in-process.

`appName` 参数是你的应用程序的名称，是在集群 UI 上显示的名称。 

`master` 可以是 Spark、 Mesos、YARN cluster URL、 或 local(本地模式)。

在实践中，当在集群上运行程序时，你不希望在程序中硬编码 `master`，而是使用 `spark-submit` 启动应用程序，并接收 `master`。然而，对于本地测试和单元测试，可以通过 `local` 运行 Spark 进程。

### Java

> The first thing a Spark program must do is to create a [JavaSparkContext](https://spark.apache.org/docs/3.3.2/api/java/index.html?org/apache/spark/api/java/JavaSparkContext.html) object, which tells Spark how to access a cluster. To create a `SparkContext` you first need to build a [SparkConf](https://spark.apache.org/docs/3.3.2/api/java/index.html?org/apache/spark/SparkConf.html) object that contains information about your application.

Spark 程序首先要做的事情是创建一个 `JavaSparkContext` 对象，它会告诉 Spark 如何访问集群。要创建一个 `SparkContext`，首先需要构建一个包含应用程序的信息的 `SparkConf` 对象。

```java
SparkConf conf = new SparkConf().setAppName(appName).setMaster(master);
JavaSparkContext sc = new JavaSparkContext(conf);
```

> The `appName` parameter is a name for your application to show on the cluster UI. `master` is a [Spark, Mesos or YARN cluster URL](https://spark.apache.org/docs/3.3.2/submitting-applications.html#master-urls), or a special “local” string to run in local mode. In practice, when running on a cluster, you will not want to hardcode `master` in the program, but rather [launch the application with spark-submit](https://spark.apache.org/docs/3.3.2/submitting-applications.html) and receive it there. However, for local testing and unit tests, you can pass “local” to run Spark in-process.

`appName` 参数是你的应用程序的名称，是在集群 UI 上显示的名称。 

`master` 可以是 Spark、 Mesos、YARN cluster URL、 或 local(本地模式)。

在实践中，当在集群上运行程序时，你不希望在程序中硬编码 `master`，而是使用 `spark-submit` 启动应用程序，并接收 `master`。然而，对于本地测试和单元测试，可以通过 `local` 运行 Spark 进程。

### Python

> The first thing a Spark program must do is to create a [SparkContext](https://spark.apache.org/docs/3.3.2/api/python/reference/api/pyspark.SparkContext.html#pyspark.SparkContext) object, which tells Spark how to access a cluster. To create a SparkContext you first need to build a [SparkConf](https://spark.apache.org/docs/3.3.2/api/python/reference/api/pyspark.SparkConf.html#pyspark.SparkConf) object that contains information about your application.

Spark 程序首先要做的事情是创建一个 `SparkContext` 对象，它会告诉 Spark 如何访问集群。要创建一个 `SparkContext`，首先需要构建一个包含应用程序的信息的 `SparkConf` 对象。

```python
conf = SparkConf().setAppName(appName).setMaster(master)
sc = SparkContext(conf=conf)
```

> The `appName` parameter is a name for your application to show on the cluster UI. master is a [Spark, Mesos or YARN cluster URL](https://spark.apache.org/docs/3.3.2/submitting-applications.html#master-urls), or a special “local” string to run in local mode. In practice, when running on a cluster, you will not want to hardcode `master` in the program, but rather [launch the application with spark-submit](https://spark.apache.org/docs/3.3.2/submitting-applications.html) and receive it there. However, for local testing and unit tests, you can pass “local” to run Spark in-process.

`appName` 参数是你的应用程序的名称，是在集群 UI 上显示的名称。 

`master` 可以是 Spark、 Mesos、YARN cluster URL、 或 local(本地模式)。

在实践中，当在集群上运行程序时，你不希望在程序中硬编码 `master`，而是使用 `spark-submit` 启动应用程序，并接收 `master`。然而，对于本地测试和单元测试，可以通过 `local` 运行 Spark 进程。

### Using the Shell

#### Scala

> In the Spark shell, a special interpreter-aware SparkContext is already created for you, in the variable called `sc`. Making your own SparkContext will not work. You can set which master the context connects to using the `--master` argument, and you can add JARs to the classpath by passing a comma-separated list to the `--jars` argument. You can also add dependencies (e.g. Spark Packages) to your shell session by supplying a comma-separated list of Maven coordinates to the `--packages` argument. Any additional repositories where dependencies might exist (e.g. Sonatype) can be passed to the `--repositories` argument. For example, to run `bin/spark-shell` on exactly four cores, use:

在 Spark Shell 中，有一个称为 `sc` 的内置 SparkContext 可供使用，但不能自己再创建一个 SparkContext 了。

相关启动参数如下：

`--master` 参数用来设置这个 SparkContext 连接到哪一个 master 上。

`--jars` 参数用来在类路径中添加 JARS 文件，通过传递逗号分隔的列表。

`--packages ` 参数可以为 shell 会话指定一些依赖(如 Spark包)，通过提供逗号分隔 Maven coordinates(坐标) 列表。

`--repositories` 参数设置任意额外存在且依赖的仓库（例如 Sonatype）

例如，使用四个核运行 `bin/spark-shell`:

```
$ ./bin/spark-shell --master local[4]
```

> Or, to also add `code.jar` to its classpath, use:

往类路径上添加 `code.jar`

```
$ ./bin/spark-shell --master local[4] --jars code.jar
```

> To include a dependency using Maven coordinates:

使用 Maven 坐标包含依赖

```
$ ./bin/spark-shell --master local[4] --packages "org.example:example:0.1"
```

> For a complete list of options, run `spark-shell --help`. Behind the scenes, `spark-shell` invokes the more general [spark-submit script](https://spark.apache.org/docs/3.3.2/submitting-applications.html).

所有的选项，请运行 `spark-shell --help` 。在后台，`spark-shell` 调用更通用的 `spark-submit` 脚本 。

#### Python

> In the PySpark shell, a special interpreter-aware SparkContext is already created for you, in the variable called `sc`. Making your own SparkContext will not work. You can set which master the context connects to using the `--master` argument, and you can add Python `.zip`, `.egg` or `.py` files to the runtime path by passing a comma-separated list to `--py-files`. For third-party Python dependencies, see [Python Package Management](https://spark.apache.org/docs/3.3.2/api/python/user_guide/python_packaging.html). You can also add dependencies (e.g. Spark Packages) to your shell session by supplying a comma-separated list of Maven coordinates to the `--packages` argument. Any additional repositories where dependencies might exist (e.g. Sonatype) can be passed to the `--repositories` argument. For example, to run `bin/pyspark` on exactly four cores, use:

在 Spark Shell 中，有一个称为 `sc` 的内置 SparkContext 可供使用，但不能自己再创建一个 SparkContext 了。

相关启动参数如下：

`--master` 参数用来设置这个 SparkContext 连接到哪一个 master 上。

`--py-files` 参数用来在运行时的路径上指定 Python `.zip`, `.egg` 或 `.py` 文件，通过传递逗号分隔的列表。

`--packages ` 参数可以为 shell 会话指定一些依赖(如 Spark包)，通过提供逗号分隔 Maven coordinates(坐标) 列表。

`--repositories` 参数设置任意额外存在且依赖的仓库（例如 Sonatype）

例如，使用四个核运行 `bin/pyspark`:

```
$ ./bin/pyspark --master local[4]
```

> Or, to also add `code.py` to the search path (in order to later be able to import code), use:

向搜索路径添加 `code.py` (为了之后导入 code) 

```
$ ./bin/pyspark --master local[4] --py-files code.py
```

> For a complete list of options, run `pyspark --help`. Behind the scenes, `pyspark` invokes the more general [spark-submit script](https://spark.apache.org/docs/3.3.2/submitting-applications.html).

所有的选项，请运行 `pyspark --help` 。在后台，`pyspark` 调用更通用的 `spark-submit` 脚本 。

> It is also possible to launch the PySpark shell in [IPython](http://ipython.org/), the enhanced Python interpreter. PySpark works with IPython 1.0.0 and later. To use IPython, set the `PYSPARK_DRIVER_PYTHON` variable to `ipython` when running `bin/pyspark`:

也可以在 IPython 中启动 PySpark shell，要求 IPython 的版本是 1.0.0 及更高。要使用 IPython, 在运行 `bin/pyspark` 时，需要设置 `PYSPARK_DRIVER_PYTHON` 变量为 `ipython`

```
$ PYSPARK_DRIVER_PYTHON=ipython ./bin/pyspark
```

> To use the Jupyter notebook (previously known as the IPython notebook),

使用 Jupyter notebook 需要作如下配置

```
$ PYSPARK_DRIVER_PYTHON=jupyter PYSPARK_DRIVER_PYTHON_OPTS=notebook ./bin/pyspark
```

> You can customize the `ipython` or `jupyter` commands by setting `PYSPARK_DRIVER_PYTHON_OPTS`.

你可以通过设置 `PYSPARK_DRIVER_PYTHON_OPTS` 参数自定义 ipython 或 jupyter 命令

> After the Jupyter Notebook server is launched, you can create a new notebook from the “Files” tab. Inside the notebook, you can input the command `%pylab inline` as part of your notebook before you start to try Spark from the Jupyter notebook.

Jupyter Notebook 服务启动后，你可以点击 “Files” 创建一个新的 “Python 2” 笔记本。在开始使用 Spark 前，需要在你的笔记本里输入 `%pylab inline` 。

## Resilient Distributed Datasets (RDDs)

> Spark revolves around the concept of a resilient distributed dataset (RDD), which is a fault-tolerant collection of elements that can be operated on in parallel. There are two ways to create RDDs: parallelizing an existing collection in your driver program, or referencing a dataset in an external storage system, such as a shared filesystem, HDFS, HBase, or any data source offering a Hadoop InputFormat.

Spark 以一个弹性分布式数据集 RDD 的概念为核心，它是一个容错、可以执行并行操作的元素的集合。

有两种方法可以创建 RDD：在你的驱动程序中 parallelizing 一个已存在的集合，或者在外部存储系统中引用一个数据集，例如，一个共享文件系统、HDFS、HBase、或者提供 Hadoop InputFormat 的任何数据源。

### Parallelized Collections

#### Scala

> Parallelized collections are created by calling `SparkContext’s parallelize` method on an existing collection in your driver program (a Scala Seq). The elements of the collection are copied to form a distributed dataset that can be operated on in parallel. For example, here is how to create a parallelized collection holding the numbers 1 to 5:

在驱动程序中，使用 SparkContext 的 parallelize 方法在已存在的集合上创建并行集合。复制集合元素，以形成执行并行操作的分布式数据集。

```scala
val data = Array(1, 2, 3, 4, 5)
val distData = sc.parallelize(data)
```

> Once created, the distributed dataset (`distData`) can be operated on in parallel. For example, we might call `distData.reduce((a, b) => a + b)` to add up the elements of the array. We describe operations on distributed datasets later on.

分布式数据集一旦创建，就可以执行并行操作。例如，可以这样使用 `distData.reduce(lambda a, b: a + b)` 累加元素。

> One important parameter for parallel collections is the number of partitions to cut the dataset into. Spark will run one task for each partition of the cluster. Typically you want 2-4 partitions for each CPU in your cluster. Normally, Spark tries to set the number of partitions automatically based on your cluster. However, you can also set it manually by passing it as a second parameter to `parallelize` (e.g. `sc.parallelize(data, 10)`). Note: some places in the code use the term slices (a synonym for partitions) to maintain backward compatibility.

并行集合的一个重要参数就是分区数量。 Spark 的一个任务操作一个分区。通常每个 CPU 划分2-4个分区。 正常情况下，Spark 会根据集群情况，自动设置分区数量。

然而，你也可以给 parallelize 方法传递一个参数来手动设置。 (如 `sc.parallelize(data, 10)`)。

注意：代码中的一些地方使用术语 slice(分区的同义词)来维护向后兼容性。

#### Java

> Parallelized collections are created by calling `JavaSparkContext’s parallelize` method on an existing `Collection` in your driver program. The elements of the collection are copied to form a distributed dataset that can be operated on in parallel. For example, here is how to create a parallelized collection holding the numbers 1 to 5:

在驱动程序中，使用 JavaSparkContext 的 parallelize 方法在已存在的集合上创建并行集合。复制集合元素，以形成执行并行操作的分布式数据集。

```java
List<Integer> data = Arrays.asList(1, 2, 3, 4, 5);
JavaRDD<Integer> distData = sc.parallelize(data);
```

> Once created, the distributed dataset (`distData`) can be operated on in parallel. For example, we might call `distData.reduce((a, b) -> a + b)` to add up the elements of the list. We describe operations on distributed datasets later on.

分布式数据集一旦创建，就可以执行并行操作。例如，可以这样使用 `distData.reduce(lambda a, b: a + b)` 累加元素。

> One important parameter for parallel collections is the number of partitions to cut the dataset into. Spark will run one task for each partition of the cluster. Typically you want 2-4 partitions for each CPU in your cluster. Normally, Spark tries to set the number of partitions automatically based on your cluster. However, you can also set it manually by passing it as a second parameter to `parallelize` (e.g. `sc.parallelize(data, 10)`). Note: some places in the code use the term slices (a synonym for partitions) to maintain backward compatibility.

并行集合的一个重要参数就是分区数量。 Spark 的一个任务操作一个分区。通常每个 CPU 划分2-4个分区。 正常情况下，Spark 会根据集群情况，自动设置分区数量。

然而，你也可以给 parallelize 方法传递一个参数来手动设置。 (如 `sc.parallelize(data, 10)`)。

注意：代码中的一些地方使用术语 slice(分区的同义词)来维护向后兼容性。

#### Python

> Parallelized collections are created by calling `SparkContext’s parallelize` method on an existing iterable or collection in your driver program. The elements of the collection are copied to form a distributed dataset that can be operated on in parallel. For example, here is how to create a parallelized collection holding the numbers 1 to 5:

在驱动程序中，使用 SparkContext 的 parallelize 方法在已存在的迭代器或集合上创建并行集合。复制集合元素，以形成执行并行操作的分布式数据集。


```python
data = [1, 2, 3, 4, 5]
distData = sc.parallelize(data)
```

> Once created, the distributed dataset (`distData`) can be operated on in parallel. For example, we can call `distData.reduce(lambda a, b: a + b)` to add up the elements of the list. We describe operations on distributed datasets later on.

分布式数据集一旦创建，就可以执行并行操作。例如，可以这样使用 `distData.reduce(lambda a, b: a + b)` 累加元素。

> One important parameter for parallel collections is the number of partitions to cut the dataset into. Spark will run one task for each partition of the cluster. Typically you want 2-4 partitions for each CPU in your cluster. Normally, Spark tries to set the number of partitions automatically based on your cluster. However, you can also set it manually by passing it as a second parameter to `parallelize` (e.g. `sc.parallelize(data, 10)`). Note: some places in the code use the term slices (a synonym for partitions) to maintain backward compatibility.

并行集合的一个重要参数就是分区数量。 Spark 的一个任务操作一个分区。通常每个 CPU 划分2-4个分区。 正常情况下，Spark 会根据集群情况，自动设置分区数量。

然而，你也可以给 parallelize 方法传递一个参数来手动设置。 (如 `sc.parallelize(data, 10)`)。

注意：代码中的一些地方使用术语 slice(分区的同义词)来维护向后兼容性。

### External Datasets

#### Scala

> Spark can create distributed datasets from any storage source supported by Hadoop, including your local file system, HDFS, Cassandra, HBase, [Amazon S3](http://wiki.apache.org/hadoop/AmazonS3), etc. Spark supports text files, [SequenceFiles](https://hadoop.apache.org/docs/stable/api/org/apache/hadoop/mapred/SequenceFileInputFormat.html), and any other Hadoop [InputFormat](http://hadoop.apache.org/docs/stable/api/org/apache/hadoop/mapred/InputFormat.html).

Spark 可以从 Hadoop 所支持的任何存储源中创建分布式数据集，包括本地文件系统、HDFS、Cassandra、HBase、Amazon S3 等等。

Spark 支持文本文件、SequenceFiles、以及任何其它的 Hadoop InputFormat.

> Text file RDDs can be created using `SparkContext’s textFile` method. This method takes a URI for the file (either a local path on the machine, or a `hdfs://`, `s3a://`, etc URI) and reads it as a collection of lines. Here is an example invocation:

可以使用 SparkContext 的 textFile 方法创建文本文件的 RDD. 此方法需要一个文件的 URI
（要么是计算机上的本地路径，要么是 `hdfs://`，`s3n://` 等 URI），并作为一个行的集合读取它们。

```scala
scala> val distFile = sc.textFile("data.txt")
distFile: org.apache.spark.rdd.RDD[String] = data.txt MapPartitionsRDD[10] at textFile at <console>:26
```

> Once created, `distFile` can be acted on by dataset operations. For example, we can add up the sizes of all the lines using the map and reduce operations as follows: `distFile.map(s => s.length).reduce((a, b) => a + b)`.

一旦创建分布式数据集，便可以对其进行操作。例如，我们可以使用下面的 map 和 reduce 操作来统计行的数量：`distFile.map(s -> s.length()).reduce((a, b) -> a + b)`.

> Some notes on reading files with Spark:

使用 Spark 读文件需要注意以下几点：

> If using a path on the local filesystem, the file must also be accessible at the same path on worker nodes. Either copy the file to all workers or use a network-mounted shared file system.

- 如果读取本地文件系统上的文件，那么在所有的工作节点上，文件的路径也相同。要么将文件复制到所有工作节点上，要么使用网络挂载的共享文件系统。

> All of Spark’s file-based input methods, including `textFile`, support running on directories, compressed files, and wildcards as well. For example, you can use `textFile("/my/directory")`, `textFile("/my/directory/*.txt")`, and `textFile("/my/directory/*.gz")`. When multiple files are read, the order of the partitions depends on the order the files are returned from the filesystem. It may or may not, for example, follow the lexicographic ordering of the files by path. Within a partition, elements are ordered according to their order in the underlying file.

- 包括 `textFile` 在内的所有基于文件的输入方法，均支持读取目录、压缩文件，和通配符匹配。例如，`textFile("/my/directory")`, `textFile("/my/directory/*.txt")`, 和 `textFile("/my/directory/*.gz")`.

> The `textFile` method also takes an optional second argument for controlling the number of partitions of the file. By default, Spark creates one partition for each block of the file (blocks being 128MB by default in HDFS), but you can also ask for a higher number of partitions by passing a larger value. Note that you cannot have fewer partitions than blocks.

- `textFile` 方法也有一个设置文件分区数的可选的第二个参数项。默认情况下，Spark 为文件的一个块（HDFS 中块大小默认是 128MB）创建一个分区，但你可以手动设置一个更大的分区数，但不能比块的数量还少。 

> Apart from text files, Spark’s Scala API also supports several other data formats:

除了文本文件之外，Spark 的 Scala API 也支持一些其它的数据格式:

> `SparkContext.wholeTextFiles` lets you read a directory containing multiple small text files, and returns each of them as (filename, content) pairs. This is in contrast with `textFile`, which would return one record per line in each file. Partitioning is determined by data locality which, in some cases, may result in too few partitions. For those cases, `wholeTextFiles` provides an optional second argument for controlling the minimal number of partitions.

- `SparkContext.wholeTextFiles` 可以读取包含多个小文本文件的目录，并且将它们作为一个 (filename, content) 对返回。而 `textFile` 是将文件中的每一行返回一个记录。分区是由数据位置决定的，在某些情况下，这可能导致分区过少。对于这些情况，`wholeTextFiles` 提供了可选的第二个参数，来控制分区的最小数量。

> For [SequenceFiles](https://hadoop.apache.org/docs/stable/api/org/apache/hadoop/mapred/SequenceFileInputFormat.html), use SparkContext’s `sequenceFile[K, V]` method where K and V are the types of key and values in the file. These should be subclasses of Hadoop’s [Writable](https://hadoop.apache.org/docs/stable/api/org/apache/hadoop/io/Writable.html) interface, like [IntWritable](https://hadoop.apache.org/docs/stable/api/org/apache/hadoop/io/IntWritable.html) and [Text](https://hadoop.apache.org/docs/stable/api/org/apache/hadoop/io/Text.html). In addition, Spark allows you to specify native types for a few common Writables; for example, `sequenceFile[Int, String]` will automatically read IntWritables and Texts.

- 对于 `SequenceFiles`，使用 SparkContext 的 `sequenceFile[K, V]` 方法读取数据，其中 K 和 V 指的是文件中键和值的类型。这些类型应该是 Hadoop 的 Writable 接口的子类，例如 IntWritable 和 Text. 此外，Spark 可以让你为一些常见的 Writables 指定原生类型; 例如，`sequenceFile[Int, String]` 会自动读取 IntWritables 和 Texts.

> For other Hadoop InputFormats, you can use the `SparkContext.hadoopRDD` method, which takes an arbitrary `JobConf` and input format class, key class and value class. Set these the same way you would for a Hadoop job with your input source. You can also use `SparkContext.newAPIHadoopRDD` for InputFormats based on the “new” MapReduce API (`org.apache.hadoop.mapreduce`).

- 针对其它的 Hadoop InputFormats, 你可以使用 `SparkContext.hadoopRDD` 方法，它接受一个任意的 `JobConf` 和输入格式类、 键类和值类。设置这些的方式与使用输入源设置 Hadoop job 相同。你还可以针对 InputFormats 使用基于新的 MapReduce API(`org.apache.hadoop.mapreduce`) 的 `SparkContext.newAPIHadoopRDD`.

> `RDD.saveAsObjectFile` and `SparkContext.objectFile` support saving an RDD in a simple format consisting of serialized Java objects. While this is not as efficient as specialized formats like Avro, it offers an easy way to save any RDD.

- `RDD.saveAsObjectFile` 和 `SparkContext.objectFile` 可以以序列化的 Java 对象的格式存储一个 RDD. 虽然这不如 Avro 等专门格式高效，但它提供了一种简单的方法来保存任意 RDD.

#### Java

> Spark can create distributed datasets from any storage source supported by Hadoop, including your local file system, HDFS, Cassandra, HBase, [Amazon S3](http://wiki.apache.org/hadoop/AmazonS3), etc. Spark supports text files, [SequenceFiles](https://hadoop.apache.org/docs/stable/api/org/apache/hadoop/mapred/SequenceFileInputFormat.html), and any other Hadoop [InputFormat](http://hadoop.apache.org/docs/stable/api/org/apache/hadoop/mapred/InputFormat.html).

Spark 可以从 Hadoop 所支持的任何存储源中创建分布式数据集，包括本地文件系统、HDFS、Cassandra、HBase、Amazon S3 等等。

Spark 支持文本文件、SequenceFiles、以及任何其它的 Hadoop InputFormat.

> Text file RDDs can be created using `SparkContext’s textFile` method. This method takes a URI for the file (either a local path on the machine, or a `hdfs://`, `s3a://`, etc URI) and reads it as a collection of lines. Here is an example invocation:

可以使用 SparkContext 的 textFile 方法创建文本文件的 RDD. 此方法需要一个文件的 URI
（要么是计算机上的本地路径，要么是 `hdfs://`，`s3n://` 等 URI），并作为一个行的集合读取它们。

```java
JavaRDD<String> distFile = sc.textFile("data.txt");
```

> Once created, `distFile` can be acted on by dataset operations. For example, we can add up the sizes of all the lines using the map and reduce operations as follows: `distFile.map(s -> s.length()).reduce((a, b) -> a + b)`.

一旦创建分布式数据集，便可以对其进行操作。例如，我们可以使用下面的 map 和 reduce 操作来统计行的数量：`distFile.map(s -> s.length()).reduce((a, b) -> a + b)`.

> Some notes on reading files with Spark:

使用 Spark 读文件需要注意以下几点：

> If using a path on the local filesystem, the file must also be accessible at the same path on worker nodes. Either copy the file to all workers or use a network-mounted shared file system.

- 如果读取本地文件系统上的文件，那么在所有的工作节点上，文件的路径也相同。要么将文件复制到所有工作节点上，要么使用网络挂载的共享文件系统。

> All of Spark’s file-based input methods, including `textFile`, support running on directories, compressed files, and wildcards as well. For example, you can use `textFile("/my/directory")`, `textFile("/my/directory/*.txt")`, and `textFile("/my/directory/*.gz")`.

- 包括 `textFile` 在内的所有基于文件的输入方法，均支持读取目录、压缩文件，和通配符匹配。例如，`textFile("/my/directory")`, `textFile("/my/directory/*.txt")`, 和 `textFile("/my/directory/*.gz")`.

> The `textFile` method also takes an optional second argument for controlling the number of partitions of the file. By default, Spark creates one partition for each block of the file (blocks being 128MB by default in HDFS), but you can also ask for a higher number of partitions by passing a larger value. Note that you cannot have fewer partitions than blocks.

- `textFile` 方法也有一个设置文件分区数的可选的第二个参数项。默认情况下，Spark 为文件的一个块（HDFS 中块大小默认是 128MB）创建一个分区，但你可以手动设置一个更大的分区数，但不能比块的数量还少。 

> Apart from text files, Spark’s Java API also supports several other data formats:

除了文本文件之外，Spark 的 Java API 也支持一些其它的数据格式:

> `JavaSparkContext.wholeTextFiles` lets you read a directory containing multiple small text files, and returns each of them as (filename, content) pairs. This is in contrast with textFile, which would return one record per line in each file.

- `JavaSparkContext.wholeTextFiles` 可以读取包含多个小文本文件的目录，并且将它们作为一个 (filename, content) 对返回。而 `textFile` 是将文件中的每一行返回一个记录。

> For [SequenceFiles](https://hadoop.apache.org/docs/stable/api/org/apache/hadoop/mapred/SequenceFileInputFormat.html), use SparkContext’s `sequenceFile[K, V]` method where K and V are the types of key and values in the file. These should be subclasses of Hadoop’s [Writable](https://hadoop.apache.org/docs/stable/api/org/apache/hadoop/io/Writable.html) interface, like [IntWritable](https://hadoop.apache.org/docs/stable/api/org/apache/hadoop/io/IntWritable.html) and [Text](https://hadoop.apache.org/docs/stable/api/org/apache/hadoop/io/Text.html).

- 对于 `SequenceFiles`，使用 SparkContext 的 `sequenceFile[K, V]` 方法读取数据，其中 K 和 V 指的是文件中键和值的类型。这些类型应该是 Hadoop 的 Writable 接口的子类，例如 IntWritable 和 Text. 

> For other Hadoop InputFormats, you can use the `JavaSparkContext.hadoopRDD` method, which takes an arbitrary JobConf and input format class, key class and value class. Set these the same way you would for a Hadoop job with your input source. You can also use `JavaSparkContext.newAPIHadoopRDD` for InputFormats based on the “new” MapReduce API (`org.apache.hadoop.mapreduce`).

- 针对其它的 Hadoop InputFormats, 你可以使用 `SparkContext.hadoopRDD` 方法，它接受一个任意的 `JobConf` 和输入格式类、 键类和值类。设置这些的方式与使用输入源设置 Hadoop job 相同。你还可以针对 InputFormats 使用基于新的 MapReduce API(`org.apache.hadoop.mapreduce`) 的 `SparkContext.newAPIHadoopRDD`.

> `JavaRDD.saveAsObjectFile` and `JavaSparkContext.objectFile` support saving an RDD in a simple format consisting of serialized Java objects. While this is not as efficient as specialized formats like Avro, it offers an easy way to save any RDD.

- `JavaRDD.saveAsObjectFile` 和 `JavaSparkContext.objectFile` 可以以序列化的 Java 对象的格式存储一个 RDD. 虽然这不如 Avro 等专门格式高效，但它提供了一种简单的方法来保存任意 RDD.

#### Python

> PySpark can create distributed datasets from any storage source supported by Hadoop, including your local file system, HDFS, Cassandra, HBase, [Amazon S3](http://wiki.apache.org/hadoop/AmazonS3), etc. Spark supports text files, [SequenceFiles](https://hadoop.apache.org/docs/stable/api/org/apache/hadoop/mapred/SequenceFileInputFormat.html), and any other Hadoop [InputFormat](http://hadoop.apache.org/docs/stable/api/org/apache/hadoop/mapred/InputFormat.html).

PySpark 可以从 Hadoop 所支持的任何存储源中创建分布式数据集，包括本地文件系统、HDFS、Cassandra、HBase、Amazon S3 等等。

Spark 支持文本文件、SequenceFiles、以及任何其它的 Hadoop InputFormat.

> Text file RDDs can be created using `SparkContext’s textFile` method. This method takes a URI for the file (either a local path on the machine, or a `hdfs://`, `s3a://`, etc URI) and reads it as a collection of lines. Here is an example invocation:

可以使用 SparkContext 的 textFile 方法创建文本文件的 RDD. 此方法需要一个文件的 URI （要么是计算机上的本地路径，要么是 `hdfs://`，`s3n://` 等 URI），并作为一个行的集合读取它们。

```
>>> distFile = sc.textFile("data.txt")
```

> Once created, `distFile` can be acted on by dataset operations. For example, we can add up the sizes of all the lines using the map and reduce operations as follows: `distFile.map(lambda s: len(s)).reduce(lambda a, b: a + b)`.

一旦创建分布式数据集，便可以对其进行操作。例如，我们可以使用下面的 map 和 reduce 操作来统计行的数量：`distFile.map(lambda s: len(s)).reduce(lambda a, b: a + b)`.

> Some notes on reading files with Spark:

使用 Spark 读文件需要注意以下几点：

> If using a path on the local filesystem, the file must also be accessible at the same path on worker nodes. Either copy the file to all workers or use a network-mounted shared file system.

- 如果读取本地文件系统上的文件，那么在所有的工作节点上，文件的路径也相同。要么将文件复制到所有工作节点上，要么使用网络挂载的共享文件系统。

> All of Spark’s file-based input methods, including `textFile`, support running on directories, compressed files, and wildcards as well. For example, you can use `textFile("/my/directory")`, `textFile("/my/directory/*.txt")`, and `textFile("/my/directory/*.gz")`.

- 包括 `textFile` 在内的所有基于文件的输入方法，均支持读取目录、压缩文件，和通配符匹配。例如，`textFile("/my/directory")`, `textFile("/my/directory/*.txt")`, 和 `textFile("/my/directory/*.gz")`.

> The `textFile` method also takes an optional second argument for controlling the number of partitions of the file. By default, Spark creates one partition for each block of the file (blocks being 128MB by default in HDFS), but you can also ask for a higher number of partitions by passing a larger value. Note that you cannot have fewer partitions than blocks.

- `textFile` 方法也有一个设置文件分区数的可选的第二个参数项。默认情况下，Spark 为文件的一个块（HDFS 中块大小默认是 128MB）创建一个分区，但你可以手动设置一个更大的分区数，但不能比块的数量还少。

> Apart from text files, Spark’s Python API also supports several other data formats:

除了文本文件之外，Spark 的 Python API 也支持一些其它的数据格式:

> `SparkContext.wholeTextFiles` lets you read a directory containing multiple small text files, and returns each of them as (filename, content) pairs. This is in contrast with textFile, which would return one record per line in each file.

- `SparkContext.wholeTextFiles` 可以读取包含多个小文本文件的目录，并且将它们作为一个 (filename, content) 对返回。而 textFile 是将文件中的每一行返回一个记录。

> `RDD.saveAsPickleFile` and `SparkContext.pickleFile` support saving an RDD in a simple format consisting of pickled Python objects. Batching is used on pickle serialization, with default batch size 10.

- `RDD.saveAsPickleFile` 和 `SparkContext.pickleFile` 可以以序列化的 Python 对象的格式存储一个 RDD. 可以批量序列化，默认批次大小是10.

- SequenceFile and Hadoop Input/Output Formats

> Note this feature is currently marked Experimental and is intended for advanced users. It may be replaced in future with read/write support based on Spark SQL, in which case Spark SQL is the preferred approach.

注意：这个特性当前处在试验阶段，是为高级用户准备的。未来可能会被 Spark SQL 的 read/write 方法取代。

##### Writable Support

> PySpark SequenceFile support loads an RDD of key-value pairs within Java, converts Writables to base Java types, and pickles the resulting Java objects using [pickle](https://github.com/irmen/pickle/). When saving an RDD of key-value pairs to SequenceFile, PySpark does the reverse. It unpickles Python objects into Java objects and then converts them to Writables. The following Writables are automatically converted:

PySpark SequenceFile 支持载入 Java 中的键值对形式的 RDD, 将 Writables 转换成基础 Java 类型，使用 pickle 序列化结果 Java 对象。当将键值对形式的 RDD 保存为 SequenceFile 时，PySpark 会做相应的反向转换。它反序列化 Python 对象成 Java 对象，然后将它们转成 Writables.

下面的 Writables 是自动转换的： 

Writable Type | Python Type
---|:---
Text | unicode str
IntWritable | int
FloatWritable | float
DoubleWritable | float
BooleanWritable | bool
BytesWritable | bytearray
NullWritable | None
MapWritable | dict

> Arrays are not handled out-of-the-box. Users need to specify custom `ArrayWritable` subtypes when reading or writing. When writing, users also need to specify custom converters that convert arrays to custom `ArrayWritable` subtypes. When reading, the default converter will convert custom `ArrayWritable` subtypes to Java `Object[]`, which then get pickled to Python tuples. To get Python `array.array` for arrays of primitive types, users need to specify custom converters.

数组不是开箱即用的。当读写数据时，用户需要指定自定义的 `ArrayWritable` 子类型。当写入数据时，用户也需要指定自定义的转换器，用来将数组转换成自定义的 `ArrayWritable` 子类型。当读取数据时，默认的转换器将自定义的 `ArrayWritable` 子类型转换成 Java `Object[]`, 然后序列化成 Python 元组。对于基础类型的数组，为了得到 Python `array.array`，用户需要指定自定义的转换器。

##### Saving and Loading SequenceFiles

> Similarly to text files, SequenceFiles can be saved and loaded by specifying the path. The key and value classes can be specified, but for standard Writables this is not required.

和文本文件类型，根据指定的路径，保存和加载 SequenceFiles 文件。可以指定键和值类型，但对于标准的 Writables, 这并不是必要的。

```
>>> rdd = sc.parallelize(range(1, 4)).map(lambda x: (x, "a" * x))
>>> rdd.saveAsSequenceFile("path/to/file")
>>> sorted(sc.sequenceFile("path/to/file").collect())
[(1, u'a'), (2, u'aa'), (3, u'aaa')]
```

##### Saving and Loading Other Hadoop Input/Output Formats

> PySpark can also read any Hadoop InputFormat or write any Hadoop OutputFormat, for both ‘new’ and ‘old’ Hadoop MapReduce APIs. If required, a Hadoop configuration can be passed in as a Python dict. Here is an example using the Elasticsearch ESInputFormat:

对于新的和旧的 Hadoop MapReduce APIs, PySpark 也可以读取任意的 Hadoop InputFormat 或写入任意的 Hadoop OutputFormat. 如果要求的话，可以将 Hadoop 配置作为 Python 字典传入。

```
$ ./bin/pyspark --jars /path/to/elasticsearch-hadoop.jar
>>> conf = {"es.resource" : "index/type"}  # assume Elasticsearch is running on localhost defaults
>>> rdd = sc.newAPIHadoopRDD("org.elasticsearch.hadoop.mr.EsInputFormat",
                             "org.apache.hadoop.io.NullWritable",
                             "org.elasticsearch.hadoop.mr.LinkedMapWritable",
                             conf=conf)
>>> rdd.first()  # the result is a MapWritable that is converted to a Python dict
(u'Elasticsearch ID',
 {u'field1': True,
  u'field2': u'Some Text',
  u'field3': 12345})
```

> Note that, if the InputFormat simply depends on a Hadoop configuration and/or input path, and the key and value classes can easily be converted according to the above table, then this approach should work well for such cases.

注意，如果 InputFormat 仅依赖一个 Hadoop 配置和/或输入路径，键和值类可以轻易地根据上表转换，那么这个方法应该很适合这种情况。

> If you have custom serialized binary data (such as loading data from Cassandra / HBase), then you will first need to transform that data on the Scala/Java side to something which can be handled by pickle’s pickler. A [Converter](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/api/python/Converter.html) trait is provided for this. Simply extend this trait and implement your transformation code in the `convert` method. Remember to ensure that this class, along with any dependencies required to access your `InputFormat`, are packaged into your Spark job jar and included on the PySpark classpath.

如果你有自定义的序列化二进制数据（例如从Cassandra/HBase载入数据），那么你将首先要把 Scala/Java 端的数据转换成能被 pickle 的 pickler 处理的形式。

为了解决这个问题，提供了一个 Converter 特质。仅需要继承这个特质，在 `convert` 方法中实现你的转换代码。

记住要确保这个类和访问你的 `InputFormat` 所需的依赖被打包进 Spark job jar 文件中，且被包含在 PySpark 类路径中。

> See the [Python examples](https://github.com/apache/spark/tree/master/examples/src/main/python) and the [Converter examples](https://github.com/apache/spark/tree/master/examples/src/main/scala/org/apache/spark/examples/pythonconverters) for examples of using Cassandra / HBase InputFormat and OutputFormat with custom converters.

### RDD Operations

> RDDs support two types of operations: transformations, which create a new dataset from an existing one, and actions, which return a value to the driver program after running a computation on the dataset. For example, `map` is a transformation that passes each dataset element through a function and returns a new RDD representing the results. On the other hand, `reduce` is an action that aggregates all the elements of the RDD using some function and returns the final result to the driver program (although there is also a parallel `reduceByKey` that returns a distributed dataset).

RDDs 支持转换和行动两种类型的操作。转换是根据已存在的数据集创建一个新的数据集。行动将在数据集上执行计算后，将值返回给驱动程序。

例如，map 就是一个转换，它将每个数据集元素传递给一个函数，并返回一个的新 RDD. reduce 是一个行动，它通过执行一些函数，聚合 RDD 中所有元素，并将最终结果给返回驱动程序（虽然也有一个并行 reduceByKey 返回一个分布式数据集）。

> All transformations in Spark are lazy, in that they do not compute their results right away. Instead, they just remember the transformations applied to some base dataset (e.g. a file). The transformations are only computed when an action requires a result to be returned to the driver program. This design enables Spark to run more efficiently. For example, we can realize that a dataset created through `map` will be used in a `reduce` and return only the result of the `reduce` to the driver, rather than the larger mapped dataset.

Spark 中所有的转换都是懒加载的，因此它不会立刻计算出结果，仅仅会记住应用在基础数据集上的转换操作。

只有当需要返回结果给驱动程序时，转换才开始计算。这种设计使 Spark 的运行更高效。例如，map 所创建的数据集将被用在 reduce 中，并且只有 reduce 的计算结果返回给驱动程序，而不是返回一个更大的映射后的数据集。

> By default, each transformed RDD may be recomputed each time you run an action on it. However, you may also persist an RDD in memory using the `persist` (or `cache`) method, in which case Spark will keep the elements around on the cluster for much faster access the next time you query it. There is also support for persisting RDDs on disk, or replicated across multiple nodes.

默认情况下，每次在一个已转换的 RDD 上运行一个行动时，它都会被重新计算。但是，可以使用 persist/cache 方法将 RDD 持久化到内存中；

在这种情况下，Spark 为了下次查询时可以更快地访问，会把数据保存在集群上。此外，还支持将 RDD 持久化到磁盘，或跨多个节点复制。

#### Basics

##### Scala

> To illustrate RDD basics, consider the simple program below:


```scala
val lines = sc.textFile("data.txt")
val lineLengths = lines.map(s => s.length)
val totalLength = lineLengths.reduce((a, b) => a + b)
```

> The first line defines a base RDD from an external file. This dataset is not loaded in memory or otherwise acted on: `lines` is merely a pointer to the file. The second line defines `lineLengths` as the result of a map transformation. Again, `lineLengths` is not immediately computed, due to laziness. Finally, we run `reduce`, which is an action. At this point Spark breaks the computation into tasks to run on separate machines, and each machine runs both its part of the map and a local reduction, returning only its answer to the driver program.

第一行从外部文件读取数据，创建一个基础的 RDD，但这个数据集并未加载到内存中或即将被操作: lines 仅仅是一个指向该文件的指针。

第二行定义了 map 转换的结果 lineLengths. 请注意，由于延迟加载，lineLengths 不会被立即计算出。

最后，运行 reduce action. 此时，Spark 把计算划分成不同的任务，并分发到不同的机器上运行，每台机器都运行自己的 map 部分，和本地聚合，将聚合后的结果返回给驱动程序。

> If we also wanted to use `lineLengths` again later, we could add:

如果希望以后再次使用 `lineLengths`, 还可以添加:

```scala
lineLengths.persist()
```

> before the `reduce`, which would cause `lineLengths` to be saved in memory after the first time it is computed.

在执行 reduce 之前，这将使 `lineLengths` 在第一次计算之后就被保存在内存中。

##### Java

> To illustrate RDD basics, consider the simple program below:

```java
JavaRDD<String> lines = sc.textFile("data.txt");
JavaRDD<Integer> lineLengths = lines.map(s -> s.length());
int totalLength = lineLengths.reduce((a, b) -> a + b);
```

> The first line defines a base RDD from an external file. This dataset is not loaded in memory or otherwise acted on: `lines` is merely a pointer to the file. The second line defines `lineLengths` as the result of a map transformation. Again, `lineLengths` is not immediately computed, due to laziness. Finally, we run `reduce`, which is an action. At this point Spark breaks the computation into tasks to run on separate machines, and each machine runs both its part of the map and a local reduction, returning only its answer to the driver program.

第一行从外部文件读取数据，创建一个基础的 RDD，但这个数据集并未加载到内存中或即将被操作: lines 仅仅是一个指向该文件的指针。

第二行定义了 map 转换的结果 lineLengths. 请注意，由于延迟加载，lineLengths 不会被立即计算出。

最后，运行 reduce action. 此时，Spark 把计算划分成不同的任务，并分发到不同的机器上运行，每台机器都运行自己的 map 部分，和本地聚合，将聚合后的结果返回给驱动程序。

> If we also wanted to use `lineLengths` again later, we could add:

如果希望以后再次使用 `lineLengths`, 还可以添加:

```java
lineLengths.persist(StorageLevel.MEMORY_ONLY());
```

> before the reduce, which would cause `lineLengths` to be saved in memory after the first time it is computed.

在执行 reduce 之前，这将使 `lineLengths` 在第一次计算之后就被保存在内存中。

##### Python

> To illustrate RDD basics, consider the simple program below:

```python
lines = sc.textFile("data.txt")
lineLengths = lines.map(lambda s: len(s))
totalLength = lineLengths.reduce(lambda a, b: a + b)
```

> The first line defines a base RDD from an external file. This dataset is not loaded in memory or otherwise acted on: `lines` is merely a pointer to the file. The second line defines `lineLengths` as the result of a map transformation. Again, `lineLengths` is not immediately computed, due to laziness. Finally, we run `reduce`, which is an action. At this point Spark breaks the computation into tasks to run on separate machines, and each machine runs both its part of the map and a local reduction, returning only its answer to the driver program.

第一行从外部文件读取数据，创建一个基础的 RDD，但这个数据集并未加载到内存中或即将被操作: lines 仅仅是一个指向该文件的指针。

第二行定义了 map 转换的结果 lineLengths. 请注意，由于延迟加载，lineLengths 不会被立即计算出。

最后，运行 reduce action. 此时，Spark 把计算划分成不同的任务，并分发到不同的机器上运行，每台机器都运行自己的 map 部分，和本地聚合，将聚合后的结果返回给驱动程序。

> If we also wanted to use `lineLengths` again later, we could add:

如果希望以后再次使用 `lineLengths`, 还可以添加:

```python
lineLengths.persist()
```

> before the reduce, which would cause `lineLengths` to be saved in memory after the first time it is computed.

在执行 reduce 之前，这将使 `lineLengths` 在第一次计算之后就被保存在内存中。

#### Passing Functions to Spark

##### Scala

> Spark’s API relies heavily on passing functions in the driver program to run on the cluster. There are two recommended ways to do this:

当在集群上运行时，Spark 的 API 在很大程度上依赖于驱动程序中传递的函数。

- 匿名函数语法用于短代码
- 全局单例对象中的静态方法。例如，你可以定义 `object MyFunctions`, 然后传递 `MyFunctions.func1`

> [Anonymous function syntax](http://docs.scala-lang.org/tour/basics.html#functions), which can be used for short pieces of code.
> Static methods in a global singleton object. For example, you can define `object MyFunctions` and then pass `MyFunctions.func1`, as follows:

```scala
object MyFunctions {
  def func1(s: String): String = { ... }
}

myRdd.map(MyFunctions.func1)
```

> Note that while it is also possible to pass a reference to a method in a class instance (as opposed to a singleton object), this requires sending the object that contains that class along with the method. For example, consider:

注意，虽然也可能传递一个类实例（与单例对象相反）的方法引用，但这需要发送整个对象，包括类中其它方法。例如，考虑:

```scala
class MyClass {
  def func1(s: String): String = { ... }
  def doStuff(rdd: RDD[String]): RDD[String] = { rdd.map(func1) }
}
```

> Here, if we create a new `MyClass` instance and call `doStuff` on it, the `map` inside there references the `func1` method of that `MyClass` instance, so the whole object needs to be sent to the cluster. It is similar to writing `rdd.map(x => this.func1(x))`.

这里，如果我们创建一个新的 `MyClass` 类，并调用 `doStuff` 方法。在 `map` 内引用 `MyClass` 实例的 `func1` 方法，所以整个对象
需要被发送到集群的，类似于 `rdd.map(x => this.func1(x))`.

> In a similar way, accessing fields of the outer object will reference the whole object:

访问外部对象的字段将引用整个对象:

```scala
class MyClass {
  val field = "Hello"
  def doStuff(rdd: RDD[String]): RDD[String] = { rdd.map(x => field + x) }
}
```

> is equivalent to writing `rdd.map(x => this.field + x)`, which references all of `this`. To avoid this issue, the simplest way is to copy `field` into a local variable instead of accessing it externally:

等价于写 `rdd.map(x => this.field + x)`, 引用 `this` 的所有。为了避免这个问题，最简单的方式就是将 `field` 复制成本地变量，而不是从外部访问它。

```scala
def doStuff(rdd: RDD[String]): RDD[String] = {
  val field_ = this.field
  rdd.map(x => field_ + x)
```

##### Java

> Spark’s API relies heavily on passing functions in the driver program to run on the cluster. In Java, functions are represented by classes implementing the interfaces in the [org.apache.spark.api.java.function](https://spark.apache.org/docs/3.3.2/api/java/index.html?org/apache/spark/api/java/function/package-summary.html) package. There are two ways to create such functions:

当在集群上运行时，Spark 的 API 在很大程度上依赖于驱动程序中传递的函数。

在 Java 中，通过实现 `org.apache.spark.api.java.function` 包里的接口的类表示函数。创建这种函数有两种方式:

- 在你的类中实现 Function 接口，可以是匿名内部类，也可以是命名的一个，然后将它的一个实例传递给 Spark
- 使用 Lambda 表达式简洁地定义一个类

> Implement the Function interfaces in your own class, either as an anonymous inner class or a named one, and pass an instance of it to Spark.
> Use [lambda expressions](http://docs.oracle.com/javase/tutorial/java/javaOO/lambdaexpressions.html) to concisely define an implementation.

> While much of this guide uses lambda syntax for conciseness, it is easy to use all the same APIs in long-form. For example, we could have written our code above as follows:

为了简洁，本指南大部分都使用了 lambda 语法，这样就很容易使用所有相同的API。如：

```java
JavaRDD<String> lines = sc.textFile("data.txt");
JavaRDD<Integer> lineLengths = lines.map(new Function<String, Integer>() {
  public Integer call(String s) { return s.length(); }
});
int totalLength = lineLengths.reduce(new Function2<Integer, Integer, Integer>() {
  public Integer call(Integer a, Integer b) { return a + b; }
});
```

> Or, if writing the functions inline is unwieldy:

或者，按如下方式写的话，会显得很笨拙

```java
class GetLength implements Function<String, Integer> {
  public Integer call(String s) { return s.length(); }
}
class Sum implements Function2<Integer, Integer, Integer> {
  public Integer call(Integer a, Integer b) { return a + b; }
}

JavaRDD<String> lines = sc.textFile("data.txt");
JavaRDD<Integer> lineLengths = lines.map(new GetLength());
int totalLength = lineLengths.reduce(new Sum());
```

> Note that anonymous inner classes in Java can also access variables in the enclosing scope as long as they are marked `final`. Spark will ship copies of these variables to each worker node as it does for other languages.

注意：只要闭包作用域内的变量标记为 final, 那么 Java 的匿名内部类就可以访问。 Spark 会将这些变量分发到每个工作节点上。

##### Python

> Spark’s API relies heavily on passing functions in the driver program to run on the cluster. There are three recommended ways to do this:

当在集群上运行时，Spark 的 API 在很大程度上依赖于驱动程序中传递的函数。有三种方式定义传递的函数:

- Lambda 表达式，对于可以写成表达式的简单函数（Lambda并不支持多语句函数或不返回值的语句）
- 本地的 `defs` 用于更长的代码
- 模块中的顶层函数

> [Lambda expressions](https://docs.python.org/2/tutorial/controlflow.html#lambda-expressions), for simple functions that can be written as an expression. (Lambdas do not support multi-statement functions or statements that do not return a value.)
> Local `defs` inside the function calling into Spark, for longer code.
> Top-level functions in a module.

> For example, to pass a longer function than can be supported using a lambda, consider the code below:

例如，传递一个比 lambda 更长的函数

```python
"""MyScript.py"""
if __name__ == "__main__":
    def myFunc(s):
        words = s.split(" ")
        return len(words)

    sc = SparkContext(...)
    sc.textFile("file.txt").map(myFunc)
```

> Note that while it is also possible to pass a reference to a method in a class instance (as opposed to a singleton object), this requires sending the object that contains that class along with the method. For example, consider:

注意，虽然也可能传递一个类实例（与单例对象相反）的方法引用，但这需要发送整个对象，包括类中其它方法。例如，考虑:

```python
class MyClass(object):
    def func(self, s):
        return s
    def doStuff(self, rdd):
        return rdd.map(self.func)
```

> Here, if we create a new `MyClass` and call `doStuff` on it, the `map` inside there references the `func` method of that `MyClass` instance, so the whole object needs to be sent to the cluster.

这里，如果我们创建一个新的 `MyClass` 类，并调用 `doStuff` 方法。在 `map` 内引用 `MyClass` 实例的 `func` 方法，所以整个对象
需要被发送到集群的。

> In a similar way, accessing fields of the outer object will reference the whole object:

访问外部对象的字段将引用整个对象:

```python
class MyClass(object):
    def __init__(self):
        self.field = "Hello"
    def doStuff(self, rdd):
        return rdd.map(lambda s: self.field + s)
```

> To avoid this issue, the simplest way is to copy `field` into a local variable instead of accessing it externally:

为了避免这个问题，最简单的方式是将 `field` 复制成一个本地变量，而不是外部访问它:

```python
def doStuff(self, rdd):
    field = self.field
    return rdd.map(lambda s: field + s)
```

#### Understanding closures

> One of the harder things about Spark is understanding the scope and life cycle of variables and methods when executing code across a cluster. RDD operations that modify variables outside of their scope can be a frequent source of confusion. In the example below we’ll look at code that uses `foreach()` to increment a counter, but similar issues can occur for other operations as well.

当跨集群执行代码时，Spark 中更难的壹点是理解变量和方法的作用域和生命周期。

在其作用域之外修改变量的 RDD 操作经常会造成
混淆。在下面的例子中，我们将看一下使用的 `foreach() `代码递增累加计数器，但类似的问题，也可能会出现其他操作上.

##### Example

> Consider the naive RDD element sum below, which may behave differently depending on whether execution is happening within the same JVM. A common example of this is when running Spark in local mode (`--master = local[n]`) versus deploying a Spark application to a cluster (e.g. via spark-submit to YARN):

考虑一个简单的 RDD 元素求和的例子，可能产生的结果是不同，取决于是否在同一个 JVM 中执行。

一个常见的例子，当 Spark 运行在本地模式(`--master = local[n]`)时，与将 Spark 应用部署到群集（例如，通过 spark-submit 到 YARN）:

```Scala
var counter = 0
var rdd = sc.parallelize(data)

// Wrong: Don't do this!!
rdd.foreach(x => counter += x)

println("Counter value: " + counter)
```

```java
int counter = 0;
JavaRDD<Integer> rdd = sc.parallelize(data);

// Wrong: Don't do this!!
rdd.foreach(x -> counter += x);

println("Counter value: " + counter);
```

```python
counter = 0
rdd = sc.parallelize(data)

# Wrong: Don't do this!!
def increment_counter(x):
    global counter
    counter += x
rdd.foreach(increment_counter)

print("Counter value: ", counter)
```

##### Local vs. cluster modes

> The behavior of the above code is undefined, and may not work as intended. To execute jobs, Spark breaks up the processing of RDD operations into tasks, each of which is executed by an executor. Prior to execution, Spark computes the task’s closure. The closure is those variables and methods which must be visible for the executor to perform its computations on the RDD (in this case `foreach()`). This closure is serialized and sent to each executor.

上面的代码行为是不确定的，并且可能无法按预期正常工作。

为了执行作业，Spark 将对 RDD 操作的处理分解成多个任务，每个任务在一个 executor 上执行。在执行之前，Spark 计算任务的闭包。

闭包是指 executor 在 RDD 上执行计算的时候必须可见的那些变量和方法（在这种情况下是`foreach()`）。闭包被序列化，并被发送到每个 executor 上。

> The variables within the closure sent to each executor are now copies and thus, when counter is referenced within the foreach function, it’s no longer the counter on the driver node. There is still a counter in the memory of the driver node but this is no longer visible to the executors! The executors only see the copy from the serialized closure. Thus, the final value of counter will still be zero since all operations on counter were referencing the value within the serialized closure.

发送给每个 executor 的闭包中的变量是副本，因此，当在 foreach 函数内使用计数器时，它不再是驱动节点上的计数器。

驱动节点的内存中仍有一个计数器，但该变量对 executors 不可见的. executors 只能看到序列化闭包的副本。因此，计数器的最终值仍
然为零，因为计数器上的所有操作都引用了序列化闭包内的值。

【每个 executor 中的变量都是副本，在其中的操作改变的都只是副本的值，而不是驱动节点上的值】

> In local mode, in some circumstances, the foreach function will actually execute within the same JVM as the driver and will reference the same original counter, and may actually update it.

在本地模式下，在某些情况下，该 foreach 函数实际上将在与驱动相同的 JVM 内执行，并且会引用相同的原始计数器，并可能实际更新它。

> To ensure well-defined behavior in these sorts of scenarios one should use an [Accumulator](https://spark.apache.org/docs/3.3.2/rdd-programming-guide.html#accumulators). Accumulators in Spark are used specifically to provide a mechanism for safely updating a variable when execution is split up across worker nodes in a cluster. The Accumulators section of this guide discusses these in more detail.

为了确保在这些场景中明确定义的行为，应该使用一个累加器. Spark 中的累加器专门用于提供一种机制，用于在集群的工作节点中拆分执行任务时，安全地更新变量。

> In general, closures - constructs like loops or locally defined methods, should not be used to mutate some global state. Spark does not define or guarantee the behavior of mutations to objects referenced from outside of closures. Some code that does this may work in local mode, but that’s just by accident and such code will not behave as expected in distributed mode. Use an Accumulator instead if some global aggregation is needed.

一般来说，闭包 - 构造闭包就像构造循环或本地定义的方法一样，不应该被用来改变一些全局状态。

Spark 并没有定义或保证从闭包外引用的对象的改变行为。这样做的一些代码可以在本地模式下工作，但这只是偶然，并且这种代码在分布式模式下的行为不会像你想的那样。如果需要某些全局聚合，请改用累加器。

##### Printing elements of an RDD

> Another common idiom is attempting to print out the elements of an RDD using `rdd.foreach(println)` or `rdd.map(println)`. On a single machine, this will generate the expected output and print all the RDD’s elements. However, in cluster mode, the output to `stdout` being called by the executors is now writing to the executor’s `stdout` instead, not the one on the driver, so `stdout` on the driver won’t show these! To print all elements on the driver, one can use the `collect()` method to first bring the RDD to the driver node thus: `rdd.collect().foreach(println)`. This can cause the driver to run out of memory, though, because `collect()` fetches the entire RDD to a single machine; if you only need to print a few elements of the RDD, a safer approach is to use the `take(): rdd.take(100).foreach(println)`.

在单台机器上, `rdd.foreach(println)` 或 `rdd.map(println)` 用于打印 RDD 的所有元素，这将产生预期的输出和打印 RDD 的所有元素。

然而，在集群模式下，输出到由 executors 调用 `stdout` 的输出被写入到 executor 的标准输出中，而不是驱动程序上的。

因此，结果不会输出到驱动程序的标准输出中。要打印驱动程序的所有元素，可以使用的 `collect()` 方法，首先把 RDD 放到驱动程序节点上，因此 `rdd.collect().foreach(println)`。

但这样做可能会导致驱动程序内存耗尽，因为 `collect()` 会把整个 RDD 到拉取到一台机器上，如果你只需要打印 RDD 的几个元素，一个更安全的方法是使用 `take()`: `rdd.take(100).foreach(println)`.

#### Working with Key-Value Pairs

##### Scala

> While most Spark operations work on RDDs containing any type of objects, a few special operations are only available on RDDs of key-value pairs. The most common ones are distributed “shuffle” operations, such as grouping or aggregating the elements by a key.

虽然大多数 Spark 操作的对象是包含任何对象类型 RDDs, 只有少数特殊的操作可用于键值对的 RDDs.

最常见的是分布式 shuffle 操作，如，通过元素的 key 进行分组或聚合操作。

> In Scala, these operations are automatically available on RDDs containing [Tuple2](http://www.scala-lang.org/api/2.12.15/index.html#scala.Tuple2) objects (the built-in tuples in the language, created by simply writing `(a, b)`). The key-value pair operations are available in the [PairRDDFunctions](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/rdd/PairRDDFunctions.html) class, which automatically wraps around an RDD of tuples.

在 Scala 中，这些操作在包含 Tuple2 对象(语言中的内建元组，通过`(a, b)`创建)的 RDDs 自动可用。键值对操作在 PairRDDFunctions 类中可用，它会自动封装元组 RDD.

> For example, the following code uses the reduceByKey operation on key-value pairs to count how many times each line of text occurs in a file:

例如，下面的代码使用 `reduceByKey` 操作统计文本文件中的每一行出现了多少次:

```
val lines = sc.textFile("data.txt")
val pairs = lines.map(s => (s, 1))
val counts = pairs.reduceByKey((a, b) => a + b)
```

> We could also use `counts.sortByKey()`, for example, to sort the pairs alphabetically, and finally `counts.collect()` to bring them back to the driver program as an array of objects.

也可以使用 `counts.sortByKey()`, 按字母表的顺序进行排序，再使用 `counts.collect()` 将结果以对象数组的形式带回驱动程序。

> Note: when using custom objects as the key in key-value pair operations, you must be sure that a custom `equals()` method is accompanied with a matching `hashCode()` method. For full details, see the contract outlined in the [Object.hashCode() documentation](https://docs.oracle.com/javase/8/docs/api/java/lang/Object.html#hashCode--).

注意：在键值对操作中，当你的键是自定义对象时，需要确保自定义的 `equals()` 方法与匹配的 `hashCode()` 方法一起使用。

##### Java

> While most Spark operations work on RDDs containing any type of objects, a few special operations are only available on RDDs of key-value pairs. The most common ones are distributed “shuffle” operations, such as grouping or aggregating the elements by a key.

虽然大多数 Spark 操作的对象是包含任何对象类型 RDDs, 只有少数特殊的操作可用于键值对的 RDDs.

最常见的是分布式 shuffle 操作，如，通过元素的 key 进行分组或聚合操作。

> In Java, key-value pairs are represented using the [scala.Tuple2](http://www.scala-lang.org/api/2.12.15/index.html#scala.Tuple2) class from the Scala standard library. You can simply call `new Tuple2(a, b)` to create a tuple, and access its fields later with `tuple._1()` and `tuple._2()`.

在 Java 中，使用来自 Scala 标准库的  `scala.Tuple2` 类表示键值对。你可以使用 `new Tuple2(a, b)` 创建一个元组，之后使用 `tuple._1()`、`tuple._2()` 访问它的字段。

> RDDs of key-value pairs are represented by the [JavaPairRDD](https://spark.apache.org/docs/3.3.2/api/java/index.html?org/apache/spark/api/java/JavaPairRDD.html) class. You can construct JavaPairRDDs from JavaRDDs using special versions of the `map` operations, like `mapToPair` and `flatMapToPair`. The JavaPairRDD will have both standard RDD functions and special key-value ones.

键值对的 RDDs 使用 JavaPairRDD 类表示。

可以使用一个特殊的 map 操作(如mapToPair和flatMapToPair)，基于 JavaRDDs 创建一个 JavaPairRDD. JavaPairRDD 将拥有标准的 RDD 函数和特有的键值对函数。

> For example, the following code uses the `reduceByKey` operation on key-value pairs to count how many times each line of text occurs in a file:

例如，下面的代码使用 `reduceByKey` 操作统计文本文件中的每一行出现了多少次:

```
JavaRDD<String> lines = sc.textFile("data.txt");
JavaPairRDD<String, Integer> pairs = lines.mapToPair(s -> new Tuple2(s, 1));
JavaPairRDD<String, Integer> counts = pairs.reduceByKey((a, b) -> a + b);
```

> We could also use `counts.sortByKey()`, for example, to sort the pairs alphabetically, and finally `counts.collect()` to bring them back to the driver program as an array of objects.

也可以使用 `counts.sortByKey()`, 按字母表的顺序进行排序，再使用 `counts.collect()` 将结果以对象数组的形式带回驱动程序。

> Note: when using custom objects as the key in key-value pair operations, you must be sure that a custom `equals()` method is accompanied with a matching `hashCode()` method. For full details, see the contract outlined in the [Object.hashCode() documentation](https://docs.oracle.com/javase/8/docs/api/java/lang/Object.html#hashCode--).

注意：在键值对操作中，当你的键是自定义对象时，需要确保自定义的 `equals()` 方法与匹配的 `hashCode()` 方法一起使用。

##### Python

> While most Spark operations work on RDDs containing any type of objects, a few special operations are only available on RDDs of key-value pairs. The most common ones are distributed “shuffle” operations, such as grouping or aggregating the elements by a key.

虽然大多数 Spark 操作的对象是包含任何对象类型 RDDs, 只有少数特殊的操作可用于键值对的 RDDs.

最常见的是分布式 shuffle 操作，如，通过元素的 key 进行分组或聚合操作。

> In Python, these operations work on RDDs containing built-in Python tuples such as `(1, 2)`. Simply create such tuples and then call your desired operation.

在 Python 中，这些操作在包含内置 Python 元组的 RDDs 上工作，例如`(1,2)`. 创建这样的元组后，再调用相关的操作。

> For example, the following code uses the `reduceByKey` operation on key-value pairs to count how many times each line of text occurs in a file:

例如，下面的代码使用 `reduceByKey` 操作统计文本文件中的每一行出现了多少次:

```
lines = sc.textFile("data.txt")
pairs = lines.map(lambda s: (s, 1))
counts = pairs.reduceByKey(lambda a, b: a + b)
```

> We could also use `counts.sortByKey()`, for example, to sort the pairs alphabetically, and finally `counts.collect()` to bring them back to the driver program as a list of objects.

也可以使用 `counts.sortByKey()`, 按字母表的顺序进行排序，再使用 `counts.collect()` 将结果以对象列表的形式带回驱动程序。

#### Transformations

> The following table lists some of the common transformations supported by Spark. Refer to the RDD API doc ([Scala](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/rdd/RDD.html), [Java](https://spark.apache.org/docs/3.3.2/api/java/index.html?org/apache/spark/api/java/JavaRDD.html), [Python](https://spark.apache.org/docs/3.3.2/api/python/reference/api/pyspark.RDD.html#pyspark.RDD), [R](https://spark.apache.org/docs/3.3.2/api/R/reference/index.html)) and pair RDD functions doc ([Scala](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/rdd/PairRDDFunctions.html), [Java](https://spark.apache.org/docs/3.3.2/api/java/index.html?org/apache/spark/api/java/JavaPairRDD.html)) for details.

Transformation|Meaning
---|:---
map(func) | Return a new distributed dataset formed by passing each element of the source through a function `func`.<br/>返回一个新的分布式数据集，它由一个函数 func 作用在数据源中的每个元素生成。
filter(func)  | Return a new dataset formed by selecting those elements of the source on which `func` returns true.<br/>返回一个新的分布式数据集，它由一个函数 func 作用在数据源的元素上，且返回值为 true 的元素组成。
flatMap(func) | Similar to map, but each input item can be mapped to 0 or more output items (so `func` should return a Seq rather than a single item).<br/>与 map 类似，但是每一个输入项可以被映射成 0 个或多个输出项（所以 func 应该返回一个序列而不是一个单独项）。
mapPartitions(func) | Similar to map, but runs separately on each partition (block) of the RDD, so `func` must be of type `Iterator<T> => Iterator<U>` when running on an RDD of type T.<br/>与 map 类似，但是独立的运行在 RDD 的每个分区(block)上。所以在一个类型为 T 的 RDD 上运行时，func 必须是 `Iterator<T> => Iterator<U>` 类型。
mapPartitionsWithIndex(func) | Similar to mapPartitions, but also provides `func` with an integer value representing the index of the partition, so func must be of type `(Int, Iterator<T>) => Iterator<U>` when running on an RDD of type T.<br/>与 mapPartitions 类似，但是也需要提供一个代表分区索引整型值作为参数的 func ，所以在一个类型为 T 的 RDD 上运行时 func 必须是 `(Int, Iterator<T>) => Iterator<U>` 类型。【指定一个分区】
sample(withReplacement, fraction, seed) | Sample a fraction `fraction` of the data, with or without replacement, using a given random number generator seed.<br/>从源数据中按一定比例抽样，并设置是否放回抽样、是否使用随机数生成器种子。
union(otherDataset) | Return a new dataset that contains the union of the elements in the source dataset and the argument.<br/>回一个包含了源数据集和其它数据集的并集的数据集。
intersection(otherDataset)  | Return a new RDD that contains the intersection of elements in the source dataset and the argument.<br/>返回一个包含了源数据集和其它数据集的交集的数据集。
distinct([numPartitions])) | Return a new dataset that contains the distinct elements of the source dataset.<br/>返回一个源数据集去重后的数据集。
groupByKey([numPartitions])| When called on a dataset of `(K, V)` pairs, returns a dataset of `(K, Iterable<V>)` pairs.当在一个`(K,V)`对数据集上调用的时候，返回一个`(K, Iterable<V>)`对数据集。<br/>Note: If you are grouping in order to perform an aggregation (such as a sum or average) over each key, using `reduceByKey` or `aggregateByKey` will yield much better performance.如果分组是为了在每一个键上执行聚合操作（如sum、average)，使用 reduceByKey 或 aggregateByKey 来计算性能会更好。<br/>Note: By default, the level of parallelism in the output depends on the number of partitions of the parent RDD. You can pass an optional `numPartitions` argument to set a different number of tasks.默认情况下，输出结果的并行度取决于父 RDD 的分区数，但可以传递一个可选的 numPartitions 参数来设置不同的任务数。
reduceByKey(func, [numPartitions])| When called on a dataset of `(K, V)` pairs, returns a dataset of `(K, V)` pairs where the values for each key are aggregated using the given reduce function `func`, which must be of `type (V,V) => V`. Like in `groupByKey`, the number of reduce tasks is configurable through an optional second argument.<br/>当在一个`(K,V)`对数据集上调用的时候，返回一个`(K, V)`对数据集，其中每个键的值是经过 func 聚合后的结果，它必须是 `type (V,V) => V` 的类型。像 groupByKey 一样，reduce 任务数可以通过第二个可选的参数配置。
aggregateByKey(zeroValue)(seqOp, combOp, [numPartitions])| When called on a dataset of `(K, V)` pairs, returns a dataset of `(K, U)` pairs where the values for each key are aggregated using the given combine functions and a neutral "zero" value. Allows an aggregated value type that is different than the input value type, while avoiding unnecessary allocations. Like in `groupByKey`, the number of reduce tasks is configurable through an optional second argument.<br/>当在一个`(K,V)`对数据集上调用的时候，返回一个`(K, U)`对数据集，其中每个键的值是使用聚合函数和一个中性0值聚合后的结果。聚合值类型可以和输入值类型不同，也为避免不必要的分配。像 groupByKey 一样，reduce 任务数可以通过可选的第二个参数配置。
sortByKey([ascending], [numPartitions])| When called on a dataset of `(K, V)` pairs where K implements Ordered, returns a dataset of `(K, V)` pairs sorted by keys in ascending or descending order, as specified in the boolean `ascending` argument.<br/>当在一个(K,V)对数据集上调用的时候，其中的 K 实现有序，返回一个按键升序或降序的(K,V)对数据集，由布尔类型的 ascending 参数来指定。
join(otherDataset, [numPartitions])| When called on datasets of type `(K, V)` and `(K, W)`, returns a dataset of `(K, (V, W))` pairs with all pairs of elements for each key. Outer joins are supported through `leftOuterJoin`, `rightOuterJoin`, and `fullOuterJoin`.<br/>在一个 (K, V) 和 (K, W) 类型的数据集上调用时，返回一个 (K, (V, W)) 对数据集，每个键含有所有对应的值。外联结有 leftOuterJoin, rightOuterJoin 和  fullOuterJoin.
cogroup(otherDataset, [numPartitions])| When called on datasets of type `(K, V)` and `(K, W)`, returns a dataset of `(K, (Iterable<V>, Iterable<W>))` tuples. This operation is also called `groupWith`.<br/>在一个 (K, V) 和 (K, W) 类型的数据集上调用时，返回一个 (K, (Iterable<V>, Iterable<W>)) 元组的数据集（所有元素的对）。此操作也称为groupWith.
cartesian(otherDataset) | When called on datasets of types T and U, returns a dataset of `(T, U)` pairs (all pairs of elements).<br/>在一个 T 和 U 类型的数据集上调用时，返回一个 (T, U) 对类型的数据集（所有元素的对）。
pipe(command, [envVars]) | Pipe each partition of the RDD through a shell command, e.g. a Perl or bash script. RDD elements are written to the process's stdin and lines output to its stdout are returned as an RDD of strings.<br/>通过 shell 命令，将每个 RDD 的分区建立管道。例如，一个 Perl 或 bash 脚本。 RDD 的元素会被写入进程的标准输入，并且输出到标准输出的行作为一个字符串型 RDD返回。
coalesce(numPartitions) | Decrease the number of partitions in the RDD to numPartitions. Useful for running operations more efficiently after filtering down a large dataset.<br/>减少 RDD 中分区数为 numPartitions。对于一个大的数据集执行过滤后再操作更有效。
repartition(numPartitions)  | Reshuffle the data in the RDD randomly to create either more or fewer partitions and balance it across them. This always shuffles all data over the network.<br/>随机地 Reshuffle RDD 中的数据以创建更多或更少的分区，并将每个分区中的数据尽量保持均匀。该操作总是通过网络来 shuffles 所有的数据。
repartitionAndSortWithinPartitions(partitioner) | Repartition the RDD according to the given partitioner and, within each resulting partition, sort records by their keys. This is more efficient than calling `repartition` and then sorting within each partition because it can push the sorting down into the shuffle machinery.<br/>根据给定的分区器，对 RDD 进行重新分区，并在每个结果分区中，按照 key 值对记录排序。这比每一个分区中先调用 repartition 然后再 sorting（排序）效率更高，因为它可以将排序过程推送到 shuffle 操作的机器上进行。

#### Actions

> The following table lists some of the common actions supported by Spark. Refer to the RDD API doc ([Scala](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/rdd/RDD.html), [Java](https://spark.apache.org/docs/3.3.2/api/java/index.html?org/apache/spark/api/java/JavaRDD.html), [Python](https://spark.apache.org/docs/3.3.2/api/python/reference/api/pyspark.RDD.html#pyspark.RDD), [R](https://spark.apache.org/docs/3.3.2/api/R/reference/index.html)) and pair RDD functions doc ([Scala](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/rdd/PairRDDFunctions.html), [Java](https://spark.apache.org/docs/3.3.2/api/java/index.html?org/apache/spark/api/java/JavaPairRDD.html)) for details.

Action|Meaning
---|:---
reduce(func)|Aggregate the elements of the dataset using a function `func` (which takes two arguments and returns one). The function should be commutative and associative so that it can be computed correctly in parallel.<br/>使用函数 func, 聚合数据集中的元素，这个函数输入两个元素，返回一个元素。这个函数应该是commutative和associative的，这样才能保证它可以被正确地并行计算。
collect() |Return all the elements of the dataset as an array at the driver program. This is usually useful after a filter or other operation that returns a sufficiently small subset of the data.<br/>在驱动程序中，以数组的形式返回数据集中的所有元素。这在 filter 返回足够的数据子集的操作后，通常是有用的。
count()|Return the number of elements in the dataset.<br/>返回数据集中元素的个数。
first() |Return the first element of the dataset (similar to take(1)).<br/>返回数据集中的第一个元素（类似于 take(1)。
take(n) |Return an array with the first `n` elements of the dataset.<br/>将数据集中的前 n 个元素作为一个数组返回。
takeSample(withReplacement, num, [seed]) |Return an array with a random sample of num elements of the dataset, with or without replacement, optionally pre-specifying a random number generator seed.<br/>对一个数据集进行随机抽样，返回一个包含 num 个随机样本元素的数组，参数 withReplacement 指定是否有放回抽样，参数 seed 指定生成随机数的种子。
takeOrdered(n, [ordering])|Return the first `n` elements of the RDD using either their natural order or a custom comparator.<br/>返回 RDD 按自然顺序或自定义比较器排序后的前 n 个元素。
saveAsTextFile(path) |Write the elements of the dataset as a text file (or set of text files) in a given directory in the local filesystem, HDFS or any other Hadoop-supported file system. Spark will call toString on each element to convert it to a line of text in the file.<br/>将数据集中的元素以文本文件（或文本文件集合）的形式写入本地文件系统、HDFS 或其它 Hadoop 支持的文件系统中的给定目录中。Spark 将对每个元素调用 toString 方法，将数据元素转换为文本文件中的一行记录。
saveAsSequenceFile(path)(Java and Scala) | Write the elements of the dataset as a Hadoop SequenceFile in a given path in the local filesystem, HDFS or any other Hadoop-supported file system. This is available on RDDs of key-value pairs that implement Hadoop's Writable interface. In Scala, it is also available on types that are implicitly convertible to Writable (Spark includes conversions for basic types like Int, Double, String, etc).<br/>将数据集中的元素以 Hadoop SequenceFile 的形式写入到本地文件系统、HDFS 或其它 Hadoop 支持的文件系统指定的路径中。该操作可以在实现了 Hadoop Writable 接口的键值对 RDD 上使用。在 Scala 中，它还可以隐式转换为 Writable 的类型（Spark 包括了基本类型的转换，例如 Int，Double，String 等等)。
saveAsObjectFile(path)(Java and Scala) | Write the elements of the dataset in a simple format using Java serialization, which can then be loaded using `SparkContext.objectFile()`.<br/>使用 Java 序列化以简单的格式写入数据集元素，然后使用 SparkContext.objectFile() 进行加载。
countByKey() |Only available on RDDs of type `(K, V)`. Returns a hashmap of `(K, Int)` pairs with the count of each key.<br/>仅适用于（K,V）类型的 RDD。返回（K , Int）对的 hashmap，其中Int表示 key 的计数。
foreach(func) |Run a function func on each element of the dataset. This is usually done for side effects such as updating an [Accumulator](https://spark.apache.org/docs/3.3.2/rdd-programming-guide.html#accumulators) or interacting with external storage systems.对数据集中每个元素运行函数 func 。这通常作为副作用完成这个操作，例如更新一个累加器或与外部存储系统进行交互。 <br/>Note: modifying variables other than Accumulators outside of the `foreach()` may result in undefined behavior. See [Understanding closures](https://spark.apache.org/docs/3.3.2/rdd-programming-guide.html#understanding-closures-a-nameclosureslinka) for more details.注意：在foreach()外修改变量，而修改不是累加器可能会造成错误行为。

> The Spark RDD API also exposes asynchronous versions of some actions, like `foreachAsync` for `foreach`, which immediately return a `FutureAction` to the caller instead of blocking on completion of the action. This can be used to manage or wait for the asynchronous execution of the action.

该 Spark RDD API 还暴露了一些 actions 的异步版本，例如 foreach 的 foreachAsyn, 它们会立即返回
一个 FutureAction 给调用者，而不是在完成 action 时阻塞。这可以用于管理或等待 action 的异步执行。

#### Shuffle operations

> Certain operations within Spark trigger an event known as the shuffle. The shuffle is Spark’s mechanism for re-distributing data so that it’s grouped differently across partitions. This typically involves copying data across executors and machines, making the shuffle a complex and costly operation.

Spark 里的某些操作会触发 shuffle 事件。

shuffle 是 spark  重新分配数据的一种机制，使得这些数据可以跨分区分组。这通常涉及在 executors 间和机器间复制数据，这使得 shuffle 成为一个复杂高代价的操作。

##### Background

> To understand what happens during the shuffle, we can consider the example of the [reduceByKey](https://spark.apache.org/docs/3.3.2/rdd-programming-guide.html#ReduceByLink) operation. The `reduceByKey` operation generates a new RDD where all values for a single key are combined into a tuple - the key and the result of executing a reduce function against all values associated with that key. The challenge is that not all values for a single key necessarily reside on the same partition, or even the same machine, but they must be co-located to compute the result.

为了理解 shuffle 操作，以 reduceByKey 为例。 

reduceBykey 操作会产生一个新的 RDD, 其中每个键对应的所有值组合进入一个元组 - 键以及在与键相关联的所有值上执行 reduce 函数的结果。

面临的挑战是，与这个键相关联的所有值不一定都在同一个分区内，甚至是不一定在同一台机器里，但是它们必须共同被计算。

> In Spark, data is generally not distributed across partitions to be in the necessary place for a specific operation. During computations, a single task will operate on a single partition - thus, to organize all the data for a single `reduceByKey` reduce task to execute, Spark needs to perform an all-to-all operation. It must read from all partitions to find all the values for all keys, and then bring together values across partitions to compute the final result for each key - this is called the shuffle.

在 spark 里，数据通常不会跨分区分布在特定操作所需的位置。

在计算期间，一个任务在一个分区上执行，因此，为了所有数据都在一个 reduceByKey 的 reduce 任务上运行，Spark 需要执行一个 all-to-all 操作。

它必须从所有分区读取所有的键及其对应的值，并且将它们跨分区的聚集起来来计算每个键的结果，这个过程就叫做 shuffle.

> Although the set of elements in each partition of newly shuffled data will be deterministic, and so is the ordering of partitions themselves, the ordering of these elements is not. If one desires predictably ordered data following shuffle then it’s possible to use:

尽管新混洗后的数据的每个分区中的元素集是确定的，分区本身的顺序也是确定的，但是这些元素的顺序不是确定的。如果希望混洗后的数据是有序的，可以使用:

- mapPartitions 对每个分区进行排序，例如，`.sorted`
- repartitionAndSortWithinPartitions 在分区的同时对分区进行高效的排序
- sortBy 对 RDD 进行全局的排序

> `mapPartitions` to sort each partition using, for example, `.sorted`
> `repartitionAndSortWithinPartitions` to efficiently sort partitions while simultaneously repartitioning
> `sortBy` to make a globally ordered RDD

> Operations which can cause a shuffle include repartition operations like [repartition](https://spark.apache.org/docs/3.3.2/rdd-programming-guide.html#RepartitionLink) and [coalesce](https://spark.apache.org/docs/3.3.2/rdd-programming-guide.html#CoalesceLink), ‘ByKey operations (except for counting) like [groupByKey](https://spark.apache.org/docs/3.3.2/rdd-programming-guide.html#GroupByLink) and [reduceByKey](https://spark.apache.org/docs/3.3.2/rdd-programming-guide.html#ReduceByLink), and join operations like [cogroup](https://spark.apache.org/docs/3.3.2/rdd-programming-guide.html#CogroupLink) and [join](https://spark.apache.org/docs/3.3.2/rdd-programming-guide.html#JoinLink).

触发的 shuffle 的操作包括再分区操作，如 `repartition` 和 `coalesce`; `‘ByKey` 操作，如 `groupByKey` 和 `reduceByKey`, 除了统计类算子，和 join 操作，如 `cogroup` 和 `join`.

##### Performance Impact

> The Shuffle is an expensive operation since it involves disk I/O, data serialization, and network I/O. To organize data for the shuffle, Spark generates sets of tasks - `map` tasks to organize the data, and a set of `reduce` tasks to aggregate it. This nomenclature comes from MapReduce and does not directly relate to Spark’s `map` and `reduce` operations.

Shuffle 是一个代价比较高的操作，它涉及磁盘 I/O、数据序列化、网络 I/O. 为了准备 shuffle 操作的数据，Spark 生成一系列的任务 - `map` 任务组织数据, `reduce` 任务完成数据的聚合。这些术语来自 MapReduce, 跟 Spark 的 map 操作和 reduce 操作没有关系。

> Internally, results from individual map tasks are kept in memory until they can’t fit. Then, these are sorted based on the target partition and written to a single file. On the reduce side, tasks read the relevant sorted blocks.

在内部，一个 map 任务的结果数据会保存在内存中，直到内存不能全部存储为止。然后，这些数据将基于目标分区进行排序，并写入一个单独的文件中。在 reduce 端，任务将读取相关的已排序的数据块。

> Certain shuffle operations can consume significant amounts of heap memory since they employ in-memory data structures to organize records before or after transferring them. Specifically, `reduceByKey` and `aggregateByKey` create these structures on the map side, and `'ByKey` operations generate these on the reduce side. When data does not fit in memory Spark will spill these tables to disk, incurring the additional overhead of disk I/O and increased garbage collection.

某些 shuffle 操作会消耗大量的堆内存空间，因为 shuffle 操作在数据转换前后，会采用内存中的数据结构组织数据。

需要特别说明的是, `reduceByKey` 和 `aggregateByKey` 会在 map 端创建这些结构, `'ByKey` 操作在 reduce 端生成这些结构。

当数据不能装入内存中时, Spark 会把数据存到磁盘上，这将导致额外的磁盘 I/O 开销和垃圾回收开销的增加。

> Shuffle also generates a large number of intermediate files on disk. As of Spark 1.3, these files are preserved until the corresponding RDDs are no longer used and are garbage collected. This is done so the shuffle files don’t need to be re-created if the lineage is re-computed. Garbage collection may happen only after a long period of time, if the application retains references to these RDDs or if GC does not kick in frequently. This means that long-running Spark jobs may consume a large amount of disk space. The temporary storage directory is specified by the `spark.local.dir` configuration parameter when configuring the Spark context.

shuffle 操作还会在磁盘上生成大量的中间文件。

在 Spark 1.3 中，这些文件将会保留至对应的 RDD 不再使用并被垃圾回收为止。这么做的好处是，如果要重新计算 RDD 的血统关系时, shuffle 操作产生的这些中间文件不需要重新创建。

如果应用程序长期保持对 RDD 的引用，或者垃圾回收不频繁，这将导致垃圾回收的周期比较长。这意味着，长时间运行的 Spark 作业可能会消耗大量的磁盘空间。

临时数据存储路径可以设置参数 `spark.local.dir` 进行配置。

> Shuffle behavior can be tuned by adjusting a variety of configuration parameters. See the ‘Shuffle Behavior’ section within the [Spark Configuration Guide](https://spark.apache.org/docs/3.3.2/configuration.html).

shuffle 操作的行为可以通过调节多个参数进行设置。

### RDD Persistence

> One of the most important capabilities in Spark is persisting (or caching) a dataset in memory across operations. When you persist an RDD, each node stores any partitions of it that it computes in memory and reuses them in other actions on that dataset (or datasets derived from it). This allows future actions to be much faster (often by more than 10x). Caching is a key tool for iterative algorithms and fast interactive use.

Spark 中一个很重要的能力是跨操作地将数据集持久化（或缓存）到内存中。

当持久化一个 RDD 时，每个节点存储它的任意分区，那么它在内存中计算，在那个数据集（或从它衍生的数据集）上的其他行动算子中再次使用。

这样会让以后的行动算子计算速度加快（通常超过 10 倍）。缓存是迭代算法和快速的交互式使用的重要工具。

> You can mark an RDD to be persisted using the `persist()` or `cache()` methods on it. The first time it is computed in an action, it will be kept in memory on the nodes. Spark’s cache is fault-tolerant – if any partition of an RDD is lost, it will automatically be recomputed using the transformations that originally created it.

可以使用 persist() 方法或 cache() 方法持久化 RDD. 数据将会在第一次执行行动算子时计算，它将被缓存在节点的内存中。

Spark 的缓存具有容错机制，如果 RDD 的任意分区丢失了，它将自动地使用原来的转换算子自动重新计算。

> In addition, each persisted RDD can be stored using a different storage level, allowing you, for example, to persist the dataset on disk, persist it in memory but as serialized Java objects (to save space), replicate it across nodes. These levels are set by passing a `StorageLevel` object ([Scala](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/storage/StorageLevel.html), [Java](https://spark.apache.org/docs/3.3.2/api/java/index.html?org/apache/spark/storage/StorageLevel.html), [Python](https://spark.apache.org/docs/3.3.2/api/python/reference/api/pyspark.StorageLevel.html#pyspark.StorageLevel)) to `persist()`. The `cache()` method is a shorthand for using the default storage level, which is `StorageLevel.MEMORY_ONLY` (store deserialized objects in memory). The full set of storage levels is:

另外，每个持久化的 RDD 可以使用不同的存储级别存储。例如，将数据集持久化到磁盘、作为序列化后的 Java 对象持久化到内存（可以节省空间）、跨节点复制。

这些存储级别通过给 persist() 方法传递一个 StorageLevel 对象设置。 

cache() 方法是使用默认存储级别的快捷设置方法，默认的存储级别是 StorageLevel.MEMORY_ONLY（将反序列化的对象存储到内存中）。

详细的存储级别介绍如下:

Storage Level | Meaning
---|:---
MEMORY_ONLY | Store RDD as deserialized Java objects in the JVM. If the RDD does not fit in memory, some partitions will not be cached and will be recomputed on the fly each time they're needed. This is the default level.<br/>将 RDD 以反序列化的 Java 对象的形式存储在 JVM 中。如果内存空间不够，一些分区将不再缓存，而是在每次需要用到这些数据时重新进行计算。这是默认的级别。
MEMORY_AND_DISK | Store RDD as deserialized Java objects in the JVM. If the RDD does not fit in memory, store the partitions that don't fit on disk, and read them from there when they're needed.<br/>将 RDD 以反序列化的 Java 对象的形式存储在 JVM 中。如果内存空间不够，将未缓存的分区存储到磁盘，在需要使用时从磁盘读取。
MEMORY_ONLY_SER(Java and Scala)  | Store RDD as serialized Java objects (one byte array per partition). This is generally more space-efficient than deserialized objects, especially when using a [fast serializer](https://spark.apache.org/docs/3.3.2/tuning.html), but more CPU-intensive to read.<br/>将 RDD 以序列化的 Java 对象的形式进行存储（每个分区为一个字节数组）。这种方式会比反序列化对象的方式节省很多空间，尤其是在使用 fast serializer时，但是会加重 CPU 的计算负担。
MEMORY_AND_DISK_SER(Java and Scala)  |Similar to MEMORY_ONLY_SER, but spill partitions that don't fit in memory to disk instead of recomputing them on the fly each time they're needed.<br/>类似于 MEMORY_ONLY_SER,但是溢出的分区会存储到磁盘，而不是在用到它们时重新计算。  
DISK_ONLY | Store the RDD partitions only on disk.<br/>只在磁盘上缓存 RDD
MEMORY_ONLY_2, MEMORY_AND_DISK_2, etc.  | Same as the levels above, but replicate each partition on two cluster nodes.与上面的级别功能相同，只不过每个分区只在集群的两个节点上建立副本。
OFF_HEAP (experimental) | Similar to MEMORY_ONLY_SER, but store the data in [off-heap memory](https://spark.apache.org/docs/3.3.2/configuration.html#memory-management). This requires off-heap memory to be enabled.<br/>类似于 MEMORY_ONLY_SER，但是将数据存储在堆外内存中。这需要启用 off-heap 内存。

> Note: In Python, stored objects will always be serialized with the [Pickle](https://docs.python.org/3/library/pickle.html) library, so it does not matter whether you choose a serialized level. The available storage levels in Python include `MEMORY_ONLY`, `MEMORY_ONLY_2`, `MEMORY_AND_DISK`, `MEMORY_AND_DISK_2`, `DISK_ONLY`, `DISK_ONLY_2`, and `DISK_ONLY_3`.

在 Python 中，存储对象总是使用 Pickle 库序列化对象，所以你选择哪种序列化级别都没关系。在 Python 中可用的存储级别有MEMORY_ONLY`, `MEMORY_ONLY_2`, `MEMORY_AND_DISK`, `MEMORY_AND_DISK_2`, `DISK_ONLY`, `DISK_ONLY_2`, and `DISK_ONLY_3`.

> Spark also automatically persists some intermediate data in shuffle operations (e.g. `reduceByKey`), even without users calling `persist`. This is done to avoid recomputing the entire input if a node fails during the shuffle. We still recommend users call `persist` on the resulting RDD if they plan to reuse it.

在 shuffle 操作中（例如 reduceByKey），即便用户没有调用 persist 方法，Spark 也会自动缓存一些中间数据。

这么做的目的是，如果在 shuffle 的过程中某个节点运行失败时，就不需要重新计算所有的输入数据。如果用户想多次使用某个 RDD，强烈推荐在该 RDD 上调用 persist 方法。

#### Which Storage Level to Choose?

> Spark’s storage levels are meant to provide different trade-offs between memory usage and CPU efficiency. We recommend going through the following process to select one:

Spark 的存储级别的为了在内存使用率和 CPU 效率之间进行权衡。建议按下面的过程进行存储级别的选择:

> If your RDDs fit comfortably with the default storage level (`MEMORY_ONLY`), leave them that way. This is the most CPU-efficient option, allowing operations on the RDDs to run as fast as possible.

- 如果 RDD 适合于默认存储级别MEMORY_ONLY, 就保持这种配置。这是 CPU 效率最高的选项，让 RDD 上的操作尽可能快地运行。

> If not, try using `MEMORY_ONLY_SER` and [selecting a fast serialization library](https://spark.apache.org/docs/3.3.2/tuning.html) to make the objects much more space-efficient, but still reasonably fast to access. (Java and Scala)

- 如果不是，试着使用 MEMORY_ONLY_SER 和选择一个能快速序列化的库，以使对象更加节省空间，但仍然能够快速访问。(Java和Scala)

> Don’t spill to disk unless the functions that computed your datasets are expensive, or they filter a large amount of the data. Otherwise, recomputing a partition may be as fast as reading it from disk.

- 除非计算数据集的函数是消耗很多资源，或者过滤大量的数据，否则不要溢出到磁盘。不然，重新计算分区可能与从磁盘读取分区一样快。

> Use the replicated storage levels if you want fast fault recovery (e.g. if using Spark to serve requests from a web application). All the storage levels provide full fault tolerance by recomputing lost data, but the replicated ones let you continue running tasks on the RDD without waiting to recompute a lost partition.

- 如果需要快速故障恢复，请使用复制的存储级别（例如，如果使用 Spark 来服务来自网络应用程序的请求）。所有的 存储级别通过重新计算丢失的数据来提供完整的容错能力，但副本可继续在 RDD 上运行任务，而无需等待重新计算一个丢失的分区。

#### Removing Data

> Spark automatically monitors cache usage on each node and drops out old data partitions in a least-recently-used (LRU) fashion. If you would like to manually remove an RDD instead of waiting for it to fall out of the cache, use the `RDD.unpersist()` method. Note that this method does not block by default. To block until resources are freed, specify `blocking=true` when calling this method.

Spark 会自动监视每个节点上的缓存使用情况，并按照 least-recently-used(LRU) 方式丢弃旧数据分区。

如果想手动删除 RDD, 使用 RDD.unpersist() 方法。

这个方法默认是不阻塞的。若要阻塞直到释放资源，请在调用此方法时指定 `blocking=true`

## Shared Variables

> Normally, when a function passed to a Spark operation (such as `map` or `reduce`) is executed on a remote cluster node, it works on separate copies of all the variables used in the function. These variables are copied to each machine, and no updates to the variables on the remote machine are propagated back to the driver program. Supporting general, read-write shared variables across tasks would be inefficient. However, Spark does provide two limited types of `shared variables` for two common usage patterns: broadcast variables and accumulators.

通常，一个传递给 Spark 操作（例如 map 或 reduce）的函数是在远程集群节点上执行的，该函数在所有变量的不同副本上执行计算。

这些变量被复制到每台机器上，并且各个远程机器上变量的更新并不会传播回驱动程序。支持的通用的读写共享变量在任务间是低效的。所以，Spark 提供了两种特定类型的共享变量: 广播变量和累加器。

### Broadcast Variables

> Broadcast variables allow the programmer to keep a read-only variable cached on each machine rather than shipping a copy of it with tasks. They can be used, for example, to give every node a copy of a large input dataset in an efficient manner. Spark also attempts to distribute broadcast variables using efficient broadcast algorithms to reduce communication cost.

广播变量允许程序员将一个只读变量缓存到每台机器上，而不是传递一个副本

。例如，广播变量可以用一种高效的方式给每个节点传递一份比较大的输入数据集副本。在使用广播变量时，Spark 也尝试使用高效广播算法分发广播变量，以降低通信成本。

> Spark actions are executed through a set of stages, separated by distributed “shuffle” operations. Spark automatically broadcasts the common data needed by tasks within each stage. The data broadcasted this way is cached in serialized form and deserialized before running each task. This means that explicitly creating broadcast variables is only useful when tasks across multiple stages need the same data or when caching the data in deserialized form is important.

Spark 的行动算子是通过一系列的阶段执行的，这些阶段是通过分布式的 shuffle 操作进行拆分的。

Spark 会自动广播出每个阶段内任务所需要的公共数据。以这种方式广播的数据以序列化的形式缓存，并在每个任务运行前进行反序列化。

这也就意味着，只有在跨越多个阶段的任务使用相同的数据，或者在使用反序列化形式缓存数据时，使用广播变量会有比较好的效果。

> Broadcast variables are created from a variable `v` by calling `SparkContext.broadcast(v)`. The broadcast variable is a wrapper around `v`, and its value can be accessed by calling the `value` method. The code below shows this:

广播变量通过在一个变量 v 上调用 `SparkContext.broadcast(v)` 方法创建。广播变量是 v 的一个包装，可以通过调用 `value` 方法访问它的值。代码示例如下:

```scala
scala> val broadcastVar = sc.broadcast(Array(1, 2, 3))
broadcastVar: org.apache.spark.broadcast.Broadcast[Array[Int]] = Broadcast(0)

scala> broadcastVar.value
res0: Array[Int] = Array(1, 2, 3)
```

```java
Broadcast<int[]> broadcastVar = sc.broadcast(new int[] {1, 2, 3});

broadcastVar.value();
// returns [1, 2, 3]
```

```python
>>> broadcastVar = sc.broadcast([1, 2, 3])
<pyspark.broadcast.Broadcast object at 0x102789f10>

>>> broadcastVar.value
[1, 2, 3]
```

> After the broadcast variable is created, it should be used instead of the value `v` in any functions run on the cluster so that `v` is not shipped to the nodes more than once. In addition, the object `v` should not be modified after it is broadcast in order to ensure that all nodes get the same value of the broadcast variable (e.g. if the variable is shipped to a new node later).

在创建广播变量之后，在集群上执行的所有的函数中，应该使用该广播变量，而不是原来的 v 值，这样节点上的 v 最多分发一次。

另外，对象 v 在广播后不应该再被修改，以保证分发到所有的节点上的广播变量具有同样的值（例如，如果以后该变量会被运到一个新的节点）。

> To release the resources that the broadcast variable copied onto executors, call `.unpersist()`. If the broadcast is used again afterwards, it will be re-broadcast. To permanently release all resources used by the broadcast variable, call `.destroy()`. The broadcast variable can’t be used after that. Note that these methods do not block by default. To block until resources are freed, specify `blocking=true` when calling them.

为了释放分发广播变量占用的资源，调用 `.unpersist() `方法释放。如果该广播变量在后面再次被使用，则会再次广播。

如果要彻底释放资源，可以使用 `.destroy()` 方法，之后，该广播变量就不能再使用了。

这些方法默认是不阻塞的。若要阻塞直到释放资源，请在调用此方法时指定 `blocking=true` 。

### Accumulators

> Accumulators are variables that are only “added” to through an associative and commutative operation and can therefore be efficiently supported in parallel. They can be used to implement counters (as in MapReduce) or sums. Spark natively supports accumulators of numeric types, and programmers can add support for new types.

累加器是一个仅可以通过 associative 和 commutative 执行 added 的变量，因此可以有效地并行支持。它们用来实现计数和加和。

原生 Spark 支持数值型的累加器，并且程序员可以添加新的支持类型。

> As a user, you can create named or unnamed accumulators. As seen in the image below, a named accumulator (in this instance `counter`) will display in the web UI for the stage that modifies that accumulator. Spark displays the value for each accumulator modified by a task in the “Tasks” table.

用户可以创建命名或未命名的累加器。如下图所示，一个命名的累加器（在这个例子中是 counter）显示在 web UI 中，处在修改累加器的阶段。 Spark 在 Tasks 任务表中显示了任务修改的每个累加器的值。

![spark-webui-accumulators.png](./images/spark-webui-accumulators.png)

> Tracking accumulators in the UI can be useful for understanding the progress of running stages (NOTE: this is not yet supported in Python).

在 UI 中跟踪累加器可以有助于了解运行阶段的进度（注：这在 Python 中尚不支持）.

#### Scala

> A numeric accumulator can be created by calling `SparkContext.longAccumulator()` or `SparkContext.doubleAccumulator()` to accumulate values of type Long or Double, respectively. Tasks running on a cluster can then add to it using the `add` method. However, they cannot read its value. Only the driver program can read the accumulator’s value, using its value method.

可以通过调用 `SparkContext.longAccumulator()` 或 `SparkContext.doubleAccumulator()` 方法创建数值类型的累加器，以分别累加 Long 或 Double 类型的值。

集群上正在运行的任务就可以使用 add 方法累计数值。然而，它们不能够读取它的值。只有驱动程序才可以使用 value 方法读取累加器的值。

> The code below shows an accumulator being used to add up the elements of an array:

下面的代码展示了一个累加器用于对一个数组中的元素求和:

```
scala> val accum = sc.longAccumulator("My Accumulator")
accum: org.apache.spark.util.LongAccumulator = LongAccumulator(id: 0, name: Some(My Accumulator), value: 0)

scala> sc.parallelize(Array(1, 2, 3, 4)).foreach(x => accum.add(x))
...
10/09/29 18:41:08 INFO SparkContext: Tasks finished in 0.317106 s

scala> accum.value
res2: Long = 10
```

> While this code used the built-in support for accumulators of type Long, programmers can also create their own types by subclassing [AccumulatorV2](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/util/AccumulatorV2.html). The AccumulatorV2 abstract class has several methods which one has to override: `reset` for resetting the accumulator to zero, `add` for adding another value into the accumulator, `merge` for merging another same-type accumulator into this one. Other methods that must be overridden are contained in the [API documentation](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/util/AccumulatorV2.html). For example, supposing we had a `MyVector` class representing mathematical vectors, we could write:

虽然此代码使用 Long 类型累加器的内置支持，但是开发者通过 AccumulatorV2 的子类创建自己的类型。

AccumulatorV2 抽象类有几个需要重写的方法：

- reset 方法可将累加器重置为 0
- add 方法可将其它值添加到累加器中
- merge 方法可将其他同样类型的累加器合并为一个。

其他需要重写的方法可参考 API documentation。

例如，假设我们有一个表示数学上向量的 MyVector 类，我们可以写成:

```scala
class VectorAccumulatorV2 extends AccumulatorV2[MyVector, MyVector] {

  private val myVector: MyVector = MyVector.createZeroVector

  def reset(): Unit = {
    myVector.reset()
  }

  def add(v: MyVector): Unit = {
    myVector.add(v)
  }
  ...
}

// Then, create an Accumulator of this type:
val myVectorAcc = new VectorAccumulatorV2
// Then, register it into spark context:
sc.register(myVectorAcc, "MyVectorAcc1")
```

> Note that, when programmers define their own type of AccumulatorV2, the resulting type can be different than that of the elements added.

注意:在开发者定义自己的 AccumulatorV2 类型时，返回值类型可能与添加的元素的类型不一致。

> For accumulator updates performed inside actions only, Spark guarantees that each task’s update to the accumulator will only be applied once, i.e. restarted tasks will not update the value. In transformations, users should be aware of that each task’s update may be applied more than once if tasks or job stages are re-executed.

累加器的更新只发生在行动算子中，Spark 保证每个任务只更新累加器一次，例如，重启任务不会更新值。

在转换算子中，如果任务或作业的阶段重新执行，每个任务的更新操作可能会执行多次。

> Accumulators do not change the lazy evaluation model of Spark. If they are being updated within an operation on an RDD, their value is only updated once that RDD is computed as part of an action. Consequently, accumulator updates are not guaranteed to be executed when made within a lazy transformation like `map()`. The below code fragment demonstrates this property:

累加器不会改变 Spark 懒加载模式。

如果累加器在 RDD 中的一个操作中进行更新，它们的值仅被作为行动算子的一部分更新一次。 因此，在一个像 map() 这样的转换算子中，累加器的更新不保证被执行。

下面的代码片段证明了这个特性:

```scala
val accum = sc.longAccumulator
data.map { x => accum.add(x); x }
// Here, accum is still 0 because no actions have caused the map operation to be computed.
```

#### Java

> A numeric accumulator can be created by calling `SparkContext.longAccumulator()` or `SparkContext.doubleAccumulator()` to accumulate values of type Long or Double, respectively. Tasks running on a cluster can then add to it using the add method. However, they cannot read its value. Only the driver program can read the accumulator’s value, using its value method.

可以通过调用 `SparkContext.longAccumulator()` 或 `SparkContext.doubleAccumulator()` 方法创建数值类型的累加器，以分别累加 Long 或 Double 类型的值。

集群上正在运行的任务就可以使用 add 方法累计数值。然而，它们不能够读取它的值。只有驱动程序才可以使用 value 方法读取累加器的值。

The code below shows an accumulator being used to add up the elements of an array:

下面的代码展示了一个累加器用于对一个数组中的元素求和:

```
LongAccumulator accum = jsc.sc().longAccumulator();

sc.parallelize(Arrays.asList(1, 2, 3, 4)).foreach(x -> accum.add(x));
// ...
// 10/09/29 18:41:08 INFO SparkContext: Tasks finished in 0.317106 s

accum.value();
// returns 10
```

> While this code used the built-in support for accumulators of type Long, programmers can also create their own types by subclassing [AccumulatorV2](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/util/AccumulatorV2.html). The AccumulatorV2 abstract class has several methods which one has to override: `reset` for resetting the accumulator to zero, `add` for adding another value into the accumulator, `merge` for merging another same-type accumulator into this one. Other methods that must be overridden are contained in the [API documentation](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/util/AccumulatorV2.html). For example, supposing we had a `MyVector` class representing mathematical vectors, we could write:

虽然此代码使用 Long 类型累加器的内置支持，但是开发者通过 AccumulatorV2 的子类创建自己的类型。

AccumulatorV2 抽象类有几个需要重写的方法：

- reset 方法可将累加器重置为 0
- add 方法可将其它值添加到累加器中
- merge 方法可将其他同样类型的累加器合并为一个。

其他需要重写的方法可参考 API documentation。

例如，假设我们有一个表示数学上向量的 MyVector 类，我们可以写成:

```java
class VectorAccumulatorV2 implements AccumulatorV2<MyVector, MyVector> {

  private MyVector myVector = MyVector.createZeroVector();

  public void reset() {
    myVector.reset();
  }

  public void add(MyVector v) {
    myVector.add(v);
  }
  ...
}

// Then, create an Accumulator of this type:
VectorAccumulatorV2 myVectorAcc = new VectorAccumulatorV2();
// Then, register it into spark context:
jsc.sc().register(myVectorAcc, "MyVectorAcc1");
```

> Note that, when programmers define their own type of AccumulatorV2, the resulting type can be different than that of the elements added.

注意:在开发者定义自己的 AccumulatorV2 类型时，返回值类型可能与添加的元素的类型不一致。

> Warning: When a Spark task finishes, Spark will try to merge the accumulated updates in this task to an accumulator. If it fails, Spark will ignore the failure and still mark the task successful and continue to run other tasks. Hence, a buggy accumulator will not impact a Spark job, but it may not get updated correctly although a Spark job is successful.

警告：当一个 spark 任务完成时，spark 将尝试合并这个任务中的累加更新到累加器中。如果失败了，spark 将忽略失败，仍将任务标记为成功，继续运行其他任务。因此，一个有 bug 的累加器不会影响 spark 作业，尽管 spark 作业是成功，但它可能不能正确更新。

> For accumulator updates performed inside actions only, Spark guarantees that each task’s update to the accumulator will only be applied once, i.e. restarted tasks will not update the value. In transformations, users should be aware of that each task’s update may be applied more than once if tasks or job stages are re-executed.

累加器的更新只发生在行动算子中，Spark 保证每个任务只更新累加器一次，例如，重启任务不会更新值。

在转换算子中，如果任务或作业的阶段重新执行，每个任务的更新操作可能会执行多次。

> Accumulators do not change the lazy evaluation model of Spark. If they are being updated within an operation on an RDD, their value is only updated once that RDD is computed as part of an action. Consequently, accumulator updates are not guaranteed to be executed when made within a lazy transformation like `map()`. The below code fragment demonstrates this property:

累加器不会改变 Spark 懒加载模式。

如果累加器在 RDD 中的一个操作中进行更新，它们的值仅被作为行动算子的一部分更新一次。 因此，在一个像 map() 这样的转换算子中，累加器的更新不保证被执行。

下面的代码片段证明了这个特性:

```java
LongAccumulator accum = jsc.sc().longAccumulator();
data.map(x -> { accum.add(x); return f(x); });
// Here, accum is still 0 because no actions have caused the `map` to be computed.
```

#### Python

> An accumulator is created from an initial value `v` by calling `SparkContext.accumulator(v)`. Tasks running on a cluster can then add to it using the `add` method or the `+=` operator. However, they cannot read its value. Only the driver program can read the accumulator’s value, using its value method.

根据一个初始值 v ，通过调用 `SparkContext.accumulator(v)` 方法创建累加器。

集群上正在运行的任务就可以使用 add 方法或者 `+=` 操作符累加数值。然而，它们不能够读取它的值。只有驱动程序才可以使用 value 方法读取累加器的值。

> The code below shows an accumulator being used to add up the elements of an array:

下面的代码展示了一个累加器用于对一个数组中的元素求和:

```
>>> accum = sc.accumulator(0)
>>> accum
Accumulator<id=0, value=0>

>>> sc.parallelize([1, 2, 3, 4]).foreach(lambda x: accum.add(x))
...
10/09/29 18:41:08 INFO SparkContext: Tasks finished in 0.317106 s

>>> accum.value
10
```

> While this code used the built-in support for accumulators of type Int, programmers can also create their own types by subclassing [AccumulatorParam](https://spark.apache.org/docs/3.3.2/api/python/reference/api/pyspark.AccumulatorParam.html#pyspark.AccumulatorParam). The AccumulatorParam interface has two methods: `zero` for providing a “zero value” for your data type, and `addInPlace` for adding two values together. For example, supposing we had a `Vector` class representing mathematical vectors, we could write:

虽然此代码使用内置支持的 Int 类型的累加器，但是程序员可以通过创建 AccumulatorParam 的子类自定义类型。

AccumulatorParam 有两个方法：

- zero用来提供一个初始零值。
- addInPlace 用来加两个值。

例如，有一个表示数学向量的 Vector 类，可以这么写：

```python
class VectorAccumulatorParam(AccumulatorParam):
    def zero(self, initialValue):
        return Vector.zeros(initialValue.size)

    def addInPlace(self, v1, v2):
        v1 += v2
        return v1

# Then, create an Accumulator of this type:
vecAccum = sc.accumulator(Vector(...), VectorAccumulatorParam())
```

> For accumulator updates performed inside actions only, Spark guarantees that each task’s update to the accumulator will only be applied once, i.e. restarted tasks will not update the value. In transformations, users should be aware of that each task’s update may be applied more than once if tasks or job stages are re-executed.

累加器的更新只发生在行动算子中，Spark 保证每个任务只更新累加器一次，例如，重启任务不会更新值。

在转换算子中，如果任务或作业的阶段重新执行，每个任务的更新操作可能会执行多次。

> Accumulators do not change the lazy evaluation model of Spark. If they are being updated within an operation on an RDD, their value is only updated once that RDD is computed as part of an action. Consequently, accumulator updates are not guaranteed to be executed when made within a lazy transformation like `map()`. The below code fragment demonstrates this property:

累加器不会改变 Spark 懒加载模式。

如果累加器在 RDD 中的一个操作中进行更新，它们的值仅被作为行动算子的一部分更新一次。 因此，在一个像 map() 这样的转换算子中，累加器的更新不保证被执行。

下面的代码片段证明了这个特性:

```python
accum = sc.accumulator(0)
def g(x):
    accum.add(x)
    return f(x)
data.map(g)
# Here, accum is still 0 because no actions have caused the `map` to be computed.
```

## Deploying to a Cluster

> The [application submission guide](https://spark.apache.org/docs/3.3.2/submitting-applications.html) describes how to submit applications to a cluster. In short, once you package your application into a JAR (for Java/Scala) or a set of `.py` or `.zip` files (for Python), the `bin/spark-submit` script lets you submit it to any supported cluster manager.

应用程序提交指南描述了如何提交应用程序到集群。

总之，一旦将应用程序打包为 JAR (for Java/Scala) 、 `.py` 集合或 `.zip` (for Python)，再使用 `bin/spark-submit` 脚本将它提交到任意支持的集群管理器中。

## Launching Spark jobs from Java / Scala

> The `org.apache.spark.launcher` package provides classes for launching Spark jobs as child processes using a simple Java API.

`org.apache.spark.launcher` 包提供了类，用于使用简单的 Java API 作为一个子进程启动 Spark jobs.

## Unit Testing

> Spark is friendly to unit testing with any popular unit test framework. Simply create a `SparkContext` in your test with the master URL set to `local`, run your operations, and then call `SparkContext.stop()` to tear it down. Make sure you stop the context within a `finally` block or the test framework’s `tearDown` method, as Spark does not support two contexts running concurrently in the same program.

Spark 可以使用任意流行的单元测试框架进行单元测试。

在你的测试中，将 master URL 设置为 local, 会简单的创建一个 SparkContext 运行你的操作，然后调用 `SparkContext.stop()` 将该作业停止。

因为 Spark 不支持在同一个程序中并行的运行两个上下文，所以需要确保使用 finally 块或者测试框架的 tearDown 方法停止了上下文。

## Where to Go from Here

You can see some [example Spark programs](https://spark.apache.org/examples.html) on the Spark website. In addition, Spark includes several samples in the `examples` directory ([Scala](https://github.com/apache/spark/tree/master/examples/src/main/scala/org/apache/spark/examples), [Java](https://github.com/apache/spark/tree/master/examples/src/main/java/org/apache/spark/examples), [Python](https://github.com/apache/spark/tree/master/examples/src/main/python), [R](https://github.com/apache/spark/tree/master/examples/src/main/r)). You can run Java and Scala examples by passing the class name to Spark’s `bin/run-example` script; for instance:

    ./bin/run-example SparkPi

For Python examples, use `spark-submit` instead:

    ./bin/spark-submit examples/src/main/python/pi.py

For R examples, use `spark-submit` instead:

    ./bin/spark-submit examples/src/main/r/dataframe.R

For help on optimizing your programs, the [configuration](https://spark.apache.org/docs/3.3.2/configuration.html) and [tuning](https://spark.apache.org/docs/3.3.2/tuning.html) guides provide information on best practices. They are especially important for making sure that your data is stored in memory in an efficient format. For help on deploying, the [cluster mode overview](https://spark.apache.org/docs/3.3.2/cluster-overview.html) describes the components involved in distributed operation and supported cluster managers.

Finally, full API documentation is available in [Scala](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/), [Java](https://spark.apache.org/docs/3.3.2/api/java/), [Python](https://spark.apache.org/docs/3.3.2/api/python/) and [R](https://spark.apache.org/docs/3.3.2/api/R/).