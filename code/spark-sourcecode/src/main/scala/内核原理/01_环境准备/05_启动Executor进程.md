

在 `04_启动Driver线程.md` 中，启动了 ApplicationMaster 进程，执行 ApplicationMaster 的 main 方法：

- 创建一个 ApplicationMaster 对象
  - 创建用来在 YARN ResourceManager 中处理注册和取消注册 ApplicationMaster 的 YarnRMClient 对象

- 运行创建的 ApplicationMaster
  - 启动一个单独的用户线程（也就是Driver线程），以执行用户类的 main 方法（初始化SparkContext）
  - 在 SparkContext 初始化完成后，原线程继续执行，YarnRMClient 对象调用 register 方法在 ResourceManager 中注册 ApplicationMaster
  - 由 YarnRMClient 对象创建一个 YarnAllocator 对象，用来从 YARN ResourceManager 请求资源
  - 得到资源以后，就唤醒 Driver 线程继续执行应用程序剩余的逻辑部分

接下来，从得到资源的地方往下看，就是 allocateResources() [YarnAllocator.scala] 这个方法的后半段：

YarnAllocator.scala

```scala
  /**
   * Request resources such that, if YARN gives us all we ask for, we'll have a number of containers
   * equal to maxExecutors.
   * 请求资源，这样，如果YARN给了我们所有我们想要的，我们将有许多等于maxExecutors的容器。
   */
  def allocateResources(): Unit = synchronized {
    updateResourceRequests()

    val progressIndicator = 0.1f
    // 分配容器（资源）,返回 allocateResponse 包含了新分配的容器
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
      // 【从这开始】先对rm给的容器按主机和机架进行分类（执行程序时首选位置的考虑），
      // 然后遍历每个容器，在容器里启动 executor 
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

YarnAllocator.scala

```scala
/**
   * Handle containers granted by the RM by launching executors on them.
   * 通过启动 executors 来处理 RM 授予的容器
   * 
   * Due to the way the YARN allocation protocol works, certain healthy race conditions can result
   * in YARN granting containers that we no longer need. In this case, we release them.
   *
   * Visible for testing.
   */
  def handleAllocatedContainers(allocatedContainers: Seq[Container]): Unit = {
    // 包含 rm 给的所有的容器的列表
    val containersToUse = new ArrayBuffer[Container](allocatedContainers.size)

    // 按主机分类容器
    // Match incoming requests by host
    val remainingAfterHostMatches = new ArrayBuffer[Container]
    for (allocatedContainer <- allocatedContainers) {
      matchContainerToRequest(allocatedContainer, allocatedContainer.getNodeId.getHost,
        containersToUse, remainingAfterHostMatches)
    }

    // 按机架分类容器，使用一个独立的线程执行这个操作
    // Match remaining by rack. Because YARN's RackResolver swallows thread interrupts
    // (see SPARK-27094), which can cause this code to miss interrupts from the AM, use
    // a separate thread to perform the operation.
    val remainingAfterRackMatches = new ArrayBuffer[Container]
    if (remainingAfterHostMatches.nonEmpty) {
      var exception: Option[Throwable] = None
      val thread = new Thread("spark-rack-resolver") {
        override def run(): Unit = {
          try {
            for (allocatedContainer <- remainingAfterHostMatches) {
              val rack = resolver.resolve(allocatedContainer.getNodeId.getHost)
              matchContainerToRequest(allocatedContainer, rack, containersToUse,
                remainingAfterRackMatches)
            }
          } catch {
            case e: Throwable =>
              exception = Some(e)
          }
        }
      }
      thread.setDaemon(true)
      thread.start()

      try {
        thread.join()
      } catch {
        case e: InterruptedException =>
          thread.interrupt()
          throw e
      }

      if (exception.isDefined) {
        throw exception.get
      }
    }
    
    // 分配既不是本地节点，也不是本地机架的剩余的容器
    // Assign remaining that are neither node-local nor rack-local
    val remainingAfterOffRackMatches = new ArrayBuffer[Container]
    for (allocatedContainer <- remainingAfterRackMatches) {
      matchContainerToRequest(allocatedContainer, ANY_HOST, containersToUse,
        remainingAfterOffRackMatches)
    }

    // 分配完之后，还有剩余的容器，那么就是不需要的容器，就释放掉
    if (remainingAfterOffRackMatches.nonEmpty) {
      logDebug(s"Releasing ${remainingAfterOffRackMatches.size} unneeded containers that were " +
        s"allocated to us")
      for (container <- remainingAfterOffRackMatches) {
        internalReleaseContainer(container)
      }
    }
    // 在分配的容器里启动 executors
    runAllocatedContainers(containersToUse)

    logInfo("Received %d containers from YARN, launching executors on %d of them."
      .format(allocatedContainers.size, containersToUse.size))
  }
