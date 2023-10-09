# Spark Streaming Programming Guide

[TOC]

## Note

> Spark Streaming is the previous generation of Spark’s streaming engine. There are no longer updates to Spark Streaming and it’s a legacy project. There is a newer and easier to use streaming engine in Spark called Structured Streaming. You should use Spark Structured Streaming for your streaming applications and pipelines. See [Structured Streaming Programming Guide](https://spark.apache.org/docs/3.3.2/structured-streaming-programming-guide.html).

Spark Streaming 是 spark 前一代流引擎。已不再更新，成为一个遗产项目。在 spark 中有一个更新、且使用更简单的流引擎，称为 Structured Streaming. 你应该为你的流应用程序和管道使用 Structured Streaming.

## Overview

> Spark Streaming is an extension of the core Spark API that enables scalable, high-throughput, fault-tolerant stream processing of live data streams. Data can be ingested from many sources like Kafka, Kinesis, or TCP sockets, and can be processed using complex algorithms expressed with high-level functions like map, reduce, join and window. Finally, processed data can be pushed out to filesystems, databases, and live dashboards. In fact, you can apply Spark’s [machine learning](https://spark.apache.org/docs/3.3.2/ml-guide.html) and [graph processing](https://spark.apache.org/docs/3.3.2/graphx-programming-guide.html) algorithms on data streams.

Spark Streaming 是 Spark core API 的扩展，具有对实时数据流处理的可扩展性、高吞吐和容错特性。

- 输入可以是Kafka, Kinesis, or TCP sockets

- 可以使用 map, reduce, join and window 等高级函数处理数据

- 处理后的数据可以输出到文件系统、数据库、实时看板

- 也可以在数据流上使用 spark 机器学习和图处理算法

![streaming-arch.png](./images/streaming-arch.png)

> Internally, it works as follows. Spark Streaming receives live input data streams and divides the data into batches, which are then processed by the Spark engine to generate the final stream of results in batches.

内部流程是: Spark Streaming 接收实时输入数据流，将其划分成多个批次，Spark 引擎处理批次，生成各批次的最终结果流。

![streaming-flow.png](./images/streaming-flow.png)

> Spark Streaming provides a high-level abstraction called discretized stream or DStream, which represents a continuous stream of data. DStreams can be created either from input data streams from sources such as Kafka, and Kinesis, or by applying high-level operations on other DStreams. Internally, a DStream is represented as a sequence of [RDDs](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/rdd/RDD.html).

DStream 表示一个持续的数据流。可以从 Kafka 或 Kinesis 等数据源创建，也可以通过在其他 DStream 上应用高级操作创建。 在内部，一个 DStream 就是一个 RDD 序列。

> This guide shows you how to start writing Spark Streaming programs with DStreams. You can write Spark Streaming programs in Scala, Java or Python (introduced in Spark 1.2), all of which are presented in this guide. You will find tabs throughout this guide that let you choose between code snippets of different languages.

本指南向你展示了如何使用 DStreams 写 spark streaming 程序。你可以使用 Scala, Java 或 Python(Spark 1.2引入) 写 spark streaming 程序

> Note: There are a few APIs that are either different or not available in Python. Throughout this guide, you will find the tag Python API highlighting these differences.

## A Quick Example

> Before we go into the details of how to write your own Spark Streaming program, let’s take a quick look at what a simple Spark Streaming program looks like. Let’s say we want to count the number of words in text data received from a data server listening on a TCP socket. All you need to do is as follows.

从 TCP socket 发送数据，再统计文本中单词的数量。

### Scala

> First, we import the names of the Spark Streaming classes and some implicit conversions from StreamingContext into our environment in order to add useful methods to other classes we need (like DStream). [StreamingContext](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/streaming/StreamingContext.html) is the main entry point for all streaming functionality. We create a local StreamingContext with two execution threads, and a batch interval of 1 second.

首先导入 Spark Streaming 类的名称和一些 StreamingContext 中的隐式转换。 

StreamingContext 是所有流式功能的主入口，我们创建了一个本地具有两个执行线程 StreamingContext, 批次为间隔1秒。

```scala
import org.apache.spark._
import org.apache.spark.streaming._
import org.apache.spark.streaming.StreamingContext._ // not necessary since Spark 1.3

// Create a local StreamingContext with two working thread and batch interval of 1 second.
// The master requires 2 cores to prevent a starvation scenario.

val conf = new SparkConf().setMaster("local[2]").setAppName("NetworkWordCount")
val ssc = new StreamingContext(conf, Seconds(1))
```

> Using this context, we can create a DStream that represents streaming data from a TCP source, specified as hostname `(e.g. localhost)` and port `(e.g. 9999)`.

使用这个上下文，我们创建 DStream, 它表示来自 TCP 源的流数据，该源由 hostname 和 port 指定。

```scala
// Create a DStream that will connect to hostname:port, like localhost:9999
val lines = ssc.socketTextStream("localhost", 9999)
```

> This `lines` DStream represents the stream of data that will be received from the data server. Each record in this DStream is a line of text. Next, we want to split the lines by space characters into words.

`lines` DStream 表示已从数据服务处接受到的数据流。

这个 DStream 中的每条记录是文本的一行。

下面的程序就是使用空格将行划分成单词。

```scala
// Split each line into words
val words = lines.flatMap(_.split(" "))
```

> `flatMap` is a one-to-many DStream operation that creates a new DStream by generating multiple new records from each record in the source DStream. In this case, each line will be split into multiple words and the stream of words is represented as the `words` DStream. Next, we want to count these words.

`flatMap` 是一个一对多的 DStream 操作，它会将原 DStream 中的每条记录生成多条记录，返回一个新的 DStream。

在此案例中，每行都会划分成多个单词，那么 words 流由 `words` DStream 表示。接下来，计算单词的数量。

```scala
import org.apache.spark.streaming.StreamingContext._ // not necessary since Spark 1.3
// Count each word in each batch
val pairs = words.map(word => (word, 1))
val wordCounts = pairs.reduceByKey(_ + _)

// Print the first ten elements of each RDD generated in this DStream to the console
wordCounts.print()
```

> The `words` DStream is further mapped (one-to-one transformation) to a DStream of `(word, 1)` pairs, which is then reduced to get the frequency of words in each batch of data. Finally, `wordCounts.print()` will print a few of the counts generated every second.

`words` DStream 被映射（一对一转换）成 `(word, 1)` 对的 DStream, 然后使用 reduceByKey 得到每个数据批次中单词的数量。最后，`wordCounts.pprint()` 将打印每秒生成的一些计数。

> Note that when these lines are executed, Spark Streaming only sets up the computation it will perform when it is started, and no real processing has started yet. To start the processing after all the transformations have been setup, we finally call

注意，当这些行被执行的时候，Spark Streaming 仅仅设置了计算，并没有开始真正地处理，只有在启动时才会执行。

为了在所有的转换都已经设置好之后开始处理，我们在最后调用:

```
ssc.start()             // Start the computation
ssc.awaitTermination()  // Wait for the computation to terminate
```

> The complete code can be found in the Spark Streaming example [NetworkWordCount](https://github.com/apache/spark/blob/v3.3.2/examples/src/main/scala/org/apache/spark/examples/streaming/NetworkWordCount.scala).

完整代码可用在 Spark Streaming 示例 NetworkWordCount 中找到。

> If you have already [downloaded](https://spark.apache.org/docs/3.3.2/index.html#downloading) and [built](https://spark.apache.org/docs/3.3.2/index.html#building) Spark, you can run this example as follows. You will first need to run Netcat (a small utility found in most Unix-like systems) as a data server by using

如果你已经下载并构建了 spark, 你可以像下面这样运行这个例子。你将首先需要运行 Netcat, 作为使用的数据服务。

```
$ nc -lk 9999
```

> Then, in a different terminal, you can start the example by using

然后，在不同终端，你可以使用如下命令启动例子。

```
$ ./bin/run-example streaming.NetworkWordCount localhost 9999
```

> Then, any lines typed in the terminal running the netcat server will be counted and printed on screen every second. It will look something like the following.

然后，在运行 netcat 服务的终端键入的任意行，都会被计数，并在屏幕中每秒打印。

![streaming-scala.png](./images/streaming-scala.png)

### Java

> First, we create a [JavaStreamingContext](https://spark.apache.org/docs/3.3.2/api/java/index.html?org/apache/spark/streaming/api/java/JavaStreamingContext.html) object, which is the main entry point for all streaming functionality. We create a local StreamingContext with two execution threads, and a batch interval of 1 second.

```java
import org.apache.spark.*;
import org.apache.spark.api.java.function.*;
import org.apache.spark.streaming.*;
import org.apache.spark.streaming.api.java.*;
import scala.Tuple2;

// Create a local StreamingContext with two working thread and batch interval of 1 second
SparkConf conf = new SparkConf().setMaster("local[2]").setAppName("NetworkWordCount");
JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(1));
```

> Using this context, we can create a DStream that represents streaming data from a TCP source, specified as hostname `(e.g. localhost)` and port `(e.g. 9999)`.

```java
// Create a DStream that will connect to hostname:port, like localhost:9999
JavaReceiverInputDStream<String> lines = jssc.socketTextStream("localhost", 9999);
```

> This `lines` DStream represents the stream of data that will be received from the data server. Each record in this stream is a line of text. Then, we want to split the lines by space into words.

```java
// Split each line into words
JavaDStream<String> words = lines.flatMap(x -> Arrays.asList(x.split(" ")).iterator());
```

> `flatMap` is a DStream operation that creates a new DStream by generating multiple new records from each record in the source DStream. In this case, each line will be split into multiple words and the stream of words is represented as the `words` DStream. Note that we defined the transformation using a [FlatMapFunction](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/api/java/function/FlatMapFunction.html) object. As we will discover along the way, there are a number of such convenience classes in the Java API that help defines DStream transformations.

> Next, we want to count these words.

```java
// Count each word in each batch
JavaPairDStream<String, Integer> pairs = words.mapToPair(s -> new Tuple2<>(s, 1));
JavaPairDStream<String, Integer> wordCounts = pairs.reduceByKey((i1, i2) -> i1 + i2);

// Print the first ten elements of each RDD generated in this DStream to the console
wordCounts.print();
```

> The `words` DStream is further mapped (one-to-one transformation) to a DStream of `(word, 1)` pairs, using a [PairFunction](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/api/java/function/PairFunction.html) object. Then, it is reduced to get the frequency of words in each batch of data, using a [Function2](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/api/java/function/Function2.html) object. Finally, `wordCounts.print()` will print a few of the counts generated every second.

> Note that when these lines are executed, Spark Streaming only sets up the computation it will perform after it is started, and no real processing has started yet. To start the processing after all the transformations have been setup, we finally call start method.

```java
jssc.start();              // Start the computation
jssc.awaitTermination();   // Wait for the computation to terminate
```

> The complete code can be found in the Spark Streaming example [JavaNetworkWordCount](https://github.com/apache/spark/blob/v3.3.2/examples/src/main/java/org/apache/spark/examples/streaming/JavaNetworkWordCount.java).

> If you have already [downloaded](https://spark.apache.org/docs/3.3.2/index.html#downloading) and [built](https://spark.apache.org/docs/3.3.2/index.html#building) Spark, you can run this example as follows. You will first need to run Netcat (a small utility found in most Unix-like systems) as a data server by using

```
$ nc -lk 9999
```

> Then, in a different terminal, you can start the example by using

```
$ ./bin/run-example streaming.JavaNetworkWordCount localhost 9999
```

> Then, any lines typed in the terminal running the netcat server will be counted and printed on screen every second. It will look something like the following.

![streaming-java.png](./images/streaming-java.png)

### Python

> First, we import [StreamingContext](https://spark.apache.org/docs/3.3.2/api/python/reference/api/pyspark.streaming.StreamingContext.html#pyspark.streaming.StreamingContext), which is the main entry point for all streaming functionality. We create a local StreamingContext with two execution threads, and batch interval of 1 second.

```python
from pyspark import SparkContext
from pyspark.streaming import StreamingContext

# Create a local StreamingContext with two working thread and batch interval of 1 second
sc = SparkContext("local[2]", "NetworkWordCount")
ssc = StreamingContext(sc, 1)
```

> Using this context, we can create a DStream that represents streaming data from a TCP source, specified as hostname `(e.g. localhost)` and port `(e.g. 9999)`.

```python
# Create a DStream that will connect to hostname:port, like localhost:9999
lines = ssc.socketTextStream("localhost", 9999)
```

> This `lines` DStream represents the stream of data that will be received from the data server. Each record in this DStream is a line of text. Next, we want to split the lines by space into words.

```python
# Split each line into words
words = lines.flatMap(lambda line: line.split(" "))
```

> `flatMap` is a one-to-many DStream operation that creates a new DStream by generating multiple new records from each record in the source DStream. In this case, each line will be split into multiple words and the stream of words is represented as the words DStream. Next, we want to count these words.

```python
# Count each word in each batch
pairs = words.map(lambda word: (word, 1))
wordCounts = pairs.reduceByKey(lambda x, y: x + y)

# Print the first ten elements of each RDD generated in this DStream to the console
wordCounts.pprint()
```

> The `words` DStream is further mapped (one-to-one transformation) to a DStream of `(word, 1)` pairs, which is then reduced to get the frequency of words in each batch of data. Finally, `wordCounts.pprint()` will print a few of the counts generated every second.

> Note that when these lines are executed, Spark Streaming only sets up the computation it will perform when it is started, and no real processing has started yet. To start the processing after all the transformations have been setup, we finally call

```python
ssc.start()             # Start the computation
ssc.awaitTermination()  # Wait for the computation to terminate
```

> The complete code can be found in the Spark Streaming example [NetworkWordCount](https://github.com/apache/spark/blob/v3.3.2/examples/src/main/python/streaming/network_wordcount.py).

> If you have already [downloaded](https://spark.apache.org/docs/3.3.2/index.html#downloading) and [built](https://spark.apache.org/docs/3.3.2/index.html#building) Spark, you can run this example as follows. You will first need to run Netcat (a small utility found in most Unix-like systems) as a data server by using

```
$ nc -lk 9999
```

> Then, in a different terminal, you can start the example by using

```
$ ./bin/spark-submit examples/src/main/python/streaming/network_wordcount.py localhost 9999
```

> Then, any lines typed in the terminal running the netcat server will be counted and printed on screen every second. It will look something like the following.

![streaming-python.png](./images/streaming-python.png)

## Basic Concepts

> Next, we move beyond the simple example and elaborate on the basics of Spark Streaming.

接下来，详细说明 Spark Streaming 的基础内容。

### Linking

> Similar to Spark, Spark Streaming is available through Maven Central. To write your own Spark Streaming program, you will have to add the following dependency to your SBT or Maven project.

和 spark 类似，可以在 Maven Central 中获取 Spark Streaming. 要写你自己的 Spark Streaming 程序，你必须添加依赖。

#### Maven

```xml
<dependency>
    <groupId>org.apache.spark</groupId>
    <artifactId>spark-streaming_2.12</artifactId>
    <version>3.3.2</version>
    <scope>provided</scope>
</dependency>
```

> For ingesting data from sources like Kafka and Kinesis that are not present in the Spark Streaming core API, you will have to add the corresponding artifact `spark-streaming-xyz_2.12` to the dependencies. For example, some of the common ones are as follows.

对于从 Kafka 和 Kinesis 获取数据，还需要添加 Kafka 和 Kinesis 的依赖。

Source | Artifact
---|:---
Kafka | spark-streaming-kafka-0-10_2.12
Kinesis | spark-streaming-kinesis-asl_2.12 [Amazon Software License]

> For an up-to-date list, please refer to the [Maven repository](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.apache.spark%22%20AND%20v%3A%223.3.2%22) for the full list of supported sources and artifacts.

#### SBT

```
libraryDependencies += "org.apache.spark" % "spark-streaming_2.12" % "3.3.2" % "provided"
```

> For ingesting data from sources like Kafka and Kinesis that are not present in the Spark Streaming core API, you will have to add the corresponding artifact `spark-streaming-xyz_2.12` to the dependencies. For example, some of the common ones are as follows.

Source	| Artifact
---|:---
Kafka  | spark-streaming-kafka-0-10_2.12
Kinesis | spark-streaming-kinesis-asl_2.12 [Amazon Software License]

> For an up-to-date list, please refer to the [Maven repository](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.apache.spark%22%20AND%20v%3A%223.3.2%22) for the full list of supported sources and artifacts.

### Initializing StreamingContext

> To initialize a Spark Streaming program, a StreamingContext object has to be created which is the main entry point of all Spark Streaming functionality.

初始化一个 Spark Streaming 程序，首先要创建一个 StreamingContext 对象，作为整个功能的主入口。

#### Scala

> A [StreamingContext](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/streaming/StreamingContext.html) object can be created from a [SparkConf](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/SparkConf.html) object.

可以根据 SparkConf 对象创建 StreamingContext 对象。

```scala
import org.apache.spark._
import org.apache.spark.streaming._

val conf = new SparkConf().setAppName(appName).setMaster(master)
val ssc = new StreamingContext(conf, Seconds(1))
```

> The `appName` parameter is a name for your application to show on the cluster UI. `master` is a [Spark, Mesos, Kubernetes or YARN cluster URL](https://spark.apache.org/docs/3.3.2/submitting-applications.html#master-urls), or a special “`local[*]`” string to run in local mode. In practice, when running on a cluster, you will not want to hardcode `master` in the program, but rather [launch the application with spark-submit](https://spark.apache.org/docs/3.3.2/submitting-applications.html) and receive it there. However, for local testing and unit tests, you can pass “`local[*]`” to run Spark Streaming in-process (detects the number of cores in the local system). Note that this internally creates a [SparkContext](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/SparkContext.html) (starting point of all Spark functionality) which can be accessed as `ssc.sparkContext`.

`appName` 参数是应用程序在集群 UI 上的名字。

`master` 可以是 Spark, Mesos 或 YARN cluster URL，或是运行在本地模式时的 `local[*]` 字符串
。实际上，在集群上运行时，你并不想在程序中硬编码 `master`, 此时可以使用 spark-submit 脚本启动应用程序，接受此参数。然而，对于本地测试和单元测试，可以传递 `local[*]`, 以在进程中运行 Spark Streaming. 注意，这会在内部创建 SparkContext, 可以通过 `ssc.sparkContext` 访问。

> The batch interval must be set based on the latency requirements of your application and available cluster resources. See the [Performance Tuning](https://spark.apache.org/docs/3.3.2/streaming-programming-guide.html#setting-the-right-batch-interval) section for more details.

批次间隔的设置依据 程序的延迟要求 和 集群可用资源 设置。

> A `StreamingContext` object can also be created from an existing `SparkContext` object.

也可以从一个已存在的 `SparkContext` 对象上创建 `StreamingContext` 对象。

```scala
import org.apache.spark.streaming._

val sc = ...                // existing SparkContext
val ssc = new StreamingContext(sc, Seconds(1))
```

> After a context is defined, you have to do the following.

创建上下文后，还需要做如下几件事：

- 1. 通过创建输入 DStreams 来定义输入源

- 2. 使用 转换 和 输出操作 定义流计算模型

- 3. 使用 `streamingContext.start()` 启动接收数据，并处理

- 4. 使用 `streamingContext.awaitTermination()` 等待处理完成(手动，或遇到错误)

- 5. 使用 `streamingContext.stop()` 手动停止

> Define the input sources by creating input DStreams.
> Define the streaming computations by applying transformation and output operations to DStreams.
> Start receiving data and processing it using `streamingContext.start()`.
> Wait for the processing to be stopped (manually or due to any error) using `streamingContext.awaitTermination()`.
> The processing can be manually stopped using `streamingContext.stop()`.

> Points to remember:

以下几点要注意：

- 1. 启动上下文后，不能再设置、添加新的流计算模型。

- 2. 停止上下文后，不能再重启。

- 3. 在 JVM 中，只能有一个 StreamingContext 活跃。

- 4. `streamingContext.stop()` 也会导致 SparkContext 停止。要避免这个情况，需要填参数 `stopSparkContext = false`

- 5. 一个 SparkContext 可用用来创建多个 StreamingContext. 前提是，在下一个 StreamingContext 创建前，停止了前有个  StreamingContext （不是通过停止 SparkContext 实现的）。

> Once a context has been started, no new streaming computations can be set up or added to it.
> Once a context has been stopped, it cannot be restarted.
> Only one StreamingContext can be active in a JVM at the same time.
> `stop()` on StreamingContext also stops the SparkContext. To stop only the StreamingContext, set the optional parameter of `stop()` called `stopSparkContext` to false.
> A SparkContext can be re-used to create multiple StreamingContexts, as long as the previous StreamingContext is stopped (without stopping the SparkContext) before the next StreamingContext is created.

#### Java

> A [JavaStreamingContext](https://spark.apache.org/docs/3.3.2/api/java/index.html?org/apache/spark/streaming/api/java/JavaStreamingContext.html) object can be created from a [SparkConf](https://spark.apache.org/docs/3.3.2/api/java/index.html?org/apache/spark/SparkConf.html) object.

```java
import org.apache.spark.*;
import org.apache.spark.streaming.api.java.*;

SparkConf conf = new SparkConf().setAppName(appName).setMaster(master);
JavaStreamingContext ssc = new JavaStreamingContext(conf, new Duration(1000));
```

> The `appName` parameter is a name for your application to show on the cluster UI. `master` is a [Spark, Mesos or YARN cluster URL](https://spark.apache.org/docs/3.3.2/submitting-applications.html#master-urls), or a special “`local[*]`” string to run in local mode. In practice, when running on a cluster, you will not want to hardcode master in the program, but rather [launch the application with spark-submit](https://spark.apache.org/docs/3.3.2/submitting-applications.html) and receive it there. However, for local testing and unit tests, you can pass “`local[*]`” to run Spark Streaming in-process. Note that this internally creates a [JavaSparkContext](https://spark.apache.org/docs/3.3.2/api/java/index.html?org/apache/spark/api/java/JavaSparkContext.html) (starting point of all Spark functionality) which can be accessed as `ssc.sparkContext`.

> The batch interval must be set based on the latency requirements of your application and available cluster resources. See the [Performance Tuning](https://spark.apache.org/docs/3.3.2/streaming-programming-guide.html#setting-the-right-batch-interval) section for more details.

> A JavaStreamingContext object can also be created from an existing JavaSparkContext.

```java
import org.apache.spark.streaming.api.java.*;

JavaSparkContext sc = ...   //existing JavaSparkContext
JavaStreamingContext ssc = new JavaStreamingContext(sc, Durations.seconds(1));
```
> After a context is defined, you have to do the following.

> Define the input sources by creating input DStreams.
> Define the streaming computations by applying transformation and output operations to DStreams.
> Start receiving data and processing it using `streamingContext.start()`.
> Wait for the processing to be stopped (manually or due to any error) using `streamingContext.awaitTermination()`.
> The processing can be manually stopped using `streamingContext.stop()`.

> Points to remember:
> Once a context has been started, no new streaming computations can be set up or added to it.
> Once a context has been stopped, it cannot be restarted.
> Only one StreamingContext can be active in a JVM at the same time.
> `stop()` on StreamingContext also stops the SparkContext. To stop only the StreamingContext, set the optional parameter of `stop()` called `stopSparkContext` to false.
> A SparkContext can be re-used to create multiple StreamingContexts, as long as the previous StreamingContext is stopped (without stopping the SparkContext) before the next StreamingContext is created.

#### Python

> A [StreamingContext](https://spark.apache.org/docs/3.3.2/api/python/reference/api/pyspark.streaming.StreamingContext.html#pyspark.streaming.StreamingContext) object can be created from a [SparkContext](https://spark.apache.org/docs/3.3.2/api/python/reference/api/pyspark.SparkContext.html#pyspark.SparkContext) object.

```python
from pyspark import SparkContext
from pyspark.streaming import StreamingContext

sc = SparkContext(master, appName)
ssc = StreamingContext(sc, 1)
```

> The `appName` parameter is a name for your application to show on the cluster UI. `master` is a [Spark, Mesos or YARN cluster URL](https://spark.apache.org/docs/3.3.2/submitting-applications.html#master-urls), or a special “`local[*]`” string to run in local mode. In practice, when running on a cluster, you will not want to hardcode master in the program, but rather [launch the application with spark-submit](https://spark.apache.org/docs/3.3.2/submitting-applications.html) and receive it there. However, for local testing and unit tests, you can pass “`local[*]`” to run Spark Streaming in-process (detects the number of cores in the local system).

> The batch interval must be set based on the latency requirements of your application and available cluster resources. See the [Performance Tuning](https://spark.apache.org/docs/3.3.2/streaming-programming-guide.html#setting-the-right-batch-interval) section for more details.

> After a context is defined, you have to do the following.

> Define the input sources by creating input DStreams.
> Define the streaming computations by applying transformation and output operations to DStreams.
> Start receiving data and processing it using `streamingContext.start()`.
> Wait for the processing to be stopped (manually or due to any error) using `streamingContext.awaitTermination()`.
> The processing can be manually stopped using `streamingContext.stop()`.

> Points to remember:
> Once a context has been started, no new streaming computations can be set up or added to it.
> Once a context has been stopped, it cannot be restarted.
> Only one StreamingContext can be active in a JVM at the same time.
> `stop()` on StreamingContext also stops the SparkContext. To stop only the StreamingContext, set the optional parameter of `stop()` called `stopSparkContext` to false.
> A SparkContext can be re-used to create multiple StreamingContexts, as long as the previous StreamingContext is stopped (without stopping the SparkContext) before the next StreamingContext is created.
> 
### Discretized Streams (DStreams)

> Discretized Stream or DStream is the basic abstraction provided by Spark Streaming. It represents a continuous stream of data, either the input data stream received from source, or the processed data stream generated by transforming the input stream. Internally, a DStream is represented by a continuous series of RDDs, which is Spark’s abstraction of an immutable, distributed dataset (see [Spark Programming Guide](https://spark.apache.org/docs/3.3.2/rdd-programming-guide.html#resilient-distributed-datasets-rdds) for more details). Each RDD in a DStream contains data from a certain interval, as shown in the following figure.

DStreams 是 Spark Streaming 提供的基本的抽象。它表示一个持续的数据流，要么是从源接受到的输入数据流，要么是通过转换输入流生成的已被处理的数据流。

在内部，它是一个持续的 RDDs 序列。 DStreams 中的每个 RDD 是特定时间间隔的一批数据。

![streaming-dstream.png](./images/streaming-dstream.png)

> Any operation applied on a DStream translates to operations on the underlying RDDs. For example, in the [earlier example](https://spark.apache.org/docs/3.3.2/streaming-programming-guide.html#a-quick-example) of converting a stream of lines to words, the flatMap operation is applied on each RDD in the lines DStream to generate the RDDs of the words DStream. This is shown in the following figure.

对 DStream 的任何操作都会转换成对底层 RDDs 的操作。

![streaming-dstream-ops.png](./images/streaming-dstream-ops.png)

> These underlying RDD transformations are computed by the Spark engine. The DStream operations hide most of these details and provide the developer with a higher-level API for convenience. These operations are discussed in detail in later sections.

底层 RDD 的转换操作是由 Spark engine 计算的。 DStream 操作隐藏了大部分细节，并提供给开发者更高级的 API.

### Input DStreams and Receivers

> Input DStreams are DStreams representing the stream of input data received from streaming sources. In the [quick example](https://spark.apache.org/docs/3.3.2/streaming-programming-guide.html#a-quick-example), `lines` was an input DStream as it represented the stream of data received from the netcat server. Every input DStream (except file stream, discussed later in this section) is associated with a Receiver ([Scala doc](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/streaming/receiver/Receiver.html), [Java doc](https://spark.apache.org/docs/3.3.2/api/java/org/apache/spark/streaming/receiver/Receiver.html)) object which receives the data from a source and stores it in Spark’s memory for processing.

输入 DStreams 表示了从流源接收到的输入数据流。

每个输入 DStream （除了文件流） 都和 Receiver 对象相关连，它从数据源中接收数据，存储在 Spark 内存中等待处理。

> Spark Streaming provides two categories of built-in streaming sources.

Spark Streaming 有两种内置的流的源：

- 1. 基本源：在 StreamingContext API 中直接可用，如：文件系统和 socket 连接。

- 2. 高级源：像 Kafka, Kinesis 等需要添加额外的依赖。

> Basic sources: Sources directly available in the StreamingContext API. Examples: file systems, and socket connections.
> Advanced sources: Sources like Kafka, Kinesis, etc. are available through extra utility classes. These require linking against extra dependencies as discussed in the [linking](https://spark.apache.org/docs/3.3.2/streaming-programming-guide.html#linking) section.

> We are going to discuss some of the sources present in each category later in this section.

我们将讨论在后面每个目录下呈现的数据源。

> Note that, if you want to receive multiple streams of data in parallel in your streaming application, you can create multiple input DStreams (discussed further in the [Performance Tuning](https://spark.apache.org/docs/3.3.2/streaming-programming-guide.html#level-of-parallelism-in-data-receiving) section). This will create multiple receivers which will simultaneously receive multiple data streams. But note that a Spark worker/executor is a long-running task, hence it occupies one of the cores allocated to the Spark Streaming application. Therefore, it is important to remember that a Spark Streaming application needs to be allocated enough cores (or threads, if running locally) to process the received data, as well as to run the receiver(s).

如果想要并行接收多个数据流，可以创建多个输入 DStreams, 然后会创建多个 receivers, 它们会同时接收多个数据流。

注意，Spark worker/executor 是一个长时间运行的任务，因为它会占用分配给 Spark Streaming 程序的核。因此，需要确保分配给 Spark Streaming 程序足够的核（在本地模式下是线程），去处理接收的数据、运行 receiver.

> When running a Spark Streaming program locally, do not use “local” or “local[1]” as the master URL. Either of these means that only one thread will be used for running tasks locally. If you are using an input DStream based on a receiver (e.g. sockets, Kafka, etc.), then the single thread will be used to run the receiver, leaving no thread for processing the received data. Hence, when running locally, always use “local[n]” as the master URL, where n > number of receivers to run (see [Spark Properties](https://spark.apache.org/docs/3.3.2/configuration.html#spark-properties) for information on how to set the master).

当本地运行 Spark Streaming 时，不要将 master URL 设置为 `local` 或 `local[1]`. 这是因为它只会创建一个线程用于运行本地任务。如果你正基于 receiver 使用输入 DStream （例如sockets, Kafka等），那么就会使用这个线程运行 receiver, 就没有线程处理接收的数据了。所以最好设置为 `local[n]`, n的数值大于 receivers 的数量。

> Extending the logic to running on a cluster, the number of cores allocated to the Spark Streaming application must be more than the number of receivers. Otherwise the system will receive data, but not be able to process it.

在集群模式下，分配给 Spark Streaming 程序的核的数量要超过 receivers 的数量。否则系统将只接收数据，而不处理它。

#### Basic Sources

> We have already taken a look at the `ssc.socketTextStream(...)` in the [quick example](https://spark.apache.org/docs/3.3.2/streaming-programming-guide.html#a-quick-example) which creates a DStream from text data received over a TCP socket connection. Besides sockets, the StreamingContext API provides methods for creating DStreams from files as input sources.

我们已经在 quick example 里看了 `ssc.socketTextStream(...)`, 除了 sockets 外，StreamingContext API 提供了从文件中创建 DStreams 的方法。

##### File Streams

> For reading data from files on any file system compatible with the HDFS API (that is, HDFS, S3, NFS, etc.), a DStream can be created as via `StreamingContext.fileStream[KeyClass, ValueClass, InputFormatClass]`.

对于从和 HDFS API 兼容的文件系统（如:HDFS,S3,NFS等）中读取文件数据，可以通过 `StreamingContext.fileStream[KeyClass, ValueClass, InputFormatClass]` 创建 DStream.

> File streams do not require running a receiver so there is no need to allocate any cores for receiving file data.

文件流不需要运行 receiver, 所以就不需要为接收文件数据分配核。

> For simple text files, the easiest method is `StreamingContext.textFileStream(dataDirectory)`.

对于简单文本文件，其方法是 `StreamingContext.textFileStream(dataDirectory)`

```scala
streamingContext.fileStream[KeyClass, ValueClass, InputFormatClass](dataDirectory)

# For text files
streamingContext.textFileStream(dataDirectory)
```

```java
streamingContext.fileStream<KeyClass, ValueClass, InputFormatClass>(dataDirectory);

# For text files
streamingContext.textFileStream(dataDirectory);
```

```python
# fileStream is not available in the Python API; only textFileStream is available.
# fileStream 方法在 Python API 中是不可用的，仅可以使用 textFileStream。

streamingContext.textFileStream(dataDirectory)
```

##### How Directories are Monitored

> Spark Streaming will monitor the directory dataDirectory and process any files created in that directory.

Spark Streaming 会监控数据所在目录，并处理其中的文件：

> A simple directory can be monitored, such as `"hdfs://namenode:8040/logs/"`. All files directly under such a path will be processed as they are discovered.

- 像 `hdfs://namenode:8040/logs/` 监控目录。该目录下的文件都会被处理。

> A [POSIX glob pattern](http://pubs.opengroup.org/onlinepubs/009695399/utilities/xcu_chap02.html#tag_02_13_02) can be supplied, such as `"hdfs://namenode:8040/logs/2017/*"`. Here, the DStream will consist of all files in the directories matching the pattern. That is: it is a pattern of directories, not of files in directories.

- 可以使用 POSIX glob pattern 监控多个目录，比如 `"hdfs://namenode:8040/logs/2017/*"`. 因此，DStream 将由匹配到这个模式的目录下的所有文件组成。这是针对目录的匹配模式，而不是目录下的文件。

> All files must be in the same data format.

- 所有文件必须具有相同的数据格式。

> A file is considered part of a time period based on its modification time, not its creation time.

- 根据文件的修改时间，而不是创建时间，将文件视为时间段的一部分。

> Once processed, changes to a file within the current window will not cause the file to be reread. That is: updates are ignored.

- 文件一旦被处理，在当前窗口下对文件的修改，不会再次读取此文件，仍旧读修改前的文件。

> The more files under a directory, the longer it will take to scan for changes — even if no files have been modified.

- 目录下的文件越多，就会为检查文件是否有过修改而扫描整个目录的时间越久，即使文件没有被修改。

> If a wildcard is used to identify directories, such as `"hdfs://namenode:8040/logs/2016-*"`, renaming an entire directory to match the path will add the directory to the list of monitored directories. Only the files in the directory whose modification time is within the current window will be included in the stream.

- 如果使用通配符来标识目录，比如 `hdfs://namenode:8040/logs/2016-*`, 重命名整个目录以匹配路径会将该目录添加到监视目录列表中。只有目录中修改时间在当前窗口内的文件才会包含在流中。

> Calling [FileSystem.setTimes()](https://hadoop.apache.org/docs/current/api/org/apache/hadoop/fs/FileSystem.html#setTimes-org.apache.hadoop.fs.Path-long-long-) to fix the timestamp is a way to have the file picked up in a later window, even if its contents have not changed.

- 调用 `filesysystem.settimes()` 设置时间戳，可用于在以后的窗口中再次使用文件，即使文件的内容没有改变。

##### Using Object Stores as a source of data

> “Full” Filesystems such as HDFS tend to set the modification time on their files as soon as the output stream is created. When a file is opened, even before data has been completely written, it may be included in the `DStream` - after which updates to the file within the same window will be ignored. That is: changes may be missed, and data omitted from the stream.

对于 HDFS 这样的 “Full” 文件系统，一旦创建了输出流，那么就会对文件设置修改时间。

当打开一个文件时，即使是数据全部写入之前，它也可能包含在 `DStream` 中。在同一窗口中对此文件的更新将被忽略。

也就是可能错过修改，忽略流中的数据。 

> To guarantee that changes are picked up in a window, write the file to an unmonitored directory, then, immediately after the output stream is closed, rename it into the destination directory. Provided the renamed file appears in the scanned destination directory during the window of its creation, the new data will be picked up.

要保证能获取在窗口中修改后的数据，将该文件写入一个不受监控的目录，然后在关闭输出流之后，立即将文件重命名到目标目录。

如果重命名的文件在其创建窗口期间出现在扫描的目标目录中，则将拾取新数据。

> In contrast, Object Stores such as Amazon S3 and Azure Storage usually have slow rename operations, as the data is actually copied. Furthermore, renamed object may have the time of the `rename()` operation as its modification time, so may not be considered part of the window which the original create time implied they were.

相反，像 Amazon S3 和 Azure 这样的对象存储通常有很慢的重命名操作，因为数据真的在复制。

此外，重命名的对象可能将 `rename()` 操作的时间作为其修改时间，因此可能不会将其被认为是窗口的一部分，而是将原始创建时间认定是窗口的一部分。

> Careful testing is needed against the target object store to verify that the timestamp behavior of the store is consistent with that expected by Spark Streaming. It may be that writing directly into a destination directory is the appropriate strategy for streaming data via the chosen object store.

需要对目标对象存储进行仔细测试，以验证存储的时间戳行为是否与 Spark Streaming 的预期一致。

对于流数据来说，通过选择的对象存储，直接写入目标目录可能是合适的策略。

> For more details on this topic, consult the [Hadoop Filesystem Specification](https://hadoop.apache.org/docs/stable2/hadoop-project-dist/hadoop-common/filesystem/introduction.html).

##### Streams based on Custom Receivers

> DStreams can be created with data streams received through custom receivers. See the [Custom Receiver Guide](https://spark.apache.org/docs/3.3.2/streaming-custom-receivers.html) for more details.

也可以通过自定义的 receivers 接收的数据，来创建 DStreams.

#### Queue of RDDs as a Stream

> For testing a Spark Streaming application with test data, one can also create a DStream based on a queue of RDDs, using `streamingContext.queueStream(queueOfRDDs)`. Each RDD pushed into the queue will be treated as a batch of data in the DStream, and processed like a stream.

要使用测试数据测试 Spark Streaming 应用程序，可以基于一个 RDDs 队列，使用 `streamingContext.queueStream(queueOfRDDs)` 创建 DStream.

每个入队的 RDD 将作为 DStream 里的一批数据，像流一样被处理。

> For more details on streams from sockets and files, see the API documentations of the relevant functions in [StreamingContext](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/streaming/StreamingContext.html) for Scala, [JavaStreamingContext](https://spark.apache.org/docs/3.3.2/api/java/index.html?org/apache/spark/streaming/api/java/JavaStreamingContext.html) for Java, and [StreamingContext](https://spark.apache.org/docs/3.3.2/api/python/reference/api/pyspark.streaming.StreamingContext.html#pyspark.streaming.StreamingContext) for Python.

#### Advanced Sources

> **Python API** As of Spark 3.3.2, out of these sources, Kafka and Kinesis are available in the Python API.

Spark 3.3.2 中， 可以使用 Python API 创建 Kafka 和 Kinesis 源。

> This category of sources requires interfacing with external non-Spark libraries, some of them with complex dependencies (e.g., Kafka). Hence, to minimize issues related to version conflicts of dependencies, the functionality to create DStreams from these sources has been moved to separate libraries that can be [linked](https://spark.apache.org/docs/3.3.2/streaming-programming-guide.html#linking) to explicitly when necessary.

这个类别的源要求和外部的非 spark 库交互，其中一些具有复杂的依赖（例如Kafka）。因此，为了最小化依赖的版本冲突问题，基于这些源创建 DStreams 的功能被移除到独立的库中，这些库在必要的情况下可以被明确地链接。

> Note that these advanced sources are not available in the Spark shell, hence applications based on these advanced sources cannot be tested in the shell. If you really want to use them in the Spark shell you will have to download the corresponding Maven artifact’s JAR along with its dependencies and add it to the classpath.

注意：这些高级源在 Spark shell 中不可用，因此基于这些高级源的应用程序不能在 shell 中测试。如果要在 Spark shell 中使用，需要下载对应的 Maven artifact 的 JAR, 放到类路径中。

> Some of these advanced sources are as follows.

这些高级源有如下这些：

- Kafka: Spark Streaming 3.3.2 要求 Kafka broker 0.10 或更高

- Kinesis: Spark Streaming 3.3.2 要求 Kinesis 客户端库 1.2.1

> Kafka: Spark Streaming 3.3.2 is compatible with Kafka broker versions 0.10 or higher. See the [Kafka Integration Guide](https://spark.apache.org/docs/3.3.2/streaming-kafka-0-10-integration.html) for more details.

> Kinesis: Spark Streaming 3.3.2 is compatible with Kinesis Client Library 1.2.1. See the [Kinesis Integration Guide](https://spark.apache.org/docs/3.3.2/streaming-kinesis-integration.html) for more details.

#### Custom Sources

> **Python API** This is not yet supported in Python.

python 尚不支持此特性。

> Input DStreams can also be created out of custom data sources. All you have to do is implement a user-defined receiver (see next section to understand what that is) that can receive data from the custom sources and push it into Spark. See the [Custom Receiver Guide](https://spark.apache.org/docs/3.3.2/streaming-custom-receivers.html) for details.

可以从自定义的数据源中创建输入 DStreams. 只需要实现一个用户定义的 receiver, 它能从自定义源中接收数据，并推向 spark.

#### Receiver Reliability

> There can be two kinds of data sources based on their reliability. Sources (like Kafka) allow the transferred data to be acknowledged. If the system receiving data from these reliable sources acknowledges the received data correctly, it can be ensured that no data will be lost due to any kind of failure. This leads to two kinds of receivers:

有两种基于可靠性的数据源。数据源（如 Kafka）允许确认传输的数据。如果系统正确地确认了接收的数据，就表明没有数据丢失。

这样就出现了两种接收器：

> Reliable Receiver - A reliable receiver correctly sends acknowledgment to a reliable source when the data has been received and stored in Spark with replication.

- 可靠接收器：当数据被接收，存储在 Spark 中，并进行了备份时，一个可靠的接收器向可靠的数据源正确地发送确认。

> Unreliable Receiver - An unreliable receiver does not send acknowledgment to a source. This can be used for sources that do not support acknowledgment, or even for reliable sources when one does not want or need to go into the complexity of acknowledgment.

- 不可靠的接收器：不会向源发送确认。这可以用于不支持确认的源，或者用于并不想或不需要确认的可靠源。

> The details of how to write a reliable receiver are discussed in the [Custom Receiver Guide](https://spark.apache.org/docs/3.3.2/streaming-custom-receivers.html).

如何写一个可靠的接收器，看这里 Custom Receiver Guide.

### Transformations on DStreams

> Similar to that of RDDs, transformations allow the data from the input DStream to be modified. DStreams support many of the transformations available on normal Spark RDD’s. Some of the common ones are as follows.

和对 RDDs 的操作类似，转换操作允许修改来自输入 DStreams 的数据。 DStreams 支持标准的 Spark RDD 上的许多转换操作。一些常见的如下：

- map(func)	

	> Return a new DStream by passing each element of the source DStream through a function `func`.

    将源 DStream 的每个元素传给 `func` 处理，返回一个新的 DStream

- flatMap(func)	

	> Similar to map, but each input item can be mapped to 0 or more output items.

    和 map 类似，但是每个输入项可以被映射成 0 个或更多输出项。

- filter(func)	

	> Return a new DStream by selecting only the records of the source DStream on which `func` returns true.

    仅选择函数 `func` 返回为 true 的源 DStream 记录，以返回一个新的 DStream

- repartition(numPartitions)	

	> Changes the level of parallelism in this DStream by creating more or fewer partitions.

    通过创建更多或更少的分区，来改变这个 DStream 的并行度。

- union(otherStream)	

	> Return a new DStream that contains the union of the elements in the source DStream and otherDStream.

    返回一个新的 DStream, 它包含了源 DStream 和 otherDStream 元素的并集。

- count()	

	> Return a new DStream of single-element RDDs by counting the number of elements in each RDD of the source DStream.

    通过统计源 DStream 的每个 RDD 中的元素个数，返回一个只有一个元素的 RDDs 组成的 DStream

- reduce(func)	

	> Return a new DStream of single-element RDDs by aggregating the elements in each RDD of the source DStream using a function `func` (which takes two arguments and returns one). The function should be associative and commutative so that it can be computed in parallel.

    通过使用函数 `func` （接收两个参数并返回一个）聚合源 DStream 的每个 RDD 中的元素个数，返回一个只有一个元素的 RDDs 组成的 DStream.

- countByValue()	

	> When called on a DStream of elements of type K, return a new DStream of (K, Long) pairs where the value of each key is its frequency in each RDD of the source DStream.

    当在类型为 K 的元素组成的 DStream 上调用此算子时，返回一个 (K, Long) 对的 DStream, 每个键的值是它在源 DStream 的每个 RDD 中出现的频率。

- reduceByKey(func, [numTasks])	

	> When called on a DStream of (K, V) pairs, return a new DStream of (K, V) pairs where the values for each key are aggregated using the given reduce function. Note: By default, this uses Spark's default number of parallel tasks (2 for local mode, and in cluster mode the number is determined by the config property `spark.default.parallelism`) to do the grouping. You can pass an optional numTasks argument to set a different number of tasks.

    当在 (K, V) 的 DStream 上调用此算子时，返回一个 (K, V) 对的 DStream, 每个键的值使用给定的 reduce 函数聚合得到。

    注意：默认情况下，它使用 spark 默认的并行任务数量（本地模式是2，在集群模式下由`spark.default.parallelism`决定）做分组。

    你可以传递一个可选地 numTasks 参数，来设置一个不同的任务数量。

- join(otherStream, [numTasks])	

	> When called on two DStreams of (K, V) and (K, W) pairs, return a new DStream of (K, (V, W)) pairs with all pairs of elements for each key.

    当在两个 (K, V) 和 (K, W) 对的 DStreams 上调用此算子时，返回 (K, (V, W)) 的 DStream

- cogroup(otherStream, [numTasks])	

	> When called on a DStream of (K, V) and (K, W) pairs, return a new DStream of (K, Seq[V], Seq[W]) tuples.

    当在两个 (K, V) 和 (K, W) 对的 DStreams 上调用此算子时，返回 (K, Seq[V], Seq[W]) 元组的 DStream

- transform(func)	

	> Return a new DStream by applying a RDD-to-RDD function to every RDD of the source DStream. This can be used to do arbitrary RDD operations on the DStream.

    通过在源 DStream 的每个 RDD 上应用 RDD-to-RDD 函数，返回一个新的 DStream. 这可以被用来做任意的 RDD 操作。 

- updateStateByKey(func)	

	Return a new "state" DStream where the state for each key is updated by applying the given function on the previous state of the key and the new values for the key. This can be used to maintain arbitrary state data for each key.

    返回一个新状态的 DStream, 通过在每个键的前一个状态和键的新值上应用给定的函数来更新其状态。

    这可以用于维护每个键的任意状态的数据。

> A few of these transformations are worth discussing in more detail.

#### UpdateStateByKey Operation

> The `updateStateByKey` operation allows you to maintain arbitrary state while continuously updating it with new information. To use this, you will have to do two steps.

`updateStateByKey` 可以使用新信息持续维持任意状态。需要做如下两步：

- 1. 定义状态：状态可以是任意的数据类型。

- 2. 定义状态更新函数：基于先前状态和新值，指定一个状态更新函数来更新状态。

> Define the state - The state can be an arbitrary data type.
> Define the state update function - Specify with a function how to update the state using the previous state and the new values from an input stream.

> In every batch, Spark will apply the state update function for all existing keys, regardless of whether they have new data in a batch or not. If the update function returns None then the key-value pair will be eliminated.

Spark 会使用状态更新函数操作每个批次中的所有已存在 keys, 无论批次中是否有新数据。如果状态更新函数返回 None, 键值对会被消除。

> Let’s illustrate this with an example. Say you want to maintain a running count of each word seen in a text data stream. Here, the running count is the state and it is an integer. We define the update function as:

让我们使用一个示例阐述这点。你想在文本数据流中，维护每个单词的计数。这里，运行中的计数是状态，且是一个整型，我们这样定义状态更新函数：

##### Scala

```scala
def updateFunction(newValues: Seq[Int], runningCount: Option[Int]): Option[Int] = {
    val newCount = ...  // add the new values with the previous running count to get the new count
    Some(newCount)
}
```

> This is applied on a DStream containing words (say, the `pairs` DStream containing `(word, 1)` pairs in the [earlier example](https://spark.apache.org/docs/3.3.2/streaming-programming-guide.html#a-quick-example)).

这被应用在包含单词的 DStream 上（在前面例子中，`pairs` DStream 包含 `(word, 1)` 对）

```scala
val runningCounts = pairs.updateStateByKey[Int](updateFunction _)
```

> The update function will be called for each word, with `newValues` having a sequence of 1’s (from the `(word, 1)` pairs) and the `runningCount` having the previous count.

更新函数作用在每个 word 上，其中 `newValues` 是 1 的序列，`runningCount` 是先前的计数。

> Note that using `updateStateByKey` requires the checkpoint directory to be configured, which is discussed in detail in the [checkpointing](https://spark.apache.org/docs/3.3.2/streaming-programming-guide.html#checkpointing) section.

注意：使用 updateStateByKey 需要设置 checkpoint 目录。

##### Java

```java
Function2<List<Integer>, Optional<Integer>, Optional<Integer>> updateFunction =
  (values, state) -> {
    Integer newSum = ...  // add the new values with the previous running count to get the new count
    return Optional.of(newSum);
  };
```

> This is applied on a DStream containing words (say, the `pairs` DStream containing `(word, 1)`pairs in the [quick example](https://spark.apache.org/docs/3.3.2/streaming-programming-guide.html#a-quick-example)).

```java
JavaPairDStream<String, Integer> runningCounts = pairs.updateStateByKey(updateFunction);
```

> The update function will be called for each word, with `newValues` having a sequence of 1’s (from the `(word, 1)` pairs) and the `runningCount` having the previous count. For the complete Java code, take a look at the example [JavaStatefulNetworkWordCount.java](https://github.com/apache/spark/blob/v3.3.2/examples/src/main/java/org/apache/spark/examples/streaming/JavaStatefulNetworkWordCount.java).

> Note that using `updateStateByKey` requires the checkpoint directory to be configured, which is discussed in detail in the [checkpointing](https://spark.apache.org/docs/3.3.2/streaming-programming-guide.html#checkpointing) section.

##### Python

```python
def updateFunction(newValues, runningCount):
    if runningCount is None:
        runningCount = 0
    return sum(newValues, runningCount)  # add the new values with the previous running count to get the new count
```

> This is applied on a DStream containing words (say, the `pairs` DStream containing `(word, 1)`pairs in the [earlier example](https://spark.apache.org/docs/3.3.2/streaming-programming-guide.html#a-quick-example)).

```python
runningCounts = pairs.updateStateByKey(updateFunction)
```

> The update function will be called for each word, with `newValues` having a sequence of 1’s (from the `(word, 1)` pairs) and the `runningCount` having the previous count. For the complete Python code, take a look at the example [stateful_network_wordcount.py](https://github.com/apache/spark/blob/v3.3.2/examples/src/main/python/streaming/stateful_network_wordcount.py).

> Note that using `updateStateByKey` requires the checkpoint directory to be configured, which is discussed in detail in the [checkpointing](https://spark.apache.org/docs/3.3.2/streaming-programming-guide.html#checkpointing) section.

#### Transform Operation

> The `transform` operation (along with its variations like `transformWith`) allows arbitrary RDD-to-RDD functions to be applied on a DStream. It can be used to apply any RDD operation that is not exposed in the DStream API. For example, the functionality of joining every batch in a data stream with another dataset is not directly exposed in the DStream API. However, you can easily use `transform` to do this. This enables very powerful possibilities. For example, one can do real-time data cleaning by joining the input data stream with precomputed spam information (maybe generated with Spark as well) and then filtering based on it.

`transform` 和 `transformWith` 可以在 DStream 上使用任意的 RDD-to-RDD 函数。而这个操作可以用于应用在 DStream API 是没有的 RDD 操作。例如：和其他数据集的每个批次的 join 的功能在 DStream API 是没有的，但你可以使用 `transform` 实现。

一个实际实例就是：通过 join 输入数据流和预计算后的垃圾邮件信息(也由 Spark 生成)，再基于此过滤数据，实现实时数据清洗。

```scala
val spamInfoRDD = ssc.sparkContext.newAPIHadoopRDD(...) // RDD containing spam information

val cleanedDStream = wordCounts.transform { rdd =>
  rdd.join(spamInfoRDD).filter(...) // join data stream with spam information to do data cleaning
  ...
}
```

```java
import org.apache.spark.streaming.api.java.*;
// RDD containing spam information
JavaPairRDD<String, Double> spamInfoRDD = jssc.sparkContext().newAPIHadoopRDD(...);

JavaPairDStream<String, Integer> cleanedDStream = wordCounts.transform(rdd -> {
  rdd.join(spamInfoRDD).filter(...); // join data stream with spam information to do data cleaning
  ...
});
```

```python
spamInfoRDD = sc.pickleFile(...)  # RDD containing spam information

# join data stream with spam information to do data cleaning
cleanedDStream = wordCounts.transform(lambda rdd: rdd.join(spamInfoRDD).filter(...))
```

> Note that the supplied function gets called in every batch interval. This allows you to do time-varying RDD operations, that is, RDD operations, number of partitions, broadcast variables, etc. can be changed between batches.

在每个批次内调用提供的函数。这就允许你执行时变的 RDD 操作，也就是说，RDD 操作、分区数量、广播变量等可以在批次之间更改。

#### Window Operations

> Spark Streaming also provides windowed computations, which allow you to apply transformations over a sliding window of data. The following figure illustrates this sliding window.

Spark Streaming 也提供了窗口计算，允许你在数据的一个滑动窗口上应用转换操作。下图说明了这个滑动窗口：

![streaming-dstream-window.png](./images/streaming-dstream-window.png)

> As shown in the figure, every time the window slides over a source DStream, the source RDDs that fall within the window are combined and operated upon to produce the RDDs of the windowed DStream. In this specific case, the operation is applied over the last 3 time units of data, and slides by 2 time units. This shows that any window operation needs to specify two parameters.

每当窗口在源 DStream 上滑动，在该窗口的 RDDs 会被合并、操作，进而产生 windowed DStream 的 RDDs.

本例中，操作横跨3个时间单元、滑动2个时间单元。这就表明了窗口操作需要指定两个参数：

- 窗口长度：窗口的持续时间

- 滑动间隔：执行窗口操作的间隔

> window length - The duration of the window (3 in the figure).
> sliding interval - The interval at which the window operation is performed (2 in the figure).
 
>These two parameters must be multiples of the batch interval of the source DStream (1 in the figure).

这两个参数必须是源 DStream 的批间隔的倍数。

> Let’s illustrate the window operations with an example. Say, you want to extend the [earlier example](https://spark.apache.org/docs/3.3.2/streaming-programming-guide.html#a-quick-example) by generating word counts over the last 30 seconds of data, every 10 seconds. To do this, we have to apply the `reduceByKey` operation on the `pairs` DStream of `(word, 1)` pairs over the last 30 seconds of data. This is done using the operation `reduceByKeyAndWindow`.

扩展前面的例子：计算过去 30 秒的词频，间隔时间是 10 秒。

我们必须在过去 30 秒的 `(word, 1)` 对的 `pairs` DStream 上应用 `reduceByKey` 操作。

这里使用方法 `reduceByKeyAndWindow` 实现。

```scala
// Reduce last 30 seconds of data, every 10 seconds
val windowedWordCounts = pairs.reduceByKeyAndWindow((a:Int,b:Int) => (a + b), Seconds(30), Seconds(10))
```

```java
// Reduce last 30 seconds of data, every 10 seconds
JavaPairDStream<String, Integer> windowedWordCounts = pairs.reduceByKeyAndWindow((i1, i2) -> i1 + i2, Durations.seconds(30), Durations.seconds(10));
```

```python
# Reduce last 30 seconds of data, every 10 seconds
windowedWordCounts = pairs.reduceByKeyAndWindow(lambda x, y: x + y, lambda x, y: x - y, 30, 10)
```

> Some of the common window operations are as follows. All of these operations take the said two parameters - `windowLength` and `slideInterval`.

一些常用的窗口操作如下所示。这些操作都接收两个参数: `windowLength` 和 `slideInterval`

- window(windowLength, slideInterval)	

	> Return a new DStream which is computed based on windowed batches of the source DStream.

    基于源 DStream 的窗口批次计算，返回一个新的 DStream

- countByWindow(windowLength, slideInterval)	

	> Return a sliding window count of elements in the stream.

    返回流中滑动窗口内的元素计数

- reduceByWindow(func, windowLength, slideInterval)	

	> Return a new single-element stream, created by aggregating elements in the stream over a sliding interval using `func`. The function should be associative and commutative so that it can be computed correctly in parallel.

    使用 `func` 函数，在滑动间隔内，通过聚合流上的元素，返回一个新的单元素的流。

- reduceByKeyAndWindow(func, windowLength, slideInterval, [numTasks])	

	> When called on a DStream of `(K, V)` pairs, returns a new DStream of `(K, V)` pairs where the values for each key are aggregated using the given reduce function `func` over batches in a sliding window. Note: By default, this uses Spark's default number of parallel tasks (2 for local mode, and in cluster mode the number is determined by the config property `spark.default.parallelism`) to do the grouping. You can pass an optional `numTasks` argument to set a different number of tasks.

    当在 `(K, V)` 对 DStream 上调用此算子时，返回一个新的 `(K, V)` 对 DStream, 每个键的值通过在滑动窗口内的批次上使用 `func` 函数聚合得到。

    注意，默认情况下，使用 spark 的默认并行任务数量（本地模式是2, 集群模式是由 `spark.default.parallelism` 决定 ）来分组。

    你可以传递一个可选地 `numTasks` 来设置一个不同的任务数量。

- reduceByKeyAndWindow(func, invFunc, windowLength, slideInterval, [numTasks])	

	> A more efficient version of the above `reduceByKeyAndWindow()` where the reduce value of each window is calculated incrementally using the reduce values of the previous window. This is done by reducing the new data that enters the sliding window, and “inverse reducing” the old data that leaves the window. An example would be that of “adding” and “subtracting” counts of keys as the window slides. However, it is applicable only to “invertible reduce functions”, that is, those reduce functions which have a corresponding “inverse reduce” function (taken as parameter `invFunc`). Like in `reduceByKeyAndWindow`, the number of reduce tasks is configurable through an optional argument. Note that [checkpointing](https://spark.apache.org/docs/3.3.2/streaming-programming-guide.html#checkpointing) must be enabled for using this operation.

    `reduceByKeyAndWindow()` 的更高效版本，每个窗口的 reduce聚合值使用前一个窗口的 reduce聚合值增量地计算。

    通过 reduce 进入滑动窗口的心数据，并“可逆reduce”离开窗口的旧数据实现操作。一个例子就是，随着窗口滑动，加减键的计数。

    然而，这仅适用于“可逆reduce函数”，也就是说，这些 reduce 函数 都有对应的可逆 reduce 函数（能接收 `invFunc` 参数）。

    就像 `reduceByKeyAndWindow`, reduce 任务的数量可以通过可选的参数配置。注意，对于这个操作，必须启用 checkpoint.

- countByValueAndWindow(windowLength, slideInterval, [numTasks])	

	> When called on a DStream of `(K, V)` pairs, returns a new DStream of `(K, Long)` pairs where the value of each key is its frequency within a sliding window. Like in `reduceByKeyAndWindow`, the number of reduce tasks is configurable through an optional argument.

    当在 `(K, V)` 对 DStream 上调用此算子时，返回一个新的 `(K, Long)` 对 DStream, 每个键的值是它在一个滑动窗口内的频数。

    就像 `reduceByKeyAndWindow`, reduce 任务的数量可以通过可选的参数配置。

#### Join Operations

> Finally, its worth highlighting how easily you can perform different kinds of joins in Spark Streaming.

最后，值得强调的是，在 Spark Streaming 中执行不同类型的 joins 是多么容易。

##### Stream-stream joins

> Streams can be very easily joined with other streams.

流可以和其他流 join

```scala
val stream1: DStream[String, String] = ...
val stream2: DStream[String, String] = ...
val joinedStream = stream1.join(stream2)
```

```java
JavaPairDStream<String, String> stream1 = ...
JavaPairDStream<String, String> stream2 = ...
JavaPairDStream<String, Tuple2<String, String>> joinedStream = stream1.join(stream2);
```

```python
stream1 = ...
stream2 = ...
joinedStream = stream1.join(stream2)
```

> Here, in each batch interval, the RDD generated by `stream1` will be joined with the RDD generated by `stream2`. You can also do `leftOuterJoin`, `rightOuterJoin`, `fullOuterJoin`. Furthermore, it is often very useful to do joins over windows of the streams. That is pretty easy as well.

在每个批次间隔内, `stream1` 生成的 RDD 将 join `stream2` 生成的 RDD. 还可以做 `leftOuterJoin`, `rightOuterJoin`, `fullOuterJoin`. 

此外，在流的窗口上进行 join 通常是非常有用的。这也很容易做到。

```scala
val windowedStream1 = stream1.window(Seconds(20))
val windowedStream2 = stream2.window(Minutes(1))
val joinedStream = windowedStream1.join(windowedStream2)
```

```java
JavaPairDStream<String, String> windowedStream1 = stream1.window(Durations.seconds(20));
JavaPairDStream<String, String> windowedStream2 = stream2.window(Durations.minutes(1));
JavaPairDStream<String, Tuple2<String, String>> joinedStream = windowedStream1.join(windowedStream2);
```

```python
windowedStream1 = stream1.window(20)
windowedStream2 = stream2.window(60)
joinedStream = windowedStream1.join(windowedStream2)
```

##### Stream-dataset joins

> This has already been shown earlier while explain `DStream.transform` operation. Here is yet another example of joining a windowed stream with a dataset.

在解释 `DStream.transform` 操作时已展示过。下面展示的是窗口流和数据集的 join 的另一个例子。

```scala
val dataset: RDD[String, String] = ...
val windowedStream = stream.window(Seconds(20))...
val joinedStream = windowedStream.transform { rdd => rdd.join(dataset) }
```

```java
JavaPairRDD<String, String> dataset = ...
JavaPairDStream<String, String> windowedStream = stream.window(Durations.seconds(20));
JavaPairDStream<String, String> joinedStream = windowedStream.transform(rdd -> rdd.join(dataset));
```

```python
dataset = ... # some RDD
windowedStream = stream.window(20)
joinedStream = windowedStream.transform(lambda rdd: rdd.join(dataset))
```

> In fact, you can also dynamically change the dataset you want to join against. The function provided to `transform` is evaluated every batch interval and therefore will use the current dataset that `dataset` reference points to.

实际上，你也可以动态更改你想 join 的数据集。在每个批处理间隔内计算提供给 `transform` 的函数，因此将使用 `dataset` 引用 所指向的当前数据集。

> The complete list of DStream transformations is available in the API documentation. For the Scala API, see [DStream](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/streaming/dstream/DStream.html) and [PairDStreamFunctions](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/streaming/dstream/PairDStreamFunctions.html). For the Java API, see [JavaDStream](https://spark.apache.org/docs/3.3.2/api/java/index.html?org/apache/spark/streaming/api/java/JavaDStream.html) and [JavaPairDStream](https://spark.apache.org/docs/3.3.2/api/java/index.html?org/apache/spark/streaming/api/java/JavaPairDStream.html). For the Python API, see [DStream](https://spark.apache.org/docs/3.3.2/api/python/reference/api/pyspark.streaming.DStream.html#pyspark.streaming.DStream).

### Output Operations on DStreams

> Output operations allow DStream’s data to be pushed out to external systems like a database or a file systems. Since the output operations actually allow the transformed data to be consumed by external systems, they trigger the actual execution of all the DStream transformations (similar to actions for RDDs). Currently, the following output operations are defined:

输出操作可以将 DStream 的数据存入外部系统，如数据库、文件系统。

由于输出操作真正地允许外部系统消费转换的数据，所以是输出操作触发了所有 DStream 转换操作的真正的计算(类似 RDD action)

当前定义了如下的输出操作：

- print()	

	> Prints the first ten elements of every batch of data in a DStream on the driver node running the streaming application. This is useful for development and debugging.

    在运行流应用程序的驱动节点上，打印每个批次的前十个元素。这对开发和 debug 非常有用。

	> Python API This is called `pprint()` in the Python API.

- saveAsTextFiles(prefix, [suffix])	

	> Save this DStream's contents as text files. The file name at each batch interval is generated based on prefix and suffix: `"prefix-TIME_IN_MS[.suffix]"`.

    将这个 DStream 的内容保存为文本文件。

    每个批次间隔上生成的文件名基于 `"prefix-TIME_IN_MS[.suffix]"` 前后缀生成。

- saveAsObjectFiles(prefix, [suffix])	

	> Save this DStream's contents as SequenceFiles of serialized Java objects. The file name at each batch interval is generated based on prefix and suffix: `"prefix-TIME_IN_MS[.suffix]"`.

    将这个 DStream 的内容保存为序列化的 Java 对象的 SequenceFiles. 

    每个批次间隔上生成的文件名基于 `"prefix-TIME_IN_MS[.suffix]"` 前后缀生成。

	> Python API This is not available in the Python API.

- saveAsHadoopFiles(prefix, [suffix])	

	> Save this DStream's contents as Hadoop files. The file name at each batch interval is generated based on prefix and suffix: `"prefix-TIME_IN_MS[.suffix]"`.

    将这个 DStream 的内容保存为 hadoop 文件。

    每个批次间隔上生成的文件名基于 `"prefix-TIME_IN_MS[.suffix]"` 前后缀生成。

	> Python API This is not available in the Python API.

- foreachRDD(func)	

	> The most generic output operator that applies a function, `func`, to each RDD generated from the stream. This function should push the data in each RDD to an external system, such as saving the RDD to files, or writing it over the network to a database. Note that the function `func` is executed in the driver process running the streaming application, and will usually have RDD actions in it that will force the computation of the streaming RDDs.

    将函数 `func` 应用到流上生成的每个 RDD 上。这个函数应该将每个 RDD 上的数据推向外部系统，比如将 RDD 保存到文件，或者通过网络写入到数据库。

    注意，函数 `func` 在运行流应用程序的驱动进程上执行，通常在其中会有 RDD actions 操作，会强制执行流 RDD 计算。

#### Design Patterns for using foreachRDD

> `dstream.foreachRDD` is a powerful primitive that allows data to be sent out to external systems. However, it is important to understand how to use this primitive correctly and efficiently. Some of the common mistakes to avoid are as follows.

dstream.foreachRDD 可以将数据发送到外部系统。但要注意避免如下的常见错误：

> Often writing data to external system requires creating a connection object (e.g. TCP connection to a remote server) and using it to send data to a remote system. For this purpose, a developer may inadvertently try creating a connection object at the Spark driver, and then try to use it in a Spark worker to save records in the RDDs. For example (in Scala),

将数据写到外部系统需要先创建一个连接对象（例如到远程服务的TCP连接），使用它来向远程系统发送数据。

开发者可能无意地在 Spark 驱动上创建一个连接对象，然后在 Spark worker 上使用它来存储 RDDs 中的记录。

```scala
dstream.foreachRDD { rdd =>
  val connection = createNewConnection()  // executed at the driver
  rdd.foreach { record =>
    connection.send(record) // executed at the worker
  }
}
```

```java
dstream.foreachRDD(rdd -> {
  Connection connection = createNewConnection(); // executed at the driver
  rdd.foreach(record -> {
    connection.send(record); // executed at the worker
  });
});
```

```python
def sendRecord(rdd):
    connection = createNewConnection()  # executed at the driver
    rdd.foreach(lambda record: connection.send(record))
    connection.close()

dstream.foreachRDD(sendRecord)
```

> This is incorrect as this requires the connection object to be serialized and sent from the driver to the worker. Such connection objects are rarely transferable across machines. This error may manifest as serialization errors (connection object not serializable), initialization errors (connection object needs to be initialized at the workers), etc. The correct solution is to create the connection object at the worker.

这是不正确的，因为这需要序列化连接对象，并从驱动发送到 worker.

这种连接对象很少能跨机器转移。此错误表现为序列化错误（连接对象不可序列化）、 初始化错误（连接对象需要在 worker 初始化）等。

正确的解决方案是在 worker 创建连接对象。

> However, this can lead to another common mistake - creating a new connection for every record. For example,

但是，这可能会导致另一个常见的错误 - 为每个记录创建一个新的连接。例如:

```scala
dstream.foreachRDD { rdd =>
  rdd.foreach { record =>
    val connection = createNewConnection()
    connection.send(record)
    connection.close()
  }
}
```

```java
dstream.foreachRDD(rdd -> {
  rdd.foreach(record -> {
    Connection connection = createNewConnection();
    connection.send(record);
    connection.close();
  });
});
```

```python
def sendRecord(record):
    connection = createNewConnection()
    connection.send(record)
    connection.close()

dstream.foreachRDD(lambda rdd: rdd.foreach(sendRecord))
```

> Typically, creating a connection object has time and resource overheads. Therefore, creating and destroying a connection object for each record can incur unnecessarily high overheads and can significantly reduce the overall throughput of the system. A better solution is to use `rdd.foreachPartition` - create a single connection object and send all the records in a RDD partition using that connection.

通常，创建连接对象具有时间和资源开销。因此，创建和销毁每个记录的连接对象可能会引起不必要的高开销，并可显著降低系统的总体吞吐量。一个更好的解决方案是使用 `rdd.foreachPartition` - 创建一个单连接对象，并使用该连接在一个 RDD 分区中发送所有记录。

```scala
dstream.foreachRDD { rdd =>
  rdd.foreachPartition { partitionOfRecords =>
    val connection = createNewConnection()
    partitionOfRecords.foreach(record => connection.send(record))
    connection.close()
  }
}
```

```java
dstream.foreachRDD(rdd -> {
  rdd.foreachPartition(partitionOfRecords -> {
    Connection connection = createNewConnection();
    while (partitionOfRecords.hasNext()) {
      connection.send(partitionOfRecords.next());
    }
    connection.close();
  });
});
```

```python
def sendPartition(iter):
    connection = createNewConnection()
    for record in iter:
        connection.send(record)
    connection.close()

dstream.foreachRDD(lambda rdd: rdd.foreachPartition(sendPartition))
```

> This amortizes the connection creation overheads over many records.

这样可以将创建连接的开销分摊到许多记录上。

> Finally, this can be further optimized by reusing connection objects across multiple RDDs/batches. One can maintain a static pool of connection objects than can be reused as RDDs of multiple batches are pushed to the external system, thus further reducing the overheads.

最后，可以通过跨多个RDD/批次重用连接对象来进一步优化。

可以维护连接对象的静态池，而不是将多个批次的 RDDs 推送到外部系统时重新使用，从而进一步减少开销。

```scala
dstream.foreachRDD { rdd =>
  rdd.foreachPartition { partitionOfRecords =>
    // ConnectionPool is a static, lazily initialized pool of connections
    val connection = ConnectionPool.getConnection()
    partitionOfRecords.foreach(record => connection.send(record))
    ConnectionPool.returnConnection(connection)  // return to the pool for future reuse
  }
}
```

```java
dstream.foreachRDD(rdd -> {
  rdd.foreachPartition(partitionOfRecords -> {
    // ConnectionPool is a static, lazily initialized pool of connections
    Connection connection = ConnectionPool.getConnection();
    while (partitionOfRecords.hasNext()) {
      connection.send(partitionOfRecords.next());
    }
    ConnectionPool.returnConnection(connection); // return to the pool for future reuse
  });
});
```

```python
def sendPartition(iter):
    # ConnectionPool is a static, lazily initialized pool of connections
    connection = ConnectionPool.getConnection()
    for record in iter:
        connection.send(record)
    # return to the pool for future reuse
    ConnectionPool.returnConnection(connection)

dstream.foreachRDD(lambda rdd: rdd.foreachPartition(sendPartition))
```

> Note that the connections in the pool should be lazily created on demand and timed out if not used for a while. This achieves the most efficient sending of data to external systems.

注意：池中的连接对此应根据需要懒惰地创建，如果不使用一段时间，则会超时。这实现了最高效的将数据发送到外部系统。

> Other points to remember:

> DStreams are executed lazily by the output operations, just like RDDs are lazily executed by RDD actions. Specifically, RDD actions inside the DStream output operations force the processing of the received data. Hence, if your application does not have any output operation, or has output operations like `dstream.foreachRDD()` without any RDD action inside them, then nothing will get executed. The system will simply receive the data and discard it.

DStreams 通过输出操作懒执行，就像 RDD 通过 actions 操作懒执行一样。

具体来说，DStream 输出操作中的底层 RDD actions 操作强制处理接收到的数据。

因此，如果你的应用程序没有任何输出操作，或者像 `dstream.foreachRDD()` 的没有 RDD actions 的输出操作，则不会执行任何操作。系统将简单地接收数据并将其丢弃。

> By default, output operations are executed one-at-a-time. And they are executed in the order they are defined in the application.

默认情况下，输出操作是一次只执行一个。它们按照它们在应用程序中定义的顺序执行。

### DataFrame and SQL Operations

> You can easily use [DataFrames and SQL](https://spark.apache.org/docs/3.3.2/sql-programming-guide.html) operations on streaming data. You have to create a SparkSession using the SparkContext that the StreamingContext is using. Furthermore, this has to done such that it can be restarted on driver failures. This is done by creating a lazily instantiated singleton instance of SparkSession. This is shown in the following example. It modifies the earlier [word count example](https://spark.apache.org/docs/3.3.2/streaming-programming-guide.html#a-quick-example) to generate word counts using DataFrames and SQL. Each RDD is converted to a DataFrame, registered as a temporary table and then queried using SQL.

可以在流数据上使用 DataFrames 和 SQL 操作。必须使用 StreamingContext 正在使用的 SparkContext 创建一个 SparkSession.

此外，必须这样做，以便可以在驱动故障时重新启动。通过懒惰地创建一个实例化的 SparkSession 单例对象来完成。

在下面例子中展示。修改了前面的计数例子，以使用 DataFrames 和 SQL 生成单词计数。将每个 RDD 转换成注册成临时表的 DataFrame, 然后使用 SQL 查询。

```scala
/** DataFrame operations inside your streaming program */

val words: DStream[String] = ...

words.foreachRDD { rdd =>

  // Get the singleton instance of SparkSession
  val spark = SparkSession.builder.config(rdd.sparkContext.getConf).getOrCreate()
  import spark.implicits._

  // Convert RDD[String] to DataFrame
  val wordsDataFrame = rdd.toDF("word")

  // Create a temporary view
  wordsDataFrame.createOrReplaceTempView("words")

  // Do word count on DataFrame using SQL and print it
  val wordCountsDataFrame = 
    spark.sql("select word, count(*) as total from words group by word")
  wordCountsDataFrame.show()
}
```

See the full [source code](https://github.com/apache/spark/blob/v3.3.2/examples/src/main/scala/org/apache/spark/examples/streaming/SqlNetworkWordCount.scala).

```java
/** Java Bean class for converting RDD to DataFrame */
public class JavaRow implements java.io.Serializable {
  private String word;

  public String getWord() {
    return word;
  }

  public void setWord(String word) {
    this.word = word;
  }
}

...

/** DataFrame operations inside your streaming program */

JavaDStream<String> words = ... 

words.foreachRDD((rdd, time) -> {
  // Get the singleton instance of SparkSession
  SparkSession spark = SparkSession.builder().config(rdd.sparkContext().getConf()).getOrCreate();

  // Convert RDD[String] to RDD[case class] to DataFrame
  JavaRDD<JavaRow> rowRDD = rdd.map(word -> {
    JavaRow record = new JavaRow();
    record.setWord(word);
    return record;
  });
  DataFrame wordsDataFrame = spark.createDataFrame(rowRDD, JavaRow.class);

  // Creates a temporary view using the DataFrame
  wordsDataFrame.createOrReplaceTempView("words");

  // Do word count on table using SQL and print it
  DataFrame wordCountsDataFrame =
    spark.sql("select word, count(*) as total from words group by word");
  wordCountsDataFrame.show();
});
```

See the full [source code](https://github.com/apache/spark/blob/v3.3.2/examples/src/main/java/org/apache/spark/examples/streaming/JavaSqlNetworkWordCount.java).

```python
# Lazily instantiated global instance of SparkSession
def getSparkSessionInstance(sparkConf):
    if ("sparkSessionSingletonInstance" not in globals()):
        globals()["sparkSessionSingletonInstance"] = SparkSession \
            .builder \
            .config(conf=sparkConf) \
            .getOrCreate()
    return globals()["sparkSessionSingletonInstance"]

...

# DataFrame operations inside your streaming program

words = ... # DStream of strings

def process(time, rdd):
    print("========= %s =========" % str(time))
    try:
        # Get the singleton instance of SparkSession
        spark = getSparkSessionInstance(rdd.context.getConf())

        # Convert RDD[String] to RDD[Row] to DataFrame
        rowRdd = rdd.map(lambda w: Row(word=w))
        wordsDataFrame = spark.createDataFrame(rowRdd)

        # Creates a temporary view using the DataFrame
        wordsDataFrame.createOrReplaceTempView("words")

        # Do word count on table using SQL and print it
        wordCountsDataFrame = spark.sql("select word, count(*) as total from words group by word")
        wordCountsDataFrame.show()
    except:
        pass

words.foreachRDD(process)
```

See the full [source code](https://github.com/apache/spark/blob/v3.3.2/examples/src/main/python/streaming/sql_network_wordcount.py).

> You can also run SQL queries on tables defined on streaming data from a different thread (that is, asynchronous to the running StreamingContext). Just make sure that you set the StreamingContext to remember a sufficient amount of streaming data such that the query can run. Otherwise the StreamingContext, which is unaware of the any asynchronous SQL queries, will delete off old streaming data before the query can complete. For example, if you want to query the last batch, but your query can take 5 minutes to run, then call `streamingContext.remember(Minutes(5))` (in Scala, or equivalent in other languages).

你也可以从一个不同的线程上基于定义在流数据的表上运行 SQL 查询。只需确保设置 StreamingContext 以记住足够数量的流数据，以便查询可以运行。否则，不清楚任意的异步 SQL 查询的 StreamingContext 将在查询完成前，删除旧的流数据。

例如，如果你想查询最后的批次，但是你的查询可能需要5分钟才能运行，那么调用 `streamingContext.remember(Minutes(5))`

> See the [DataFrames and SQL](https://spark.apache.org/docs/3.3.2/sql-programming-guide.html) guide to learn more about DataFrames.

### MLlib Operations

> You can also easily use machine learning algorithms provided by [MLlib](https://spark.apache.org/docs/3.3.2/ml-guide.html). First of all, there are streaming machine learning algorithms (e.g. [Streaming Linear Regression](https://spark.apache.org/docs/3.3.2/mllib-linear-methods.html#streaming-linear-regression), [Streaming KMeans](https://spark.apache.org/docs/3.3.2/mllib-clustering.html#streaming-k-means), etc.) which can simultaneously learn from the streaming data as well as apply the model on the streaming data. Beyond these, for a much larger class of machine learning algorithms, you can learn a learning model offline (i.e. using historical data) and then apply the model online on streaming data. See the [MLlib](https://spark.apache.org/docs/3.3.2/ml-guide.html) guide for more details.

你也可以使用 MLlib 提供的机器学习算法。首先，存在流机器学习算法，可以从流数据学习，也可以将模型应用在流数据上。

除此之外，对于更大的机器学习算法，你可以离线学习模型（使用历史数据），然后在流数据上在线地应用模型。

### Caching / Persistence

> Similar to RDDs, DStreams also allow developers to persist the stream’s data in memory. That is, using the `persist()` method on a DStream will automatically persist every RDD of that DStream in memory. This is useful if the data in the DStream will be computed multiple times (e.g., multiple operations on the same data). For window-based operations like `reduceByWindow` and `reduceByKeyAndWindow` and state-based operations like `updateStateByKey`, this is implicitly true. Hence, DStreams generated by window-based operations are automatically persisted in memory, without the developer calling `persist()`.

类似 RDDs, DStreams 也允许开发者将流数据持久化到内存中，也就是，在 DStream 上使用 `persist()` 方法将 DStream 上的每个 RDD 自动持久化到内存中。如果 DStreams 中的数据需要被计算多次时是有用的（例如，在同一数据的多次操作）。

对于基于窗口的操作和基于状态的操作是非常有用的。基于窗口的操作产生的 DStreams 会自动持久化到内存，而不用调用 `persist()`.

> For input streams that receive data over the network (such as, Kafka, sockets, etc.), the default persistence level is set to replicate the data to two nodes for fault-tolerance.

对于通过网络接收数据的输入流，默认的持久化级别是将数据复制到两个结点，以容错。

> Note that, unlike RDDs, the default persistence level of DStreams keeps the data serialized in memory. This is further discussed in the [Performance Tuning](https://spark.apache.org/docs/3.3.2/streaming-programming-guide.html#memory-tuning) section. More information on different persistence levels can be found in the [Spark Programming Guide](https://spark.apache.org/docs/3.3.2/rdd-programming-guide.html#rdd-persistence).

注意：不同于 RDDs, DStreams 默认的持久化级别是将数据序列化在内存中。在性能调优部分再深入讨论。

### Checkpointing

> A streaming application must operate 24/7 and hence must be resilient to failures unrelated to the application logic (e.g., system failures, JVM crashes, etc.). For this to be possible, Spark Streaming needs to checkpoint enough information to a fault- tolerant storage system such that it can recover from failures. There are two types of data that are checkpointed.

流应用程序必须 24/7 运行，因此必须对与程序逻辑无关的故障（例如，系统故障，JVM 崩溃等）具有弹性。为了使其成可能，Spark Streaming 需要将足够的信息 checkpoint 到容错存储系统，以便可以从故障中恢复。有两种类型的 checkpoint  数据：

> Metadata checkpointing - Saving of the information defining the streaming computation to fault-tolerant storage like HDFS. This is used to recover from failure of the node running the driver of the streaming application (discussed in detail later). Metadata includes:

- 元数据 checkpointing: 将包含了流计算的信息存入到像 HDFS 一样的容错系统。这用于从运行流应用程序驱动的节点的故障中恢复。元数据主要有：

    + 配置：创建流程序的配置信息。
    + DStream 操作：定义流程序的 DStream 操作集。
    + 未完成的批次：进入作业队列但尚未完成的批次。

> Configuration - The configuration that was used to create the streaming application.
> DStream operations - The set of DStream operations that define the streaming application.
> Incomplete batches - Batches whose jobs are queued but have not completed yet.

> Data checkpointing - Saving of the generated RDDs to reliable storage. This is necessary in some stateful transformations that combine data across multiple batches. In such transformations, the generated RDDs depend on RDDs of previous batches, which causes the length of the dependency chain to keep increasing with time. To avoid such unbounded increases in recovery time (proportional to dependency chain), intermediate RDDs of stateful transformations are periodically checkpointed to reliable storage (e.g. HDFS) to cut off the dependency chains.

- 数据 checkpointing: 将生成的 RDDs 存入可靠的存储。这在跨批次合并数据的状态转换操作中是非常必要的。在这种转换操作中，生成的 RDDs 依赖于先前批次的 RDDs, 这就产生了一种随时间而增长的依赖链。为了避免在恢复时间内的这种无限增长（与依赖关系链成比例），有状态的转换操作的中间 RDD 会定期 checkpoint 到可靠的存储（例如 HDFS）以切断依赖关系链。

> To summarize, metadata checkpointing is primarily needed for recovery from driver failures, whereas data or RDD checkpointing is necessary even for basic functioning if stateful transformations are used.

总而言之，元数据 checkpoint 主要用于从驱动故障中恢复，而数据或 RDD checkpoint 对于基本功能（如果使用有状态转换）则是必需的。

#### When to enable Checkpointing

> Checkpointing must be enabled for applications with any of the following requirements:

对于具有以下任一要求的应用程序，必须启用 checkpoint: 

> Usage of stateful transformations - If either `updateStateByKey` or `reduceByKeyAndWindow` (with inverse function) is used in the application, then the checkpoint directory must be provided to allow for periodic RDD checkpointing.

- 有状态转换操作的使用：如果在应用程序中使用 `updateStateByKey` 或 `reduceByKeyAndWindow`（具有逆向功能），则必须提供 checkpoint 目录，以允许定期的 RDD checkpoint.

> Recovering from failures of the driver running the application - Metadata checkpoints are used to recover with progress information.

- 从运行应用程序的驱动的故障中恢复：元数据 checkpoint 用于使用进度信息进行恢复。

> Note that simple streaming applications without the aforementioned stateful transformations can be run without enabling checkpointing. The recovery from driver failures will also be partial in that case (some received but unprocessed data may be lost). This is often acceptable and many run Spark Streaming applications in this way. Support for non-Hadoop environments is expected to improve in the future.

注意：没有状态转换的简单流应用程序无需启用 checkpoint 即可运行。

从驱动故障恢复中也是那种情况的一部分（一些接收但未处理的数据可能会丢失）。这通常是可以接受的，许多 Spark Streaming 应用程序都以这种方式运行。未来对非 Hadoop 环境的支持预计会有所改善。

#### How to configure Checkpointing

> Checkpointing can be enabled by setting a directory in a fault-tolerant, reliable file system (e.g., HDFS, S3, etc.) to which the checkpoint information will be saved. This is done by using `streamingContext.checkpoint(checkpointDirectory)`. This will allow you to use the aforementioned stateful transformations. Additionally, if you want to make the application recover from driver failures, you should rewrite your streaming application to have the following behavior.

通过在容错、可靠的文件系统中设置一个 checkpoint 信息存储的目录，来启动 checkpoint. 可以使用 `streamingContext.checkpoint(checkpointDirectory)` 来设置。这将允许你使用状态转换操作。

如果要使应用程序从驱动故障中恢复，您应该重写流应用程序，使其具有以下行为：

- 当第一次启动项目，会创建一个新的 StreamingContext, 设置好所有的流，然后调用 `start()`

- 当出现故障后重启项目，会根据 checkpoint 目录下的 checkpoint 数据，再次创建一个新的 StreamingContext 

> When the program is being started for the first time, it will create a new StreamingContext, set up all the streams and then call `start()`.

> When the program is being restarted after failure, it will re-create a StreamingContext from the checkpoint data in the checkpoint directory.

##### Scala

> This behavior is made simple by using `StreamingContext.getOrCreate`. This is used as follows.

```scala
// Function to create and setup a new StreamingContext
def functionToCreateContext(): StreamingContext = {
  val ssc = new StreamingContext(...)   // new context
  val lines = ssc.socketTextStream(...) // create DStreams
  ...
  ssc.checkpoint(checkpointDirectory)   // set checkpoint directory
  ssc
}

// Get StreamingContext from checkpoint data or create a new one
val context = StreamingContext.getOrCreate(checkpointDirectory, functionToCreateContext _)

// Do additional setup on context that needs to be done,
// irrespective of whether it is being started or restarted
context. ...

// Start the context
context.start()
context.awaitTermination()
```

> If the `checkpointDirectory` exists, then the context will be recreated from the checkpoint data. If the directory does not exist (i.e., running for the first time), then the function `functionToCreateContext` will be called to create a new context and set up the DStreams. See the Scala example [RecoverableNetworkWordCount](https://github.com/apache/spark/tree/master/examples/src/main/scala/org/apache/spark/examples/streaming/RecoverableNetworkWordCount.scala). This example appends the word counts of network data into a file.

如果 `checkpointDirectory` 存在，那么将从 checkpoint 数据中再次创建上下文。如果目录不存在（例如，第一次允许），会调用 `functionToCreateContext` 来创建一个新的上下文，并设置 DStreams.

> In addition to using `getOrCreate` one also needs to ensure that the driver process gets restarted automatically on failure. This can only be done by the deployment infrastructure that is used to run the application. This is further discussed in the [Deployment](https://spark.apache.org/docs/3.3.2/streaming-programming-guide.html#deploying-applications) section.

除了使用 `getOrCreate` 之外，还需要确保在失败时能自动重新启动驱动进程。这只能由用于运行应用程序的部署基础架构完成。

> Note that checkpointing of RDDs incurs the cost of saving to reliable storage. This may cause an increase in the processing time of those batches where RDDs get checkpointed. Hence, the interval of checkpointing needs to be set carefully. At small batch sizes (say 1 second), checkpointing every batch may significantly reduce operation throughput. Conversely, checkpointing too infrequently causes the lineage and task sizes to grow, which may have detrimental effects. For stateful transformations that require RDD checkpointing, the default interval is a multiple of the batch interval that is at least 10 seconds. It can be set by using `dstream.checkpoint(checkpointInterval)`. Typically, a checkpoint interval of 5 - 10 sliding intervals of a DStream is a good setting to try.

注意: RDDs 的 checkpoint 会使保存到可靠存储产生代价。这会增加这些 RDDs checkpoint 所在批次的处理时间。因此需要仔细设置 checkpoint 的间隔, 批次太小（1秒）, checkpoint 每个批次可能会显著降低吞吐量，相反，如果太不频繁，会导致谱系和任务大小的增长。

对于需要 RDD checkpoint 的状态转换操作，默认间隔是批次间隔的倍数，批次间隔至少是10秒。可以通过使用 `dstream.checkpoint(checkpointInterval)`进行设置。通常，一个 DStream 的5到10个滑动间隔的 checkpoint 间隔是一个很好的设置。

##### Java

> This behavior is made simple by using `JavaStreamingContext.getOrCreate`. This is used as follows.

```java
// Create a factory object that can create and setup a new JavaStreamingContext
JavaStreamingContextFactory contextFactory = new JavaStreamingContextFactory() {
  @Override public JavaStreamingContext create() {
    JavaStreamingContext jssc = new JavaStreamingContext(...);  // new context
    JavaDStream<String> lines = jssc.socketTextStream(...);     // create DStreams
    ...
    jssc.checkpoint(checkpointDirectory);                       // set checkpoint directory
    return jssc;
  }
};

// Get JavaStreamingContext from checkpoint data or create a new one
JavaStreamingContext context = JavaStreamingContext.getOrCreate(checkpointDirectory, contextFactory);

// Do additional setup on context that needs to be done,
// irrespective of whether it is being started or restarted
context. ...

// Start the context
context.start();
context.awaitTermination();
```

> If the `checkpointDirectory` exists, then the context will be recreated from the checkpoint data. If the directory does not exist (i.e., running for the first time), then the function contextFactory will be called to create a new context and set up the DStreams. See the Java example [JavaRecoverableNetworkWordCount](https://github.com/apache/spark/tree/master/examples/src/main/java/org/apache/spark/examples/streaming/JavaRecoverableNetworkWordCount.java). This example appends the word counts of network data into a file.

> In addition to using `getOrCreate` one also needs to ensure that the driver process gets restarted automatically on failure. This can only be done by the deployment infrastructure that is used to run the application. This is further discussed in the [Deployment](https://spark.apache.org/docs/3.3.2/streaming-programming-guide.html#deploying-applications) section.

> Note that checkpointing of RDDs incurs the cost of saving to reliable storage. This may cause an increase in the processing time of those batches where RDDs get checkpointed. Hence, the interval of checkpointing needs to be set carefully. At small batch sizes (say 1 second), checkpointing every batch may significantly reduce operation throughput. Conversely, checkpointing too infrequently causes the lineage and task sizes to grow, which may have detrimental effects. For stateful transformations that require RDD checkpointing, the default interval is a multiple of the batch interval that is at least 10 seconds. It can be set by using `dstream.checkpoint(checkpointInterval)`. Typically, a checkpoint interval of 5 - 10 sliding intervals of a DStream is a good setting to try.

##### Python

> This behavior is made simple by using `StreamingContext.getOrCreate`. This is used as follows.

```python
# Function to create and setup a new StreamingContext
def functionToCreateContext():
    sc = SparkContext(...)  # new context
    ssc = StreamingContext(...)
    lines = ssc.socketTextStream(...)  # create DStreams
    ...
    ssc.checkpoint(checkpointDirectory)  # set checkpoint directory
    return ssc

# Get StreamingContext from checkpoint data or create a new one
context = StreamingContext.getOrCreate(checkpointDirectory, functionToCreateContext)

# Do additional setup on context that needs to be done,
# irrespective of whether it is being started or restarted
context. ...

# Start the context
context.start()
context.awaitTermination()
```

> If the `checkpointDirectory` exists, then the context will be recreated from the checkpoint data. If the directory does not exist (i.e., running for the first time), then the function `functionToCreateContext` will be called to create a new context and set up the DStreams. See the Python example [recoverable_network_wordcount.py](https://github.com/apache/spark/tree/master/examples/src/main/python/streaming/recoverable_network_wordcount.py). This example appends the word counts of network data into a file.

> You can also explicitly create a `StreamingContext` from the checkpoint data and start the computation by using `StreamingContext.getOrCreate(checkpointDirectory, None)`.

> In addition to using `getOrCreate` one also needs to ensure that the driver process gets restarted automatically on failure. This can only be done by the deployment infrastructure that is used to run the application. This is further discussed in the [Deployment](https://spark.apache.org/docs/3.3.2/streaming-programming-guide.html#deploying-applications) section.

> Note that checkpointing of RDDs incurs the cost of saving to reliable storage. This may cause an increase in the processing time of those batches where RDDs get checkpointed. Hence, the interval of checkpointing needs to be set carefully. At small batch sizes (say 1 second), checkpointing every batch may significantly reduce operation throughput. Conversely, checkpointing too infrequently causes the lineage and task sizes to grow, which may have detrimental effects. For stateful transformations that require RDD checkpointing, the default interval is a multiple of the batch interval that is at least 10 seconds. It can be set by using `dstream.checkpoint(checkpointInterval)`. Typically, a checkpoint interval of 5 - 10 sliding intervals of a DStream is a good setting to try.

### Accumulators, Broadcast Variables, and Checkpoints

> [Accumulators](https://spark.apache.org/docs/3.3.2/rdd-programming-guide.html#accumulators) and [Broadcast variables](https://spark.apache.org/docs/3.3.2/rdd-programming-guide.html#broadcast-variables) cannot be recovered from checkpoint in Spark Streaming. If you enable checkpointing and use Accumulators or Broadcast variables as well, you’ll have to create lazily instantiated singleton instances for Accumulators and Broadcast variables so that they can be re-instantiated after the driver restarts on failure. This is shown in the following example.

在 Spark Streaming 中，累加器和广播变量不能从 checkpoint 中恢复。如果你既启用了 checkpoint, 也使用了累加器和广播变量，你必须创建一个懒惰地实例化的累加器和广播变量的单例对象。以便在驱动失败重启后，再次实例化。

```Scala
object WordExcludeList {

  @volatile private var instance: Broadcast[Seq[String]] = null

  def getInstance(sc: SparkContext): Broadcast[Seq[String]] = {
    if (instance == null) {
      synchronized {
        if (instance == null) {
          val wordExcludeList = Seq("a", "b", "c")
          instance = sc.broadcast(wordExcludeList)
        }
      }
    }
    instance
  }
}

object DroppedWordsCounter {

  @volatile private var instance: LongAccumulator = null

  def getInstance(sc: SparkContext): LongAccumulator = {
    if (instance == null) {
      synchronized {
        if (instance == null) {
          instance = sc.longAccumulator("DroppedWordsCounter")
        }
      }
    }
    instance
  }
}

wordCounts.foreachRDD { (rdd: RDD[(String, Int)], time: Time) =>
  // Get or register the excludeList Broadcast
  val excludeList = WordExcludeList.getInstance(rdd.sparkContext)
  // Get or register the droppedWordsCounter Accumulator
  val droppedWordsCounter = DroppedWordsCounter.getInstance(rdd.sparkContext)
  // Use excludeList to drop words and use droppedWordsCounter to count them
  val counts = rdd.filter { case (word, count) =>
    if (excludeList.value.contains(word)) {
      droppedWordsCounter.add(count)
      false
    } else {
      true
    }
  }.collect().mkString("[", ", ", "]")
  val output = "Counts at time " + time + " " + counts
})
```

See the full [source code](https://github.com/apache/spark/blob/v3.3.2/examples/src/main/scala/org/apache/spark/examples/streaming/RecoverableNetworkWordCount.scala).

```Java
class JavaWordExcludeList {

  private static volatile Broadcast<List<String>> instance = null;

  public static Broadcast<List<String>> getInstance(JavaSparkContext jsc) {
    if (instance == null) {
      synchronized (JavaWordExcludeList.class) {
        if (instance == null) {
          List<String> wordExcludeList = Arrays.asList("a", "b", "c");
          instance = jsc.broadcast(wordExcludeList);
        }
      }
    }
    return instance;
  }
}

class JavaDroppedWordsCounter {

  private static volatile LongAccumulator instance = null;

  public static LongAccumulator getInstance(JavaSparkContext jsc) {
    if (instance == null) {
      synchronized (JavaDroppedWordsCounter.class) {
        if (instance == null) {
          instance = jsc.sc().longAccumulator("DroppedWordsCounter");
        }
      }
    }
    return instance;
  }
}

wordCounts.foreachRDD((rdd, time) -> {
  // Get or register the excludeList Broadcast
  Broadcast<List<String>> excludeList = JavaWordExcludeList.getInstance(new JavaSparkContext(rdd.context()));
  // Get or register the droppedWordsCounter Accumulator
  LongAccumulator droppedWordsCounter = JavaDroppedWordsCounter.getInstance(new JavaSparkContext(rdd.context()));
  // Use excludeList to drop words and use droppedWordsCounter to count them
  String counts = rdd.filter(wordCount -> {
    if (excludeList.value().contains(wordCount._1())) {
      droppedWordsCounter.add(wordCount._2());
      return false;
    } else {
      return true;
    }
  }).collect().toString();
  String output = "Counts at time " + time + " " + counts;
}
```

See the full [source code](https://github.com/apache/spark/blob/v3.3.2/examples/src/main/java/org/apache/spark/examples/streaming/JavaRecoverableNetworkWordCount.java).

```Python
def getWordExcludeList(sparkContext):
    if ("wordExcludeList" not in globals()):
        globals()["wordExcludeList"] = sparkContext.broadcast(["a", "b", "c"])
    return globals()["wordExcludeList"]

def getDroppedWordsCounter(sparkContext):
    if ("droppedWordsCounter" not in globals()):
        globals()["droppedWordsCounter"] = sparkContext.accumulator(0)
    return globals()["droppedWordsCounter"]

def echo(time, rdd):
    # Get or register the excludeList Broadcast
    excludeList = getWordExcludeList(rdd.context)
    # Get or register the droppedWordsCounter Accumulator
    droppedWordsCounter = getDroppedWordsCounter(rdd.context)

    # Use excludeList to drop words and use droppedWordsCounter to count them
    def filterFunc(wordCount):
        if wordCount[0] in excludeList.value:
            droppedWordsCounter.add(wordCount[1])
            False
        else:
            True

    counts = "Counts at time %s %s" % (time, rdd.filter(filterFunc).collect())

wordCounts.foreachRDD(echo)
```

See the full [source code](https://github.com/apache/spark/blob/v3.3.2/examples/src/main/python/streaming/recoverable_network_wordcount.py).

### Deploying Applications

> This section discusses the steps to deploy a Spark Streaming application.

本部分讨论部署一个 Spark Streaming 应用程序的步骤。

#### Requirements

> To run a Spark Streaming applications, you need to have the following.

要运行一个 Spark Streaming 应用程序，你需要有下列内容：

> Cluster with a cluster manager - This is the general requirement of any Spark application, and discussed in detail in the [deployment guide](https://spark.apache.org/docs/3.3.2/cluster-overview.html).

- 带有集群管理器的集群：这是任意 spark 应用程序的普遍要求。

> Package the application JAR - You have to compile your streaming application into a JAR. If you are using [spark-submit](https://spark.apache.org/docs/3.3.2/submitting-applications.html) to start the application, then you will not need to provide Spark and Spark Streaming in the JAR. However, if your application uses [advanced sources](https://spark.apache.org/docs/3.3.2/streaming-programming-guide.html#advanced-sources) (e.g. Kafka), then you will have to package the extra artifact they link to, along with their dependencies, in the JAR that is used to deploy the application. For example, an application using `KafkaUtils` will have to include `spark-streaming-kafka-0-10_2.12` and all its transitive dependencies in the application JAR.

- 打包应用程序的 JAR: 你必须将你的流应用程序编译到一个 JAR 包里。如果你使用了 `spark-submit` 启动应用程序，则不需要在 JAR 包提供 Spark 和 Spark Streaming. 然而，如果你的应用程序使用了高级源(kafka)，那么你需要将链接的额外 artifact 和依赖打包进 JAR 包中。例如，使用 `KafkaUtils` 的应用程序将必须在应用程序 JAR 包中包含 `spark-streaming-kafka-0-10_2.12` 和它的传递依赖。

> Configuring sufficient memory for the executors - Since the received data must be stored in memory, the executors must be configured with sufficient memory to hold the received data. Note that if you are doing 10 minute window operations, the system has to keep at least last 10 minutes of data in memory. So the memory requirements for the application depends on the operations used in it.

- 为 executors 配置足够的内存：因为接收的数据会一直存放在内存中，所以必须为保存接收的数据而为 executors 配置足够的内存。注意，如果你做10分钟的窗口操作，系统必须至少将最近10分钟的数据保存在内存中。所以，应用程序的内容要求依赖于其中的操作。

> Configuring checkpointing - If the stream application requires it, then a directory in the Hadoop API compatible fault-tolerant storage (e.g. HDFS, S3, etc.) must be configured as the checkpoint directory and the streaming application written in a way that checkpoint information can be used for failure recovery. See the [checkpointing](https://spark.apache.org/docs/3.3.2/streaming-programming-guide.html#checkpointing) section for more details.

- 配置 checkpoint: 如果流应用程序要求，那么必须将和 Hadoop API 兼容的容错存储中的目录配置为 checkpoint 目录，并以 checkpoint 信息可用于故障恢复的方式编写流应用程序。

> Configuring automatic restart of the application driver - To automatically recover from a driver failure, the deployment infrastructure that is used to run the streaming application must monitor the driver process and relaunch the driver if it fails. Different [cluster managers](https://spark.apache.org/docs/3.3.2/cluster-overview.html#cluster-manager-types) have different tools to achieve this.

> Spark Standalone - A Spark application driver can be submitted to run within the Spark Standalone cluster (see [cluster deploy mode](https://spark.apache.org/docs/3.3.2/spark-standalone.html#launching-spark-applications)), that is, the application driver itself runs on one of the worker nodes. Furthermore, the Standalone cluster manager can be instructed to supervise the driver, and relaunch it if the driver fails either due to non-zero exit code, or due to failure of the node running the driver. See cluster mode and supervise in the [Spark Standalone guide](https://spark.apache.org/docs/3.3.2/spark-standalone.html) for more details.

> YARN - Yarn supports a similar mechanism for automatically restarting an application. Please refer to YARN documentation for more details.

> Mesos - [Marathon](https://github.com/mesosphere/marathon) has been used to achieve this with Mesos.

- 配置应用程序驱动的自动重启：要自动从驱动故障中恢复，用于运行流应用程序的部署基础架构必须监控驱动进程，并在失败时重启驱动。不同集群管理器有不同工具实现：

    - Spark Standalone: 提交 Spark 应用程序驱动，以在 Spark Standalone 集群中运行，即用程序驱动本身在其中一个工作节点上运行。此外，可以指示 Standalone 集群管理器监督驱动，如果由于 非零退出代码 而导致驱动发生故障，或由于运行驱动的节点发生故障，则可以重新启动它。
    
    - YARN: 支持相似的自动重启程序的机制
    
    - Mesos: 使用 Marathon 实现这个功能

> Configuring write-ahead logs - Since Spark 1.2, we have introduced write-ahead logs for achieving strong fault-tolerance guarantees. If enabled, all the data received from a receiver gets written into a write-ahead log in the configuration checkpoint directory. This prevents data loss on driver recovery, thus ensuring zero data loss (discussed in detail in the [Fault-tolerance Semantics](https://spark.apache.org/docs/3.3.2/streaming-programming-guide.html#fault-tolerance-semantics) section). This can be enabled by setting the [configuration parameter](https://spark.apache.org/docs/3.3.2/configuration.html#spark-streaming) `spark.streaming.receiver.writeAheadLog.enable` to true. However, these stronger semantics may come at the cost of the receiving throughput of individual receivers. This can be corrected by running [more receivers in parallel](https://spark.apache.org/docs/3.3.2/streaming-programming-guide.html#level-of-parallelism-in-data-receiving) to increase aggregate throughput. Additionally, it is recommended that the replication of the received data within Spark be disabled when the write-ahead log is enabled as the log is already stored in a replicated storage system. This can be done by setting the storage level for the input stream to `StorageLevel.MEMORY_AND_DISK_SER`. While using S3 (or any file system that does not support flushing) for write-ahead logs, please remember to enable `spark.streaming.driver.writeAheadLog.closeFileAfterWrite` and `spark.streaming.receiver.writeAheadLog.closeFileAfterWrite`. See [Spark Streaming Configuration](https://spark.apache.org/docs/3.3.2/configuration.html#spark-streaming) for more details. Note that Spark will not encrypt data written to the write-ahead log when I/O encryption is enabled. If encryption of the write-ahead log data is desired, it should be stored in a file system that supports encryption natively.

> Configuring write-ahead logs - Since Spark 1.2, we have introduced write-ahead logs for achieving strong fault-tolerance guarantees. If enabled, all the data received from a receiver gets written into a write-ahead log in the configuration checkpoint directory. This prevents data loss on driver recovery, thus ensuring zero data loss (discussed in detail in the [Fault-tolerance Semantics](https://spark.apache.org/docs/3.3.2/streaming-programming-guide.html#fault-tolerance-semantics) section). This can be enabled by setting the [configuration parameter](https://spark.apache.org/docs/3.3.2/configuration.html#spark-streaming) `spark.streaming.receiver.writeAheadLog.enable` to true. However, these stronger semantics may come at the cost of the receiving throughput of individual receivers. This can be corrected by running [more receivers in parallel](https://spark.apache.org/docs/3.3.2/streaming-programming-guide.html#level-of-parallelism-in-data-receiving) to increase aggregate throughput. Additionally, it is recommended that the replication of the received data within Spark be disabled when the write-ahead log is enabled as the log is already stored in a replicated storage system. This can be done by setting the storage level for the input stream to `StorageLevel.MEMORY_AND_DISK_SER`. While using S3 (or any file system that does not support flushing) for write-ahead logs, please remember to enable `spark.streaming.driver.writeAheadLog.closeFileAfterWrite` and `spark.streaming.receiver.writeAheadLog.closeFileAfterWrite`. See [Spark Streaming Configuration](https://spark.apache.org/docs/3.3.2/configuration.html#spark-streaming) for more details. Note that Spark will not encrypt data written to the write-ahead log when I/O encryption is enabled. If encryption of the write-ahead log data is desired, it should be stored in a file system that supports encryption natively.

- 配置预写日志： 

    从 Spark 1.2 开始，我们引入了预写日志，以实现强容错保证。如果启用，所有从接收器接收的数据都被写入到配置的 checkpoint 目录中的预写日志中。这样可以防止驱动恢复时数据丢失，从而确保零数据丢失。这可以通过设置 `spark.streaming.receiver.writeAheadLog.enable` 为 true 启用。

    然而，这些更强语义可能以牺牲单个接收器的接收吞吐量为代价。。这可以通过并行运行多个接收器纠正，以增加总吞吐量。另外，当启用了预写日志时，推荐禁用 spark 中接收数据的副本机制，因为日志已存储在了具有副本机制的存储系统中。这可以通过将输入流的存储级别设置为 `StorageLevel.MEMORY_AND_DISK_SER` 实现。  

    当使用 S3 （或任意不支持刷新的文件系统）作为预写日志的存储系统时，推荐启用 `spark.streaming.driver.writeAheadLog.closeFileAfterWrite` 和 `spark.streaming.receiver.writeAheadLog.closeFileAfterWrite`.

    注意，当启用 I/O 加密时, spark 不会加密写入预写日志的数据。如果希望加密，应该将数据存储在原生支撑加密的文件系统。

> Setting the max receiving rate - If the cluster resources is not large enough for the streaming application to process data as fast as it is being received, the receivers can be rate limited by setting a maximum rate limit in terms of `records / sec`. See the [configuration parameters](https://spark.apache.org/docs/3.3.2/configuration.html#spark-streaming) `spark.streaming.receiver.maxRate` for receivers and `spark.streaming.kafka.maxRatePerPartition` for Direct Kafka approach. In Spark 1.5, we have introduced a feature called backpressure that eliminate the need to set this rate limit, as Spark Streaming automatically figures out the rate limits and dynamically adjusts them if the processing conditions change. This backpressure can be enabled by setting the [configuration parameter](https://spark.apache.org/docs/3.3.2/configuration.html#spark-streaming) `spark.streaming.backpressure.enabled` to true.

- 配置最大接受速率：如果集群资源不能满足应用程序处理数据和接收数据同样快的要求，接收器可以以 `records / sec` 形式设置最大的比例限制，来限制接收器的速率。对于接收器接收数据使用 `spark.streaming.receiver.maxRate`, 对于直连 Kafka 的方式使用 `spark.streaming.kafka.maxRatePerPartition`. 在 Spark 1.5 中，我们引入了背压机制，消除速率限制的需求。因为 Spark Streaming 会自动计算出速率限制，并在处理条件发生变化时动态调整它们。

#### Upgrading Application Code

> If a running Spark Streaming application needs to be upgraded with new application code, then there are two possible mechanisms.

如果正在运行的 Spark Streaming 应用程序需要添加新的代码，存在如下两种机制：

> The upgraded Spark Streaming application is started and run in parallel to the existing application. Once the new one (receiving the same data as the old one) has been warmed up and is ready for prime time, the old one be can be brought down. Note that this can be done for data sources that support sending the data to two destinations (i.e., the earlier and upgraded applications).

- 启动更新后的应用程序，并和已存在的程序并行运行。一旦更新后的程序准备好成为主要程序，旧的可以被关掉（接收与旧的数据相同的数据）。注意：可以用于支持将数据发送到两个目的地（即较早和更新后的应用程序）的数据源。

> The existing application is shutdown gracefully (see [StreamingContext.stop(...)](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/streaming/StreamingContext.html) or [JavaStreamingContext.stop(...)](https://spark.apache.org/docs/3.3.2/api/java/index.html?org/apache/spark/streaming/api/java/JavaStreamingContext.html) for graceful shutdown options) which ensure data that has been received is completely processed before shutdown. Then the upgraded application can be started, which will start processing from the same point where the earlier application left off. Note that this can be done only with input sources that support source-side buffering (like Kafka) as data needs to be buffered while the previous application was down and the upgraded application is not yet up. And restarting from earlier checkpoint information of pre-upgrade code cannot be done. The checkpoint information essentially contains serialized Scala/Java/Python objects and trying to deserialize objects with new, modified classes may lead to errors. In this case, either start the upgraded app with a different checkpoint directory, or delete the previous checkpoint directory.

- 已存在的程序优雅地关闭，确保在关闭之前已接收的数据被完全处理。然后启动升级后的程序，从旧的应用程序停止的同一点开始处理。注意，只有在源端支持缓冲的输入源（如：Kafka）时才可以进行此操作，因为数据需要在更早的应用程序关闭并且升级后的应用程序尚未启动时，需要缓存数据。 无法从升级前代码的早期 checkpoint 信息重新启动. checkpoint 信息包含了序列化的 Scala/Java/Python 对象，并尝试使用新的应用程序反序列化对象，修改后的类可能会导致错误。在这个例子中，既不是使用不同的 checkpoint 目录来启动更新后的应用程序，也不是删除前面的 checkpoint 目录。

### Monitoring Applications

> Beyond Spark’s [monitoring capabilities](https://spark.apache.org/docs/3.3.2/monitoring.html), there are additional capabilities specific to Spark Streaming. When a StreamingContext is used, the [Spark web UI ](https://spark.apache.org/docs/3.3.2/monitoring.html#web-interfaces) shows an additional Streaming tab which shows statistics about running receivers (whether receivers are active, number of records received, receiver error, etc.) and completed batches (batch processing times, queueing delays, etc.). This can be used to monitor the progress of the streaming application.

除了 Spark 的监控能力，还有其他特定于 Spark Streaming 的能力。当使用 StreamingContext 时， Spark web UI 会展示另外的 Streaming tab 页，展示运行中的接收器（接收器是否活跃，接收了多少记录，接收器错误）和完成的批次（批次处理时间、队列延迟等）的统计信息。这些可以用来监控流应用程序的进度。 

> The following two metrics in web UI are particularly important:

在 web UI 中有两种度量非常重要：

- 处理时间：处理数据的每个批次的时间。

- 调度延迟：前一个批次完成处理，当前批次等待处理的时长。如果批次处理时间一直超过了批次间隔，和/或队列延迟保存增长，那么就表示系统的处理速度不能和生产批次一样快，也就是落后了。在那种情况下，考虑减少批次处理时间。

> Processing Time - The time to process each batch of data.

> Scheduling Delay - the time a batch waits in a queue for the processing of previous batches to finish.
If the batch processing time is consistently more than the batch interval and/or the queueing delay keeps increasing, then it indicates that the system is not able to process the batches as fast they are being generated and is falling behind. In that case, consider [reducing](https://spark.apache.org/docs/3.3.2/streaming-programming-guide.html#reducing-the-batch-processing-times) the batch processing time.

> The progress of a Spark Streaming program can also be monitored using the [StreamingListener](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/streaming/scheduler/StreamingListener.html) interface, which allows you to get receiver status and processing times. Note that this is a developer API and it is likely to be improved upon (i.e., more information reported) in the future.

Spark Streaming 程序进度也可以使用 StreamingListener 接口监控，这允许你获得接收器状态和处理时间。注意，这是一个开发人员 API 并且将来可能会改善（例如，更多的信息报告）。

## Performance Tuning

> Getting the best performance out of a Spark Streaming application on a cluster requires a bit of tuning. This section explains a number of the parameters and configurations that can be tuned to improve the performance of you application. At a high level, you need to consider two things:

获取 Spark Streaming 应用程序的最佳性能要求一些调优。这部分解释能用来调优应用程序性能的参数和配置。在最高层面考虑，你需要配置两种：

- 通过高效利用集群资源，减少数据的每个批次的处理时间。

- 设置正确的批次大小，这样数据的批次可以和接收数据一样快速的处理。（即数据处理与数据摄取保持速度一致）。

> Reducing the processing time of each batch of data by efficiently using cluster resources.

> Setting the right batch size such that the batches of data can be processed as fast as they are received (that is, data processing keeps up with the data ingestion).

### Reducing the Batch Processing Times

> There are a number of optimizations that can be done in Spark to minimize the processing time of each batch. These have been discussed in detail in the [Tuning Guide](https://spark.apache.org/docs/3.3.2/tuning.html). This section highlights some of the most important ones.

在 Spark 中有许多优化可以最小化每个批次的处理时间。这些已在 Tuning Guide 详细讨论。本节重点介绍其中最重要的一些。

### Setting the Right Batch Interval

> Receiving data over the network (like Kafka, socket, etc.) requires the data to be deserialized and stored in Spark. If the data receiving becomes a bottleneck in the system, then consider parallelizing the data receiving. Note that each input DStream creates a single receiver (running on a worker machine) that receives a single stream of data. Receiving multiple data streams can therefore be achieved by creating multiple input DStreams and configuring them to receive different partitions of the data stream from the source(s). For example, a single Kafka input DStream receiving two topics of data can be split into two Kafka input streams, each receiving only one topic. This would run two receivers, allowing data to be received in parallel, thus increasing overall throughput. These multiple DStreams can be unioned together to create a single DStream. Then the transformations that were being applied on a single input DStream can be applied on the unified stream. This is done as follows.

通过网络接收数据（如Kafka,socket 等）需要反序列化数据，然后存储在 Spark 中。如果数据接收成为系统的瓶颈，那么考虑并行化数据接收。

注意每个输入 DStream 创建接收单个数据流的单个接收器（在 worker 机器上运行）。因此，可以通过创建多个输入 DStreams 来实现接收多个数据流，并配置它们以从源中接收数据流的不同分区。

例如，接收两个数据主题的单个 Kafka 输入 DStream 可以分为两个 Kafka 输入 streams, 每个只接收一个主题。这将运行两个接收器，允许并行接收数据，从而提高总体吞吐量。

这些多个 DStreams 可以联合起来创建一个单个 DStream. 然后 应用于单个输入 DStream 的转换可以应用于统一流。如下这样做。


```Scala
val numStreams = 5
val kafkaStreams = (1 to numStreams).map { i => KafkaUtils.createStream(...) }
val unifiedStream = streamingContext.union(kafkaStreams)
unifiedStream.print()
```

```Java
int numStreams = 5;
List<JavaPairDStream<String, String>> kafkaStreams = new ArrayList<>(numStreams);
for (int i = 0; i < numStreams; i++) {
  kafkaStreams.add(KafkaUtils.createStream(...));
}
JavaPairDStream<String, String> unifiedStream = streamingContext.union(kafkaStreams.get(0), kafkaStreams.subList(1, kafkaStreams.size()));
unifiedStream.print();
```

```Python
numStreams = 5
kafkaStreams = [KafkaUtils.createStream(...) for _ in range (numStreams)]
unifiedStream = streamingContext.union(*kafkaStreams)
unifiedStream.pprint()
```

> Another parameter that should be considered is the receiver’s block interval, which is determined by the [configuration parameter](https://spark.apache.org/docs/3.3.2/configuration.html#spark-streaming) `spark.streaming.blockInterval`. For most receivers, the received data is coalesced together into blocks of data before storing inside Spark’s memory. The number of blocks in each batch determines the number of tasks that will be used to process the received data in a map-like transformation. The number of tasks per receiver per batch will be approximately (batch interval / block interval). For example, block interval of 200 ms will create 10 tasks per 2 second batches. If the number of tasks is too low (that is, less than the number of cores per machine), then it will be inefficient as all available cores will not be used to process the data. To increase the number of tasks for a given batch interval, reduce the block interval. However, the recommended minimum value of block interval is about 50 ms, below which the task launching overheads may be a problem.

应考虑的另一个参数是接收器的块间隔，这由 `spark.streaming.blockInterval` 决定。对于大多数接收器，存储在 Spark 内存之前，接收到的数据被合并到一个数据块中。

在类似 map 的转换中，每个批次中的块数决定了用于处理接收数据的任务数。 

每批接收器的任务数量将大约是（批间隔/块间隔）。例如，每 2 秒批次，200 ms的块间隔创建 10 个任务。

如果任务数量太少（即少于每个机器的内核数量），那么它将没那么有效了，因为所有可用的内核都不会被使用处理数据。要增加给定批间隔的任务数量，请减少块间隔。但是，推荐的块间隔最小值约为 50ms，低于这个值，任务启动开销可能会出现问题。

> An alternative to receiving data with multiple input streams / receivers is to explicitly repartition the input data stream (using `inputStream.repartition(<number of partitions>)`). This distributes the received batches of data across the specified number of machines in the cluster before further processing.

使用多个输入流/接收器 接收数据的替代方法是重分区输入数据流（使用 `inputStream.repartition(<number of partitions>`)）。这会在进一步处理之前，将 接收的数据批次分发到集群中指定数量的计算机。

> For direct stream, please refer to [Spark Streaming + Kafka Integration Guide](https://spark.apache.org/docs/3.3.2/streaming-kafka-0-10-integration.html)

对于直连流，请参考 `Spark Streaming + Kafka Integration Guide`

#### Level of Parallelism in Data Processing

> Cluster resources can be under-utilized if the number of parallel tasks used in any stage of the computation is not high enough. For example, for distributed reduce operations like `reduceByKey` and `reduceByKeyAndWindow`, the default number of parallel tasks is controlled by the `spark.default.parallelism` [configuration property](https://spark.apache.org/docs/3.3.2/configuration.html#spark-properties). You can pass the level of parallelism as an argument (see [PairDStreamFunctions](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/streaming/dstream/PairDStreamFunctions.html) documentation), or set the `spark.default.parallelism` configuration property to change the default.

如果在任何计算阶段中使用的并行任务的数量不够大，则集群资源可能未得到充分利用。例如，对于分布式 reduce 操作，如 `reduceByKey` 和 `reduceByKeyAndWindow`, 默认并行任务的数量由 `spark.default.parallelism` 配置属性控制。可以将并行度级别作为参数，或设置 `spark.default.parallelism` 更改默认值。

#### Data Serialization

> The overheads of data serialization can be reduced by tuning the serialization formats. In the case of streaming, there are two types of data that are being serialized.

可以通过调优序列化格式来减少数据序列化的开销。在流模式下，有两种类型的数据被序列化：

> Input data: By default, the input data received through Receivers is stored in the executors’ memory with [StorageLevel.MEMORY_AND_DISK_SER_2](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/storage/StorageLevel$.html). That is, the data is serialized into bytes to reduce GC overheads, and replicated for tolerating executor failures. Also, the data is kept first in memory, and spilled over to disk only if the memory is insufficient to hold all of the input data necessary for the streaming computation. This serialization obviously has overheads – the receiver must deserialize the received data and re-serialize it using Spark’s serialization format.

- 输入数据：默认情况下，通过接收器接收的输入数据使用 `StorageLevel。MEMORY_AND_DISK_SER_2` 存储在 executors 的内存中。也就是说，将数据序列化为字节，以减少 GC 开销，并复制以容忍 executor 故障。

此外，数据首先保存在内存中，并且只有在内存不足以容纳流计算所需的所有输入数据时，才会溢出到磁盘。这个序列化显然具有开销 - 接收器必须反序列化接收的数据，并使用 Spark 的序列化格式重新序列化它。

> Persisted RDDs generated by Streaming Operations: RDDs generated by streaming computations may be persisted in memory. For example, window operations persist data in memory as they would be processed multiple times. However, unlike the Spark Core default of [StorageLevel.MEMORY_ONLY](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/storage/StorageLevel$.html), persisted RDDs generated by streaming computations are persisted with [StorageLevel.MEMORY_ONLY_SER](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/storage/StorageLevel.html$) (i.e. serialized) by default to minimize GC overheads.

- 通过流式操作生成的持久化的 RDDs: 通过流式计算生成的 RDD 可能会持久存储在内存中。例如，窗口操作会将数据保留在内存中，因为它们将被处理多次。但是，与 Spark Core 默认的 `StorageLevel.MEMORY_ONLY` 情况不同，通过流式计算生成的持久化 RDD 将使用 `StorageLevel.MEMORY_ONLY_SER`（例如序列化）持久化，以最小化 GC 开销。

> In both cases, using Kryo serialization can reduce both CPU and memory overheads. See the [Spark Tuning Guide](https://spark.apache.org/docs/3.3.2/tuning.html#data-serialization) for more details. For Kryo, consider registering custom classes, and disabling object reference tracking (see Kryo-related configurations in the [Configuration Guide](https://spark.apache.org/docs/3.3.2/configuration.html#compression-and-serialization)).

在这两种情况下，使用 Kryo 序列化可以同时减少 CPU 和内存开销。

对于 Kryo，请考虑注册自定义的类，并禁用对象引用跟踪。

> In specific cases where the amount of data that needs to be retained for the streaming application is not large, it may be feasible to persist data (both types) as deserialized objects without incurring excessive GC overheads. For example, if you are using batch intervals of a few seconds and no window operations, then you can try disabling serialization in persisted data by explicitly setting the storage level accordingly. This would reduce the CPU overheads due to serialization, potentially improving performance without too much GC overheads.

在流应用程序需要保留的数据量不大的特定情况下，可以将数据 (both types) 作为反序列化对象持久化，而不会导致过多的 GC 开销。

例如，如果你使用几秒钟的批次间隔并且没有窗口操作，那么可以通过设置存储级别，来尝试禁用 持久化数据中的序列化。这将减少由于序列化造成的 CPU 开销，潜在地提高性能，而不需要太多的 GC 开销。

#### Task Launching Overheads

> If the number of tasks launched per second is high (say, 50 or more per second), then the overhead of sending out tasks to the executors may be significant and will make it hard to achieve sub-second latencies. The overhead can be reduced by the following changes:

如果每秒启动的任务数量很高（比如每秒50个或更多），那么向 executors 发送任务的开销可能是重要的，并且将难以实现亚秒级延迟。可以通过以下更改减少开销:

> Execution mode: Running Spark in Standalone mode or coarse-grained Mesos mode leads to better task launch times than the fine-grained Mesos mode. Please refer to the [Running on Mesos guide](https://spark.apache.org/docs/3.3.2/running-on-mesos.html) for more details.

- 执行模式：以 Standalone 模式或粗粒度 Mesos 模式运行 Spark 比细粒度 Mesos 模式有更好的任务启动次数。

> These changes may reduce batch processing time by 100s of milliseconds, thus allowing sub-second batch size to be viable.

这些更改可能会将批处理时间缩短 100 毫秒，从而允许亚秒级批次大小是可行的。

#### Setting the Right Batch Interval

> For a Spark Streaming application running on a cluster to be stable, the system should be able to process data as fast as it is being received. In other words, batches of data should be processed as fast as they are being generated. Whether this is true for an application can be found by [monitoring](https://spark.apache.org/docs/3.3.2/streaming-programming-guide.html#monitoring-applications) the processing times in the streaming web UI, where the batch processing time should be less than the batch interval.

对于在集群上稳定地运行的 Spark Streaming 应用程序，该系统的处理数据速度应该能够和接收数据一样。换句话说，批次数据的处理应该就像生成它们一样快。

对于是否适用于一个应用程序，可以通过监控流 web UI 中的处理时间来判断，批次处理时间应小于批间隔。

> Depending on the nature of the streaming computation, the batch interval used may have significant impact on the data rates that can be sustained by the application on a fixed set of cluster resources. For example, let us consider the earlier WordCountNetwork example. For a particular data rate, the system may be able to keep up with reporting word counts every 2 seconds (i.e., batch interval of 2 seconds), but not every 500 milliseconds. So the batch interval needs to be set such that the expected data rate in production can be sustained.

取决于流式计算的性质，使用的批次间隔能对数据速率有重大的影响，这个数据速率由一组集群资源上的应用程序来维持。

例如，对于 WordCountNetwork 示例。对于特定的数据速率，系统可能能以每 2 秒报告单词的计数（例如2秒的批次间隔），但不能是每 500 毫秒。因此，需要设置批次间隔，以使预期的数据速率在生产中可以维持。

> A good approach to figure out the right batch size for your application is to test it with a conservative batch interval (say, 5-10 seconds) and a low data rate. To verify whether the system is able to keep up with the data rate, you can check the value of the end-to-end delay experienced by each processed batch (either look for “Total delay” in Spark driver log4j logs, or use the [StreamingListener](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/streaming/scheduler/StreamingListener.html) interface). If the delay is maintained to be comparable to the batch size, then system is stable. Otherwise, if the delay is continuously increasing, it means that the system is unable to keep up and it therefore unstable. Once you have an idea of a stable configuration, you can try increasing the data rate and/or reducing the batch size. Note that a momentary increase in the delay due to temporary data rate increases may be fine as long as the delay reduces back to a low value (i.e., less than batch size).

为使你的应用程序找出正确的批次大小的一个好方法是，使用保守的批次间隔（例如 5-10 秒）和低数据速率进行测试。

为了验证系统能否适应数据速率，你可以检查每个处理后的批次所经历的端到端延迟的值（在 Spark driver log4j 日志中查找 “Total delay”，或使用 StreamingListener 接口）。

如果延迟保持与批次大小相当，那么系统是稳定的。除此以外，如果延迟不断增加，则意味着系统无法跟上，因此不稳定。

一旦你有一个稳定的配置的想法，你可以尝试增加数据速率和/或 减少批次大小。

请注意，只要延迟降低回一个低值（例如，小于批次大小），由临时数据速率增加引起的延迟的短暂增加可能是好的，

### Memory Tuning

> Tuning the memory usage and GC behavior of Spark applications has been discussed in great detail in the [Tuning Guide](https://spark.apache.org/docs/3.3.2/tuning.html#memory-tuning). It is strongly recommended that you read that. In this section, we discuss a few tuning parameters specifically in the context of Spark Streaming applications.

调优 Spark 应用程序的内存使用和 GC 行为已经在 Tuning Guide 中有很多的讨论。我们强烈建议你阅读一下。在本节中，我们将在 Spark Streaming 应用程序的上下文中讨论一些调优参数。

> The amount of cluster memory required by a Spark Streaming application depends heavily on the type of transformations used. For example, if you want to use a window operation on the last 10 minutes of data, then your cluster should have sufficient memory to hold 10 minutes worth of data in memory. Or if you want to use `updateStateByKey` with a large number of keys, then the necessary memory will be high. On the contrary, if you want to do a simple map-filter-store operation, then the necessary memory will be low.

Spark Streaming 应用程序所需的集群内存量在很大程度上取决于所使用的转换操作类型。例如，如果要在最近 10 分钟的数据中使用窗口操作，那么你的集群应该有足够的内存，以在内存中容纳 10 分钟的数据。或者如果要使用 `updateStateByKey` 处理大量键 ，那么必要的内存将会很高。相反，如果你想做一个简单的 map-filter-store 操作，那么所需的内存就会很低。

> In general, since the data received through receivers is stored with `StorageLevel.MEMORY_AND_DISK_SER_2`, the data that does not fit in memory will spill over to the disk. This may reduce the performance of the streaming application, and hence it is advised to provide sufficient memory as required by your streaming application. Its best to try and see the memory usage on a small scale and estimate accordingly.

一般来说，由于通过接收器接收的数据使用 `StorageLevel.MEMORY_AND_DISK_SER_2` 存储，所以不存入内存的数据将会溢出到磁盘上。这可能会降低流应用程序的性能，因此建议提供足够的流应用程序所需的内存。最好在小规模集群上仔细查看内存使用量并相应地进行估算。

> Another aspect of memory tuning is garbage collection. For a streaming application that requires low latency, it is undesirable to have large pauses caused by JVM Garbage Collection.

内存调优的另一个方面是垃圾回收。对于需要低延迟的流应用程序，由 JVM Garbage Collection 引起的大量暂停是不希望的。

> There are a few parameters that can help you tune the memory usage and GC overheads:

有几个参数可以调优内存使用量和 GC 开销:

> Persistence Level of DStreams: As mentioned earlier in the [Data Serialization](https://spark.apache.org/docs/3.3.2/streaming-programming-guide.html#data-serialization) section, the input data and RDDs are by default persisted as serialized bytes. This reduces both the memory usage and GC overheads, compared to deserialized persistence. Enabling Kryo serialization further reduces serialized sizes and memory usage. Further reduction in memory usage can be achieved with compression (see the Spark configuration `spark.rdd.compress`), at the cost of CPU time.

- DStreams 的持久化级别：默认情况下，输入数据和 RDD 持久化为序列化字节。与反序列化持久化相比，这减少了内存使用量和 GC 开销。启用 Kryo 序列化进一步减少了序列化大小和内存使用。可以通过压缩来实现内存使用的进一步减少，代价是 CPU 时间。

> Clearing old data: By default, all input data and persisted RDDs generated by DStream transformations are automatically cleared. Spark Streaming decides when to clear the data based on the transformations that are used. For example, if you are using a window operation of 10 minutes, then Spark Streaming will keep around the last 10 minutes of data, and actively throw away older data. Data can be retained for a longer duration (e.g. interactively querying older data) by setting `streamingContext.remember`.

- 清除旧数据：默认情况下，所有输入数据和 DStream 转换生成的持久化 RDDs 将被自动清除。根据所使用的转换操作, Spark Streaming 决定何时清除数据。例如，如果你使用 10 分钟的窗口操作，则 Spark Streaming 将保留最近 10 分钟的数据，并主动丢弃旧数据。数据可以通过设置 `streamingContext.remember` 保持更长的持续时间（例如交互式查询旧数据）。

> CMS Garbage Collector: Use of the concurrent mark-and-sweep GC is strongly recommended for keeping GC-related pauses consistently low. Even though concurrent GC is known to reduce the overall processing throughput of the system, its use is still recommended to achieve more consistent batch processing times. Make sure you set the CMS GC on both the driver (using `--driver-java-options` in `spark-submit`) and the executors (using [Spark configuration](https://spark.apache.org/docs/3.3.2/configuration.html#runtime-environment) `spark.executor.extraJavaOptions`).

- CMS垃圾收集器：强烈建议使用并发的标记清除 GC，以保持 GC 相关的暂停始终很低。即使并发的 GC 会减少系统的整体吞吐量，仍然建议使用，以实现更多一致的批处理时间。确保在驱动（在 `spark-submit` 中使用 `--driver-java-options`）和 executors（使用 Spark 配置`spark.executor.extraJavaOptions`）中设置 CMS GC.

> Other tips: To further reduce GC overheads, here are some more tips to try.

> Persist RDDs using the `OFF_HEAP` storage level. See more detail in the [Spark Programming Guide](https://spark.apache.org/docs/3.3.2/rdd-programming-guide.html#rdd-persistence).

> Use more executors with smaller heap sizes. This will reduce the GC pressure within each JVM heap.

- 其他提示：为了进一步降低 GC 开销，以下是一些更多的提示。

    - 使用 `OFF_HEAP` 存储级别来持久化 RDDs
    
    - 使用更多的 executors，每个 executors 占用更小的堆大小。这将降低每个 JVM heap 内的 GC 压力。

#### Important points to remember:

> A DStream is associated with a single receiver. For attaining read parallelism multiple receivers i.e. multiple DStreams need to be created. A receiver is run within an executor. It occupies one core. Ensure that there are enough cores for processing after receiver slots are booked i.e. `spark.cores.max` should take the receiver slots into account. The receivers are allocated to executors in a round robin fashion.

- 一个 DStream 对应一个接收器。为了维持读取并行度，设置多个接收器，则也需要创建多个 DStream.  一个接收器运行在一个 executor 中，占用一个核。 在接收器 slots 确定后，确保有足够的核处理数据。设置 `spark.cores.max ` 的时候需要考虑接收器 slots 的数量。 接收器以轮循的方式将 slots 分配给 executors.

> When data is received from a stream source, receiver creates blocks of data. A new block of data is generated every `blockInterval` milliseconds. N blocks of data are created during the `batchInterval` where `N = batchInterval/blockInterval`. These blocks are distributed by the BlockManager of the current executor to the block managers of other executors. After that, the Network Input Tracker running on the driver is informed about the block locations for further processing.

- 当从一个数据源接收数据时，接收器会创建数据块。每 `blockInterval` 毫秒就创建一个新的数据块。在 `batchInterval` 期间，创建 N 个数据块，其中 `N = batchInterval/blockInterval`. 这些数据块由当前 executor 的 BlockManager 分发给其他 executor 的块管理器。然后 通知运行在驱动上的 Network Input Tracker 块的位置。

> An RDD is created on the driver for the blocks created during the batchInterval. The blocks generated during the batchInterval are partitions of the RDD. Each partition is a task in `spark.blockInterval == batchinterval` would mean that a single partition is created and probably it is processed locally.

- 在驱动上，为在 batchInterval 期间创建的块 创建一个 RDD. 在 batchInterval 期间生成的块是 RDD 的分区。当 `spark.blockInterval == batchinterval` 时，每个分区是一个任务，意味着创建单个分区，并且可能在本地进行处理。

> The map tasks on the blocks are processed in the executors (one that received the block, and another where the block was replicated) that has the blocks irrespective of block interval, unless non-local scheduling kicks in. Having bigger blockinterval means bigger blocks. A high value of `spark.locality.wait`increases the chance of processing a block on the local node. A balance needs to be found out between these two parameters to ensure that the bigger blocks are processed locally.

- 除非进行非本地调度，否则块上的 map 任务在 executors（一个接收块，另一个复制块）中进行处理，此 executors 具有块，而不考虑块间隔。具有更大的 blockinterval 意味着更大的块。 `spark.locality.wait` 的高值增加了在本地节点上处理块的机会。需要在这两个参数之间找到平衡，以确保在本地处理较大的块。

> Instead of relying on batchInterval and blockInterval, you can define the number of partitions by calling `inputDstream.repartition(n)`. This reshuffles the data in RDD randomly to create n number of partitions. Yes, for greater parallelism. Though comes at the cost of a shuffle. An RDD’s processing is scheduled by driver’s jobscheduler as a job. At a given point of time only one job is active. So, if one job is executing the other jobs are queued.

- 不是依赖于 batchInterval 和 blockInterval, 你可以通过调用 `inputDstream.repartition(n)` 来定义分区数。这样可以随机重新组合 RDD 中的数据，以创建 n 个分区。虽然会增加混洗的成本，但提高了并行度。对 RDD 的处理是由驱动的 jobscheduler 作为一项作业来调度的。在给定的时间点，只有一个作业是活跃的。因此，如果一个作业正在执行，则其他作业将排队。

> If you have two dstreams there will be two RDDs formed and there will be two jobs created which will be scheduled one after the another. To avoid this, you can union two dstreams. This will ensure that a single unionRDD is formed for the two RDDs of the dstreams. This unionRDD is then considered as a single job. However, the partitioning of the RDDs is not impacted.

- 如果有两个 dstream, 将会形成两个 RDD, 并创建两个作业，这两个作业一个接一个的调度。为了避免这种情况，你可以合并两个 dstream. 这将形成一个 unionRDD. 然后这个 unionRDD 被认为是一个单个作业。但 RDD 的分区不受影响。

> If the batch processing time is more than batchinterval then obviously the receiver’s memory will start filling up and will end up in throwing exceptions (most probably BlockNotFoundException). Currently, there is no way to pause the receiver. Using SparkConf configuration `spark.streaming.receiver.maxRate`, rate of receiver can be limited.

- 如果批处理时间超过 batchinterval, 那么显然将会开始填满接收器的内存，最终会抛出异常（最可能是 BlockNotFoundException）。目前没有办法暂停接收器。使用 SparkConf 的 `spark.streaming.receiver.maxRate` 配置，可以限制接收器的速率。

## Fault-tolerance Semantics

> In this section, we will discuss the behavior of Spark Streaming applications in the event of failures.

在这部分，我们将讨论故障下的 Spark Streaming 应用程序的行为。

### Background

> To understand the semantics provided by Spark Streaming, let us remember the basic fault-tolerance semantics of Spark’s RDDs.

要理解 Spark Streaming 提供的语义，先记住基本的 spark RDD 的容错语义

> An RDD is an immutable, deterministically re-computable, distributed dataset. Each RDD remembers the lineage of deterministic operations that were used on a fault-tolerant input dataset to create it.

1. 一个 RDD 是一个不可变的、确定被再次计算的、分布式的数据集。每个 RDD 都会记住它确定计算的血统，用于容错输入数据集，以创建它。

> If any partition of an RDD is lost due to a worker node failure, then that partition can be re-computed from the original fault-tolerant dataset using the lineage of operations.

2. 如果因 worker 节点故障，RDD 的任意分区丢失，那么可以利用血统，从原始的容错数据集中再次计算分区。

> Assuming that all of the RDD transformations are deterministic, the data in the final transformed RDD will always be the same irrespective of failures in the Spark cluster.

3. 假设所有的 RDD 转换操作都是确定性的，最后的转换 RDD 数据将总是相同的，而不管 Spark 集群中的故障。

> Spark operates on data in fault-tolerant file systems like HDFS or S3. Hence, all of the RDDs generated from the fault-tolerant data are also fault-tolerant. However, this is not the case for Spark Streaming as the data in most cases is received over the network (except when `fileStream` is used). To achieve the same fault-tolerance properties for all of the generated RDDs, the received data is replicated among multiple Spark executors in worker nodes in the cluster (default replication factor is 2). This leads to two kinds of data in the system that need to recovered in the event of failures:

Spark 在容错的文件系统中操作数据，就像 HDFS 或 S3, 所以从容错的数据中产生的 RDDs 也是容错的。但 Spark Streaming 不是这样，因为它通过网络接收的数据（除了使用`fileStream`外）。所以为了实现同样的容错属性，接收的数据在 worker 节点的多个 executors 间复制（默认副本因子是2）。 这导致在故障后，需要恢复两种数据：

- 接收并复制的数据：数据从单个 worker 节点故障中存活，因为数据还在其他节点存在。

- 接收了数据，进行了缓存：由于没有复制，所以恢复数据的方式只能再次从源读取。

> Data received and replicated - This data survives failure of a single worker node as a copy of it exists on one of the other nodes.
> Data received but buffered for replication - Since this is not replicated, the only way to recover this data is to get it again from the source.

> Furthermore, there are two kinds of failures that we should be concerned about:

存在两种需要考虑的故障：

- worker 节点故障：运行 executors 的任意 worker 节点都可以故障，这些节点上的所有内存中的数据都将丢失。如果任意的接收器运行在故障的节点上，那么它们缓存的数据将丢失。 

- 驱动节点故障：如果运行 Spark Streaming 应用程序的驱动节点故障，那么将丢失 SparkContext, 所有的 executors 及其中的数据也会丢失。

> Failure of a Worker Node - Any of the worker nodes running executors can fail, and all in-memory data on those nodes will be lost. If any receivers were running on failed nodes, then their buffered data will be lost.

> Failure of the Driver Node - If the driver node running the Spark Streaming application fails, then obviously the SparkContext is lost, and all executors with their in-memory data are lost.

> With this basic knowledge, let us understand the fault-tolerance semantics of Spark Streaming.

有了这些基础只是，让我们理解 Spark Streaming 的容错语义。

### Definitions

> The semantics of streaming systems are often captured in terms of how many times each record can be processed by the system. There are three types of guarantees that a system can provide under all possible operating conditions (despite failures, etc.)

流系统的语义通常是通过系统处理每个记录的次数获取的。三种类型的保证：

- At most once: 一条记录要么被处理一次，要么不被处理。

- At least once: 一条记录被处理一次或多次，这比 at-most once 有更强的语义，它确保了数据不会丢失，但会有冗余。

- Exactly once: 一条记录正好被处理一次。不会数据丢失，也不会被处理多次。这是最强语义。

> At most once: Each record will be either processed once or not processed at all.

> At least once: Each record will be processed one or more times. This is stronger than at-most once as it ensure that no data will be lost. But there may be duplicates.

> Exactly once: Each record will be processed exactly once - no data will be lost and no data will be processed multiple times. This is obviously the strongest guarantee of the three.

### Basic Semantics

> In any stream processing system, broadly speaking, there are three steps in processing the data.

在任意流处理系统中，处理数据有三步骤：

1. 接收数据：使用接收器或其他从源接收数据
2. 转换数据：使用 DStream 和 RDD 转换转换接收的数据
3. 推送数据：最终转换的数据被推送到外部系统，比如文件系统、数据库、看板等。

> Receiving the data: The data is received from sources using Receivers or otherwise.
> Transforming the data: The received data is transformed using DStream and RDD transformations.
> Pushing out the data: The final transformed data is pushed out to external systems like file systems, databases, dashboards, etc.

> If a streaming application has to achieve end-to-end exactly-once guarantees, then each step has to provide an exactly-once guarantee. That is, each record must be received exactly once, transformed exactly once, and pushed to downstream systems exactly once. Let’s understand the semantics of these steps in the context of Spark Streaming.

如果一个流应用程序要实现端到端的 exactly-once 语义保证，那么每一步都要实现 exactly-once 保证。也就是说，每条记录只接收一次，转换一次，推到下流系统一次。

下面让我们理解 Spark Streaming 上下文中这些步骤的语义：

> Receiving the data: Different input sources provide different guarantees. This is discussed in detail in the next subsection.

1. 接收数据：不同输入源提供不同的保证。

> Transforming the data: All data that has been received will be processed exactly once, thanks to the guarantees that RDDs provide. Even if there are failures, as long as the received input data is accessible, the final transformed RDDs will always have the same contents.

2. 转换数据：由于 RDDs 提供的保证，接收的所有数据只被精确处理一次。即使有故障，只要接收的输入数据可访问，最终转换的 RDDs 总会有相同的内容。

> Pushing out the data: Output operations by default ensure at-least once semantics because it depends on the type of output operation (idempotent, or not) and the semantics of the downstream system (supports transactions or not). But users can implement their own transaction mechanisms to achieve exactly-once semantics. This is discussed in more details later in the section.

3. 推送数据：默认情况下，输出操作提供 at-least once 语义，因为它取决于输出操作的类型（是否幂等）和下流系统的语义（支持或不支持事务）。但用户可以自己实现事务机制，来实现 exactly-once 语义

### Semantics of Received Data

> Different input sources provide different guarantees, ranging from at-least once to exactly once. Read for more details.

不同的输入源提供了不同的保证，范围从 at-least once 到 exactly once.

### With Files

> If all of the input data is already present in a fault-tolerant file system like HDFS, Spark Streaming can always recover from any failure and process all of the data. This gives exactly-once semantics, meaning all of the data will be processed exactly once no matter what fails.

如果所有的输入数据存在于容错的文件系统，就像 HDFS, Spark Streaming 总是可以从故障中恢复、处理数据。这就提供了 exactly-once 语义，就是所有的数据仅被精确处理一次，不管失败。

### With Receiver-based Sources

> For input sources based on receivers, the fault-tolerance semantics depend on both the failure scenario and the type of receiver. As we discussed [earlier](https://spark.apache.org/docs/3.3.2/streaming-programming-guide.html#receiver-reliability), there are two types of receivers:

对于基于接收器的输入源，容错语义依赖于故障场景和接收器类型。有两种接收器：

> Reliable Receiver - These receivers acknowledge reliable sources only after ensuring that the received data has been replicated. If such a receiver fails, the source will not receive acknowledgment for the buffered (unreplicated) data. Therefore, if the receiver is restarted, the source will resend the data, and no data will be lost due to the failure.

- 可靠接收器：这些可靠接收器仅在确保接收的数据被复制后，向可靠的源发出确认。如果那种接收器故障了，源将不会接收到缓存（未复制的）数据的确认信息。因此，如果重启接收器，源将重发数据，不会有数据会因故障丢失。

> Unreliable Receiver - Such receivers do not send acknowledgment and therefore can lose data when they fail due to worker or driver failures.

- 不可靠接收器：这种接收器不会发送确认信息，因此由于 worker 或 driver 节点故障导致的失败时，会丢失数据。

> Depending on what type of receivers are used we achieve the following semantics. If a worker node fails, then there is no data loss with reliable receivers. With unreliable receivers, data received but not replicated can get lost. If the driver node fails, then besides these losses, all of the past data that was received and replicated in memory will be lost. This will affect the results of the stateful transformations.

取决于使用何种类型的接收器，我们可以实现下列语义。如果使用可靠接收器，当 worker 节点故障，不会丢失数据。如果使用不可靠接收器，数据被接收，但不被复制，那么会丢失数据。

当驱动节点故障，除了这些数据丢失，所有接收且在内存中复制的旧数据也将丢失。这会影响状态转换的结果。

> To avoid this loss of past received data, Spark 1.2 introduced write ahead logs which save the received data to fault-tolerant storage. With the [write-ahead logs enabled](https://spark.apache.org/docs/3.3.2/streaming-programming-guide.html#deploying-applications) and reliable receivers, there is zero data loss. In terms of semantics, it provides an at-least once guarantee.

为了避免接收的旧数据的丢失，Spark 1.2 引入了预写日志，来存储接收到的数据到容错存储介质。

启动预写日志和使用可靠接收器，就可以实现零数据丢失，也就是 at-least once 语义。

> The following table summarizes the semantics under failures:

下面的表总结了故障下的语义：

Deployment Scenario | Worker Failure  | Driver Failure
---|:---|:---
Spark 1.1 or earlier, OR Spark 1.2 or later without write-ahead logs | Buffered data lost with unreliable receivers<br/> Zero data loss with reliable receivers<br/> At-least once semantics | Buffered data lost with unreliable receivers<br/> Past data lost with all receivers<br/> Undefined semantics
Spark 1.2 or later with write-ahead logs | Zero data loss with reliable receivers<br/> At-least once semantics | Zero data loss with reliable receivers and files At-least once semantics

### With Kafka Direct API

> In Spark 1.3, we have introduced a new Kafka Direct API, which can ensure that all the Kafka data is received by Spark Streaming exactly once. Along with this, if you implement exactly-once output operation, you can achieve end-to-end exactly-once guarantees. This approach is further discussed in the [Kafka Integration Guide](https://spark.apache.org/docs/3.3.2/streaming-kafka-0-10-integration.html).

在 Spark 1.3 中, 引入了 Kafka Direct API，可以确保所有 Spark Streaming 接收的 kafka 数据只被处理一次，如果你实现了 exactly-once 输出操作，那么就实现了端到端的 exactly-once 保证。

### Semantics of output operations

> Output operations (like `foreachRDD`) have at-least once semantics, that is, the transformed data may get written to an external entity more than once in the event of a worker failure. While this is acceptable for saving to file systems using the `saveAs***Files` operations (as the file will simply get overwritten with the same data), additional effort may be necessary to achieve exactly-once semantics. There are two approaches.

输出操作（如`foreachRDD`）有 at-least once 语义。也就是说，在 worker 故障中，转换的数据可能写入外部实体超过一次。

虽然这对于使用 `saveAs...Files` 保存到文件系统是可以接受（因为文件只会被相同的数据覆盖），但是可能需要额外的努力来实现精确的一次语义。有两种方法。

> Idempotent updates: Multiple attempts always write the same data. For example, `saveAs***Files` always writes the same data to the generated files.

- 幂等更新：多次尝试总是写入相同的数据。例如，`saveAs...Files` 总是将相同的数据写入生成的文件。

> Transactional updates: All updates are made transactionally so that updates are made exactly once atomically. One way to do this would be the following.

- 事务更新：所有更新都是事务性的，以便更新完全按原子进行。这样做的一个方法如下：

    - 使用批次时间（在`foreachRDD`中可用）和 RDD 的分区索引来创建一个标识符。这个标识符唯一地标识流应用程序的一团数据。

    - 使用具有标识符的这团数据事务地更新外部系统（原子地exactly once）。如果并没有提交标识符，则以原子地方式提交分区数据和标识符。如果标识符已提交，则跳过更新。

> Use the batch time (available in `foreachRDD`) and the partition index of the RDD to create an identifier. This identifier uniquely identifies a blob data in the streaming application.

> Update external system with this blob transactionally (that is, exactly once, atomically) using the identifier. That is, if the identifier is not already committed, commit the partition data and the identifier atomically. Else, if this was already committed, skip the update.

```scala
dstream.foreachRDD { (rdd, time) =>
  rdd.foreachPartition { partitionIterator =>
    val partitionId = TaskContext.get.partitionId()
    val uniqueId = generateUniqueId(time.milliseconds, partitionId)
    // use this uniqueId to transactionally commit the data in partitionIterator
  }
}
```

## Where to Go from Here

- Additional guides

    - [Kafka Integration Guide](https://spark.apache.org/docs/3.3.2/streaming-kafka-0-10-integration.html)
    - [Kinesis Integration Guide](https://spark.apache.org/docs/3.3.2/streaming-kinesis-integration.html)
    - [Custom Receiver Guide](https://spark.apache.org/docs/3.3.2/streaming-custom-receivers.html)

- Third-party DStream data sources can be found in [Third Party Projects](https://spark.apache.org/third-party-projects.html)

- API documentation

    - Scala docs
        + [StreamingContext](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/streaming/StreamingContext.html) and [DStream](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/streaming/dstream/DStream.html)
        + [KafkaUtils](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/streaming/kafka/KafkaUtils$.html), [KinesisUtils](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/streaming/kinesis/KinesisInputDStream.html),
    - Java docs
        + [JavaStreamingContext](https://spark.apache.org/docs/3.3.2/api/java/index.html?org/apache/spark/streaming/api/java/JavaStreamingContext.html), [JavaDStream](https://spark.apache.org/docs/3.3.2/api/java/index.html?org/apache/spark/streaming/api/java/JavaDStream.html) and [JavaPairDStream](https://spark.apache.org/docs/3.3.2/api/java/index.html?org/apache/spark/streaming/api/java/JavaPairDStream.html)
        + [KafkaUtils](https://spark.apache.org/docs/3.3.2/api/java/index.html?org/apache/spark/streaming/kafka/KafkaUtils.html), [KinesisUtils](https://spark.apache.org/docs/3.3.2/api/java/index.html?org/apache/spark/streaming/kinesis/KinesisInputDStream.html)
    - Python docs
        + [StreamingContext](https://spark.apache.org/docs/3.3.2/api/python/reference/api/pyspark.streaming.StreamingContext.html#pyspark.streaming.StreamingContext) and [DStream](https://spark.apache.org/docs/3.3.2/api/python/reference/api/pyspark.streaming.DStream.html#pyspark.streaming.DStream)

- More examples in [Scala](https://github.com/apache/spark/tree/master/examples/src/main/scala/org/apache/spark/examples/streaming) and [Java](https://github.com/apache/spark/tree/master/examples/src/main/java/org/apache/spark/examples/streaming) and [Python](https://github.com/apache/spark/tree/master/examples/src/main/python/streaming)

- [Paper](http://www.eecs.berkeley.edu/Pubs/TechRpts/2012/EECS-2012-259.pdf) and [video](http://youtu.be/g171ndOHgJ0) describing Spark Streaming.