package 算子.转换.saveAsHadoopFile


import org.apache.spark.{SparkConf, SparkContext}

object SaveAsHadoopFileDemo {
  def main(Args:Array[String]): Unit = {
    val conf = new SparkConf().setAppName("saveAsHadoopFile").setMaster("spark://zgg:7077")
    val sc = new SparkContext(conf)

    val data = Array(("a","2"),("a","1"),("b","5"),("b","3"),("b","9"))
    val rdd = sc.parallelize(data,2)

    val outPath = "hdfs://bigdata101:9000/out/saveAsHadoopFile"
    rdd.saveAsHadoopFile(outPath)
  }
}
