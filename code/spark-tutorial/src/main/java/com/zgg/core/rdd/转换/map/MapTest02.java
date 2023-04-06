package com.zgg.core.rdd.转换.map;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;

/**
 * 练习：从服务器日志数据 apache.log 中获取用户请求 URL 资源路径
 * */
public class MapTest02 {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("MapTest02");
        JavaSparkContext sc = new JavaSparkContext(conf);

        JavaRDD<String> rdd = sc.textFile("src/main/resources/apache.log");
        JavaRDD<String> res1 = rdd.map(new Function<String, String>() {
            @Override
            public String call(String line) throws Exception {
                String[] contentArr = line.split(" ");
                return contentArr[6];
            }
        });
        res1.collect().forEach(System.out::println);

        sc.stop();
    }
}
