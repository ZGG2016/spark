package com.zgg.core.rdd.转换.filter;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;

import java.util.Arrays;
import java.util.List;

/**
 * 练习：从服务器日志数据 apache.log 中获取 2015 年 5 月 17 日的请求路径
 * */
public class FilterTest02 {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("FilterTest01");
        JavaSparkContext sc = new JavaSparkContext(conf);

        JavaRDD<String> rdd = sc.textFile("src/main/resources/apache.log",4);
        JavaRDD<String> res = rdd.filter(new Function<String, Boolean>() {
            @Override
            public Boolean call(String v1) throws Exception {
                String[] items = v1.split(" ");
                return items[3].startsWith("17/05/2015");
            }
        });
        res.collect().forEach(System.out::println);

        sc.stop();
    }
}
