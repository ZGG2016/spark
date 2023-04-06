# Spark Standalone Mode

[TOC]

> In addition to running on the Mesos or YARN cluster managers, Spark also provides a simple standalone deploy mode. You can launch a standalone cluster either manually, by starting a master and workers by hand, or use our provided [launch scripts](https://spark.apache.org/docs/3.0.1/spark-standalone.html#cluster-launch-scripts). It is also possible to run these daemons on a single machine for testing.

Spark 不仅可以运行在 Mesos 和 YARN 集群管理器上，也提供了一个独立集群模式。

**它可以通过手动启动 master 和 workers 来手动启动独立集群，也可以使用 启动脚本 启动。**

也可以在一台机器上运行这些进程，但仅为测试。

## 1、Security

> Security in Spark is OFF by default. This could mean you are vulnerable to attack by default. Please see [Spark Security](https://spark.apache.org/docs/3.0.1/security.html) and the specific security sections in this doc before running Spark.

**默认情况下，Spark 的安全模式是关闭的**，所以易受侵害，但可以通过设置来避免。

## 2、Installing Spark Standalone to a Cluster

> To install Spark Standalone mode, you simply place a compiled version of Spark on each node on the cluster. You can obtain pre-built versions of Spark with each release or [build it yourself](https://spark.apache.org/docs/3.0.1/building-spark.html).

要配置 Spark Standalone 模式，你只需在集群的每个节点上放置 Spark 的编译版本。

你可以获取 Spark 的每个发行版的 pre-built 版本，或者自己 build.

## 3、Starting a Cluster Manually

> You can start a standalone master server by executing:

执行下面的命令启动 master 服务：

```sh
./sbin/start-master.sh
```

> Once started, the master will print out a spark://HOST:PORT URL for itself, which you can use to connect workers to it, or pass as the “master” argument to SparkContext. You can also find this URL on the master’s web UI, which is http://localhost:8080 by default.

> Similarly, you can start one or more workers and connect them to the master via:

启动后，master 打印出 `spark://HOST:PORT` ，可以使用它将 workers 连接到 master，或者作为 "master" 参数传给 SparkContext。

你可以在 master’s web UI 找到这个 URL，默认是 `http://localhost:8080`

同样，你可以启动一个或多个 workers ，通过下面的命令将它们链接到 master ：

```sh
./sbin/start-slave.sh <master-spark-URL>
```

> Once you have started a worker, look at the master’s web UI (http://localhost:8080 by default). You should see the new node listed there, along with its number of CPUs and memory (minus one gigabyte left for the OS).

> Finally, the following configuration options can be passed to the master and worker:

启动 worker 后，会在 master’s web UI (http://localhost:8080 by default) 上看到新的节点，包含 CPUs 和内存(大小为OS总内存减1GB)。

下面的配置项可以传给 master 和 worker：

Argument | Meaning
---|:---
-h HOST, --host HOST | Hostname to listen on
-i HOST, --ip HOST | Hostname to listen on (deprecated, use -h or --host) 弃用了
-p PORT, --port PORT | Port for service to listen on (default: 7077 for master, random for worker)
--webui-port PORT | Port for web UI (default: 8080 for master, 8081 for worker)
-c CORES, --cores CORES | Total CPU cores to allow Spark applications to use on the machine (default: all available); only on worker 允许spark应用程序在这台机器上使用的总的CPU核心数(默认全部)，仅限worker节点
-m MEM, --memory MEM | Total amount of memory to allow Spark applications to use on the machine, in a format like 1000M or 2G (default: your machine's total RAM minus 1 GiB); only on worker 允许spark应用程序在这台机器上使用的总的内存量，格式为1000M/2G(默认机器总的内存减1GB)，仅限worker节点
-d DIR, --work-dir DIR | Directory to use for scratch space and job output logs (default: SPARK_HOME/work); only on worker  暂存空间和job输出日志的目录，仅限worker节点
--properties-file FILE | Path to a custom Spark properties file to load (default: conf/spark-defaults.conf) 配置文件路径


## 4、Cluster Launch Scripts

> To launch a Spark standalone cluster with the launch scripts, you should create a file called conf/slaves in your Spark directory, which must contain the hostnames of all the machines where you intend to start Spark workers, one per line. If conf/slaves does not exist, the launch scripts defaults to a single machine (localhost), which is useful for testing. Note, the master machine accesses each of the worker machines via ssh. By default, ssh is run in parallel and requires password-less (using a private key) access to be setup. If you do not have a password-less setup, you can set the environment variable SPARK_SSH_FOREGROUND and serially provide a password for each worker.

> Once you’ve set up this file, you can launch or stop your cluster with the following shell scripts, based on Hadoop’s deploy scripts, and available in SPARK_HOME/sbin:

要想使用 启动脚本 启动 Spark standalone 集群，你需要 **创建 `conf/slaves` 文件，文件必须包含所有你想启动的 worker 机器的主机名，一行列一个**。

如果 conf/slaves 不存在，启动脚本 会默认启动一个机器(localhost)

master 机器通过 ssh 访问每个 worker 节点。

默认情况下，ssh 并行运行，并且需要配置无密码(使用一个私钥)的访问。

**如果没有设置无密码访问，可以设置环境变量 `SPARK_SSH_FOREGROUND` 并且为每个 worker 提供一个密码。**

设置完这个 slaves 文件后，可以使用下列的 shell 脚本启动、停止集群，这个脚本是基于 hadoop 的部署脚本，存在 `SPARK_HOME/sbin` 目录下。

- sbin/start-master.sh - Starts a master instance on the machine the script is executed on.
- sbin/start-slaves.sh - Starts a worker instance on each machine specified in the conf/slaves file.
- sbin/start-slave.sh - Starts a worker instance on the machine the script is executed on.
- sbin/start-all.sh - Starts both a master and a number of workers as described above.
- sbin/stop-master.sh - Stops the master that was started via the sbin/start-master.sh script.
- sbin/stop-slave.sh - Stops all worker instances on the machine the script is executed on.
- sbin/stop-slaves.sh - Stops all worker instances on the machines specified in the conf/slaves file.
- sbin/stop-all.sh - Stops both the master and the workers as described above.

> Note that these scripts must be executed on the machine you want to run the Spark master on, not your local machine.

> You can optionally configure the cluster further by setting environment variables in conf/spark-env.sh. Create this file by starting with the conf/spark-env.sh.template, and copy it to all your worker machines for the settings to take effect. The following settings are available:

注意：**这些脚本必须在 Spark master 的机器上执行**，而不是你的本地机器。

可以通过**在 `conf/spark-env.sh` 中设置环境变量**来进一步配置集群。

利用 `conf/spark-env.sh.template` 文件来创建这个文件，然后将它**复制到所有的 worker 机器上使设置有效**。下面的设置是可用的：

Environment Variable    | Meaning
---|:---
SPARK_MASTER_HOST       | Bind the master to a specific hostname or IP address, for example a public one. 绑定master和一个指定的主机名或IP，如公共的一个。
SPARK_MASTER_PORT       | Start the master on a different port (default: 7077).启动master的端口
SPARK_MASTER_WEBUI_PORT | Port for the master web UI (default: 8080). master web UI 端口
SPARK_MASTER_OPTS       | Configuration properties that apply only to the master in the form "-Dx=y" (default: none). See below for a list of possible options. 应用到master的配置，格式为'-Dx=y'，默认是none
SPARK_LOCAL_DIRS        | Directory to use for "scratch" space in Spark, including map output files and RDDs that get stored on disk. This should be on a fast, local disk in your system. It can also be a comma-separated list of multiple directories on different disks. 用作暂存空间的目录，包括了map输出文件、存储在磁盘上的RDDs。这个目录应该设置在系统上的一个快速的本地磁盘上。可以用逗号分隔，来设置不同磁盘上的多个目录。
SPARK_WORKER_CORES      | Total number of cores to allow Spark applications to use on the machine (default: all available cores).允许spark应用程序在这台机器上使用的总的CPU核心数(默认全部可用)
SPARK_WORKER_MEMORY     | Total amount of memory to allow Spark applications to use on the machine, e.g. 1000m, 2g (default: total memory minus 1 GiB); note that each application's individual memory is configured using its spark.executor.memory property.允许spark应用程序在这台机器上使用的总的内存量，格式为1000M/2G(默认机器总的内存减1GB)。注意：每个应用程序的独立内存使用它的`spark.executor.memory `配置
SPARK_WORKER_PORT       | Start the Spark worker on a specific port (default: random).启动worker的端口
SPARK_WORKER_WEBUI_PORT | Port for the worker web UI (default: 8081).worker web UI 端口
SPARK_WORKER_DIR        | Directory to run applications in, which will include both logs and scratch space (default: SPARK_HOME/work).运行应用程序的目录，包括日志和临时空间(默认值:`SPARK_HOME/work`)。
SPARK_WORKER_OPTS       | Configuration properties that apply only to the worker in the form "-Dx=y" (default: none). See below for a list of possible options.应用到worker的配置，格式为'-Dx=y'，默认是none
SPARK_DAEMON_MEMORY     | Memory to allocate to the Spark master and worker daemons themselves (default: 1g).分配给master和worker进程本身的内存。(默认1G)
SPARK_DAEMON_JAVA_OPTS | JVM options for the Spark master and worker daemons themselves in the form "-Dx=y" (default: none). master和worker进程本身的jvm选项，格式为'-Dx=y'，默认是none
SPARK_DAEMON_CLASSPATH | Classpath for the Spark master and worker daemons themselves (default: none). master和worker进程本身的 Classpath，默认是none
SPARK_PUBLIC_DNS       | The public DNS name of the Spark master and workers (default: none). master和worker的公共DNS名称。

> Note: The launch scripts do not currently support Windows. To run a Spark cluster on Windows, start the master and workers by hand.

> SPARK_MASTER_OPTS supports the following system properties:

注意： 启动脚本 现在还不支持 Windows。要在 Windows 上运行一个 Spark 集群，需要手动启动 master 和 workers。

`SPARK_MASTER_OPTS` 支持以下系统属性:

Property Name                                         | Default   |Meaning | Since Version
---|:---|:---|:---
spark.deploy.retainedApplications                     |200        |The maximum number of completed applications to display. Older applications will be dropped from the UI to maintain this limit. 展示的完成的应用程序的最大数量 |0.8.0
spark.deploy.retainedDrivers                          |200        |The maximum number of completed drivers to display. Older drivers will be dropped from the UI to maintain this limit. 展示的完成的drivers的最大数量 | 1.1.0
spark.deploy.spreadOut                                |true       |Whether the standalone cluster manager should spread applications out across nodes or try to consolidate them onto as few nodes as possible. Spreading out is usually better for data locality in HDFS, but consolidating is more efficient for compute-intensive workloads. 独立集群管理器是否应该跨节点分发应用程序，还是尝试将它们合并到尽可能少的节点上。分发通常对HDFS中的数据局部性更好，但是合并对计算密集型工作负载更有效。           | 0.6.1
spark.deploy.defaultCores                             |(infinite) |Default number of cores to give to applications in Spark's standalone mode if they don't set spark.cores.max. If not set, applications always get all available cores unless they configure spark.cores.max themselves. Set this lower on a shared cluster to prevent users from grabbing the whole cluster by default.没有设置`spark .core .max`的情况下，独立模式给应用程序的默认内核数。如果没有设置，应用程序总是获得所有可用的内核。在共享集群上设置较低的值，以防止用户在默认情况下占用整个集群。  | 0.9.0
spark.deploy.maxExecutorRetries                      |10          |Limit on the maximum number of back-to-back executor failures that can occur before the standalone cluster manager removes a faulty application. An application will never be removed if it has any running executors. If an application experiences more than spark.deploy.maxExecutorRetries failures in a row, no executors successfully start running in between those failures, and the application has no running executors then the standalone cluster manager will remove the application and mark it as failed. To disable this automatic removal, set spark.deploy.maxExecutorRetries to -1. 在独立集群管理器删除错误应用程序之前，executor连续出现故障的最大次数。如果一个应用程序有可以运行的executor，它将永远不会被删除。如果一个应用程序连续的故障次数超过了`spark.deploy.maxExecutorRetries`，在这些故障间没有executor能成功启动，并且应用程序没有正在运行的executor，那么独立的集群管理器将删除这个应用程序并标记为失败。若要禁用自动删除，请设置`spark.deploy.maxExecutorRetries`为-1。 | 1.6.3
spark.worker.timeout                                 |60         |Number of seconds after which the standalone deploy master considers a worker lost if it receives no heartbeats. |0.6.2
spark.worker.resource.{resourceName}.amount          |(none)     |Amount of a particular resource to use on the worker. 在worker上使用的特定资源的数量 |3.0.0
spark.worker.resource.{resourceName}.discoveryScript |(none)     |Path to resource discovery script, which is used to find a particular resource while worker starting up. And the output of the script should be formatted like the ResourceInformation class.资源发现脚本的路径，用于在worker启动时，查找特定的资源。脚本的输出应该像`ResourceInformation`类那样格式化。 | 3.0.0
spark.worker.resourcesFile                           |(none)     |Path to resources file which is used to find various resources while worker starting up. The content of resources file should be formatted like [{"id":{"componentName": "spark.worker","resourceName":"gpu"},"addresses":["0","1","2"]}]. If a particular resource is not found in the resources file, the discovery script would be used to find that resource. If the discovery script also does not find the resources, the worker will fail to start up. 资源文件的路径，用于在worker时，查找各种资源。资源文件的内容应该格式化为`[{"id":{"componentName": "spark.worker"，"resourceName":"gpu"}，"addresses":["0"，"1"，"2"]}]`。如果在资源文件中没有找到特定的资源，则将使用发现脚本来查找该资源。如果发现脚本也没有找到资源，worker将启动失败。| 3.0.0

> SPARK_WORKER_OPTS supports the following system properties:

`SPARK_WORKER_OPTS` 支持以下系统属性:

Property Name                                   | Default | Meaning | Since Version
---|:---|:---|:---
spark.worker.cleanup.enabled                    | false             | Enable periodic cleanup of worker / application directories. Note that this only affects standalone mode, as YARN works differently. Only the directories of stopped applications are cleaned up. This should be enabled if spark.shuffle.service.db.enabled is "true" 启用对worker和application目录的定期清理。注意，这只影响独立模式，因为YARN的工作方式不同。只有已停止应用程序的目录会被清理。这个应该在`spark.shuffle.service.db=true`中启用。| 1.0.0
spark.worker.cleanup.interval                   | 1800 (30 minutes) | Controls the interval, in seconds, at which the worker cleans up old application work dirs on the local machine. worker清理本地机器上旧应用程序工作目录的时间间隔(以秒为单位)。| 1.0.0
spark.worker.cleanup.appDataTtl                 | 604800 (7 days, 7 * 24 * 3600) | The number of seconds to retain application work directories on each worker. This is a Time To Live and should depend on the amount of available disk space you have. Application logs and jars are downloaded to each application work dir. Over time, the work dirs can quickly fill up disk space, especially if you run jobs very frequently. 在每个worker上，保留应用程序工作目录的秒数。这是一个Time To Live，取决于有多少可用的磁盘空间。应用程序日志和jar被下载到每个应用程序工作目录。随着时间的推移，工作目录会很快填满磁盘空间，特别是在非常频繁地运行作业的情况下。| 1.0.0
spark.shuffle.service.db.enabled                | true               | Store External Shuffle service state on local disk so that when the external shuffle service is restarted, it will automatically reload info on current executors. This only affects standalone mode (yarn always has this behavior enabled). You should also enable spark.worker.cleanup.enabled, to ensure that the state eventually gets cleaned up. This config may be removed in the future.将External Shuffle服务状态存储在本地磁盘上，以便当External Shuffle服务重新启动时，它会自动重新加载当前executors的信息。这只影响独立模式(yarn总是启用此行为)。还应该启用`spark.worker.cleanup.enabled`，以确保状态最终被清除。这个配置将来可能会被删除。 | 3.0.0
spark.storage.cleanupFilesAfterExecutorExit     | true               | Enable cleanup non-shuffle files(such as temp.shuffle blocks, cached RDD/broadcast blocks, spill files, etc) of worker directories following executor exits. Note that this doesn't overlap with `spark.worker.cleanup.enabled`, as this enables cleanup of non-shuffle files in local directories of a dead executor, while `spark.worker.cleanup.enabled` enables cleanup of all files/subdirectories of a stopped and timeout application. This only affects Standalone mode, support of other cluster managers can be added in the future.在执行程序退出后，启用清理工作目录的non-shuffle文件(如temp.shuffle块、缓存的RDD/广播块、溢出文件等)。注意，这没有与`spark.worker.cleanup`重叠。enabled '，因为这是清除死的executor的本地目录中的non-shuffle文件，而`spark.worker.cleanup.enabled`清除已停止和超时的应用程序的所有文件/子目录。这只影响独立模式，将来可以添加对其他集群管理器的支持。 | 2.4.0
spark.worker.ui.compressedLogFileLengthCacheSize| 100                | For compressed log files, the uncompressed file can only be computed by uncompressing the files. Spark caches the uncompressed file size of compressed log files. This property controls the cache size.对于压缩的日志文件，未压缩的文件只能通过解压缩文件来计算。Spark缓存压缩日志文件的未压缩时的大小。此属性控制缓存大小。 | 2.0.2

## 5、Resource Allocation and Configuration Overview

> Please make sure to have read the Custom Resource Scheduling and Configuration Overview section on the [configuration page](https://spark.apache.org/docs/3.0.1/configuration.html). This section only talks about the Spark Standalone specific aspects of resource scheduling.

先阅读配置页的自定义资源调度和配置总览。**下面只讲述资源调度的特定方面**。

> Spark Standalone has 2 parts, the first is configuring the resources for the Worker, the second is the resource allocation for a specific application.

Spark Standalone 有两部分：第一为 worker 配置资源，第二为特定应用程序分配资源。

> The user must configure the Workers to have a set of resources available so that it can assign them out to Executors. The spark.worker.resource.{resourceName}.amount is used to control the amount of each resource the worker has allocated. The user must also specify either spark.worker.resourcesFile or spark.worker.resource.{resourceName}.discoveryScript to specify how the Worker discovers the resources its assigned. See the descriptions above for each of those to see which method works best for your setup.

用户需要给 workers 配置一些可用资源，使其能进一步为 Executors 分配。

`spark.worker.resource.{resourceName}.amount` 控制每个 worker 分配到的资源量。

`spark.worker.resourcesFile` 或 `spark.worker.resource.{resourceName}.discoveryScript` 表示 worker 如何发现分配的资源。

> The second part is running an application on Spark Standalone. The only special case from the standard Spark resource configs is when you are running the Driver in client mode. For a Driver in client mode, the user can specify the resources it uses via spark.driver.resourcesFile or spark.driver.resource.{resourceName}.discoveryScript. If the Driver is running on the same host as other Drivers, please make sure the resources file or discovery script only returns resources that do not conflict with other Drivers running on the same node.

**标准的 spark 的资源配置的一种特殊情况就是以客户端模式运行 driver**。 以客户端模式运行的 driver ，用户通过设置 `spark.driver.resourcesfile`  或 `spark.driver.resource.{resourceName}.discoveryScript` 来指定它所使用的资源。

如果 Driver 与其他 Driver 在同一主机上运行，请确保资源文件或发现脚本只返回与在同一节点上运行的其他驱动程序不冲突的资源。

> Note, the user does not need to specify a discovery script when submitting an application as the Worker will start each Executor with the resources it allocates to it.

注意：当提交应用程序时，用户不需要指定一个发现脚本，因为 Worker 会启动每个带有资源的 Executor 。

## 6、Connecting an Application to the Cluster

> To run an application on the Spark cluster, simply pass the spark://IP:PORT URL of the master as to the [SparkContext constructor](https://spark.apache.org/docs/3.0.1/rdd-programming-guide.html#initializing-spark).

To run an interactive Spark shell against the cluster, run the following command:

为了在集群上运行应用程序，需给 SparkContext 传递 master 的 `spark://IP:PORT URL`。

运行一个交互的 Spark shell：

```sh
./bin/spark-shell --master spark://IP:PORT
```

> You can also pass an option --total-executor-cores <numCores> to control the number of cores that spark-shell uses on the cluster.

可用通过 `--total-executor-cores <numCores>` 项来控制 spark-shell 使用核数。

## 7、Launching Spark Applications

> The [spark-submit script](https://spark.apache.org/docs/3.0.1/submitting-applications.html) provides the most straightforward way to submit a compiled Spark application to the cluster. For standalone clusters, Spark currently supports two deploy modes. In client mode, the driver is launched in the same process as the client that submits the application. In cluster mode, however, the driver is launched from one of the Worker processes inside the cluster, and the client process exits as soon as it fulfills its responsibility of submitting the application without waiting for the application to finish.

spark-submit 脚本是最直接的提交程序的方式。**对于 standalone 集群有两种部署模式**：

- 客户端模式：driver 在`提交应用程序的客户端`的相同进程中启动。
- 集群模式：driver 在集群的一个 worker 进程中启动，客户端进程一旦完成提交应用程序的职责，而不用等待应用程序完成，就会立即退出。

> If your application is launched through Spark submit, then the application jar is automatically distributed to all worker nodes. For any additional jars that your application depends on, you should specify them through the --jars flag using comma as a delimiter (e.g. --jars jar1,jar2). To control the application’s configuration or execution environment, see [Spark Configuration](https://spark.apache.org/docs/3.0.1/configuration.html).

> Additionally, standalone cluster mode supports restarting your application automatically if it exited with non-zero exit code. To use this feature, you may pass in the --supervise flag to spark-submit when launching your application. Then, if you wish to kill an application that is failing repeatedly, you may do so through:

**使用 spark-submit 脚本启动，应用程序 jar 会自动分发到各 worker 节点。对于额外的 jar ，可用通过 `--jars` 指定。**

你可以传一个 `--supervise` 参数，可用在 `non-zero exit code` 退出时，自动重启程序。

如果你想 kill 一个频繁失败的程序，你可以这么做：

```sh
./bin/spark-class org.apache.spark.deploy.Client kill <master url> <driver ID>
```

> You can find the driver ID through the standalone Master web UI at http://<master url>:8080.

## 8、Resource Scheduling

> The standalone cluster mode currently only supports a simple FIFO scheduler across applications. However, to allow multiple concurrent users, you can control the maximum number of resources each application will use. By default, it will acquire all cores in the cluster, which only makes sense if you just run one application at a time. You can cap the number of cores by setting spark.cores.max in your SparkConf. For example:

当前 standalone cluster mode **仅支持 FIFO 调度器**。然而可以用控制每个应用程序使用的资源的最大量。

默认情况下，它将获取集群中的所有核，这只有在某一时刻只允许一个应用程序运行时才有意义。

可以通过 `spark.cores.max` 在 SparkConf 中设置核的数量。例如：

```java
val conf = new SparkConf()
  .setMaster(...)
  .setAppName(...)
  .set("spark.cores.max", "10")
val sc = new SparkContext(conf)
```

> In addition, you can configure spark.deploy.defaultCores on the cluster master process to change the default for applications that don’t set spark.cores.max to something less than infinite. Do this by adding the following to conf/spark-env.sh:

此外，可以在集群的 master 进程中配置 `spark.deploy.defaultCores` ，来修改 为没有将`spark.cores.max` 设置为小于无穷大的应用程序的默认情况。通过添加下面的命令到 `conf/spark-env.sh` 执行以上的操作：

```sh
export SPARK_MASTER_OPTS="-Dspark.deploy.defaultCores=<value>"
```

> This is useful on shared clusters where users might not have configured a maximum number of cores individually.

这在用户没有配置最大独立核数的共享集群中是有用的。

## 9、Executors Scheduling

> The number of cores assigned to each executor is configurable. When spark.executor.cores is explicitly set, multiple executors from the same application may be launched on the same worker if the worker has enough cores and memory. Otherwise, each executor grabs all the cores available on the worker by default, in which case only one executor per application may be launched on each worker during one single schedule iteration.

**分配给每个 executor 的核的数量是可配置的**。如果 worker 有足够的核和内存，当设置 `spark.executor.cores` 后，在相同 worker 的相同应用程序的多个 executors 会被启动。

否则，默认情况下，每个 executor 会取到 worker 上所有可用的核。在这种情况下，在单个调度迭代期间，每个应用程序只能在每个 worker 上启动一个 executor 。

## 10、Monitoring and Logging

> Spark’s standalone mode offers a web-based user interface to monitor the cluster. The master and each worker has its own web UI that shows cluster and job statistics. By default, you can access the web UI for the master at port 8080. The port can be changed either in the configuration file or via command-line options.

> In addition, detailed log output for each job is also written to the work directory of each slave node (SPARK_HOME/work by default). You will see two files for each job, stdout and stderr, with all output it wrote to its console.

Spark standalone 模式提供了一个基于 web 的用户接口来监控集群。 **master 和每个 worker 都有它自己的显示集群和作业信息的 web UI。**

默认情况下，可以通过 master 的 8080 端口来访问 web UI 。这个端口可以通过配置文件修改或者通过命令行选项修改。

此外，**每个 job 的详细日志输出也会写入到每个 slave 节点的工作目录中。(默认是 `SPARK_HOME/work`)**。你会看到每个作业的两个文件，分别是 stdout 和 stderr，其中所有输出都写入其控制台。

## 11、Running Alongside Hadoop

> You can run Spark alongside your existing Hadoop cluster by just launching it as a separate service on the same machines. To access Hadoop data from Spark, just use an hdfs:// URL (typically hdfs://<namenode>:9000/path, but you can find the right URL on your Hadoop Namenode’s web UI). Alternatively, you can set up a separate cluster for Spark, and still have it access HDFS over the network; this will be slower than disk-local access, but may not be a concern if you are still running in the same local area network (e.g. you place a few Spark machines on each rack that you have Hadoop on).

**可以将 Spark 集成到现有的 Hadoop 集群，只需在同一台机器上将其作为单独的服务启动**。要 Spark 访问 Hadoop 的数据，只需要使用 hdfs:// URL(通常为 `hdfs://<namenode>:9000/path`，但是可以在 Hadoop Namenode 的 web UI 中找到正确的 URL)

也**可以为 Spark 设置一个单独的集群，但仍然可以通过网络访问 HDFS**。这将比磁盘本地访问速度慢，但是如果仍然在同一个局域网中运行(例如，将 Hadoop 上的每个机架放置几台 Spark 机器)，延迟可能不会引起注意。

## 12、Configuring Ports for Network Security

> Generally speaking, a Spark cluster and its services are not deployed on the public internet. They are generally private services, and should only be accessible within the network of the organization that deploys Spark. Access to the hosts and ports used by Spark services should be limited to origin hosts that need to access the services.

> This is particularly important for clusters using the standalone resource manager, as they do not support fine-grained access control in a way that other resource managers do.

> For a complete list of ports to configure, see the [security page](https://spark.apache.org/docs/3.0.1/security.html#configuring-ports-for-network-security).

**Spark 及其服务通常并不部署到公共网络，它们是私有服务**，应当在部署 Spark 的组织的网络内访问。对 Spark 服务使用的主机和端口的访问应该仅限于需要访问服务的原始主机。

这对于使用独立资源管理器的集群尤其重要，因为它们不像其他资源管理器那样支持细粒度访问控制。

## 13、High Availability

> By default, standalone scheduling clusters are resilient to Worker failures (insofar as Spark itself is resilient to losing work by moving it to other workers). However, the scheduler uses a Master to make scheduling decisions, and this (by default) creates a single point of failure: if the Master crashes, no new applications can be created. In order to circumvent this, we have two high availability schemes, detailed below.

默认情况下，standalone 调度集群对于 Worker 的失败是有弹性的。但是，调度器使用一个 Master 进行调度决策，并且(默认情况下)会导致一个单点故障：如果 Master 崩溃，新的应用程序将不会被创建。为了规避这一点，我们有两个高可用性方案，详细说明如下。

### 13.1、Standby Masters with ZooKeeper

#### 13.1.1、Overview

> Utilizing ZooKeeper to provide leader election and some state storage, you can launch multiple Masters in your cluster connected to the same ZooKeeper instance. One will be elected “leader” and the others will remain in standby mode. If the current leader dies, another Master will be elected, recover the old Master’s state, and then resume scheduling. The entire recovery process (from the time the first leader goes down) should take between 1 and 2 minutes. Note that this delay only affects scheduling new applications – applications that were already running during Master failover are unaffected.

> Learn more about getting started with ZooKeeper [here](https://zookeeper.apache.org/doc/current/zookeeperStarted.html).

使用 ZooKeeper 提供的领导选举和一些状态存储，**在连接到同一 ZooKeeper 实例的集群中，启动多个 Masters**。

一个节点将被选举为 "leader" ，其他节点进入 standby 模式。**如果当前的 leader 宕掉了，另一个 Master 将会被选举，从老的 Master 恢复状态，并且恢复调度**。整个恢复过程(从第一个 leader 宕掉开始)应该会使用 1 到 2 分钟。

注意此延迟仅仅影响调度新应用程序 – 在 Master failover 期间已经运行的应用程序不受影响。

#### 13.1.2、Configuration

> In order to enable this recovery mode, you can set SPARK_DAEMON_JAVA_OPTS in spark-env by configuring spark.deploy.recoveryMode and related spark.deploy.zookeeper.* configurations. For more information about these configurations please refer to the [configuration doc](https://spark.apache.org/docs/3.0.1/configuration.html#deploy)

> Possible gotcha: If you have multiple Masters in your cluster but fail to correctly configure the Masters to use ZooKeeper, the Masters will fail to discover each other and think they’re all leaders. This will not lead to a healthy cluster state (as all Masters will schedule independently).

为了启用这个恢复模式，**可以在 spark-env 中设置 `SPARK_DAEMON_JAVA_OPTS` ，通过配置 `spark.deploy.recoveryMode` 和相关的 `spark.deploy.zookeeper`**.配置。

可能的陷阱：如果在集群中有多个 Masters ，但是没有正确地配置 Masters 使用 ZooKeeper，Masters 将无法相互发现，并认为它们都是 leader。这将不会形成一个健康的集群状态(因为所有的 Masters 将会独立调度)。

#### 13.1.3、Details

> After you have a ZooKeeper cluster set up, enabling high availability is straightforward. Simply start multiple Master processes on different nodes with the same ZooKeeper configuration (ZooKeeper URL and directory). Masters can be added and removed at any time.

> In order to schedule new applications or add Workers to the cluster, they need to know the IP address of the current leader. This can be accomplished by simply passing in a list of Masters where you used to pass in a single one. For example, you might start your SparkContext pointing to spark://host1:port1,host2:port2. This would cause your SparkContext to try registering with both Masters – if host1 goes down, this configuration would still be correct as we’d find the new leader, host2.

在设置了 ZooKeeper 集群之后，实现高可用性是很简单的。**只需要在具有相同 ZooKeeper 配置(ZooKeeper URL 和 目录)的不同节点上启动多个 Master 进程。**

Masters 随时可以被添加和删除。

为了调度新的应用程序或者添加新的 Worker 到集群中，**他们需要知道当前的 leader 的 IP 地址。这可以通过传递一个 Masters 的列表来完成。例如，可以启动 SparkContext 指向 spark://host1:port1,host2:port2。**

这将导致 SparkContext 尝试去注册两个 Masters – 如果 host1 宕掉，这个配置仍然是正确地，因为我们将会发现新的 leader host2。

在注册 Master 与正常操作之间有一个重要的区别。当启动的时候，一个应用程序或者 Worker 需要找到当前的 lead Master ，并向其注册。一旦它成功注册，它就在系统中了(即存储在了 ZooKeeper 中)。

**如果发生 failover，新的 leader 将会联系所有已经注册的应用程序和 Workers ，通知他们领导层的变化，所以他们甚至不知道新的 Master 存在。**

由于这个属性，新的 Masters 可以在任何时间创建，唯一需要担心的是，新应用程序和 Workers 可以找到它，并注册，以防其成为 leader。

> There’s an important distinction to be made between “registering with a Master” and normal operation. When starting up, an application or Worker needs to be able to find and register with the current lead Master. Once it successfully registers, though, it is “in the system” (i.e., stored in ZooKeeper). If failover occurs, the new leader will contact all previously registered applications and Workers to inform them of the change in leadership, so they need not even have known of the existence of the new Master at startup.

> Due to this property, new Masters can be created at any time, and the only thing you need to worry about is that new applications and Workers can find it to register with in case it becomes the leader. Once registered, you’re taken care of.

### 13.2、Single-Node Recovery with Local File System

#### 13.2.1、Overview

> ZooKeeper is the best way to go for production-level high availability, but if you just want to be able to restart the Master if it goes down, FILESYSTEM mode can take care of it. When applications and Workers register, they have enough state written to the provided directory so that they can be recovered upon a restart of the Master process.

ZooKeeper 是生产级别的高可用性的最佳方法，但是当 Master 宕掉，如果你只想能重启 Master 服务器，那么就可用使用 FILESYSTEM 模式。当应用程序和 Workers 注册了之后，它们具有写入目录的足够状态，以便在 Master 进程重启后可以恢复它们。

#### 13.2.2、Configuration

> In order to enable this recovery mode, you can set SPARK_DAEMON_JAVA_OPTS in spark-env using this configuration:

为了启用此恢复模式，你可以通过使用以下配置 spark-env 中的 `SPARK_DAEMON_JAVA_OPTS`：

System property | Meaning | Since Version
---|:---|:---
spark.deploy.recoveryMode | Set to FILESYSTEM to enable single-node recovery mode (default: NONE). | 0.8.1
spark.deploy.recoveryDirectory | The directory in which Spark will store recovery state, accessible from the Master's perspective.存储恢复状态的目录 | 0.8.1

#### 13.2.3、Details

> This solution can be used in tandem with a process monitor/manager like monit, or just to enable manual recovery via restart.

> While filesystem recovery seems straightforwardly better than not doing any recovery at all, this mode may be suboptimal for certain development or experimental purposes. In particular, killing a master via stop-master.sh does not clean up its recovery state, so whenever you start a new Master, it will enter recovery mode. This could increase the startup time by up to 1 minute if it needs to wait for all previously-registered Workers/clients to timeout.

> While it’s not officially supported, you could mount an NFS directory as the recovery directory. If the original Master node dies completely, you could then start a Master on a different node, which would correctly recover all previously registered Workers/applications (equivalent to ZooKeeper recovery). Future applications will have to be able to find the new Master, however, in order to register.

这个解决方案可以与 像 monit 这样的流程监视/管理器 一起使用，或者只是通过重启手动恢复。

虽然文件系统恢复似乎比根本不进行任何恢复更好，但**这种模式对于某些开发或实验目的可能不是最优的**。特别是，通过 stop-master.sh kill master 并不会清除其恢复状态，因此无论何时启动一个新的Master，它都将进入恢复模式。

如果需要等待之前注册的所有 worker/clients 超时，这可能会增加最多1分钟的启动时间。

虽然没有正式的支持，你也可以挂载 NFS 目录作为恢复目录。如果 original Master 完全地死亡，则您可以在一个不同的节点上启动 Master，这将正确恢复所有以前注册的 Workers/applications(相当于 ZooKeeper 恢复)。然而，未来的应用程序必须能够找到新的 Master 才能注册。