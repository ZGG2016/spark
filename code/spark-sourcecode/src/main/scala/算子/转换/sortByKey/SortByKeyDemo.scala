package 算子.转换.sortByKey

import org.apache.spark.{SparkConf, SparkContext}

object SortByKeyDemo {
  def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf().setAppName("SortByKeyDemo").setMaster("local")
    val sc = new SparkContext(sparkConf)

    val rdd = sc.parallelize(List(("a", 1),("b", 5), ("c", 3)),2)

    val rlt1 = rdd.sortByKey()
    val rlt2 = rdd.sortByKey(ascending = false)

    rlt1.saveAsTextFile("output1")

    rlt2.saveAsTextFile("output2")

    sc.stop()

  }
}
