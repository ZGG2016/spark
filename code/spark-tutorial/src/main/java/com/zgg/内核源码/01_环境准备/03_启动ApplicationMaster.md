
在 `02_向yarn提交应用.md` 中，会创建一个 SparkSubmit 对象，并调用 doSubmit 方法，在这个方法中，会依次执行：

- 为提交应用程序准备环境，取到 childMainClass=YarnClusterApplication 字符串
- 获得 childMainClass 字符串相关联的 Class 对象
- 再根据这个 Class 对象，创建一个 YarnClusterApplication
- 最后调用 `app.start` 启动应用程序 

接下来，查看 YarnClusterApplication 的 start 方法

Client.scala

```scala
private[spark] class YarnClusterApplication extends SparkApplication {

  override def start(args: Array[String], conf: SparkConf): Unit = {
    // SparkSubmit would use yarn cache to distribute files & jars in yarn mode,
    // so remove them from sparkConf here for yarn mode.
    conf.remove(JARS)
    conf.remove(FILES)
    conf.remove(ARCHIVES)
    // ClientArguments 对象可以解析 --class --jar 等参数
    // 1.1节 Client() 内部会创建一个 yarnClient  在启动YarnClient服务时，会创建 resourcemanager Client
    // 1.2节 run() 将应用程序提交给 ResourceManager
    new Client(new ClientArguments(args), conf, null).run()
  }
}
```

## 1.1 Client()

Client.scala

```scala
private[spark] class Client(
    val args: ClientArguments,
    val sparkConf: SparkConf,
    val rpcEnv: RpcEnv)
  extends Logging {
  // 在spark端创建一个 yarnClient  在启动YarnClient服务时，会创建 resourcemanager Client
  private val yarnClient = YarnClient.createYarnClient
  }
```

YarnClient.java

```java
public abstract class YarnClient extends AbstractService {
    /**
     * Create a new instance of YarnClient.
     */
    @Public
    public static YarnClient createYarnClient() {
        // 在启动YarnClient服务时，会创建 resourcemanager Client
        YarnClient client = new YarnClientImpl();
        return client;
    }
}
```

YarnClientImpl.java

```java
public class YarnClientImpl extends YarnClient {
    // 这个接口是客户端和ResourceManager间提交或终止job的协议
    protected ApplicationClientProtocol rmClient;
    @Override
    protected void serviceStart() throws Exception {
        try {
            rmClient = ClientRMProxy.createRMProxy(getConfig(),
                    ApplicationClientProtocol.class);
            //...
        } catch (IOException e) {
            throw new YarnRuntimeException(e);
        }
        //...
        super.serviceStart();
    }
}
```

## 1.2 run()

Client.scala

```scala
  // 将应用程序提交给 ResourceManager
  def run(): Unit = {
    submitApplication()
  //...
  }
```

