SchedulerBackend
在 `04_启动Driver线程.md` 中，启动了Driver线程、注册了 ApplicationMaster、从 ResourceManager 请求到了资源。

在 `05_启动Executor进程.md` 中，在每个容器里启动了 executor:

- 先对 ResourceManager 授予的容器按主机和机架进行分类（执行程序时首选位置的考虑）
- 遍历每个容器，在每个容器里启动 executor 
  - 在 ApplicationMaster 的一个线程池里，创建一个 ExecutorRunnable 对象
  - 在运行这个对象时，会创建并启动一个 NMClient 服务，然后就开始启动容器（Executor）【找到一个NodeManager来启动Executor】
    - 准备好一个启动 YarnCoarseGrainedExecutorBackend 的命令，也就是启动 Executor 进程的命令，然后再执行启动
  
接下来，就查看 YarnCoarseGrainedExecutorBackend 的 main 方法：

YarnCoarseGrainedExecutorBackend.scala

```scala
/**
 * Custom implementation of CoarseGrainedExecutorBackend for YARN resource manager.
 * This class extracts executor log URLs and executor attributes from system environment which
 * properties are available for container being set via YARN.
 * 为 YARN resource manager 定制的 CoarseGrainedExecutorBackend 实现。
 * 这个类从系统环境中抽取了 executor 的日志URLs和 executor 属性，属性对通过 YARN 设置的容器是可用的。
 */
private[spark] class YarnCoarseGrainedExecutorBackend(
    rpcEnv: RpcEnv,
    driverUrl: String,
    executorId: String,
    bindAddress: String,
    hostname: String,
    cores: Int,
    env: SparkEnv,
    resourcesFile: Option[String],
    resourceProfile: ResourceProfile)
  extends CoarseGrainedExecutorBackend(
    rpcEnv,
    driverUrl,
    executorId,
    bindAddress,
    hostname,
    cores,
    env,
    resourcesFile,
    resourceProfile) with Logging {

  private[spark] object YarnCoarseGrainedExecutorBackend extends Logging {

    def main(args: Array[String]): Unit = {
      // 创建了一个 YarnCoarseGrainedExecutorBackend 对象的方法
      val createFn: (RpcEnv, CoarseGrainedExecutorBackend.Arguments, SparkEnv, ResourceProfile) =>
              CoarseGrainedExecutorBackend = {
        case (rpcEnv, arguments, env, resourceProfile) =>
          new YarnCoarseGrainedExecutorBackend(rpcEnv, arguments.driverUrl, arguments.executorId,
            arguments.bindAddress, arguments.hostname, arguments.cores,
            env, arguments.resourcesFileOpt, resourceProfile)
      }
      // 解析参数，如 --driver-url  --executor-id  --bind-address  --cores
      val backendArgs = CoarseGrainedExecutorBackend.parseArguments(args,
        this.getClass.getCanonicalName.stripSuffix("$"))
      CoarseGrainedExecutorBackend.run(backendArgs, createFn)
      System.exit(0)
    }
  }
}
```

CoarseGrainedExecutorBackend.scala

