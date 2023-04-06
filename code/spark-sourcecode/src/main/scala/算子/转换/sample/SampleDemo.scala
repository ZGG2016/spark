package 算子.转换.sample

import org.apache.spark.{SparkConf, SparkContext}

object SampleDemo {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("SampleDemo").setMaster("local")
    val sc = new SparkContext(conf)

    val data = sc.parallelize(1 to 10)
    // 无放回抽样，抽样比例：0.2  ，随机数生成器种子：100
    val rlt = data.sample(withReplacement = false,0.2,100).collect()

    rlt.foreach(println)  //4、5、16、19

  }
}
