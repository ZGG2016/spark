# Overview

[TOC]

> This document gives a short overview of how Spark runs on clusters, to make it easier to understand the components involved. Read through the [application submission guide](https://spark.apache.org/docs/3.3.2/submitting-applications.html) to learn about launching applications on a cluster.

## Components

> Spark applications run as independent sets of processes on a cluster, coordinated by the SparkContext object in your main program (called the driver program).

Spark 应用程序在集群上作为独立的进程组运行，在你的主程序（称之为驱动程序）中通过 SparkContext 来协调。

> Specifically, to run on a cluster, the SparkContext can connect to several types of cluster managers (either Spark’s own standalone cluster manager, Mesos, YARN or Kubernetes), which allocate resources across applications. Once connected, Spark acquires executors on nodes in the cluster, which are processes that run computations and store data for your application. Next, it sends your application code (defined by JAR or Python files passed to SparkContext) to the executors. Finally, SparkContext sends tasks to the executors to run.

首先，SparkContext 可以连接多种类型的集群管理器(standalone\Mesos\YARN\Kubernetes)，用来在应用程序间分配资源。

然后，连接成功后，Spark 获得节点上的 executors, 这些进程可以执行计算并且为你的应用程序存储数据。

然后，将你的应用程序代码(由 JAR 或 python 文件传递到 SparkContext)发送到 executors.

最后，SparkContext 发送任务给 executors 运行。

![cluster-overview.png](./images/cluster-overview.png)

> There are several useful things to note about this architecture:

有几个关于架构需要注意的地方 :

> Each application gets its own executor processes, which stay up for the duration of the whole application and run tasks in multiple threads. This has the benefit of isolating applications from each other, on both the scheduling side (each driver schedules its own tasks) and executor side (tasks from different applications run in different JVMs). However, it also means that data cannot be shared across different Spark applications (instances of SparkContext) without writing it to an external storage system.

1. 每个应用程序获取它自己的 executor 进程，在整个应用程序生命周期内都会占用这些进程，并且在多个线程中运行任务。

这样做的优点是，在调度方面和 executor 方面，把应用程序间互相隔离。 在调度方面，每个驱动调度它自己的任务，在 executor 方面，不同应用程序的任务运行在不同的 JVM 中。

然而，这也意味着，若是不把数据写到外部存储系统的话，数据就不能在不同的 Spark 应用程序（SparkContext 的实例）之间共享。

> Spark is agnostic to the underlying cluster manager. As long as it can acquire executor processes, and these communicate with each other, it is relatively easy to run it even on a cluster manager that also supports other applications (e.g. Mesos/YARN/Kubernetes).

2. Spark 是不知道底层的集群管理器是哪一种。只要它能够获得 executor 进程，并且它们之间可以通信，那么即便在一个也支持其它应用程序的集群管理器（例如，Mesos/YARN/Kubernetes）上来运行它也是相对简单的。

> The driver program must listen for and accept incoming connections from its executors throughout its lifetime (e.g., see [spark.driver.port in the network config section](https://spark.apache.org/docs/3.3.2/configuration.html#networking)). As such, the driver program must be network addressable from the worker nodes.

3. 驱动程序必须在自己生命周期内监听、接受来自它的 executors 的连接请求。同样的，驱动程序必须可以从 worker 节点上网络寻址。

> Because the driver schedules tasks on the cluster, it should be run close to the worker nodes, preferably on the same local area network. If you’d like to send requests to the cluster remotely, it’s better to open an RPC to the driver and have it submit operations from nearby than to run a driver far away from the worker nodes.

4. 因为驱动会调度集群上的任务，更好的方式应该是在相同的局域网中，靠近 worker 的节点上运行。如果远程向集群发送请求，不如打开一个到驱动的 RPC, 让它就近提交操作而不是从很远的 worker 节点上运行一个驱动。

## Cluster Manager Types

The system currently supports several cluster managers:

- [Standalone](https://spark.apache.org/docs/3.3.2/spark-standalone.html) – a simple cluster manager included with Spark that makes it easy to set up a cluster.
- [Apache Mesos](https://spark.apache.org/docs/3.3.2/running-on-mesos.html) – a general cluster manager that can also run Hadoop MapReduce and service applications. (Deprecated)
- [Hadoop YARN](https://spark.apache.org/docs/3.3.2/running-on-yarn.html) – the resource manager in Hadoop 2 and 3.
- [Kubernetes](https://spark.apache.org/docs/3.3.2/running-on-kubernetes.html) – an open-source system for automating deployment, scaling, and management of containerized applications.

## Submitting Applications

> Applications can be submitted to a cluster of any type using the `spark-submit` script. The [application submission guide](https://spark.apache.org/docs/3.3.2/submitting-applications.html) describes how to do this.

使用 spark-submit 脚本，可以将应用程序提交到任意类型的集群。

## Monitoring

> Each driver program has a web UI, typically on port 4040, that displays information about running tasks, executors, and storage usage. Simply go to `http://<driver-node>:4040` in a web browser to access this UI. The [monitoring guide](https://spark.apache.org/docs/3.3.2/monitoring.html) also describes other monitoring options.

每个驱动程序都有一个 web UI, 端口为4040，展示运行中的任务和 executors 的信息、存储的使用情况。

在浏览器访问 `http://<driver-node>:4040` , 查看这个 UI

## Job Scheduling

> Spark gives control over resource allocation both across applications (at the level of the cluster manager) and within applications (if multiple computations are happening on the same SparkContext). The [job scheduling overview](https://spark.apache.org/docs/3.3.2/job-scheduling.html) describes this in more detail.

spark 能控制应用程序间（集群管理器层面）和应用程序内（如果在相同 SparkContext 上执行多个计算）的资源分配。

## Glossary

> The following table summarizes terms you’ll see used to refer to cluster concepts:

Term | Meaning
---|:---
Application   |  User program built on Spark. Consists of a driver program and executors on the cluster.<br/> spark 上构建的用户程序。由驱动程序和 executors 组成。
Application jar   |  A jar containing the user's Spark application. In some cases users will want to create an "uber jar" containing their application along with its dependencies. The user's jar should never include Hadoop or Spark libraries, however, these will be added at runtime.<br/>包含用户spark应用程序的jar. 在一些情况下，用户像创建一个 uber jar, 它包含了应用程序及其依赖。用户的jar不要包含hadoop或spark库，这些将在运行时添加。
Driver program  |  The process running the main() function of the application and creating the SparkContext<br/>运行应用程序的 main 函数、创建SparkContext的进程
Cluster manager   |  An external service for acquiring resources on the cluster (e.g. standalone manager, Mesos, YARN, Kubernetes)<br/>获取资源的外部服务
Deploy mode  |  Distinguishes where the driver process runs. In "cluster" mode, the framework launches the driver inside of the cluster. In "client" mode, the submitter launches the driver outside of the cluster.<br/>用来区分驱动进程运行在哪。在cluster模式下，框架在集群内启动驱动。在client模型下，提交者在集群外启动驱动。
Worker node  |  Any node that can run application code in the cluster<br/>可以运行应用程序代码的任意节点。
Executor  |  A process launched for an application on a worker node, that runs tasks and keeps data in memory or disk storage across them. Each application has its own executors.<br/>在工作节点，启动应用程序的进程，它运行任务、将数据保存在内存或磁盘。每个应用程序有它自己的executors
Task  |  A unit of work that will be sent to one executor<br/>发送给一个executor的工作单元
Job	| A parallel computation consisting of multiple tasks that gets spawned in response to a Spark action (e.g. save, collect); you'll see this term used in the driver's logs.<br/>由多个任务组成的并行计算，这些任务在响应Spark行动算子时生成
Stage  |  Each job gets divided into smaller sets of tasks called stages that depend on each other (similar to the map and reduce stages in MapReduce); you'll see this term used in the driver's logs.<br/>每个作业被划分成更小的任务集，称为stages, stages间互相依赖。