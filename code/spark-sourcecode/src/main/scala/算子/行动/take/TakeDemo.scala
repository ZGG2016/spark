package 算子.行动.take

import org.apache.spark.{SparkConf, SparkContext}

object TakeDemo {
  def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf().setAppName("TakeDemo").setMaster("local")
    val sc = new SparkContext(sparkConf)

    val rdd = sc.parallelize(List(1,2,3,4,5))
    println(rdd.take(2).toBuffer)

    sc.stop()

  }
}
