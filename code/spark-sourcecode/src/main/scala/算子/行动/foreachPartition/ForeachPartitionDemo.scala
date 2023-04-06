package 算子.行动.foreachPartition

import org.apache.spark.{SparkConf, SparkContext}

object ForeachPartitionDemo {
  def main(Args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("ForeachDemo").setMaster("local")
    val sc = new SparkContext(conf)

    val rdd = sc.parallelize(List("ab", "cd"), 2)

    //ab cd
    rdd.foreachPartition(
      x => {
        x.foreach(println)
      })

    sc.stop()
  }
}
