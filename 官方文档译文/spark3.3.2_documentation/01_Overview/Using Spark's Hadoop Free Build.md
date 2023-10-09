# Using Spark's "Hadoop Free" Build

[TOC]

> Spark uses Hadoop client libraries for HDFS and YARN. Starting in version Spark 1.4, the project packages “Hadoop free” builds that lets you more easily connect a single Spark binary to any Hadoop version. To use these builds, you need to modify `SPARK_DIST_CLASSPATH` to include Hadoop’s package jars. The most convenient place to do this is by adding an entry in `conf/spark-env.sh`.

为使用 HDFS 和 YARN, Spark 需要使用 Hadoop 客户端库。从 1.4 版本开始，工程包 `“Hadoop free” builds` 让你更容易地将 Spark 二进制文件和任意 Hadoop 版本联系起来。

此时，需要将 `SPARK_DIST_CLASSPATH` 包含 Hadoop 包的 jars 文件。 最便利的地方就是在 `conf/spark-env.sh` 中添加。

> This page describes how to connect Spark to Hadoop for different types of distributions.

本页就描述了 Spark 如何连接不同的 Hadoop 版本。

## Apache Hadoop

> For Apache distributions, you can use Hadoop’s ‘classpath’ command. For instance:

对于 Apache 版本，你可以使用 Hadoop 的 `classpath` 命令。

```
### in conf/spark-env.sh

# If 'hadoop' binary is on your PATH
export SPARK_DIST_CLASSPATH=$(hadoop classpath)

# With explicit path to 'hadoop' binary
export SPARK_DIST_CLASSPATH=$(/path/to/hadoop/bin/hadoop classpath)

# Passing a Hadoop configuration directory
export SPARK_DIST_CLASSPATH=$(hadoop --config /path/to/configs classpath)
```

## Hadoop Free Build Setup for Spark on Kubernetes

> To run the Hadoop free build of Spark on Kubernetes, the executor image must have the appropriate version of Hadoop binaries and the correct `SPARK_DIST_CLASSPATH` value set. See the example below for the relevant changes needed in the executor Dockerfile:

在 Kubernetes 上运行 Spark 的 Hadoop 免构建版本，executor image 必须有合适的 Hadoop 二进制版本和正确的 `SPARK_DIST_CLASSPATH` 值集。

```
### Set environment variables in the executor dockerfile

ENV SPARK_HOME="/opt/spark"  
ENV HADOOP_HOME="/opt/hadoop"  
ENV PATH="$SPARK_HOME/bin:$HADOOP_HOME/bin:$PATH"  
...  

#Copy your target hadoop binaries to the executor hadoop home   

COPY /opt/hadoop3  $HADOOP_HOME  
...

#Copy and use the Spark provided entrypoint.sh. It sets your SPARK_DIST_CLASSPATH using the hadoop binary in $HADOOP_HOME and starts the executor. If you choose to customize the value of SPARK_DIST_CLASSPATH here, the value will be retained in entrypoint.sh

ENTRYPOINT [ "/opt/entrypoint.sh" ]
... 
``` 