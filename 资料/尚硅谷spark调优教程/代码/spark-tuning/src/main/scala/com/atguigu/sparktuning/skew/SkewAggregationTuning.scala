package com.atguigu.sparktuning.skew

import java.util.Random

import com.atguigu.sparktuning.utils.InitUtil
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

object SkewAggregationTuning {
  def main( args: Array[String] ): Unit = {

    val sparkConf = new SparkConf().setAppName("SkewAggregationTuning")
      .set("spark.sql.shuffle.partitions", "36")
//      .setMaster("local[*]")
    val sparkSession: SparkSession = InitUtil.initSparkSession(sparkConf)

    sparkSession.udf.register("random_prefix", ( value: Int, num: Int ) => randomPrefixUDF(value, num))
    sparkSession.udf.register("remove_random_prefix", ( value: String ) => removeRandomPrefixUDF(value))


    val sql1 =
      """
        |select
        |  courseid,
        |  sum(course_sell) totalSell
        |from
        |  (
        |    select
        |      remove_random_prefix(random_courseid) courseid,
        |      course_sell
        |    from
        |      (
        |        select
        |          random_courseid,
        |          sum(sellmoney) course_sell
        |        from
        |          (
        |            select
        |              random_prefix(courseid, 6) random_courseid,
        |              sellmoney
        |            from
        |              sparktuning.course_shopping_cart
        |          ) t1
        |        group by random_courseid
        |      ) t2
        |  ) t3
        |group by
        |  courseid
      """.stripMargin


    val sql2=
      """
        |select
        |  courseid,
        |  sum(sellmoney)
        |from sparktuning.course_shopping_cart
        |group by courseid
      """.stripMargin

    sparkSession.sql(sql1).show(10000)


//    while(true){}
  }


  def randomPrefixUDF( value: Int, num: Int ): String = {
    new Random().nextInt(num).toString + "_" + value
  }

  def removeRandomPrefixUDF( value: String ): String = {
    value.toString.split("_")(1)
  }
}