```scala
  def run(
      arguments: Arguments,
      backendCreateFn: (RpcEnv, Arguments, SparkEnv, ResourceProfile) =>
        CoarseGrainedExecutorBackend): Unit = {

    Utils.initDaemon(log)

    SparkHadoopUtil.get.runAsSparkUser { () =>
      // Debug code
      Utils.checkHost(arguments.hostname)

      // Bootstrap to fetch the driver's Spark properties.
      val executorConf = new SparkConf
      /* 
         RpcEnv:
            An RPC environment. RpcEndpoints need to register itself with a name to RpcEnv to receives messages. 
            Then RpcEnv will process messages sent from RpcEndpointRef or remote nodes, and deliver them to corresponding RpcEndpoints.
            一个 RPC 环境。 
            RpcEndpoints 需要向 RpcEnv 注册一个名称来接收消息。
            然后，RpcEnv 将处理从 RpcEndpointRef 或远程节点发送的消息，并将它们传递给相应的 RpcEndpoints
       */
      // 创建一个 RpcEnv 对象
      val fetcher = RpcEnv.create(
        "driverPropsFetcher",
        arguments.bindAddress,
        arguments.hostname,
        -1,
        executorConf,
        new SecurityManager(executorConf),
        numUsableCores = 0,
        clientMode = true)
      // RpcEndpointRef: A reference for a remote RpcEndpoint 一个远程RpcEndpoint的引用
      var driver: RpcEndpointRef = null
      val nTries = 3
      for (i <- 0 until nTries if driver == null) {
        try {
          // ApplicationMaster 中的 Driver 的 一个远程RpcEndpoint的引用（在 CoarseGrainedExecutorBackend 中）
          driver = fetcher.setupEndpointRefByURI(arguments.driverUrl)
        } catch {
          case e: Throwable => if (i == nTries - 1) {
            throw e
          }
        }
      }

      val cfg = driver.askSync[SparkAppConfig](RetrieveSparkAppConfig(arguments.resourceProfileId))
      val props = cfg.sparkProperties ++ Seq[(String, String)](("spark.app.id", arguments.appId))
      fetcher.shutdown()

      // Create SparkEnv using properties we fetched from the driver.
      val driverConf = new SparkConf()
      for ((key, value) <- props) {
        // this is required for SSL in standalone mode
        if (SparkConf.isExecutorStartupConf(key)) {
          driverConf.setIfMissing(key, value)
        } else {
          driverConf.set(key, value)
        }
      }

      cfg.hadoopDelegationCreds.foreach { tokens =>
        SparkHadoopUtil.get.addDelegationTokens(tokens, driverConf)
      }

      driverConf.set(EXECUTOR_ID, arguments.executorId)
      // 为 Executor 创建一个 SparkEnv
      val env = SparkEnv.createExecutorEnv(driverConf, arguments.executorId, arguments.bindAddress,
        arguments.hostname, arguments.cores, cfg.ioEncryptionKey, isLocal = false)
      // Set the application attemptId in the BlockStoreClient if available.
      val appAttemptId = env.conf.get(APP_ATTEMPT_ID)
      appAttemptId.foreach(attemptId =>
        env.blockManager.blockStoreClient.setAppAttemptId(attemptId)
      )
      // 这里才创建一个 YarnCoarseGrainedExecutorBackend 对象  （前面的driver的引用和ExecutorEnv都在YarnCoarseGrainedExecutorBackend里）
      val backend = backendCreateFn(env.rpcEnv, arguments, env, cfg.resourceProfile)
      // setupEndpoint: Register a RpcEndpoint with a name and return its RpcEndpointRef.
      // 在 ExecutorEnv 里的 rpcEnv 里注册一个 名称为 Executor 的终端
      env.rpcEnv.setupEndpoint("Executor", backend)
      arguments.workerUrl.foreach { url =>
        env.rpcEnv.setupEndpoint("WorkerWatcher",
          new WorkerWatcher(env.rpcEnv, url, isChildProcessStopping = backend.stopping))
      }
      env.rpcEnv.awaitTermination()
    }
  }
```

查看 RpcEnv 子类 NettyRpcEnv 的 setupEndpoint 方法

```scala
  override def setupEndpoint(name: String, endpoint: RpcEndpoint): RpcEndpointRef = {
    /*
      Dispatcher: A message dispatcher, responsible for routing RPC messages to the appropriate endpoint(s).
                  负责路由 rpc 消息给合适的终端
      1.把这个终端及其引用放入到 Dispatcher 里的一个名为 endpointRefs 的 map 里
      2.为这个终端创建 MessageLoop
     */
    dispatcher.registerRpcEndpoint(name, endpoint)
  }
```

Dispatcher.scala

