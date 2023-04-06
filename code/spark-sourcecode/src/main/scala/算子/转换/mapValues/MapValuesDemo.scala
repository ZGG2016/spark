package 算子.转换.mapValues

import org.apache.spark.{SparkConf, SparkContext}

object MapValuesDemo {
  def main(Args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("MapValuesDemo").setMaster("local")
    val sc = new SparkContext(conf)

    val data = List(("a", "tom"), ("b", "jack"), ("c", "mike"), ("d", "alice"))
    val rdd = sc.parallelize(data, 2)
    // 针对分区操作
    val rlt = rdd.mapValues(x => x.toUpperCase)
    // ArrayBuffer((a,TOM), (b,JACK), (c,MIKE), (d,ALICE))
    println(rlt.collect().toBuffer)
  }
}
