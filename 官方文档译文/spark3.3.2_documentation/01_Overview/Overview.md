# Overview

[TOC]

## Spark Overview

> Apache Spark is a unified analytics engine for large-scale data processing. It provides high-level APIs in Java, Scala, Python and R, and an optimized engine that supports general execution graphs. 

Apache Spark 是一个为大规模数据处理而生的统一分析引擎。它提供了针对 Java, Scala, Python 和 R 的高级 APIs，也提供了一个支持通用图计算的优化引擎。

> It also supports a rich set of higher-level tools including [Spark SQL](https://spark.apache.org/docs/3.3.2/sql-programming-guide.html) for SQL and structured data processing, [pandas API on Spark](https://spark.apache.org/docs/3.3.2/api/python/getting_started/quickstart_ps.html) for pandas workloads, [MLlib](https://spark.apache.org/docs/3.3.2/ml-guide.html) for machine learning, [GraphX](https://spark.apache.org/docs/3.3.2/graphx-programming-guide.html) for graph processing, and [Structured Streaming](https://spark.apache.org/docs/3.3.2/structured-streaming-programming-guide.html) for incremental computation and stream processing.

它也包括了更高级的工具：用于 SQL 和结构化数据处理的 Spark SQL; 用于执行 pandas 计算的 pandas API on Spark;用于机器学习的 MLlib; 用于图计算的 GraphX; 用于增量计算和流处理的 Structured Streaming.

## Downloading

> Get Spark from the [downloads page](https://spark.apache.org/downloads.html) of the project website. This documentation is for Spark version 3.3.2. Spark uses Hadoop’s client libraries for HDFS and YARN. Downloads are pre-packaged for a handful of popular Hadoop versions. Users can also download a “Hadoop free” binary and run Spark with any Hadoop version [by augmenting Spark’s classpath](https://spark.apache.org/docs/3.3.2/hadoop-provided.html). Scala and Java users can include Spark in their projects using its Maven coordinates and Python users can install Spark from PyPI.

从项目网站的下载页获取 Spark. 这个文档是 Spark 3.3.2 版本。 为使用 HDFS 和 YARN, Spark 使用 Hadoop 的客户端库。下载的 Spark 是包含了 Hadoop 的。用户也可以下载不含 Hadoop 的版本，在运行 Spark 时，[通过 Spark 类路径参数指定任意的 Hadoop 版本]()。

Scala 和 Java 用户可以通过使用 Maven 在工程中包含 Spark, Python 用户可以从 PyPi 安装 Spark.

> If you’d like to build Spark from source, visit [Building Spark](https://spark.apache.org/docs/3.3.2/building-spark.html).

如果你想从源代码构建 Spark, 访问构建 Spark.

> Spark runs on both Windows and UNIX-like systems (e.g. Linux, Mac OS), and it should run on any platform that runs a supported version of Java. This should include JVMs on x86_64 and ARM64. It’s easy to run locally on one machine — all you need is to have java installed on your system PATH, or the JAVA_HOME environment variable pointing to a Java installation.

Spark 可以运行在 Windows 和 Unix 系统。它运行在能运行 Java 的平台上。这包括了 x86_64 和 ARM64 的 JVMs.

在一台机器上本地运行也很简单：只需安装 java, 并在系统 Path 中指定，或通过 JAVA_HOME 环境变量指定。

> Spark runs on Java 8/11/17, Scala 2.12/2.13, Python 3.7+ and R 3.5+. Java 8 prior to version 8u201 support is deprecated as of Spark 3.2.0. When using the Scala API, it is necessary for applications to use the same version of Scala that Spark was compiled for. For example, when using Scala 2.13, use Spark compiled for 2.13, and compile code/applications for Scala 2.13 as well.

Spark 运行在 ava 8/11/17, Scala 2.12/2.13, Python 3.7+ and R 3.5+ 上。在 Spark 3.2.0 版本弃用了 Java 8u201 之前的版本。

当使用 Scala API 时，编译 Spark 的 Scala 版本和应用程序使用的版本应该相同。例如，当使用 Scala 2.13 时, 使用 2.13 编译的 Spark,并使用 Scala 2.13 编译程序。 

> For Python 3.9, Arrow optimization and pandas UDFs might not work due to the supported Python versions in Apache Arrow. Please refer to the latest [Python Compatibility](https://arrow.apache.org/docs/python/install.html#python-compatibility) page. For Java 11, `-Dio.netty.tryReflectionSetAccessible=true` is required additionally for Apache Arrow library. This prevents `java.lang.UnsupportedOperationException: sun.misc.Unsafe` or `java.nio.DirectByteBuffer.(long, int) not available` when Apache Arrow uses Netty internally.

对于 Python 3.9, 由于 Apache Arrow 中支持的 Python 版本， Arrow 优化和 pandas UDFs 可能并不可用。请参考最新的 Python 兼容页。

对于 Java 11, 要求 `-Dio.netty.tryReflectionSetAccessible=true`。当 Apache Arrow 内部使用 Netty 时，这就避免了 `java.lang.UnsupportedOperationException: sun.misc.Unsafe` 或 `java.nio.DirectByteBuffer.(long, int)` 不可用。

## Running the Examples and Shell

> Spark comes with several sample programs. Scala, Java, Python and R examples are in the `examples/src/main` directory. To run one of the Java or Scala sample programs, use `bin/run-example <class> [params]` in the top-level Spark directory. (Behind the scenes, this invokes the more general [spark-submit script](https://spark.apache.org/docs/3.3.2/submitting-applications.html) for launching applications). For example,

Spark 自带了一些示例程序。 Scala, Java, Python 和 R 示例都在 `examples/src/main` 目录。在 Spark 顶层目录使用 `bin/run-example <class> [params]` 命令来运行。（这会调用 spark-submit 脚本来启动应用程序）。例如：

	./bin/run-example SparkPi 10

> You can also run Spark interactively through a modified version of the Scala shell. This is a great way to learn the framework.

也可以使用修改了的 Scala shell 交互地运行 Spark

	./bin/spark-shell --master local[2]

> The `--master` option specifies the [master URL for a distributed cluster](https://spark.apache.org/docs/3.3.2/submitting-applications.html#master-urls), or `local` to run locally with one thread, or `local[N]` to run locally with N threads. You should start by using `local` for testing. For a full list of options, run Spark shell with the `--help` option.

`--master` 选项可以指定了一个分布式集群的主 URL, 或者是表示运行一个线程的 `local`, 或者是运行多个线程的 `local[N]`. 可以使用 `local` 测试。

运行 Spark shell 时添加 `--help` 选项可以查看完整的选项列表。

> Spark also provides a Python API. To run Spark interactively in a Python interpreter, use `bin/pyspark`:

Spark 提供了 Python API. 使用 `bin/pyspark` 在 Python 解释器中交互地运行 Spark.

	./bin/pyspark --master local[2]

> Example applications are also provided in Python. For example,

也提供了 Python 版本的示例程序

	./bin/spark-submit examples/src/main/python/pi.py 10

> Spark also provides an [R API](https://spark.apache.org/docs/3.3.2/sparkr.html) since 1.4 (only DataFrames APIs included). To run Spark interactively in an R interpreter, use `bin/sparkR`:

从 1.4(仅包括DataFrames APIs) 版本开始, Spark 提供了 R API. 使用 `bin/sparkR` 在 R 解释器中交互地运行 Spark.

	./bin/sparkR --master local[2]

> Example applications are also provided in R. For example,

也提供了 R 版本的示例程序

	./bin/spark-submit examples/src/main/r/dataframe.R

## Launching on a Cluster

> The Spark [cluster mode overview](https://spark.apache.org/docs/3.3.2/cluster-overview.html) explains the key concepts in running on a cluster. Spark can run both by itself, or over several existing cluster managers. It currently provides several options for deployment:

Spark 集群模式总览解释了在集群运行的关键概念。 

- [Standalone Deploy Mode](https://spark.apache.org/docs/3.3.2/spark-standalone.html): simplest way to deploy Spark on a private cluster
- [Apache Mesos](https://spark.apache.org/docs/3.3.2/running-on-mesos.html) (deprecated)
- [Hadoop YARN](https://spark.apache.org/docs/3.3.2/running-on-yarn.html)
- [Kubernetes](https://spark.apache.org/docs/3.3.2/running-on-kubernetes.html)

## Where to Go from Here

**Programming Guides:**

- [Quick Start](https://spark.apache.org/docs/3.3.2/quick-start.html): a quick introduction to the Spark API; start here!
- [RDD Programming Guide](https://spark.apache.org/docs/3.3.2/rdd-programming-guide.html): overview of Spark basics - RDDs (core but old API), accumulators, and broadcast variables
- [Spark SQL, Datasets, and DataFrames](https://spark.apache.org/docs/3.3.2/sql-programming-guide.html): processing structured data with relational queries (newer API than RDDs)
- [Structured Streaming](https://spark.apache.org/docs/3.3.2/structured-streaming-programming-guide.html): processing structured data streams with relation queries (using Datasets and DataFrames, newer API than DStreams)
- [Spark Streaming](https://spark.apache.org/docs/3.3.2/streaming-programming-guide.html): processing data streams using DStreams (old API)
- [MLlib](https://spark.apache.org/docs/3.3.2/ml-guide.html): applying machine learning algorithms
- [GraphX](https://spark.apache.org/docs/3.3.2/graphx-programming-guide.html): processing graphs
- [SparkR](https://spark.apache.org/docs/3.3.2/sparkr.html): processing data with Spark in R
- [PySpark](https://spark.apache.org/docs/3.3.2/api/python/getting_started/index.html): processing data with Spark in Python
- [Spark SQL CLI](https://spark.apache.org/docs/3.3.2/sql-distributed-sql-engine-spark-sql-cli.html): processing data with SQL on the command line

**API Docs:**

- [Spark Scala API (Scaladoc)](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/index.html)
- [Spark Java API (Javadoc)](https://spark.apache.org/docs/3.3.2/api/java/index.html)
- [Spark Python API (Sphinx)](https://spark.apache.org/docs/3.3.2/api/python/index.html)
- [Spark R API (Roxygen2)](https://spark.apache.org/docs/3.3.2/api/R/index.html)
- [Spark SQL, Built-in Functions (MkDocs)](https://spark.apache.org/docs/3.3.2/api/sql/index.html)

**Deployment Guides:**

- [Cluster Overview](https://spark.apache.org/docs/3.3.2/cluster-overview.html): overview of concepts and components when running on a cluster
- [Submitting Applications](https://spark.apache.org/docs/3.3.2/submitting-applications.html): packaging and deploying applications
- Deployment modes:
	- [Amazon EC2](https://github.com/amplab/spark-ec2): scripts that let you launch a cluster on EC2 in about 5 minutes
	- [Standalone Deploy Mode](https://spark.apache.org/docs/3.3.2/spark-standalone.html): launch a standalone cluster quickly without a third-party cluster manager
	- [Mesos](https://spark.apache.org/docs/3.3.2/running-on-mesos.html): deploy a private cluster using [Apache Mesos](https://mesos.apache.org/)
	- [YARN](https://spark.apache.org/docs/3.3.2/running-on-yarn.html): deploy Spark on top of Hadoop NextGen (YARN)
	- [Kubernetes](https://spark.apache.org/docs/3.3.2/running-on-kubernetes.html): deploy Spark on top of Kubernetes

**Other Documents:**

- [Configuration](https://spark.apache.org/docs/3.3.2/configuration.html): customize Spark via its configuration system
- [Monitoring](https://spark.apache.org/docs/3.3.2/monitoring.html): track the behavior of your applications
- [Tuning Guide](https://spark.apache.org/docs/3.3.2/tuning.html): best practices to optimize performance and memory use
- [Job Scheduling](https://spark.apache.org/docs/3.3.2/job-scheduling.html): scheduling resources across and within Spark applications
- [Security](https://spark.apache.org/docs/3.3.2/security.html): Spark security support
- [Hardware Provisioning](https://spark.apache.org/docs/3.3.2/hardware-provisioning.html): recommendations for cluster hardware
- Integration with other storage systems:
	- [Cloud Infrastructures](https://spark.apache.org/docs/3.3.2/cloud-integration.html)
	- [OpenStack Swift](https://spark.apache.org/docs/3.3.2/storage-openstack-swift.html)
- [Migration Guide](https://spark.apache.org/docs/3.3.2/migration-guide.html): Migration guides for Spark components
- [Building Spark](https://spark.apache.org/docs/3.3.2/building-spark.html): build Spark using the Maven system
- [Contributing to Spark](https://spark.apache.org/contributing.html)
- [Third Party Projects](https://spark.apache.org/third-party-projects.html): related third party Spark projects

**External Resources:**

- [Spark Homepage](https://spark.apache.org/)
- [Spark Community](https://spark.apache.org/community.html) resources, including local meetups
- [StackOverflow tag apache-spark](http://stackoverflow.com/questions/tagged/apache-spark)
- [Mailing Lists](https://spark.apache.org/mailing-lists.html): ask questions about Spark here
- AMP Camps: a series of training camps at UC Berkeley that featured talks and exercises about Spark, Spark Streaming, Mesos, and more. [Videos](https://www.youtube.com/user/BerkeleyAMPLab/search?query=amp%20camp), are available online for free.
- [Code Examples](https://spark.apache.org/examples.html): more are also available in the `examples` subfolder of Spark ([Scala](https://github.com/apache/spark/tree/master/examples/src/main/scala/org/apache/spark/examples), [Java](https://github.com/apache/spark/tree/master/examples/src/main/java/org/apache/spark/examples), [Python](https://github.com/apache/spark/tree/master/examples/src/main/python), [R](https://github.com/apache/spark/tree/master/examples/src/main/r))