package 算子.转换.foldByKey

import org.apache.spark.{SparkConf, SparkContext}

object FoldByKeyDemo {
  def func2(index:Int,iter:Iterator[Any]):Iterator[Any]={
    iter.toList.map(x=>"分区 "+index+" 的计算结果是 "+x).iterator
  }

  def main(Args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("groupByKey").setMaster("local")
    val sc = new SparkContext(conf)

    val rdd = sc.parallelize(List(("a", 1), ("a", 6),("a", 7),("b", 5), ("b", 3)), 2)

    //rdd.mapPartitionsWithIndex(func2).foreach(println)

    val rlt = rdd.foldByKey(0)(_ + _)

    println(rlt.collect().toList) // List((b,8), (a,14))
  }
}

/**
 * 分区0：("a", 1), ("a", 6)
 * 分区1：("a", 7),("b", 5), ("b", 3)
 *
 * "0值": 0   【创建初始值】
 *  对分区0：  【分区内计算】
 *         0+1=1 --> 1+6=7  --> ("a",7)
 *  对分区1：
 *         0+7=7 -->  ("a",7)
 *         0+5=5  5+3=8  --> ("b",8)
 *
 * 【分区间计算】
 *  ("a",7+7=14)   -->  ("a",14)
 *  ("b",8)       -->  ("b",8)
 *
 * */