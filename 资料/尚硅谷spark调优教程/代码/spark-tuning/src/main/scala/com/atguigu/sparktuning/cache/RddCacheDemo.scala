package com.atguigu.sparktuning.cache

import com.atguigu.sparktuning.utils.InitUtil
import org.apache.spark.SparkConf
import org.apache.spark.sql.{Row, SparkSession}

object RddCacheDemo {

  def main( args: Array[String] ): Unit = {
    val sparkConf = new SparkConf().setAppName("RddCacheDemo")
    val sparkSession: SparkSession = InitUtil.initSparkSession(sparkConf)

    val result = sparkSession.sql("select * from sparktuning.course_pay").rdd
    result.cache()
    result.foreachPartition(( p: Iterator[Row] ) => p.foreach(item => println(item.get(0))))
    while (true) {
      //因为历史服务器上看不到，storage内存占用，所以这里加个死循环 不让sparkcontext立马结束
    }
  }

}
