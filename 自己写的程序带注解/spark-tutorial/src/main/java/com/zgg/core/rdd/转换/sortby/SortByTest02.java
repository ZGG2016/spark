package com.zgg.core.rdd.转换.sortby;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
// 排序二元组
public class SortByTest02 {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("SortByTest02");
        JavaSparkContext sc = new JavaSparkContext(conf);

        List<Tuple2<String,Integer>> data = new ArrayList<Tuple2<String,Integer>>();
        data.add(new Tuple2<String,Integer>("b",2));
        data.add(new Tuple2<String,Integer>("a",1));
        JavaRDD<Tuple2<String,Integer>> rdd = sc.parallelize(data,2);

        JavaRDD<Tuple2<String, Integer>> res = rdd.sortBy(new Function<Tuple2<String, Integer>, String>() {
            @Override
            public String call(Tuple2<String, Integer> v1) throws Exception {
                return v1._1;
            }
        }, true, 2);

        res.collect().forEach(System.out::println);
        sc.stop();
    }
}
