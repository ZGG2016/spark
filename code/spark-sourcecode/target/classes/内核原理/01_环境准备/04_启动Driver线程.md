
在 `03_启动ApplicationMaster.md` 中，调用 YarnClusterApplication 的 start 来启动应用程序，在 start 方法内部：

- 在 spark 端创建一个 Client，其内部还会创建一个 yarnClient (在启动YarnClient服务时会创建一个ApplicationClientProtocol，即rmClient)

- 由 Client 调用 run 方法将应用程序提交给 ResourceManager 
  - 建立 ResourceManager 和客户端间的连接 
  - 调用 start 方法来启动 yarnClient，内部会创建一个 ApplicationClientProtocol，即 rmClient
  - 在 yarnClient 创建了一个 Application
  - 为启动 ApplicationMaster 设置上下文环境（应用程序信息和container等,container会包含拼接成的启动am的命令）
  - 由 yarnClient 调用 submitApplication 方法将一个新的应用程序提交给 yarn
  - 由 rmClient 调用 submitApplication 方法将一个新的应用程序提交给 ResourceManager

当 ResourceManager 接收到客户端发来的这些信息（含启动am的命令）时，就分配一个 NodeManager 执行这个命令，这样就启动了 ApplicationMaster 进程。

接下来就查看 ApplicationMaster 的 main 方法

```scala
object ApplicationMaster extends Logging {
  def main(args: Array[String]): Unit = {
    SignalUtils.registerLogger(log)
    // 解析像 --jar --class 这种参数
    val amArgs = new ApplicationMasterArguments(args)
    val sparkConf = new SparkConf()
    if (amArgs.propertiesFile != null) {
      Utils.getPropertiesFromFile(amArgs.propertiesFile).foreach { case (k, v) =>
        sparkConf.set(k, v)
      }
    }
    // Set system properties for each config entry. This covers two use cases:
    // - The default configuration stored by the SparkHadoopUtil class
    // - The user application creating a new SparkConf in cluster mode
    //
    // Both cases create a new SparkConf object which reads these configs from system properties.
    sparkConf.getAll.foreach { case (k, v) =>
      sys.props(k) = v
    }
    
    // 根据 sparkconf 的配置封装一个 YarnConfiguration 对象
    val yarnConf = new YarnConfiguration(SparkHadoopUtil.newConfiguration(sparkConf))
    // 1.1节 创建一个 ApplicationMaster 对象，在其中创建一个 YarnRMClient 对象
    master = new ApplicationMaster(amArgs, sparkConf, yarnConf)
    //....

    ugi.doAs(new PrivilegedExceptionAction[Unit]() {
      // 1.2节 
      override def run(): Unit = System.exit(master.run())
    })
  }
}
```

## 1.1 new ApplicationMaster()

ApplicationMaster.scala

```scala
/**
 * Common application master functionality for Spark on Yarn.
 */
private[spark] class ApplicationMaster(
    args: ApplicationMasterArguments,
    sparkConf: SparkConf,
    yarnConf: YarnConfiguration) extends Logging {
        // YarnRMClient 用来在 YARN ResourceManager 中处理注册和取消注册 ApplicationMaster
        private val client = new YarnRMClient()
        
    }
```

YarnRMClient.scala

```scala
/**
 * Handles registering and unregistering the application with the YARN ResourceManager.
 * 在 YARN ResourceManager 中处理注册和取消注册应用程序
 */
private[spark] class YarnRMClient extends Logging {
  // AMRMClient: am和rm之间的客户端，就可以理解成 am和rm之间的服务
  private var amClient: AMRMClient[ContainerRequest] = _
  private var uiHistoryAddress: String = _
  private var registered: Boolean = false
  //....
}
```

## 1.2 master.run()

ApplicationMaster.scala

```scala
  final def run(): Int = {

    val attemptID = if (isClusterMode) {
      //.... 配置一些属性 
      System.setProperty("spark.master", "yarn")
      System.setProperty(SUBMIT_DEPLOY_MODE.key, "cluster")
  
      if (isClusterMode) {
        // 这里
        runDriver()
      } else {
        runExecutorLauncher()
      }
    }
}
```

ApplicationMaster.scala

