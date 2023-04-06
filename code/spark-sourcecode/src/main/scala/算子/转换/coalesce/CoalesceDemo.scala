package 算子.转换.coalesce

import org.apache.spark.{SparkConf, SparkContext}

object CoalesceDemo {
  def main(Args:Array[String]): Unit = {
    val conf = new SparkConf().setAppName("coalesce").setMaster("local")
    val sc = new SparkContext(conf)

    val data = sc.textFile("src/main/resources/coalesce.txt",10)

    val rlt1 = data.coalesce(4)
    val rlt2 = data.coalesce(100,shuffle = true)
    val rlt3 = data.coalesce(100)

    println(data.partitions.length) //10
    println(rlt1.partitions.length) //4
    println(rlt2.partitions.length) //100
    println(rlt3.partitions.length) //10
  }
}
