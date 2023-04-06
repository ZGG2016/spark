package 算子.行动.aggregate

import org.apache.spark.{SparkConf, SparkContext}

object AggregateDemo {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("AggregateDemo").setMaster("local")
    val sc = new SparkContext(conf)

    val data = List(1, 2, 3, 4, 5, 6, 7, 8, 9)
    val rdd = sc.parallelize(data, 2)

    val res = rdd
      .aggregate((0, 0))(
        (acc, number) => (acc._1 + number, acc._2 + 1),
        (par1, par2) => (par1._1 + par2._1, par1._2 + par2._2)
      )
    println(res) //(45,9)
  }
}

/**
 * 分区0：1,2,3,4
 * 分区1：5,6,7,8,9
 * 【初始0值不仅用在每个分区内的累积，也是在合并分区间结果时的初始值】
 * acc是(0,0),number是data。
 * 1、seqOp操作：foreach (x => result = op(result, x))
 * 对分区0：
 * 0+1,0+1  -->  1,1
 * 1+2,1+1  -->  3,2
 * 3+3,2+1  -->  6,3
 * 6+4,3+1  -->  10,4
 *
 * 对分区1：
 * 0+5,0+1  --> 5,1
 * 5+6,1+1  --> 11,2
 * 11+7,2+1 --> 18,3
 * 18+8,3+1 --> 26,4
 * 26+9,4+1 --> 35,5
 *
 * 2、combOp操作：jobResult = combOp(jobResult, taskResult)
 *
 * 0,0 + 10,4 + 35,5 == 45,9
 *
 * */