# sparksql 操作 hive

[TOC]

如果要在 Spark SQL 中包含 Hive 的库，并不需要事先安装 Hive。一般来说，最好还是在编译 Spark SQL 时引入 Hive 支持，这样就可以使用这些特性了。

如果你下载的是二进制版本的 Spark，它应该已经在编译时添加了 Hive 支持。

若要把 Spark SQL 连接到一个部署好的 Hive 上，你必须把 hive-site.xml 复制到 Spark 的配置文件目录中(`$SPARK_HOME/conf`)。即使没有部署好 Hive，Spark SQL 也可以运行。 

需要注意的是，如果你没有部署好 Hive，Spark SQL 会在当前的工作目录中创建出自己的 Hive 元数据仓库，叫作 `metastore_db`。

此外，如果你尝试使用 HiveQL 中的 `CREATE TABLE` (并非 `CREATE EXTERNAL TABLE`)语句来创建表，这些表会被放在你默认的文件系统中的 `/user/hive/warehouse` 目录中(如果你的 classpath 中有配好的 `hdfs-site.xml`，默认的文件系统就是 HDFS，否则就是本地文件系统)。

spark-shell 默认是 Hive 支持的；代码中是默认不支持的，需要手动指定（加一个参数即可）。

## 1 内嵌的 HIVE

如果使用 Spark 内嵌的 Hive, 则什么都不用做, 直接使用即可.

Hive 的元数据存储在 derby 中, 默认仓库地址:`$SPARK_HOME/spark-warehouse`

```scala
scala> spark.sql("show tables").show
+--------+---------+-----------+
|database|tableName|isTemporary|
+--------+---------+-----------+
+--------+---------+-----------+

scala> spark.sql("create table aa(id int)")

scala> spark.sql("show tables").show
+--------+---------+-----------+
|database|tableName|isTemporary|
+--------+---------+-----------+
| default| aa| false|
+--------+---------+-----------+

// 向表加载本地数据
scala> spark.sql("load data local inpath 'input/ids.txt' into table aa")

scala> spark.sql("select * from aa").show
+---+
| id|
+---+
| 1|
| 2|
| 3|
| 4|
+---+
```

在实际使用中, 几乎没有任何人会使用内置的 Hive

## 2 外部的 HIVE

如果想连接外部已经部署好的 Hive，需要通过以下几个步骤：

- Spark 要接管 Hive 需要把 `hive-site.xml` 拷贝到 `conf/` 目录下
- 把 Mysql 的驱动 copy 到 `jars/` 目录下
- 如果访问不到 hdfs，则需要把 `core-site.xml` 和 `hdfs-site.xml` 拷贝到 `conf/` 目录下
- 重启 spark-shell

```scala
scala> spark.sql("show tables").show
20/04/25 22:05:14 WARN ObjectStore: Failed to get database global_temp, returning
NoSuchObjectException
+--------+--------------------+-----------+
|database| tableName|isTemporary|
+--------+--------------------+-----------+
| default| emp| false|
| default|hive_hbase_emp_table| false|
| default| relevance_hbase_emp| false|
| default| staff_hive| false|
| default| ttt| false|
| default| user_visit_action| false|
+--------+--------------------+-----------+
```

## 3  运行 Spark SQL CLI

Spark SQL CLI 可以很方便的在本地运行 Hive 元数据服务以及从命令行执行查询任务。

在 Spark 目录下执行如下命令启动 Spark SQL CLI，直接执行 SQL 语句，类似 Hive 窗口

```sh
root@bigdata101:/opt/spark-yarn# bin/spark-sql
spark-sql (default)> show tables;
namespace       tableName       isTemporary
business
city_info
dept
```

## 4 运行 Spark beeline

Spark Thrift Server 是 Spark 社区基于 HiveServer2 实现的一个 Thrift 服务。旨在无缝兼容 HiveServer2。

因为 Spark Thrift Server 的接口和协议都和 HiveServer2 完全一致，因此我们部署好 Spark Thrift Server 后，可以直接使用 hive 的 beeline 访问 Spark Thrift Server 执行相关语句。

