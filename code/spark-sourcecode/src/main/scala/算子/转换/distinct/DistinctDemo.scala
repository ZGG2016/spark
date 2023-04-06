package 算子.转换.distinct

import org.apache.spark.{SparkConf, SparkContext}

object DistinctDemo {

  def main(Args:Array[String]):Unit = {
    val conf = new SparkConf().setAppName("DistinctDemo").setMaster("local")
    val sc = new SparkContext(conf)

    val rdd = sc.parallelize(List(1,2,2,3,3,4,5))
    val rlt1 = rdd.distinct()
    println(rdd.partitioner)
    println(rlt1.partitioner)

    // 4、1、3、5、2
    rlt1.foreach(println)

    val rlt2 = rdd.distinct(2)
    //4、2   |   1、3、5  两个分区
    rlt2.foreach(println)

//--------------------------
//    val mapRDD = rdd.map(x => (x, null))
//
//    val reduceRDD = mapRDD.reduceByKey((x, _) => x, 2)
//    // ArrayBuffer((4,null), (2,null), (1,null), (3,null), (5,null))
//    println(reduceRDD.collect().toBuffer)
//    println("------------")
//
//    val resRDD = reduceRDD.map(_._1)
//    // ArrayBuffer(4, 2, 1, 3, 5)
//    println(resRDD.collect().toBuffer)
  }
}
