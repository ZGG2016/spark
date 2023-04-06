
在 SparkContext 中，有如下几个对象：

```scala
/**
 * Main entry point for Spark functionality. A SparkContext represents the connection to a Spark
 * cluster, and can be used to create RDDs, accumulators and broadcast variables on that cluster.
 * Spark功能的主入口点。
 * 一个 SparkContext 表示和一个 Spark 集群的连接，可以在连接的集群上创建 RDDs、累加器和广播变量
 * 
 * @note Only one `SparkContext` should be active per JVM. You must `stop()` the
 *   active `SparkContext` before creating a new one.
 *   在一个 JVM 上，应当仅有一个活跃的 SparkContext。
 *   在创建一个新的 SparkContext 前，必须调用 stop() 方法停止当前活跃的 SparkContext
 * @param config a Spark Config object describing the application configuration. Any settings in
 *   this config overrides the default configs as well as system properties.
 *   一个描述了应用程序配置项的 Spark Config 对象。
 *   在这个 config 中的任意配置项都会覆盖默认的配置和系统属性
 */
class SparkContext(config: SparkConf) extends Logging {
  private var _conf: SparkConf = _
  private var _env: SparkEnv = _
  private var _schedulerBackend: SchedulerBackend = _
  private var _taskScheduler: TaskScheduler = _
  @volatile private var _dagScheduler: DAGScheduler = _
}
```
- SparkConf: 应用程序的一些配置项
- SparkEnv: 持有运行一个 spark 实例（master或worker）所需的所有运行时环境对象，如序列化器、通信环境等...
- SchedulerBackend: 保持和 executors 连接，调度任务
- TaskScheduler: 任务调度
- DAGScheduler: 阶段的划分调度、任务的切分

## SparkConf

```scala
/**
 * Configuration for a Spark application. Used to set various Spark parameters as key-value pairs.
 * 一个 Spark 应用程序的配置。
 * 用来设置多个 Spark 参数，以键值对的形式。
 * 
 * Most of the time, you would create a SparkConf object with `new SparkConf()`, which will load
 * values from any `spark.*` Java system properties set in your application as well. In this case,
 * parameters you set directly on the `SparkConf` object take priority over system properties.
 * 你需要使用 `new SparkConf()` 创建一个 SparkConf 对象，将会从 `spark.*` 系统属性中加载值。
 * 如果在 SparkConf 对象上直接设置了参数，那么它就会覆盖系统属性。
 * 
 * For unit tests, you can also call `new SparkConf(false)` to skip loading external settings and
 * get the same configuration no matter what the system properties are.
 * 对于单元测试，你可以调用 `new SparkConf(false)`
 * 
 * All setter methods in this class support chaining. For example, you can write
 * `new SparkConf().setMaster("local").setAppName("My app")`.
 *
 * @param loadDefaults whether to also load values from Java system properties
 *
 * @note Once a SparkConf object is passed to Spark, it is cloned and can no longer be modified
 * by the user. Spark does not support modifying the configuration at runtime.
 *  一旦一个 SparkConf 对象被传给 Spark, 它就被复制，并不再能被用户修改。
 */
class SparkConf(loadDefaults: Boolean) extends Cloneable with Logging with Serializable {}
```

## SparkEnv

```scala
/**
 * :: DeveloperApi ::
 * Holds all the runtime environment objects for a running Spark instance (either master or worker),
 * including the serializer, RpcEnv, block manager, map output tracker, etc. Currently
 * Spark code finds the SparkEnv through a global variable, so all the threads can access the same
 * SparkEnv. It can be accessed by SparkEnv.get (e.g. after creating a SparkContext).
 * 持有运行一个 spark 实例（master或worker）所需的所有运行时环境对象，如序列化器、通信环境、块管理器等...
 * 当前，Spark 代码通过一个全局变量找到 SparkEnv，所以所有线程都可以访问到相同的 SparkEnv.
 * SparkEnv 可以通过 SparkEnv.get 方法获得（例如，在创建一个SparkContext之后）。
 */
@DeveloperApi
class SparkEnv (
    val executorId: String,
    private[spark] val rpcEnv: RpcEnv,
    val serializer: Serializer,
    val closureSerializer: Serializer,
    val serializerManager: SerializerManager,
    val mapOutputTracker: MapOutputTracker,
    val shuffleManager: ShuffleManager,
    val broadcastManager: BroadcastManager,
    val blockManager: BlockManager,
    val securityManager: SecurityManager,
    val metricsSystem: MetricsSystem,
    val memoryManager: MemoryManager,
    val outputCommitCoordinator: OutputCommitCoordinator,
    val conf: SparkConf) extends Logging {}
```

## SchedulerBackend

