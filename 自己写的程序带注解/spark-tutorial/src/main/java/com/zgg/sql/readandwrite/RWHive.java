package com.zgg.sql.readandwrite;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;

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
