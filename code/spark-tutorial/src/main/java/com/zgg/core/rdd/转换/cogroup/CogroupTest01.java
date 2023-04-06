package com.zgg.core.rdd.转换.cogroup;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.Optional;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;

/**
 * 在类型为(K,V)和(K,W)的 RDD 上调用，返回一个(K,(Iterable<V>,Iterable<W>))类型的 RDD
 */
public class CogroupTest01 {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("CogroupTest01");
        JavaSparkContext sc = new JavaSparkContext(conf);

        List<Tuple2<String, Integer>> data1 = new ArrayList<Tuple2<String, Integer>>();
        data1.add(new Tuple2<String, Integer>("a", 1));
        data1.add(new Tuple2<String, Integer>("b", 2));
//        data1.add(new Tuple2<String, Integer>("c", 3));
        JavaPairRDD<String, Integer> rdd1 = sc.parallelizePairs(data1, 2);

        List<Tuple2<String, Integer>> data2 = new ArrayList<Tuple2<String, Integer>>();
        data2.add(new Tuple2<String, Integer>("a", 4));
        data2.add(new Tuple2<String, Integer>("b", 6));
        data2.add(new Tuple2<String, Integer>("c", 6));
        data2.add(new Tuple2<String, Integer>("c", 6));
        JavaPairRDD<String, Integer> rdd2 = sc.parallelizePairs(data2, 2);
        /*
           cogroup : connect + group (分组，连接)
           输出：
                (b,([2],[6]))
                (a,([1],[4]))
                (c,([],[6, 6]))

           可能存在shuffle
         */
        JavaPairRDD<String, Tuple2<Iterable<Integer>, Iterable<Integer>>> res = rdd1.cogroup(rdd2);

        res.collect().forEach(System.out::println);
        sc.stop();
    }
}