```scala
// 1.把这个终端及其引用放入到 Dispatcher 里的一个名为 endpointRefs 的 map 里
// 2.为这个终端创建 MessageLoop
// 此时，Dispatcher 就知道这个终端了，且有一个专门为这个终端发送消息的 MessageLoop
def registerRpcEndpoint(name: String, endpoint: RpcEndpoint): NettyRpcEndpointRef = {
    val addr = RpcEndpointAddress(nettyEnv.address, name)
    val endpointRef = new NettyRpcEndpointRef(nettyEnv.conf, addr, nettyEnv)
    synchronized {
      if (stopped) {
        throw new IllegalStateException("RpcEnv has been stopped")
      }
      if (endpoints.containsKey(name)) {
        throw new IllegalArgumentException(s"There is already an RpcEndpoint called $name")
      }
    
      /*
        private val endpointRefs: ConcurrentMap[RpcEndpoint, RpcEndpointRef] =
             new ConcurrentHashMap[RpcEndpoint, RpcEndpointRef]
       */
      
      // This must be done before assigning RpcEndpoint to MessageLoop, as MessageLoop sets Inbox be
      // active when registering, and endpointRef must be put into endpointRefs before onStart is
      // called.
      // 把这个终端及其引用放入到 Dispatcher 里的一个名为 endpointRefs 的 map 里
      endpointRefs.put(endpoint, endpointRef)
      /*
        MessageLoop: A message loop used by Dispatcher to deliver messages to endpoint
                     Dispatcher 用来传递消息给终端
       */
      var messageLoop: MessageLoop = null
      try {
        // 对传来的 endpoint 进行模式匹配，进而创建不同类型的 MessageLoop
        messageLoop = endpoint match {
          // YarnCoarseGrainedExecutorBackend --> CoarseGrainedExecutorBackend --> IsolatedRpcEndpoint
          case e: IsolatedRpcEndpoint =>  
            /*
              DedicatedMessageLoop: A message loop that is dedicated to a single RPC endpoint.
                    专门发给一个 RPC 终端的 MessageLoop
              在这个 MessageLoop 会创建一个 Inbox （为一个 RpcEndpoint 存储消息，并将消息 post 给 RpcEndpoint）
             */
            new DedicatedMessageLoop(name, e, this)
          case _ =>
            sharedLoop.register(name, endpoint)
            sharedLoop
        }
        endpoints.put(name, messageLoop)
      } catch {
        case NonFatal(e) =>
          endpointRefs.remove(endpoint)
          throw e
      }
    }
    endpointRef
  }
```

MessageLoop.scala

```scala
/**
 * A message loop that is dedicated to a single RPC endpoint.
 */
private class DedicatedMessageLoop(
    name: String,
    endpoint: IsolatedRpcEndpoint,
    dispatcher: Dispatcher)
  extends MessageLoop(dispatcher) {
  // 为一个 RpcEndpoint 存储消息，并将消息 post 给 RpcEndpoint
  private val inbox = new Inbox(name, endpoint)
}
```

Inbox.scala

```scala
/**
 * An inbox that stores messages for an [[RpcEndpoint]] and posts messages to it thread-safely.
 * 收件箱 
 * 【post可以理解成告知、公布信息】
 */
private[netty] class Inbox(val endpointName: String, val endpoint: RpcEndpoint)
  extends Logging {

  protected val messages = new java.util.LinkedList[InboxMessage]()
  
  // OnStart should be the first message to process
  // OnStart 应该是要处理的第一条消息 （这条消息发给自己，告诉自己要启动，即 onStart，这里的自己就是前面创建的 YarnCoarseGrainedExecutorBackend）
  // onStart 是一个 endpoint 生命周期的一个阶段  （constructor -> onStart -> receive* -> onStop）
  inbox.synchronized {
    messages.add(OnStart)
  }
}
```

YarnCoarseGrainedExecutorBackend 接收到这个第一条消息后，就执行 onStart 方法。

在其父类 CoarseGrainedExecutorBackend.scala 中查找到这个方法

