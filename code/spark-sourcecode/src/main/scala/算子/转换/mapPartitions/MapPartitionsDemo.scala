package 算子.转换.mapPartitions

import org.apache.spark.{SparkConf, SparkContext}

object MapPartitionsDemo {
  def main(Args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("MapPartitionsDemo").setMaster("local")
    val sc = new SparkContext(conf)
    val rdd = sc.parallelize(List(1, 2, 3, 4, 5, 6, 7, 8, 9), 2)

    rdd
      .mapPartitions(
        iter => {
          iter.toList.map(x=>x*10).iterator
        })
      .foreach(println)

  }
}
