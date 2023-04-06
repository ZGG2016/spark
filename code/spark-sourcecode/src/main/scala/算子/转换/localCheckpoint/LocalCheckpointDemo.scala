package 算子.转换.localCheckpoint

import org.apache.spark.{SparkConf, SparkContext}

object LocalCheckpointDemo {
  def main(Args: Array[String]): Unit = {

    val conf = new SparkConf().setAppName("LocalCheckpointDemo").setMaster("local")
    val sc = new SparkContext(conf)

    val rdd1 = sc.parallelize(List(1, 2, 3, 4, 5, 6, 7, 8, 9))
    val rdd2 = rdd1.map(x => x + 1).cache()

    rdd2.localCheckpoint()
    rdd2.collect().foreach(println)

    //    rdd2.unpersist()
    //    rdd2.collect().foreach(println)
    //抛异常：
    //Caused by: org.apache.spark.SparkException:
    // Checkpoint block rdd_1_0 not found!
    // Either the executor that originally checkpointed this partition is no longer alive,
    // or the original RDD is unpersisted.
    // If this problem persists, you may consider using `rdd.checkpoint()` instead,
    // which is slower than local checkpointing but more fault-tolerant.

    //rdd2在执行完localCheckpoint、collect后rdd2对rdd1的依赖切断并做了缓存，所以第一个collect指向成功。
    //随着rdd2.unpersist的调用，executor上的数据被清理，且此时切断了rdd1的依赖，所以再第二个collect抛异常
  }
}
