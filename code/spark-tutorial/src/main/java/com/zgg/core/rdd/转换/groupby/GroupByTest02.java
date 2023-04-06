package com.zgg.core.rdd.转换.groupby;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;

import java.util.Arrays;
import java.util.List;

public class GroupByTest02 {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("GroupByTest02");
        JavaSparkContext sc = new JavaSparkContext(conf);

        List<String> data = Arrays.asList("Spark", "Hive", "Scala", "Hadoop","Flink");
        JavaRDD<String> rdd = sc.parallelize(data, 2);

        JavaPairRDD<String, Iterable<String>> res = rdd.groupBy(new Function<String, String>() {
            @Override
            public String call(String v1) throws Exception {
                if (v1.startsWith("S")){
                    return "s-group";
                }else if (v1.startsWith("H")) {
                    return "h-group";
                }
                return "unknown-group";
            }
        });

        res.collect().forEach(System.out::println);

        sc.stop();
    }
}
