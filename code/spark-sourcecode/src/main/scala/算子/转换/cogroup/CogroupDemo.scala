package 算子.转换.cogroup

import org.apache.spark.{SparkConf, SparkContext}

object CogroupDemo {
  def main(Args:Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("CogroupDemo").setMaster("local")
    val sc = new SparkContext(sparkConf)

    val rdd1 = sc.parallelize(Array(("aa", 1), ("bb", 5), ("cc", 9)))
    val rdd2 = sc.parallelize(Array(("aa", 2), ("dd", 6), ("aa", 10)))
    val rdd3 = sc.parallelize(Array(("aa", 3), ("bb", 7), ("cc", 11)))
    val rdd4 = sc.parallelize(Array(("aa", 4), ("dd", 8), ("aa", 12)))

    /**
     * (aa,(CompactBuffer(1),CompactBuffer(2, 10)))
     * (dd,(CompactBuffer(),CompactBuffer(6)))
     * (bb,(CompactBuffer(5),CompactBuffer()))
     * (cc,(CompactBuffer(9),CompactBuffer()))
     */
    val rlt1 = rdd1.cogroup(rdd2)
    rlt1.collect.foreach(println)
    println("--------------连接两个RDD--------------")

    /**
     * (aa,(CompactBuffer(1),CompactBuffer(2, 10)))
     * (dd,(CompactBuffer(),CompactBuffer(6)))
     * (bb,(CompactBuffer(5),CompactBuffer()))
     * (cc,(CompactBuffer(9),CompactBuffer()))
     */
    val prlt = rdd1.cogroup(rdd2,2)
    prlt.collect.foreach(println)
    println("--------------指定分区数，连接两个RDD--------------")

    /**
     * (aa,(CompactBuffer(1),CompactBuffer(2, 10),CompactBuffer(3)))
     * (dd,(CompactBuffer(),CompactBuffer(6),CompactBuffer()))
     * (bb,(CompactBuffer(5),CompactBuffer(),CompactBuffer(7)))
     * (cc,(CompactBuffer(9),CompactBuffer(),CompactBuffer(11)))
     *
     */
    val rlt2 = rdd1.cogroup(rdd2,rdd3)
    rlt2.collect.foreach(println)
    println("--------------连接三个RDD--------------")

    /**
     * (aa,(CompactBuffer(1),CompactBuffer(2, 10),CompactBuffer(3),CompactBuffer(4, 12)))
     * (dd,(CompactBuffer(),CompactBuffer(6),CompactBuffer(),CompactBuffer(8)))
     * (bb,(CompactBuffer(5),CompactBuffer(),CompactBuffer(7),CompactBuffer()))
     * (cc,(CompactBuffer(9),CompactBuffer(),CompactBuffer(11),CompactBuffer()))
     */
    val rlt3 = rdd1.cogroup(rdd2,rdd3,rdd4)
    rlt3.collect.foreach(println)
    println("--------------连接四个RDD--------------")


  }
}
