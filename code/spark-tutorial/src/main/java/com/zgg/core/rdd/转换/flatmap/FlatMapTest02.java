package com.zgg.core.rdd.转换.flatmap;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;

import java.util.*;

/**
 * 练习：划分单词
 * */
public class FlatMapTest02 {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("FlatMapTest02");
        JavaSparkContext sc = new JavaSparkContext(conf);

        List<String> data = Arrays.asList("aa-bb","cc-dd");
        JavaRDD<String> rdd = sc.parallelize(data);

        JavaRDD<String> res1 = rdd.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public Iterator<String> call(String s) throws Exception {
                String[] words = s.split("-");
                return Arrays.asList(words).iterator();
            }
        });
        res1.collect().forEach(System.out::println);
        sc.stop();
    }
}
