package 算子.转换.zipPartitions

import org.apache.spark.{SparkConf, SparkContext}

object ZipPartitionsDemo {
  def main(Args:Array[String]):Unit = {
    val conf = new SparkConf().setAppName("ZipPartitionsDemo").setMaster("local")
    val sc = new SparkContext(conf)

    val rdd1 = sc.parallelize(List(1, 2, 3,4),2)
    val rdd2 = sc.parallelize(List(5, 6, 7, 8),2)

    val prlt = rdd1.zipPartitions(rdd2){
      (iter1,iter2) => {
        var rlt = List[String]()
        while(iter1.hasNext && iter2.hasNext){
          rlt::=(iter1.next() + "_" + iter2.next())
        }
        rlt.iterator
      }
    }

    /**
     * 2_6
     * 1_5
     * 4_8
     * 3_7
     */
    prlt.collect.foreach(println)

  }
}
