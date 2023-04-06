package 算子.转换.sortBy

import org.apache.spark.{SparkConf, SparkContext}

object SortByDemo {
  def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf().setAppName("SortByDemo").setMaster("local")
    val sc = new SparkContext(sparkConf)

    val rdd1 = sc.makeRDD(List(3,8,1), 2)
    rdd1.sortBy(num=>num)
       .saveAsTextFile("output1")

    val rdd2 = sc.makeRDD(List(("1", 1), ("11", 2), ("2", 3)), 2)
    rdd2.sortBy(t=>t._1.toInt, ascending = false).saveAsTextFile("output2")

    sc.stop()

  }
}
