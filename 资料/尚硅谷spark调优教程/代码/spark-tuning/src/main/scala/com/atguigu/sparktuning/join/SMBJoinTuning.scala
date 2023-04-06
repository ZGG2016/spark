package com.atguigu.sparktuning.join

import com.atguigu.sparktuning.utils.InitUtil
import org.apache.spark.SparkConf
import org.apache.spark.sql.functions.broadcast
import org.apache.spark.sql.{SaveMode, SparkSession}

object SMBJoinTuning {

  def main( args: Array[String] ): Unit = {

    val sparkConf = new SparkConf().setAppName("SMBJoinTuning")
      .set("spark.sql.shuffle.partitions", "36")
    val sparkSession: SparkSession = InitUtil.initSparkSession(sparkConf)
    useSMBJoin(sparkSession)

  }

  def useSMBJoin( sparkSession: SparkSession ) = {
    //查询出三张表 并进行join 插入到最终表中
    val saleCourse = sparkSession.sql("select *from sparktuning.sale_course")
    val coursePay = sparkSession.sql("select * from sparktuning.course_pay_cluster")
      .withColumnRenamed("discount", "pay_discount")
      .withColumnRenamed("createtime", "pay_createtime")
    val courseShoppingCart = sparkSession.sql("select *from sparktuning.course_shopping_cart_cluster")
      .drop("coursename")
      .withColumnRenamed("discount", "cart_discount")
      .withColumnRenamed("createtime", "cart_createtime")

    val tmpdata = courseShoppingCart.join(coursePay, Seq("orderid"), "left")
    val result = broadcast(saleCourse).join(tmpdata, Seq("courseid"), "right")
    result
      .select("courseid", "coursename", "status", "pointlistid", "majorid", "chapterid", "chaptername", "edusubjectid"
        , "edusubjectname", "teacherid", "teachername", "coursemanager", "money", "orderid", "cart_discount", "sellmoney",
        "cart_createtime", "pay_discount", "paymoney", "pay_createtime", "sparktuning.sale_course.dt", "sparktuning.sale_course.dn")
      .write
      .mode(SaveMode.Overwrite)
      .saveAsTable("sparktuning.salecourse_detail_2")

  }
}