```scala
override def onStart(): Unit = {
  if (env.conf.get(DECOMMISSION_ENABLED)) {
    val signal = env.conf.get(EXECUTOR_DECOMMISSION_SIGNAL)
    logInfo(s"Registering SIG$signal handler to trigger decommissioning.")
    SignalUtils.register(signal, s"Failed to register SIG$signal handler - disabling" +
            s" executor decommission feature.") (self.askSync[Boolean](ExecutorDecommissionSigReceived))
  }

  logInfo("Connecting to driver: " + driverUrl)
  try {
    if (PlatformDependent.directBufferPreferred() &&
            PlatformDependent.maxDirectMemory() < env.conf.get(MAX_REMOTE_BLOCK_SIZE_FETCH_TO_MEM)) {
      throw new SparkException(s"Netty direct memory should at least be bigger than " +
              s"'${MAX_REMOTE_BLOCK_SIZE_FETCH_TO_MEM.key}', but got " +
              s"${PlatformDependent.maxDirectMemory()} bytes < " +
              s"${env.conf.get(MAX_REMOTE_BLOCK_SIZE_FETCH_TO_MEM)}")
    }

    _resources = parseOrFindResources(resourcesFileOpt)
  } catch {
    case NonFatal(e) =>
      exitExecutor(1, "Unable to create executor due to " + e.getMessage, e)
  }
  rpcEnv.asyncSetupEndpointRefByURI(driverUrl).flatMap { ref =>
    // This is a very fast action so we can use "ThreadUtils.sameThread"
    driver = Some(ref)
    env.executorBackend = Option(this)
    /*
      ask:Send a message to the corresponding ) and return a Future to receive the reply within a default timeout.
          This method only sends the message once and never retries
          发送消息给对应的终端 receiveAndReply 方法，并返回一个Future以在默认超时内接收回复。
          这个方法仅发送一次消息，不会重试
     */
    //（1.1节） driver引用(YarnCoarseGrainedExecutorBackend)给对应的终端(Driver)发送消息，消息就是 RegisterExecutor对象, 
    // 发的消息就是要向 Driver 注册 Executor
    ref.ask[Boolean](RegisterExecutor(executorId, self, hostname, cores, extractLogUrls,
      extractAttributes, _resources, resourceProfile.id))
  }(ThreadUtils.sameThread).onComplete {
    // Driver回复注册成功后，这里再给自己发送 （1.2节）
    case Success(_) =>
      /*
           // Sends a one-way asynchronous message. Fire-and-forget semantics.
           // 发送单向异步消息。发送后不管的语义。
          def send(message: Any): Unit
          
          // Message used internally to start the executor when the driver successfully accepted the registration request.
          // 当 Driver 成功地接受注册请求时，用于启动 executor 的消息
          case object RegisteredExecutor
      */
      self.send(RegisteredExecutor)
    case Failure(e) =>
      exitExecutor(1, s"Cannot register with driver: $driverUrl", e, notifyDriver = false)
  }(ThreadUtils.sameThread)
}
```

## 1.1 Driver 接收消息

在 `ref.ask[Boolean](RegisterExecutor)...` 中，driver引用给对应的终端(Driver)发送了消息，那么 Driver 就能接收

所以，这里查看 Driver 终端里的 SparkContext 

```scala
class SparkContext(config: SparkConf) extends Logging {
  
  private var _schedulerBackend: SchedulerBackend = _
  
}
```

