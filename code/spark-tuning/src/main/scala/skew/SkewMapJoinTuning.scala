package skew

import org.apache.spark.SparkConf
import org.apache.spark.sql.{SaveMode, SparkSession}
import util.InitUtil

/**
 * Join 数据倾斜优化: 广播 Join
 */
object SkewMapJoinTuning {
  def main( args: Array[String] ): Unit = {
    val sparkConf = new SparkConf().setAppName("SkewMapJoinTuning")
      .set("spark.sql.autoBroadcastJoinThreshold", "10m")
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
