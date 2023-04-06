package 算子.转换.zipWithIndex

import org.apache.spark.{SparkConf, SparkContext}

object ZipWithIndexDemo {
  def main(Args:Array[String]):Unit = {
    val conf = new SparkConf().setAppName("ZipWithIndexDemo").setMaster("local")
    val sc = new SparkContext(conf)

    val rdd = sc.parallelize(List("a","b","b","c","d","e"),2)

    val rlt1 = rdd.zipWithIndex();
    rlt1.mapPartitionsWithIndex(
      (index, iter) => {
        iter.toList.map(x => "分区 " + index + " 的计算结果是 " + x).iterator
      })
      .foreach(println)

    println("---------------")

    val rlt2 = rdd.zipWithUniqueId();
    rlt2.mapPartitionsWithIndex(
      (index, iter) => {
        iter.toList.map(x => "分区 " + index + " 的计算结果是 " + x).iterator
      })
      .foreach(println)
  }
}
/*
// 第一个分区的第一项索引是0，最后一个分区的最后一项索引是最大的索引
分区 0 的计算结果是 (a,0)
分区 0 的计算结果是 (b,1)
分区 0 的计算结果是 (b,2)
分区 1 的计算结果是 (c,3)
分区 1 的计算结果是 (d,4)
分区 1 的计算结果是 (e,5)
---------------
分区 0 的计算结果是 (a,0)
分区 0 的计算结果是 (b,2)
分区 0 的计算结果是 (b,4)
分区 1 的计算结果是 (c,1)
分区 1 的计算结果是 (d,3)
分区 1 的计算结果是 (e,5)
 */