package 算子.行动.fold

import org.apache.spark.{SparkConf, SparkContext}

object FoldDemo {
  def main(Args:Array[String]):Unit = {
    val conf = new SparkConf().setAppName("FoldDemo").setMaster("local")
    val sc = new SparkContext(conf)

    val data = List(1, 2, 3, 4, 5, 6, 7, 8, 9)
    val rdd = sc.parallelize(data, 2)

    val rlt = rdd.fold(2)(_+_)

    println(rlt)

  }
}

/**
 * 分区0：1,2,3,4
 * 分区1：5,6,7,8,9
 * 【初始0值不仅用在每个分区内的累积，也是在合并分区间结果时的初始值】
 * zeroValue=2
 * 1、分区内操作  (iter: Iterator[T]) => iter.fold(zeroValue)(cleanOp)
 * 对分区0：
 * 2 + 1+2+3+4 = 12
 *
 * 对分区1：
 * 2 + 5+6+7+8+9 = 37
 *
 * 2、分区间操作 (_: Int, taskResult: T) => jobResult = op(jobResult, taskResult)
 *
 * 2 + 12 + 37 = 51
 *
 * */
