package 算子.转换.join

import org.apache.spark.{SparkConf, SparkContext}

object JoinDemo {
  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setAppName("JoinDemo").setMaster("local")
    val sc = new SparkContext(conf)

    val rdd1 = sc.parallelize(Array(("1", "Spark"), ("2", "Hadoop"), ("3", "Scala"), ("4", "Java")), 2)
    val rdd2 = sc.parallelize(Array(("1", "30K"), ("2", "15K"), ("3", "25K"), ("5", "10K")), 2)

    val rlt1 = rdd1.join(rdd2)
    //(2,(Hadoop,15K))、(3,(Scala,25K))、(1,(Spark,30K))
    rlt1.collect.foreach(println)
    println("---------join---------")

    val rlt2 = rdd1.leftOuterJoin(rdd2)
    //(4,(Java,None))、(2,(Hadoop,Some(15K)))、(3,(Scala,Some(25K)))、(1,(Spark,Some(30K)))
    rlt2.collect.foreach(println)
    println("---------leftOuterJoin---------")

    val rlt3 = rdd1.rightOuterJoin(rdd2)
    //(2,(Some(Hadoop),15K))、(5,(None,10K))、(3,(Some(Scala),25K))、(1,(Some(Spark),30K))
    rlt3.collect.foreach(println)
    println("---------rightOuterJoin---------")


    val rlt4 = rdd1.fullOuterJoin(rdd2)
    //(4,(Some(Java),None))、(2,(Some(Hadoop),Some(15K)))、(5,(None,Some(10K)))、(3,(Some(Scala),Some(25K)))、(1,(Some(Spark),Some(30K)))
    rlt4.collect.foreach(println)
    println("---------fullOuterJoin---------")

    val rlt5 = rdd1.join(rdd2,2)
    //(2,(Hadoop,15K))、(3,(Scala,25K))、(1,(Spark,30K))
    rlt5.collect.foreach(println)
    println("---------指定分区数、join---------")

  }
}