Spark Thrift Server 的目的也只是取代 HiveServer2，因此它依旧可以和 Hive Metastore 进行交互，获取到 hive 的元数据。

要在 `hive-site.xml` 中添加如下配置：

```xml
	<property>
		<name>hive.server2.thrift.bind.host</name>
		<value>bigdata101</value>
	</property>
	<property>
		<name>hive.server2.thrift.port</name>
		<value>10000</value>
	</property>
	<property>
		<name>hive.server2.transport.mode</name>
		<value>binary</value>
	</property>
```

如果想连接 Thrift Server，需要通过以下几个步骤：

- Spark 要接管 Hive 需要把 `hive-site.xml` 拷贝到 `conf/` 目录下
- 把 Mysql 的驱动 copy 到 `jars/` 目录下
- 如果访问不到 hdfs，则需要把 `core-site.xml` 和 `hdfs-site.xml` 拷贝到 `conf/` 目录下
- 启动 Thrift Server

```sh
sbin/start-thriftserver.sh
```

- 使用 beeline 连接 Thrift Server

```sh
root@bigdata101:/opt/spark-yarn# bin/beeline -u jdbc:hive2://bigdata101:10000/default;auth=noSasl -n root
Connecting to jdbc:hive2://bigdata101:10000/default
Connected to: Spark SQL (version 3.3.0)
Driver: Hive JDBC (version 2.3.9)
Transaction isolation: TRANSACTION_REPEATABLE_READ
Beeline version 2.3.9 by Apache Hive
0: jdbc:hive2://bigdata101:10000/default> show tables;
+------------+---------------------+--------------+
| namespace  |      tableName      | isTemporary  |
+------------+---------------------+--------------+
| default    | business            | false        |
| default    | city_info           | false        |
| default    | dept                | false        |
| default    | dept_partition      | false        |
.....
```

## 5 代码操作 Hive

导入依赖

```xml
<dependency>
	<groupId>org.apache.spark</groupId>
	<artifactId>spark-hive_2.12</artifactId>
	<version>3.3.0</version>
</dependency>
<dependency>
	<groupId>org.apache.hive</groupId>
	<artifactId>hive-exec</artifactId>
	<version>3.1.2</version>
</dependency>
<dependency>
	<groupId>mysql</groupId>
	<artifactId>mysql-connector-java</artifactId>
	<version>8.0.30</version>
</dependency>
```

将 `hive-site.xml` 文件拷贝到项目的 resources 目录中，代码实现


```scala
public class RWHive {
    public static void main(String[] args) {
        // 出现权限问题，添加此行
        System.setProperty("HADOOP_USER_NAME", "root");

        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("RWHive")
                // 在开发工具中创建数据库默认是在本地仓库，通过参数修改数据库仓库的地址
                .set("spark.sql.warehouse.dir", "hdfs://bigdata101:9000/user/hive/warehouse");

        SparkSession sparkSession = SparkSession
                .builder()
                .enableHiveSupport()
                .config(conf)
                .getOrCreate();

        /*
            使用SparkSQL连接外置的Hive
              1. 拷贝Hive-size.xml文件到classpath下
                 要保证 target/class 目录下也有这个文件
              2. 启用Hive的支持
              3. 增加对应的依赖关系（包含MySQL驱动）
         */

        sparkSession.sql("show tables").show();

        sparkSession.close();

    }
}
```

注意：在开发工具中创建数据库默认是在本地仓库，通过参数修改数据库仓库的地址:

```scala
config("spark.sql.warehouse.dir", "hdfs://bigdata101:9000/user/hive/warehouse")
```

如果在执行操作时，出现用户权限错误，可以代码最前面增加如下代码解决：

```scala
System.setProperty("HADOOP_USER_NAME", "root")
```

此处的 root 改为你们自己的 hadoop 用户名称

<hr>

参考：[尚硅谷 spark 教程](https://www.bilibili.com/video/BV11A411L7CK)