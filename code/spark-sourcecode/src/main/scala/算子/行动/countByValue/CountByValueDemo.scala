package 算子.行动.countByValue

import org.apache.spark.{SparkConf, SparkContext}

object CountByValueDemo {

  def main(Args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("countByValue").setMaster("local")
    val sc = new SparkContext(conf)

    val rdd1 = sc.parallelize(List(1,1,2,2,2,1,4,5))
    val rlt1 = rdd1.countByValue()  //本身就会collect到driver
    println(rlt1) // Map(4 -> 1, 1 -> 3, 5 -> 1, 2 -> 3)
    println("-----------------------")

    val rdd2 = sc.parallelize(List(("a", 1), ("a", 1),("a", 7),("b", 2), ("b", 3)), 2)
    val rlt2 = rdd2.countByValue()
    println(rlt2) // Map((b,2) -> 1, (a,7) -> 1, (b,3) -> 1, (a,1) -> 2)
    println("-----------------------")

    val rlt3 = rdd2.map(value => (value, null))
    // ((a,1),null)   ((a,1),null)  ((a,7),null)  ((b,2),null)  ((b,3),null)
    rlt3.collect().foreach(println)

  }
}
