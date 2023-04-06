package 算子.转换.mapPartitionsWithIndex

import org.apache.spark.{SparkConf, SparkContext}

object MapPartitionsWithIndexDemo {
  def main(Args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("MapPartitionsWithIndexDemo").setMaster("local")
    val sc = new SparkContext(conf)
    val rdd = sc.parallelize(List(1, 2, 3, 4, 5, 6, 7, 8, 9), 2)

    rdd
      .mapPartitionsWithIndex(
        (index, iter) => {
          iter.toList.map(x => "分区 " + index + " 的计算结果是 " + x * 10).iterator
        })
      .foreach(println)

  }
}
