package com.atguigu.sparktuning.reduce

import com.atguigu.sparktuning.utils.InitUtil
import org.apache.spark.SparkConf
import org.apache.spark.sql.{SaveMode, SparkSession}

object DynamicPartitionSmallFileTuning {


  def main( args: Array[String] ): Unit = {
    val sparkConf = new SparkConf().setAppName("DynamicPartitionSmallFileTuning")
      .set("spark.sql.shuffle.partitions", "36")
    //      .setMaster("local[*]") //TODO 要打包提交集群执行，注释掉
    val sparkSession: SparkSession = InitUtil.initSparkSession(sparkConf)

    //    sparkSession.sql(
    //      """
    //        |CREATE TABLE if not exists `sparktuning`.`dynamic_csc` (
    //        |  `courseid` BIGINT,
    //        |  `coursename` STRING,
    //        |  `createtime` STRING,
    //        |  `discount` STRING,
    //        |  `orderid` STRING,
    //        |  `sellmoney` STRING,
    //        |  `dt` STRING,
    //        |  `dn` STRING)
    //        |USING parquet
    //        |PARTITIONED BY (dt, dn)
    //      """.stripMargin)

    // TODO 非倾斜分区写入
    sparkSession.sql(
      """
        |insert overwrite sparktuning.dynamic_csc partition(dt,dn)
        |select * from sparktuning.course_shopping_cart
        |where dt!='20190722' and dn!='webA'
        |distribute by dt,dn
      """.stripMargin)

    // TODO 倾斜分区打散写入
    sparkSession.sql(
      """
        |insert overwrite sparktuning.dynamic_csc partition(dt,dn)
        |select * from sparktuning.course_shopping_cart
        |where dt='20190722' and dn='webA'
        |distribute by cast(rand() * 5 as int)
      """.stripMargin)


    //    while (true) {}
  }
}
