package 算子.转换.groupByKey

import org.apache.spark.{SparkConf, SparkContext}

object GroupByKeyDemo {

  def main(Args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("GroupByKeyDemo").setMaster("local")
    val sc = new SparkContext(conf)

    val rdd = sc.parallelize("aabbaab")

    val rlt = rdd.map((_, 1)).groupByKey()

    // ArrayBuffer((a,CompactBuffer(1, 1, 1, 1)), (b,CompactBuffer(1, 1, 1)))
    println(rlt.collect().toBuffer)
  }
}
