package 算子.行动.count

import org.apache.spark.{SparkConf, SparkContext}

object CountDemo {
  def main(Args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("count").setMaster("local")
    val sc = new SparkContext(conf)

    val rdd = sc.parallelize(List(1,1,2,2,2,1,4,5))

    val rlt = rdd.count() //8
    println(rlt)
  }
}
