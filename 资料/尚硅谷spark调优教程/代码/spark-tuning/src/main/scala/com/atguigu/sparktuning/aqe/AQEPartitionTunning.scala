package com.atguigu.sparktuning.aqe

import com.atguigu.sparktuning.utils.InitUtil
import org.apache.spark.SparkConf
import org.apache.spark.sql.{SaveMode, SparkSession}

object AQEPartitionTunning {
  def main( args: Array[String] ): Unit = {
    val sparkConf = new SparkConf().setAppName("AQEPartitionTunning")
      .set("spark.sql.autoBroadcastJoinThreshold", "-1") //为了演示效果，禁用广播join
      .set("spark.sql.adaptive.enabled", "true")
      .set("spark.sql.adaptive.coalescePartitions.enabled", "true") // 合并分区的开关
      .set("spark.sql.adaptive.coalescePartitions.initialPartitionNum","1000") // 初始的并行度
      .set("spark.sql.adaptive.coalescePartitions.minPartitionNum","10") // 合并后的最小分区数
      .set("spark.sql.adaptive.advisoryPartitionSizeInBytes", "20mb") // 合并后的分区，期望有多大
    val sparkSession: SparkSession = InitUtil.initSparkSession(sparkConf)
    useJoin(sparkSession)
  }

  def useJoin( sparkSession: SparkSession ) = {
    val saleCourse = sparkSession.sql("select *from sparktuning.sale_course")
    val coursePay = sparkSession.sql("select * from sparktuning.course_pay")
      .withColumnRenamed("discount", "pay_discount")
      .withColumnRenamed("createtime", "pay_createtime")
    val courseShoppingCart = sparkSession.sql("select *from sparktuning.course_shopping_cart")
      .drop("coursename")
      .withColumnRenamed("discount", "cart_discount")
      .withColumnRenamed("createtime", "cart_createtime")

    saleCourse.join(courseShoppingCart, Seq("courseid", "dt", "dn"), "right")
      .join(coursePay, Seq("orderid", "dt", "dn"), "left")
      .select("courseid", "coursename", "status", "pointlistid", "majorid", "chapterid", "chaptername", "edusubjectid"
        , "edusubjectname", "teacherid", "teachername", "coursemanager", "money", "orderid", "cart_discount", "sellmoney",
        "cart_createtime", "pay_discount", "paymoney", "pay_createtime", "dt", "dn")
      .write.mode(SaveMode.Overwrite).insertInto("sparktuning.salecourse_detail_1")
  }

}
