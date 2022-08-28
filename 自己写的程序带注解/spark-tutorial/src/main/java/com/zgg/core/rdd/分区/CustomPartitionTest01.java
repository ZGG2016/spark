package com.zgg.core.rdd.分区;

import org.apache.spark.Partitioner;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class CustomPartitionTest01 {
    public static void main(String[] args) {
        SparkConf sparkConf = new SparkConf().setAppName("CustomPartitionTest01").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);

        List<Tuple2<String, Integer>> data = new ArrayList<Tuple2<String, Integer>>();
        data.add(new Tuple2<String, Integer>("a", 1));
        data.add(new Tuple2<String, Integer>("a", 2));
        data.add(new Tuple2<String, Integer>("b", 3));
        data.add(new Tuple2<String, Integer>("b", 4));
        data.add(new Tuple2<String, Integer>("b", 5));
        data.add(new Tuple2<String, Integer>("a", 6));
        JavaPairRDD<String, Integer> rdd = sc.parallelizePairs(data);
        JavaPairRDD<String, Integer> res = rdd.partitionBy(new MyPartitioner());

        res.saveAsTextFile("output");
    }
    /**
     * 自定义分区器
     * 1. 继承Partitioner
     * 2. 重写方法
     */
    static class MyPartitioner extends Partitioner{

        // 分区数量
        @Override
        public int numPartitions() {
            return 2;
        }

        // 根据数据的key值返回数据所在的分区索引（从0开始）
        @Override
        public int getPartition(Object key) {
            if (key.equals("a")){
                return 0;
            }else{
                return 1;
            }
        }
    }
}
