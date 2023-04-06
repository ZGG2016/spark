package aqe

import util.InitUtil
import org.apache.spark.SparkConf
import org.apache.spark.sql.{SaveMode, SparkSession}

/**
 * 动态优化 Join 倾斜
 */
object AqeOptimizingSkewJoin {
  def main( args: Array[String] ): Unit = {
    val sparkConf = new SparkConf().setAppName("AqeOptimizingSkewJoin").setMaster("local[*]")
      .set("spark.sql.autoBroadcastJoinThreshold", "-1")  //为了演示效果，禁用广播join
//      .set("spark.sql.adaptive.coalescePartitions.enabled", "false") // 为了演示效果，关闭自动缩小分区
      .set("spark.sql.adaptive.coalescePartitions.enabled", "true")
      .set("spark.sql.adaptive.enabled", "true")
      .set("spark.sql.adaptive.skewJoin.enable","true")
      /*
          中位数1.2 * 5 = 6
          观察未启用这个功能的输出，101 103分区的数据量是超过了6，且大于8mb，所以就判断是数据倾斜的。
          就会自动打散A表的101 103分区，扩充表B对应的分区，所以分区数会超过默认的200个。
          join后的数据，每个分区的数据量不会超过8mb（推荐大小，不是真实大小）

          开启了 spark.sql.adaptive.coalescePartitions.enabled 动态合并分区功能，
          那么会先合并分区，再去判断倾斜
       */
      .set("spark.sql.adaptive.skewJoin.skewedPartitionFactor","5")
      .set("spark.sql.adaptive.skewJoin.skewedPartitionThresholdInBytes","20mb")
      .set("spark.sql.adaptive.advisoryPartitionSizeInBytes", "8mb")
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
