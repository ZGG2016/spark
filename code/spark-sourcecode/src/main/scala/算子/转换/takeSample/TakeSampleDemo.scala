package 算子.转换.takeSample

import org.apache.spark.{SparkConf, SparkContext}

object TakeSampleDemo {
  def main(Args:Array[String]):Unit = {
    val conf = new SparkConf().setAppName("TakeSampleDemo").setMaster("local")
    val sc = new SparkContext(conf)

    val rdd = sc.parallelize(List(1, 2, 2, 2, 5, 6, 8, 8, 8, 8))

    //给定参数：是否是有放回抽样、抽样返回的样本的个数，随机生成器种子
    //Return a fixed-size sampled subset of this RDD in an array  数组
    val rlt = rdd.takeSample(withReplacement = false,4,10)
    rlt.foreach(println)  //8、6、8、2
  }
}
