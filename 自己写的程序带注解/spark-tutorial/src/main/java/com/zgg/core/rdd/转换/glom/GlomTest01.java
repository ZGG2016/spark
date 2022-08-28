package com.zgg.core.rdd.转换.glom;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.util.Arrays;
import java.util.List;

/**
 * 将同一个分区的数据直接转换为相同类型的内存数组进行处理，分区不变
 * */
public class GlomTest01 {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("GlomTest01");
        JavaSparkContext sc = new JavaSparkContext(conf);

        List<Integer> data = Arrays.asList(1, 2, 3, 4);
        JavaRDD<Integer> rdd = sc.parallelize(data,2);

        JavaRDD<List<Integer>> res = rdd.glom();

        res.collect().forEach(System.out::println);
        sc.stop();
    }
}
