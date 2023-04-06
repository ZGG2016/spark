package 算子.转换.map

import org.apache.spark.{SparkConf, SparkContext}

object MapDemo {
  def main(Args:Array[String]): Unit ={
    val conf = new SparkConf().setAppName("MapDemo").setMaster("local")
    val sc = new SparkContext(conf)

    val rdd = sc.parallelize(List("ab","cd"))

    val rlt = rdd.map(x=>x.toUpperCase)
    // ArrayBuffer(AB, CD)
    println(rlt.collect().toBuffer)

  }
}
