package com.zgg.core.rdd.转换.union;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 
 */
public class UnionTest02 {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("UnionTest02");
        JavaSparkContext sc = new JavaSparkContext(conf);

        List<Tuple2<String, Integer>> data1 = new ArrayList<Tuple2<String, Integer>>();
        data1.add(new Tuple2<String, Integer>("a", 1));
        data1.add(new Tuple2<String, Integer>("a", 2));
        data1.add(new Tuple2<String, Integer>("b", 3));
        JavaPairRDD<String, Integer> rdd1 = sc.parallelizePairs(data1);

        List<Tuple2<String, Integer>> data2 = new ArrayList<Tuple2<String, Integer>>();
        data2.add(new Tuple2<String, Integer>("b", 4));
        data2.add(new Tuple2<String, Integer>("b", 5));
        data2.add(new Tuple2<String, Integer>("a", 6));
        JavaPairRDD<String, Integer> rdd2 = sc.parallelizePairs(data2);

        JavaPairRDD<String, Integer> res = rdd1.union(rdd2);
        res.collect().forEach(System.out::println);

//        (a,1)
//        (a,2)
//        (b,3)
//        (b,4)
//        (b,5)
//        (a,6)

        sc.stop();
    }
}
