package map

import org.apache.spark.SparkConf
import org.apache.spark.sql.{SaveMode, SparkSession}
import util.InitUtil

/**
 * 读取小文件优化
 */
object MapSmallFileTuning {

  def main( args: Array[String] ): Unit = {
    val sparkConf = new SparkConf().setAppName("MapSmallFileTuning")
      .set("spark.files.openCostInBytes", "7194304") //默认4m   4194304
      .set("spark.sql.files.maxPartitionBytes", "128MB") //默认128M
      .setMaster("local[1]") //TODO 要打包提交集群执行，注释掉
    val sparkSession: SparkSession = InitUtil.initSparkSession(sparkConf)

    sparkSession.sql("select * from sparktuning.course_shopping_cart")
      .write
      .mode(SaveMode.Overwrite)
      .saveAsTable("sparktuning.test2")

//    while (true) {}
  }
}