```scala
class CoarseGrainedSchedulerBackend(scheduler: TaskSchedulerImpl, val rpcEnv: RpcEnv)
  extends ExecutorAllocationClient with SchedulerBackend with Logging {

  // 接收消息，并回复
  override def receiveAndReply(context: RpcCallContext): PartialFunction[Any, Unit] = {
    // 就匹配到了这里
    case RegisterExecutor(executorId, executorRef, hostname, cores, logUrls,
    attributes, resources, resourceProfileId) =>
      
      if (executorDataMap.contains(executorId)) {
        context.sendFailure(new IllegalStateException(s"Duplicate executor ID: $executorId"))
      } else if (scheduler.excludedNodes.contains(hostname) ||
              isExecutorExcluded(executorId, hostname)) {
        // If the cluster manager gives us an executor on an excluded node (because it
        // already started allocating those resources before we informed it of our exclusion,
        // or if it ignored our exclusion), then we reject that executor immediately.
        logInfo(s"Rejecting $executorId as it has been excluded.")
        context.sendFailure(
          new IllegalStateException(s"Executor is excluded due to failures: $executorId"))
      } else {
        // If the executor's rpc env is not listening for incoming connections, `hostPort`
        // will be null, and the client connection should be used to contact the executor.
        val executorAddress = if (executorRef.address != null) {
          executorRef.address
        } else {
          context.senderAddress
        }
        // 看这个日志
        logInfo(s"Registered executor $executorRef ($executorAddress) with ID $executorId, " +
                s" ResourceProfileId $resourceProfileId")
        addressToExecutorId(executorAddress) = executorId
        // 注册了一个 executor，那么这个集群的核心数也就相应的增加了
        totalCoreCount.addAndGet(cores)
        totalRegisteredExecutors.addAndGet(1)
        val resourcesInfo = resources.map { case (rName, info) =>
          // tell the executor it can schedule resources up to numSlotsPerAddress times,
          // as configured by the user, or set to 1 as that is the default (1 task/resource)
          val numParts = scheduler.sc.resourceProfileManager
                  .resourceProfileFromId(resourceProfileId).getNumSlotsPerAddress(rName, conf)
          (info.name, new ExecutorResourceInfo(info.name, info.addresses, numParts))
        }
        val data = new ExecutorData(executorRef, executorAddress, hostname,
          0, cores, logUrlHandler.applyPattern(logUrls, attributes), attributes,
          resourcesInfo, resourceProfileId, registrationTs = System.currentTimeMillis())
        // This must be synchronized because variables mutated
        // in this block are read when requesting executors
        CoarseGrainedSchedulerBackend.this.synchronized {
          executorDataMap.put(executorId, data)
          if (currentExecutorIdCounter < executorId.toInt) {
            currentExecutorIdCounter = executorId.toInt
          }
        }
        listenerBus.post(
          SparkListenerExecutorAdded(System.currentTimeMillis(), executorId, data))
        // 接收处理了后，回复了
        // Note: some tests expect the reply to come after we put the executor in the map
        context.reply(true)
      }
      //....
  }
}
```

## 1.2 driver引用(YarnCoarseGrainedExecutorBackend)接收消息

Driver 回复注册成功后，这里再给自己发送一条消息(RegisteredExecutor)

CoarseGrainedExecutorBackend.scala

```scala
override def receive: PartialFunction[Any, Unit] = {
  //....
  case RegisteredExecutor =>
    logInfo("Successfully registered with driver")
    try {
      // 创建 Executor 计算对象  
      // 前面创建启动的 Executor(YarnCoarseGrainedExecutorBackend)是用来通信的
      executor = new Executor(executorId, hostname, env, getUserClassPath, isLocal = false,
        resources = _resources)
      // driver引用(YarnCoarseGrainedExecutorBackend)再给 Driver 发送启动 Executor 的消息 
      // 
      driver.get.send(LaunchedExecutor(executorId))
    } catch {
      case NonFatal(e) =>
        exitExecutor(1, "Unable to create executor due to " + e.getMessage, e)
    }
  //....
}
```

在 `05_启动Executor进程.md` 中，在 NodeManager 中启动了 Executor 进程(YarnCoarseGrainedExecutorBackend), 

在启动这个进程中，会在 rpcEnv 中注册一个名称为 Executor 的终端

- 把这个终端及其引用放入到 Dispatcher（在RpcEnv里） 里的一个名为 endpointRefs 的 map 里
- 在 Dispatcher 中为这个终端创建 MessageLoop
- 在这个 MessageLoop 会创建一个 Inbox, 用于存储、post消息
- 在这个 Inbox 中，会首先给自己(YarnCoarseGrainedExecutorBackend)发送一条 `OnStart` 消息，告诉自己要启动
- 它自己接收到后，执行 onStart 方法
- 在 onStart 方法中，driver引用(YarnCoarseGrainedExecutorBackend)给对应的终端(Driver)发送注册 Executor
- Driver 回复注册成功后，它还会再给自己发送一条消息，用于启动 executor
- 它自己接收到消息后，就会创建 Executor 计算对象，然后再给 Driver 发送启动 Executor 的消息
- 启动 Executor 后，至此，执行环境就准备好了，开始执行用户的程序