package aqe

import util.InitUtil
import org.apache.spark.SparkConf
import org.apache.spark.sql.{SaveMode, SparkSession}

/**
 * 动态切换 Join 策略
 *
 * 不启用就会使用 sort merge join,，启用了就使用  broadcast hash join
 */
object AqeDynamicSwitchJoin {
  def main( args: Array[String] ): Unit = {
    val sparkConf = new SparkConf().setAppName("AqeDynamicSwitchJoin").setMaster("local[*]")
      .set("spark.sql.adaptive.enabled", "false") // 这两个参数默认都是开启的
      .set("spark.sql.adaptive.localShuffleReader.enabled", "false") //在不需要进行shuffle重分区时，尝试使用本地shuffle读取器。将sort-meger join 转换为广播join
    val sparkSession: SparkSession = InitUtil.initSparkSession(sparkConf)
    switchJoinStartegies(sparkSession)
  }

  def switchJoinStartegies( sparkSession: SparkSession ) = {
    val coursePay = sparkSession.sql("select * from sparktuning.course_pay")
      .withColumnRenamed("discount", "pay_discount")
      .withColumnRenamed("createtime", "pay_createtime")
      .where("orderid between 'odid-9999000' and 'odid-9999999'")
    val courseShoppingCart = sparkSession.sql("select *from sparktuning.course_shopping_cart")
      .drop("coursename")
      .withColumnRenamed("discount", "cart_discount")
      .withColumnRenamed("createtime", "cart_createtime")
    val tmpdata = coursePay.join(courseShoppingCart, Seq("orderid"), "right")
    tmpdata.show()

    while(true){}
  }
}
