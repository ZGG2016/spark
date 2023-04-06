package 算子.转换.glom

import org.apache.spark.{SparkConf, SparkContext}

object GlomDemo {
  def main(Args:Array[String]):Unit = {
    val conf = new SparkConf().setAppName("GlomDemo").setMaster("local")
    val sc = new SparkContext(conf)
    val rdd = sc.parallelize(List(1, 2, 2, 2, 5, 6, 8, 8, 8, 8),2)

    val rlt = rdd.glom()

    rlt.collect().foreach(x =>{
      x.foreach(println)
      println("------------------")
    })

  }
}
/**
 * 1
 * 2
 * 2
 * 2
 * 5
 * ------------------
 * 6
 * 8
 * 8
 * 8
 * 8
 * ------------------
 */