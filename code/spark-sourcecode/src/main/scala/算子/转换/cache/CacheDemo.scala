package 算子.转换.cache

import org.apache.spark.{SparkConf, SparkContext}

object CacheDemo {
  def main(Args:Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("CacheDemo").setMaster("local")
    val sc = new SparkContext(sparkConf)

    val rdd = sc.parallelize(Array(("aa", 1), ("bb", 5), ("cc", 9))).cache()

    println(rdd.collect().toBuffer)

  }
}