```scala
private def runDriver(): Unit = {
    addAmIpFilter(None, System.getenv(ApplicationConstants.APPLICATION_WEB_PROXY_BASE_ENV))
    // 1.2.1节  创建、启动一个单独的用户线程（也就是Driver线程），来执行用户类的main方法，其中包含 spark driver
    // 执行这条语句，就会初始化SparkContext，初始化完成后，
    //  下面的 `val sc = ThreadUtils.awaitResult`就会检测到，
    //  然后就运行runDriver的线程开始申请资源，而这里创建启动的线程就挂起，直到申请到资源之后，才被唤醒
    userClassThread = startUserApplication()

    // This a bit hacky, but we need to wait until the spark.driver.port property has
    // been set by the Thread executing the user class.
    logInfo("Waiting for spark context initialization...")
    val totalWaitTime = sparkConf.get(AM_MAX_WAIT_TIME)
    try {
      // sparkContextPromise: In cluster mode, used to tell the AM when the user's SparkContext has been initialized.
      // sparkContextPromise: 在集群模式下，用于告诉 AM 的 SparkContext 已经初始化。
      // 等待 SparkContext 初始化完成
      val sc = ThreadUtils.awaitResult(sparkContextPromise.future,
        Duration(totalWaitTime, TimeUnit.MILLISECONDS))
      if (sc != null) {
        val rpcEnv = sc.env.rpcEnv

        val userConf = sc.getConf
        val host = userConf.get(DRIVER_HOST_ADDRESS)
        val port = userConf.get(DRIVER_PORT)
        // 1.2.2节 YarnRMClient 调用 register 方法注册 ApplicationMaster
        registerAM(host, port, userConf, sc.ui.map(_.webUrl), appAttemptId)

        val driverRef = rpcEnv.setupEndpointRef(
          RpcAddress(host, port),
          YarnSchedulerBackend.ENDPOINT_NAME)
        // 1.2.3节 从 YARN ResourceManager 请求资源
        createAllocator(driverRef, userConf, rpcEnv, appAttemptId, distCacheConf)
      } else {
        // Sanity check; should never happen in normal operation, since sc should only be null
        // if the user app did not create a SparkContext.
        throw new IllegalStateException("User did not initialize spark context!")
      }
      // 前面通过 registerAM 方法注册am, createAllocator 方法申请到资源之后，接下来就开始执行应用程序剩余的逻辑部分
      //  notify driver 线程，也就是 userClassThread
      // 上面的申请资源和这里的执行应用程序是两个线程、两个部分
      resumeDriver()
      // 开始执行前面创建启动的 userClassThread  
      userClassThread.join()
    } catch {
      case e: SparkException if e.getCause().isInstanceOf[TimeoutException] =>
        logError(
          s"SparkContext did not initialize after waiting for $totalWaitTime ms. " +
           "Please check earlier log output for errors. Failing the application.")
        finish(FinalApplicationStatus.FAILED,
          ApplicationMaster.EXIT_SC_NOT_INITED,
          "Timed out waiting for SparkContext.")
    } finally {
      resumeDriver()
    }
  }
```

### 1.2.1 startUserApplication()

ApplicationMaster.scala

```scala
/**
   * Start the user class, which contains the spark driver, in a separate Thread.
   * If the main routine exits cleanly or exits with System.exit(N) for any N
   * we assume it was successful, for all other cases we assume failure.
   * 在一个单独的线程中，启动用户类，其中包含 spark driver
   * 
   * Returns the user thread that was started.
   * 返回启动的用户线程 
   */
  private def startUserApplication(): Thread = {
    logInfo("Starting the user application in a separate Thread")
    // userArgs 就是解析的 --arg，返回的列表
    var userArgs = args.userArgs
    if (args.primaryPyFile != null && args.primaryPyFile.endsWith(".py")) {
      // When running pyspark, the app is run using PythonRunner. The second argument is the list
      // of files to add to PYTHONPATH, which Client.scala already handles, so it's empty.
      userArgs = Seq(args.primaryPyFile, "") ++ userArgs
    }
    if (args.primaryRFile != null &&
        (args.primaryRFile.endsWith(".R") || args.primaryRFile.endsWith(".r"))) {
      // TODO(davies): add R dependencies here
    }
    // 这里的 args 就是前面创建的 用来解析像 --jar --class 这种参数的 ApplicationMasterArguments 对象
    // userClass 就是解析的 --class
    // 通过反射取到main方法  
    val mainMethod = userClassLoader.loadClass(args.userClass)
      .getMethod("main", classOf[Array[String]])
    
    // new了一个线程
    val userThread = new Thread {
      override def run(): Unit = {
        try {
          if (!Modifier.isStatic(mainMethod.getModifiers)) {
            logError(s"Could not find static main method in object ${args.userClass}")
            finish(FinalApplicationStatus.FAILED, ApplicationMaster.EXIT_EXCEPTION_USER_CLASS)
          } else {
            // 调用main方法  （初始化SparkContext） 
            mainMethod.invoke(null, userArgs.toArray)
            finish(FinalApplicationStatus.SUCCEEDED, ApplicationMaster.EXIT_SUCCESS)
            logDebug("Done running user class")
          }
        } 
        //....
      }
    }
    userThread.setContextClassLoader(userClassLoader)
    userThread.setName("Driver")
    // 启动线程 ，线程名称就是 Driver 线程
    userThread.start()
    userThread
  }
```

