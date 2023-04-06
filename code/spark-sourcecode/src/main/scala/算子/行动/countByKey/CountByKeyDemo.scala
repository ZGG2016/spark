package 算子.行动.countByKey

import org.apache.spark.{SparkConf, SparkContext}

object CountByKeyDemo {
  def main(Args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("countByKey").setMaster("local")
    val sc = new SparkContext(conf)

    val rdd = sc.parallelize(List(("a", 1), ("a", 6),("a", 7),("b", 5), ("b", 3)), 2)

    // 本身就会collect到driver
//    val rlt = rdd.countByKey()
//    println(rlt)  //Map(b -> 2, a -> 3)

    val valueRDD = rdd.mapValues(_ => 1L)
    valueRDD.collect().foreach(println)

    val reduceRDD = valueRDD.reduceByKey(_ + _)
//    reduceRDD.collect().foreach(println)

  }
}
