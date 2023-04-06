

在 `spark-submit` 脚本后，接下来使用传入的参数，执行 `org.apache.spark.deploy.SparkSubmit` 下的 `main` 方法

```scala
object SparkSubmit extends CommandLineUtils with Logging {
  override def main(args: Array[String]): Unit = {
    val submit = new SparkSubmit() {
      self =>

      override protected def parseArguments(args: Array[String]): SparkSubmitArguments = {
        new SparkSubmitArguments(args) {
          override protected def logInfo(msg: => String): Unit = self.logInfo(msg)

          override protected def logWarning(msg: => String): Unit = self.logWarning(msg)

          override protected def logError(msg: => String): Unit = self.logError(msg)
        }
      }
      //......

      override def doSubmit(args: Array[String]): Unit = {
        try {
          super.doSubmit(args)
        } catch {
          case e: SparkUserAppException =>
            exitFn(e.exitCode)
        }
      }

    }
    // 这里
    submit.doSubmit(args)
  }
}
```

上面 SparkSubmit 是下面 SparkSubmit 类的伴生对象

```scala
/**
 * Main gateway of launching a Spark application.
 *
 * This program handles setting up the classpath with relevant Spark dependencies and provides
 * a layer over the different cluster managers and deploy modes that Spark supports.
 */
private[spark] class SparkSubmit extends Logging {
  // 这里
  def doSubmit(args: Array[String]): Unit = {
    //...

    // 1.1 节  new了一个 SparkSubmitArguments 对象，
    val appArgs = parseArguments(args)
    if (appArgs.verbose) {
      logInfo(appArgs.toString)
    }
    /*
        // Action should be SUBMIT unless otherwise specified
        action = Option(action).getOrElse(SUBMIT)
     */
    appArgs.action match {
      // 1.2 节
      case SparkSubmitAction.SUBMIT => submit(appArgs, uninitLog)
      case SparkSubmitAction.KILL => kill(appArgs)
      case SparkSubmitAction.REQUEST_STATUS => requestStatus(appArgs)
      case SparkSubmitAction.PRINT_VERSION => printVersion()
    }
  }

}
```

## 1.1 parseArguments

SparkSubmit.scala

```scala
  protected def parseArguments(args: Array[String]): SparkSubmitArguments = {
    new SparkSubmitArguments(args)
  }
```

SparkSubmitArguments.scala

```scala
/**
 * Parses and encapsulates arguments from the spark-submit script.
 * The env argument is used for testing.
 */
private[deploy] class SparkSubmitArguments(args: Seq[String], env: Map[String, String] = sys.env)
  extends SparkSubmitArgumentsParser with Logging {
  var master: String = null
  var deployMode: String = null
  var executorMemory: String = null
  var executorCores: String = null
  var action: SparkSubmitAction = null
  // 一些执行参数...

  // Set parameters from command line arguments 
  // 解析一个 spark-submit 命令行选项的列表
  parse(args.asJava)
}
```

SparkSubmitOptionParser.java

```java
/**
 * Parser for spark-submit command line options.
 * <p>
 * This class encapsulates the parsing code for spark-submit command line options, so that there
 * is a single list of options that needs to be maintained (well, sort of, but it makes it harder
 * to break things).
 */
class SparkSubmitOptionParser {

    /**
     * 解析一个 spark-submit 命令行选项的列表   [参数将赋给SparkSubmitArguments类中的属性，见 handler 方法]
     * Parse a list of spark-submit command line options.
     * <p>
     * See SparkSubmitArguments.scala for a more formal description of available options.
     *
     * @throws IllegalArgumentException If an error is found during parsing.
     */
    protected final void parse(List<String> args) {
        Pattern eqSeparatedOpt = Pattern.compile("(--[^=]+)=(.+)");

        int idx = 0;
        for (idx = 0; idx < args.size(); idx++) {
            String arg = args.get(idx);
            String value = null;
            
            // 使用正则匹配出 参数及其值
            Matcher m = eqSeparatedOpt.matcher(arg);
            if (m.matches()) {
                arg = m.group(1);
                value = m.group(2);
            }

            // Look for options with a value.
            // opts: 这是spark-submit选项的规范列表，比如 DEPLOY_MODE DRIVER_MEMORY EXECUTOR_CORES
            // 匹配出 arg 对应的 参数名，比如 DEPLOY_MODE DRIVER_MEMORY EXECUTOR_CORES
            String name = findCliOption(arg, opts);
            if (name != null) {
                // 有参数，但没有为它指定值
                if (value == null) {
                    if (idx == args.size() - 1) {
                        throw new IllegalArgumentException(
                                String.format("Missing argument for option '%s'.", arg));
                    }
                    // 当前的参数没有值，就往下遍历
                    idx++;
                    value = args.get(idx);
                }
                // handle: Return Whether to continue parsing the argument list.
                // 遇到 PRINT_VERSION action 就返回 false
                if (!handle(name, value)) {
                    break;
                }
                continue;
            }

            // Look for a switch.
            // 处理 比如 HELP  VERSION 参数
            // switches: List of switches (command line options that do not take parameters) recognized by spark-submit. 
            name = findCliOption(arg, switches);
            if (name != null) {
                if (!handle(name, null)) {
                    break;
                }
                continue;
            }

            if (!handleUnknown(arg)) {
                break;
            }
        }
        // 上面没有遍历完成
        if (idx < args.size()) {
            idx++;
        }
        handleExtraArgs(args.subList(idx, args.size()));
    }
}
```

