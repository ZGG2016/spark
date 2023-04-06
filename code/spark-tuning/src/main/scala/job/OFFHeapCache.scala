package job


import bean.CoursePay
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.storage.StorageLevel
import util.InitUtil

/**
 * 使用堆外内存
 */
object OFFHeapCache {

  def main( args: Array[String] ): Unit = {
    val sparkConf = new SparkConf().setAppName("OFFHeapCache").setMaster("local[*]")
    val sparkSession: SparkSession = InitUtil.initSparkSession(sparkConf)
    useOFFHeapMemory(sparkSession)
  }

  def useOFFHeapMemory( sparkSession: SparkSession ): Unit = {
    import sparkSession.implicits._
    val result = sparkSession.sql("select * from sparktuning.course_pay").as[CoursePay]
    // TODO 指定持久化到 堆外内存
    result.persist(StorageLevel.OFF_HEAP)
    result.foreachPartition(( p: Iterator[CoursePay] ) => p.foreach(item => println(item.orderid)))

//    while (true) {}
  }
}
