package com.zgg.core.rdd.转换.flatmap;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;

import java.util.*;

/**
 * 将处理的数据进行扁平化后再进行映射处理，所以算子也称之为扁平映射
 * */
public class FlatMapTest01 {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("FlatMapTest01");
        JavaSparkContext sc = new JavaSparkContext(conf);

        List<List<Integer>> data = new ArrayList<>();
        data.add(Arrays.asList(1,2));
        data.add(Arrays.asList(3,4));
        JavaRDD<List<Integer>> rdd = sc.parallelize(data);
        /*
        * 为每个元素调用一次 FlatMapFunction
        * */
        JavaRDD<Integer> res1 = rdd.flatMap(new FlatMapFunction<List<Integer>, Integer>() {
            @Override
            public Iterator<Integer> call(List<Integer> items) throws Exception {
                System.out.println(">>>>>>>>>>>>");
                return items.iterator();
            }
        });
        res1.collect().forEach(System.out::println);
        sc.stop();
    }
}
