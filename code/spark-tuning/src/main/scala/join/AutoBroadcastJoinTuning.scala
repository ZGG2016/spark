package join

import util.InitUtil
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

/**
 *  大小表join测试：广播 Join
 */
object AutoBroadcastJoinTuning {

  def main( args: Array[String] ): Unit = {
    val sparkConf = new SparkConf().setAppName("BroadcastJoinTuning")
      .set("spark.sql.autoBroadcastJoinThreshold","-1") // 关闭自动广播
//      .set("spark.sql.autoBroadcastJoinThreshold","10mb") // 开启自动广播
      .setMaster("local[*]") //  要打包提交集群执行，注释掉
    val sparkSession: SparkSession = InitUtil.initSparkSession(sparkConf)

    val sqlstr =
      """
        |select
        |  sc.courseid,
        |  csc.courseid
        |from sale_course sc join course_shopping_cart csc
        |on sc.courseid=csc.courseid
      """.stripMargin

    sparkSession.sql("use sparktuning;")
    sparkSession.sql(sqlstr).show()

    while(true){}

  }
}
