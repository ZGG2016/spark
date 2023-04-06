package 算子.转换.randomSplit

import org.apache.spark.{SparkConf, SparkContext}

object RandomSplitDemo {
  def main(Args:Array[String]):Unit = {
    val conf = new SparkConf().setAppName("RandomSplitDemo").setMaster("local")
    val sc = new SparkContext(conf)

    val rdd = sc.parallelize(List(1, 2, 2, 2, 5, 6, 8, 8, 8, 8))

    val rlt = rdd.randomSplit(Array(1.0,2.0,3.0,4.0),10) //返回值类型：Array[RDD[T]]

    println(rlt.length)  //4  ，划分出了4个RDD，组成一个数组  （每个权重对应一个RDD）

    for(x <- rlt){
      x.collect().foreach(println)
      println()
      println("---------------------")
    }

  }
}
