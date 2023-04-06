package 算子.转换.reduceByKey

import org.apache.spark.{SparkConf, SparkContext}

object ReduceByKeyDemo {
  def main(Args:Array[String]):Unit = {
    val conf = new SparkConf().setAppName("ReduceByKeyDemo").setMaster("local")
    val sc = new SparkContext(conf)

    val data = sc.parallelize("aabbaab")
    val pairs = data.map(s=>(s,1))

    val count = pairs.reduceByKey(_+_)

    // (a,4)  (b,3)
    count.collect().foreach(println)
  }
}