```

YarnAllocator.scala

```scala
  /**
   * Launches executors in the allocated containers.
   * 在分配的容器里启动 executors 
   */
  private def runAllocatedContainers(containersToUse: ArrayBuffer[Container]): Unit = synchronized {
    // 遍历每个容器
    for (container <- containersToUse) {
      val rpId = getResourceProfileIdFromPriority(container.getPriority)
      executorIdCounter += 1
      val executorHostname = container.getNodeId.getHost
      val containerId = container.getId
      val executorId = executorIdCounter.toString
      val yarnResourceForRpId = rpIdToYarnResource.get(rpId)
      assert(container.getResource.getMemory >= yarnResourceForRpId.getMemory)
      logInfo(s"Launching container $containerId on host $executorHostname " +
        s"for executor with ID $executorId for ResourceProfile Id $rpId")

      def updateInternalState(): Unit = synchronized {
        getOrUpdateRunningExecutorForRPId(rpId).add(executorId)
        getOrUpdateNumExecutorsStartingForRPId(rpId).decrementAndGet()
        executorIdToContainer(executorId) = container
        containerIdToExecutorIdAndResourceProfileId(container.getId) = (executorId, rpId)

        val localallocatedHostToContainersMap = getOrUpdateAllocatedHostToContainersMapForRPId(rpId)
        val containerSet = localallocatedHostToContainersMap.getOrElseUpdate(executorHostname,
          new HashSet[ContainerId])
        containerSet += containerId
        allocatedContainerToHostMap.put(containerId, executorHostname)
      }

      val rp = rpIdToResourceProfile(rpId)
      val defaultResources = ResourceProfile.getDefaultProfileExecutorResources(sparkConf)
      val containerMem = rp.executorResources.get(ResourceProfile.MEMORY).
        map(_.amount).getOrElse(defaultResources.executorMemoryMiB).toInt
      val containerCores = rp.getExecutorCores.getOrElse(defaultResources.cores)
      val rpRunningExecs = getOrUpdateRunningExecutorForRPId(rpId).size
      // 判断正在运行的 Executor 数量和所需的Executors 数量，小于的话，就启动新的 Executor
      if (rpRunningExecs < getOrUpdateTargetNumExecutorsForRPId(rpId)) {
        getOrUpdateNumExecutorsStartingForRPId(rpId).incrementAndGet()
        if (launchContainers) {
          // 这里
          // 线程池在 YarnAllocator 中创建，而这个分配器是在 ApplicationMaster 中的 runDriver 方法创建的，所有这个线程池在 ApplicationMaster 中
          launcherPool.execute(() => {
            try {
              // 在 am 的一个线程池里，创建一个 ExecutorRunnable 对象，
              // 在这个对象运行时，会创建并启动一个 NMClient 服务（此时就找到一个nodemanager来启动容器），然后就开始启动容器（启动Executor）
              new ExecutorRunnable(
                Some(container),
                conf,
                sparkConf,
                driverUrl,
                executorId,
                executorHostname,
                containerMem,
                containerCores,
                appAttemptId.getApplicationId.toString,
                securityMgr,
                localResources,
                rp.id
              ).run() // 这里
              updateInternalState()
            } catch {
              case e: Throwable =>
                getOrUpdateNumExecutorsStartingForRPId(rpId).decrementAndGet()
                if (NonFatal(e)) {
                  logError(s"Failed to launch executor $executorId on container $containerId", e)
                  // Assigned container should be released immediately
                  // to avoid unnecessary resource occupation.
                  amClient.releaseAssignedContainer(containerId)
                } else {
                  throw e
                }
            }
          })
        } else {
          // For test only
          updateInternalState()
        }
      } else {
        logInfo(("Skip launching executorRunnable as running executors count: %d " +
          "reached target executors count: %d.").format(rpRunningExecs,
          getOrUpdateTargetNumExecutorsForRPId(rpId)))
      }
    }
  }
