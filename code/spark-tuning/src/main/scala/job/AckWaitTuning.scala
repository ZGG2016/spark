package job

import bean.CoursePay
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.storage.StorageLevel
import util.InitUtil

object AckWaitTuning {

  def main( args: Array[String] ): Unit = {
    val sparkConf = new SparkConf().setAppName("AckWaitTuning")
//      .set("spark.core.connection.ack.wait.timeout", "2s") // 连接超时时间，默认等于spark.network.timeout的值，默认120s
//          .setMaster("local[*]")
    val sparkSession: SparkSession = InitUtil.initSparkSession(sparkConf)
    useOFFHeapMemory(sparkSession)
  }

  def useOFFHeapMemory( sparkSession: SparkSession ): Unit = {
    import sparkSession.implicits._
    val result = sparkSession.sql("select * from sparktuning.course_pay").as[CoursePay]
    result.cache()
    result.foreachPartition(( p: Iterator[CoursePay] ) => p.foreach(item => println(item.orderid)))

//    while (true) {}
  }
}
