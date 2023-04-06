package dpp

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import util.InitUtil

/**
 * 动态分区裁剪
 */
object DPPTest {

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("DPPTest")
      .set("spark.sql.optimizer.dynamicPartitionPruning.enabled", "true")
      .setMaster("local[*]")
    val sparkSession: SparkSession = InitUtil.initSparkSession(sparkConf)

    val result=sparkSession.sql(
      """
        |select a.id,a.name,a.age,b.name
        |from sparktuning.test_student a
        |inner join sparktuning.test_school b
        |on a.partition=b.partition and b.id<1000
      """.stripMargin)
//      .explain(mode="extended")

    result.foreach(item=>println(item.get(1)))
  }


}
