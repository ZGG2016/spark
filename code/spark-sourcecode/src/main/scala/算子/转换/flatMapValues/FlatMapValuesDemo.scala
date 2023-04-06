package 算子.转换.flatMapValues

import org.apache.spark.{SparkConf, SparkContext}

object FlatMapValuesDemo {
  def main(Args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("MapValuesDemo").setMaster("local")
    val sc = new SparkContext(conf)

    val data = List(("a", "tom"), ("b", "jack"), ("c", "mike"), ("d", "alice"))
    val rdd = sc.parallelize(data, 2)
    // 针对分区操作
    val rlt = rdd.flatMapValues(x => x.toUpperCase)
    // ArrayBuffer((a,T), (a,O), (a,M), (b,J)....
    println(rlt.collect().toBuffer)
  }
}
