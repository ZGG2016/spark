
我们使用如下命令执行一个应用程序

```shell
bin/spark-submit \
--class org.apache.spark.examples.SparkPi \
--master local[2] \
./examples/jars/spark-examples_2.12-3.0.0.jar \
10
```

在 `spark-submit` 脚本中

```shell
exec "${SPARK_HOME}"/bin/spark-class org.apache.spark.deploy.SparkSubmit "$@"

# "$@" 表示传递给脚本或函数的所有参数
```

在 `bin/spark-class` 中，

```shell
...
if [ -n "${JAVA_HOME}" ]; then
  RUNNER="${JAVA_HOME}/bin/java"
...
# The launcher library will print arguments separated by a NULL character, to allow arguments with
# characters that would be otherwise interpreted by the shell. Read that in a while loop, populating
# an array that will be used to exec the final command.
# 启动程序库将打印以NULL字符分隔的参数，以允许参数带有shell可能解释的字符。
# 在while循环中读取它，填充一个用于执行最终命令的数组。   【CMD+=("$ARG")】

# The exit code of the launcher is appended to the output, so the parent shell removes it from the
# command array and checks the value to see if the launcher succeeded.
# 启动程序的退出代码被附加到输出中，因此父shell从命令数组中删除它，并检查该值以查看启动程序是否成功。 
# 【 LAST=$((COUNT - 1))  CMD=("${CMD[@]:0:$LAST}")】
build_command() {
  "$RUNNER" -Xmx128m $SPARK_LAUNCHER_OPTS -cp "$LAUNCH_CLASSPATH" org.apache.spark.launcher.Main "$@"
  printf "%d\0" $?
}
...
CMD=()
DELIM=$'\n'
CMD_START_FLAG="false"
# 这个循环就是 遍历 `build_command` 的结果，拼接成最终执行的命令CMD
while IFS= read -d "$DELIM" -r ARG; do
  if [ "$CMD_START_FLAG" == "true" ]; then
    CMD+=("$ARG")  # 在while循环中读取它，填充一个用于执行最终命令的数组。
  else
    ...
  fi
done < <(build_command "$@")
...
LAST=$((COUNT - 1))
CMD=("${CMD[@]:0:$LAST}")
exec "${CMD[@]}"

# 以上程序就相当于：将类名和参数通过 build_command 处理后，循环放入 CMD 里，再执行 exec
```

下面看下 `org.apache.spark.launcher.Main` 里的 `main` 方法

```java
class Main {
    public static void main(String[] argsArray) throws Exception {
        checkArgument(argsArray.length > 0, "Not enough arguments: missing class name.");
        
        // 转成集合
        List<String> args = new ArrayList<>(Arrays.asList(argsArray));
        // 取出类名
        String className = args.remove(0);

        boolean printLaunchCommand = !isEmpty(System.getenv("SPARK_PRINT_LAUNCH_COMMAND"));
        Map<String, String> env = new HashMap<>();
        List<String> cmd;
        if (className.equals("org.apache.spark.deploy.SparkSubmit")) {
            try {
                // SparkSubmitCommandBuilder() 在调用spark-submit时使用; 
                // 这个 builder 用来解析并验证用户在命令行上提供的参数。
                AbstractCommandBuilder builder = new SparkSubmitCommandBuilder(args);
                // 使用上面创建的 builder 准备spark命令
                cmd = buildCommand(builder, env, printLaunchCommand);
            } catch (IllegalArgumentException e) {
                printLaunchCommand = false;
                System.err.println("Error: " + e.getMessage());
                System.err.println();

                MainClassOptionParser parser = new MainClassOptionParser();
                try {
                    parser.parse(args);
                } catch (Exception ignored) {
                    // Ignore parsing exceptions.
                }

                List<String> help = new ArrayList<>();
                if (parser.className != null) {
                    help.add(parser.CLASS);
                    help.add(parser.className);
                }
                help.add(parser.USAGE_ERROR);
                AbstractCommandBuilder builder = new SparkSubmitCommandBuilder(help);
                cmd = buildCommand(builder, env, printLaunchCommand);
            }
        } else {
            AbstractCommandBuilder builder = new SparkClassCommandBuilder(className, args);
            cmd = buildCommand(builder, env, printLaunchCommand);
        }

        // 判断操作系统
        if (isWindows()) {
            System.out.println(prepareWindowsCommand(cmd, env));
        } else {
            // A sequence of NULL character and newline separates command-strings and others.
            System.out.println('\0');

            // In bash, use NULL as the arg separator since it cannot be used in an argument.
            // 为执行，准备命令。 这样就得到了最终要执行的命令
            List<String> bashCmd = prepareBashCommand(cmd, env);
            for (String c : bashCmd) {
                System.out.print(c);
                System.out.print('\0');
            }
        }
    }
}
```

最终执行的命令就是: 

```shell
# exec "${CMD[@]} -->
exec ${JAVA_HOME}/bin/java org.apache.spark.deploy.SparkSubmit (--class 等其他参数) 
```

所以，接下来执行 `org.apache.spark.deploy.SparkSubmit` 下的 `main` 方法，使用 `"$@"` 传入的参数。此时就会启动一个 JVM 进程(SparkSubmit).


