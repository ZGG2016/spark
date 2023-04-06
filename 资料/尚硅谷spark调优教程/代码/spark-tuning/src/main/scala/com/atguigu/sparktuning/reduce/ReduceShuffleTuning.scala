package com.atguigu.sparktuning.reduce

import com.atguigu.sparktuning.utils.InitUtil
import org.apache.spark.SparkConf
import org.apache.spark.sql.{SaveMode, SparkSession}

object ReduceShuffleTuning {


  def main( args: Array[String] ): Unit = {
    val sparkConf = new SparkConf().setAppName("ReduceShuffleTuning")
      .set("spark.sql.autoBroadcastJoinThreshold", "-1")//为了演示效果，先禁用了广播join
      .set("spark.sql.shuffle.partitions", "36")
      .set("spark.reducer.maxSizeInFlight", "96m") // reduce缓冲区，默认48m
      .set("spark.shuffle.io.maxRetries", "6")  // 重试次数，默认3次
      .set("spark.shuffle.io.retryWait", "60s")  // 重试的间隔，默认5s
//          .setMaster("local[*]") //TODO 要打包提交集群执行，注释掉
    val sparkSession: SparkSession = InitUtil.initSparkSession(sparkConf)


    //查询出三张表 并进行join 插入到最终表中
    val saleCourse = sparkSession.sql("select * from sparktuning.sale_course")
    val coursePay = sparkSession.sql("select * from sparktuning.course_pay")
      .withColumnRenamed("discount", "pay_discount")
      .withColumnRenamed("createtime", "pay_createtime")
    val courseShoppingCart = sparkSession.sql("select * from sparktuning.course_shopping_cart")
      .drop("coursename")
      .withColumnRenamed("discount", "cart_discount")
      .withColumnRenamed("createtime", "cart_createtime")

    saleCourse
      .join(courseShoppingCart, Seq("courseid", "dt", "dn"), "right")
      .join(coursePay, Seq("orderid", "dt", "dn"), "left")
      .select("courseid", "coursename", "status", "pointlistid", "majorid", "chapterid", "chaptername", "edusubjectid"
        , "edusubjectname", "teacherid", "teachername", "coursemanager", "money", "orderid", "cart_discount", "sellmoney",
        "cart_createtime", "pay_discount", "paymoney", "pay_createtime", "dt", "dn")
      .write.mode(SaveMode.Overwrite).saveAsTable("sparktuning.salecourse_detail")


//    while (true) {}
  }
}
