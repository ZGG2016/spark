# Spark Configuration

[TOC]

> Spark provides three locations to configure the system:

Spark 提供三个配置系统的途径：

- Spark 属性控制着大部分的应用程序参数，可以通过 SparkConf 对象设置，或通过 Java 系统属性设置。
- 环境变量可以在每个节点上通过 `conf/spark-env.sh` 设置单个机器的属性，例如 IP 地址。
- 通过 `log4j2.properties` 配置 Logging.

> [Spark properties](https://spark.apache.org/docs/3.3.2/configuration.html#spark-properties) control most application parameters and can be set by using a [SparkConf](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/SparkConf.html) object, or through Java system properties.
> [Environment variables](https://spark.apache.org/docs/3.3.2/configuration.html#environment-variables) can be used to set per-machine settings, such as the IP address, through the `conf/spark-env.sh` script on each node.
> [Logging](https://spark.apache.org/docs/3.3.2/configuration.html#configuring-logging) can be configured through `log4j2.properties`.

## Spark Properties

> Spark properties control most application settings and are configured separately for each application. These properties can be set directly on a [SparkConf](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/SparkConf.html) passed to your `SparkContext.SparkConf` allows you to configure some of the common properties (e.g. master URL and application name), as well as arbitrary key-value pairs through the `set()` method. For example, we could initialize an application with two threads as follows:

Spark 属性控制着大部分的应用程序参数，可以为每个应用程序单独配置。这些属性可以在 SparkConf 上直接设置，然后传给 `SparkContext.SparkConf`, 这种方式可以让你配置一些常用属性（如 master URL 和应用程序名称）。 也可以通过 `set()` 方法设置键值对属性。例如，我们可以使用两个线程初始化一个应用程序：

> Note that we run with `local[2]`, meaning two threads - which represents “minimal” parallelism, which can help detect bugs that only exist when we run in a distributed context.

注意，`local[2]` 就表示运行两个线程，也就是最小并行度，这可以帮助在分布式上下文中检测错误。

```scala
val conf = new SparkConf()
             .setMaster("local[2]")
             .setAppName("CountingSheep")
val sc = new SparkContext(conf)
```

> Note that we can have more than 1 thread in local mode, and in cases like Spark Streaming, we may actually require more than 1 thread to prevent any sort of starvation issues.

注意，在本地模式下，我们可以使用超过一个线程，就像是在 Spark Streaming 情形下，我们可以使用多个线程，防止任意类型的饥饿问题。

> Properties that specify some time duration should be configured with a unit of time. The following format is accepted:

需要指定一些持续时间的属性应该使用时间单位配置。下列格式是可接受的：

	25ms (milliseconds)
	5s (seconds)
	10m or 10min (minutes)
	3h (hours)
	5d (days)
	1y (years)

> Properties that specify a byte size should be configured with a unit of size. The following format is accepted:

需要指定一个字节大小的属性应该使用大小单位配置。下列格式是可接受的：

	1b (bytes)
	1k or 1kb (kibibytes = 1024 bytes)
	1m or 1mb (mebibytes = 1024 kibibytes)
	1g or 1gb (gibibytes = 1024 mebibytes)
	1t or 1tb (tebibytes = 1024 gibibytes)
	1p or 1pb (pebibytes = 1024 tebibytes)

> While numbers without units are generally interpreted as bytes, a few are interpreted as KiB or MiB. See documentation of individual configuration properties. Specifying units is desirable where possible.

没有单位的数字通常被解释为字节，少数被解释成 KiB 或 MiB. 

尽可能指定单位。

### Dynamically Loading Spark Properties

> In some cases, you may want to avoid hard-coding certain configurations in a `SparkConf`. For instance, if you’d like to run the same application with different masters or different amounts of memory. Spark allows you to simply create an empty conf:

在一些情况下，你可能想要避免在 `SparkConf` 中硬编码特定配置。例如，如果你想在不同的主机下或使用不同的内存量来运行相同的应用程序，那么 Spark 允许你创建一个空的 conf:

	val sc = new SparkContext(new SparkConf())

> Then, you can supply configuration values at runtime:

然后，你就可以在运行时提供配置值：

	./bin/spark-submit --name "My app" --master local[4] --conf spark.eventLog.enabled=false
  	--conf "spark.executor.extraJavaOptions=-XX:+PrintGCDetails -XX:+PrintGCTimeStamps" myApp.jar

> The Spark shell and [spark-submit](https://spark.apache.org/docs/3.3.2/submitting-applications.html) tool support two ways to load configurations dynamically. The first is command line options, such as `--master`, as shown above. `spark-submit` can accept any Spark property using the `--conf/-c` flag, but uses special flags for properties that play a part in launching the Spark application. Running `./bin/spark-submit --help` will show the entire list of these options.

Spark shell 和 spark-submit 工具支持两种动态载入配置的方式。一种是命令行选项，例如 `--master`. `spark-submit` 可以使用 `--conf/-c` 标志接收任意的 Spark 属性，但是对在启动 Spark 应用程序中起作用的属性使用特殊标志。

> `bin/spark-submit` will also read configuration options from `conf/spark-defaults.conf`, in which each line consists of a key and a value separated by whitespace. For example:

`bin/spark-submit` 也将从 `conf/spark-defaults.conf` 中读取配置选项，在这个配置文件中，每一行都是由空格划分的键值对组成。例如：

	spark.master            spark://5.6.7.8:7077
	spark.executor.memory   4g
	spark.eventLog.enabled  true
	spark.serializer        org.apache.spark.serializer.KryoSerializer

> Any values specified as flags or in the properties file will be passed on to the application and merged with those specified through `SparkConf`. Properties set directly on the `SparkConf` take highest precedence, then flags passed to `spark-submit` or `spark-shell`, then options in the `spark-defaults.conf` file. A few configuration keys have been renamed since earlier versions of Spark; in such cases, the older key names are still accepted, but take lower precedence than any instance of the newer key.

任意通过标志指定的值或属性文件中的值都将被传到应用程序，和通过 `SparkConf` 指定的属性值合并。

在 `SparkConf` 中直接指定的属性具有最高优先级，然后是通过 `spark-submit` 或 `spark-shell` 配置的属性值，最后是 `spark-defaults.conf` 文件中的选项。

一些配置的键由于早期版本的原因已被重命名，在这种情况下，旧的键的名字会仍被接受，但比新的名字具有较低优先级。

> Spark properties mainly can be divided into two kinds: one is related to deploy, like “spark.driver.memory”, “spark.executor.instances”, this kind of properties may not be affected when setting programmatically through `SparkConf` in runtime, or the behavior is depending on which cluster manager and deploy mode you choose, so it would be suggested to set through configuration file or `spark-submit` command line options; another is mainly related to Spark runtime control, like “spark.task.maxFailures”, this kind of properties can be set in either way.

Spark 属性可以划分成两类：

一类是和部署相关的，就像 `spark.driver.memory` 和 `spark.executor.instances`, 这类属性在运行时通过 `SparkConf` 设置时不会受到影响，或者说行为取决于你选择的集群管理器和部署模式。所以，建议这类属性通过配置文件或 `spark-submit` 命令行设置。

另一类和 spark 运行时相关，就像 `spark.task.maxFailures`, 这类属性可以用任何一种方式设置。

### Viewing Spark Properties

> The application web UI at `http://<driver>:4040` lists Spark properties in the “Environment” tab. This is a useful place to check to make sure that your properties have been set correctly. Note that only values explicitly specified through `spark-defaults.conf`, SparkConf, or the command line will appear. For all other configuration properties, you can assume the default value is used.

在 `http://<driver>:4040` 展示的应用程序 WEB UI 的　“Environment”　tab 页中列出了 spark 属性。这里可以检查你设置的属性是否正确。

只有通过 `spark-defaults.conf`, SparkConf, 或命令行明确指定的值才会在这里出现。对于所有其他配置属性，你可以假定使用的默认值。

#### Available Properties

> Most of the properties that control internal settings have reasonable default values. Some of the most common options to set are:

大部分控制内部设置的属性都有合理的默认值。大部分的常见选项有：

##### Application Properties

- spark.app.name	

	Default: (none)	
	
	Since Version: 0.9.0
	
	> The name of your application. This will appear in the UI and in log data.	

	你的应用程序的名称。这会出现在 UI 和日志中。

- spark.driver.cores	

	Default: 1	
	
	Since Version: 1.3.0
	
	> Number of cores to use for the driver process, only in cluster mode.	

	驱动进程使用的核心数，仅在集群模式下。

- spark.driver.maxResultSize	

	Default: 1g	
	
	Since Version: 1.2.0
	
	> Limit of total size of serialized results of all partitions for each Spark action (e.g. collect) in bytes. Should be at least 1M, or 0 for unlimited. Jobs will be aborted if the total size is above this limit. Having a high limit may cause out-of-memory errors in driver (depends on `spark.driver.memory` and memory overhead of objects in JVM). Setting a proper limit can protect the driver from out-of-memory errors.	

	对于每个 spark 行动算子，所有分区的序列化结果使用的内存的限制。最小的 1M，0表示无限制。

	如果总的大小超过了限制值，作业将会失败。设置较高的限制可能会造成驱动节点的 OOM 错误（取决于 `spark.driver.memory` 和 JVM 中对象的内存负载）。设置一个合适的限制可以避免驱动节点产生 OOM 错误。

- spark.driver.memory	

	Default: 1g	
	
	Since Version: 1.1.1
	
	> Amount of memory to use for the driver process, i.e. where SparkContext is initialized, in the same format as JVM memory strings with a size unit suffix ("k", "m", "g" or "t") (e.g. 512m, 2g).
	
	驱动进程使用的内存量，例如，在初始化 SparkContext 的位置，和 JVM 内存字符串使用相同的大小单位后缀。

	> Note: In client mode, this config must not be set through the `SparkConf` directly in your application, because the driver JVM has already started at that point. Instead, please set this through the `--driver-memory` command line option or in your default properties file.	

	注意：在客户端模式下，这个配置一定不要通过 `SparkConf` 直接设置，因为驱动 JVM 早已启动。应该通过 `--driver-memory` 命令行选项设置，或者在你的默认属性文件中。


- spark.driver.memoryOverhead	

	Default: driverMemory * spark.driver.memoryOverheadFactor, with minimum of 384	

	Since Version: 2.3.0

	> Amount of non-heap memory to be allocated per driver process in cluster mode, in MiB unless otherwise specified. This is memory that accounts for things like VM overheads, interned strings, other native overheads, etc. This tends to grow with the container size (typically 6-10%). This option is currently supported on YARN, Mesos and Kubernetes. 
	
	在集群模式下，分配给驱动进程的非堆内存的量，除非指定，否则单位是 MiB. 这个值考虑了 VM 负载、interned字符串和其他原生负载等。

	这个值随着容器大小逐渐增长（一般是6-10%）。这个选项当前支持 YARN, Mesos 和 Kubernetes.	

	> Note: Non-heap memory includes off-heap memory (when `spark.memory.offHeap.enabled=true`) and memory used by other driver processes (e.g. python process that goes with a PySpark driver) and memory used by other non-driver processes running in the same container. The maximum memory size of container to running driver is determined by the sum of `spark.driver.memoryOverhead` and `spark.driver.memory`.	

	注意：非堆内存包含堆外内存（当 `spark.memory.offHeap.enabled` 为 true 时），和其他驱动进程使用的内存（如，和 PySpark 驱动一起运行的 python 进程），和其他运行在相同容器中的非驱动进程使用的内存。

	运行驱动的容器的最大内存大小等于 `spark.driver.memoryOverhead` 和 `spark.driver.memory` 的总和。

- spark.driver.memoryOverheadFactor	

	Default: 0.10	

	Since Version: 3.3.0

	> Fraction of driver memory to be allocated as additional non-heap memory per driver process in cluster mode. This is memory that accounts for things like VM overheads, interned strings, other native overheads, etc. This tends to grow with the container size. This value defaults to 0.10 except for Kubernetes non-JVM jobs, which defaults to 0.40. This is done as non-JVM tasks need more non-JVM heap space and such tasks commonly fail with "Memory Overhead Exceeded" errors. This preempts this error with a higher default. This value is ignored if `spark.driver.memoryOverhead` is set directly.	

	在集群模式下，分配的驱动内存的比例，此内存作为每个驱动进程的额外的非堆内存。

	这个内存考虑了 VM 负载、interned字符串和其他原生负载等。这个值随着容器大小逐渐增长。这个值默认是 0.10，而对于 Kubernetes 的非 JVM 作业默认值是 0.40.

	这是因为非 JVM 任务需要更多的非 JVM 堆空间，这种任务一般失败是因为 "Memory Overhead Exceeded" 错误。可以设置一个较高的默认值阻止这个错误出现。

	如果直接设置了 `spark.driver.memoryOverhead` 值，那么这个值就会被忽略。

- spark.driver.resource.{resourceName}.amount	

	Default: 0	
	
	Since Version: 3.0.0
	
	> Amount of a particular resource type to use on the driver. If this is used, you must also specify the `spark.driver.resource.{resourceName}.discoveryScript` for the driver to find the resource on startup.	

	在驱动上使用的特定的资源类型的量。如果这个类型已使用，你必须为驱动指定 `spark.driver.resource.{resourceName}.discoveryScript` 找到该资源。 

- spark.driver.resource.{resourceName}.discoveryScript	

	Default: None	
	
	Since Version: 3.0.0
	
	> A script for the driver to run to discover a particular resource type. This should write to `STDOUT` a JSON string in the format of the ResourceInformation class. This has a name and an array of addresses. For a client-submitted driver, discovery script must assign different resource addresses to this driver comparing to other drivers on the same host.	

	在驱动上运行的脚本，用来发现特定的资源类型。这应该以 ResourceInformation 类的格式向 STDOUT 写入一个 JSON 字符串，它有一个名称和地址的数组。

	对于 client-submitted driver, 发现脚本必须给这个驱动分配不同的资源地址，以区分相同主机上的其他驱动。

- spark.driver.resource.{resourceName}.vendor	

	Default: None	
	
	Since Version: 3.0.0
	
	> Vendor of the resources to use for the driver. This option is currently only supported on Kubernetes and is actually both the vendor and domain following the Kubernetes device plugin naming convention. (e.g. For GPUs on Kubernetes this config would be set to `nvidia.com` or `amd.com`)	

	驱动使用的资源的供应商。当前这个选项仅支持 Kubernetes.

- spark.resources.discoveryPlugin	

	Default: org.apache.spark.resource.ResourceDiscoveryScriptPlugin	
	
	Since Version: 3.0.0
	
	> Comma-separated list of class names implementing `org.apache.spark.api.resource.ResourceDiscoveryPlugin` to load into the application. This is for advanced users to replace the resource discovery class with a custom implementation. Spark will try each class specified until one of them returns the resource information for that resource. It tries the discovery script last if none of the plugins return information for that resource.	

	要载入应用程序的类名的列表，类实现了 `org.apache.spark.api.resource.ResourceDiscoveryPlugin`.

	这是为高级用户用来替代自定义实现的资源发现类。

	spark 将尝试每一个指定的类，直到其中一个类返回此资源的资源信息。

	如果没有一个插件返回资源信息，最后它尝试发现脚本。

- spark.executor.memory	

	Default: 1g	
	
	Since Version: 0.7.0
	
	> Amount of memory to use per executor process, in the same format as JVM memory strings with a size unit suffix ("k", "m", "g" or "t") (e.g. 512m, 2g).	

	每个 executor 进程使用的内存量，格式和 JVM 内存字符串从相同，具有相同的单位后缀。

- spark.executor.pyspark.memory	

	Default: Not set	
	
	Since Version: 2.4.0
	
	> The amount of memory to be allocated to PySpark in each executor, in MiB unless otherwise specified. If set, PySpark memory for an executor will be limited to this amount. If not set, Spark will not limit Python's memory use and it is up to the application to avoid exceeding the overhead memory space shared with other non-JVM processes. When PySpark is run in YARN or Kubernetes, this memory is added to executor resource requests.
	
	在 executor 中，分配给 PySpark 的内存量，默认单位是 MiB. 如果设置了此项，那么 一个 executor 的 PySpark 内存就被限制在这个值。如果不设置，将不会限制 python 使用的内存量，这取决于应用程序如何避免超出与其他非 JVM 进程共享的开销内存空间。当 PySpark 运行在 YARN 和 Kubernetes 上，这个内存就被添加到 executor 的资源请求中。

	> Note: This feature is dependent on Python's `resource` module; therefore, the behaviors and limitations are inherited. For instance, Windows does not support resource limiting and actual resource is not limited on MacOS.	

- spark.executor.memoryOverhead	

	Default: executorMemory * spark.executor.memoryOverheadFactor, with minimum of 384	
	
	Since Version: 2.3.0
	
	> Amount of additional memory to be allocated per executor process, in MiB unless otherwise specified. This is memory that accounts for things like VM overheads, interned strings, other native overheads, etc. This tends to grow with the executor size (typically 6-10%). This option is currently supported on YARN and Kubernetes.
	
	分配给每个 executor 进程的额外的内存量，默认单位是 MiB. 这个值考虑了 VM 负载、interned字符串和其他原生负载等。

	这个值随着 executor 大小逐渐增长（一般是6-10%）。这个选项当前支持 YARN 和 Kubernetes.

	> Note: Additional memory includes PySpark executor memory (when `spark.executor.pyspark.memory` is not configured) and memory used by other non-executor processes running in the same container. The maximum memory size of container to running executor is determined by the sum of `spark.executor.memoryOverhead`, `spark.executor.memory`, `spark.memory.offHeap.size` and `spark.executor.pyspark.memory`.	

	注意：额外的内存包含 PySpark executor 内存（当没有设置 `spark.executor.pyspark.memory` 时）和其他运行在相同容器中的非 executor 进程使用的内存。

	运行 executor 的最大内存大小取决于 `spark.executor.memoryOverhead`, `spark.executor.memory`, `spark.memory.offHeap.size` 和 `spark.executor.pyspark.memory` 的和。

- spark.executor.memoryOverheadFactor	

	Default: 0.10	
	
	Since Version: 3.3.0
	
	> Fraction of executor memory to be allocated as additional non-heap memory per executor process. This is memory that accounts for things like VM overheads, interned strings, other native overheads, etc. This tends to grow with the container size. This value defaults to 0.10 except for Kubernetes non-JVM jobs, which defaults to 0.40. This is done as non-JVM tasks need more non-JVM heap space and such tasks commonly fail with "Memory Overhead Exceeded" errors. This preempts this error with a higher default. This value is ignored if `spark.executor.memoryOverhead` is set directly.	

	分配给每个 executor 进程、作为额外的非堆内存的内存。这个内存考虑了 VM 负载、interned字符串和其他原生负载等。这个值随着容器大小逐渐增长。这个值默认是 0.10，而对于 Kubernetes 的非 JVM 作业默认值是 0.40.

	这是因为非 JVM 任务需要更多的非 JVM 堆空间，这种任务一般失败是因为 “Memory Overhead Exceeded” 错误。可以设置一个较高的默认值阻止这个错误出现。

	如果直接设置了 `spark.executor.memoryOverhead` 值，那么这个值就会被忽略。

- spark.executor.resource.{resourceName}.amount	

	Default: 0	
	
	Since Version: 3.0.0
	
	> Amount of a particular resource type to use per executor process. If this is used, you must also specify the `spark.executor.resource.{resourceName}.discoveryScript` for the executor to find the resource on startup.	

	每个 executor 进程使用的特定的资源类型的量。如果使用了此项，必须为 executor 指定 `spark.executor.resource.{resourceName}.discoveryScript` 以找到该资源。

- spark.executor.resource.{resourceName}.discoveryScript	

	Default: None	
	
	Since Version: 3.0.0
	
	> A script for the executor to run to discover a particular resource type. This should write to STDOUT a JSON string in the format of the ResourceInformation class. This has a name and an array of addresses.	

	在 executor 上运行的脚本，用来发现特定的资源类型。这应该以 ResourceInformation 类的格式向 STDOUT 写入一个 JSON 字符串，它有一个名称和地址的数组。

- spark.executor.resource.{resourceName}.vendor	

	Default: None	
	
	Since Version: 3.0.0
	
	> Vendor of the resources to use for the executors. This option is currently only supported on Kubernetes and is actually both the vendor and domain following the Kubernetes device plugin naming convention. (e.g. For GPUs on Kubernetes this config would be set to nvidia.com or amd.com)	

	executors 使用的资源供应商。当前这个选项仅支持 Kubernetes.

- spark.extraListeners	

	Default: (none)	
	
	Since Version: 1.3.0
	
	> A comma-separated list of classes that implement SparkListener; when initializing SparkContext, instances of these classes will be created and registered with Spark's listener bus. If a class has a single-argument constructor that accepts a SparkConf, that constructor will be called; otherwise, a zero-argument constructor will be called. If no valid constructor can be found, the SparkContext creation will fail with an exception.	

	实现了 SparkListener 的类的列表，用逗号分隔。在初始化 SparkContext 时，将创建这些类的示例，向 Spark listener bus 注册。	

	如果一个类有一个 SparkConf 参数的构造函数，那么将调用这个构造器，否则就调用无参构造。如果没有找到有效的构造函数，那么 SparkContext 将抛出异常，创建失败。

- spark.local.dir	

	Default: `/tmp`	
	
	Since Version: 0.5.0
	
	> Directory to use for "scratch" space in Spark, including map output files and RDDs that get stored on disk. This should be on a fast, local disk in your system. It can also be a comma-separated list of multiple directories on different disks.

	spark 中暂存空间的目录，包括 map 输出文件、存储在磁盘上的 RDDs. 这应该在系统的一个快速的、本地磁盘上。它也可以是不同磁盘上的多个目录组成的逗号分隔的列表。

	> Note: This will be overridden by `SPARK_LOCAL_DIRS` (Standalone), `MESOS_SANDBOX` (Mesos) or `LOCAL_DIRS` (YARN) environment variables set by the cluster manager.	

	注意：在不同的集群管理器下，会被不同参数覆盖。

- spark.logConf	

	Default: false	
	
	Since Version: 0.9.0
	
	> Logs the effective SparkConf as INFO when a SparkContext is started.	

	在启动 SparkContext 时，将有效的 SparkConf 记录成 INFO.

- spark.master	

	Default: (none)	
	
	Since Version: 0.9.0
	
	> The cluster manager to connect to. See the list of [allowed master URL's](https://spark.apache.org/docs/3.3.2/submitting-applications.html#master-urls).	

	连接的集群管理器。

- spark.submit.deployMode	

	Default: (none)	
	
	Since Version: 1.5.0
	
	> The deploy mode of Spark driver program, either "client" or "cluster", Which means to launch driver program locally ("client") or remotely ("cluster") on one of the nodes inside the cluster.	

	spark 驱动程序的部署模式，要么是 client, 要么是 cluster, 这表示驱动程序是本地启用还是集群节点上远程启动。

- spark.log.callerContext	

	Default: (none)	
	
	Since Version: 2.2.0
	
	> Application information that will be written into Yarn RM log/HDFS audit log when running on Yarn/HDFS. Its length depends on the Hadoop configuration `hadoop.caller.context.max.size`. It should be concise, and typically can have up to 50 characters.

	当运行在 Yarn/HDFS 上时，将被写到 Yarn RM log/HDFS audit log 中的应用程序信息。它的长度取决于 `hadoop.caller.context.max.size` hadoop 配置。它应该是简洁的，不超过给50个字符。

- spark.driver.supervise	

	Default: false	
	
	Since Version: 1.3.0
	
	> If true, restarts the driver automatically if it fails with a non-zero exit status. Only has effect in Spark standalone mode or Mesos cluster deploy mode.	

	如果设为 true, 在驱动以 non-zero exit 错误启动失败时，会自动重启驱动。仅在独立模式或 Mesos 集群模式下有效。

- spark.driver.log.dfsDir	

	Default: (none)	
	
	Since Version: 3.0.0
	
	> Base directory in which Spark driver logs are synced, if `spark.driver.log.persistToDfs.enabled` is true. Within this base directory, each application logs the driver logs to an application specific file. Users may want to set this to a unified location like an HDFS directory so driver log files can be persisted for later usage. This directory should allow any Spark user to read/write files and the Spark History Server user to delete files. Additionally, older logs from this directory are cleaned by the Spark History Server if `spark.history.fs.driverlog.cleaner.enabled` is true and, if they are older than max age configured by setting `spark.history.fs.driverlog.cleaner.maxAge`.	

	如果将 `spark.driver.log.persistToDfs.enabled` 设为 true, 同步 Spark 驱动日志的基础目录。在这个基础目录下，每个应用程序将驱动日志记录到应用程序的特定文件中。

	用户可能想将这个参数设为一个统一的位置，就像在一个 HDFS 目录下，这样的话，就可以持久化驱动日志。

	这个目录应该允许任意的 spark 用户读写其中的文件，并且 Spark History Server 用户可以删除文件。

	如果 `spark.history.fs.driverlog.cleaner.enabled` 设为 true, 同时旧的日志的时间比 `spark.history.fs.driverlog.cleaner.maxAge` 还大，那么 Spark History Server 会清理这个目录下的这类日志。

- spark.driver.log.persistToDfs.enabled	

	Default: false	
	
	Since Version: 3.0.0
	
	> If true, spark application running in client mode will write driver logs to a persistent storage, configured in `spark.driver.log.dfsDir`. If `spark.driver.log.dfsDir` is not configured, driver logs will not be persisted. Additionally, enable the cleaner by setting `spark.history.fs.driverlog.cleaner.enabled` to true in Spark History Server.	
	如果设为 true, 运行在客户端模式下的 spark 应用程序将驱动日志写到一个持久化的存储中，此存储由 `spark.driver.log.dfsDir` 配置。如果没有设置 `spark.driver.log.dfsDir`, 驱动日志将不会被持久化。

	另外，通过在 Spark History Server 中设置 `spark.history.fs.driverlog.cleaner.enabled` 为 true 来启动清理器。

- spark.driver.log.layout	

	Default: %d{yy/MM/dd HH:mm:ss.SSS} %t %p %c{1}: %m%n%ex	
	
	Since Version: 3.0.0
	
	> The layout for the driver logs that are synced to `spark.driver.log.dfsDir`. If this is not configured, it uses the layout for the first appender defined in log4j2.properties. If that is also not configured, driver logs use the default layout.	



- spark.driver.log.allowErasureCoding	

	Default: false	
	
	Since Version: 3.0.0

	> Whether to allow driver logs to use erasure coding. On HDFS, erasure coded files will not update as quickly as regular replicated files, so they make take longer to reflect changes written by the application. Note that even if this is true, Spark will still not force the file to use erasure coding, it will simply use file system defaults.	

	是否允许驱动日志使用纠删码。在 HDFS 中，纠删码文件不会想常规的副本文件一样更新，所以它们会使用更长时间显示出来应用程序写入的更改。

	注意，即使这个是 true, Spark 仍不会强制文件使用纠删码，它将简单地使用文件系统默认值。

> Apart from these, the following properties are also available, and may be useful in some situations:

除了这些属性，下面的属性也是可用的，在一些情况下也是有用的：

##### Runtime Environment

- spark.driver.extraClassPath	

	Default: (none)	

	Since Version: 1.0.0

	> Extra classpath entries to prepend to the classpath of the driver.

	追加到驱动的类路径前面的额外类路径项。

	> Note: In client mode, this config must not be set through the SparkConf directly in your application, because the driver JVM has already started at that point. Instead, please set this through the `--driver-class-path` command line option or in your default properties file.	

	注意：在客户端模式下，一定不要在应用程序的 SparkConf 直接配置，因为在那时，驱动的 JVM 早已启动。而要通过 `--driver-class-path` 命令行选项设置，或在你的默认的属性文件中设置。

- spark.driver.defaultJavaOptions	

	Default: (none)	

	Since Version: 3.0.0

	> A string of default JVM options to prepend to `spark.driver.extraJavaOptions`. This is intended to be set by administrators. For instance, GC settings or other logging. Note that it is illegal to set maximum heap size (-Xmx) settings with this option. Maximum heap size settings can be set with `spark.driver.memory` in the cluster mode and through the `--driver-memory` command line option in the client mode.

	在 `spark.driver.extraJavaOptions` 前面追加的默认 JVM 选项的字符串。这要通过管理员设置，例如，垃圾回收设置或其他的日志记录。

	注意，使用这个选项设置最大的堆大小是非法的。在集群模式下，应该通过 `spark.driver.memory` 设置，在客户端模式下，通过 `--driver-memory` 命令行设置。

	> Note: In client mode, this config must not be set through the SparkConf directly in your application, because the driver JVM has already started at that point. Instead, please set this through the `--driver-java-options` command line option or in your default properties file.	

	注意：在客户端模式下，一定不要在应用程序的 SparkConf 直接配置，因为在那时，驱动的 JVM 早已启动。而要通过 `--driver-java-options` 命令行选项设置，或在你的默认的属性文件中设置。	

- spark.driver.extraJavaOptions	

	Default: (none)	

	Since Version: 1.0.0

	> A string of extra JVM options to pass to the driver. This is intended to be set by users. For instance, GC settings or other logging. Note that it is illegal to set maximum heap size (-Xmx) settings with this option. Maximum heap size settings can be set with `spark.driver.memory` in the cluster mode and through the `--driver-memory` command line option in the client mode.

	传给驱动的额外的 JVM 选项。这由用户设置。例如，垃圾回收设置或其他的日志记录。

	注意，使用这个选项设置最大的堆大小是非法的。在集群模式下，应该通过 `spark.driver.memory` 设置，在客户端模式下，通过 `--driver-memory` 命令行设置。

	> Note: In client mode, this config must not be set through the SparkConf directly in your application, because the driver JVM has already started at that point. Instead, please set this through the `--driver-java-options` command line option or in your default properties file. `spark.driver.defaultJavaOptions` will be prepended to this configuration.	

	注意：在客户端模式下，一定不要在应用程序的 SparkConf 直接配置，因为在那时，驱动的 JVM 早已启动。而要通过 `--driver-java-options` 命令行选项设置，或在你的默认的属性文件中设置。 配置 `spark.driver.defaultJavaOptions` 将在这个配置前面追加。

- spark.driver.extraLibraryPath	

	Default: (none)	

	Since Version: 1.0.0

	> Set a special library path to use when launching the driver JVM.

	当启动驱动 JVM 时，设置使用的库路径。

	> Note: In client mode, this config must not be set through the SparkConf directly in your application, because the driver JVM has already started at that point. Instead, please set this through the `--driver-library-path` command line option or in your default properties file.	

	注意：在客户端模式下，一定不要在应用程序的 SparkConf 直接配置，因为在那时，驱动的 JVM 早已启动。而要通过 `--driver-library-path` 命令行选项设置，或在你的默认的属性文件中设置。 

- spark.driver.userClassPathFirst	

	Default: false	

	Since Version: 1.3.0

	> (Experimental) Whether to give user-added jars precedence over Spark's own jars when loading classes in the driver. This feature can be used to mitigate conflicts between Spark's dependencies and user dependencies. It is currently an experimental feature. This is used in cluster mode only.	

	（实验）当在驱动中加载类时，是否让用户添加的 jar 优先于 spark 自己的 jar. 这个特性可用用于减轻 spark 的依赖和用户依赖键的矛盾。当前是一个实验性的特性。仅在集群模式下使用。

- spark.executor.extraClassPath	

	Default: (none)	

	Since Version: 1.0.0

	> Extra classpath entries to prepend to the classpath of executors. This exists primarily for backwards-compatibility with older versions of Spark. Users typically should not need to set this option.	

	追加到 executors 的类路径前面的额外类路径项。这个配置存在是为了向后兼容旧的 spark 版本。用户不用设置此项。

- spark.executor.defaultJavaOptions	

	Default: (none)	

	Since Version: 3.0.0

	> A string of default JVM options to prepend to `spark.executor.extraJavaOptions`. This is intended to be set by administrators. For instance, GC settings or other logging. Note that it is illegal to set Spark properties or maximum heap size (-Xmx) settings with this option. Spark properties should be set using a SparkConf object or the `spark-defaults.conf` file used with the spark-submit script. Maximum heap size settings can be set with `spark.executor.memory`. The following symbols, if present will be interpolated: will be replaced by application ID and will be replaced by executor ID. For example, to enable verbose gc logging to a file named for the executor ID of the app in `/tmp`, pass a 'value' of: `-verbose:gc -Xloggc:/tmp/-.gc`	

	在 `spark.executor.extraJavaOptions` 前面追加的默认 JVM 选项的字符串。这要通过管理员设置，例如，垃圾回收设置或其他的日志记录。

	注意，使用这个选项设置最大的堆大小是非法的。在集群模式下，应该通过 `spark.driver.memory` 设置，在客户端模式下，通过 `--driver-memory` 命令行设置。

	spark 属性应该使用 SparkConf 对象设置，或者在 spark-submit 脚本中使用 `spark-defaults.conf` 文件设置。最大堆大小通过 `spark.executor.memory` 设置。

	The following symbols, if present will be interpolated: will be replaced by application ID and will be replaced by executor ID. For example, to enable verbose gc logging to a file named for the executor ID of the app in `/tmp`, pass a 'value' of: `-verbose:gc -Xloggc:/tmp/-.gc`

- spark.executor.extraJavaOptions	

	Default: (none)	

	Since Version: 1.0.0

	> A string of extra JVM options to pass to executors. This is intended to be set by users. For instance, GC settings or other logging. Note that it is illegal to set Spark properties or maximum heap size (-Xmx) settings with this option. Spark properties should be set using a SparkConf object or the `spark-defaults.conf` file used with the spark-submit script. Maximum heap size settings can be set with `spark.executor.memory`. The following symbols, if present will be interpolated: will be replaced by application ID and will be replaced by executor ID. For example, to enable verbose gc logging to a file named for the executor ID of the app in `/tmp`, pass a 'value' of: `-verbose:gc -Xloggc:/tmp/-.gc` 
	
	> `spark.executor.defaultJavaOptions` will be prepended to this configuration.	

	传给 executors 的额外的 JVM 选项。这由用户设置。例如，垃圾回收设置或其他的日志记录。

	注意，使用这个选项设置最大的堆大小是非法的。 spark 属性应该使用 SparkConf 对象设置，或者在 spark-submit 脚本中使用 `spark-defaults.conf` 文件设置。最大堆大小通过 `spark.executor.memory` 设置。

	`spark.executor.defaultJavaOptions` 配置将在这个配置前面追加。

- spark.executor.extraLibraryPath	

	Default: (none)	

	Since Version: 1.0.0

	> Set a special library path to use when launching executor JVM's.	

	当启动 executor JVM 时，设置使用的库路径。

- spark.executor.logs.rolling.maxRetainedFiles	

	Default: (none)	

	Since Version: 1.1.0

	> Sets the number of latest rolling log files that are going to be retained by the system. Older log files will be deleted. Disabled by default.	

	设置系统将保留的最近的滚动日志文件的数量。旧的日志文件将被删除。默认禁用。

- spark.executor.logs.rolling.enableCompression	

	Default: false	

	Since Version: 2.0.2

	> Enable executor log compression. If it is enabled, the rolled executor logs will be compressed. Disabled by default.	

	启用 executor 日志压缩。如果启用了，滚动的 executor 日志文件将被压缩。默认禁用。

- spark.executor.logs.rolling.maxSize	

	Default: (none)	

	Since Version: 1.4.0

	> Set the max size of the file in bytes by which the executor logs will be rolled over. Rolling is disabled by default. See `spark.executor.logs.rolling.maxRetainedFiles` for automatic cleaning of old logs.	

	将滚动的 executor 日志文件的最大大小。默认是禁用滚动的。

- spark.executor.logs.rolling.strategy	

	Default: (none)	

	Since Version: 1.1.0

	> Set the strategy of rolling of executor logs. By default it is disabled. It can be set to "time" (time-based rolling) or "size" (size-based rolling). For "time", use `spark.executor.logs.rolling.time.interval` to set the rolling interval. For "size", use `spark.executor.logs.rolling.maxSize` to set the maximum file size for rolling.	

	设置 executor 日志文件滚动的策略。默认是禁用的。

	可以设置为 time 或者 size.

- spark.executor.logs.rolling.time.interval	

	Default: daily	

	Since Version: 1.1.0

	> Set the time interval by which the executor logs will be rolled over. Rolling is disabled by default. Valid values are daily, hourly, minutely or any interval in seconds. See `spark.executor.logs.rolling.maxRetainedFiles` for automatic cleaning of old logs.	

	滚动 executor 日志文件的时间间隔。默认是禁用滚动的。

	有效值有: daily, hourly, minutely, 和任意以秒为单位的间隔。

- spark.executor.userClassPathFirst	

	Default: false	

	Since Version: 1.3.0

	> (Experimental) Same functionality as `spark.driver.userClassPathFirst`, but applied to executor instances.	

	应用于 executor 实例

- spark.executorEnv.[EnvironmentVariableName]	

	Default: (none)	

	Since Version: 0.9.0

	> Add the environment variable specified by EnvironmentVariableName to the Executor process. The user can specify multiple of these to set multiple environment variables.	

	将 EnvironmentVariableName 指定的环境变量添加到 Executor 进程。用户可以指定多个环境变量。

- spark.redaction.regex	

	Default: (?i)secret|password|token	

	Since Version: 2.1.2

	> Regex to decide which Spark configuration properties and environment variables in driver and executor environments contain sensitive information. When this regex matches a property key or value, the value is redacted from the environment UI and various logs like YARN and event logs.	

	决定了 driver 和 executor 环境中的哪个 spark 配置属性和环境变量包含敏感信息的正则表达式。当这个正则表达式匹配到一个属性的键或值时，这个值会被修改。

- spark.python.profile	

	Default: false	

	Since Version: 1.2.0

	> Enable profiling in Python worker, the profile result will show up by `sc.show_profiles()`, or it will be displayed before the driver exits. It also can be dumped into disk by `sc.dump_profiles(path)`. If some of the profile results had been displayed manually, they will not be displayed automatically before driver exiting. By default the `pyspark.profiler.BasicProfiler` will be used, but this can be overridden by passing a profiler class in as a parameter to the SparkContext constructor.	



- spark.python.profile.dump	

	Default: (none)	

	Since Version: 1.2.0

	> The directory which is used to dump the profile result before driver exiting. The results will be dumped as separated file for each RDD. They can be loaded by `pstats.Stats()`. If this is specified, the profile result will not be displayed automatically.	

- spark.python.worker.memory	

	Default: 512m	

	Since Version: 1.1.0

	> Amount of memory to use per python worker process during aggregation, in the same format as JVM memory strings with a size unit suffix ("k", "m", "g" or "t") (e.g. 512m, 2g). If the memory used during aggregation goes above this amount, it will spill the data into disks.	

	在聚合期间，每个 python worker 使用的内存量。格式和 JVM 内存字符串相同，单位后缀也相同。如果在聚合期间，使用的内存超过了这个值，数据将会溢写到磁盘。

- spark.python.worker.reuse	

	Default: true	

	Since Version: 1.2.0

	> Reuse Python worker or not. If yes, it will use a fixed number of Python workers, does not need to `fork()` a Python process for every task. It will be very useful if there is a large broadcast, then the broadcast will not need to be transferred from JVM to Python worker for every task.	

	是否重复使用 python worker. 如果是，将使用固定数量的 python worker, 不需要为每个任务 fork 一个 python 进程。如果存在一个非常大的广播变量，那么这将非常有用，那么不需要为每个任务将这个广播变量从 JVM 传输到 python worker.

- spark.files		

	Since Version: 1.0.0

	> Comma-separated list of files to be placed in the working directory of each executor. Globs are allowed.	

	放置在每个 executor 的工作目录中的文件列表。允许使用通配符。

- spark.submit.pyFiles		

	Since Version: 1.0.1

	> Comma-separated list of `.zip`, `.egg`, or `.py` files to place on the `PYTHONPATH` for Python apps. Globs are allowed.	

	为每个 python 应用程序，放置在 `PYTHONPATH` 路径下的 `.zip`, `.egg` 或 `.py` 文件列表。允许使用通配符。

- spark.jars		

	Since Version: 0.9.0

	> Comma-separated list of jars to include on the driver and executor classpaths. Globs are allowed.	

	包含在驱动和 executor 类路径下的 jars 的列表。允许使用通配符。

- spark.jars.packages		

	Since Version: 1.5.0

	> Comma-separated list of Maven coordinates of jars to include on the driver and executor classpaths. The coordinates should be `groupId:artifactId:version`. If `spark.jars.ivySettings` is given artifacts will be resolved according to the configuration in the file, otherwise artifacts will be searched for in the local maven repo, then maven central and finally any additional remote repositories given by the command-line option `--repositories`. For more details, see [Advanced Dependency Management](https://spark.apache.org/docs/3.3.2/submitting-applications.html#advanced-dependency-management).	

	包含在驱动和 executor 类路径下的 jars 的 maven 坐标的列表。坐标的格式是 `groupId:artifactId:version`. 如果设置了 `spark.jars.ivySettings` 配置，那么将根据文件中的配置解析 artifacts, 否则将在本地的 maven 仓库中搜索 artifacts, 然后 maven central 和远程存储库通过 `--repositories` 命令行给出。

- spark.jars.excludes		

	Since Version: 1.5.0

	> Comma-separated list of `groupId:artifactId`, to exclude while resolving the dependencies provided in `spark.jars.packages` to avoid dependency conflicts.	

	`groupId:artifactId` 列表，用来在解析 `spark.jars.packages` 提供的依赖时避免依赖冲突。

- spark.jars.ivy		

	Since Version: 1.3.0

	> Path to specify the Ivy user directory, used for the local Ivy cache and package files from `spark.jars.packages`. This will override the Ivy property `ivy.default.ivy.user.dir` which defaults to `~/.ivy2`.	

	指定的 Ivy 用户目录，用于本地 Ivy 缓存和来自 `spark.jars.packages` 的打包文件。这将覆盖 Ivy 属性 `ivy.default.ivy.user.dir`, 其默认值是 `~/.ivy2`.

- spark.jars.ivySettings		

	Since Version: 2.2.0

	> Path to an Ivy settings file to customize resolution of jars specified using `spark.jars.packages` instead of the built-in defaults, such as maven central. Additional repositories given by the command-line option `--repositories` or `spark.jars.repositories` will also be included. Useful for allowing Spark to resolve artifacts from behind a firewall e.g. via an in-house artifact server like Artifactory. Details on the settings file format can be found at [Settings Files](http://ant.apache.org/ivy/history/latest-milestone/settings.html). Only paths with `file://` scheme are supported. Paths without a scheme are assumed to have a `file://` scheme.

	使用 `spark.jars.packages` 指定的自定义解析 jars 的 Ivy 设置文件的路径。通过 `--repositories` 或 `spark.jars.repositories` 给定的额外的存储库也将被包含。用于允许 Spark 从防火墙后面解析 artifacts. 

	仅支持 `file://` 结构的路径。没有这个结构的路径将默认使用 `file://`.

	> When running in YARN cluster mode, this file will also be localized to the remote driver for dependency resolution within `SparkContext#addJar`

	当在 YARN 集群模式下运行时，该文件也将被本地化到远程驱动中，以便在 SparkContext#addJar 中进行依赖解析。

- spark.jars.repositories		

	Since Version: 2.3.0

	> Comma-separated list of additional remote repositories to search for the maven coordinates given with `--packages` or `spark.jars.packages`.	

	额外的远程存储库的列表，用于搜索通过 `--repositories` 或 `spark.jars.repositories` 给定的 maven 坐标。

- spark.archives		

	Since Version: 3.1.0

	> Comma-separated list of archives to be extracted into the working directory of each executor. `.jar`, `.tar.gz`, `.tgz` and `.zip` are supported. You can specify the directory name to unpack via adding # after the file name to unpack, for example, `file.zip#directory`. This configuration is experimental.	

	将被抽取到每个 executor 的工作目录的归档文件的列表。支持 `.jar`, `.tar.gz`, `.tgz` and `.zip` 文件。

	你可以在文件名后添加 # 表示文件将被解压到这个目录下。

	这个配置是实验性的。

- spark.pyspark.driver.python		

	Since Version: 2.1.0

	> Python binary executable to use for PySpark in driver. (default is `spark.pyspark.python`)	

	在驱动里，PySpark 使用的 python 二进制可执行文件。

- spark.pyspark.python		

	Since Version: 2.1.0

	> Python binary executable to use for PySpark in both driver and executors.	

	在驱动和 executors 里，PySpark 使用的 python 二进制可执行文件。

##### Shuffle Behavior

- spark.reducer.maxSizeInFlight	

	Default: 48m	

	Since Version: 1.4.0

	> Maximum size of map outputs to fetch simultaneously from each reduce task, in MiB unless otherwise specified. Since each output requires us to create a buffer to receive it, this represents a fixed memory overhead per reduce task, so keep it small unless you have a large amount of memory.	

	同时从每个 reduce 任务获取的 map 输出的最大大小。

	由于每个输出要求创建一个缓存来接收它，这就表示每个 reduce 任务都要有一个固定的内存开销，所以除非你有很大的内存，狗则旧要设置一个小值。

- spark.reducer.maxReqsInFlight	

	Default: Int.MaxValue	

	Since Version: 2.0.0

	> This configuration limits the number of remote requests to fetch blocks at any given point. When the number of hosts in the cluster increase, it might lead to very large number of inbound connections to one or more nodes, causing the workers to fail under load. By allowing it to limit the number of fetch requests, this scenario can be mitigated.	

	限制从任意给定点获取块的远程请求的数量。当集群中主机的数量增加，它可能会导致发送到一个或多个节点的连接数量增加，造成 worker 失败。

	通过允许限制获取请求的数量，这种场景可以被减轻。

- spark.reducer.maxBlocksInFlightPerAddress	

	Default: Int.MaxValue	

	Since Version: 2.2.1

	> This configuration limits the number of remote blocks being fetched per reduce task from a given host port. When a large number of blocks are being requested from a given address in a single fetch or simultaneously, this could crash the serving executor or Node Manager. This is especially useful to reduce the load on the Node Manager when external shuffle is enabled. You can mitigate this issue by setting it to a lower value.	

	限制了每个 reduce 任务从一个给定的主机端口中获取远程块的数量。当在一次获取中或同时从给定地址请求大量的块时，这可能会服务 executor 或 Node Manager 宕机。

	当启用了 external shuffle 时，减少 Node Manager 上的载入是非常有用的。你可以通过将其设置为一个较低的值减轻这个问题。

- spark.shuffle.compress	

	Default: true	

	Since Version: 0.6.0

	> Whether to compress map output files. Generally a good idea. Compression will use `spark.io.compression.codec`.	

	是否压缩 map 输出文件。通常来说是一个好主意。将使用 `spark.io.compression.codec` 指定的压缩。

- spark.shuffle.file.buffer	

	Default: 32k	

	Since Version: 1.4.0

	> Size of the in-memory buffer for each shuffle file output stream, in KiB unless otherwise specified. These buffers reduce the number of disk seeks and system calls made in creating intermediate shuffle files.	

	对于每个 shuffle 文件输出流，内存中缓存的大小。这些缓存减少了磁盘寻址的次数，在创建中间 shuffle 文件中进行系统调用。

- spark.shuffle.io.maxRetries	

	Default: 3	

	Since Version: 1.2.0

	> (Netty only) Fetches that fail due to IO-related exceptions are automatically retried if this is set to a non-zero value. This retry logic helps stabilize large shuffles in the face of long GC pauses or transient network connectivity issues.	

	如果将其设为一个非0值，由于 IO 相关的异常产生的失败的获取将自动重试。在面对长时间的 GC 暂停或短暂的网络连接问题时，这个重试逻辑帮助稳定大型 shuffles.

- spark.shuffle.io.numConnectionsPerPeer	

	Default: 1	

	Since Version: 1.2.1

	> (Netty only) Connections between hosts are reused in order to reduce connection buildup for large clusters. For clusters with many hard disks and few hosts, this may result in insufficient concurrency to saturate all disks, and so users may consider increasing this value.	

	对于大型集群，为了减少连接构建，重复使用主机间的连接。对于具有大量硬盘和少量主机的集群，这可能导致并发性不足，无法使所有磁盘饱和，所以用户可能考虑增大这个值。

- spark.shuffle.io.preferDirectBufs	

	Default: true	

	Since Version: 1.2.0

	> (Netty only) Off-heap buffers are used to reduce garbage collection during shuffle and cache block transfer. For environments where off-heap memory is tightly limited, users may wish to turn this off to force all allocations from Netty to be on-heap.	

	堆外缓存用于减少 shuffle 和缓存块传输期间的垃圾回收。对于堆外内存有限的情况下，用户可能希望关闭它，来强制 netty 上的所有分配都在堆上。

- spark.shuffle.io.retryWait	

	Default: 5s	

	Since Version: 1.2.1

	> (Netty only) How long to wait between retries of fetches. The maximum delay caused by retrying is 15 seconds by default, calculated as `maxRetries * retryWait`.	

	在获取尝试之间等待的时间。重试造成的最大的延迟默认是15秒，等于 `maxRetries * retryWait` 

- spark.shuffle.io.backLog	

	Default: -1	

	Since Version: 1.1.1

	> Length of the accept queue for the shuffle service. For large applications, this value may need to be increased, so that incoming connections are not dropped if the service cannot keep up with a large number of connections arriving in a short period of time. This needs to be configured wherever the shuffle service itself is running, which may be outside of the application (see `spark.shuffle.service.enabled` option below). If set below 1, will fallback to OS default defined by Netty's `io.netty.util.NetUtil#SOMAXCONN`.	

	对于 shuffle 服务，接受的队列长度。对于大型应用程序，这个值可能需要增加，为了在短期内，服务不能跟上大量到来的连接时，传来的连接不被删除。

	这需要在运行 shuffle 服务本身的任何地方进行配置，这可能在应用程序之外。如果设的值低于1，那么将回退到由 Netty `io.netty.util.NetUtil#SOMAXCONN` 定义的系统默认值。

- spark.shuffle.io.connectionTimeout	

	Default: value of spark.network.timeout	

	Since Version: 1.2.0

	> Timeout for the established connections between shuffle servers and clients to be marked as idled and closed if there are still outstanding fetch requests but no traffic no the channel for at least `connectionTimeout`.	

	在 shuffle 服务和客户端之间建立的连接被标记为空闲，如果仍有未完成的获取请求，但在 `connectionTimeout` 时间内仍没有满足，就会关闭连接。

- spark.shuffle.service.enabled	

	Default: false	

	Since Version: 1.2.0

	> Enables the external shuffle service. This service preserves the shuffle files written by executors e.g. so that executors can be safely removed, or so that shuffle fetches can continue in the event of executor failure. The external shuffle service must be set up in order to enable it. See [dynamic allocation configuration and setup documentation](https://spark.apache.org/docs/3.3.2/job-scheduling.html#configuration-and-setup) for more information.	

	启用 external shuffle 服务。

	这个服务会保留 executors 写入的 shuffle 文件，为了能安全移除 executors, 或者在 executors 失败事件中，能够继续 shuffle 获取。要启用它，必须设置 external shuffle 服务。

- spark.shuffle.service.port	

	Default: 7337	

	Since Version: 1.2.0

	> Port on which the external shuffle service will run.	

	external shuffle 服务运行的端口

- spark.shuffle.service.index.cache.size	

	Default: 100m	

	Since Version: 2.3.0

	> Cache entries limited to the specified memory footprint, in bytes unless otherwise specified.	

	仅限于指定的内存占用空间的缓存项。

- spark.shuffle.service.removeShuffle	

	Default: false	

	Since Version: 3.3.0

	> Whether to use the ExternalShuffleService for deleting shuffle blocks for deallocated executors when the shuffle is no longer needed. Without this enabled, shuffle data on executors that are deallocated will remain on disk until the application ends.	

	当不再需要 shuffle 时，为了释放 executors，是否使用 ExternalShuffleService 删除 shuffle 块。

	不启用此项时，等待释放的 executors 上的 shuffle 数据将保留在磁盘上，直至应用程序结束。

- spark.shuffle.maxChunksBeingTransferred	

	Default: Long.MAX_VALUE	

	Since Version: 2.3.0

	> The max number of chunks allowed to be transferred at the same time on shuffle service. Note that new incoming connections will be closed when the max number is hit. The client will retry according to the shuffle retry configs (see `spark.shuffle.io.maxRetries` and `spark.shuffle.io.retryWait`), if those limits are reached the task will fail with fetch failure.	

	在 shuffle 服务中，允许同时传输的块的最大数量。

	注意，当达到最大数量时，将关闭新传来的连接。客户端将根据 shuffle 重试配置重试，如果达到了这些限制，任务将获取失败。

- spark.shuffle.sort.bypassMergeThreshold	

	Default: 200	

	Since Version: 1.1.1

	> (Advanced) In the sort-based shuffle manager, avoid merge-sorting data if there is no map-side aggregation and there are at most this many reduce partitions.	

	在 sort-based shuffle manager 中，如果没有 map 端聚合，就可以避免合并-排序数据，最多有这么多个 reduce 分区。

- spark.shuffle.spill.compress	

	Default: true	

	Since Version: 0.9.0

	> Whether to compress data spilled during shuffles. Compression will use `spark.io.compression.codec`.	

	在 shuffle 期间，是否压缩溢写的数据。将使用 `spark.io.compression.codec` 配置的压缩类型。

- spark.shuffle.accurateBlockThreshold	

	Default: 100 * 1024 * 1024	

	Since Version: 2.2.1

	> Threshold in bytes above which the size of shuffle blocks in HighlyCompressedMapStatus is accurately recorded. This helps to prevent OOM by avoiding underestimating shuffle block size when fetch shuffle blocks.	

	超过该阈值，HighlyCompressedMapStatus 中 shuffle 块的大小将被准确记录。

	这可以在获取 shuffle 块时，通过避免低估了 shuffle 块的大小而造成的 OOM.

- spark.shuffle.registration.timeout	

	Default: 5000	

	Since Version: 2.3.0

	> Timeout in milliseconds for registration to the external shuffle service.	

	注册 external shuffle 服务的超时时间。

- spark.shuffle.registration.maxAttempts	

	Default: 3	

	Since Version: 2.3.0

	> When we fail to register to the external shuffle service, we will retry for maxAttempts times.	

	当注册 external shuffle 服务失败时，将重试 maxAttempts 次数。

- spark.files.io.connectionTimeout	

	Default: value of spark.network.timeout	

	Since Version: 1.6.0

	> Timeout for the established connections for fetching files in Spark RPC environments to be marked as idled and closed if there are still outstanding files being downloaded but no traffic no the channel for at least `connectionTimeout`.	

	在 spark PRC 环境中获取文件而建立的连接被标记为空闲时，如果仍有正在下载的文件，但在 `connectionTimeout` 时间内仍没有完成，就会关闭连接。

- spark.shuffle.checksum.enabled	

	Default: true	

	Since Version: 3.2.0

	> Whether to calculate the checksum of shuffle data. If enabled, Spark will calculate the checksum values for each partition data within the map output file and store the values in a checksum file on the disk. When there's shuffle data corruption detected, Spark will try to diagnose the cause (e.g., network issue, disk issue, etc.) of the corruption by using the checksum file.	

	是否计算 shuffle 数据的 checksum. 如果启用，spark 将在每个 map 输出文件中为每个分区数据计算 checksum 值，并将其存储在磁盘上的 checksum 文件中。

	当检测到 shuffle 数据文件损害时，spark 将使用 checksum 文件尝试诊断损害原因（例如网络问题、磁盘问题）。

- spark.shuffle.checksum.algorithm	

	Default: ADLER32	

	Since Version: 3.2.0

	> The algorithm is used to calculate the shuffle checksum. Currently, it only supports built-in algorithms of JDK, e.g., ADLER32, CRC32.	

	用来计算 shuffle checksum 的算法。当前仅支持 JDK 的内建算法，例如 ADLER32, CRC32.

- spark.shuffle.service.fetch.rdd.enabled	

	Default: false	

	Since Version: 3.0.0

	> Whether to use the ExternalShuffleService for fetching disk persisted RDD blocks. In case of dynamic allocation if this feature is enabled executors having only disk persisted blocks are considered idle after `spark.dynamicAllocation.executorIdleTimeout` and will be released accordingly.	

	是否使用 ExternalShuffleService 获取磁盘持久化的 RDD 块。在动态分配的情况下，如果启用了这个特性，在 `spark.dynamicAllocation.executorIdleTimeout` 时间之后，仅具有磁盘持久化块的 executors 将被认为是空闲的，并将被释放。

##### Spark UI

- spark.eventLog.logBlockUpdates.enabled	

	Default: false	

	Since Version: 2.3.0

	> Whether to log events for every block update, if `spark.eventLog.enabled` is true. *Warning*: This will increase the size of the event log considerably.

	如果 `spark.eventLog.enabled` 设为 true, 每个块更新时，是否记录事件。

	警告：这将会大大增加事件日志的大小。

- spark.eventLog.longForm.enabled	

	Default: false	

	Since Version: 2.4.0

	> If true, use the long form of call sites in the event log. Otherwise use the short form.	

	如果为 true, 在事件日志中，使用调用站点的长表格，否则使用短表格。

- spark.eventLog.compress	

	Default: false	

	Since Version: 1.0.0

	> Whether to compress logged events, if `spark.eventLog.enabled` is true.	

	如果 `spark.eventLog.enabled` 设为 true, 是否压缩记录的事件。

- spark.eventLog.compression.codec	

	Default: zstd	

	Since Version: 3.0.0

	> The codec to compress logged events. By default, Spark provides four codecs: lz4, lzf, snappy, and zstd. You can also use fully qualified class names to specify the codec, e.g. `org.apache.spark.io.LZ4CompressionCodec`, `org.apache.spark.io.LZFCompressionCodec`, `org.apache.spark.io.SnappyCompressionCodec`, and `org.apache.spark.io.ZStdCompressionCodec`.	

	压缩记录的事件的编解码。默认情况下，spark 提供了四种: lz4, lzf, snappy, zstd

	也可以使用全限定类名

- spark.eventLog.erasureCoding.enabled	

	Default: false	

	Since Version: 3.0.0

	> Whether to allow event logs to use erasure coding, or turn erasure coding off, regardless of filesystem defaults. On HDFS, erasure coded files will not update as quickly as regular replicated files, so the application updates will take longer to appear in the History Server. Note that even if this is true, Spark will still not force the file to use erasure coding, it will simply use filesystem defaults.	

	不管文件系统默认设置，是否允许事件日志使用纠删码，或关闭纠删码。在 HDFS 上，纠删码文件将不会像常规的副本文件一样快速更新，所以在 History Server 中，应用程序更新将花费更长的事件开始。

	即使此项设为 true, spark 将仍然不会强制文件使用纠删码，它将简单地使用文件系统默认值。

- spark.eventLog.dir	

	Default: file:///tmp/spark-events	

	Since Version:1.0.0

	> Base directory in which Spark events are logged, if spark.eventLog.enabled is true. Within this base directory, Spark creates a sub-directory for each application, and logs the events specific to the application in this directory. Users may want to set this to a unified location like an HDFS directory so history files can be read by the history server.	

	如果 `spark.eventLog.enabled` 设为 true, spark 事件写入的基础目录。在这个基础目录下，spark 为每个应用程序创建一个子目录，将特定于这个应用程序的事件写入这个目录。

	用户可能想要将其设置为一个统一的路径，如 HDFS 目录，这样历史文件可以通过 history server 读取。

- spark.eventLog.enabled	

	Default: false	

	Since Version: 1.0.0

	> Whether to log Spark events, useful for reconstructing the Web UI after the application has finished.	

	是否记录 spark 事件，在应用程序完成后，重构 WEB UI 时有用。

- spark.eventLog.overwrite	

	Default: false	

	Since Version: 1.0.0

	> Whether to overwrite any existing files.	

	是否覆盖任意存在的文件。

- spark.eventLog.buffer.kb	

	Default: 100k	

	Since Version: 1.0.0

	> Buffer size to use when writing to output streams, in KiB unless otherwise specified.	

	当写入到输出流时，使用的缓存大小。

- spark.eventLog.rolling.enabled	

	Default: false	

	Since Version: 3.0.0

	> Whether rolling over event log files is enabled. If set to true, it cuts down each event log file to the configured size.	

	是否启用滚动事件日志文件。如果为 true, 它会将每个事件日志文件切断成配置的大小。

- spark.eventLog.rolling.maxFileSize	

	Default: 128m	

	Since Version: 3.0.0

	> When `spark.eventLog.rolling.enabled=true`, specifies the max size of event log file before it's rolled over.	

	当 spark.eventLog.rolling.enabled=true 时，指定事件日志文件滚动前的最大大小。

- spark.ui.dagGraph.retainedRootRDDs	

	Default: Int.MaxValue	

	Since Version: 2.1.0

	> How many DAG graph nodes the Spark UI and status APIs remember before garbage collecting.	

	在垃圾回收前，Spark UI 和 status APIs 记住的 DAG 图节点的数量。

- spark.ui.enabled	

	Default: true	

	Since Version: 1.1.1

	> Whether to run the web UI for the Spark application.	

	是否运行 web UI

- spark.ui.killEnabled	

	Default: true	

	Since Version: 1.0.0

	> Allows jobs and stages to be killed from the web UI.	

	允许从 web UI 上杀掉 jobs 和 stages

- spark.ui.liveUpdate.period	

	Default: 100ms	

	Since Version: 2.3.0

	> How often to update live entities. -1 means "never update" when replaying applications, meaning only the last write will happen. For live applications, this avoids a few operations that we can live without when rapidly processing incoming task events.	


- spark.ui.liveUpdate.minFlushPeriod	

	Default: 1s	

	Since Version: 2.4.2

	> Minimum time elapsed before stale UI data is flushed. This avoids UI staleness when incoming task events are not fired frequently.	

	在刷新旧的 UI 数据前，逝去的最短时间。在传来的任务事件不被频繁触发时，这避免了 UI 的陈旧。

- spark.ui.port	

	Default: 4040	

	Since Version: 0.7.0

	> Port for your application's dashboard, which shows memory and workload data.	

	你的应用程序的看板的端口

- spark.ui.retainedJobs	

	Default: 1000	

	Since Version: 1.2.0

	> How many jobs the Spark UI and status APIs remember before garbage collecting. This is a target maximum, and fewer elements may be retained in some circumstances.	

	在垃圾回收前，Spark UI 和 status APIs 记住的作业数量。这是一个目标最大值，在一些情况下，可能保留更少的元素。

- spark.ui.retainedStages	

	Default: 1000	

	Since Version: 0.9.0

	> How many stages the Spark UI and status APIs remember before garbage collecting. This is a target maximum, and fewer elements may be retained in some circumstances.	

	在垃圾回收前，Spark UI 和 status APIs 记住的 stages 数量。这是一个目标最大值，在一些情况下，可能保留更少的元素。

- spark.ui.retainedTasks	

	Default: 100000	

	Since Version: 2.0.1

	> How many tasks in one stage the Spark UI and status APIs remember before garbage collecting. This is a target maximum, and fewer elements may be retained in some circumstances.	

	在垃圾回收前，Spark UI 和 status APIs 记住的任务数量。这是一个目标最大值，在一些情况下，可能保留更少的元素。

- spark.ui.reverseProxy	

	Default: false	

	Since Version: 2.1.0

	> Enable running Spark Master as reverse proxy for worker and application UIs. In this mode, Spark master will reverse proxy the worker and application UIs to enable access without requiring direct access to their hosts. Use it with caution, as worker and application UI will not be accessible directly, you will only be able to access them through spark master/proxy public URL. This setting affects all the workers and application UIs running in the cluster and must be set on all the workers, drivers and masters.	

	为 worker 和应用程序 UIs, 启用运行中的 Spark Master 作为反向代理。

	在这个模式下，Spark Master 将反向代理 worker 和应用程序 UIs，为了能在不用直接访问它们主机的情况下启用访问。

	使用这个特性需要小心，由于 worker 和应用程序 UIs 将不能直接访问，你将仅能通过 spark master/proxy public URL 访问它们。

	这个设置影响所有运行在集群中的 workers 和应用程序 UIs，必须在所有 workers, drivers 和 masters 上设置。

- spark.ui.reverseProxyUrl		

	Since Version: 2.1.0

	> If the Spark UI should be served through another front-end reverse proxy, this is the URL for accessing the Spark master UI through that reverse proxy. This is useful when running proxy for authentication e.g. an OAuth proxy. The URL may contain a path prefix, like http://mydomain.com/path/to/spark/, allowing you to serve the UI for multiple Spark clusters and other web applications through the same virtual host and port. Normally, this should be an absolute URL including scheme (http/https), host and port. It is possible to specify a relative URL starting with "/" here. In this case, all URLs generated by the Spark UI and Spark REST APIs will be server-relative links -- this will still work, as the entire Spark UI is served through the same host and port.

	如果 Spark UI 通过其他前端反向代理来服务，那么这个就是通过那个反向代理访问 Spark master UI 的 URL. 当为身份验证运行代理时是有用的，例如，一个 OAuth 代理。

	URL 可能包含一个路径前缀，就像 http://mydomain.com/path/to/spark/, 允许你为多个 spark 集群和其他 web 应用程序通过相同的虚拟主机和端口服务 UI.

	正常情况下，这应该是一个包含 http/https, host 和 port 的绝对路径。使用 `\` 开头指定一个相对路径也是可以的。在这种情况下，Spark UI 和 Spark REST APIs 生成的所有 URLs 都是相对于服务的链接，这将仍然有用，因为整个 Spark UI 通过相同的主机和端口服务。

	> The setting affects link generation in the Spark UI, but the front-end reverse proxy is responsible for

	在 Spark UI 中，这个设置影响链接生成，但是前端反向代理负责：

	- 在提交请求前，去掉路径前缀
	- 重写直接指向 Spark master 的重定向
	- 将 `http://mydomain.com/path/to/spark` 重定向到 `http://mydomain.com/path/to/spark/` （在路径前缀后有斜杠）；否则在 master 页的相对链接不能正确工作。

	> stripping a path prefix before forwarding the request,
	> rewriting redirects which point directly to the Spark master,
	> redirecting access from `http://mydomain.com/path/to/spark` to `http://mydomain.com/path/to/spark/` (trailing slash after path prefix); otherwise relative links on the master page do not work correctly.

	> This setting affects all the workers and application UIs running in the cluster and must be set identically on all the workers, drivers and masters. In is only effective when `spark.ui.reverseProxy` is turned on. This setting is not needed when the Spark master web UI is directly reachable.	

	这个设置影响集群中所有的 workers 和应用程序 UIs, 必须在所有 workers, drivers 和 masters 上设置相同的配置。

	只有在 `spark.ui.reverseProxy` 打开的时候，它才有效。当 Spark master web UI 可直接访问的时候，这个配置就不需要了。

- spark.ui.proxyRedirectUri		

	Since Version: 3.0.0

	> Where to address redirects when Spark is running behind a proxy. This will make Spark modify redirect responses so they point to the proxy server, instead of the Spark UI's own address. This should be only the address of the server, without any prefix paths for the application; the prefix should be set either by the proxy server itself (by adding the X-Forwarded-Context request header), or by setting the proxy base in the Spark app's configuration.	

	当 spark 在代理后面运行时，重定向的位置。这将让 spark 修改重定向响应，所以它们指向代理服务，而不是 Spark UI 自己的地址。

	这应该仅是服务的地址，不需要任意的前缀路径，设置前缀要么通过代理服务本身，要么在 spark 应用的配置中设置 proxy base

- spark.ui.showConsoleProgress	

	Default: false	

	Since Version: 1.2.1

	> Show the progress bar in the console. The progress bar shows the progress of stages that run for longer than 500ms. If multiple stages run at the same time, multiple progress bars will be displayed on the same line.

	在控制台显示进度条。

	进度条展示运行超过 500ms 的 stages 的进度。如果多个 stages 同时运行，那么多个进度条将在相同行展示。

	> Note: In shell environment, the default value of `spark.ui.showConsoleProgress` is true.	

	注意：在 shell 环境下，`spark.ui.showConsoleProgress` 的默认值是 true.

- spark.ui.custom.executor.log.url	

	Default: (none)	

	Since Version: 3.0.0

	> Specifies custom spark executor log URL for supporting external log service instead of using cluster managers' application log URLs in Spark UI. Spark will support some path variables via patterns which can vary on cluster manager. Please check the documentation for your cluster manager to see which patterns are supported, if any.

	指定自定义的 spark executor log URL, 以支持外部日志服务，而不是使用集群管理器的应用程序日志 URLs. spark 通过模板支持一些路径变量，为了能在集群管理器上变化。

	> Please note that this configuration also replaces original log urls in event log, which will be also effective when accessing the application on history server. The new log urls must be permanent, otherwise you might have dead link for executor log urls.

	注意，这个配置取代事件日志中的原始日志 urls, 当在 history server 上访问应用程序时，也将有效。新的日志 urls 必须是持久的，否则你可能 executor log urls 中可能有死掉的链接。

	> For now, only YARN mode supports this configuration

	这个配置暂时仅支持 YARN 模式。

- spark.worker.ui.retainedExecutors	

	Default: 1000	

	Since Version: 1.5.0

	> How many finished executors the Spark UI and status APIs remember before garbage collecting.	

	在垃圾回收前，Spark UI 和 status APIs 记住的完成的 executors 数量。

- spark.worker.ui.retainedDrivers	

	Default: 1000	

	Since Version: 1.5.0

	> How many finished drivers the Spark UI and status APIs remember before garbage collecting.	

	在垃圾回收前，Spark UI 和 status APIs 记住的完成的 drivers 数量。

- spark.sql.ui.retainedExecutions	

	Default: 1000	

	Since Version: 1.5.0

	> How many finished executions the Spark UI and status APIs remember before garbage collecting.	

	在垃圾回收前，Spark UI 和 status APIs 记住的完成的执行数量。

- spark.streaming.ui.retainedBatches	

	Default: 1000	

	Since Version: 1.0.0

	> How many finished batches the Spark UI and status APIs remember before garbage collecting.	

	在垃圾回收前，Spark UI 和 status APIs 记住的完成的批次数量。

- spark.ui.retainedDeadExecutors	

	Default: 100	

	Since Version: 2.0.0

	> How many dead executors the Spark UI and status APIs remember before garbage collecting.	

	在垃圾回收前，Spark UI 和 status APIs 记住的死掉的 executors 数量。

- spark.ui.filters	

	Default: None	

	Since Version: 1.0.0

	> Comma separated list of filter class names to apply to the Spark Web UI. The filter should be a standard javax servlet Filter.

	应用于 Spark Web UI 的过滤器的类名的列表。这个过滤器应该是标准的 javax servlet Filter.

	> Filter parameters can also be specified in the configuration, by setting config entries of the form spark.`<class name of filter>.param.<param name>=<value>`

	可以在这个配置中指定过滤器参数，通过以 `<class name of filter>.param.<param name>=<value>` 形式设置配置项：

	> For example:
	> spark.ui.filters=com.test.filter1
	> spark.com.test.filter1.param.name1=foo
	> spark.com.test.filter1.param.name2=bar	

- spark.ui.requestHeaderSize	

	Default: 8k	

	Since Version: 2.2.3

	> The maximum allowed size for a HTTP request header, in bytes unless otherwise specified. This setting applies for the Spark History Server too.	

	对于一个 HTTP 请求头，允许的最大大小。这个设置也应用于 Spark History Server.

- spark.ui.timeline.executors.maximum	

	Default: 250	

	Since Version: 3.2.0

	> The maximum number of executors shown in the event timeline.	

	在事件时间线中，executors 的最大数量。

- spark.ui.timeline.jobs.maximum	

	Default: 500	

	Since Version: 3.2.0

	> The maximum number of jobs shown in the event timeline.	

	在事件时间线中，作业的最大数量。

- spark.ui.timeline.stages.maximum	

	Default: 500	

	Since Version: 3.2.0

	> The maximum number of stages shown in the event timeline.	

	在事件时间线中，stages 的最大数量。

- spark.ui.timeline.tasks.maximum	

	Default: 1000	

	Since Version: 1.4.0

	> The maximum number of tasks shown in the event timeline.	

	在事件时间线中，任务的最大数量。

##### Compression and Serialization

- spark.broadcast.compress	

	Default: true	

	Since Version: 0.6.0

	> Whether to compress broadcast variables before sending them. Generally a good idea. Compression will use `spark.io.compression.codec`.	

	在发送广播变量前，是否压缩它。通常来说是个好主意。将使用 `spark.io.compression.codec` 指定的压缩。

- spark.checkpoint.compress	

	Default: false	

	Since Version: 2.2.0

	> Whether to compress RDD checkpoints. Generally a good idea. Compression will use `spark.io.compression.codec`.

	是否压缩 RDD checkpoints.通常来说是个好主意。将使用 `spark.io.compression.codec` 指定的压缩。

- spark.io.compression.codec	

	Default: lz4	

	Since Version: 0.8.0

	> The codec used to compress internal data such as RDD partitions, event log, broadcast variables and shuffle outputs. By default, Spark provides four codecs: lz4, lzf, snappy, and zstd. You can also use fully qualified class names to specify the codec, e.g. `org.apache.spark.io.LZ4CompressionCodec`, `org.apache.spark.io.LZFCompressionCodec`, `org.apache.spark.io.SnappyCompressionCodec`, and `org.apache.spark.io.ZStdCompressionCodec`.	

	用来压缩中间数据的编解码器，例如 RDD 分区、事件日志、广播变量和 shuffle 输出。

	默认情况下，spark 提供4种编解码器: lz4, lzf, snappy, 和 zstd. 你也可以使用全限定类名指定。

- spark.io.compression.lz4.blockSize	

	Default: 32k	

	Since Version: 1.4.0

	> Block size used in LZ4 compression, in the case when LZ4 compression codec is used. Lowering this block size will also lower shuffle memory usage when LZ4 is used. Default unit is bytes, unless otherwise specified.	

	LZ4 压缩中的块大小。当使用 LZ4 压缩时，降低这个块大小也将减低 shuffle 内存用量。

- spark.io.compression.snappy.blockSize	

	Default: 32k	

	Since Version: 1.4.0

	> Block size in Snappy compression, in the case when Snappy compression codec is used. Lowering this block size will also lower shuffle memory usage when Snappy is used. Default unit is bytes, unless otherwise specified.	

	Snappy 压缩中的块大小。当使用 Snappy 压缩时，降低这个块大小也将减低 shuffle 内存用量。

- spark.io.compression.zstd.level	

	Default: 1	

	Since Version: 2.3.0

	> Compression level for Zstd compression codec. Increasing the compression level will result in better compression at the expense of more CPU and memory.	

	使用 Zstd 压缩时的压缩级别。增加压缩级别将导致更高的压缩性能，而使用更多的 CPU 和内存。

- spark.io.compression.zstd.bufferSize	

	Default: 32k	

	Since Version: 2.3.0

	> Buffer size in bytes used in Zstd compression, in the case when Zstd compression codec is used. Lowering this size will lower the shuffle memory usage when Zstd is used, but it might increase the compression cost because of excessive JNI call overhead.	

	使用 Zstd 压缩时的缓存大小。当使用 Zstd 压缩时，降低这个大小也将减低 shuffle 内存用量，但是由于超额的 JNI 调用开销，会增加压缩成本。

- spark.kryo.classesToRegister	

	Default: (none)	

	Since Version: 1.2.0

	> If you use Kryo serialization, give a comma-separated list of custom class names to register with Kryo. See the [tuning guide](https://spark.apache.org/docs/3.3.2/tuning.html#data-serialization) for more details.	

	如果你使用 Kryo 序列化器，提供一个逗号分隔的自定义类名的列表向 Kryo 注册。

- spark.kryo.referenceTracking	

	Default: true	

	Since Version: 0.8.0

	> Whether to track references to the same object when serializing data with Kryo, which is necessary if your object graphs have loops and useful for efficiency if they contain multiple copies of the same object. Can be disabled to improve performance if you know this is not the case.	

	当使用 Kryo 序列化数据时，是否追踪相同对象的引用，如果你的对象图有循环，那么就是有必要的，如果它们包含了相同对象的多个副本，那么可以提升效率。

	如果你知道不是在这种情况下，那么禁用它可以提升性能。

- spark.kryo.registrationRequired	

	Default: false	

	Since Version: 1.1.0

	> Whether to require registration with Kryo. If set to 'true', Kryo will throw an exception if an unregistered class is serialized. If set to false (the default), Kryo will write unregistered class names along with each object. Writing class names can cause significant performance overhead, so enabling this option can enforce strictly that a user has not omitted classes from registration.	

	是否要求向 Kryo 注册。 

	如果设为 true, 且序列化未注册的类，那么 Kryo 将抛出异常。 

	如果设为 false(默认),  Kryo 将为每个对象编写未注册的类名。编写类名可以造成重大的性能开销，所以启用这个选项可以强制用户在注册时不省略类。

- spark.kryo.registrator	

	Default: (none)	

	Since Version: 0.5.0

	> If you use Kryo serialization, give a comma-separated list of classes that register your custom classes with Kryo. This property is useful if you need to register your classes in a custom way, e.g. to specify a custom field serializer. Otherwise `spark.kryo.classesToRegister` is simpler. It should be set to classes that extend [KryoRegistrator](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/serializer/KryoRegistrator.html). See the [tuning guide](https://spark.apache.org/docs/3.3.2/tuning.html#data-serialization) for more details.	

	如果你使用 Kryo 序列化器，提供一个逗号分隔的类的列表，向 Kryo 注册你自定义的类。如果你需要以自定义方式注册你的类，这个属性是有用的，例如，要指定一个自定义的字段序列化器。否则，使用 `spark.kryo.classesToRegister` 就更简单一些。 

	它应该设置成继承了 KryoRegistrator 类的类。

- spark.kryo.unsafe	

	Default: false	

	Since Version: 2.1.0

	> Whether to use unsafe based Kryo serializer. Can be substantially faster by using Unsafe Based IO.	

	是否使用不安全的基本 Kryo 序列化器。要比使用不安全的基本 IO 更快。

- spark.kryoserializer.buffer.max	

	Default: 64m	

	Since Version: 1.4.0

	> Maximum allowable size of Kryo serialization buffer, in MiB unless otherwise specified. This must be larger than any object you attempt to serialize and must be less than 2048m. Increase this if you get a "buffer limit exceeded" exception inside Kryo.	

	Kryo 序列化缓存的最大可允许的大小。这必须比你想要序列化的任意对象都要大，且必须小于 2048m.

	如果在 Kryo 中得到了 "buffer limit exceeded" 异常，那么就可以增大这个值。

- spark.kryoserializer.buffer	

	Default: 64k	

	Since Version: 1.4.0

	> Initial size of Kryo's serialization buffer, in KiB unless otherwise specified. Note that there will be one buffer per core on each worker. This buffer will grow up to `spark.kryoserializer.buffer.max` if needed.
	
	初始化 Kryo 序列化缓存的大小。

	注意，在每个 worker 上的每个核心上都有一个缓存区。如果需要，这个缓存区将增长到 `spark.kryoserializer.buffer.max`.

- spark.rdd.compress	

	Default: false	

	Since Version: 0.6.0

	> Whether to compress serialized RDD partitions (e.g. for `StorageLevel.MEMORY_ONLY_SER` in Java and Scala or `StorageLevel.MEMORY_ONLY` in Python). Can save substantial space at the cost of some extra CPU time. Compression will use `spark.io.compression.codec`.	

	是否压缩序列化的 RDD 分区。可以节省大量的空间，但代价是一些额外的 CPU 时间。将使用 `spark.io.compression.codec` 指定的压缩。

- spark.serializer	

	Default: org.apache.spark.serializer.JavaSerializer	

	Since Version: 0.5.0

	> Class to use for serializing objects that will be sent over the network or need to be cached in serialized form. The default of Java serialization works with any Serializable Java object but is quite slow, so we recommend [using `org.apache.spark.serializer.KryoSerializer` and configuring Kryo serialization](https://spark.apache.org/docs/3.3.2/tuning.html) when speed is necessary. Can be any subclass of [`org.apache.spark.Serializer`](https://spark.apache.org/docs/3.3.2/api/scala/org/apache/spark/serializer/Serializer.html).	

	用于序列化对象的类，该对象将通过网络发送，或者需要以序列化形式缓存。

	Java 序列化的默认情况是可以和任意可序列化的 Java 对象一起使用，但是要慢一些。所以在要求速度的情况下，推荐使用 `org.apache.spark.serializer.KryoSerializer`, 并配置 Kryo 序列化

	可以是 `org.apache.spark.Serializer` 的任意子类。

- spark.serializer.objectStreamReset	

	Default: 100	

	Since Version: 1.0.0

	> When serializing using `org.apache.spark.serializer.JavaSerializer`, the serializer caches objects to prevent writing redundant data, however that stops garbage collection of those objects. By calling 'reset' you flush that info from the serializer, and allow old objects to be collected. To turn off this periodic reset set it to -1. By default it will reset the serializer every 100 objects.	

	当使用 `org.apache.spark.serializer.JavaSerializer` 序列化时，序列化器缓存对象，以阻止写入冗余的数据，然而会停止这些对象的垃圾回收。

	通过调用 reset, 你将从序列化器刷新信息，允许收集旧的对象。

	将其设为-1，可关闭这个周期重置。默认情况下，它将每100个对象就重置序列化器。

##### Memory Management

- spark.memory.fraction	

	Default: 0.6	

	Since Version: 1.6.0

	> Fraction of (heap space - 300MB) used for execution and storage. The lower this is, the more frequently spills and cached data eviction occur. The purpose of this config is to set aside memory for internal metadata, user data structures, and imprecise size estimation in the case of sparse, unusually large records. Leaving this at the default value is recommended. For more detail, including important information about correctly tuning JVM garbage collection when increasing this value, see [this description](https://spark.apache.org/docs/3.3.2/tuning.html#memory-management-overview).	

	用于执行和存储内存占 (heap space - 300MB) 的比例。这个值越低，溢写和清除缓存数据越频繁。

	这个配置的目的就是为中间元数据、用户数据结构，和堆稀疏、特别大的记录的不精确的大小估计而预留的内存。

	推荐使用这个配置的默认值。

- spark.memory.storageFraction	

	Default: 0.5	

	Since Version: 1.6.0

	> Amount of storage memory immune to eviction, expressed as a fraction of the size of the region set aside by `spark.memory.fraction`. The higher this is, the less working memory may be available to execution and tasks may spill to disk more often. Leaving this at the default value is recommended. For more detail, see [this description](https://spark.apache.org/docs/3.3.2/tuning.html#memory-management-overview).	

	不被移除的存储内存量，占 spark.memory.fraction 留出区域的一个比例。

	这个值越大，用于执行的内容就越小，任务溢写磁盘就越频繁。推荐使用默认值。

- spark.memory.offHeap.enabled	

	Default: false	

	Since Version: 1.6.0

	> If true, Spark will attempt to use off-heap memory for certain operations. If off-heap memory use is enabled, then `spark.memory.offHeap.size` must be positive.	

	如果为 true, spark 将为特定操作使用堆外内存。如果启用使用堆外内存，那么 `spark.memory.offHeap.size` 必须是正值。

- spark.memory.offHeap.size	

	Default: 0	

	Since Version: 1.6.0

	> The absolute amount of memory which can be used for off-heap allocation, in bytes unless otherwise specified. This setting has no impact on heap memory usage, so if your executors' total memory consumption must fit within some hard limit then be sure to shrink your JVM heap size accordingly. This must be set to a positive value when `spark.memory.offHeap.enabled=true`.	

	分配的堆外内存的量。这个设置对堆上内存使用没有影响，所以如果你的 executors 的总内存消耗在溢写硬性限制下，然后要确保缩小 JVM 堆大小。

	当 `spark.memory.offHeap.enabled=true` 时，这个值必须设为正值。

- spark.storage.replication.proactive	

	Default: false	

	Since Version: 2.2.0

	> Enables proactive block replication for RDD blocks. Cached RDD block replicas lost due to executor failures are replenished if there are any existing available replicas. This tries to get the replication level of the block to the initial number.	

	为 RDD 块启用积极的块副本机制。

	如果存在任意可用副本，由于 executor 故障丢失的缓存 RDD 块会被补充上。这会尝试将副本级别设为初始值。

- spark.cleaner.periodicGC.interval	

	Default: 30min	

	Since Version: 1.6.0

	> Controls how often to trigger a garbage collection.

	控制多久触发一次垃圾回收。

	> This context cleaner triggers cleanups only when weak references are garbage collected. In long-running applications with large driver JVMs, where there is little memory pressure on the driver, this may happen very occasionally or not at all. Not cleaning at all may lead to executors running out of disk space after a while.	

	仅在弱引用是待收集的垃圾时，这个上下文清理器触发清理。

	在使用大量驱动 JVMs 的长时间运行的应用程序中，且在驱动有很少的内存，这会很偶然的发生，或就不会发生。

	一点都不清理可能会导致运行的 executors 用完磁盘空间。

- spark.cleaner.referenceTracking	

	Default: true	

	Since Version: 1.0.0

	> Enables or disables context cleaning.	

	启用或禁用上下文清理。

- spark.cleaner.referenceTracking.blocking	

	Default: true	

	Since Version: 1.0.0

	> Controls whether the cleaning thread should block on cleanup tasks (other than shuffle, which is controlled by `spark.cleaner.referenceTracking.blocking.shuffle` Spark property).	

	清理线程是否应该阻塞清理任务。

- spark.cleaner.referenceTracking.blocking.shuffle	

	Default: false	

	Since Version: 1.1.1

	> Controls whether the cleaning thread should block on shuffle cleanup tasks.	

	清理线程是否应该阻塞 shuffle 清理任务。

- spark.cleaner.referenceTracking.cleanCheckpoints	

	Default: false	

	Since Version: 1.4.0

	> Controls whether to clean checkpoint files if the reference is out of scope.	

	如果引用超出范围了，控制着是否清理 checkpoint 文件。

##### Execution Behavior

- spark.broadcast.blockSize	

	Default: 4m	

	Since Version: 0.5.0

	> Size of each piece of a block for TorrentBroadcastFactory, in KiB unless otherwise specified. Too large a value decreases parallelism during broadcast (makes it slower); however, if it is too small, BlockManager might take a performance hit.	

	每个块的大小。

	在广播期间，太大的值会降低并行度。然而，如果太小，会影响 BlockManager 的性能。

- spark.broadcast.checksum	

	Default: true	

	Since Version: 2.1.1

	> Whether to enable checksum for broadcast. If enabled, broadcasts will include a checksum, which can help detect corrupted blocks, at the cost of computing and sending a little more data. It's possible to disable it if the network has other mechanisms to guarantee data won't be corrupted during broadcast.	

	是否为广播启用校验和。如果启用，广播将包含校验和值，它会帮助检测损害的块，代价就是会计算并发送一些数据。

	如果网络有其他保证在广播期间数据不会损害的机制，那么就可以禁用它。

- spark.executor.cores	

	Default: 1 in YARN mode, all the available cores on the worker in standalone and Mesos coarse-grained modes.	
	Since Version: 1.0.0

	> The number of cores to use on each executor. In standalone and Mesos coarse-grained modes, for more detail, see [this description](https://spark.apache.org/docs/3.3.2/spark-standalone.html#Executors%20Scheduling).	

	每个 executor 上可以使用的核心数。

- spark.default.parallelism	

	Since Version: 0.5.0

	> For distributed shuffle operations like reduceByKey and join, the largest number of partitions in a parent RDD. For operations like parallelize with no parent RDDs, it depends on the cluster manager:
	
	对于像 reduceByKey 和 join 的分布式 shuffle 操作，在父 RDD 中分区的最大数量。对于像 parallelize 这种没有父 RDD 的操作，它取决于集群管理器：

	- 本地模式：在本地机器上的核心数
	- Mesos 细粒度模式：8
	- 其他：在所有 executor 节点核心的总数量，或者是2，取二者更大的值

	> Local mode: number of cores on the local machine
	> Mesos fine grained mode: 8
	> Others: total number of cores on all executor nodes or 2, whichever is larger

	> Default number of partitions in RDDs returned by transformations like join, reduceByKey, and parallelize when not set by user.	

	转换操作返回的 RDDs 中的默认分区数。

- spark.executor.heartbeatInterval	

	Default: 10s	

	Since Version: 1.1.0

	> Interval between each executor's heartbeats to the driver. Heartbeats let the driver know that the executor is still alive and update it with metrics for in-progress tasks. `spark.executor.heartbeatInterval` should be significantly less than `spark.network.timeout`	

	每个 executor 发送心跳到 driver 的间隔。心跳会让 driver 知道 executor 仍然活着，并更新度量。

	此项显著应该小于 `spark.network.timeout` 值。

- spark.files.fetchTimeout	

	Default: 60s	

	Since Version: 1.0.0

	> Communication timeout to use when fetching files added through `SparkContext.addFile()` from the driver.	

	当从 driver 获取通过 `SparkContext.addFile()` 添加的文件的通信超时时长。

- spark.files.useFetchCache	

	Default: true	

	Since Version: 1.2.2

	> If set to true (default), file fetching will use a local cache that is shared by executors that belong to the same application, which can improve task launching performance when running many executors on the same host. If set to false, these caching optimizations will be disabled and all executors will fetch their own copies of files. This optimization may be disabled in order to use Spark local directories that reside on NFS filesystems (see [SPARK-6313](https://issues.apache.org/jira/browse/SPARK-6313) for more details).	

	如果设为 true, 获取的文件将使用 executors 共享的本地缓存，这些 executors 属于同一应用程序。当在相同主机上运行多个 executors 时，能改善任务启动的性能。

	如果设为 false, 将禁用这些缓存优化，所有的 executors 将获取它们自己的文件副本。为了使用驻留在 NFS 文件系统上的 spark 本地目录，这个优化将被禁用。

- spark.files.overwrite	

	Default: false	

	Since Version: 1.0.0

	> Whether to overwrite any files which exist at the startup. Users can not overwrite the files added by `SparkContext.addFile` or `SparkContext.addJar` before even if this option is set true.	

	是否覆盖在启动时就存在的任意文件。即使这个选项设为 true, 用户也不能覆盖使用 `SparkContext.addFile` 或 `SparkContext.addJar` 添加的文件。

- spark.files.maxPartitionBytes	

	Default: 134217728 (128 MiB)	

	Since Version: 2.1.0

	> The maximum number of bytes to pack into a single partition when reading files.	

	在读取文件时，打包在一个分区的最大字节数。

- spark.files.openCostInBytes	

	Default: 4194304 (4 MiB)	

	Since Version: 2.1.0

	> The estimated cost to open a file, measured by the number of bytes could be scanned at the same time. This is used when putting multiple files into a partition. It is better to overestimate, then the partitions with small files will be faster than partitions with bigger files.	

	打开一个文件的估算成本，用同时扫描的字节数来衡量。在将多个文件放入同一分区时使用。

	最好过高估计，那么具有小文件的分区会比具有大文件的分区更快。

- spark.hadoop.cloneConf	

	Default: false	

	Since Version: 1.0.3

	> If set to true, clones a new Hadoop `Configuration` object for each task. This option should be enabled to work around `Configuration` thread-safety issues (see [SPARK-2546](https://issues.apache.org/jira/browse/SPARK-2546) for more details). This is disabled by default in order to avoid unexpected performance regressions for jobs that are not affected by these issues.	

	如果设为 true, 为每个任务克隆一个新的 Hadoop `Configuration` 对象。应该启用这个选项，以解决 `Configuration` 对象线程安全问题。

	默认情况下禁用此功能，以避免未受这些问题影响的作业出现意外的性能下降。

- spark.hadoop.validateOutputSpecs	

	Default: true	

	Since Version: 1.0.1

	> If set to true, validates the output specification (e.g. checking if the output directory already exists) used in saveAsHadoopFile and other variants. This can be disabled to silence exceptions due to pre-existing output directories. We recommend that users do not disable this except if trying to achieve compatibility with previous versions of Spark. Simply use Hadoop's FileSystem API to delete output directories by hand. This setting is ignored for jobs generated through Spark Streaming's StreamingContext, since data may need to be rewritten to pre-existing output directories during checkpoint recovery.	

	如果设为 true, 验证 saveAsHadoopFile 或其他变体使用的输出规范（例如，检查输出目录是否已存在）。

	可以禁用此功能，以沉默由于输出目录已存在导致的异常。如果尝试实现和前面 spark 版本的兼容，不建议禁用这个异常。使用 Hadoop FileSystem API 手动删除输出目录。

	通过 Spark Streaming 的 StreamingContext 生成的作业会忽略这个设置，因为在 checkpoint 恢复期间，数据可能需要重写到预先存在的输出目录。

- spark.storage.memoryMapThreshold	

	Default: 2m	

	Since Version: 0.9.2

	> Size of a block above which Spark memory maps when reading a block from disk. Default unit is bytes, unless specified otherwise. This prevents Spark from memory mapping very small blocks. In general, memory mapping has high overhead for blocks close to or below the page size of the operating system.	

	当从磁盘读取块时，spark 内存所映射的块的大小。

	这可以防止 spark 对非常小的块进行内存映射。通常，对于接近或低于操作系统页面大小的块，内存映射有很高的开销。

- spark.hadoop.mapreduce.fileoutputcommitter.algorithm.version	

	Default: 1	

	Since Version: 2.2.0

	> The file output committer algorithm version, valid algorithm version number: 1 or 2. Note that 2 may cause a correctness issue like MAPREDUCE-7282.	

	文件输出提交算法的版本，有效的算法版本数字是：1或2，2可能会造成正确性问题

##### Executor Metrics

- spark.eventLog.logStageExecutorMetrics	

	Default: false	

	Since Version: 3.0.0

	> Whether to write per-stage peaks of executor metrics (for each executor) to the event log.

	是否将 executor 度量的每阶段峰值写入事件日志。

	> Note: The metrics are polled (collected) and sent in the executor heartbeat, and this is always done; this configuration is only to determine if aggregated metric peaks are written to the event log.	

	指标是被轮询(收集)并在 executor 心跳中发送的，这始终是完成的；此配置仅用于确定是否将聚合指标峰值写入事件日志。

- spark.executor.processTreeMetrics.enabled	

	Default: false	

	Since Version: 3.0.0

	> Whether to collect process tree metrics (from the `/proc` filesystem) when collecting executor metrics.

	在收集 executor 度量时，是否收集进程树度量（从文件系统 `/proc` 目录下）

	> Note: The process tree metrics are collected only if the `/proc` filesystem exists.	

	进程树度量仅在 `/proc` 目录存在时才会被收集。

- spark.executor.metrics.pollingInterval	

	Default: 0	

	Since Version: 3.0.0

	> How often to collect executor metrics (in milliseconds).

	收集 executor 度量的间隔

	> If 0, the polling is done on executor heartbeats (thus at the heartbeat interval, specified by `spark.executor.heartbeatInterval`). If positive, the polling is done at this interval.	

	如果是0，在 executor 心跳时完成轮询（因此就是心跳间隔，由 `spark.executor.heartbeatInterval` 指定）。如果是正值，那么就是在这个间隔完成轮询。

##### Networking

- spark.rpc.message.maxSize	

	Default: 128	

	Since Version: 2.0.0

	> Maximum message size (in MiB) to allow in "control plane" communication; generally only applies to map output size information sent between executors and the driver. Increase this if you are running jobs with many thousands of map and reduce tasks and see messages about the RPC message size.	

	允许在“控制区”通信的最大消息大小。通常仅适用于在 executors 和 driver 之间发送的 map 输出大小信息。

	如果正在运行的作业具有成千上万的 map 和 reduce 任务，并要查看有关 RPC 消息大小的消息，就增加这个值。

- spark.blockManager.port	

	Default: (random)	

	Since Version: 1.1.0

	> Port for all block managers to listen on. These exist on both the driver and the executors.	

	所有块管理器监听的端口。这些端口同时存在于 executors 和 driver.

- spark.driver.blockManager.port	

	Default: (value of spark.blockManager.port)	

	Since Version: 2.1.0

	> Driver-specific port for the block manager to listen on, for cases where it cannot use the same configuration as executors.	

	块管理器监听的特定于 driver 的端口，用于它不能使用与 executors 相同配置的情况。

- spark.driver.bindAddress	

	Default: (value of spark.driver.host)	

	Since Version: 2.1.0

	> Hostname or IP address where to bind listening sockets. This config overrides the SPARK_LOCAL_IP environment variable (see below).

	绑定监听 sockets 的主机名或 IP 地址。这个配置会覆盖 SPARK_LOCAL_IP 环境变量。

	> It also allows a different address from the local one to be advertised to executors or external systems. This is useful, for example, when running containers with bridged networking. For this to properly work, the different ports used by the driver (RPC, block manager and UI) need to be forwarded from the container's host.	

	它还允许将不同于本地的地址通知 executors 或外部系统。在运行中的容器具有桥接网络时是有用的。要使用此配置正确工作，驱动使用的不同端口需要从容器主机转发。

- spark.driver.host	

	Default: (local hostname)	

	Since Version: 0.7.0

	> Hostname or IP address for the driver. This is used for communicating with the executors and the standalone Master.	

	驱动的主机名或 IP 地址。用于在 executors 和独立 master 间的通信。

- spark.driver.port	

	Default: (random)	

	Since Version: 0.7.0

	> Port for the driver to listen on. This is used for communicating with the executors and the standalone Master.	

	驱动监听的端口。用于在 executors 和独立 master 间的通信。

- spark.rpc.io.backLog	

	Default: 64	
	
	Since Version: 3.0.0

	> Length of the accept queue for the RPC server. For large applications, this value may need to be increased, so that incoming connections are not dropped when a large number of connections arrives in a short period of time.

	RPC 服务接受队列的长度。对于大的应用程序，需要增大这个值，这样，在短期到达大量的连接时，传来的连接不会被删除。

- spark.network.timeout	

	Default: 120s	

	Since Version: 1.3.0

	> Default timeout for all network interactions. This config will be used in place of `spark.storage.blockManagerHeartbeatTimeoutMs`, `spark.shuffle.io.connectionTimeout`, `spark.rpc.askTimeout` or `spark.rpc.lookupTimeout` if they are not configured.	

	所有网络交互的默认超时时长。

- spark.network.io.preferDirectBufs	

	Default: true	

	Since Version: 3.0.0

	> If enabled then off-heap buffer allocations are preferred by the shared allocators. Off-heap buffers are used to reduce garbage collection during shuffle and cache block transfer. For environments where off-heap memory is tightly limited, users may wish to turn this off to force all allocations to be on-heap.	

	如果启用的话，那么共享分配器更倾向于使用堆外缓存分配。堆外缓存用于减少 shuffle 和缓存块转移期间的垃圾回收。

	对于堆外内存有限的环境，用户可能想要关闭这个功能，以强制所有的分配发送在堆上。

- spark.port.maxRetries	

	Default: 16	

	Since Version: 1.1.1

	> Maximum number of retries when binding to a port before giving up. When a port is given a specific value (non 0), each subsequent retry will increment the port used in the previous attempt by 1 before retrying. This essentially allows it to try a range of ports from the start port specified to port + maxRetries.	

	绑定一个端口时，重试的最大次数。

	当为一个端口分配一个特定的非0值时，每次的后续重试都会在重试之前将前一次尝试中使用的端口增加1。这就允许尝试从指定的初始端口到 port + maxRetries 的端口范围。

- spark.rpc.numRetries	

	Default: 3	

	Since Version: 1.4.0

	> Number of times to retry before an RPC task gives up. An RPC task will run at most times of this number.	

	在一个 RPC 任务放弃前，重试的次数。

- spark.rpc.retry.wait	

	Default: 3s	

	Since Version: 1.4.0

	> Duration for an RPC ask operation to wait before retrying.	

	在重试前，一个 RPC 请求一个操作等待的时间。

- spark.rpc.askTimeout	

	Since Version: 1.4.0

	Default: spark.network.timeout	

	> Duration for an RPC ask operation to wait before timing out.	

	在超时前，一个 RPC 请求一个操作等待的时间。

- spark.rpc.lookupTimeout	

	Default: 120s	

	Since Version: 1.4.0

	> Duration for an RPC remote endpoint lookup operation to wait before timing out.	

	在超时前，一个 RPC 远程终端查找操作等待的时间。

- spark.network.maxRemoteBlockSizeFetchToMem	

	Default: 200m	

	Since Version: 3.0.0

	> Remote block will be fetched to disk when size of the block is above this threshold in bytes. This is to avoid a giant request takes too much memory. Note this configuration will affect both shuffle fetch and block manager remote block fetch. For users who enabled external shuffle service, this feature can only work when external shuffle service is at least 2.3.0.	

	当块的大小大于这个阈值时，将远程块拉取到磁盘。这就避免了大请求占据太多内存。注意这个配置将同时影响 shuffle 拉取和块管理器远程块拉取。

	对于启用外部 shuffle 服务的用户，这个特性仅在外部 shuffle 服务至少是 2.3.0 版本才可用。

- spark.rpc.io.connectionTimeout	

	Default: value of spark.network.timeout	

	Since Version: 1.2.0

	> Timeout for the established connections between RPC peers to be marked as idled and closed if there are outstanding RPC requests but no traffic on the channel for at least `connectionTimeout`.	

	如果有未完成的 RPC 请求，但通道上没有流量，则 RPC 同级间建立的连接标记为空闲和关闭的的超时时间。

##### Scheduling

- spark.cores.max	

	Default: (not set)	

	Since Version: 0.6.0

	> When running on a standalone deploy cluster or a Mesos cluster in "coarse-grained" sharing mode, the maximum amount of CPU cores to request for the application from across the cluster (not from each machine). If not set, the default will be `spark.deploy.defaultCores` on Spark's standalone cluster manager, or infinite (all available cores) on Mesos.	

	当在独立部署集群或以粗粒度的共享模式的 Mesos 集群下运行，应用程序从整个集群（不是每个机器）请求的 CPU 核心的最大数量。

	如果不设置，在独立集群管理器下，默认是 `spark.deploy.defaultCores` 值，在 Mesos 下，是无限（所有可用的核心）。

- spark.locality.wait	

	Default: 3s	

	Since Version: 0.5.0

	> How long to wait to launch a data-local task before giving up and launching it on a less-local node. The same wait will be used to step through multiple locality levels (process-local, node-local, rack-local and then any). It is also possible to customize the waiting time for each level by setting `spark.locality.wait.node`, etc. You should increase this setting if your tasks are long and see poor locality, but the default usually works well.	

	放弃启动数据本地化任务、在启动少本地节点上启动任务前，等待的时长。此等待时长可用于多种本地化级别（进程本地化、节点本地化、机架本地化和任意其他）。

	通过设置 `spark.locality.wait.node` 等配置，可为每个级别自定义等待时间。

	如果你的任务运行时间长，且局部性差，你应该增加这个值，但是通常默认值即可。

- spark.locality.wait.node	

	Default: spark.locality.wait	

	Since Version: 0.8.0

	> Customize the locality wait for node locality. For example, you can set this to 0 to skip node locality and search immediately for rack locality (if your cluster has rack information).	

	为节点本地化自定义本地等待时长。例如，将这个配置设为0，可以跳过节点本地化，立即搜索机架本地化（如果你的集群有机架信息）。

- spark.locality.wait.process	

	Default: spark.locality.wait	

	Since Version: 0.8.0

	> Customize the locality wait for process locality. This affects tasks that attempt to access cached data in a particular executor process.	
	
	为进程本地化自定义本地等待时长。这会影响尝试访问特定 executor 进程中的缓存数据的任务。

- spark.locality.wait.rack	

	Default: spark.locality.wait	

	Since Version: 0.8.0

	> Customize the locality wait for rack locality.	

	为机架本地化自定义本地等待时长。

- spark.scheduler.maxRegisteredResourcesWaitingTime	

	Default: 30s	

	Since Version: 1.1.1

	> Maximum amount of time to wait for resources to register before scheduling begins.	

	在调度开始前，注册资源等待的最大时间。

- spark.scheduler.minRegisteredResourcesRatio	

	Default: 0.8 for KUBERNETES mode; 0.8 for YARN mode; 0.0 for standalone mode and Mesos coarse-grained mode	

	Since Version: 1.1.1

	> The minimum ratio of registered resources (registered resources / total expected resources) (resources are executors in yarn mode and Kubernetes mode, CPU cores in standalone mode and Mesos coarse-grained mode ['spark.cores.max' value is total expected resources for Mesos coarse-grained mode] ) to wait for before scheduling begins. Specified as a double between 0.0 and 1.0. Regardless of whether the minimum ratio of resources has been reached, the maximum amount of time it will wait before scheduling begins is controlled by config `spark.scheduler.maxRegisteredResourcesWaitingTime`.	

	在调度开始前，需要等待的注册资源的最小比例（注册资源/总预期资源）（在 yarn 模式和 Kubernetes 模式下，资源是 executors, 在独立模式和 Mesos 粗粒度模式下，资源是 CPU 核心[对于 Mesos 模式，'spark.cores.max' 值是总预期资源]）。

	值为 0.0 和 1.0 之间的双精度值。不管资源最小比例是否达到，在调度开始前，它将等待的最大时间由 `spark.scheduler.maxRegisteredResourcesWaitingTime` 控制。

- spark.scheduler.mode	

	Default: FIFO	

	Since Version: 0.8.0

	> The [scheduling mode](https://spark.apache.org/docs/3.3.2/job-scheduling.html#scheduling-within-an-application) between jobs submitted to the same SparkContext. Can be set to FAIR to use fair sharing instead of queueing jobs one after another. Useful for multi-user services.	

	在作业被提交到相同的 SparkContext 前，使用的调度模式。设置为 FAIR 使用公平共享，而不是队列。对于多用户服务是有用的。

- spark.scheduler.revive.interval	

	Default: 1s	

	Since Version: 0.8.1

	> The interval length for the scheduler to revive the worker resource offers to run tasks.	

	调度器恢复 worker 资源的间隔长度。

- spark.scheduler.listenerbus.eventqueue.capacity	

	Default: 10000	

	Since Version: 2.3.0

	> The default capacity for event queues. Spark will try to initialize an event queue using capacity specified by `spark.scheduler.listenerbus.eventqueue.queueName.capacity` first. If it's not configured, Spark will use the default capacity specified by this config. Note that capacity must be greater than 0. Consider increasing value (e.g. 20000) if listener events are dropped. Increasing this value may result in the driver using more memory.	

	事件队列的默认容量。

	spark 将首先尝试使用 `spark.scheduler.listenerbus.eventqueue.queueName.capacity` 指定的容量初始化事件队列。如果未配置，spark 将使用这个配置指定的默认容量。注意，容量必须大于0. 
	
	如果监听器事件已被删除，那么考虑增加此值（例如20000）。增加此值可能会造成驱动使用更多内存。

- spark.scheduler.listenerbus.eventqueue.shared.capacity	

	Default: spark.scheduler.listenerbus.eventqueue.capacity	

	Since Version: 3.0.0

	> Capacity for shared event queue in Spark listener bus, which hold events for external listener(s) that register to the listener bus. Consider increasing value, if the listener events corresponding to shared queue are dropped. Increasing this value may result in the driver using more memory.	

	spark 监听器总线中，共享事件队列的容量，该队列为注册到监听器总线的外部监听器保存事件。

	如果对应于共享队列的监听器事件被删除，考虑增加这个值。增加此值可能会造成驱动使用更多内存。

- spark.scheduler.listenerbus.eventqueue.appStatus.capacity	

	Default: spark.scheduler.listenerbus.eventqueue.capacity	

	Since Version: 3.0.0

	> Capacity for appStatus event queue, which hold events for internal application status listeners. Consider increasing value, if the listener events corresponding to appStatus queue are dropped. Increasing this value may result in the driver using more memory.	

	appStatus 事件队列的容量，该队列为内部应用程序状态监听器保存事件。

	如果对应于 appStatus 队列的监听器事件被删除，考虑增加这个值。增加此值可能会造成驱动使用更多内存。

- spark.scheduler.listenerbus.eventqueue.executorManagement.capacity	

	Default: spark.scheduler.listenerbus.eventqueue.capacity	

	Since Version: 3.0.0

	> Capacity for executorManagement event queue in Spark listener bus, which hold events for internal executor management listeners. Consider increasing value if the listener events corresponding to executorManagement queue are dropped. Increasing this value may result in the driver using more memory.	

	spark 监听器总线中，executorManagement 事件队列的容量，该队列为内部 executor 管理监听器保存事件。

	如果对应于 executorManagement 队列的监听器事件被删除，考虑增加这个值。增加此值可能会造成驱动使用更多内存。	

- spark.scheduler.listenerbus.eventqueue.eventLog.capacity	

	Default: spark.scheduler.listenerbus.eventqueue.capacity	

	Since Version: 3.0.0

	> Capacity for eventLog queue in Spark listener bus, which hold events for Event logging listeners that write events to eventLogs. Consider increasing value if the listener events corresponding to eventLog queue are dropped. Increasing this value may result in the driver using more memory.	

	spark 监听器总线中，eventLog 事件队列的容量，该队列为将事件写入 eventLogs 的事件记录监听器保存事件。

	如果对应于 eventLog 队列的监听器事件被删除，考虑增加这个值。增加此值可能会造成驱动使用更多内存。

- spark.scheduler.listenerbus.eventqueue.streams.capacity	

	Default: spark.scheduler.listenerbus.eventqueue.capacity	

	Since Version: 3.0.0

	> Capacity for streams queue in Spark listener bus, which hold events for internal streaming listener. Consider increasing value if the listener events corresponding to streams queue are dropped. Increasing this value may result in the driver using more memory.	

	spark 监听器总线中，streams 事件队列的容量，该队列为流式监听器保存事件。

	如果对应于 streams 队列的监听器事件被删除，考虑增加这个值。增加此值可能会造成驱动使用更多内存。

- spark.scheduler.resource.profileMergeConflicts	

	Default: false	

	Since Version: 3.1.0

	> If set to "true", Spark will merge ResourceProfiles when different profiles are specified in RDDs that get combined into a single stage. When they are merged, Spark chooses the maximum of each resource and creates a new ResourceProfile. The default of false results in Spark throwing an exception if multiple different ResourceProfiles are found in RDDs going into the same stage.	

	如果设为 true, 那么在 RDDs 中指定了不同的文件，这些 RDDs 将被合并到一个 stage 中时，spark 将合并 ResourceProfiles. 当它们被合并时，spark 选择每个资源的最大值，并创建一个新的 ResourceProfiles.

	如果在进入相同 stage 的 RDDs 中找到了不同的 ResourceProfiles, 默认的 false 会导致 spark 抛出异常。

- spark.scheduler.excludeOnFailure.unschedulableTaskSetTimeout	

	Default: 120s	

	Since Version: 2.4.1

	> The timeout in seconds to wait to acquire a new executor and schedule a task before aborting a TaskSet which is unschedulable because all executors are excluded due to task failures.	

	在停止一个 TaskSet 之前，获取一个新的 executor 并调度一个任务等待的超时时长，这个 TaskSet 是由于任务失败导致的所有的 executor 被被排除在外而导致的不可调度。

- spark.excludeOnFailure.enabled	

	Default: false	

	Since Version: 2.1.0

	> If set to "true", prevent Spark from scheduling tasks on executors that have been excluded due to too many task failures. The algorithm used to exclude executors and nodes can be further controlled by the other "spark.excludeOnFailure" configuration options.	

	如果设为 true, 阻止 spark 在被排除在外的 executors 上调度任务，此类 executors 是由于太多任务失败导致被排除在外。

	排除 executors 的算法和节点可以通过 "spark.excludeOnFailure" 配置控制。

- spark.excludeOnFailure.timeout	

	Default: 1h	

	Since Version: 2.1.0

	> (Experimental) How long a node or executor is excluded for the entire application, before it is unconditionally removed from the excludelist to attempt running new tasks.	

	（实验）对于一个整个应用程序，在节点或 executor 从列表中移除，以运行新任务前，被排除在外多久，

- spark.excludeOnFailure.task.maxTaskAttemptsPerExecutor	

	Default: 1	

	Since Version: 2.1.0

	> (Experimental) For a given task, how many times it can be retried on one executor before the executor is excluded for that task.	

	（实验）对于一个给定任务，在一个 executor 被排除前，该任务在此 executor 上重试的次数。

- spark.excludeOnFailure.task.maxTaskAttemptsPerNode	

	Default: 2	

	Since Version: 2.1.0

	> (Experimental) For a given task, how many times it can be retried on one node, before the entire node is excluded for that task.	

	（实验）对于一个给定任务，在一个节点被排除前，该任务在此节点上重试的次数。

- spark.excludeOnFailure.stage.maxFailedTasksPerExecutor	

	Default: 2	

	Since Version: 2.1.0

	> (Experimental) How many different tasks must fail on one executor, within one stage, before the executor is excluded for that stage.	

	（实验）在一个 stage 内，在 executor 被排除前，必须要有多少不同的任务在此 executor 上失败。  

- spark.excludeOnFailure.stage.maxFailedExecutorsPerNode	

	Default: 2	

	Since Version: 2.1.0

	> (Experimental) How many different executors are marked as excluded for a given stage, before the entire node is marked as failed for the stage.	

	（实验）对于一个 stage, 在整个节点被标记为失败前，必须要有多少不同的 executors 被标记为被排除。  

- spark.excludeOnFailure.application.maxFailedTasksPerExecutor	

	Default: 2	

	Since Version: 2.2.0

	> (Experimental) How many different tasks must fail on one executor, in successful task sets, before the executor is excluded for the entire application. Excluded executors will be automatically added back to the pool of available resources after the timeout specified by `spark.excludeOnFailure.timeout`. Note that with dynamic allocation, though, the executors may get marked as idle and be reclaimed by the cluster manager.	

	（实验）对于整个应用程序，在 executor 被排除前，在一个成功的任务集中，必须要有多少不同的任务在这个 executor 上失败。 

	在 `spark.excludeOnFailure.timeout` 指定的超时时间之后，排除的 executors 将自动添加回可用资源池中。

	注意，如果使用了动态分配，那么将 executors 标记为空闲，并由集群管理器回收。

- spark.excludeOnFailure.application.maxFailedExecutorsPerNode	

	Default: 2	

	Since Version: 2.2.0

	> (Experimental) How many different executors must be excluded for the entire application, before the node is excluded for the entire application. Excluded nodes will be automatically added back to the pool of available resources after the timeout specified by `spark.excludeOnFailure.timeout`. Note that with dynamic allocation, though, the executors on the node may get marked as idle and be reclaimed by the cluster manager.	

	（实验）对于整个应用程序，在节点被排除前，必须要有多少不同的 executors 被排除在外。 

	在 `spark.excludeOnFailure.timeout` 指定的超时时间之后，排除的节点将自动添加回可用资源池中。

	注意，如果使用了动态分配，那么将节点上的 executors 标记为空闲，并由集群管理器回收。

- spark.excludeOnFailure.killExcludedExecutors	

	Default: false	

	Since Version: 2.2.0

	> (Experimental) If set to "true", allow Spark to automatically kill the executors when they are excluded on fetch failure or excluded for the entire application, as controlled by `spark.killExcludedExecutors.application.*`. Note that, when an entire node is added excluded, all of the executors on that node will be killed.	

	（实验）如果设为 true, 那么就允许 spark 在获取失败而被被排除或整个应用程序被排除时自动杀死 executors, 这由 `spark.killExcludedExecutors.application.*` 控制。

	注意，当一个节点上被排除，那么该节点上的所有 executors 将被杀死。

- spark.excludeOnFailure.application.fetchFailure.enabled	

	Default: false	

	Since Version: 2.3.0

	> (Experimental) If set to "true", Spark will exclude the executor immediately when a fetch failure happens. If external shuffle service is enabled, then the whole node will be excluded.	

	（实验）如果设为 true, 那么在获取失败时，spark 将立即排除 executor. 如果启用了外部 shuffle, 那么整个节点将被排除。

- spark.speculation	

	Default: false	

	Since Version: 0.6.0

	> If set to "true", performs speculative execution of tasks. This means if one or more tasks are running slowly in a stage, they will be re-launched.	

	如果设为 true, 执行任务的推测执行。这意味着如果一个或多个任务在一个 stage 中运行缓慢，它们将被重新启动。

- spark.speculation.interval	

	Default: 100ms	

	Since Version: 0.6.0

	> How often Spark will check for tasks to speculate.	

	spark 检查任务开始推测执行的频率

- spark.speculation.multiplier	

	Default: 1.5	

	Since Version: 0.6.0

	> How many times slower a task is than the median to be considered for speculation.	

	一个任务要比中位数慢多少次，才会被考虑推测执行

- spark.speculation.quantile	

	Default: 0.75	

	Since Version: 0.6.0

	> Fraction of tasks which must be complete before speculation is enabled for a particular stage.	

	对于一个特定的 stage, 在推测执行前，任务完成的比例。

- spark.speculation.minTaskRuntime	

	Default: 100ms	

	Since Version: 3.2.0

	> Minimum amount of time a task runs before being considered for speculation. This can be used to avoid launching speculative copies of tasks that are very short.	

	在考虑推测执行前，一个任务运行的最小时间。这可以用于避免启动短时任务的推测副本。

- spark.speculation.task.duration.threshold	

	Default: None	

	Since Version: 3.0.0

	> Task duration after which scheduler would try to speculative run the task. If provided, tasks would be speculatively run if current stage contains less tasks than or equal to the number of slots on a single executor and the task is taking longer time than the threshold. This config helps speculate stage with very few tasks. Regular speculation configs may also apply if the executor slots are large enough. E.g. tasks might be re-launched if there are enough successful runs even though the threshold hasn't been reached. The number of slots is computed based on the conf values of `spark.executor.cores` and `spark.task.cpus` minimum 1. Default unit is bytes, unless otherwise specified.	

	调度器将尝试推测运行任务之后，任务的持续时间。

	如果设置了此值，且当前 stage 保护的任务数小于等于一个 executor 上的槽的数量，且任务花费的时间超过这个阈值，那么任务将推测执行。这个配置帮助推测具有少数几个任务的 stage. 

	如果 executor 槽足够大，也将会应用常规的推测配置。例如，如果有足够多的成功运行，即使未达到阈值，任务也可能会重新启动。

- spark.task.cpus	

	Default: 1	

	Since Version: 0.5.0

	> Number of cores to allocate for each task.	

	为每个任务分配的核心数

- spark.task.resource.{resourceName}.amount	

	Default: 1	

	Since Version: 3.0.0

	> Amount of a particular resource type to allocate for each task, note that this can be a double. If this is specified you must also provide the executor config `spark.executor.resource.{resourceName}.amount` and any corresponding discovery configs so that your executors are created with that resource type. In addition to whole amounts, a fractional amount (for example, 0.25, which means 1/4th of a resource) may be specified. Fractional amounts must be less than or equal to 0.5, or in other words, the minimum amount of resource sharing is 2 tasks per resource. Additionally, fractional amounts are floored in order to assign resource slots (e.g. a 0.2222 configuration, or 1/0.2222 slots will become 4 tasks/resource, not 5).	

	为每个任务分配的特定资源类型的数量，这可以是 double 类型。如果指定了这个值，那么为了能使用那个资源类型创建 executors，你必须指定 `spark.executor.resource.{resourceName}.amount` 和任意相应的发现配置。

	除了整个的数量，也可以指定一个比例的量（0.25表示资源的1/4）。比例的量必须小于等于0.5，换句话说，共享资源的最小量是两个任务共享一个资源。另外，为了分配资源槽，比例的量是向下取整（0.2222配置或1/0.2222槽将是四个任务共享一个资源，而不是5）。

- spark.task.maxFailures	

	Default: 4	

	Since Version: 0.8.0

	> Number of continuous failures of any particular task before giving up on the job. The total number of failures spread across different tasks will not cause the job to fail; a particular task has to fail this number of attempts continuously. If any attempt succeeds, the failure count for the task will be reset. Should be greater than or equal to 1. Number of allowed retries = this value - 1.	

	在放弃一个任务前，该任务连续失败的次数。不同任务的失败总数不会造成作业失败；一个特定任务必须连续失败这个次数。

	如果任意的尝试成功，任务的失败次数将被重置。应该大于等于1。允许的重试次数等于这个值减1。

- spark.task.reaper.enabled	

	Default: false	

	Since Version: 2.0.3

	> Enables monitoring of killed / interrupted tasks. When set to true, any task which is killed will be monitored by the executor until that task actually finishes executing. See the other `spark.task.reaper.*` configurations for details on how to control the exact behavior of this monitoring. When set to false (the default), task killing will use an older code path which lacks such monitoring.	

	启用杀死/中断任务的监控。当设为 true 时，任意被杀死的任务将被 executor 监控，直到任务真正的完成执行。当设为 false 时（默认），杀死的任务将使用更老的代码路径，也就是缺少监控。

- spark.task.reaper.pollingInterval	

	Default: 10s	

	Since Version: 2.0.3

	> When spark.task.reaper.enabled = true, this setting controls the frequency at which executors will poll the status of killed tasks. If a killed task is still running when polled then a warning will be logged and, by default, a thread-dump of the task will be logged (this thread dump can be disabled via the `spark.task.reaper.threadDump` setting, which is documented below).	

	当 spark.task.reaper.enabled = true, 这个配置控制着 executors 轮询杀死任务状态的频率。在轮询时，如果一个杀死的任务仍在运行，那么就会记录一个警告，和任务的线程转储（这个线程转储可通过 `spark.task.reaper.threadDump` 设置禁用）。

- spark.task.reaper.threadDump	

	Default: true	

	Since Version: 2.0.3

	> When spark.task.reaper.enabled = true, this setting controls whether task thread dumps are logged during periodic polling of killed tasks. Set this to false to disable collection of thread dumps.	

	当 spark.task.reaper.enabled = true, 这个配置控制着，在杀死任务的周期轮询期间，是否记录任务线程转储。设为 false 禁用线程转储的收集。

- spark.task.reaper.killTimeout	

	Default: -1	

	Since Version: 2.0.3

	> When spark.task.reaper.enabled = true, this setting specifies a timeout after which the executor JVM will kill itself if a killed task has not stopped running. The default value, -1, disables this mechanism and prevents the executor from self-destructing. The purpose of this setting is to act as a safety-net to prevent runaway noncancellable tasks from rendering an executor unusable.	

	当 spark.task.reaper.enabled = true, 如果一个杀死的任务未停止运行，这个配置指定了一个超时时长，在超过这个时长后，executor JVM 将杀死他自己。

	默认值-1禁用这个机制，并阻止 executor 自我销毁。此设置的目的是充当安全网，以防止失控的不可取消任务使 executor 不可用。

- spark.stage.maxConsecutiveAttempts	

	Default: 4	

	Since Version: 2.2.0

	> Number of consecutive stage attempts allowed before a stage is aborted.	

	在一个 stage 中止前，允许的连续 stage 尝试的次数。

##### Barrier Execution Mode

- spark.barrier.sync.timeout	

	Default: 365d	

	Since Version: 2.4.0

	> The timeout in seconds for each `barrier()` call from a barrier task. If the coordinator didn't receive all the sync messages from barrier tasks within the configured time, throw a SparkException to fail all the tasks. The default value is set to 31536000(3600 * 24 * 365) so the `barrier()` call shall wait for one year.	

	一个屏障任务调用 `barrier()` 的超时时长。在配置的时间内，如果协调器没有接收到来自屏障任务的所有同步消息，那么任务就会抛出 SparkException.

	默认值是31536000(3600 * 24 * 365)，所以 `barrier()` 调用将等待一年。

- spark.scheduler.barrier.maxConcurrentTasksCheck.interval	

	Default: 15s	

	Since Version: 2.4.0

	> Time in seconds to wait between a max concurrent tasks check failure and the next check. A max concurrent tasks check ensures the cluster can launch more concurrent tasks than required by a barrier stage on job submitted. The check can fail in case a cluster has just started and not enough executors have registered, so we wait for a little while and try to perform the check again. If the check fails more than a configured max failure times for a job then fail current job submission. Note this config only applies to jobs that contain one or more barrier stages, we won't perform the check on non-barrier jobs.	

	在一次最大的并发任务检查失败和下次检查间的等待时长。

	一次最大的并发任务检查确保，集群能启动比提交作业上的屏障 stage 要求的更多的并发任务。

	当集群刚好启动，并没有注册足够的 executors 时，检查就会失败，所以要等一会，尝试再次执行检查。

	如果检查失败次数超过了配置的一个作业最大失败次数，那么当前的作业提交就会失败。

	注意，这个配置仅适用于包含一个或多个屏障 stages 的作业，不会在没有屏障的作业上执行检查。

- spark.scheduler.barrier.maxConcurrentTasksCheck.maxFailures	

	Default: 40	

	Since Version: 2.4.0

	> Number of max concurrent tasks check failures allowed before fail a job submission. A max concurrent tasks check ensures the cluster can launch more concurrent tasks than required by a barrier stage on job submitted. The check can fail in case a cluster has just started and not enough executors have registered, so we wait for a little while and try to perform the check again. If the check fails more than a configured max failure times for a job then fail current job submission. Note this config only applies to jobs that contain one or more barrier stages, we won't perform the check on non-barrier jobs.	

	在一个作业提交失败前，允许的最大的并发任务检查失败的次数。

	一次最大的并发任务检查确保，集群能启动比提交作业上的屏障 stage 要求的更多的并发任务。

	当集群刚好启动，并没有注册足够的 executors 时，检查就会失败，所以要等一会，尝试再次执行检查。

	如果检查失败次数超过了配置的一个作业最大失败次数，那么当前的作业提交就会失败。

	注意，这个配置仅适用于包含一个或多个屏障 stages 的作业，不会在没有屏障的作业上执行检查。

##### Dynamic Allocation

- spark.dynamicAllocation.enabled	

	Default: false	

	Since Version: 1.2.0

	> Whether to use dynamic resource allocation, which scales the number of executors registered with this application up and down based on the workload. For more detail, see the description [here](https://spark.apache.org/docs/3.3.2/job-scheduling.html#dynamic-resource-allocation).

	是否使用动态资源分配，根据工作负载，上下调整此应用程序注册的 executors 数量。

	> This requires `spark.shuffle.service.enabled` or `spark.dynamicAllocation.shuffleTracking.enabled` to be set. The following configurations are also relevant: `spark.dynamicAllocation.minExecutors`, `spark.dynamicAllocation.maxExecutors`, and `spark.dynamicAllocation.initialExecutors` `spark.dynamicAllocation.executorAllocationRatio`	

	这要求设置 `spark.shuffle.service.enabled` 或 `spark.dynamicAllocation.shuffleTracking.enabled`

- spark.dynamicAllocation.executorIdleTimeout	

	Default: 60s	

	Since Version: 1.2.0

	> If dynamic allocation is enabled and an executor has been idle for more than this duration, the executor will be removed. For more detail, see this [description](https://spark.apache.org/docs/3.3.2/job-scheduling.html#resource-allocation-policy).

	如果启用了动态分配，并且超过这个时间，一个 executor 是空闲的，那么这个将移除这个 executor

- spark.dynamicAllocation.cachedExecutorIdleTimeout	

	Default: infinity	

	Since Version: 1.4.0

	> If dynamic allocation is enabled and an executor which has cached data blocks has been idle for more than this duration, the executor will be removed. For more details, see this [description](https://spark.apache.org/docs/3.3.2/job-scheduling.html#resource-allocation-policy).	

	如果启用了动态分配，并且超过这个时间，一个具有缓存数据块的 executor 是空闲的，那么这个将移除这个 executor

- spark.dynamicAllocation.initialExecutors	

	Default: spark.dynamicAllocation.minExecutors	

	Since Version: 1.3.0

	> Initial number of executors to run if dynamic allocation is enabled.

	如果启用了动态分配，运行 executors 的初始数量。

	> If `--num-executors` (or `spark.executor.instances`) is set and larger than this value, it will be used as the initial number of executors.	

	如果设置了 `--num-executors` 或 `spark.executor.instances`，且大于这个值，那么它将作为 executors 的初始值使用。

- spark.dynamicAllocation.maxExecutors	

	Default: infinity	

	Since Version: 1.2.0

	> Upper bound for the number of executors if dynamic allocation is enabled.	

	如果启用了动态分配，executors 数量的上界。

- spark.dynamicAllocation.minExecutors	

	Default: 0	

	Since Version: 1.2.0

	> Lower bound for the number of executors if dynamic allocation is enabled.	

	如果启用了动态分配，executors 数量的下界。

- spark.dynamicAllocation.executorAllocationRatio	

	Default: 1	

	Since Version: 2.4.0

	> By default, the dynamic allocation will request enough executors to maximize the parallelism according to the number of tasks to process. While this minimizes the latency of the job, with small tasks this setting can waste a lot of resources due to executor allocation overhead, as some executor might not even do any work. This setting allows to set a ratio that will be used to reduce the number of executors w.r.t. full parallelism. Defaults to 1.0 to give maximum parallelism. 0.5 will divide the target number of executors by 2 The target number of executors computed by the dynamicAllocation can still be overridden by the `spark.dynamicAllocation.minExecutors` and `spark.dynamicAllocation.maxExecutors` settings	

	默认情况下，动态分配将根据处理的任务数量，请求足够的 executors 来最大化并行度。

	当这个最小化了具有小任务的作业的延迟，那么这个设置会由于 executor 分配开销，浪费很多资源，因为一些 executor 可能都不会做任何工作。

	这个配置运行设置一个比率，可用于在完全并行的情况下减少 executors 的数量。默认是1，表示给与最大并行度，0.5将 executors 的目标数量除以2，通过 dynamicAllocation 计算而来的 executors 的目标数量仍可被 `spark.dynamicAllocation.minExecutors` 和 `spark.dynamicAllocation.maxExecutors` 覆盖。

- spark.dynamicAllocation.schedulerBacklogTimeout	

	Default: 1s	

	Since Version: 1.2.0

	> If dynamic allocation is enabled and there have been pending tasks backlogged for more than this duration, new executors will be requested. For more detail, see this [description](https://spark.apache.org/docs/3.3.2/job-scheduling.html#resource-allocation-policy).	

	如果启用了动态分配，并且超过这个时间，存在积压的挂起的任务，将请求新的 executors

- spark.dynamicAllocation.sustainedSchedulerBacklogTimeout	

	Default: schedulerBacklogTimeout

	Since Version: 1.2.0

	> Same as `spark.dynamicAllocation.schedulerBacklogTimeout`, but used only for subsequent executor requests. For more detail, see this [description](https://spark.apache.org/docs/3.3.2/job-scheduling.html#resource-allocation-policy).	

	类似 `spark.dynamicAllocation.schedulerBacklogTimeout`, 但是仅用于后续 executor 请求。

- spark.dynamicAllocation.shuffleTracking.enabled	

	Default: false	

	Since Version: 3.0.0

	> Enables shuffle file tracking for executors, which allows dynamic allocation without the need for an external shuffle service. This option will try to keep alive executors that are storing shuffle data for active jobs.	

	启用 executors 的 shuffle 文件追踪，此 executors 允许动态分配，且不需要外部 shuffle 服务。

	这个选项将尝试保持为活动作业存储 shuffle 数据的 executors 活跃。

- spark.dynamicAllocation.shuffleTracking.timeout	

	Default: infinity	

	Since Version: 3.0.0

	> When shuffle tracking is enabled, controls the timeout for executors that are holding shuffle data. The default value means that Spark will rely on the shuffles being garbage collected to be able to release executors. If for some reason garbage collection is not cleaning up shuffles quickly enough, this option can be used to control when to time out executors even when they are storing shuffle data.	

	当启用 shuffle 追踪时，控制 executors 持有 shuffle 数据的时长。

	默认值意味着 Spark 将依赖于垃圾收集的 shuffle 来释放 executors。

	如果由于某种原因，垃圾收集不能足够快地清理 shuffle，则可以使用此选项来控制何时使 executors 超时，即使它们正在存储 shuffle 数据。

##### Thread Configurations

> Depending on jobs and cluster configurations, we can set number of threads in several places in Spark to utilize available resources efficiently to get better performance. Prior to Spark 3.0, these thread configurations apply to all roles of Spark, such as driver, executor, worker and master. From Spark 3.0, we can configure threads in finer granularity starting from driver and executor. Take RPC module as example in below table. For other modules, like shuffle, just replace “rpc” with “shuffle” in the property names except `spark.{driver|executor}.rpc.netty.dispatcher.numThreads`, which is only for RPC module.

取决于作业和集群配置，我们可以在多处设置线程数量，以利用可用的资源获得更换的性能。在 Spark 3.0 版本之前，这些线程配置适用于 spark 的所有角色，例如 driver, executor, worker and master. 从 Spark 3.0 版本开始，可以在 driver 和 executor 上以更细粒度的方式配置线程。

下面以 RPC 模块作为示例，对于像 shuffle 的其他模块，除了 `spark.{driver|executor}.rpc.netty.dispatcher.numThreads` 仅适用于 RPC 模块，其他的只需要将 rpc 替换成 shuffle 即可。

- spark.{driver|executor}.rpc.io.serverThreads	

	Default: Fall back on `spark.rpc.io.serverThreads`	

	Since Version: 1.6.0

	> Number of threads used in the server thread pool	

	在服务器线程池中使用的线程数量

- spark.{driver|executor}.rpc.io.clientThreads	

	Default: Fall back on `spark.rpc.io.clientThreads`	

	Since Version: 1.6.0

	> Number of threads used in the client thread pool	

	在客户端线程池中使用的线程数量

- spark.{driver|executor}.rpc.netty.dispatcher.numThreads	

	Default: Fall back on `spark.rpc.netty.dispatcher.numThreads`	
	
	Since Version: 3.0.0

	> Number of threads used in RPC message dispatcher thread pool	

	在 RPC 消息分发器线程池中使用的线程数量

> The default value for number of thread-related config keys is the minimum of the number of cores requested for the driver or executor, or, in the absence of that value, the number of cores available for the JVM (with a hardcoded upper limit of 8).

线程相关配置的数量默认值就是 driver 或 executor 请求的核心的最小数量，或者，在那个值缺少的情况下，就是 JVM 可用的核心数（硬编码上限为8）

##### Security

> Please refer to the [Security](https://spark.apache.org/docs/3.3.2/security.html) page for available options on how to secure different Spark subsystems.

【接 01_Configuration_2.md】