```scala
/**
 * A backend interface for scheduling systems that allows plugging in different ones under
 * TaskSchedulerImpl. We assume a Mesos-like model where the application gets resource offers as
 * machines become available and can launch tasks on them.
 * 调度系统的后端接口   【其子类 CoarseGrainedSchedulerBackend 传入了一个 TaskSchedulerImpl 参数】
 * 我们假设提供一个类似 mesos 的模型，当机器可用时，应用程序可以从这个模型获得资源，并可以在机器上启动任务。
 */
private[spark] trait SchedulerBackend {}
```

```scala
/**
 * A scheduler backend that waits for coarse-grained executors to connect.
 * This backend holds onto each executor for the duration of the Spark job rather than relinquishing
 * executors whenever a task is done and asking the scheduler to launch a new executor for
 * each new task. Executors may be launched in a variety of ways, such as Mesos tasks for the
 * coarse-grained Mesos mode or standalone processes for Spark's standalone deploy mode
 * (spark.deploy.*).
 * 等待粗粒度 executors 连接的一个调度后端。
 * 这个后端在 Spark 作业执行期间，保持和每个 executor 连接，而不是等任务执行完成后放弃 executor，并要求调度器为每个新任务启动一个新的 executor。 【就是不反复启动注册executor】
 * executor 可以通过多种方式启动，比如粗粒度Mesos模式的Mesos任务，或者Spark的独立部署模式的独立进程(spark.deploy.*)。
 * 
 * 传入了一个 TaskSchedulerImpl 参数
 */
private[spark] class CoarseGrainedSchedulerBackend(scheduler: TaskSchedulerImpl, val rpcEnv: RpcEnv)
  extends ExecutorAllocationClient with SchedulerBackend with Logging {}
```

## TaskScheduler

```scala
/**
 * Low-level task scheduler interface, currently implemented exclusively by
 * [[org.apache.spark.scheduler.TaskSchedulerImpl]].
 * This interface allows plugging in different task schedulers. Each TaskScheduler schedules tasks
 * for a single SparkContext. These schedulers get sets of tasks submitted to them from the
 * DAGScheduler for each stage, and are responsible for sending the tasks to the cluster, running
 * them, retrying if there are failures, and mitigating stragglers. They return events to the
 * DAGScheduler.
 * 低等级的任务调度接口，TaskSchedulerImpl 是其唯一实现
 * 这个接口允许插入不同的任务调度器。
 * 每个任务调度器在一个 SparkContext 中调度任务。
 * 对于每个阶段，这些任务调度器接收 DAGScheduler 发给它的任务集，并将任务集发到集群来运行、如果任务失败就重试、mitigating stragglers。
 * 它们返回事件给 DAGScheduler
 */
private[spark] trait TaskScheduler {}
```

```scala
/**
 * Schedules tasks for multiple types of clusters by acting through a SchedulerBackend.
 * It can also work with a local setup by using a `LocalSchedulerBackend` and setting
 * isLocal to true. It handles common logic, like determining a scheduling order across jobs, waking
 * up to launch speculative tasks, etc.
 * 通过 SchedulerBackend 为多种类型的集群调度任务。
 * 。。。
 * 它处理公共逻辑，比如，决定跨 job 的调度顺序、唤醒启动推测任务等
 * 
 * Clients should first call initialize() and start(), then submit task sets through the
 * submitTasks method.
 * 客户端应该首先调用 initialize() 和 start() 方法，然后通过 submitTasks 方法提交任务集。
 *
 * THREADING: [[SchedulerBackend]]s and task-submitting clients can call this class from multiple
 * threads, so it needs locks in public API methods to maintain its state. In addition, some
 * [[SchedulerBackend]]s synchronize on themselves when they want to send events here, and then
 * acquire a lock on us, so we need to make sure that we don't try to lock the backend while
 * we are holding a lock on ourselves.  This class is called from many threads, notably:
 *   * The DAGScheduler Event Loop
 *   * The RPCHandler threads, responding to status updates from Executors
 *   * Periodic revival of all offers from the CoarseGrainedSchedulerBackend, to accommodate delay
 *      scheduling
 *   * task-result-getter threads
 *
 * CAUTION: Any non fatal exception thrown within Spark RPC framework can be swallowed.
 * Thus, throwing exception in methods like resourceOffers, statusUpdate won't fail
 * the application, but could lead to undefined behavior. Instead, we shall use method like
 * TaskSetManger.abort() to abort a stage and then fail the application (SPARK-31485).
 *
 * Delay Scheduling:
 *  Delay scheduling is an optimization that sacrifices job fairness for data locality in order to
 *  improve cluster and workload throughput. One useful definition of "delay" is how much time
 *  has passed since the TaskSet was using its fair share of resources. Since it is impractical to
 *  calculate this delay without a full simulation, the heuristic used is the time since the
 *  TaskSetManager last launched a task and has not rejected any resources due to delay scheduling
 *  since it was last offered its "fair share". A "fair share" offer is when [[resourceOffers]]'s
 *  parameter "isAllFreeResources" is set to true. A "delay scheduling reject" is when a resource
 *  is not utilized despite there being pending tasks (implemented inside [[TaskSetManager]]).
 *  The legacy heuristic only measured the time since the [[TaskSetManager]] last launched a task,
 *  and can be re-enabled by setting spark.locality.wait.legacyResetOnTaskLaunch to true.
 */
private[spark] class TaskSchedulerImpl(
    val sc: SparkContext,
    val maxTaskFailures: Int,
    isLocal: Boolean = false,
    clock: Clock = new SystemClock)
  extends TaskScheduler with Logging {}
```