```scala
/**
   * Submit an application running our ApplicationMaster to the ResourceManager.
   *
   * The stable Yarn API provides a convenience method (YarnClient#createApplication) for
   * creating applications and setting up the application submission context. This was not
   * available in the alpha API.
   */
  def submitApplication(): Unit = {
    ResourceRequestHelper.validateResources(sparkConf)

    try {
      // 1.2.1节  建立 connection between a launcher server and client. (yarn 和客户端间的连接)
      launcherBackend.connect()
      yarnClient.init(hadoopConf)
      // 会执行 YarnClientImpl 中的 serviceStart 方法，来启动 yarnClient，同时会创建 resourcemanager Client
      // rmClient是 ApplicationClientProtocol 接口的实现类对象，这个接口是客户端和ResourceManager间提交或终止job的协议
      yarnClient.start()
      //....
      
      // Get a new application from our RM
      // 1.2.2节 在 YarnClient 创建了一个 Application （通过向rm发创建请求，rm返回创建的响应，YarnClient再创建）
      val newApp = yarnClient.createApplication()
      val newAppResponse = newApp.getNewApplicationResponse()
      // rm返回创建的响应里包含有这个应用程序的id
      this.appId = newAppResponse.getApplicationId()

      // The app staging dir based on the STAGING_DIR configuration if configured
      // otherwise based on the users home directory.
      // scalastyle:off FileSystemGet
      val appStagingBaseDir = sparkConf.get(STAGING_DIR)
        .map { new Path(_, UserGroupInformation.getCurrentUser.getShortUserName) }
        .getOrElse(FileSystem.get(hadoopConf).getHomeDirectory())
      stagingDirPath = new Path(appStagingBaseDir, getAppStagingDir(appId))
      // scalastyle:on FileSystemGet

      new CallerContext("CLIENT", sparkConf.get(APP_CALLER_CONTEXT),
        Option(appId.toString)).setCurrentContext()

      // Verify whether the cluster has enough resources for our AM
      verifyClusterResources(newAppResponse)
      
      // Set up the appropriate contexts to launch our AM
      /*
        为启动ApplicationMaster设置上下文环境
       */

      /*
        1.2.3节 
        为启动 ApplicationMaster container 设置 ContainerLaunchContext（就是返回一个Container）
        这会设置一些启动环境、java jvm选项和启动ApplicationMaster的命令 （拼接成一个启动am的命令）
        
        ContainerLaunchContext 包含了 NodeManager 启动一个 container 所需的所有信息 
      */
      val containerContext = createContainerLaunchContext()
      // 创建ApplicationSubmissionContext，它包含了ResourceManager启动ApplicationMaster所需的所有信息（应用程序信息、Container）
      val appContext = createApplicationSubmissionContext(newApp, containerContext)

      // Finally, submit and monitor the application
      logInfo(s"Submitting application $appId to ResourceManager")
      // 1.2.4节 将一个新的应用程序提交给yarn.
      // （注意：参数是一个ApplicationSubmissionContext，而它包含了containerContext，当rm接收到客户端发来的这些信息（含启动am的命令）时，就分配一个nm执行这个命令）   
      // 阻塞调用。直到提交的应用程序被成功提交，且被ResourceManager接收，才会返回 ApplicationId
      yarnClient.submitApplication(appContext)
      launcherBackend.setAppId(appId.toString)
      reportLauncherState(SparkAppHandle.State.SUBMITTED)
    } catch {
      //....
    }
  }
```

### 1.2.1 launcherBackend.connect()

```scala
/**
 * 一个用来和启动服务通信的类
 * A class that can be used to talk to a launcher server. Users should extend this class to
 * provide implementation for the abstract methods.
 *
 * See `LauncherServer` for an explanation of how launcher communication works.
 */
private[spark] abstract class LauncherBackend {
  def connect(): Unit = {
    val port = conf.getOption(LauncherProtocol.CONF_LAUNCHER_PORT)
      .orElse(sys.env.get(LauncherProtocol.ENV_LAUNCHER_PORT))
      .map(_.toInt)
    val secret = conf.getOption(LauncherProtocol.CONF_LAUNCHER_SECRET)
      .orElse(sys.env.get(LauncherProtocol.ENV_LAUNCHER_SECRET))
    if (port.isDefined && secret.isDefined) {
      // 地址 端口
      val s = new Socket(InetAddress.getLoopbackAddress(), port.get)
      // 启动服务和客户端间的连接，负责之间的通信（发送和接收消息）
      connection = new BackendConnection(s)
      connection.send(new Hello(secret.get, SPARK_VERSION))
      clientThread = LauncherBackend.threadFactory.newThread(connection)
      // 为这个连接创建、启动一个线程
      clientThread.start()
      _isConnected = true
    }
  }
}
```

### 1.2.2 createApplication()

