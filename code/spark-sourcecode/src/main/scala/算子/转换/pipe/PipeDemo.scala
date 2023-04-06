package 算子.转换.pipe

import org.apache.spark.{SparkConf, SparkContext}

object PipeDemo {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("PipeDemo").setMaster("local")
    val sc = new SparkContext(sparkConf)

    val data = List("hi", "hello", "how", "are", "you")
    val dataRDD = sc.parallelize(data)

    val scriptPath = "src/main/resources/echo.sh"
    val pipeRDD = dataRDD.pipe("C:\\Users\\zgg\\Desktop\\spark-sourcecode\\src\\main\\resources\\echo.sh")

    print(pipeRDD.collect().toBuffer)
    sc.stop()

  }
}