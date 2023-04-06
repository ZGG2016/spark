package com.atguigu.sparktuning.partition

import com.atguigu.sparktuning.utils.InitUtil
import org.apache.spark.SparkConf
import org.apache.spark.sql.{SaveMode, SparkSession}

object PartitionTuning {
  def main( args: Array[String] ): Unit = {
    val sparkConf = new SparkConf().setAppName("PartitionTuning")
      .set("spark.sql.autoBroadcastJoinThreshold", "-1")//为了演示效果，先禁用了广播join
//      .setMaster("local[*]")
      .set("spark.sql.shuffle.partitions", "36")
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
  }

}





/*
    val sqlstr =
      """
        |select
        |  sc.courseid,
        |  sc.coursename,
        |  status,
        |  pointlistid,
        |  majorid,
        |  chapterid,
        |  chaptername,
        |  edusubjectid,
        |  edusubjectname,
        |  teacherid,
        |  teachername,
        |  coursemanager,
        |  money,
        |  csc.orderid,
        |  csc.discount cart_discount,
        |  sellmoney,
        |  csc.createtime cart_createtime,
        |  cp.discount pay_discount,
        |  paymoney,
        |  cp.createtime pay_createtime,
        |  sc.dt,
        |  sc.dn
        |from course_shopping_cart csc
        |join course_pay cp
        |  on csc.orderid=cp.orderid and csc.dt=cp.dt and csc.dn=cp.dn
        |join sale_course sc
        |  on sc.courseid=csc.courseid and sc.dt=csc.dt and sc.dn=csc.dn
      """.stripMargin*/
