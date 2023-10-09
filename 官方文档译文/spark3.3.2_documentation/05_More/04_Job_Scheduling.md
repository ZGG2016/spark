# Job Scheduling

[TOC]

## Overview

> Spark has several facilities for scheduling resources between computations. First, recall that, as described in the [cluster mode overview](https://spark.apache.org/docs/3.3.2/cluster-overview.html), each Spark application (instance of SparkContext) runs an independent set of executor processes. The cluster managers that Spark runs on provide facilities for [scheduling across applications](https://spark.apache.org/docs/3.3.2/job-scheduling.html#scheduling-across-applications). Second, within each Spark application, multiple “jobs” (Spark actions) may be running concurrently if they were submitted by different threads. This is common if your application is serving requests over the network. Spark includes a [fair scheduler](https://spark.apache.org/docs/3.3.2/job-scheduling.html#scheduling-within-an-application) to schedule resources within each SparkContext.

Spark 有多种在计算程序间调度资源的工具。

首先，每个应用程序(SparkContext实例)运行在一组独立的 executor 进程。集群管理器提供在多个应用程序间调度资源的工具。

然后，在每个 Spark 应用程序中，如果通过不同线程提交，那么多个作业(Spark actions) 可能就会同时运行。

如果你的应用程序服务于多个网络请求，那这种情况是非常常见。 Spark 提供了公平调度器在每个 SparkContext 中调度资源。

## Scheduling Across Applications

> When running on a cluster, each Spark application gets an independent set of executor JVMs that only run tasks and store data for that application. If multiple users need to share your cluster, there are different options to manage allocation, depending on the cluster manager.

当在一个集群运行时，每个 Spark 应用程序都会获得一组独立的 executor JVMs, 仅用来运行应用程序的任务和存储数据。

如果多个用户需要共享你的集群，就会多种选项来管理资源分配，具体方式取决于集群管理器。

> The simplest option, available on all cluster managers, is static partitioning of resources. With this approach, each application is given a maximum amount of resources it can use and holds onto them for its whole duration. This is the approach used in Spark’s [standalone](https://spark.apache.org/docs/3.3.2/spark-standalone.html) and [YARN](https://spark.apache.org/docs/3.3.2/running-on-yarn.html) modes, as well as the [coarse-grained Mesos mode](https://spark.apache.org/docs/3.3.2/running-on-mesos.html#mesos-run-modes). Resource allocation can be configured as follows, based on the cluster type:

在所有集群管理器上，最简单的一种方式就是资源的静态分区，即为每个应用程序赋予一个能使用的最大资源量。这个方式均存在于 standalone\YARN\Mesos 中。 可按如下进行资源分配的配置：

> Standalone mode: By default, applications submitted to the standalone mode cluster will run in FIFO (first-in-first-out) order, and each application will try to use all available nodes. You can limit the number of nodes an application uses by setting the `spark.cores.max` configuration property in it, or change the default for applications that don’t set this setting through `spark.deploy.defaultCores`. Finally, in addition to controlling cores, each application’s s`park.executor.memory` setting controls its memory use.

- Standalone mode: 默认情况下，提交到独立模式集群的应用程序按照 FIFO 顺序执行，每个应用程序会尝试使用所有可用节点。可以通过设置 `spark.cores.max` 限制使用节点的数量，或者通过设置 `spark.deploy.defaultCores` 改变默认值。通过设置 `spark.executor.memory` 来控制使用的内存。

> Mesos: To use static partitioning on Mesos, set the `spark.mesos.coarse` configuration property to true, and optionally set `spark.cores.max` to limit each application’s resource share as in the standalone mode. You should also set `spark.executor.memory` to control the executor memory.

- Mesos: 要在 Mesos 上使用静态分区，将 `spark.mesos.coarse` 属性设为 true, 可选地设置 `spark.cores.max` 来限制每个应用程序使用的资源共享。设置 `spark.executor.memory` 控制 executor 内存。

> YARN: The `--num-executors` option to the Spark YARN client controls how many executors it will allocate on the cluster (`spark.executor.instances` as configuration property), while `--executor-memory` (`spark.executor.memory` configuration property) and `--executor-cores` (`spark.executor.cores` configuration property) control the resources per executor. For more information, see the [YARN Spark Properties](https://spark.apache.org/docs/3.3.2/running-on-yarn.html).

- YARN: Spark YARN client 的 `--num-executors` 选项控制分配到集群的 executors 的数量。`--executor-memory` 和 `--executor-cores` 控制每个 executor 占用的资源量。

> A second option available on Mesos is dynamic sharing of CPU cores. In this mode, each Spark application still has a fixed and independent memory allocation (set by `spark.executor.memory`), but when the application is not running tasks on a machine, other applications may run tasks on those cores. This mode is useful when you expect large numbers of not overly active applications, such as shell sessions from separate users. However, it comes with a risk of less predictable latency, because it may take a while for an application to gain back cores on one node when it has work to do. To use this mode, simply use a `mesos://` URL and set `spark.mesos.coarse` to false.

在 Mesos 上可用的第二个选项就是 CPU 核心的动态共享。在这个模式下，每个 spark 应用程序仍具有一个固定独立的内存分配，但是当应用程序不在机器上运行任务时，其他应用程序可用在这些核心上运行任务。

当你期望有大量不太活跃的应用程序(例如来自不同用户的shell会话)时，此模式非常有用。然而，会带来不可预见的延迟风险，因为应用程序会花费一段时间重新获取节点上的核心。要使用这个模式，使用 `mesos://` URL, 并设置 `spark.mesos.coarse` 为 false.

> Note that none of the modes currently provide memory sharing across applications. If you would like to share data this way, we recommend running a single server application that can serve multiple requests by querying the same RDDs.

注意：现在没有哪种模式可以实现在应用程序间共享内存。如果你向按照这种方法共享数据，推荐运行一个单服务应用程序，它可以通过查询相同的 RDDs 服务于多个请求。

### Dynamic Resource Allocation

> Spark provides a mechanism to dynamically adjust the resources your application occupies based on the workload. This means that your application may give resources back to the cluster if they are no longer used and request them again later when there is demand. This feature is particularly useful if multiple applications share resources in your Spark cluster.

Spark 提供了一种根据工作负载，动态调整应用程序占用的资源量。这意味着如果它们不再被使用这些资源，或当有需要时再请求它们时，应用程序可能会将资源归还给集群。这种特性非常适应于多个应用程序共享集群资源的情况。

> This feature is disabled by default and available on all coarse-grained cluster managers, i.e. [standalone mode](https://spark.apache.org/docs/3.3.2/spark-standalone.html), [YARN mode](https://spark.apache.org/docs/3.3.2/running-on-yarn.html), [Mesos coarse-grained mode](https://spark.apache.org/docs/3.3.2/running-on-mesos.html#mesos-run-modes) and [K8s mode](https://spark.apache.org/docs/3.3.2/running-on-kubernetes.html).

默认是关闭的，在所有细粒度集群管理上可用。

#### Configuration and Setup

> There are two ways for using this feature. First, your application must set both `spark.dynamicAllocation.enabled` and `spark.dynamicAllocation.shuffleTracking.enabled` to true. Second, your application must set both `spark.dynamicAllocation.enabled` and `spark.shuffle.service.enabled` to true after you set up an external shuffle service on each worker node in the same cluster. The purpose of the shuffle tracking or the external shuffle service is to allow executors to be removed without deleting shuffle files written by them (more detail described [below](https://spark.apache.org/docs/3.3.2/job-scheduling.html#graceful-decommission-of-executors)). While it is simple to enable shuffle tracking, the way to set up the external shuffle service varies across cluster managers:

使用这种特性有两种方式：

- 你的应用程序必须设置 `spark.dynamicAllocation.enabled` 和 `spark.dynamicAllocation.shuffleTracking.enabled` 为 true

- 在同一集群的每个工作上节点设置外部 shuffle 服务之后，你的应用程序必须设置 `spark.dynamicAllocation.enabled` 和 `spark.shuffle.service.enabled` 为 true

设置 shuffle 跟踪和外部 shuffle 服务目的是在移除 executor 的时候，能够保留 executor 输出的 shuffle 文件。

虽然启用 shuffle 跟踪很简单，但设置外部 shuffle 服务的方式因集群管理器而异:

- standalone mode: 设置 `spark.shuffle.service.enabled = true` 后，启动工作节点

- Mesos coarse-grained mode: 将 `spark.shuffle.service.enabled` 设置为 true 后，在所有工作节点上运行 `$SPARK_HOME/sbin/start-mesos-shuffle-service.sh`. 例如，可以通过 Marathon 来实现。

- YARN mode: 根据如下描述

> In standalone mode, simply start your workers with `spark.shuffle.service.enabled` set to true.

> In Mesos coarse-grained mode, run `$SPARK_HOME/sbin/start-mesos-shuffle-service.sh` on all worker nodes with `spark.shuffle.service.enabled` set to true. For instance, you may do so through Marathon.

> In YARN mode, follow the instructions [here](https://spark.apache.org/docs/3.3.2/running-on-yarn.html#configuring-the-external-shuffle-service).

> All other relevant configurations are optional and under the `spark.dynamicAllocation.*` and `spark.shuffle.service.*` namespaces. For more detail, see the [configurations page](https://spark.apache.org/docs/3.3.2/configuration.html#dynamic-allocation).

所有其他相关的配置都是可选的，且都在 `spark.dynamicAllocation.*` 和 `spark.shuffle.service.*` 命名空间下。

#### Resource Allocation Policy

> At a high level, Spark should relinquish executors when they are no longer used and acquire executors when they are needed. Since there is no definitive way to predict whether an executor that is about to be removed will run a task in the near future, or whether a new executor that is about to be added will actually be idle, we need a set of heuristics to determine when to remove and request executors.

Spark 应该在不使用 executors 时候丢弃，使用的时候获取。

但并没有一个确定的方式来预测： 将被移除的 executor 接下来是否会运行一个任务，或将被添加的 executor 是否会被闲置。我们需要试探着决定什么时候移除、请求 executors.

##### Request Policy

> A Spark application with dynamic allocation enabled requests additional executors when it has pending tasks waiting to be scheduled. This condition necessarily implies that the existing set of executors is insufficient to simultaneously saturate all tasks that have been submitted but not yet finished.

对于一个启用了动态分配的 Spark 应用程序，当它有等待的任务需要调度的时候，会请求额外的 executors. 在这种情况下，意味着已有的 executors 已经不足以同时执行所有未完成的任务。

> Spark requests executors in rounds. The actual request is triggered when there have been pending tasks for `spark.dynamicAllocation.schedulerBacklogTimeout` seconds, and then triggered again every `spark.dynamicAllocation.sustainedSchedulerBacklogTimeout` seconds thereafter if the queue of pending tasks persists. Additionally, the number of executors requested in each round increases exponentially from the previous round. For instance, an application will add 1 executor in the first round, and then 2, 4, 8 and so on executors in the subsequent rounds.

Spark 会循环请求 executors. 挂起的任务只有在 `spark.dynamicAllocation.schedulerBacklogTimeout` 秒后，才会触发真正的请求，然后如果挂起的任务一直在队列中，就会每 `spark.dynamicAllocation.sustainedSchedulerBacklogTimeout` 秒都会触发一次。

每次循环请求的 executors 的数量都会比上次请求的数量呈指数级增加。 例如，应用程序将在第一轮请求种添加1个 executor, 然后在接下来的几轮种依次请求 2，4，8

> The motivation for an exponential increase policy is twofold. First, an application should request executors cautiously in the beginning in case it turns out that only a few additional executors is sufficient. This echoes the justification for TCP slow start. Second, the application should be able to ramp up its resource usage in a timely manner in case it turns out that many executors are actually needed.

采用指数级增长策略的原因有两个：

首先，请求一开始就应该谨慎地请求 executors, 以防只有几个额外的 executors 就能满足需求。这与 TCP 缓慢启动的原因是一致的。其次，应用程序应该能够及时地增加其资源使用，以防实际需要许多 executors.

##### Remove Policy

> The policy for removing executors is much simpler. A Spark application removes an executor when it has been idle for more than `spark.dynamicAllocation.executorIdleTimeout` seconds. Note that, under most circumstances, this condition is mutually exclusive with the request condition, in that an executor should not be idle if there are still pending tasks to be scheduled.

当 executors 空闲时间超过 `spark.dynamicAllocation.executorIdleTimeout` 秒时, Spark 应用程序将删除它。 

请注意，在大多数情况下，此条件与请求条件是互斥的，因为如果仍有等待调度的任务，则 executor 不应空闲。

#### Graceful Decommission of Executors

> Before dynamic allocation, if a Spark executor exits when the associated application has also exited then all state associated with the executor is no longer needed and can be safely discarded. With dynamic allocation, however, the application is still running when an executor is explicitly removed. If the application attempts to access state stored in or written by the executor, it will have to perform a recompute the state. Thus, Spark needs a mechanism to decommission an executor gracefully by preserving its state before removing it.

在没启用动态分配前，如果 Spark executor 就会退出，与之相关的应用程序也会退出，那么 executor 中的所有状态都不在需要了，可以被安全的删除。

在启用动态分配后，在 executor 被删除后，应用程序仍会继续运行。如果应用程序尝试获取存储在 executor 中的状态或由 executor 写入状态，那么就需要再次计算状态了。

因此 Spark 需要一个机制，保证在删除 executor 前，保存它的状态后，优雅停止。

> This requirement is especially important for shuffles. During a shuffle, the Spark executor first writes its own map outputs locally to disk, and then acts as the server for those files when other executors attempt to fetch them. In the event of stragglers, which are tasks that run for much longer than their peers, dynamic allocation may remove an executor before the shuffle completes, in which case the shuffle files written by that executor must be recomputed unnecessarily.

这个要求对 shuffles 来说是非常重要的。在 shuffles 期间，executor 会首先将它的 map 输出写入本地磁盘，然后当其他 executor 想要获取它们时，它会充当一个服务器的角色。

在一些任务的运行时间比同辈要长的事件中，会在 shuffle 完成前，动态分配会删除 executor, 在这种情况下，由 executor 写入的 shuffle 文件就必须再次计算。
【运行时间长的任务在用到运行时间短的任务产生的输出文件时，可能这个文件已经被删除了】

> The solution for preserving shuffle files is to use an external shuffle service, also introduced in Spark 1.2. This service refers to a long-running process that runs on each node of your cluster independently of your Spark applications and their executors. If the service is enabled, Spark executors will fetch shuffle files from the service instead of from each other. This means any shuffle state written by an executor may continue to be served beyond the executor’s lifetime.

保存 shuffle 文件的作用就是使用外部 shuffle 服务，在 Spark 1.2 中引入。它是在集群的每个节点长期运行的一个进程，它独立于 Spark 应用程序及其执行器。

启用服务后, executors 从服务取 shuffle 文件，而不再是互相获取。这就意味着由 executor 写入的任意 shuffle 状态在 executors 生命周期结束后会持续服务。

> In addition to writing shuffle files, executors also cache data either on disk or in memory. When an executor is removed, however, all cached data will no longer be accessible. To mitigate this, by default executors containing cached data are never removed. You can configure this behavior with `spark.dynamicAllocation.cachedExecutorIdleTimeout`. When set `spark.shuffle.service.fetch.rdd.enabled` to true, Spark can use ExternalShuffleService for fetching disk persisted RDD blocks. In case of dynamic allocation if this feature is enabled executors having only disk persisted blocks are considered idle after `spark.dynamicAllocation.executorIdleTimeout` and will be released accordingly. In future releases, the cached data may be preserved through an off-heap storage similar in spirit to how shuffle files are preserved through the external shuffle service.

除了写入 shuffle 文件, executors 也会将数据缓存到磁盘或内存。

然而当 executors 被移除后，它所有的缓存数据都不再可访问。为了减轻这个问题，默认情况下，通过配置 `spark.dynamicAllocation.cachedExecutorIdleTimeout`， 包含缓存数据的 executors 永远不会被移除。

当 `spark.shuffle.service.fetch.rdd.enabled` 设置为 true 时, spark 可以使用 ExternalShuffleService 获取持久化到磁盘的 RDD 块。

在动态分配情况下，如果启用了这个特性，在 `spark.dynamicAllocation.executorIdleTimeout` 之后，仅具有磁盘持久化块的 executors 就被认为是空闲的，并将被释放掉。

在未来的版本中，可能会采用类似外部 shuffle 服务的方法，将缓存数据保存在堆外存储中，以解决这一问题。

## Scheduling Within an Application

> Inside a given Spark application (SparkContext instance), multiple parallel jobs can run simultaneously if they were submitted from separate threads. By “job”, in this section, we mean a Spark action (e.g. `save`, `collect`) and any tasks that need to run to evaluate that action. Spark’s scheduler is fully thread-safe and supports this use case to enable applications that serve multiple requests (e.g. queries for multiple users).

在指定的 Spark 应用程序（SparkContext 实例）中，如果从不同的线程提交，那么就会同时运行多个并行任务。在本节中，job 指的是 Spark 行动算子(例如save、collect)和行动算子触发运行的任意任务。

Spark 调度器是完全线程安全的，并且能够支持 Spark 应用程序同时处理多个请求（比如：来自不同用户的查询）。

> By default, Spark’s scheduler runs jobs in FIFO fashion. Each job is divided into “stages” (e.g. map and reduce phases), and the first job gets priority on all available resources while its stages have tasks to launch, then the second job gets priority, etc. If the jobs at the head of the queue don’t need to use the whole cluster, later jobs can start to run right away, but if the jobs at the head of the queue are large, then later jobs may be delayed significantly.

默认情况下，Spark 使用 FIFO 调度策略调度作业。每个作业被划分为多个 stage （例如：map 阶段和 reduce 阶段），第一个作业会优先使用所有可用资源，然后是第二个作业，依次类推。

如果队列前面的作业不需要使用全部的集群资源，则后续的作业可以立即启动运行。如果队列前面的作业非常大，后面的作业就可能会延迟很久。

> Starting in Spark 0.8, it is also possible to configure fair sharing between jobs. Under fair sharing, Spark assigns tasks between jobs in a “round robin” fashion, so that all jobs get a roughly equal share of cluster resources. This means that short jobs submitted while a long job is running can start receiving resources right away and still get good response times, without waiting for the long job to finish. This mode is best for multi-user settings.

从 Spark 0.8 开始，支持各个作业间配置公平共享。在公平共享下, Spark 以轮询的方式在作业间分配任务，为了所有的作业能获得大致相等的集群资源。

这意味着，一个长的作业正在运行时，短的作业会立马开始接收资源，不需要等待长的 job 完成。这种模式特别适合于多用户配置。

> This feature is disabled by default and available on all coarse-grained cluster managers, i.e. standalone mode, YARN mode, K8s mode and Mesos coarse-grained mode. To enable the fair scheduler, simply set the `spark.scheduler.mode` property to `FAIR` when configuring a SparkContext:

这个特性默认是禁用的，且在所有细粒度的集群管理器上可用。要启用公平调度器，只需在设置 SparkContext 时将 `spark.scheduler.mode` 属性配置为 `FAIR` 即可

```scala
val conf = new SparkConf().setMaster(...).setAppName(...)
conf.set("spark.scheduler.mode", "FAIR")
val sc = new SparkContext(conf)
```

### Fair Scheduler Pools

> The fair scheduler also supports grouping jobs into pools, and setting different scheduling options (e.g. weight) for each pool. This can be useful to create a “high-priority” pool for more important jobs, for example, or to group the jobs of each user together and give users equal shares regardless of how many concurrent jobs they have instead of giving jobs equal shares. This approach is modeled after the [Hadoop Fair Scheduler](http://hadoop.apache.org/docs/current/hadoop-yarn/hadoop-yarn-site/FairScheduler.html).

公平调度器支持将作业分组放入池中，然后给每个池配置不同的选项（如：权重）。

就能实现为比较重要的作业创建一个高优先级的池，或者也可以将每个用户的作业分到一组，让各用户平均共享集群资源，不用考虑每个用户有多少作业。

Spark 公平调度的实现方式基本都是模仿 Hadoop Fair Scheduler 实现的。

> Without any intervention, newly submitted jobs go into a default pool, but jobs’ pools can be set by adding the `spark.scheduler.pool` “local property” to the SparkContext in the thread that’s submitting them. This is done as follows:

新提交的作业都会进入到默认池中，不过作业进入哪个池可以在提交它们的线程中通过添加 `spark.scheduler.pool` 本地属性设置。

```
// Assuming sc is your SparkContext variable
sc.setLocalProperty("spark.scheduler.pool", "pool1")
```

> After setting this local property, all jobs submitted within this thread (by calls in this thread to RDD.save, count, collect, etc) will use this pool name. The setting is per-thread to make it easy to have a thread run multiple jobs on behalf of the same user. If you’d like to clear the pool that a thread is associated with, simply call:

设置此本地属性后，在此线程中提交的所有作业（即：在该线程中调用行动算子，如：RDD.save/count/collect 等）将使用此池名称。此设置是 per-thread, 可以方便地让一个线程代表同一用户运行多个作业。

如果想要清除线程关联的池，只需调用:

```
sc.setLocalProperty("spark.scheduler.pool", null)
```

### Default Behavior of Pools

> By default, each pool gets an equal share of the cluster (also equal in share to each job in the default pool), but inside each pool, jobs run in FIFO order. For example, if you create one pool per user, this means that each user will get an equal share of the cluster, and that each user’s queries will run in order instead of later queries taking resources from that user’s earlier ones.

默认情况下，每个池获得相同的集群份额(与默认池中的每个作业的份额相同)，但是在每个池中，作业按 FIFO 顺序运行。

例如，如果为每个用户创建一个池，这意味着每个用户将获得相同的集群份额，并且每个用户的查询将按顺序运行，而不是前一个查询完成、释放资源后，后面的查询再获取资源。

### Configuring Pool Properties

> Specific pools’ properties can also be modified through a configuration file. Each pool supports three properties:

可以修改池属性，主要有以下三个属性：

> `schedulingMode`: This can be FIFO or FAIR, to control whether jobs within the pool queue up behind each other (the default) or share the pool’s resources fairly.

- schedulingMode: FIFO 或 FAIR, 控制着池中作业是否在其他作业后面排队（默认），或者公平地共享池中资源。

> `weight`: This controls the pool’s share of the cluster relative to other pools. By default, all pools have a weight of 1. If you give a specific pool a weight of 2, for example, it will get 2x more resources as other active pools. Setting a high weight such as 1000 also makes it possible to implement priority between pools--in essence, the weight-1000 pool will always get to launch tasks first whenever it has jobs active.

- weight: 控制一个池获取资源的比例。 默认所有池权重都是1。如果指定一个池的权重为2，那么它就能获取相比其他池两倍的资源。设置一个高的权重，例如1000，具有高的优先级，权重1000的池将总是首先启动任务，不管何时有作业活跃。

> `minShare`: Apart from an overall weight, each pool can be given a minimum shares (as a number of CPU cores) that the administrator would like it to have. The fair scheduler always attempts to meet all active pools’ minimum shares before redistributing extra resources according to the weights. The `minShare` property can, therefore, be another way to ensure that a pool can always get up to a certain number of resources (e.g. 10 cores) quickly without giving it a high priority for the rest of the cluster. By default, each pool’s `minShare` is 0.

- minShare： 除了总权重，每个池的能获取最小资源份额（也就是CPU核心的数量）。在根据权重分发额外资源之前，公平调度器总是会尝试优先满足所有活跃池的最小资源分配值。因此，minShare 属性能够确保每个资源池都能至少获得一定量的集群资源，而不需要给它高优先级。每个池默认的 minShare 是0。

> The pool properties can be set by creating an XML file, similar to `conf/fairscheduler.xml.template`, and either putting a file named `fairscheduler.xml` on the classpath, or setting `spark.scheduler.allocation.file` property in your [SparkConf](https://spark.apache.org/docs/3.3.2/configuration.html#spark-properties). The file path respects the hadoop configuration and can either be a local file path or HDFS file path.

可以通过 XML 文件设置属性，类似 `conf/fairscheduler.xml.template`, 要么在类路径上放一个名为 `fairscheduler.xml` 的文件，要么在 SparkConf 中设置 `spark.scheduler.allocation.file` 属性。hadoop配置优先文件路径，文件路径可以是本地文件路径，也可以是 HDFS 文件路径。

```
// scheduler file at local
conf.set("spark.scheduler.allocation.file", "file:///path/to/file")
// scheduler file at hdfs
conf.set("spark.scheduler.allocation.file", "hdfs:///path/to/file")
```

> The format of the XML file is simply a `<pool>` element for each pool, with different elements within it for the various settings. For example:

XML 文件的格式是一个 `<pool>` 元素表示一个池，它可以具有不同的元素。

```xml
<?xml version="1.0"?>
<allocations>
  <pool name="production">
    <schedulingMode>FAIR</schedulingMode>
    <weight>1</weight>
    <minShare>2</minShare>
  </pool>
  <pool name="test">
    <schedulingMode>FIFO</schedulingMode>
    <weight>2</weight>
    <minShare>3</minShare>
  </pool>
</allocations>
```

> A full example is also available in `conf/fairscheduler.xml.template`. Note that any pools not configured in the XML file will simply get default values for all settings (scheduling mode FIFO, weight 1, and minShare 0).

没有在 XML 文件中配置的池就使用默认值(调度模式是FIFO,权重是1,最小份额就是0)

### Scheduling using JDBC Connections

> To set a [Fair Scheduler](https://spark.apache.org/docs/3.3.2/job-scheduling.html#fair-scheduler-pools) pool for a JDBC client session, users can set the `spark.sql.thriftserver.scheduler.pool` variable:

JDBC 客户端会话要设置公平调度器池，用户可以设置 `spark.sql.thriftserver.scheduler.pool` 变量

```
SET spark.sql.thriftserver.scheduler.pool=accounting;
```

### Concurrent Jobs in PySpark

> PySpark, by default, does not support to synchronize PVM threads with JVM threads and launching multiple jobs in multiple PVM threads does not guarantee to launch each job in each corresponding JVM thread. Due to this limitation, it is unable to set a different job group via `sc.setJobGroup` in a separate PVM thread, which also disallows to cancel the job via `sc.cancelJobGroup` later.

在默认情况下，PySpark 不支持同步 PVM 线程和 JVM 线程，并且在多个 PVM 线程中启动多个作业，并不保证启动每个对应 JVM 线程中的每个作业。

由于此限制，无法在单独的 PVM 线程中通过 `sc.setJobGroup` 设置不同的作业组，该线程也不允许稍后通过 `sc.cancelJobGroup` 取消作业。

> `pyspark.InheritableThread` is recommended to use together for a PVM thread to inherit the inheritable attributes such as local properties in a JVM thread.

推荐使用 `pyspark.InheritableThread` 与 PVM 线程一起使用继承可继承属性，例如 JVM 线程中的本地属性。