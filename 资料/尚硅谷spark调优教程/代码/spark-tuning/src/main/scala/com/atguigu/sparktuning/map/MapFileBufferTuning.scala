package com.atguigu.sparktuning.map

import com.atguigu.sparktuning.utils.InitUtil
import org.apache.spark.SparkConf
import org.apache.spark.sql.{SaveMode, SparkSession}

object MapFileBufferTuning {


  def main( args: Array[String] ): Unit = {
    val sparkConf = new SparkConf().setAppName("MapFileBufferTuning")
      .set("spark.sql.shuffle.partitions", "36")
      .set("spark.shuffle.file.buffer", "64")//对比 shuffle write 的stage 耗时
//      .set("spark.shuffle.spill.batchSize", "20000")// 不可修改
//      .set("spark.shuffle.spill.initialMemoryThreshold", "104857600")//不可修改
//      .setMaster("local[1]") //TODO 要打包提交集群执行，注释掉
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