## DAGScheduler

```scala
/**
 * The high-level scheduling layer that implements stage-oriented scheduling. It computes a DAG of
 * stages for each job, keeps track of which RDDs and stage outputs are materialized, and finds a
 * minimal schedule to run the job. It then submits stages as TaskSets to an underlying
 * TaskScheduler implementation that runs them on the cluster. A TaskSet contains fully independent
 * tasks that can run right away based on the data that's already on the cluster (e.g. map output
 * files from previous stages), though it may fail if this data becomes unavailable.
 * 高等级的调度层，实现了基于阶段的调度。
 * 它为每个作业计算一个阶段的 DAG, 追踪被物化的 RDDs 和阶段输出，并查找运行作业的最小调度。
 * 然后，它以 TaskSets 形式提交阶段给底层的 TaskScheduler, TaskScheduler 会发给集群执行。
 * 一个 TaskSet 是一个完全独立的任务集，可以基于集群上的数据（如，前面阶段的map输出文件）立即运行，尽管这个数据如果变的不可用，任务就会失败
 * 
 * Spark stages are created by breaking the RDD graph at shuffle boundaries. RDD operations with
 * "narrow" dependencies, like map() and filter(), are pipelined together into one set of tasks
 * in each stage, but operations with shuffle dependencies require multiple stages (one to write a
 * set of map output files, and another to read those files after a barrier). In the end, every
 * stage will have only shuffle dependencies on other stages, and may compute multiple operations
 * inside it. The actual pipelining of these operations happens in the RDD.compute() functions of
 * various RDDs
 * Spark 阶段通过在 shuffle 边界处打断 RDD 图来创建。
 * 在每个阶段中，具有“窄”依赖的 RDD 操作，例如 map filter, 被放入一个任务集里，
 * 但是具有 shuffle 依赖的操作就要求多个阶段（一个阶段是将map输出文件写入一个集合，另一个阶段是在一个barrier后读取这些文件）。
 * 最后，每个阶段将只对其他阶段有 shuffle 依赖，并可能在它内部计算多个操作。
 * 这些操作的实际流水线操作发生在各种 RDDs 的 RDD.compute() 函数中
 *
 * In addition to coming up with a DAG of stages, the DAGScheduler also determines the preferred
 * locations to run each task on, based on the current cache status, and passes these to the
 * low-level TaskScheduler. Furthermore, it handles failures due to shuffle output files being
 * lost, in which case old stages may need to be resubmitted. Failures *within* a stage that are
 * not caused by shuffle file loss are handled by the TaskScheduler, which will retry each task
 * a small number of times before cancelling the whole stage.
 * DAGScheduler 也会基于当前的缓存状态，决定运行每个任务的偏好位置，并将任务发送低等级的 TaskScheduler。
 * 此外，它也处理由于 shuffle 输出文件的丢失导致的失败，在这种情况下，旧的阶段需要被重新提交。
 * 一个阶段内部的失败，这个失败不是由于 shuffle 文件丢失导致的，那么这个失败就由 TaskScheduler 处理，
 * TaskScheduler会在取消整个阶段前，重试每个任务几次。
 * 
 * When looking through this code, there are several key concepts:
 * 几个关键的概念：
 * 
 *  - Jobs (represented by [[ActiveJob]]) are the top-level work items submitted to the scheduler.
 *    For example, when the user calls an action, like count(), a job will be submitted through
 *    submitJob. Each Job may require the execution of multiple stages to build intermediate data.
 *  - Jobs
 *        它是提交给调度器的顶层的工作项。
 *        例如，当用户调用一个action操作，例如 count(), 将会通过 submitJob 提交一个 job.
 *        每个 job 可能要求多个阶段执行，来构建中间数据。
 *  
 *  - Stages ([[Stage]]) are sets of tasks that compute intermediate results in jobs, where each
 *    task computes the same function on partitions of the same RDD. Stages are separated at shuffle
 *    boundaries, which introduce a barrier (where we must wait for the previous stage to finish to
 *    fetch outputs). There are two types of stages: [[ResultStage]], for the final stage that
 *    executes an action, and [[ShuffleMapStage]], which writes map output files for a shuffle.
 *    Stages are often shared across multiple jobs, if these jobs reuse the same RDDs.
 *  - Stages
 *          它是 job 中计算中间结果的任务集合，每个任务在相同 RDD 的分区上执行相同的函数。
 *          阶段在 shuffle 边界处划分，边界处引入一个 barrier（在这里，必须等待前面的阶段完成后，再获取输出）
 *          有两种类型的阶段：
 *            （1）ResultStage: 执行action操作的最后的阶段 
 *            （2）ShuffleMapStage: 在一次shuffle中，写入 map 输出文件
 *          如果这些 jobs 重用相同的 RDDs, 阶段通常会跨多个 jobs 共享。
 *  
 *  - Tasks are individual units of work, each sent to one machine.
 *  - Tasks
 *         Tasks是独立的工作单元，每个 Tasks 被发送到一个机器上。
 *  
 *  - Cache tracking: the DAGScheduler figures out which RDDs are cached to avoid recomputing them
 *    and likewise remembers which shuffle map stages have already produced output files to avoid
 *    redoing the map side of a shuffle.
 *  - Cache tracking  【追踪缓存】
 *         DAGScheduler 需要搞清楚缓存了哪个 RDDs, 来避免重新计算它们。
 *         同样，记住哪个 shuffle map stages 已经产生了输出文件，来避免重新执行一回 shuffle 的map端
 * 
 *  - Preferred locations: the DAGScheduler also computes where to run each task in a stage based
 *    on the preferred locations of its underlying RDDs, or the location of cached or shuffle data.
 *  - Preferred locations 【偏好位置】
 *         DAGScheduler 也要计算阶段中的每个任务在哪里计算，基于它底层的 RDDs 的偏好位置，或 缓存的或shuffle数据的位置。
 *         
 *  - Cleanup: all data structures are cleared when the running jobs that depend on them finish,
 *    to prevent memory leaks in a long-running application.
 *  - Cleanup
 *         当依赖于它们的运行作业完成时，将清除所有数据结构，以防止长时间运行的应用程序中的内存泄漏。
 *         
 * To recover from failures, the same stage might need to run multiple times, which are called
 * "attempts". If the TaskScheduler reports that a task failed because a map output file from a
 * previous stage was lost, the DAGScheduler resubmits that lost stage. This is detected through a
 * CompletionEvent with FetchFailed, or an ExecutorLost event. The DAGScheduler will wait a small
 * amount of time to see whether other nodes or tasks fail, then resubmit TaskSets for any lost
 * stage(s) that compute the missing tasks. As part of this process, we might also have to create
 * Stage objects for old (finished) stages where we previously cleaned up the Stage object. Since
 * tasks from the old attempt of a stage could still be running, care must be taken to map any
 * events received in the correct Stage object.
 * 为了从失败中恢复，同样的阶段可能需要运行多次，这就称为“尝试”。
 * 如果 TaskScheduler 报告，由于来自前面阶段的map输出文件丢失，而导致任务失败，DAGScheduler 就会重新提交那个丢失的阶段。
 * 这是通过带有 FetchFailed 的 CompletionEvent 或 ExecutorLost 事件检测到的。
 * DAGScheduler 将等待一会，查看其他节点或任务是否失败，然后为丢失的阶段重新提交 TaskSets。
 * 作为这个过程的一部分，我们可能还需要为之前清理过的阶段对象创建阶段对象。
 * 由于来自旧阶段的尝试任务可能仍然在运行，所以必须小心映射 正确Stage对象中接收的任何事件。
 * 
 * Here's a checklist to use when making or reviewing changes to this class:
 *
 *  - All data structures should be cleared when the jobs involving them end to avoid indefinite
 *    accumulation of state in long-running programs.
 *
 *  - When adding a new data structure, update `DAGSchedulerSuite.assertDataStructuresEmpty` to
 *    include the new structure. This will help to catch memory leaks.
 */
private[spark] class DAGScheduler(
    private[scheduler] val sc: SparkContext,
    private[scheduler] val taskScheduler: TaskScheduler,
    listenerBus: LiveListenerBus,
    mapOutputTracker: MapOutputTrackerMaster,
    blockManagerMaster: BlockManagerMaster,
    env: SparkEnv,
    clock: Clock = new SystemClock())
  extends Logging {}
```