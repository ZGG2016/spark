package 算子.转换.persist

import org.apache.spark.{SparkConf, SparkContext}

object PersistDemo {
  def main(Args:Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("PersistDemo").setMaster("local")
    val sc = new SparkContext(sparkConf)

    val rdd = sc.parallelize(Array(("aa", 1), ("bb", 5), ("cc", 9))).persist()

    println(rdd.collect().toBuffer)

  }
}
