package 算子.转换.flatMap

import org.apache.spark.{SparkConf, SparkContext}

object FlatMapDemo {
  def main(Args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("FlatMapDemo").setMaster("local")
    val sc = new SparkContext(conf)

    val rdd = sc.parallelize(List("ab", "cd"))

    val rlt = rdd.flatMap(x => x.toUpperCase)
    println(rlt.collect().toBuffer)

  }
}
