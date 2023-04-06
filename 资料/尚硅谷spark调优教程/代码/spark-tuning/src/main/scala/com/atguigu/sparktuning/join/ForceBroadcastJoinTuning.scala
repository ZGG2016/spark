package com.atguigu.sparktuning.join

import com.atguigu.sparktuning.utils.InitUtil
import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}

object ForceBroadcastJoinTuning {


  def main( args: Array[String] ): Unit = {
    val sparkConf = new SparkConf().setAppName("ForceBroadcastJoinTuning")
      .set("spark.sql.autoBroadcastJoinThreshold","-1") // 关闭自动广播
      .setMaster("local[*]") //TODO 要打包提交集群执行，注释掉
    val sparkSession: SparkSession = InitUtil.initSparkSession(sparkConf)


    //TODO SQL Hint方式
    val sqlstr1 =
      """
        |select /*+  BROADCASTJOIN(sc) */
        |  sc.courseid,
        |  csc.courseid
        |from sale_course sc join course_shopping_cart csc
        |on sc.courseid=csc.courseid
      """.stripMargin

    val sqlstr2 =
      """
        |select /*+  BROADCAST(sc) */
        |  sc.courseid,
        |  csc.courseid
        |from sale_course sc join course_shopping_cart csc
        |on sc.courseid=csc.courseid
      """.stripMargin

    val sqlstr3 =
      """
        |select /*+  MAPJOIN(sc) */
        |  sc.courseid,
        |  csc.courseid
        |from sale_course sc join course_shopping_cart csc
        |on sc.courseid=csc.courseid
      """.stripMargin



    sparkSession.sql("use sparktuning;")
    println("=======================BROADCASTJOIN Hint=============================")
    sparkSession.sql(sqlstr1).explain()
    println("=======================BROADCAST Hint=============================")
    sparkSession.sql(sqlstr2).explain()
    println("=======================MAPJOIN Hint=============================")
    sparkSession.sql(sqlstr3).explain()

    // TODO API的方式
    val sc: DataFrame = sparkSession.sql("select * from sale_course").toDF()
    val csc: DataFrame = sparkSession.sql("select * from course_shopping_cart").toDF()
    println("=======================DF API=============================")
    import org.apache.spark.sql.functions._
    broadcast(sc)
      .join(csc,Seq("courseid"))
      .select("courseid")
      .explain()
  }
}
