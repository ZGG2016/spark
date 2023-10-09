# Running Spark on YARN

[TOC]

> Support for running on [YARN (Hadoop NextGen)](http://hadoop.apache.org/docs/stable/hadoop-yarn/hadoop-yarn-site/YARN.html) was added to Spark in version 0.6.0, and improved in subsequent releases.

从 Spark 0.6.0 版本添加对 YARN 的支持。

## Security

> Security features like authentication are not enabled by default. When deploying a cluster that is open to the internet or an untrusted network, it’s important to secure access to the cluster to prevent unauthorized applications from running on the cluster. Please see [Spark Security](https://spark.apache.org/docs/3.3.2/security.html) and the specific security sections in this doc before running Spark.

像身份验证这种安全特性，默认情况下，是关闭的。

当部署一个集群，它面向互联网或不受信网络时，安全访问集群，以保护运行在集群上的未认证的应用程序。

## Launching Spark on YARN

> Ensure that `HADOOP_CONF_DIR` or `YARN_CONF_DIR` points to the directory which contains the (client side) configuration files for the Hadoop cluster. These configs are used to write to HDFS and connect to the YARN ResourceManager. The configuration contained in this directory will be distributed to the YARN cluster so that all containers used by the application use the same configuration. If the configuration references Java system properties or environment variables not managed by YARN, they should also be set in the Spark application’s configuration (driver, executors, and the AM when running in client mode).

确保 `HADOOP_CONF_DIR` or `YARN_CONF_DIR` 指向了 Hadoop 集群中包含配置文件的目录，其作用就是向 HDFS 写数据、连接 YARN ResourceManager.

这个目录中包含的配置文件会被分发到 YARN 集群，所以该应用程序使用的所有 containers 使用相同的配置。

如果配置引用了 Java 系统属性或者未由 YARN 管理的环境变量，则还应在 Spark 应用程序的配置中设置(当在客户端模式下运行时，配置driver, executors, and the AM ).

> There are two deploy modes that can be used to launch Spark applications on YARN. In `cluster` mode, the Spark driver runs inside an application master process which is managed by YARN on the cluster, and the client can go away after initiating the application. In `client` mode, the driver runs in the client process, and the application master is only used for requesting resources from YARN.

支持两种部署模式：

- 集群模式: 驱动运行在 YARN 管理下的应用程序 master 进程，客户端在初始化程序后离开。

- 客户端模式: 驱动运行在客户端进程，应用程序 master 仅被用来向 YARN 请求资源。

> Unlike other cluster managers supported by Spark in which the master’s address is specified in the `--master` parameter, in YARN mode the ResourceManager’s address is picked up from the Hadoop configuration. Thus, the `--master` parameter is `yarn`.

与 Spark 支持的其他集群管理器不同的是, master 地址在 `--master` 参数中指定，而在 YARN 模式下, ResourceManager 的地址从 Hadoop 配置中选取，因此，`--master` 参数是 `yarn`.

> To launch a Spark application in `cluster` mode:

集群模式下启动

	$ ./bin/spark-submit --class path.to.your.Class --master yarn --deploy-mode cluster [options] <app jar> [app options]

For example:

	$ ./bin/spark-submit --class org.apache.spark.examples.SparkPi \
    	--master yarn \
    	--deploy-mode cluster \
    	--driver-memory 4g \
    	--executor-memory 2g \
    	--executor-cores 1 \
    	--queue thequeue \
    	examples/jars/spark-examples*.jar \
    	10

> The above starts a YARN client program which starts the default Application Master. Then SparkPi will be run as a child thread of Application Master. The client will periodically poll the Application Master for status updates and display them in the console. The client will exit once your application has finished running. Refer to the [Debugging your Application](https://spark.apache.org/docs/3.3.2/running-on-yarn.html#debugging-your-application) section below for how to see driver and executor logs.

上面就启动了一个 YARN 客户端程序，它启动了默认的 Application Master. 然后 SparkPi 会作为 Application Master 的子线程运行。

客户端周期地拉取 Application Master, 以获取状态的更新并在控制台中显示它们。 应用程序运行完后，客户端就会退出。

> To launch a Spark application in `client` mode, do the same, but replace cluster with client. The following shows how you can run `spark-shell` in `client` mode:

客户端模式下启动 spark-shell

	$ ./bin/spark-shell --master yarn --deploy-mode client

### Adding Other JARs

> In `cluster` mode, the driver runs on a different machine than the client, so `SparkContext.addJar` won’t work out of the box with files that are local to the client. To make files on the client available to `SparkContext.addJar`, include them with the `--jars` option in the launch command.

在集群模式下，驱动在与客户端不同的机器上运行，因此 `SparkContext.addJar` 将不会立即使用客户端本地的文件运行。

要使客户端上的文件可用于 `SparkContext.addJar`, 请在启动命令中使用 `--jars` 选项来包含这些文件。

	$ ./bin/spark-submit --class my.main.Class \
    	--master yarn \
    	--deploy-mode cluster \
    	--jars my-other-jar.jar,my-other-other-jar.jar \
    	my-main-jar.jar \
    	app_arg1 app_arg2

## Preparations

> Running Spark on YARN requires a binary distribution of Spark which is built with YARN support. Binary distributions can be downloaded from the [downloads page](https://spark.apache.org/downloads.html) of the project website. There are two variants of Spark binary distributions you can download. One is pre-built with a certain version of Apache Hadoop; this Spark distribution contains built-in Hadoop runtime, so we call it `with-hadoop` Spark distribution. The other one is pre-built with user-provided Hadoop; since this Spark distribution doesn’t contain a built-in Hadoop runtime, it’s smaller, but users have to provide a Hadoop installation separately. We call this variant `no-hadoop` Spark distribution. For `with-hadoop` Spark distribution, since it contains a built-in Hadoop runtime already, by default, when a job is submitted to Hadoop Yarn cluster, to prevent jar conflict, it will not populate Yarn’s classpath into Spark. To override this behavior, you can set `spark.yarn.populateHadoopClasspath=true`. For `no-hadoop` Spark distribution, Spark will populate Yarn’s classpath by default in order to get Hadoop runtime. For `with-hadoop` Spark distribution, if your application depends on certain library that is only available in the cluster, you can try to populate the Yarn classpath by setting the property mentioned above. If you run into jar conflict issue by doing so, you will need to turn it off and include this library in your application jar.

在 YARN 之上运行 Spark 要求是含 YARN 支持的 Spark 二进制发行版，可以从项目网站的下载页下载。

有两类二进制发行版，一种就是包含 Apache Hadoop 特定版本的预构建版本，这个版本包含内建 Hadoop 运行时，所以称其为 `with-hadoop` Spark 发行版。另一种是使用用户提供的 Hadoop 的预构建版本，由于这个版本的发行版不含内建 Hadoop 运行时，所以它更小，但需要用户单独安装 Hadoop, 所以称其为 `no-hadoop` Spark 发行版。  

对于 `with-hadoop` Spark 发行版，默认情况下，它包含内建 Hadoop 运行时，当作业被提交到 Hadoop Yarn 集群时，为防止 jar 冲突，它将不会将 Yarn 的类路径添加到 Spark 中。如果要覆盖这种行为，你可以设置 `spark.yarn.populateHadoopClasspath=true`. 

对于 `no-hadoop` Spark 发行版，默认情况下，为了获得 Hadoop 运行时，会将 Yarn 的类路径添加到 Spark 中。

对于 `with-hadoop` Spark 发行版，如果你的应用程序依赖仅在集群可用的特定库，你可以通过设置上述属性，尝试添加 Yarn 的类路径。如果你这么做，发生了 jar 冲突，你将需要关闭它，并将这个库加入到你的应用程序 jar 包中。

> To build Spark yourself, refer to [Building Spark](https://spark.apache.org/docs/3.3.2/building-spark.html).

> To make Spark runtime jars accessible from YARN side, you can specify `spark.yarn.archive` or `spark.yarn.jars`. For details please refer to [Spark Properties](https://spark.apache.org/docs/3.3.2/running-on-yarn.html#spark-properties). If neither `spark.yarn.archive` nor `spark.yarn.jars` is specified, Spark will create a zip file with all jars under `$SPARK_HOME/jars` and upload it to the distributed cache.
 
为了能从 YARN 端访问到 Spark 运行时 jars 包，你可以指定 `spark.yarn.archive` 或者 `spark.yarn.jars`.

如果这两属性都没有指定, Spark 将在 `$SPARK_HOME/jars` 下创建包含所有 jars 包的 zip 文件，并将其上传到分布式缓存中。

## Configuration

> Most of the configs are the same for Spark on YARN as for other deployment modes. See the [configuration page](https://spark.apache.org/docs/3.3.2/configuration.html) for more information on those. These are configs that are specific to Spark on YARN.

关于 Spark on YARN 的大部分配置和其他部署模式是相同。

配置页含有特定于 Spark on YARN 的配置。

## Debugging your Application

> In YARN terminology, executors and application masters run inside “containers”. YARN has two modes for handling container logs after an application has completed. If log aggregation is turned on (with the `yarn.log-aggregation-enable` config), container logs are copied to HDFS and deleted on the local machine. These logs can be viewed from anywhere on the cluster with the yarn logs command.

在 YARN 术语下, executors 和 application masters 运行在 containers 中。 在应用程序完成后, YARN 有两种处理容器日志的模式。

如果启用了日志聚合(`yarn.log-aggregation-enable`)，容器日志会被复制到 HDFS, 并删除本地机器中的容器日志。这些日志就可以使用 yarn 日志命令从集群任意位置访问。

	yarn logs -applicationId <app ID>

> will print out the contents of all log files from all containers from the given application. You can also view the container log files directly in HDFS using the HDFS shell or API. The directory where they are located can be found by looking at your YARN configs (`yarn.nodemanager.remote-app-log-dir` and `yarn.nodemanager.remote-app-log-dir-suffix`). The logs are also available on the Spark Web UI under the Executors Tab. You need to have both the Spark history server and the MapReduce history server running and configure `yarn.log.server.url` in `yarn-site.xml` properly. The log URL on the Spark history server UI will redirect you to the MapReduce history server to show the aggregated logs.

上述命令会打印给定应用程序的所有容器中的所有日志文件。也可以使用 HDFS shell 或 API 在 HDFS 中直接查看日志文件。

通过 `yarn.nodemanager.remote-app-log-dir` 和 `yarn.nodemanager.remote-app-log-dir-suffix` 可以找到日志文件的目录。

这些日志也可以在 Spark Web UI 的 Executors Tab 下访问。但需要 Spark history server 和 MapReduce history server 同时运行，并在 `yarn-site.xml` 里配置 `yarn.log.server.url` 属性。

在 Spark history server UI 上的日志 URL 将重定向到 MapReduce history server, 以展示聚合日志。

> When log aggregation isn’t turned on, logs are retained locally on each machine under `YARN_APP_LOGS_DIR`, which is usually configured to `/tmp/logs` or `$HADOOP_HOME/logs/userlogs` depending on the Hadoop version and installation. Viewing logs for a container requires going to the host that contains them and looking in this directory. Subdirectories organize log files by application ID and container ID. The logs are also available on the Spark Web UI under the Executors Tab and doesn’t require running the MapReduce history server.

当未启用日志聚合时，日志将保留在每台机器上的本地 `YARN_APP_LOGS_DIR` 目录下，通常配置为 `/tmp/logs` 或者 `$HADOOP_HOME/logs/userlogs`, 具体取决于 Hadoop 版本和安装。

查看一个容器的日志需要到包含它们的主机的此目录中查看。子目录下的日志文件根据应用程序 ID 和容器 ID 组织。日志还可以在 Spark Web UI 的 Executors Tab 下找到，并且不需要运行 MapReduce history server.

> To review per-container launch environment, increase `yarn.nodemanager.delete.debug-delay-sec` to a large value (e.g. 36000), and then access the application cache through `yarn.nodemanager.local-dirs` on the nodes on which containers are launched. This directory contains the launch script, JARs, and all environment variables used for launching each container. This process is useful for debugging classpath problems in particular. (Note that enabling this requires admin privileges on cluster settings and a restart of all node managers. Thus, this is not applicable to hosted clusters).

要查看每个容器的启动环境，要将 `yarn.nodemanager.delete.debug-delay-sec` 设置为一个较大的值（例如 36000），然后通过节点上的 `yarn.nodemanager.local-dirs` 访问应用程序缓存，节点是容器启动的节点。

此目录包含启动脚本、JARs、和用于启动每个容器的所有环境变量。这个过程对于调试类路径问题特别有用。

请注意，启用此功能需要集群设置的管理员权限，并且还需要重新启动所有的 node managers, 因此这不适用于托管集群。

> To use a custom log4j configuration for the application master or executors, here are the options:

要为 application master 或者 executors 使用自定义的 log4j 配置，请选择以下选项:

> upload a custom `log4j.properties` using `spark-submit`, by adding it to the `--files` list of files to be uploaded with the application.

- 使用 spark-submit 上传一个自定义的 log4j.properties, 通过将其添加到要与应用程序一起上传的文件的 `--files` 列表中。

> add `-Dlog4j.configuration=<location of configuration file>` to `spark.driver.extraJavaOptions` (for the driver) or `spark.executor.extraJavaOptions` (for executors). Note that if using a file, the `file:` protocol should be explicitly provided, and the file needs to exist locally on all the nodes.

- 将 `-Dlog4j.configuration=<location of configuration file>` 添加到 `spark.driver.extraJavaOptions`(for the driver) 或 `spark.executor.extraJavaOptions`(for executors)。注意：如果使用了文件，应明确提供 `file:` 协议，文件需要存在于本地的所有节点。

> update the `$SPARK_CONF_DIR/log4j.properties` file and it will be automatically uploaded along with the other configurations. Note that other 2 options has higher priority than this option if multiple options are specified.

- 更新 `$SPARK_CONF_DIR/log4j.properties `
文件，并且它将与其他配置一起自动上传。注意：如果指定了多个选项，其他 2 个选项的优先级高于此选项。

> Note that for the first option, both executors and the application master will share the same log4j configuration, which may cause issues when they run on the same node (e.g. trying to write to the same log file).

注意：对于第一个选项, executors 和 application master 将共享相同的 log4j 配置，当它们运行在同一个节点上时，可能会导致问题（例如，试图写入相同的日志文件）。

> If you need a reference to the proper location to put log files in the YARN so that YARN can properly display and aggregate them, use `spark.yarn.app.container.log.dir` in your `log4j.properties`. For example, `log4j.appender.file_appender.File=${spark.yarn.app.container.log.dir}/spark.log`. For streaming applications, configuring `RollingFileAppender` and setting file location to YARN’s log directory will avoid disk overflow caused by large log files, and logs can be accessed using YARN’s log utility.

为了 YARN 可以正确显示和聚合日志文件，需要将日志文件的正确位置放在 YARN 中，通过在 `log4j.properties` 中设置 `spark.yarn.app.container.log.dir`.

例如，`log4j.appender.file_appender.File=${spark.yarn.app.container.log.dir}/spark.log`

对于流应用程序，配置 `RollingFileAppender` 并将文件路径设置为 YARN 的日志目录 将避免由于大型日志文件导致的磁盘溢出，并且可以使用 YARN 的日志实用程序访问日志。

> To use a custom `metrics.properties` for the application master and executors, update the `$SPARK_CONF_DIR/metrics.properties` file. It will automatically be uploaded with other configurations, so you don’t need to specify it manually with `--files`.

要为 application master 和 executors 使用一个自定义的 `metrics.properties`, 请更新 `$SPARK_CONF_DIR/metrics.properties` 文件。它将自动与其他配置一起上传，因此不需要使用 `--files` 手动指定。

### Spark Properties

- spark.yarn.am.memory	

	Default: 512m	

	Since Version: 1.3.0
	
	> Amount of memory to use for the YARN Application Master in client mode, in the same format as JVM memory strings (e.g. `512m`, `2g`). In cluster mode, use `spark.driver.memory` instead.
	Use lower-case suffixes, e.g. `k`, `m`, `g`, `t`, and `p`, for `kibi-`, `mebi-`, `gibi-`, `tebi-`, and pebibytes, respectively.
	
	在客户端模式下, YARN Application Master 使用的内存量，和 JVM 内存字符串格式相同。

	在集群模式下，使用 `spark.driver.memory` 代替。

	使用小写前缀，例如 `k`, `m`, `g`, `t`, and `p` 对应于 `kibi-`, `mebi-`, `gibi-`, `tebi-`, and pebibytes,

- spark.yarn.am.resource.{resource-type}.amount	

	Default: (none)	

	Since Version: 3.0.0

	> Amount of resource to use for the YARN Application Master in client mode. In cluster mode, use `spark.yarn.driver.resource.<resource-type>.amount` instead. Please note that this feature can be used only with YARN 3.0+ For reference, see YARN Resource Model documentation: https://hadoop.apache.org/docs/r3.0.1/hadoop-yarn/hadoop-yarn-site/ResourceModel.html

	在客户端模式下, YARN Application Master 使用的资源量。

	在集群模式下，使用 `spark.yarn.driver.resource.<resource-type>.amount` 代替。

	此特性仅用于 YARN 3.0+.

	Example: To request GPU resources from YARN, use: `spark.yarn.am.resource.yarn.io/gpu.amount`

- spark.yarn.applicationType	

	Default: SPARK	
	
	Since Version: 3.1.0

	> Defines more specific application types, e.g. `SPARK`, `SPARK-SQL`, `SPARK-STREAMING`, `SPARK-MLLIB` and `SPARK-GRAPH`. Please be careful not to exceed 20 characters.	

	定义更多的应用程序类型，可以是 `SPARK`, `SPARK-SQL`, `SPARK-STREAMING`, `SPARK-MLLIB` and `SPARK-GRAPH`.

	不要超过20个字符。

- spark.yarn.driver.resource.{resource-type}.amount	

	Default: (none)	
	
	Since Version: 3.0.0

	> Amount of resource to use for the YARN Application Master in cluster mode. Please note that this feature can be used only with YARN 3.0+ For reference, see YARN Resource Model documentation: https://hadoop.apache.org/docs/r3.0.1/hadoop-yarn/hadoop-yarn-site/ResourceModel.html
	
	在集群模式下, YARN Application Master 使用的资源量。

	此特性仅用于 YARN 3.0+.

	Example: To request GPU resources from YARN, use: `spark.yarn.driver.resource.yarn.io/gpu.amount`

- spark.yarn.executor.resource.{resource-type}.amount	

	Default: (none)	
	
	Since Version: 3.0.0

	> Amount of resource to use per executor process. Please note that this feature can be used only with YARN 3.0+ For reference, see YARN Resource Model documentation: https://hadoop.apache.org/docs/r3.0.1/hadoop-yarn/hadoop-yarn-site/ResourceModel.html
	
	每个 executor 进程使用的资源量。

	此特性仅用于 YARN 3.0+.

	Example: To request GPU resources from YARN, use: `spark.yarn.executor.resource.yarn.io/gpu.amount`

- spark.yarn.resourceGpuDeviceName	

	Default: yarn.io/gpu	
	
	Since Version: 3.2.1

	> Specify the mapping of the Spark resource type of gpu to the YARN resource representing a GPU. By default YARN uses `yarn.io/gpu` but if YARN has been configured with a custom resource type, this allows remapping it. Applies when using the `spark.{driver/executor}.resource.gpu.*` configs.	

	指定将 gpu 的 Spark 资源类型映射到表示 gpu 的 YARN 资源。默认情况下, YARN 使用 `yarn.io/gpu`, 但是如果使用自定义的资源类型配置 YARN, 那么这就允许再次映射。

	当使用 `spark.{driver/executor}.resource.gpu.*` 配置时，应用它。

- spark.yarn.resourceFpgaDeviceName	

	Default: yarn.io/fpga	
	
	Since Version: 3.2.1

	> Specify the mapping of the Spark resource type of fpga to the YARN resource representing a FPGA. By default YARN uses `yarn.io/fpga` but if YARN has been configured with a custom resource type, this allows remapping it. Applies when using the `spark.{driver/executor}.resource.fpga.*` configs.	

	指定将 fpga 的 Spark 资源类型映射到表示 fpga 的 YARN 资源。默认情况下, YARN 使用 `yarn.io/fpga`, 但是如果使用自定义的资源类型配置 YARN, 那么这就允许再次映射。

	当使用 `spark.{driver/executor}.resource.fpga.*` 配置时，应用它。

- spark.yarn.am.cores	

	Default: 1	
	
	Since Version: 1.3.0

	> Number of cores to use for the YARN Application Master in `client` mode. In `cluster` mode, use `spark.driver.cores` instead.	
	
	在客户端模式下, YARN Application Master 使用的核心数。

	在集群模式下，使用 `spark.driver.cores` 替代。

- spark.yarn.am.waitTime

	Default: 100s	
	
	Since Version: 1.3.0

	> Only used in `cluster` mode. Time for the YARN Application Master to wait for the SparkContext to be initialized.	

	仅用在集群模式下。

	YARN Application Master 等待 SparkContext 初始化的时间

- spark.yarn.submit.file.replication	

	Default: The default HDFS replication (usually 3)	
	
	Since Version: 0.8.1

	> HDFS replication level for the files uploaded into HDFS for the application. These include things like the Spark jar, the app jar, and any distributed cache files/archives.

	上传到 HDFS 的文件副本数。这也包含了 Spark jar, the app jar 和任意的分布式缓存文件/归档文件。

- spark.yarn.stagingDir	

	Default: Current user's home directory in the filesystem	
	
	Since Version: 2.0.0

	> Staging directory used while submitting applications.	

	在提交应用程序时，使用的暂存目录。

- spark.yarn.preserve.staging.files	

	Default: false	

	Since Version: 1.1.0

	> Set to true to preserve the staged files (Spark jar, app jar, distributed cache files) at the end of the job rather than delete them.	

	设为 true 时，在作业结束时，保存暂存文件(Spark jar, app jar 和分布式缓存文件)，而不是删除它们。

- spark.yarn.scheduler.heartbeat.interval-ms	

	Default: 3000	

	Since Version: 0.8.1
	
	> The interval in ms in which the Spark application master heartbeats into the YARN ResourceManager. The value is capped at half the value of YARN's configuration for the expiry interval, i.e. `yarn.am.liveness-monitor.expiry-interval-ms`.	
	
	Spark application master 向 YARN ResourceManager 发送心跳的间隔（毫秒）

	该值的上限为 YARN 配置的过期间隔值的一半，例如 `yarn.am.liveness-monitor.expiry-interval-ms`.
	
- spark.yarn.scheduler.initial-allocation.interval

	Default: 200ms	

	Since Version: Since Version: 1.4.0

	> The initial interval in which the Spark application master eagerly heartbeats to the YARN ResourceManager when there are pending container allocation requests. It should be no larger than `spark.yarn.scheduler.heartbeat.interval-ms`. The allocation interval will doubled on successive eager heartbeats if pending containers still exist, until `spark.yarn.scheduler.heartbeat.interval-ms` is reached.	
	
	当有挂起的容器分配请求时，Spark application master 立即向 YARN ResourceManager 发送心跳的初始间隔（毫秒）

	它不应大于 `spark.yarn.scheduler.heartbeat.interval-ms`

	如果挂起的容器仍然存在，分配间隔将在连续的心跳中加倍，直到达到 `spark.yarn.scheduler.heartbeat.interval-ms` 值。

- spark.yarn.max.executor.failures	

	Default: numExecutors * 2, with minimum of 3	
	
	Since Version: 1.0.0

	> The maximum number of executor failures before failing the application.	
	
	在应用程序失败之前, executor 失败的最大次数。

- spark.yarn.historyServer.address	

	Default: (none)	

	Since Version: 1.0.0
	
	> The address of the Spark history server, e.g. `host.com:18080`. The address should not contain a scheme (`http://`). Defaults to not being set since the history server is an optional service. This address is given to the YARN ResourceManager when the Spark application finishes to link the application from the ResourceManager UI to the Spark history server UI. For this property, YARN properties can be used as variables, and these are substituted by Spark at runtime. For example, if the Spark history server runs on the same node as the YARN ResourceManager, it can be set to `${hadoopconf-yarn.resourcemanager.hostname}:18080`.	
	
	Spark history server 的地址，例如 `host.com:18080`. 地址不要包含 `http://`.

	因为历史服务是可选的服务，所以默认是不设置这个属性的。

	当 Spark 应用程序完成后，这个地址被提供给 YARN ResourceManager, 用于将应用程序从 ResourceManager UI 链接到 Spark history server UI.

	对于这个属性, YARN 属性可被用作变量，这些属性在运行时由 Spark 替换。例如，如果 Spark history server 所运行的节点和 YARN ResourceManager 相同，那么将被设置为 `${hadoopconf-yarn.resourcemanager.hostname}:18080`.	

- spark.yarn.dist.archives	

	Default: (none)	
	
	Since Version: 1.0.0

	> Comma separated list of archives to be extracted into the working directory of each executor.	
	
	归档文件的列表，由逗号分隔，它们将被抽取到每个 executor 的工作目录。

- spark.yarn.dist.files	

	Default: (none)	

	Since Version: 1.0.0
	
	> Comma-separated list of files to be placed in the working directory of each executor.	
	
	文件的列表，由逗号分隔，它们将被放到每个 executor 的工作目录。

- spark.yarn.dist.jars	

	Default: (none)	
	
	Since Version: 2.0.0

	> Comma-separated list of jars to be placed in the working directory of each executor.	
	
	jars 的列表，由逗号分隔，它们将被放到每个 executor 的工作目录。

- spark.yarn.dist.forceDownloadSchemes	

	Default: (none)	

	Since Version: 2.3.0
	
	> Comma-separated list of schemes for which resources will be downloaded to the local disk prior to being added to YARN's distributed cache. For use in cases where the YARN service does not support schemes that are supported by Spark, like http, https and ftp, or jars required to be in the local YARN client's classpath. Wildcard `'*'` is denoted to download resources for all the schemes.	
	
	计划的列表，由逗号分隔，资源将被优先下载到本地磁盘，而不是添加到 yarn 的分布式缓存中。

	对于 YARN 服务不支持 Spark 支持的计划的情况，就像 http, https 和 ftp, 或者是要求在本地 YARN 客户端的类路径下的 jars.

	通配符 `*` 下载所有计划的资源。

- spark.executor.instances	

	Default: 2	

	Since Version: 1.0.0
	
	> The number of executors for static allocation. With `spark.dynamicAllocation.enabled`, the initial set of executors will be at least this large.	
	
	执行静态分配的 executors 的数量。

	在 `spark.dynamicAllocation.enabled` 启用的情况下, executors 的初始集至少有这么大。

- spark.yarn.am.memoryOverhead	

	Default: AM memory * 0.10, with minimum of 384	
	
	Since Version: 1.3.0

	> Same as `spark.driver.memoryOverhead`, but for the YARN Application Master in client mode.	
	
	和 `spark.driver.memoryOverhead` 相同，但是这是在客户端模式下针对 YARN Application Master.

- spark.yarn.queue

	Default: default	
	
	Since Version: 1.0.0

	> The name of the YARN queue to which the application is submitted.	

	提交应用程序的 YARN 队列的名字。

- spark.yarn.jars	

	Default: (none)	
	
	Since Version: 2.0.0
	
	> List of libraries containing Spark code to distribute to YARN containers. By default, Spark on YARN will use Spark jars installed locally, but the Spark jars can also be in a world-readable location on HDFS. This allows YARN to cache it on nodes so that it doesn't need to be distributed each time an application runs. To point to jars on HDFS, for example, set this configuration to `hdfs:///some/path`. Globs are allowed.	

	包含 Spark 代码的库的列表，它会被分发给 YARN 容器。

	默认情况下, Spark on YARN 将使用本地安装的 Spark jars, 但是 Spark jars 也是在 HDFS 上全局可读的位置。这就允许 YARN 在节点上缓存它，为了避免每次运行应用程序时，都要分发。

	为了指向 HDFS 上的 jars, 将这个配置设置为 `hdfs:///some/path`. 允许Globs.

- spark.yarn.archive	

	Default: (none)	
	
	Since Version: 2.0.0
	
	> An archive containing needed Spark jars for distribution to the YARN cache. If set, this configuration replaces `spark.yarn.jars` and the archive is used in all the application's containers. The archive should contain jar files in its root directory. Like with the previous option, the archive can also be hosted on HDFS to speed up file distribution.	

	包含分发到 YARN 缓存所需的 Spark jar 的归档文件。

	如果设置了这个配置，它将替代 `spark.yarn.jars`, 归档文件将被用在所有应用程序的容器中。

	归档文件应该在它的根目录下包含 jar 文件。就像前面的选项一样，归档文件也可以托管在 HDFS 上，以加快文件分发速度。

- spark.yarn.appMasterEnv.[EnvironmentVariableName]	

	Default: (none)	
	
	Since Version: 1.1.0
	
	> Add the environment variable specified by `EnvironmentVariableName` to the Application Master process launched on YARN. The user can specify multiple of these and to set multiple environment variables. In cluster mode this controls the environment of the Spark driver and in client mode it only controls the environment of the executor launcher.	

	为启动 Application Master 进程添加由 `EnvironmentVariableName` 指定的环境变量。

	用户可以指定多个这个选项，以设置多个环境变量。

	在集群模式下，它控制着 Spark 驱动的环境。在客户端模式下，它仅控制 executor 启动器的环境。

- spark.yarn.containerLauncherMaxThreads	

	Default: 25	
	
	Since Version: 1.2.0
	
	> The maximum number of threads to use in the YARN Application Master for launching executor containers.	

	为了启动 executor 容器，在 YARN Application Master 中使用的线程的最大数量。

- spark.yarn.am.extraJavaOptions	

	Default: (none)	
	
	Since Version: 1.3.0
	
	> A string of extra JVM options to pass to the YARN Application Master in client mode. In cluster mode, use `spark.driver.extraJavaOptions` instead. Note that it is illegal to set maximum heap size (-Xmx) settings with this option. Maximum heap size settings can be set with `spark.yarn.am.memory`

	在客户端模式下，传给 YARN Application Master 的额外的 JVM 选项字符串。

	在集群模式下，使用 `spark.driver.extraJavaOptions` 代替。

	注意，使用这个选项设置最大堆内存是非法的，应该通过 `spark.yarn.am.memory` 设置。

- spark.yarn.am.extraLibraryPath	

	Default: (none)	
	
	Since Version: 1.4.0
	
	> Set a special library path to use when launching the YARN Application Master in client mode.	

	在客户端模式下，启动 YARN Application Master 时，使用的库路径。

- spark.yarn.populateHadoopClasspath	

	Default: For with-hadoop Spark distribution, this is set to false; for no-hadoop distribution, this is set to true.	
	
	Since Version: 2.4.6
	
	> Whether to populate Hadoop classpath from `yarn.application.classpath` and `mapreduce.application.classpath` Note that if this is set to false, it requires a `with-Hadoop` Spark distribution that bundles Hadoop runtime or user has to provide a Hadoop installation separately.	

	是否使用 `yarn.application.classpath` 和 `mapreduce.application.classpath` 填充 Hadoop 类路径。

	注意，如果设为 false, 要求是绑定了 Hadoop 运行时的 `with-Hadoop` Spark 发行版，或者用户单独提供 Hadoop 安装。

- spark.yarn.maxAppAttempts	

	Default: yarn.resourcemanager.am.max-attempts in YARN	
	
	Since Version: 1.3.0
	
	> The maximum number of attempts that will be made to submit the application. It should be no larger than the global number of max attempts in the YARN configuration.	

	提交应用程序的最大尝试次数。它不应该大于 YARN 配置中的全局最大尝试次数。

- spark.yarn.am.attemptFailuresValidityInterval	

	Default: (none)	
	
	Since Version: 1.6.0
	
	> Defines the validity interval for AM failure tracking. If the AM has been running for at least the defined interval, the AM failure count will be reset. This feature is not enabled if not configured.	

	定义 AM 故障跟踪的有效间隔。如果 AM 已经运行了至少是定义的时间间隔，则 AM 失败计数将被重置。如果没有配置，则不会启用此功能。

- spark.yarn.am.clientModeTreatDisconnectAsFailed	

	Default: false
	
	Since Version: 3.3.0
	
	> Treat yarn-client unclean disconnects as failures. In yarn-client mode, normally the application will always finish with a final status of SUCCESS because in some cases, it is not possible to know if the Application was terminated intentionally by the user or if there was a real error. This config changes that behavior such that if the Application Master disconnects from the driver uncleanly (ie without the proper shutdown handshake) the application will terminate with a final status of FAILED. This will allow the caller to decide if it was truly a failure. Note that if this config is set and the user just terminate the client application badly it may show a status of FAILED when it wasn't really FAILED.	

	将 yarn-client 不干净的断开连接作为失败。

	在 yarn-client 模式下，正常情况下，应用程序将总是以 SUCCESS 的状态指向完成，因为在一些情况下，并不知道应用程序是否已被用户故意终止，或者是否存在真正的错误。

	这个配置改变那个行为，如果 Application Master 不干净地从驱动断开连接（例如没有正确的关闭握手），应用程序将以 FAILED 状态终止。这将允许调用者决定是否是一个真正的失败。

	注意，如果配置了这个属性，且用户终止了客户端应用程序，即使并不是真正的 FAILED, 也可能会展示 FAILED 状态。

- spark.yarn.am.clientModeExitOnError	

	Default: false	
	
	Since Version: 3.3.0
	
	> In yarn-client mode, when this is true, if driver got application report with final status of KILLED or FAILED, driver will stop corresponding SparkContext and exit program with code 1. Note, if this is true and called from another application, it will terminate the parent application as well.	

	在 yarn-client 模式下，当设为 true 时，如果驱动获得了具有 KILLED 或 FAILED 状态的应用程序报告，驱动将停止对应的 SparkContext, 并以代码 1 退出程序。

	注意，当设为 true 时，且从其他的应用程序调用时，它将终止父应用程序。

- spark.yarn.executor.failuresValidityInterval	

	Default: (none)	
	
	Since Version: 2.0.0
	
	> Defines the validity interval for executor failure tracking. Executor failures which are older than the validity interval will be ignored.	

	定义 executor 故障跟踪的有效间隔。

	比有效间隔更久的 executor 失败将被忽略。

- spark.yarn.submit.waitAppCompletion	

	Default: true	
	
	Since Version: 1.4.0
	
	> In YARN cluster mode, controls whether the client waits to exit until the application completes. If set to true, the client process will stay alive reporting the application's status. Otherwise, the client process will exit after submission.	

	在集群模式下，它控制着客户端直到应用程序完成时退出。

	如果设为 true, 客户端进程将保存活跃，报告应用程序的状态。否则，客户端进程将在提交后退出。

- spark.yarn.am.nodeLabelExpression	

	Default: (none)	
	
	Since Version: 1.6.0
	
	> A YARN node label expression that restricts the set of nodes AM will be scheduled on. Only versions of YARN greater than or equal to 2.6 support node label expressions, so when running against earlier versions, this property will be ignored.	



- spark.yarn.executor.nodeLabelExpression	

	Default: (none)	
	
	Since Version: 1.4.0
	
	> A YARN node label expression that restricts the set of nodes executors will be scheduled on. Only versions of YARN greater than or equal to 2.6 support node label expressions, so when running against earlier versions, this property will be ignored.

- spark.yarn.tags	

	Default: (none)	
	
	Since Version: 1.5.0
	
	> Comma-separated list of strings to pass through as YARN application tags appearing in YARN ApplicationReports, which can be used for filtering when querying YARN apps.	

	作为 YARN 应用程序标签传递的字符串的列表，逗号分隔，该标签出现在  YARN ApplicationReports 中，能够在查询 YARN 应用程序时过滤。

- spark.yarn.priority	

	Default: (none)	
	
	Since Version: 3.0.0
	
	> Application priority for YARN to define pending applications ordering policy, those with higher integer value have a better opportunity to be activated. Currently, YARN only supports application priority when using FIFO ordering policy.	

	YARN 应用程序优先级，它定义了挂起的应用程序的顺序策略。具有高整型值的优先级具有更好的激活机会。

	当前，在使用 FIFO 顺序策略时, YARN 仅支持应用程序优先级.

- spark.yarn.config.gatewayPath	

	Default: (none)	
	
	Since Version: 1.5.0
	
	> A path that is valid on the gateway host (the host where a Spark application is started) but may differ for paths for the same resource in other nodes in the cluster. Coupled with `spark.yarn.config.replacementPath`, this is used to support clusters with heterogeneous configurations, so that Spark can correctly launch remote processes.
	The replacement path normally will contain a reference to some environment variable exported by YARN (and, thus, visible to Spark containers).
	
	网关主机上有效的路径（启动spark应用程序的主机），但是可能和集群中其他节点中的相同资源的路径不同。

	再加上 `spark.yarn.config.replacementPath`, 这用于支持具有异构配置的集群，为了 Spark 能正确地启动远程进程。

	更换路径将包含 YARN 输出的一些环境变量的引用（对spark容器可见）

	> For example, if the gateway node has Hadoop libraries installed on `/disk1/hadoop`, and the location of the Hadoop install is exported by YARN as the `HADOOP_HOME` environment variable, setting this value to `/disk1/hadoop` and the replacement path to `$HADOOP_HOME` will make sure that paths used to launch remote processes properly reference the local YARN configuration.

	例如，如果网关节点具有安装在 `/disk1/hadoop` 的 Hadoop 库，并且 Hadoop 安装位置作为 `HADOOP_HOME` 环境变量由 YARN 导出，那么将这个值设为 `/disk1/hadoop`, 更换路径设为 `$HADOOP_HOME` 将确保用来启用远程进程的路径能正确地引用本地 YARN 配置。

- spark.yarn.config.replacementPath	

	Default: (none)	
	
	Since Version: 1.5.0
	
	See `spark.yarn.config.gatewayPath`.	

- spark.yarn.rolledLog.includePattern	

	Default: (none)	
	
	Since Version: 2.0.0
	
	> Java Regex to filter the log files which match the defined include pattern and those log files will be aggregated in a rolling fashion. This will be used with YARN's rolling log aggregation, to enable this feature in YARN side `yarn.nodemanager.log-aggregation.roll-monitoring-interval-seconds` should be configured in `yarn-site.xml`. The Spark log4j appender needs be changed to use FileAppender or another appender that can handle the files being removed while it is running. Based on the file name configured in the log4j configuration (like spark.log), the user should set the regex (spark*) to include all the log files that need to be aggregated.	

	正则表达式，用来过滤能匹配定义的包含模板的日志文件，这些日志文件将被以上卷的方式聚合。

	这将和 YARN 的上卷日志聚合一起使用，要在 YARN 端启用这个属性，需要配置 `yarn-site.xml` 文件中的 `yarn.nodemanager.log-aggregation.roll-monitoring-interval-seconds` 属性。

	Spark log4j appender 需要改变成使用 FileAppender 或者其他可以在其运行时处理被删除的文件的 appender.

	基于在 log4j 配置中配置的文件名(like spark.log)，用户应该设置正则表达式(spark*)，以包含需要被聚合的日志文件。

- spark.yarn.rolledLog.excludePattern	

	Default: (none)		
	
	Since Version: 2.0.0
	
	> Java Regex to filter the log files which match the defined exclude pattern and those log files will not be aggregated in a rolling fashion. If the log file name matches both the include and the exclude pattern, this file will be excluded eventually.

	正则表达式，用来过滤能匹配定义的排除模板的日志文件，这些日志文件将不被以上卷的方式聚合。

	如果日志文件名同时匹配到包含模板和排除模板，那么这个文件将被排除。

- spark.yarn.executor.launch.excludeOnFailure.enabled	

	Default: false	
	
	Since Version: 2.4.0
	
	> Flag to enable exclusion of nodes having YARN resource allocation problems. The error limit for excluding can be configured by `spark.excludeOnFailure.application.maxFailedExecutorsPerNode`.	

	排除存在 YARN 资源分配问题的节点。

	可以通过 `spark.excludeOnFailure.application.maxFailedExecutorsPerNode` 属性列举出排除的节点的数量

- spark.yarn.exclude.nodes	

	Default: (none)	
	
	Since Version: 3.0.0
	
	> Comma-separated list of YARN node names which are excluded from resource allocation.	

	不参与资源分配的 YARN 节点名称的列表，逗号分隔。

- spark.yarn.metrics.namespace	

	Default: (none)	
	
	Since Version: 2.4.0
	
	> The root namespace for AM metrics reporting. If it is not set then the YARN application ID is used.	

	AM 度量报告的根命名空间。如果不设置，那么就使用 YARN application ID

### Available patterns for SHS custom executor log URL

Pattern | Meaning
---|:---
`{{HTTP_SCHEME}}`	|`http://` or `https://` according to YARN HTTP policy. (Configured via `yarn.http.policy`)
`{{NM_HOST}}`	|The "host" of node where container was run.
`{{NM_PORT}}`	|The "port" of node manager where container was run.
`{{NM_HTTP_PORT}}`	|The "port" of node manager's http server where container was run.
`{{NM_HTTP_ADDRESS}}`	|Http URI of the node on which the container is allocated.
`{{CLUSTER_ID}}`	|The cluster ID of Resource Manager. (Configured via `yarn.resourcemanager.cluster-id`)
`{{CONTAINER_ID}}`	|The ID of container.
`{{USER}}`	|`SPARK_USER` on system environment.
`{{FILE_NAME}}`	|`stdout`, `stderr`.

> For example, suppose you would like to point log url link to Job History Server directly instead of let NodeManager http server redirects it, you can configure `spark.history.custom.executor.log.url` as below:

例如，如果你想直接将日志 url 连接指向 Job History Server, 而不是让 NodeManager http server 重定向，你可以配置 `spark.history.custom.executor.log.url`:

	{{HTTP_SCHEME}}<JHS_HOST>:<JHS_PORT>/jobhistory/logs/{{NM_HOST}}:{{NM_PORT}}/{{CONTAINER_ID}}/{{CONTAINER_ID}}/{{USER}}/{{FILE_NAME}}?start=-4096

> NOTE: you need to replace `<JHS_HOST>` and `<JHS_PORT>` with actual value.

要把 `<JHS_HOST>` 和 `<JHS_PORT>` 替换成真正的值。

## Resource Allocation and Configuration Overview

> Please make sure to have read the Custom Resource Scheduling and Configuration Overview section on the [configuration page](https://spark.apache.org/docs/3.3.2/configuration.html). This section only talks about the YARN specific aspects of resource scheduling.

请先阅读配置页的 Custom Resource Scheduling 和 Configuration Overview 部分。此部分仅讨论 YARN 的资源调度方面。

> YARN needs to be configured to support any resources the user wants to use with Spark. Resource scheduling on YARN was added in YARN 3.1.0. See the YARN documentation for more information on configuring resources and properly setting up isolation. Ideally the resources are setup isolated so that an executor can only see the resources it was allocated. If you do not have isolation enabled, the user is responsible for creating a discovery script that ensures the resource is not shared between executors.

需要配置 YARN 以支持用户使用 Spark 所需的资源。在 YARN 之上的资源调度是在 YARN 3.1.0 版本添加的。 

理想情况下，为了使 executor 仅能看见分配给它的资源，资源是隔离设置的。如果没有启用隔离，用户需要创建一个发现脚本，以确保资源不会在 executors 间共享。

> YARN supports user defined resource types but has built in types for GPU (`yarn.io/gpu`) and FPGA (`yarn.io/fpga`). For that reason, if you are using either of those resources, Spark can translate your request for spark resources into YARN resources and you only have to specify the `spark.{driver/executor}.resource.configs`. Note, if you are using a custom resource type for GPUs or FPGAs with YARN you can change the Spark mapping using `spark.yarn.resourceGpuDeviceName` and `spark.yarn.resourceFpgaDeviceName`. If you are using a resource other then FPGA or GPU, the user is responsible for specifying the configs for both YARN (`spark.yarn.{driver/executor}.resource.`) and Spark (`spark.{driver/executor}.resource.`).

YARN 支持用户定义的资源类型，但是是 GPU 和 FPGA 的内建类型。如果你使用这些资源中的某一个, Spark 可以将对 spark 资源的请求转换成 YARN 资源，并且仅需指定 `spark.{driver/executor}.resource.configs`.

注意，如果你使用 GPU 和 FPGA 的自定义类型，你可以使用 `spark.yarn.resourceGpuDeviceName` 和 `spark.yarn.resourceFpgaDeviceName` 改变映射。

如果你正在使用 FPGA 或 GPU 以外的资源，则用户负责指定 YARN(`spark.yarn.{driver/executor}.resource.`) 和 Spark (`spark.{driver/executor}.resource.`) 的配置。

> For example, the user wants to request 2 GPUs for each executor. The user can just specify `spark.executor.resource.gpu.amount=2` and Spark will handle requesting `yarn.io/gpu` resource type from YARN.

例如，用户想要为每个 executor 请求 2 个 GPU, 那么用户可以指定 `spark.executor.resource.gpu.amount=2`, spark 将处理请求的 来自 YARN 的 `yarn.io/gpu` 资源

> If the user has a user defined YARN resource, lets call it acceleratorX then the user must specify `spark.yarn.executor.resource.acceleratorX.amount=2` and `spark.executor.resource.acceleratorX.amount=2`.

如果用户拥有一个用户定义的 YARN 资源，称其为 acceleratorX, 那么用户必须指定 `spark.yarn.executor.resource.acceleratorX.amount=2` 和 `spark.executor.resource.acceleratorX.amount=2`.

> YARN does not tell Spark the addresses of the resources allocated to each container. For that reason, the user must specify a discovery script that gets run by the executor on startup to discover what resources are available to that executor. You can find an example scripts in `examples/src/main/scripts/getGpusResources.sh`. The script must have execute permissions set and the user should setup permissions to not allow malicious users to modify it. The script should write to STDOUT a JSON string in the format of the ResourceInformation class. This has the resource name and an array of resource addresses available to just that executor.

YARN 不会告诉 Spark 分配给每个 container 的资源的地址。为了这，用户必须指定一个发现脚本，由 executor 在启动时运行，来发现那个 executor 可用的资源。可以在 `examples/src/main/scripts/getGpusResources.sh` 中找到一个例子脚本。

脚本必须具有可执行权限，用户应该设置权限，不允许恶意用户修改它。脚本应该以 ResourceInformation 类的格式向 STDOUT 写入一个 JSON 字符串。其中包含资源名称和资源地址数组，仅供该执行器使用。

## Stage Level Scheduling Overview

> Stage level scheduling is supported on YARN when dynamic allocation is enabled. One thing to note that is YARN specific is that each ResourceProfile requires a different container priority on YARN. The mapping is simply the ResourceProfile id becomes the priority, on YARN lower numbers are higher priority. This means that profiles created earlier will have a higher priority in YARN. Normally this won’t matter as Spark finishes one stage before starting another one, the only case this might have an affect is in a job server type scenario, so its something to keep in mind. 

当启用动态分配时，在 YARN 上支持阶段级别的调度。一个注意的事情就是，每个 ResourceProfile 请求一个不同的容器优先级。映射就是 ResourceProfile id 成为优先级，在 YARN 上，小的数值具有更高的优先级。这就意味着早期创建的配置文件将具有更高的优先级。

通常情况下，这并不重要，因为 Spark 在开始另一个阶段之前完成了一个阶段，唯一可能产生影响的情况是在作业服务器类型的场景中，所以要记住这一点。

> Note there is a difference in the way custom resources are handled between the base default profile and custom ResourceProfiles. To allow for the user to request YARN containers with extra resources without Spark scheduling on them, the user can specify resources via the `spark.yarn.executor.resource.config`. Those configs are only used in the base default profile though and do not get propagated into any other custom ResourceProfiles. This is because there would be no way to remove them if you wanted a stage to not have them. This results in your default profile getting custom resources defined in `spark.yarn.executor.resource.` plus spark defined resources of GPU or FPGA. 

在处理自定义资源的方式上，基本的默认配置文件和自定义的 ResourceProfiles 是有区别的。为了允许用户请求具有无需 spark 调度的额外资源的 YARN 容器，用户可以通过 `spark.yarn.executor.resource.config` 指定资源。

这些配置仅用在基本的默认配置文件中，不会被传播到任意其他的自定义 ResourceProfiles.这是因为如果你想要一个阶段不拥有它们，无法删除它们。

这将导致默认配置文件获得 `spark.yarn.executor.resource.` 中定义的自定义资源，加上 spark 定义的 GPU 或 FPGA 资源。

> Spark converts GPU and FPGA resources into the YARN built in types `yarn.io/gpu` and `yarn.io/fpga`, but does not know the mapping of any other resources. Any other Spark custom resources are not propagated to YARN for the default profile. So if you want Spark to schedule based off a custom resource and have it requested from YARN, you must specify it in both YARN (`spark.yarn.{driver/executor}.resource.`) and Spark (`spark.{driver/executor}.resource.`) configs. Leave the Spark config off if you only want YARN containers with the extra resources but Spark not to schedule using them. 

Spark 将 GPU 和 FPGA 资源转换成内建的 YARN 类型 `yarn.io/gpu` 和 `yarn.io/fpga`, 但是并不知道任意其他资源的映射关系。任意其他的 spark 自定义资源不会传播到 YARN 的默认配置文件中。

因此，如果希望 Spark 调度自定义资源，并从 YARN 请求它，你必须同时在 YARN 的`spark.yarn.{driver/executor}.resource.` 和 Spark 的 `spark.{driver/executor}.resource.` 指定它。

如果你只想要具有额外资源的 YARN 容器，且 spark 不调度它们，就不要配置 Spark.

> Now for custom ResourceProfiles, it doesn’t currently have a way to only specify YARN resources without Spark scheduling off of them. This means for custom ResourceProfiles we propagate all the resources defined in the ResourceProfile to YARN. We still convert GPU and FPGA to the YARN build in types as well. This requires that the name of any custom resources you specify match what they are defined as in YARN.  

现在，对于自定义 ResourceProfiles, 目前还没有一种方法可以只指定 YARN 资源，且不需要 Spark 调度它们。这意味着对于自定义的 ResourceProfile, 我们将 ResourceProfile 中定义的所有资源传播到 YARN 中。我们仍然将 GPU 和 FPGA 转换为 YARN 内建类型。这要求你指定的任何自定义资源的名称与在 YARN 中定义的名称相匹配。

## Important notes

> Whether core requests are honored in scheduling decisions depends on which scheduler is in use and how it is configured.

- 在调度决策中，对核心的请求是否得到执行，取决于使用的调度程序及其配置方式。

> In `cluster` mode, the local directories used by the Spark executors and the Spark driver will be the local directories configured for YARN (Hadoop YARN config `yarn.nodemanager.local-dirs`). If the user specifies `spark.local.dir`, it will be ignored. In `client` mode, the Spark executors will use the local directories configured for YARN while the Spark driver will use those defined in `spark.local.dir`. This is because the Spark driver does not run on the YARN cluster in `client` mode, only the Spark executors do.

- 在集群模式中，Spark executors 和 Spark dirver 使用的本地目录是为 YARN（Hadoop YARN 配置 `yarn.nodemanager.local-dirs`）配置的本地目录。如果用户指定 `spark.local.dir`，它将被忽略。在客户端模式下，Spark executors 将使用为 YARN 配置的本地目录，Spark dirver 将使用 `spark.local.dir` 中定义的目录。这是因为 Spark drivers 并不运行在 YARN 集群的客户端模式中，仅仅 Spark executors 会这样做。

> The `--files` and `--archives` options support specifying file names with the `#` similar to Hadoop. For example, you can specify: `--files localtest.txt#appSees.txt` and this will upload the file you have locally named `localtest.txt` into HDFS but this will be linked to by the name `appSees.txt`, and your application should use the name as `appSees.txt` to reference it when running on YARN.

- `--files` 和 `--archives` 支持用 `#` 指定文件名，与 Hadoop 相似。例如你可以指定：`--files localtest.txt#appSees.txt`, 然后就会将本地名为 localtest.txt 的文件上传到 HDFS 中去，但是会通过名称 appSees.txt 来链接，当你的应用程序在 YARN 上运行时，你应该使用名称 appSees.txt 来引用它。

> The `--jars` option allows the `SparkContext.addJar` function to work if you are using it with local files and running in `cluster` mode. It does not need to be used if you are using it with HDFS, HTTP, HTTPS, or FTP files.

- `--jars` 选项允许你在集群模式下使用本地文件运行 `SparkContext.addJar` 函数。如果你使用 HDFS，HTTP，HTTPS 或 FTP 文件，则不需要使用它。

## Kerberos

> Standard Kerberos support in Spark is covered in the [Security](https://spark.apache.org/docs/3.3.2/security.html#kerberos) page.

> In YARN mode, when accessing Hadoop file systems, aside from the default file system in the hadoop configuration, Spark will also automatically obtain delegation tokens for the service hosting the staging directory of the Spark application.

### YARN-specific Kerberos Configuration

Property Name | Default	 | Meaning |	Since Version
---|:---|:---|:---
`spark.kerberos.keytab` |(none) |The full path to the file that contains the keytab for the principal specified above. This keytab will be copied to the node running the YARN Application Master via the YARN Distributed Cache, and will be used for renewing the login tickets and the delegation tokens periodically. Equivalent to the `--keytab` command line argument.
(Works also with the "local" master.) | 3.0.0
`spark.kerberos.principal` |(none) |Principal to be used to login to KDC, while running on secure clusters. Equivalent to the `--principal` command line argument.
(Works also with the "local" master.) |3.0.0
`spark.yarn.kerberos.relogin.period` | 1m  |How often to check whether the kerberos TGT should be renewed. This should be set to a value that is shorter than the TGT renewal period (or the TGT lifetime if TGT renewal is not enabled). The default value should be enough for most deployments.| 2.3.0
`spark.yarn.kerberos.renewal.excludeHadoopFileSystems` | (none) |A comma-separated list of Hadoop filesystems for whose hosts will be excluded from from delegation token renewal at resource scheduler. For example, `spark.yarn.kerberos.renewal.excludeHadoopFileSystems=hdfs://nn1.com:8032, hdfs://nn2.com:8032`. This is known to work under YARN for now, so YARN Resource Manager won't renew tokens for the application. Note that as resource scheduler does not renew token, so any application running longer than the original token expiration that tries to use that token will likely fail.|3.2.0

### Troubleshooting Kerberos

> Debugging Hadoop/Kerberos problems can be “difficult”. One useful technique is to enable extra logging of Kerberos operations in Hadoop by setting the `HADOOP_JAAS_DEBUG` environment variable.

	export HADOOP_JAAS_DEBUG=true

> The JDK classes can be configured to enable extra logging of their Kerberos and SPNEGO/REST authentication via the system properties `sun.security.krb5.debug` and `sun.security.spnego.debug=true`

	-Dsun.security.krb5.debug=true -Dsun.security.spnego.debug=true

> All these options can be enabled in the Application Master:

	spark.yarn.appMasterEnv.HADOOP_JAAS_DEBUG true
	spark.yarn.am.extraJavaOptions -Dsun.security.krb5.debug=true -Dsun.security.spnego.debug=true

> Finally, if the log level for `org.apache.spark.deploy.yarn.Client` is set to `DEBUG`, the log will include a list of all tokens obtained, and their expiry details

## Configuring the External Shuffle Service

> To start the Spark Shuffle Service on each NodeManager in your YARN cluster, follow these instructions:

为了在 YARN 集群中的每个 NodeManager 启动 Spark Shuffle 服务，需根据如下指示：

> Build Spark with the YARN profile. Skip this step if you are using a pre-packaged distribution.

- 使用 YARN 配置文件构建 Spark. 如果你使用了预打包的发布版可以跳过该步骤。

> Locate the `spark-<version>-yarn-shuffle.jar`. This should be under `$SPARK_HOME/common/network-yarn/target/scala-<version>` if you are building Spark yourself, and under yarn if you are using a distribution.

- 定位 `spark-<version>-yarn-shuffle.jar`。如果是你自己构建的 Spark, 它应该在 `$SPARK_HOME/common/network-yarn/target/scala-<version>` 下，如果你使用的是一个发布版，那么它应该在 yarn 下。

> Add this jar to the classpath of all NodeManagers in your cluster.

- 将这个 jar 添加到集群中所有 NodeManager 的类路径中。

> In the `yarn-site.xml` on each node, add `spark_shuffle` to `yarn.nodemanager.aux-services`, then set `yarn.nodemanager.aux-services.spark_shuffle.class` to `org.apache.spark.network.yarn.YarnShuffleService`.

- 在每个节点的 `yarn-site.xml` 文件中，将 `spark_shuffle` 添加到 `yarn.nodemanager.aux-services`，然后将 `yarn.nodemanager.aux-services.spark_shuffle.class` 设置为 `org.apache.spark.network.yarn.YarnShuffleService` 。

> Increase NodeManager's heap size by setting `YARN_HEAPSIZE` (1000 by default) in `etc/hadoop/yarn-env.sh` to avoid garbage collection issues during shuffle.

- 在 `etc/hadoop/yarn-env.sh` 文件中，设置 `YARN_HEAPSIZE` (默认值 1000) 增加 NodeManager 的堆大小，以避免在 shuffle 时产生的垃圾回收问题。

> Restart all NodeManagers in your cluster.

- 重启集群中所有的 NodeManager

> The following extra configuration options are available when the shuffle service is running on YARN:

当 shuffle service 在 YARN 上运行时，可以使用以下额外的配置选项:

- spark.yarn.shuffle.stopOnFailure

	Default: false	
		
	> Whether to stop the NodeManager when there's a failure in the Spark Shuffle Service's initialization. This prevents application failures caused by running containers on NodeManagers where the Spark Shuffle Service is not running.	

	在初始化中出现了问题时，是否停止 NodeManager. 

	这会阻止运行容器的 NodeManagers 上没有 Spark Shuffle Service 运行造成的应用程序失败。

- spark.yarn.shuffle.service.metrics.namespace

	Default: sparkShuffleService

	> The namespace to use when emitting shuffle service metrics into Hadoop metrics2 system of the NodeManager.
	
	当将 shuffle 服务报告提交给 NodeManager 的 Hadoop metrics 系统中时，使用的命名空间。

- spark.yarn.shuffle.service.logs.namespace

	Default: (not set)

	> A namespace which will be appended to the class name when forming the logger name to use for emitting logs from the YARN shuffle service, like `org.apache.spark.network.yarn.YarnShuffleService.logsNamespaceValue`. Since some logging frameworks may expect the logger name to look like a class name, it's generally recommended to provide a value which would be a valid Java package or class name and not include spaces.

	为从 YARN shuffle service 提交日志，在形成使用的 logger 名称时，将被追加到类名的命名空间，就像是 `org.apache.spark.network.yarn.YarnShuffleService.logsNamespaceValue`.

	由于一些日志框架可能期待 logger 名称像一个类名，通常建议提供一个有效的 java 包或类名，并不包含空格的值。

> Please note that the instructions above assume that the default shuffle service name, `spark_shuffle`, has been used. It is possible to use any name here, but the values used in the YARN NodeManager configurations must match the value of `spark.shuffle.service.name` in the Spark application.

注意，上述指令假设使用的是默认的服务名称 `spark_shuffle`. 这里可以使用任意的名字，但是在 YARN NodeManager 配置中使用的值必须匹配 spark 应用程序中的 `spark.shuffle.service.name` 值。

> The shuffle service will, by default, take all of its configurations from the Hadoop Configuration used by the NodeManager (e.g. `yarn-site.xml`). However, it is also possible to configure the shuffle service independently using a file named `spark-shuffle-site.xml` which should be placed onto the classpath of the shuffle service (which is, by default, shared with the classpath of the NodeManager). The shuffle service will treat this as a standard Hadoop Configuration resource and overlay it on top of the NodeManager’s configuration.

默认情况下, shuffle 服务接收 NodeManager 使用的 Hadoop 所有配置（例如`yarn-site.xml`）。

然而，使用一个名为 `spark-shuffle-site.xml` 的文件单独配置 shuffle 服务也是可以的，这个文件应该放在 shuffle 服务的类路径下（默认，和NodeManager的类路径共享）. shuffle 服务将这个文件作为一个标准的 Hadoop 配置资源，并会将它覆盖在 NodeManager 的配置之上。

## Launching your application with Apache Oozie

> Apache Oozie can launch Spark applications as part of a workflow. In a secure cluster, the launched application will need the relevant tokens to access the cluster’s services. If Spark is launched with a keytab, this is automatic. However, if Spark is to be launched without a keytab, the responsibility for setting up security must be handed over to Oozie.

Apache Oozie 可以将 Spark 应用程序作为工作流的一部分启动。在安全的集群中，启动的应用程序将需要相关的 tokens 访问集群服务。

如果 Spark 使用 keytab 启动，这是自动的。但是，如果 Spark 在没有 keytab 的情况下启动，则设置安全的责任必须移交给 Oozie.

> The details of configuring Oozie for secure clusters and obtaining credentials for a job can be found on the [Oozie web site](http://oozie.apache.org/) in the “Authentication” section of the specific release’s documentation.

> For Spark applications, the Oozie workflow must be set up for Oozie to request all tokens which the application needs, including:

对于 Spark 应用程序，必须设置 Oozie 工作流以使 Oozie 请求应用程序需要的所有 tokens，包括：

- The YARN resource manager.
- The local Hadoop filesystem.
- Any remote Hadoop filesystems used as a source or destination of I/O.
- Hive --if used.
- HBase --if used.
- The YARN timeline server, if the application interacts with this.

> To avoid Spark attempting --and then failing-- to obtain Hive, HBase and remote HDFS tokens, the Spark configuration must be set to disable token collection for the services.

为了避免 Spark 尝试获取 Hive、HBase 和远程的 HDFS tokens 失败，必须禁用 Spark 配置中收集 tokens 的选项。

> The Spark configuration must include the lines:

Spark 配置必须包含以下行：

	spark.security.credentials.hive.enabled   false
	spark.security.credentials.hbase.enabled  false

> The configuration option `spark.kerberos.access.hadoopFileSystems` must be unset.

必须取消设置配置选项 `spark.kerberos.access.hadoopFileSystems`.

## Using the Spark History Server to replace the Spark Web UI

> It is possible to use the Spark History Server application page as the tracking URL for running applications when the application UI is disabled. This may be desirable on secure clusters, or to reduce the memory usage of the Spark driver. To set up tracking through the Spark History Server, do the following:

当应用程序 UI 被禁用时，可以使用 Spark History Server 应用程序页面作为用于跟踪运行的程序的 URL. 这在安全集群中是可取的，或者减少 Spark 驱动的内存使用量。要设置跟踪 Spark History Server, 请执行以下操作：

> On the application side, set `spark.yarn.historyServer.allowTracking=true` in Spark’s configuration. This will tell Spark to use the history server’s URL as the tracking URL if the application’s UI is disabled.

- 在应用程序方面，设置 Spark 配置中的 `spark.yarn.historyServer.allowTracking=true`. 在 application's UI 是禁用的情况下，这将告诉 Spark 使用 history server 的 URL 作为跟踪 URL.

> On the Spark History Server, add `org.apache.spark.deploy.yarn.YarnProxyRedirectFilter` to the list of filters in the `spark.ui.filters` configuration.

- 在 Spark History Server 方面，将 `org.apache.spark.deploy.yarn.YarnProxyRedirectFilter` 添加到 `spark.ui.filters` 配置中的 filters 列表中去。

> Be aware that the history server information may not be up-to-date with the application’s state.

注意: history server 信息可能不是应用程序状态的最新信息。

## Running multiple versions of the Spark Shuffle Service

> Please note that this section only applies when running on YARN versions >= 2.9.0.

注意，此部分仅适用于 YARN >= 2.9.0. 

> In some cases it may be desirable to run multiple instances of the Spark Shuffle Service which are using different versions of Spark. This can be helpful, for example, when running a YARN cluster with a mixed workload of applications running multiple Spark versions, since a given version of the shuffle service is not always compatible with other versions of Spark. 

在一些情况下，使用不同版本的 spark 运行多个 Spark Shuffle Service 实例是可取的。例如，当运行一个 YARN 集群，其中的应用程序工作负载混合运行多个 spark 版本时是有用的，因为给定版本的 shuffle 服务并不总是与其他 spark 版本的兼容。

> YARN versions since 2.9.0 support the ability to run shuffle services within an isolated classloader (see [YARN-4577](https://issues.apache.org/jira/browse/YARN-4577)), meaning multiple Spark versions can coexist within a single NodeManager. The `yarn.nodemanager.aux-services.<service-name>.classpath` and, starting from YARN 2.10.2/3.1.1/3.2.0, `yarn.nodemanager.aux-services.<service-name>.remote-classpath` options can be used to configure this. Note that YARN 3.3.0/3.3.1 have an issue which requires setting `yarn.nodemanager.aux-services.<service-name>.system-classes` as a workaround. See [YARN-11053](https://issues.apache.org/jira/browse/YARN-11053) for details. 

从 2.9.0 的 YARN 版本开始，支持运行包含一个隔离的类加载器的 shuffle 服务，这意味着多个 Spark 版本可以在单个 NodeManager 中并存. `yarn.nodemanager.aux-services.<service-name>.classpath` 和 `yarn.nodemanager.aux-services.<service-name>.remote-classpath` 选项用来配置这个功能。注意, YARN 3.3.0/3.3.1 还需要设置 `yarn.nodemanager.aux-services.<service-name>.system-classes` 作为一个替代方法。

> In addition to setting up separate classpaths, it’s necessary to ensure the two versions advertise to different ports. This can be achieved using the `spark-shuffle-site.xml` file described above. For example, you may have configuration like:

另外，要设置独立的类路径，确保两个版本使用不同的端口。这个可以通过 `spark-shuffle-site.xml` 文件实现。例如：

  	yarn.nodemanager.aux-services = spark_shuffle_x,spark_shuffle_y
  	yarn.nodemanager.aux-services.spark_shuffle_x.classpath = /path/to/spark-x-path/fat.jar:/path/to/spark-x-config
  	yarn.nodemanager.aux-services.spark_shuffle_y.classpath = /path/to/spark-y-path/fat.jar:/path/to/spark-y-config

Or

  	yarn.nodemanager.aux-services = spark_shuffle_x,spark_shuffle_y
  	yarn.nodemanager.aux-services.spark_shuffle_x.classpath = /path/to/spark-x-path/*:/path/to/spark-x-config
  	yarn.nodemanager.aux-services.spark_shuffle_y.classpath = /path/to/spark-y-path/*:/path/to/spark-y-config

> The two `spark-*-config` directories each contain one file, `spark-shuffle-site.xml`. These are XML files in the [Hadoop Configuration format](https://hadoop.apache.org/docs/r3.2.2/api/org/apache/hadoop/conf/Configuration.html) which each contain a few configurations to adjust the port number and metrics name prefix used:

每个 `spark-*-config` 目录包含一个 `spark-shuffle-site.xml` 文件。这些 XML 文件都是 Hadoop Configuration 格式，每一个都包含一些用来调整端口和度量名称前缀的配置。

	<configuration>
	  <property>
	    <name>spark.shuffle.service.port</name>
	    <value>7001</value>
	  </property>
	  <property>
	    <name>spark.yarn.shuffle.service.metrics.namespace</name>
	    <value>sparkShuffleServiceX</value>
	  </property>
	</configuration>

> The values should both be different for the two different services.

对于两个不同的服务，值应该是不同的。

> Then, in the configuration of the Spark applications, one should be configured with:

然后在 Spark 应用程序的配置中，一个应该这样配置

  	spark.shuffle.service.name = spark_shuffle_x
  	spark.shuffle.service.port = 7001

> and one should be configured with:

另一个应该这样配置

  	spark.shuffle.service.name = spark_shuffle_y
  	spark.shuffle.service.port = <other value>