```java
public class YarnClientImpl extends YarnClient {
    @Override
    public YarnClientApplication createApplication()
            throws YarnException, IOException {
        // ApplicationSubmissionContext:
        // 包含了ResourceManager启动ApplicationMaster所需的所有信息，
        // 比如 ApplicationId、优先级、执行ApplicationMaster所在的容器的上下文ContainerLaunchContext
        ApplicationSubmissionContext context = Records.newRecord(ApplicationSubmissionContext.class);
        // rm接收一个获取新的Application的请求，rm返回给客户端一个创建的响应
        GetNewApplicationResponse newApp = getNewApplication();
        // 获取ResourceManager分配的一个新的 ApplicationId
        ApplicationId appId = newApp.getApplicationId();
        context.setApplicationId(appId);
        // 在 YarnClient 创建了一个 Application
        return new YarnClientApplication(newApp, context);
    }
}
```

### 1.2.3 createContainerLaunchContext()

```scala
/**
   * Set up a ContainerLaunchContext to launch our ApplicationMaster container.
   * This sets up the launch environment, java options, and the command for launching the AM.
   */
  private def createContainerLaunchContext(): ContainerLaunchContext = {
    logInfo("Setting up container launch context for our AM")
    // ...
    // 创建一个 Container 
    val amContainer = Records.newRecord(classOf[ContainerLaunchContext])
    amContainer.setLocalResources(localResources.asJava)
    amContainer.setEnvironment(launchEnv.asJava)

    val javaOpts = ListBuffer[String]()
    // java jvm 配置...

    val userClass =
      if (isClusterMode) {
        Seq("--class", YarnSparkHadoopUtil.escapeForShell(args.userClass))
      } else {
        Nil
      }

    val amClass =
      if (isClusterMode) {
        Utils.classForName("org.apache.spark.deploy.yarn.ApplicationMaster").getName
      } else {
        Utils.classForName("org.apache.spark.deploy.yarn.ExecutorLauncher").getName
      }

    val amArgs =
      Seq(amClass) ++ userClass ++ userJar ++ primaryPyFile ++ primaryRFile ++ userArgs ++
        Seq("--properties-file",
          buildPath(Environment.PWD.$$(), LOCALIZED_CONF_DIR, SPARK_CONF_FILE)) ++
        Seq("--dist-cache-conf",
          buildPath(Environment.PWD.$$(), LOCALIZED_CONF_DIR, DIST_CACHE_CONF_FILE))
  
    /*
      拼接成一个启动am的命令
      大概是：
          /bin/java (jvm配置项) org.apache.spark.deploy.yarn.ApplicationMaster (应用程序主类) ....
     */
    // Command for the ApplicationMaster
    val commands = prefixEnv ++
      Seq(Environment.JAVA_HOME.$$() + "/bin/java", "-server") ++
      javaOpts ++ amArgs ++
      Seq(
        "1>", ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stdout",
        "2>", ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stderr")
    // TODO: it would be nicer to just make sure there are no null commands here
    val printableCommands = commands.map(s => if (s == null) "null" else s).toList
    amContainer.setCommands(printableCommands.asJava)
    //...
    // 返回一个 包含启动am命令的Container
    amContainer
  }
```

### 1.2.4 yarnClient.submitApplication()

```java
public class YarnClientImpl extends YarnClient {
  @Override
  public ApplicationId submitApplication(ApplicationSubmissionContext appContext) throws YarnException, IOException {
    ApplicationId applicationId = appContext.getApplicationId();

    // 客户端发送的请求，为将应用程序提交给 ResourceManager，
      // 并为其设置了 ApplicationSubmissionContext（它包含了ResourceManager启动ApplicationMaster所需的所有信息）
    SubmitApplicationRequest request =
      Records.newRecord(SubmitApplicationRequest.class);
    request.setApplicationSubmissionContext(appContext);

    // Automatically add the timeline DT into the CLC
    // Only when the security and the timeline service are both enabled
    if (isSecurityEnabled() && timelineV1ServiceEnabled) {
      addTimelineDelegationToken(appContext.getAMContainerSpec());
    }

    //TODO: YARN-1763:Handle RM failovers during the submitApplication call.
      // 提交给 ResourceManager
      // rmClient是 ApplicationClientProtocol 接口的实现类对象 
      // 这个接口是客户端和ResourceManager间提交或终止job的协议  
    rmClient.submitApplication(request);
    //....

    return applicationId;
  }
  
}
```