### 1.2.2 registerAM()

ApplicationMaster.scala

```scala
private def registerAM(
      host: String,
      port: Int,
      _sparkConf: SparkConf,
      uiAddress: Option[String],
      appAttempt: ApplicationAttemptId): Unit = {
    val appId = appAttempt.getApplicationId().toString()
    val attemptId = appAttempt.getAttemptId().toString()
    val historyAddress = ApplicationMaster
      .getHistoryServerAddress(_sparkConf, yarnConf, appId, attemptId)
    // client就是前面创建的YarnRMClient
    // 在 ResourceManager 中注册 ApplicationMaster
    client.register(host, port, yarnConf, _sparkConf, uiAddress, historyAddress)
    registered = true
  }
```

YarnRMClient.scala

```scala
  /**
   * Registers the application master with the RM.
   * 在 ResourceManager 中注册应用程序
   */
  def register(
      driverHost: String,
      driverPort: Int,
      conf: YarnConfiguration,
      sparkConf: SparkConf,
      uiAddress: Option[String],
      uiHistoryAddress: String): Unit = {
    // AMRMClient: am和rm之间的客户端，就可以理解成 am和rm之间的服务
    amClient = AMRMClient.createAMRMClient()
    amClient.init(conf)
    // 启动了am和rm之间的服务
    amClient.start()
    this.uiHistoryAddress = uiHistoryAddress

    val trackingUrl = uiAddress.getOrElse {
      if (sparkConf.get(ALLOW_HISTORY_SERVER_TRACKING_URL)) uiHistoryAddress else ""
    }

    logInfo("Registering the ApplicationMaster")
    synchronized {
      // 注册 ApplicationMaster
      amClient.registerApplicationMaster(driverHost, driverPort, trackingUrl)
      registered = true
    }
  }
```

AMRMClientImpl.java

```java
public class AMRMClientImpl<T extends ContainerRequest> extends AMRMClient<T> {
  @Override
  public RegisterApplicationMasterResponse registerApplicationMaster(
          String appHostName, int appHostPort, String appTrackingUrl)
          throws YarnException, IOException {
    return registerApplicationMaster(appHostName, appHostPort, appTrackingUrl,
            null);
  }

  @Override
  public RegisterApplicationMasterResponse registerApplicationMaster(
          String appHostName, int appHostPort, String appTrackingUrl,
          Map<Set<String>, PlacementConstraint> placementConstraintsMap)
          throws YarnException, IOException {
    this.appHostName = appHostName;
    this.appHostPort = appHostPort;
    this.appTrackingUrl = appTrackingUrl;
    if (placementConstraintsMap != null && !placementConstraintsMap.isEmpty()) {
      this.placementConstraints.putAll(placementConstraintsMap);
    }
    Preconditions.checkArgument(appHostName != null,
            "The host name should not be null");
    Preconditions.checkArgument(appHostPort >= -1, "Port number of the host"
            + " should be any integers larger than or equal to -1");
    // 这里
    return registerApplicationMaster();
  }

  private RegisterApplicationMasterResponse registerApplicationMaster()
          throws YarnException, IOException {
      // 先由ApplicationMaster 向 ResourceManager 发送注册请求
    RegisterApplicationMasterRequest request =
            RegisterApplicationMasterRequest.newInstance(this.appHostName,
                    this.appHostPort, this.appTrackingUrl);
    if (!this.placementConstraints.isEmpty()) {
      request.setPlacementConstraints(this.placementConstraints);
    }
    /*
      这里的rmClient： protected ApplicationMasterProtocol rmClient;
      
      ApplicationMasterProtocol: ApplicationMaster 实例和 ResourceManager 之间的协议，
           用于ApplicationMaster向ResourceManager注册/注销、请求和获取集群中的资源  
      
      RegisterApplicationMasterResponse: 注册完成后，ResourceManager发给ApplicationMaster 的响应
     */
    // 注册 ApplicationMaster
    RegisterApplicationMasterResponse response =
            rmClient.registerApplicationMaster(request);
    //....
    return response;
  }

}
```

