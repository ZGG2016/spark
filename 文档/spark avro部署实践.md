
## 方式1

使用 `--packages` ，将 `org.apache.spark:spark-avro_2.12` 及其依赖添加到 `spark-shell`

```sh
[root@zgg spark-2.4.4-bin-hadoop2.7]# bin/pyspark --packages org.apache.spark:spark-avro_2.11:2.4.4
Python 2.7.5 (default, Apr  2 2020, 13:16:51) 
[GCC 4.8.5 20150623 (Red Hat 4.8.5-39)] on linux2
Type "help", "copyright", "credits" or "license" for more information.
Ivy Default Cache set to: /root/.ivy2/cache
The jars for the packages stored in: /root/.ivy2/jars
:: loading settings :: url = jar:file:/opt/spark-2.4.4-bin-hadoop2.7/jars/ivy-2.4.0.jar!/org/apache/ivy/core/settings/ivysettings.xml
org.apache.spark#spark-avro_2.11 added as a dependency
:: resolving dependencies :: org.apache.spark#spark-submit-parent-36366a70-2d08-415b-b5e5-138633451e6d;1.0
        confs: [default]
        found org.apache.spark#spark-avro_2.11;2.4.4 in central
        found org.spark-project.spark#unused;1.0.0 in central
downloading https://repo1.maven.org/maven2/org/apache/spark/spark-avro_2.11/2.4.4/spark-avro_2.11-2.4.4.jar ...
        [SUCCESSFUL ] org.apache.spark#spark-avro_2.11;2.4.4!spark-avro_2.11.jar (8743ms)
:: resolution report :: resolve 6348ms :: artifacts dl 8748ms
        :: modules in use:
        org.apache.spark#spark-avro_2.11;2.4.4 from central in [default]
        org.spark-project.spark#unused;1.0.0 from central in [default]
        ---------------------------------------------------------------------
        |                  |            modules            ||   artifacts   |
        |       conf       | number| search|dwnlded|evicted|| number|dwnlded|
        ---------------------------------------------------------------------
        |      default     |   2   |   1   |   1   |   0   ||   2   |   1   |
        ---------------------------------------------------------------------
:: retrieving :: org.apache.spark#spark-submit-parent-36366a70-2d08-415b-b5e5-138633451e6d
        confs: [default]
        1 artifacts copied, 1 already retrieved (182kB/7ms)
20/10/30 10:29:03 WARN util.NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
Setting default log level to "WARN".
To adjust logging level use sc.setLogLevel(newLevel). For SparkR, use setLogLevel(newLevel).
20/10/30 10:29:03 WARN conf.HiveConf: HiveConf of name hive.conf.hidden.list does not exist
Welcome to
      ____              __
     / __/__  ___ _____/ /__
    _\ \/ _ \/ _ `/ __/  '_/
   /__ / .__/\_,_/_/ /_/\_\   version 2.4.4
      /_/

Using Python version 2.7.5 (default, Apr  2 2020 13:16:51)
SparkSession available as 'spark'.
>>> dfa = spark.read.format("avro").load("file:///root/data/users.avro")
>>> dfa.select("name", "favorite_color").write.format("avro").save("file:///root/data/namesAndFavColors.avro")
>>> 
```

## 方式2

spark-avro_2.12 和它的依赖可以直接使用 `--packages` 添加到 `spark-submit`

从 [spark-avro_2.11/2.4.4](https://mvnrepository.com/artifact/org.apache.spark/spark-avro_2.11/2.4.4) 下载对应版本的 JAR 包，放到 Spark 的 `jars` 目录下。

再执行如下命令:

    spark-submit 
    --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.4.4
    --master spark://zgg:7077
    avro_read_sparksql.py  
