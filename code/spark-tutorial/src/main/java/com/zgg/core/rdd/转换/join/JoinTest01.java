package com.zgg.core.rdd.转换.join;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.Optional;
import scala.Tuple2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * join: 在类型为(K,V)和(K,W)的 RDD 上调用，返回一个相同 key 对应的所有元素连接在一起的 (K,(V,W))的 RDD
 */
public class JoinTest01 {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("JoinTest01");
        JavaSparkContext sc = new JavaSparkContext(conf);

        List<Tuple2<String, Integer>> data1 = new ArrayList<Tuple2<String, Integer>>();
        data1.add(new Tuple2<String, Integer>("a", 1));
        data1.add(new Tuple2<String, Integer>("a", 2));
        data1.add(new Tuple2<String, Integer>("b", 3));
        JavaPairRDD<String, Integer> rdd1 = sc.parallelizePairs(data1, 2);

        List<Tuple2<String, Integer>> data2 = new ArrayList<Tuple2<String, Integer>>();
        data2.add(new Tuple2<String, Integer>("a", 5));
        data2.add(new Tuple2<String, Integer>("c", 6));
        data2.add(new Tuple2<String, Integer>("b", 4));
        JavaPairRDD<String, Integer> rdd2 = sc.parallelizePairs(data2, 2);
        /*
          join : 两个不同数据源的数据，相同的key的value会连接在一起，形成元组
                 如果两个数据源中key没有匹配上，那么数据不会出现在结果中
                 如果两个数据源中key有多个相同的，会依次匹配，可能会出现笛卡尔乘积，数据量会几何性增长，会导致性能降低。
         */
        JavaPairRDD<String, Tuple2<Integer, Integer>> res1 = rdd1.join(rdd2);
        JavaPairRDD<String, Tuple2<Integer, Optional<Integer>>> res2 = rdd1.leftOuterJoin(rdd2);
        JavaPairRDD<String, Tuple2<Optional<Integer>, Integer>> res3 = rdd1.rightOuterJoin(rdd2);

        res1.collect().forEach(System.out::println);
        res2.collect().forEach(System.out::println);
        res3.collect().forEach(System.out::println);
        sc.stop();
    }
}