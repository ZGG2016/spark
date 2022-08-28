package com.zgg.core.rdd.转换.partitionby;

import org.apache.spark.HashPartitioner;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

import java.util.Arrays;
import java.util.List;
/**
 * 将数据按照指定 Partitioner 重新进行分区。Spark 默认的分区器是 HashPartitioner
 * */
public class PartitionByTest01 {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("PartitionByTest01");
        JavaSparkContext sc = new JavaSparkContext(conf);

        List<Integer> data = Arrays.asList(1, 2, 3, 4);
        JavaRDD<Integer> rdd = sc.parallelize(data, 2);

        JavaPairRDD<Integer, Integer> pairRDD = rdd.mapToPair(new PairFunction<Integer, Integer, Integer>() {
            @Override
            public Tuple2<Integer, Integer> call(Integer integer) throws Exception {
                return new Tuple2<>(integer, 1);
            }
        });

        pairRDD.partitionBy(new HashPartitioner(2)).saveAsTextFile("output");

        sc.stop();
    }
}

/*
  1. 如果重分区的分区器和当前 RDD 的分区器一样怎么办？
    if (self.partitioner == Some(partitioner)) {
      self
    } else {
      new ShuffledRDD[K, V, V](self, partitioner)
    }
    -->
    override def equals(other: Any): Boolean = other match {
        case h: HashPartitioner =>
          h.numPartitions == numPartitions
        case _ =>
          false
      }
    所以，先比较类型，如果都是HashPartitioner，那么就再比较分区数量，
         如果都相同，就返回它本身，不再new一个rdd了

 */
