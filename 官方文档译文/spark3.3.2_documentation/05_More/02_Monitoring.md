# Monitoring and Instrumentation

[TOC]

> There are several ways to monitor Spark applications: web UIs, metrics, and external instrumentation.

## Web Interfaces

> Every SparkContext launches a [Web UI](https://spark.apache.org/docs/3.3.2/web-ui.html), by default on port 4040, that displays useful information about the application. This includes:

> A list of scheduler stages and tasks
> A summary of RDD sizes and memory usage
> Environmental information.
> Information about the running executors

> You can access this interface by simply opening `http://<driver-node>:4040` in a web browser. If multiple SparkContexts are running on the same host, they will bind to successive ports beginning with 4040 (4041, 4042, etc).

> Note that this information is only available for the duration of the application by default. To view the web UI after the fact, set `spark.eventLog.enabled` to true before starting the application. This configures Spark to log Spark events that encode the information displayed in the UI to persisted storage.

### Viewing After the Fact

> It is still possible to construct the UI of an application through Spark’s history server, provided that the application’s event logs exist. You can start the history server by executing:

	./sbin/start-history-server.sh

> This creates a web interface at `http://<server-url>:18080` by default, listing incomplete and completed applications and attempts.

> When using the file-system provider class (see `spark.history.provider` below), the base logging directory must be supplied in the `spark.history.fs.logDirectory` configuration option, and should contain sub-directories that each represents an application’s event logs.

> The spark jobs themselves must be configured to log events, and to log them to the same shared, writable directory. For example, if the server was configured with a log directory of `hdfs://namenode/shared/spark-logs`, then the client-side options would be:

	spark.eventLog.enabled true
	spark.eventLog.dir hdfs://namenode/shared/spark-logs

> The history server can be configured as follows:

#### Environment Variables

Environment Variable  | Meaning
---|:---
`SPARK_DAEMON_MEMORY`	|Memory to allocate to the history server (default: 1g).
`SPARK_DAEMON_JAVA_OPTS`	|JVM options for the history server (default: none).
`SPARK_DAEMON_CLASSPATH`	|Classpath for the history server (default: none).
`SPARK_PUBLIC_DNS`	|The public address for the history server. If this is not set, links to application history may use the internal address of the server, resulting in broken links (default: none).
`SPARK_HISTORY_OPTS`	| `spark.history.*` configuration options for the history server (default: none).

#### Applying compaction on rolling event log files

> A long-running application (e.g. streaming) can bring a huge single event log file which may cost a lot to maintain and also requires a bunch of resource to replay per each update in Spark History Server.

> Enabling spark.eventLog.rolling.enabled and `spark.eventLog.rolling.maxFileSize` would let you have rolling event log files instead of single huge event log file which may help some scenarios on its own, but it still doesn’t help you reducing the overall size of logs.

> Spark History Server can apply compaction on the rolling event log files to reduce the overall size of logs, via setting the configuration `spark.history.fs.eventLog.rolling.maxFilesToRetain` on the Spark History Server.

> Details will be described below, but please note in prior that compaction is LOSSY operation. Compaction will discard some events which will be no longer seen on UI - you may want to check which events will be discarded before enabling the option.

> When the compaction happens, the History Server lists all the available event log files for the application, and considers the event log files having less index than the file with smallest index which will be retained as target of compaction. For example, if the application A has 5 event log files and `spark.history.fs.eventLog.rolling.maxFilesToRetain` is set to 2, then first 3 log files will be selected to be compacted.

> Once it selects the target, it analyzes them to figure out which events can be excluded, and rewrites them into one compact file with discarding events which are decided to exclude.

> The compaction tries to exclude the events which point to the outdated data. As of now, below describes the candidates of events to be excluded:

> Events for the job which is finished, and related stage/tasks events
> Events for the executor which is terminated
> Events for the SQL execution which is finished, and related job/stage/tasks events

> Once rewriting is done, original log files will be deleted, via best-effort manner. The History Server may not be able to delete the original log files, but it will not affect the operation of the History Server.

> Please note that Spark History Server may not compact the old event log files if figures out not a lot of space would be reduced during compaction. For streaming query we normally expect compaction will run as each micro-batch will trigger one or more jobs which will be finished shortly, but compaction won’t run in many cases for batch query.

> Please also note that this is a new feature introduced in Spark 3.0, and may not be completely stable. Under some circumstances, the compaction may exclude more events than you expect, leading some UI issues on History Server for the application. Use it with caution.

#### Spark History Server Configuration Options

> Security options for the Spark History Server are covered more detail in the [Security](https://spark.apache.org/docs/3.3.2/security.html#web-ui) page.


- spark.history.provider	

	Default: `org.apache.spark.deploy.history.FsHistoryProvider`	
	
	Since Version: 1.1.0
	
	Name of the class implementing the application history backend. Currently there is only one implementation, provided by Spark, which looks for application logs stored in the file system.	

- spark.history.fs.logDirectory	

	Default: `file:/tmp/spark-events`	

	Since Version: 1.1.0

	For the filesystem history provider, the URL to the directory containing application event logs to load. This can be a local `file://` path, an HDFS path `hdfs://namenode/shared/spark-logs` or that of an alternative filesystem supported by the Hadoop APIs.	

- spark.history.fs.update.interval	

	Default: 10s	
	
	Since Version: 1.4.0
	
	The period at which the filesystem history provider checks for new or updated logs in the log directory. A shorter interval detects new applications faster, at the expense of more server load re-reading updated applications. As soon as an update has completed, listings of the completed and incomplete applications will reflect the changes.	

- spark.history.retainedApplications	

	Default: 50	
	
	Since Version: 1.0.0
	
	The number of applications to retain UI data for in the cache. If this cap is exceeded, then the oldest applications will be removed from the cache. If an application is not in the cache, it will have to be loaded from disk if it is accessed from the UI.	

- spark.history.ui.maxApplications	

	Default: Int.MaxValue	
	
	Since Version: 2.0.1
	
	The number of applications to display on the history summary page. Application UIs are still available by accessing their URLs directly even if they are not displayed on the history summary page.	

- spark.history.ui.port	

	Default: 18080	
	
	1.0.0
	
	Since Version: The port to which the web interface of the history server binds.	

- spark.history.kerberos.enabled	

	Default: false	
	
	Since Version: 1.0.1
	
	Indicates whether the history server should use kerberos to login. This is required if the history server is accessing HDFS files on a secure Hadoop cluster.	

- spark.history.kerberos.principal	

	Default: (none)	
	
	Since Version: 1.0.1
	
	When `spark.history.kerberos.enabled=true`, specifies kerberos principal name for the History Server.

- spark.history.kerberos.keytab	
	
	Default: (none)	
	
	Since Version: 1.0.1
	
	When `spark.history.kerberos.enabled=true`, specifies location of the kerberos keytab file for the History Server.	

- spark.history.fs.cleaner.enabled	

	Default: false	
	
	Since Version: 1.4.0
	
	Specifies whether the History Server should periodically clean up event logs from storage.	

- spark.history.fs.cleaner.interval	

	Default: 1d	
	
	Since Version: 1.4.0
	
	When `spark.history.fs.cleaner.enabled=true`, specifies how often the filesystem job history cleaner checks for files to delete. Files are deleted if at least one of two conditions holds. First, they're deleted if they're older than `spark.history.fs.cleaner.maxAge`. They are also deleted if the number of files is more than spark.history.fs.cleaner.maxNum, Spark tries to clean up the completed attempts from the applications based on the order of their oldest attempt time.	

- spark.history.fs.cleaner.maxAge	

	Default: 7d	
	
	Since Version: 1.4.0
	
	When `spark.history.fs.cleaner.enabled=true`, job history files older than this will be deleted when the filesystem history cleaner runs.	

- spark.history.fs.cleaner.maxNum	

	Default: Int.MaxValue	
	
	Since Version: 3.0.0
	
	When `spark.history.fs.cleaner.enabled=true`, specifies the maximum number of files in the event log directory. Spark tries to clean up the completed attempt logs to maintain the log directory under this limit. This should be smaller than the underlying file system limit like `dfs.namenode.fs-limits.max-directory-items` in HDFS.	

- spark.history.fs.endEventReparseChunkSize	

	Default: 1m	
	
	Since Version: 2.4.0
	
	How many bytes to parse at the end of log files looking for the end event. This is used to speed up generation of application listings by skipping unnecessary parts of event log files. It can be disabled by setting this config to 0.	

- spark.history.fs.inProgressOptimization.enabled	

	Default: true	
	
	Since Version: 2.4.0
	
	Enable optimized handling of in-progress logs. This option may leave finished applications that fail to rename their event logs listed as in-progress.	

- spark.history.fs.driverlog.cleaner.enabled	

	Default: `spark.history.fs.cleaner.enabled`	
	
	Since Version: 3.0.0
	
	Specifies whether the History Server should periodically clean up driver logs from storage.	

- spark.history.fs.driverlog.cleaner.interval	

	Default: `spark.history.fs.cleaner.interval`	
	
	Since Version: 3.0.0
	
	When `spark.history.fs.driverlog.cleaner.enabled=true`, specifies how often the filesystem driver log cleaner checks for files to delete. Files are only deleted if they are older than `spark.history.fs.driverlog.cleaner.maxAge`

- spark.history.fs.driverlog.cleaner.maxAge	

	Default: `spark.history.fs.cleaner.maxAge`	
	
	Since Version: 3.0.0
	
	When `spark.history.fs.driverlog.cleaner.enabled=true`, driver log files older than this will be deleted when the driver log cleaner runs.	

- spark.history.fs.numReplayThreads	

	Default: 25% of available cores	
	
	Since Version: 2.0.0
	
	Number of threads that will be used by history server to process event logs.	

- spark.history.store.maxDiskUsage	

	Default: 10g	
	
	Since Version: 2.3.0
	
	Maximum disk usage for the local directory where the cache application history information are stored.	


- spark.history.store.path	
	
	Default: (none)	
	
	Since Version: 2.3.0
	
	Local directory where to cache application history data. If set, the history server will store application data on disk instead of keeping it in memory. The data written to disk will be re-used in the event of a history server restart.	

- spark.history.custom.executor.log.url	

	Default: (none)	
	
	Since Version: 3.0.0
	
	Specifies custom spark executor log URL for supporting external log service instead of using cluster managers' application log URLs in the history server. Spark will support some path variables via patterns which can vary on cluster manager. Please check the documentation for your cluster manager to see which patterns are supported, if any. This configuration has no effect on a live application, it only affects the history server.

	For now, only YARN mode supports this configuration

- spark.history.custom.executor.log.url.applyIncompleteApplication	

	Default: true	
	
	Since Version: 3.0.0
	
	Specifies whether to apply custom spark executor log URL to incomplete applications as well. If executor logs for running applications should be provided as origin log URLs, set this to `false`. Please note that incomplete applications may include applications which didn't shutdown gracefully. Even this is set to `true`, this configuration has no effect on a live application, it only affects the history server.	

- spark.history.fs.eventLog.rolling.maxFilesToRetain	

	Default: Int.MaxValue	
	
	Since Version: 3.0.0
	
	The maximum number of event log files which will be retained as non-compacted. By default, all event log files will be retained. The lowest value is 1 for technical reason.

	Please read the section of "Applying compaction of old event log files" for more details.	

- spark.history.store.hybridStore.enabled	

	Default: false	
	
	Since Version: 3.1.0
	
	Whether to use HybridStore as the store when parsing event logs. HybridStore will first write data to an in-memory store and having a background thread that dumps data to a disk store after the writing to in-memory store is completed.	

- spark.history.store.hybridStore.maxMemoryUsage	

	Default: 2g	
	
	Since Version: 3.1.0
	
	Maximum memory space that can be used to create HybridStore. The HybridStore co-uses the heap memory, so the heap memory should be increased through the memory option for SHS if the HybridStore is enabled.	

> Note that in all of these UIs, the tables are sortable by clicking their headers, making it easy to identify slow tasks, data skew, etc.

Note

> The history server displays both completed and incomplete Spark jobs. If an application makes multiple attempts after failures, the failed attempts will be displayed, as well as any ongoing incomplete attempt or the final successful attempt.

> Incomplete applications are only updated intermittently. The time between updates is defined by the interval between checks for changed files (`spark.history.fs.update.interval`). On larger clusters, the update interval may be set to large values. The way to view a running application is actually to view its own web UI.

> Applications which exited without registering themselves as completed will be listed as incomplete —even though they are no longer running. This can happen if an application crashes.

> One way to signal the completion of a Spark job is to stop the Spark Context explicitly (`sc.stop()`), or in Python using the with `SparkContext()` as `sc`: construct to handle the Spark Context setup and tear down.

### REST API

> In addition to viewing the metrics in the UI, they are also available as JSON. This gives developers an easy way to create new visualizations and monitoring tools for Spark. The JSON is available for both running applications, and in the history server. The endpoints are mounted at /api/v1. For example, for the history server, they would typically be accessible at `http://<server-url>:18080/api/v1`, and for a running application, at `http://localhost:4040/api/v1`.

> In the API, an application is referenced by its application ID, `[app-id]`. When running on YARN, each application may have multiple attempts, but there are attempt IDs only for applications in cluster mode, not applications in client mode. Applications in YARN cluster mode can be identified by their `[attempt-id]`. In the API listed below, when running in YARN cluster mode, `[app-id]` will actually be `[base-app-id]/[attempt-id]`, where `[base-app-id]` is the YARN application ID.

- `/applications	`

	A list of all applications.
	- `?status=[completed|running]`  list only applications in the chosen state.
	- `?minDate=[date]` earliest start date/time to list.
	- `?maxDate=[date]` latest start date/time to list.
	- `?minEndDate=[date]` earliest end date/time to list.
	- `?maxEndDate=[date]` latest end date/time to list.
	- `?limit=[limit]` limits the number of applications listed.
	
	Examples:
	
	- `?minDate=2015-02-10`
	- `?minDate=2015-02-03T16:42:40.000GMT`
	- `?maxDate=2015-02-11T20:41:30.000GMT`
	- `?minEndDate=2015-02-12`
	- `?minEndDate=2015-02-12T09:15:10.000GMT`
	- `?maxEndDate=2015-02-14T16:30:45.000GMT`
	- `?limit=10`

- `/applications/[app-id]/jobs`	

	A list of all jobs for a given application.

	- `?status=[running|succeeded|failed|unknown]` list only jobs in the specific state.

- `/applications/[app-id]/jobs/[job-id]`	

	Details for the given job.

- `/applications/[app-id]/stages`	

	A list of all stages for a given application.
	
	- `?status=[active|complete|pending|failed]` list only stages in the given state.
	
	- `?details=true` lists all stages with the task data.
	
	- `?taskStatus=[RUNNING|SUCCESS|FAILED|KILLED|PENDING]` lists only those tasks with the specified task status. Query parameter taskStatus takes effect only when `details=true`. This also supports multiple `taskStatus` such as `?details=true&taskStatus=SUCCESS&taskStatus=FAILED` which will return all tasks matching any of specified task status.
	
	`?withSummaries=true` lists stages with task metrics distribution and executor metrics distribution.
	
	`?quantiles=0.0,0.25,0.5,0.75,1.0` summarize the metrics with the given quantiles. Query parameter quantiles takes effect only when `withSummaries=true`. Default value is 0.0,0.25,0.5,0.75,1.0.

- `/applications/[app-id]/stages/[stage-id]`	

	A list of all attempts for the given stage.

	+ `?details=true` lists all attempts with the task data for the given stage.
	
	+ `?taskStatus=[RUNNING|SUCCESS|FAILED|KILLED|PENDING]` lists only those tasks with the specified task status. Query parameter taskStatus takes effect only when `details=true`. This also supports multiple taskStatus such as `?details=true&taskStatus=SUCCESS&taskStatus=FAILED` which will return all tasks matching any of specified task status.
	
	+ `?withSummaries=tru`e lists task metrics distribution and executor metrics distribution of each attempt.
	?quantiles=0.0,0.25,0.5,0.75,1.0 summarize the metrics with the given quantiles. Query parameter quantiles takes effect only when withSummaries=true. Default value is 0.0,0.25,0.5,0.75,1.0.
	
	Example:
	+ `?details=true`
	+ `?details=true&taskStatus=RUNNIN`G
	+ `?withSummaries=true`
	+ `?details=true&withSummaries=true&quantiles=0.01,0.5,0.99`

- `/applications/[app-id]/stages/[stage-id]/[stage-attempt-id]`	

	Details for the given stage attempt.

	+ `?details=true` lists all task data for the given stage attempt.
	
	+ `?taskStatus=[RUNNING|SUCCESS|FAILED|KILLED|PENDING]` lists only those tasks with the specified task status. Query parameter taskStatus takes effect only when `details=true`. This also supports multiple taskStatus such as `?details=true&taskStatus=SUCCESS&taskStatus=FAILED` which will return all tasks matching any of specified task status.
	
	+ `?withSummaries=true` lists task metrics distribution and executor metrics distribution for the given stage attempt.
	
	+ `?quantiles=0.0,0.25,0.5,0.75,1.0` summarize the metrics with the given quantiles. Query parameter quantiles takes effect only when `withSummaries=true`. Default value is 0.0,0.25,0.5,0.75,1.0.
	
	Example:
	+ `?details=true`
	+ `?details=true&taskStatus=RUNNING`
	+ `?withSummaries=tru`e
	+ `?details=true&withSummaries=true&quantiles=0.01,0.5,0.99`

- `/applications/[app-id]/stages/[stage-id]/[stage-attempt-id]/taskSummary`	

	Summary metrics of all tasks in the given stage attempt.
	
	+ `?quantiles` summarize the metrics with the given quantiles.
	
	Example: `?quantiles=0.01,0.5,0.99`

- `/applications/[app-id]/stages/[stage-id]/[stage-attempt-id]/taskList`	

	A list of all tasks for the given stage attempt.
	
	+ `?offset=[offset]&length=[len]` list tasks in the given range.
	
	+ `?sortBy=[runtime|-runtime]` sort the tasks.
	
	+ `?status=[running|success|killed|failed|unknown]` list only tasks in the state.
	
	Example: `?offset=10&length=50&sortBy=runtime&status=running`

- `/applications/[app-id]/executors`	

	A list of all active executors for the given application.

- `/applications/[app-id]/executors/[executor-id]/threads`	

	Stack traces of all the threads running within the given active executor. Not available via the history server.

- `/applications/[app-id]/allexecutors`

	A list of all(active and dead) executors for the given application.

- `/applications/[app-id]/storage/rdd`	

	A list of stored RDDs for the given application.

- `/applications/[app-id]/storage/rdd/[rdd-id]`	

	Details for the storage status of a given RDD.

- `/applications/[base-app-id]/logs`	

	Download the event logs for all attempts of the given application as files within a zip file.

- `/applications/[base-app-id]/[attempt-id]/logs`	

	Download the event logs for a specific application attempt as a zip file.

- `/applications/[app-id]/streaming/statistics`	

	Statistics for the streaming context.

- `/applications/[app-id]/streaming/receivers`	

	A list of all streaming receivers.

- `/applications/[app-id]/streaming/receivers/[stream-id]`

	Details of the given receiver.

- `/applications/[app-id]/streaming/batches`	

	A list of all retained batches.

- `/applications/[app-id]/streaming/batches/[batch-id]`	

	Details of the given batch.

- `/applications/[app-id]/streaming/batches/[batch-id]/operations`	

	A list of all output operations of the given batch.

- `/applications/[app-id]/streaming/batches/[batch-id]/operations/[outputOp-id]`	

	Details of the given operation and given batch.

- `/applications/[app-id]/sql`	

	A list of all queries for a given application.

	- `?details=[true (default) | false]` lists/hides details of Spark plan nodes.

	- `?planDescription=[true (default) | false]` enables/disables Physical planDescription on demand when Physical Plan size is high.

	- `?offset=[offset]&length=[len`] lists queries in the given range.

- `/applications/[app-id]/sql/[execution-id]`	

	Details for the given query.

	- `?details=[true (default) | false]` lists/hides metric details in addition to given query details.

	- `?planDescription=[true (default) | false]` enables/disables Physical planDescription on demand for the given query when Physical Plan size is high.

- `/applications/[app-id]/environment`	

	Environment details of the given application.

- `/version`	

	Get the current spark version.

> The number of jobs and stages which can be retrieved is constrained by the same retention mechanism of the standalone Spark UI; `"spark.ui.retainedJobs"` defines the threshold value triggering garbage collection on jobs, and `spark.ui.retainedStages` that for stages. Note that the garbage collection takes place on playback: it is possible to retrieve more entries by increasing these values and restarting the history server.

#### Executor Task Metrics

> The REST API exposes the values of the Task Metrics collected by Spark executors with the granularity of task execution. The metrics can be used for performance troubleshooting and workload characterization. A list of the available metrics, with a short description:

表格见[原文](https://spark.apache.org/docs/3.3.2/monitoring.html#executor-task-metrics)

#### Executor Metrics

> Executor-level metrics are sent from each executor to the driver as part of the Heartbeat to describe the performance metrics of Executor itself like JVM heap memory, GC information. Executor metric values and their measured memory peak values per executor are exposed via the REST API in JSON format and in Prometheus format. The JSON end point is exposed at: `/applications/[app-id]/executors`, and the Prometheus endpoint at: `/metrics/executors/prometheus`. The Prometheus endpoint is conditional to a configuration parameter: `spark.ui.prometheus.enabled=true` (the default is `false`). In addition, aggregated per-stage peak values of the executor memory metrics are written to the event log if `spark.eventLog.logStageExecutorMetrics` is true.

> Executor memory metrics are also exposed via the Spark metrics system based on the [Dropwizard metrics library](http://metrics.dropwizard.io/4.2.0). A list of the available metrics, with a short description:

表格见[原文](https://spark.apache.org/docs/3.3.2/monitoring.html#executor-metrics)

> The computation of RSS and Vmem are based on [proc(5)](http://man7.org/linux/man-pages/man5/proc.5.html)

#### API Versioning Policy

> These endpoints have been strongly versioned to make it easier to develop applications on top. In particular, Spark guarantees:

> Endpoints will never be removed from one version
> Individual fields will never be removed for any given endpoint
> New endpoints may be added
> New fields may be added to existing endpoints
> New versions of the api may be added in the future as a separate endpoint (e.g., `api/v2`). New versions are not required to be backwards compatible.
> Api versions may be dropped, but only after at least one minor release of co-existing with a new api version.

> Note that even when examining the UI of running applications, the `applications/[app-id]` portion is still required, though there is only one application available. E.g. to see the list of jobs for the running app, you would go to `http://localhost:4040/api/v1/applications/[app-id]/jobs`. This is to keep the paths consistent in both modes.

## Metrics

> Spark has a configurable metrics system based on the [Dropwizard Metrics Library](http://metrics.dropwizard.io/4.2.0). This allows users to report Spark metrics to a variety of sinks including HTTP, JMX, and CSV files. The metrics are generated by sources embedded in the Spark code base. They provide instrumentation for specific activities and Spark components. The metrics system is configured via a configuration file that Spark expects to be present at `$SPARK_HOME/conf/metrics.properties`. A custom file location can be specified via the `spark.metrics.conf` [configuration property](https://spark.apache.org/docs/3.3.2/configuration.html#spark-properties). Instead of using the configuration file, a set of configuration parameters with prefix `spark.metrics.conf.` can be used. By default, the root namespace used for driver or executor metrics is the value of spark.app.id. However, often times, users want to be able to track the metrics across apps for driver and executors, which is hard to do with application ID (i.e. `spark.app.id`) since it changes with every invocation of the app. For such use cases, a custom namespace can be specified for metrics reporting using `spark.metrics.namespace` configuration property. If, say, users wanted to set the metrics namespace to the name of the application, they can set the spark.metrics.namespace property to a value like `${spark.app.name}`. This value is then expanded appropriately by Spark and is used as the root namespace of the metrics system. Non-driver and executor metrics are never prefixed with `spark.app.id`, nor does the `spark.metrics.namespace` property have any such affect on such metrics.

> Spark’s metrics are decoupled into different instances corresponding to Spark components. Within each instance, you can configure a set of sinks to which metrics are reported. The following instances are currently supported:

- `master`: The Spark standalone master process.
- `applications`: A component within the master which reports on various applications.
- `worker`: A Spark standalone worker process.
- `executor`: A Spark executor.
- `driver`: The Spark driver process (the process in which your SparkContext is created).
- `shuffleService`: The Spark shuffle service.
- `applicationMaster`: The Spark ApplicationMaster when running on YARN.
- `mesos_cluster`: The Spark cluster scheduler when running on Mesos.

> Each instance can report to zero or more sinks. Sinks are contained in the org.apache.spark.metrics.sink package:

- `ConsoleSink`: Logs metrics information to the console.
- `CSVSink`: Exports metrics data to CSV files at regular intervals.
- `JmxSink`: Registers metrics for viewing in a JMX console.
- `MetricsServlet`: Adds a servlet within the existing Spark UI to serve metrics data as JSON data.
- `PrometheusServlet`: (Experimental) Adds a servlet within the existing Spark UI to serve metrics data in Prometheus format.
- `GraphiteSink`: Sends metrics to a Graphite node.
- `Slf4jSink`: Sends metrics to slf4j as log entries.
- `StatsdSink`: Sends metrics to a StatsD node.

> Spark also supports a Ganglia sink which is not included in the default build due to licensing restrictions:

- `GangliaSink`: Sends metrics to a Ganglia node or multicast group.

> To install the `GangliaSink` you’ll need to perform a custom build of Spark. Note that by embedding this library you will include LGPL-licensed code in your Spark package. For sbt users, set the `SPARK_GANGLIA_LGPL` environment variable before building. For Maven users, enable the `-Pspark-ganglia-lgpl` profile. In addition to modifying the cluster’s Spark build user applications will need to link to the `spark-ganglia-lgpl` artifact.

> The syntax of the metrics configuration file and the parameters available for each sink are defined in an example configuration file, `$SPARK_HOME/conf/metrics.properties.template`.

> When using Spark configuration parameters instead of the metrics configuration file, the relevant parameter names are composed by the prefix spark.metrics.conf. followed by the configuration details, i.e. the parameters take the following form: `spark.metrics.conf.[instance|*].sink.[sink_name].[parameter_name]`. This example shows a list of Spark configuration parameters for a Graphite sink:

	"spark.metrics.conf.*.sink.graphite.class"="org.apache.spark.metrics.sink.GraphiteSink"
	"spark.metrics.conf.*.sink.graphite.host"="graphiteEndPoint_hostName>"
	"spark.metrics.conf.*.sink.graphite.port"=<graphite_listening_port>
	"spark.metrics.conf.*.sink.graphite.period"=10
	"spark.metrics.conf.*.sink.graphite.unit"=seconds
	"spark.metrics.conf.*.sink.graphite.prefix"="optional_prefix"
	"spark.metrics.conf.*.sink.graphite.regex"="optional_regex_to_send_matching_metrics"

> Default values of the Spark metrics configuration are as follows:

	"*.sink.servlet.class" = "org.apache.spark.metrics.sink.MetricsServlet"
	"*.sink.servlet.path" = "/metrics/json"
	"master.sink.servlet.path" = "/metrics/master/json"
	"applications.sink.servlet.path" = "/metrics/applications/json"

> Additional sources can be configured using the metrics configuration file or the configuration parameter `spark.metrics.conf.[component_name].source.jvm.class=[source_name]`. At present the JVM source is the only available optional source. For example the following configuration parameter activates the JVM source: `"spark.metrics.conf.*.source.jvm.class"="org.apache.spark.metrics.source.JvmSource"`

### List of available metrics providers

> Metrics used by Spark are of multiple types: gauge, counter, histogram, meter and timer, see [Dropwizard library documentation for details](https://metrics.dropwizard.io/4.2.0/getting-started.html). The following list of components and metrics reports the name and some details about the available metrics, grouped per component instance and source namespace. The most common time of metrics used in Spark instrumentation are gauges and counters. Counters can be recognized as they have the `.count` suffix. Timers, meters and histograms are annotated in the list, the rest of the list elements are metrics of type gauge. The large majority of metrics are active as soon as their parent component instance is configured, some metrics require also to be enabled via an additional configuration parameter, the details are reported in the list.

以下见[原文](https://spark.apache.org/docs/3.3.2/monitoring.html#component-instance--driver)

#### Component instance = Driver  
#### Component instance = Executor
#### Source = JVM Source
#### Component instance = applicationMaster
#### Component instance = mesos_cluster
#### Component instance = master
#### Component instance = ApplicationSource
#### Component instance = worker
#### Component instance = shuffleService

## Advanced Instrumentation

> Several external tools can be used to help profile the performance of Spark jobs:

> Cluster-wide monitoring tools, such as [Ganglia](http://ganglia.sourceforge.net/), can provide insight into overall cluster utilization and resource bottlenecks. For instance, a Ganglia dashboard can quickly reveal whether a particular workload is disk bound, network bound, or CPU bound.
> OS profiling tools such as [dstat](http://dag.wieers.com/home-made/dstat/), [iostat](http://linux.die.net/man/1/iostat), and [iotop](http://linux.die.net/man/1/iotop) can provide fine-grained profiling on individual nodes.
> JVM utilities such as `jstack` for providing stack traces, `jmap` for creating heap-dumps, `jstat` for reporting time-series statistics and `jconsole` for visually exploring various JVM properties are useful for those comfortable with JVM internals.

> Spark also provides a plugin API so that custom instrumentation code can be added to Spark applications. There are two configuration keys available for loading plugins into Spark:

- spark.plugins
- spark.plugins.defaultList

> Both take a comma-separated list of class names that implement the `org.apache.spark.api.plugin.SparkPlugin` interface. The two names exist so that it’s possible for one list to be placed in the Spark default config file, allowing users to easily add other plugins from the command line without overwriting the config file’s list. Duplicate plugins are ignored.