# Distributed SQL Engine

[TOC]

> Spark SQL can also act as a distributed query engine using its JDBC/ODBC or command-line interface. In this mode, end-users or applications can interact with Spark SQL directly to run SQL queries, without the need to write any code.

Spark SQL 可以使用其 JDBC/ODBC 或命令行界面作为分布式查询引擎。

在这种模式下，终端用户或应用程序可以直接与 Spark SQL 交互，来运行 SQL 查询，而不需要编写任何代码。

## Running the Thrift JDBC/ODBC server

> The Thrift JDBC/ODBC server implemented here corresponds to the [HiveServer2](https://cwiki.apache.org/confluence/display/Hive/Setting+Up+HiveServer2) in built-in Hive. You can test the JDBC server with the beeline script that comes with either Spark or compatible Hive.

Thrift JDBC/ODBC 服务对应于 Hive 中的 HiveServer2. 你可以使用 Spark 或兼容的 Hive 的 beeline 脚本测试 JDBC server.

> To start the JDBC/ODBC server, run the following in the Spark directory:

要启动 JDBC/ODBC 服务，请在 Spark 目录中运行以下命令:

```SH
./sbin/start-thriftserver.sh
```

> This script accepts all `bin/spark-submit` command line options, plus a `--hiveconf` option to specify Hive properties. You may run `./sbin/start-thriftserver.sh --help` for a complete list of all available options. By default, the server listens on localhost:10000. You may override this behaviour via either environment variables, i.e.:

此脚本接受所有 `bin/spark-submit` 命令行选项，以及 `--hiveconf` 选项来指定 Hive 属性。

可以运行 `./sbin/start-thriftserver.sh --help` 查看所有可用的选项。

默认情况下，服务监听 localhost:10000. 你可以通过设置环境变量覆盖此行为，即:

```
export HIVE_SERVER2_THRIFT_PORT=<listening-port>
export HIVE_SERVER2_THRIFT_BIND_HOST=<listening-host>
./sbin/start-thriftserver.sh \
  --master <master-uri> \
  ...
```

> or system properties:

或系统属性

```
./sbin/start-thriftserver.sh \
  --hiveconf hive.server2.thrift.port=<listening-port> \
  --hiveconf hive.server2.thrift.bind.host=<listening-host> \
  --master <master-uri>
  ...
```

> Now you can use beeline to test the Thrift JDBC/ODBC server:

现在你可以使用 beeline 测试 Thrift JDBC/ODBC 服务：

```
./bin/beeline
```

> Connect to the JDBC/ODBC server in beeline with:

在 beeline 中连接 JDBC/ODBC 服务

```
beeline> !connect jdbc:hive2://localhost:10000
```

> Beeline will ask you for a username and password. In non-secure mode, simply enter the username on your machine and a blank password. For secure mode, please follow the instructions given in the [beeline documentation](https://cwiki.apache.org/confluence/display/Hive/HiveServer2+Clients).

Beeline 将要求你需要输入用户名和密码。在非安全模式下，输入机器名称和空的密码。在安全模式下，按要求输入。

> Configuration of Hive is done by placing your `hive-site.xml`, `core-site.xml` and `hdfs-site.xml` files in `conf/`.

将 `hive-site.xml`, `core-site.xml` 和 `hdfs-site.xml` 复制到 `conf/` 目录下，完成 Hive 的配置。

> You may also use the beeline script that comes with Hive.

你也可以使用 hive 提供的 beeline 脚本。

> Thrift JDBC server also supports sending thrift RPC messages over HTTP transport. Use the following setting to enable HTTP mode as system property or in `hive-site.xml` file in `conf/`:

Thrift JDBC 服务也支持通过 HTTP transport 发送 thrift RPC 消息。

使用下面的设置启用 HTTP 模式作为系统属性，或在 `conf/` 目录下的 `hive-site.xml` 文件中设置。

```
hive.server2.transport.mode - Set this to value: http
hive.server2.thrift.http.port - HTTP port number to listen on; default is 10001
hive.server2.http.endpoint - HTTP endpoint; default is cliservice
```

> To test, use beeline to connect to the JDBC/ODBC server in http mode with:

在 http 模式下，使用 beeline 连接 Thrift JDBC 服务：

```
beeline> !connect jdbc:hive2://<host>:<port>/<database>?hive.server2.transport.mode=http;hive.server2.thrift.http.path=<http_endpoint>
```

> If you closed a session and do CTAS, you must set `fs.%s.impl.disable.cache` to true in `hive-site.xml`. See more details in [[SPARK-21067]](https://issues.apache.org/jira/browse/SPARK-21067).

如果你关闭了一个会话，并执行 CTAS 语句，那么你必须在 `hive-site.xml` 中设置 `set fs.%s.impl.disable.cache = true`.

## Running the Spark SQL CLI

> To use the Spark SQL command line interface (CLI) from the shell:

在 shell 中，使用 Spark SQL CLI:

```
./bin/spark-sql
```

> For details, please refer to [Spark SQL CLI](https://spark.apache.org/docs/3.3.2/sql-distributed-sql-engine-spark-sql-cli.html)