```

ExecutorRunnable.scala

```scala
private[yarn] class ExecutorRunnable(
    container: Option[Container],
    conf: YarnConfiguration,
    sparkConf: SparkConf,
    masterAddress: String,
    executorId: String,
    hostname: String,
    executorMemory: Int,
    executorCores: Int,
    appId: String,
    securityMgr: SecurityManager,
    localResources: Map[String, LocalResource],
    resourceProfileId: Int) extends Logging {

  var rpc: YarnRPC = YarnRPC.create(conf)
  /*
  * NMClient 也是一个服务，其实现类是 NMClientImpl ，在默认情况下，它在停止时，会停止由它启动的所有正在运行的容器
  * 它有两个方法 startContainer stopContainer 启动和停止容器
  * */ 
  var nmClient: NMClient = _

  def run(): Unit = {
    logDebug("Starting Executor Container")
    nmClient = NMClient.createNMClient()
    nmClient.init(conf)
    // 启动服务后，开始启动容器
    nmClient.start()
    // 先准备好一个启动 YarnCoarseGrainedExecutorBackend 的命令，也就是启动 Executor 进程的命令，然后再执行启动
    startContainer()
  }
}
```

```scala
  def startContainer(): java.util.Map[String, ByteBuffer] = {
    // ContainerLaunchContext 表示 NodeManager 启动容器的所有信息
    val ctx = Records.newRecord(classOf[ContainerLaunchContext])
      .asInstanceOf[ContainerLaunchContext]
    val env = prepareEnvironment().asJava

    ctx.setLocalResources(localResources.asJava)
    ctx.setEnvironment(env)

    val credentials = UserGroupInformation.getCurrentUser().getCredentials()
    val dob = new DataOutputBuffer()
    credentials.writeTokenStorageToStream(dob)
    ctx.setTokens(ByteBuffer.wrap(dob.getData()))

    // 准备好一个启动 YarnCoarseGrainedExecutorBackend 的命令，也就是启动 Executor 进程
    val commands = prepareCommand()

    ctx.setCommands(commands.asJava)
    ctx.setApplicationACLs(
      YarnSparkHadoopUtil.getApplicationAclsForYarn(securityMgr).asJava)

    // If external shuffle service is enabled, register with the Yarn shuffle service already
    // started on the NodeManager and, if authentication is enabled, provide it with our secret
    // key for fetching shuffle files later
    if (sparkConf.get(SHUFFLE_SERVICE_ENABLED)) {
      val secretString = securityMgr.getSecretKey()
      val secretBytes =
        if (secretString != null) {
          // This conversion must match how the YarnShuffleService decodes our secret
          JavaUtils.stringToBytes(secretString)
        } else {
          // Authentication is not enabled, so just provide dummy metadata
          ByteBuffer.allocate(0)
        }
      val serviceName = sparkConf.get(SHUFFLE_SERVICE_NAME)
      logInfo(s"Initializing service data for shuffle service using name '$serviceName'")
      ctx.setServiceData(Collections.singletonMap(serviceName, secretBytes))
    }

    // Send the start request to the ContainerManager
    try {
      // 启动
      nmClient.startContainer(container.get, ctx)
    } catch {
      case ex: Exception =>
        throw new SparkException(s"Exception while starting container ${container.get.getId}" +
          s" on host $hostname", ex)
    }
  }
```

ExecutorRunnable.scala

```scala
private def prepareCommand(): List[String] = {
  // JVM配置....

  // For log4j configuration to reference
  javaOpts += ("-Dspark.yarn.app.container.log.dir=" + ApplicationConstants.LOG_DIR_EXPANSION_VAR)

  YarnSparkHadoopUtil.addOutOfMemoryErrorArgument(javaOpts)
  val commands = prefixEnv ++
          Seq(Environment.JAVA_HOME.$$() + "/bin/java", "-server") ++
          javaOpts ++
          Seq("org.apache.spark.executor.YarnCoarseGrainedExecutorBackend",
            "--driver-url", masterAddress,
            "--executor-id", executorId,
            "--hostname", hostname,
            "--cores", executorCores.toString,
            "--app-id", appId,
            "--resourceProfileId", resourceProfileId.toString) ++
          Seq(
            s"1>${ApplicationConstants.LOG_DIR_EXPANSION_VAR}/stdout",
            s"2>${ApplicationConstants.LOG_DIR_EXPANSION_VAR}/stderr")

  // TODO: it would be nicer to just make sure there are no null commands here
  commands.map(s => if (s == null) "null" else s).toList
}
```

