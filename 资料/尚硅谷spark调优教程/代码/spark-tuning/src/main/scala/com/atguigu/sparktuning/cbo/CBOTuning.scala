package com.atguigu.sparktuning.cbo

import com.atguigu.sparktuning.utils.InitUtil
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

object CBOTuning {


  def main( args: Array[String] ): Unit = {
    val sparkConf = new SparkConf().setAppName("CBOTuning")
      .set("spark.sql.cbo.enabled", "true")
//      .set("spark.sql.cbo.joinReorder.enabled", "true")
//      .setMaster("local[*]") //TODO 要打包提交集群执行，注释掉
    val sparkSession: SparkSession = InitUtil.initSparkSession(sparkConf)


    val sqlstr =
      """
        |select
        |  csc.courseid,
        |  sum(cp.paymoney) as coursepay
        |from course_shopping_cart csc,course_pay cp
        |where csc.orderid=cp.orderid
        |and cp.orderid ='odid-0'
        |group by csc.courseid
      """.stripMargin

    sparkSession.sql("use sparktuning;")
    sparkSession.sql(sqlstr).show()

    while (true){}

  }
}
