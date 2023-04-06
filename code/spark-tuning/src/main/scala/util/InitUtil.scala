package util

import bean.{School, Student}

import java.util.Random
import org.apache.spark.SparkConf
import org.apache.spark.sql.{SaveMode, SparkSession}

object InitUtil {

  def main( args: Array[String] ): Unit = {
    val sparkConf = new SparkConf().setAppName("InitData")
      .setMaster("local[*]") // 要打包提交集群执行，注释掉
    val sparkSession: SparkSession = initSparkSession(sparkConf)
    initHiveTable(sparkSession)
//    initBucketTable(sparkSession)
//    saveData(sparkSession)
  }

  def initSparkSession( sparkConf: SparkConf ): SparkSession = {
    System.setProperty("HADOOP_USER_NAME", "root")
    val sparkSession = SparkSession.builder().config(sparkConf).enableHiveSupport().getOrCreate()
    val ssc = sparkSession.sparkContext
    ssc.hadoopConfiguration.set("fs.defaultFS", "hdfs://bigdata101:9000")
    sparkSession
  }

  def initHiveTable( sparkSession: SparkSession ): Unit = {
    sparkSession.read.json("/sparkdata/coursepay.log")
      .write.partitionBy("dt", "dn")
      .format("parquet")
      .mode(SaveMode.Overwrite)
      .saveAsTable("sparktuning.course_pay")

    sparkSession.read.json("/sparkdata/salecourse.log")
      .write.partitionBy("dt", "dn")
      .format("parquet")
      .mode(SaveMode.Overwrite)
      .saveAsTable("sparktuning.sale_course")

//    sparkSession.read.json("/sparkdata/courseshoppingcart.log")
//      .write.partitionBy("dt", "dn")
//      .format("parquet")
//      .mode(SaveMode.Overwrite)
//      .saveAsTable("sparktuning.course_shopping_cart")

  }

  def initBucketTable( sparkSession: SparkSession ): Unit = {
    sparkSession.read.json("/sparkdata/coursepay.log")
      .write.partitionBy("dt", "dn")
      .format("parquet")
      .bucketBy(5, "orderid")
      .sortBy("orderid")
      .mode(SaveMode.Overwrite)
      .saveAsTable("sparktuning.course_pay_cluster")
    sparkSession.read.json("/sparkdata/courseshoppingcart.log")
      .write.partitionBy("dt", "dn")
      .bucketBy(5, "orderid")
      .format("parquet")
      .sortBy("orderid")
      .mode(SaveMode.Overwrite)
      .saveAsTable("sparktuning.course_shopping_cart_cluster")
  }

  def saveData(sparkSession: SparkSession) = {
    import sparkSession.implicits._
    sparkSession.range(1000000).mapPartitions(partitions => {
      val random = new Random()
      partitions.map(item => Student(item, "name" + item, random.nextInt(100), random.nextInt(100)))
    }).write.partitionBy("partition")
      .mode(SaveMode.Append)
      .saveAsTable("sparktuning.test_student")

    sparkSession.range(1000000).mapPartitions(partitions => {
      val random = new Random()
      partitions.map(item => School(item, "school" + item, random.nextInt(100)))
    }).write.partitionBy("partition")
      .mode(SaveMode.Append)
      .saveAsTable("sparktuning.test_school")
  }
}
