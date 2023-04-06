package 算子.转换.checkpoint

import org.apache.spark.{SparkConf, SparkContext}

object CheckpointDemo {
  def main(Args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("CheckpointDemo").setMaster("local")
    val sc = new SparkContext(conf)

    sc.setCheckpointDir("cp")

    val rdd = sc.parallelize(List(1, 2, 3, 4, 5, 6, 7, 8, 9))
    val res = rdd.map(x => x + 1).cache()
//    为了保证数据安全，所以一般情况下，会独立执行作业
//    为了能够提高效率，一般情况下，是需要和cache联合使用
    res.checkpoint()
    res.collect().foreach(println)
  }
}
