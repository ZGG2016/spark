package 算子.转换.combinebykey

import org.apache.spark.{SparkConf, SparkContext}

object CombineByKeyDemo {

  def main(Args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("CombineByKeyDemo").setMaster("local")

    val sc = new SparkContext(conf)

    val data = List((1, 3), (1, 2), (1, 4), (2, 5), (3, 6), (3, 8))
    val rdd = sc.parallelize(data, 2)

    val rlt = rdd.combineByKey(
      (v: Int) => (v, 0),
      (acc: (Int, Int), v: Int) => (acc._1 + v, acc._2 + v),
      (r1: (Int, Int), r2: (Int, Int)) => (r1._1 + r2._1, r1._2 + r2._2)
    )
    //  (2,(5,0))  (1,(9,6))  (3,(14,8))
    rlt.collect().foreach(println)
  }
}

/**
 * 分区 0 ： (1,3)、(1,2)、(1,4)
 * 分区 1 ： (2,5)、(3,6)、(3,8)
 *
 * 1.createCombiner: 创建聚合的初始值的函数
 *   (v:Int) => (v,0)  【将传来的第一个数据的 v 应用于这个函数】
 * 2.mergeValue:
 *   (acc:(Int,Int),v:Int) => (acc._1+v,acc._2+v), 【分区内计算规则】
 * 3.mergeCombiners:
 *   (r1:(Int,Int),r2:(Int,Int)) => (r1._1+r2._1,r1._2+r2._2) 【分区间计算规则】
 *
 * 对于分区0：
 *    初始值: (3,0)
 *    分区内: (3,0) + (2,2) --> (5,2)    (5,2) + (4,4) --> (9,6)
 *
 * 对于分区1：
 *    初始值: (5,0)
 *           (6,0)
 *    分区内: (5,0)
 *           (6,0) + (8,8) --> (14,8)
 *
 * 分区间：(9,6) (5,0) (14,8)
 * 所以，结果是  (1,(9,6))  (2,(5,0))  (3,(14,8))
 */