package com.zgg.core.rdd.转换.union;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class UnionTest01 {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("UnionTest01");
        JavaSparkContext sc = new JavaSparkContext(conf);

        List<Integer> data1 = Arrays.asList(1, 2, 3, 4);
        List<Integer> data2 = Arrays.asList(3, 4, 5, 6);
        JavaRDD<Integer> rdd1 = sc.parallelize(data1);
        JavaRDD<Integer> rdd2 = sc.parallelize(data2);

        JavaRDD<Integer> res = rdd1.union(rdd2);
        res.collect().forEach(System.out::println);

        sc.stop();
    }
}