### 1.2.3  createAllocator()

YarnRMClient.scala

```scala
  private def createAllocator(
      driverRef: RpcEndpointRef,
      _sparkConf: SparkConf,
      rpcEnv: RpcEnv,
      appAttemptId: ApplicationAttemptId,
      distCacheConf: SparkConf): Unit = {
      // ...
     // client就是前面创建的YarnRMClient
    // 1.2.3.1节   创建一个 YarnAllocator 对象，负责从 YARN ResourceManager 请求容器
    allocator = client.createAllocator(
      yarnConf,
      _sparkConf,
      appAttemptId,
      driverUrl,
      driverRef,
      securityMgr,
      localResources)
  
    // Initialize the AM endpoint *after* the allocator has been initialized. This ensures
    // that when the driver sends an initial executor request (e.g. after an AM restart),
    // the allocator is ready to service requests.
    rpcEnv.setupEndpoint("YarnAM", new AMEndpoint(rpcEnv, driverRef))
     // 1.2.3.2节  请求资源
    allocator.allocateResources()
      }
```

#### 1.2.3.1 client.createAllocator()

YarnRMClient.scala

```scala
def createAllocator(
      conf: YarnConfiguration,
      sparkConf: SparkConf,
      appAttemptId: ApplicationAttemptId,
      driverUrl: String,
      driverRef: RpcEndpointRef,
      securityMgr: SecurityManager,
      localResources: Map[String, LocalResource]): YarnAllocator = {
    require(registered, "Must register AM before creating allocator.")
    // YarnAllocator负责从YARN ResourceManager请求容器
    new YarnAllocator(driverUrl, driverRef, conf, sparkConf, amClient, appAttemptId, securityMgr,
      localResources, SparkRackResolver.get(conf))
  }
```

#### 1.2.3.2 allocator.allocateResources()

YarnAllocator.scala

```scala
  /**
   * Request resources such that, if YARN gives us all we ask for, we'll have a number of containers
   * equal to maxExecutors.
   * 请求资源，这样，如果YARN给了我们所有我们想要的，我们将有许多等于maxExecutors的容器。
  * 
   * Deal with any containers YARN has granted to us by possibly launching executors in them.
   *
   * This must be synchronized because variables read in this method are mutated by other methods.
   */
  def allocateResources(): Unit = synchronized {
    updateResourceRequests()

    val progressIndicator = 0.1f
    // Poll the ResourceManager. This doubles as a heartbeat if there are no pending container
    // requests.
    // amClient: AMRMClient[ContainerRequest]
    // 1.2.3.2.1节 分配容器（资源）,返回 allocateResponse 包含了新分配的容器
    val allocateResponse = amClient.allocate(progressIndicator)
    
    // 获得 ResourceManager 分配的容器的列表 
    val allocatedContainers = allocateResponse.getAllocatedContainers()
    allocatorNodeHealthTracker.setNumClusterNodes(allocateResponse.getNumClusterNodes)

    if (allocatedContainers.size > 0) {
      logDebug(("Allocated containers: %d. Current executor count: %d. " +
        "Launching executor count: %d. Cluster resources: %s.")
        .format(
          allocatedContainers.size,
          getNumExecutorsRunning,
          getNumExecutorsStarting,
          allocateResponse.getAvailableResources))

      handleAllocatedContainers(allocatedContainers.asScala.toSeq)
    }

    val completedContainers = allocateResponse.getCompletedContainersStatuses()
    if (completedContainers.size > 0) {
      logDebug("Completed %d containers".format(completedContainers.size))
      processCompletedContainers(completedContainers.asScala.toSeq)
      logDebug("Finished processing %d completed containers. Current running executor count: %d."
        .format(completedContainers.size, getNumExecutorsRunning))
    }
  }
```

##### 1.2.3.2.1 amClient.allocate()

AMRMClientImpl.java

```java
public class AMRMClientImpl<T extends ContainerRequest> extends AMRMClient<T> {
  public AllocateResponse allocate(float progressIndicator) throws YarnException, IOException {
      //...
    /*
        protected ApplicationMasterProtocol rmClient;  
        
        ApplicationMasterProtocol: ApplicationMaster 实例和 ResourceManager 之间的协议，
           用于ApplicationMaster向ResourceManager注册/注销、请求和获取集群中的资源   
        
        allocateResponse: 资源协商时 ResourceManager 向 ApplicationMaster 发出的响应。
           响应包含了新分配的容器。
     */
    allocateResponse = rmClient.allocate(allocateRequest);
    //...
  }
}
```