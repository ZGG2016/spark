package 算子.转换.union

import org.apache.spark.{SparkConf, SparkContext}

object UnionDemo {
  def main(Args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("UnionDemo").setMaster("local")
    val sc = new SparkContext(conf)

    val rdd1 = sc.parallelize(List(('a', 1), ('b', 1)))
    val rdd2 = sc.parallelize(List(('c', 1), ('d', 1), ('a', 1)))

    //(a,1)(b,1)(c,1)(d,1)(a,1)
    val rlt1 = rdd1.union(rdd2)
    rlt1.collect.foreach(println)
    println("--------------------union不去重--------------------")

    //(d,1)(b,1)(a,1)(c,1)
    val rlt2 = rdd1.union(rdd2).distinct()
    rlt2.collect.foreach(println)
    println("--------------------union去重--------------------")

    //(a,1)(b,1)(c,1)(d,1)(a,1)
    val rlt3 = rdd1.++(rdd2)
    rlt3.collect().foreach(println)
    println("--------------------++不去重--------------------")

  }
}