SparkSubmitArguments.scala

```scala
  /** Fill in values by parsing user options. */
  override protected def handle(opt: String, value: String): Boolean = {
    opt match {
      case NAME =>
        name = value

      case MASTER =>
        master = value

      case CLASS =>
        mainClass = value

      case DEPLOY_MODE =>
        if (value != "client" && value != "cluster") {
          error("--deploy-mode must be either \"client\" or \"cluster\"")
        }
        deployMode = value

      case NUM_EXECUTORS =>
        numExecutors = value

      case TOTAL_EXECUTOR_CORES =>
        totalExecutorCores = value

      case EXECUTOR_CORES =>
        executorCores = value
      // ....
      case _ =>
        error(s"Unexpected argument '$opt'.")
    }
    // 遇到 PRINT_VERSION action 就返回 false
    action != SparkSubmitAction.PRINT_VERSION
  }

  var childArgs: ArrayBuffer[String] = new ArrayBuffer[String]()
  override protected def handleExtraArgs(extra: JList[String]): Unit = {
    childArgs ++= extra.asScala
  }
```

## 1.2 submit

SparkSubmit.scala

```scala
 /**
   * Submit the application using the provided parameters, ensuring to first wrap
   * in a doAs when --proxy-user is specified.
   */
  @tailrec
  private def submit(args: SparkSubmitArguments, uninitLog: Boolean): Unit = {

    def doRunMain(): Unit = {
      // 判断参数里有没有传一个代理用户，也就是服务器用户
      if (args.proxyUser != null) {
        val proxyUser = UserGroupInformation.createProxyUser(args.proxyUser,
          UserGroupInformation.getCurrentUser())
        try {
          proxyUser.doAs(new PrivilegedExceptionAction[Unit]() {
            override def run(): Unit = {
              runMain(args, uninitLog)
            }
          })
        } catch {
          case e: Exception =>
            // Hadoop's AuthorizationException suppresses the exception's stack trace, which
            // makes the message printed to the output by the JVM not very helpful. Instead,
            // detect exceptions with empty stack traces here, and treat them differently.
            if (e.getStackTrace().length == 0) {
              error(s"ERROR: ${e.getClass().getName()}: ${e.getMessage()}")
            } else {
              throw e
            }
        }
      } else {
        // 这里
        runMain(args, uninitLog)
      }
    }

    // In standalone cluster mode, there are two submission gateways:
    //   (1) The traditional RPC gateway using o.a.s.deploy.Client as a wrapper
    //   (2) The new REST-based gateway introduced in Spark 1.3
    // The latter is the default behavior as of Spark 1.3, but Spark submit will fail over
    // to use the legacy gateway if the master endpoint turns out to be not a REST server.
    if (args.isStandaloneCluster && args.useRest) {
      try {
        logInfo("Running Spark using the REST application submission protocol.")
        doRunMain()
      } catch {
        // Fail over to use the legacy submission gateway
        case e: SubmitRestConnectionException =>
          logWarning(s"Master endpoint ${args.master} was not a REST server. " +
            "Falling back to legacy submission gateway instead.")
          args.useRest = false
          submit(args, false)
      }
      // 除了 Standalone 模式以外的其他模式
    // In all other modes, just run the main class as prepared
    } else {
      doRunMain()
    }
  }
```
```scala
/**
   * Run the main method of the child class using the submit arguments.
   *
   * This runs in two steps. First, we prepare the launch environment by setting up
   * the appropriate classpath, system properties, and application arguments for
   * running the child main class based on the cluster manager and the deploy mode.
   * Second, we use this launch environment to invoke the main method of the child
   * main class.
   * 第一，准备启动环境；第二，调用main方法 
   *
   * Note that this main class will not be the one provided by the user if we're
   * running cluster deploy mode or python applications.
   */
  private def runMain(args: SparkSubmitArguments, uninitLog: Boolean): Unit = {
    // 为提交应用程序准备环境  1.2.1节
    // childMainClass 就是 "org.apache.spark.deploy.yarn.YarnClusterApplication"
    val (childArgs, childClasspath, sparkConf, childMainClass) = prepareSubmitEnvironment(args)
    // Let the main class re-initialize the logging system once it starts.
    if (uninitLog) {
      Logging.uninitialize()
    }

    if (args.verbose) {
      logInfo(s"Main class:\n$childMainClass")
      logInfo(s"Arguments:\n${childArgs.mkString("\n")}")
      // sysProps may contain sensitive information, so redact before printing
      logInfo(s"Spark config:\n${Utils.redact(sparkConf.getAll.toMap).sorted.mkString("\n")}")
      logInfo(s"Classpath elements:\n${childClasspath.mkString("\n")}")
      logInfo("\n")
    }
    // 获得类加载器
    val loader = getSubmitClassLoader(sparkConf)
    for (jar <- childClasspath) {
      addJarToClasspath(jar, loader)
    }

    var mainClass: Class[_] = null

    try {
      // 获得 childMainClass 字符串相关联的 Class 对象
      // Returns the Class object associated with the class or interface with the given string name, using the given class loader.
      mainClass = Utils.classForName(childMainClass)
    } catch {
      case e: ClassNotFoundException =>
        logError(s"Failed to load class $childMainClass.")
        if (childMainClass.contains("thriftserver")) {
          logInfo(s"Failed to load main class $childMainClass.")
          logInfo("You need to build Spark with -Phive and -Phive-thriftserver.")
        }
        throw new SparkUserAppException(CLASS_NOT_FOUND_EXIT_STATUS)
      case e: NoClassDefFoundError =>
        logError(s"Failed to load $childMainClass: ${e.getMessage()}")
        if (e.getMessage.contains("org/apache/hadoop/hive")) {
          logInfo(s"Failed to load hive class.")
          logInfo("You need to build Spark with -Phive and -Phive-thriftserver.")
        }
        throw new SparkUserAppException(CLASS_NOT_FOUND_EXIT_STATUS)
    }

    // 如果 mainClass 继承至 SparkApplication，就通过反射创建 SparkApplication 类对象
    // app 就是 YarnClusterApplication 对象
    val app: SparkApplication = if (classOf[SparkApplication].isAssignableFrom(mainClass)) {
      mainClass.getConstructor().newInstance().asInstanceOf[SparkApplication]
    } else {
      new JavaMainApplication(mainClass)
    }
    // ....
    try {
      // 启动应用程序  
      app.start(childArgs.toArray, sparkConf)
    } catch {
      case t: Throwable =>
        throw findCause(t)
    } finally {
      if (args.master.startsWith("k8s") && !isShell(args.primaryResource) &&
          !isSqlShell(args.mainClass) && !isThriftServer(args.mainClass)) {
        try {
          SparkContext.getActive.foreach(_.stop())
        } catch {
          case e: Throwable => logError(s"Failed to close SparkContext: $e")
        }
      }
    }
  }
```

### 1.2.1 prepareSubmitEnvironment

```scala
private[deploy] def prepareSubmitEnvironment(
      args: SparkSubmitArguments,
      conf: Option[HadoopConfiguration] = None)
      : (Seq[String], Seq[String], SparkConf, String) = {
  // Return values
  val childArgs = new ArrayBuffer[String]()
  val childClasspath = new ArrayBuffer[String]()
  val sparkConf = args.toSparkConf()
  var childMainClass = ""
  //...
  // In yarn-cluster mode, use yarn.Client as a wrapper around the user class
  if (isYarnCluster) {
    // YARN_CLUSTER_SUBMIT_CLASS = "org.apache.spark.deploy.yarn.YarnClusterApplication"
    childMainClass = YARN_CLUSTER_SUBMIT_CLASS
  }
  //...
}
```