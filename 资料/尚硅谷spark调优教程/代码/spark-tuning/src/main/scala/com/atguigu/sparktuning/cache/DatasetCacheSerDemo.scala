package com.atguigu.sparktuning.cache

import com.atguigu.sparktuning.bean.CoursePay
import com.atguigu.sparktuning.utils.InitUtil
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.storage.StorageLevel

object DatasetCacheSerDemo {

  def main( args: Array[String] ): Unit = {
    val sparkConf = new SparkConf()
      .setAppName("DatasetCacheSerDemo")
//      .setMaster("local[*]")
    val sparkSession: SparkSession = InitUtil.initSparkSession(sparkConf)

    import sparkSession.implicits._
    val result = sparkSession.sql("select * from sparktuning.course_pay").as[CoursePay]
    result.persist(StorageLevel.MEMORY_AND_DISK_SER)
    result.foreachPartition(( p: Iterator[CoursePay] ) => p.foreach(item => println(item.orderid)))

    while (true) {
      //因为历史服务器上看不到，storage内存占用，所以这里加个死循环 不让sparkcontext立马结束
    }
  }



}
