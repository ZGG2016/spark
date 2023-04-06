package 算子.转换.filter

import org.apache.spark.{SparkConf, SparkContext}

object FilterDemo {
  def main(Args:Array[String]):Unit = {
    val conf = new SparkConf().setAppName("FilterDemo").setMaster("local")
    val sc = new SparkContext(conf)

    val rdd = sc.parallelize(List(1,2,3,4,5))
    val rlt = rdd.filter(_>4)

    rlt.foreach(println)
  }
}
