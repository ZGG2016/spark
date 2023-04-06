package com.atguigu.sparktuning.job

import com.atguigu.sparktuning.bean.CoursePay
import com.atguigu.sparktuning.utils.InitUtil
import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, Dataset, SaveMode, SparkSession}

object LocalityWaitTuning {


  def main( args: Array[String] ): Unit = {
    val sparkConf = new SparkConf().setAppName("LocalityWaitTuning")
      // 分别打包测试
//      .set("spark.locality.wait", "1")
//      .set("spark.locality.wait.process", "1")
//      .set("spark.locality.wait.node", "1")
//      .set("spark.locality.wait.rack", "1")
      // 分别打包测试
      .set("spark.locality.wait", "6s")
      .set("spark.locality.wait.process", "60s")
      .set("spark.locality.wait.node", "30s")
      .set("spark.locality.wait.rack", "20s")
    val sparkSession: SparkSession = InitUtil.initSparkSession(sparkConf)

    import sparkSession.implicits._
    val ds: Dataset[CoursePay] = sparkSession
      .read.json("/sparkdata/coursepay.log").as[CoursePay]
    ds.cache()
    ds.foreachPartition(( p: Iterator[CoursePay] ) => p.foreach(item => println(item.orderid)))
  }
}
