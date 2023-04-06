package 算子.转换.aggregateByKey

import org.apache.spark.{SparkConf, SparkContext}

object AggregateByKeyDemo {

  def func2(index:Int,iter:Iterator[Any]):Iterator[Any]={
    iter.toList.map(x=>"分区 "+index+" 的计算结果是 "+x).iterator
  }

  def main(Args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("AggregateByKeyDemo").setMaster("local")
    val sc = new SparkContext(conf)

    val data = List((1,3),(1,2),(1,4),(2,3),(2,1),(3,6),(3,8))
    val rdd = sc.parallelize(data,2)

//    rdd.mapPartitionsWithIndex(func2).foreach(println)

//    val res  = rdd.aggregateByKey(0)(math.max, _+_)
    val res  = rdd.aggregateByKey(5)(math.max, _+_)
    res.collect.foreach(println)

  }
}

/**
 * 分区 0 ： (1,3) 、(1,2)、(1,4)
 * 分区 1 ： (2,3)、(2,1)、(3,6) 、(3,8)
 *
 *  combineByKeyWithClassTag[U](
 *                             (v: V) => cleanedSeqOp(createZero(), v),
 *                             cleanedSeqOp,
 *                             combOp,
 *                             partitioner)
 * "0值":0
 * 针对底层调用的 combineByKeyWithClassTag：
 * 第一个参数函数：创建初始值
 *      对于分区0：此时就是 0 和 传来的第一个数 3 比较，取最大值，取 3，所以初始值是 3
 *      对于分区1：此时就是 0 和 传来的第一个数 3 比较，取最大值，取 3，所以初始值是 3
 *               此时就是 0 和 传来的第一个数 6 比较，取最大值，取 6，所以初始值是 6
 *
 * 第二个参数函数：分区内计算规则
 * 对分区 0：3 依次和 2 4 比较，取最大值，得到最大的是 4，所以结果是 (1,4)
 * 对分区 1：3 和 1比较，取最大值，得到最大的是 3，所以结果是 (2,3)
 *         6 和 8 比较，取最大值，得到最大的是 8，所以结果是 (3,8)
 *
 * 第三个参数函数：分区间计算规则
 * 没有相同的key 所以输出就是 (1,4)(2,3)(3,8)
 *
 *
 * 分区 0 ： (1,3) 、(1,2)、(1,4)
 * 分区 1 ： (2,3)、 (2,1)、(3,6) 、(3,8)
 * ---------------------------------------------------------------------------------
 * 如果"0值"是5
 * 第一个参数函数：创建初始值
 *      对于分区0：此时就是 5 和 传来的第一个数 3 比较，取最大值，取 5，所以初始值是 5
 *      对于分区1：此时就是 5 和 传来的第一个数 3 比较，取最大值，取 5，所以初始值是 5
 *               此时就是 5 和 传来的第一个数 6 比较，取最大值，取 6，所以初始值是 6
 *
 * 第二个参数函数：分区内计算规则
 * 对分区 0：5 依次和 2 4 比较，取最大值，得到最大的是 5，所以结果是 (1,5)
 * 对分区 1：5 和 1比较，取最大值，得到最大的是 5，所以结果是 (2,5)
 *         6 和 8 比较，取最大值，得到最大的是 8，所以结果是 (3,8)
 *
 * 第三个参数函数：分区间计算规则
 * 没有相同的key 所以输出就是 (1,5)(2,5)(3,8)
 */