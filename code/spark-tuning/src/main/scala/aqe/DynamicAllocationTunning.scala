package aqe

import org.apache.spark.SparkConf
import org.apache.spark.sql.{SaveMode, SparkSession}
import util.InitUtil
/**
 * 动态合并分区，结合动态申请资源
 *
 * 在执行程序时，指定了使用3个executor，但实际使用的executor可能会大于3个
 */
object DynamicAllocationTunning {
  def main( args: Array[String] ): Unit = {
    val sparkConf = new SparkConf().setAppName("DynamicAllocationTunning")
      .set("spark.sql.autoBroadcastJoinThreshold", "-1")
      .set("spark.sql.adaptive.enabled", "true")
      .set("spark.sql.adaptive.coalescePartitions.enabled", "true")
      .set("spark.sql.adaptive.coalescePartitions.initialPartitionNum","1000")
      .set("spark.sql.adaptive.coalescePartitions.minPartitionNum","10")
      .set("spark.sql.adaptive.advisoryPartitionSizeInBytes", "20mb")
      .set("spark.dynamicAllocation.enabled","true")  // 动态申请资源
      .set("spark.dynamicAllocation.shuffleTracking.enabled","true") // shuffle动态跟踪
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
