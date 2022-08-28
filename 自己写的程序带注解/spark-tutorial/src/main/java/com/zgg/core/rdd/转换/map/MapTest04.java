package com.zgg.core.rdd.转换.map;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;

import java.util.Arrays;
import java.util.List;

/**
 * 理解分区不变：
 *    - 分区0中的数据，经map转换后，还是在分区0
 * */
public class MapTest04 {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("MapTest04");
        JavaSparkContext sc = new JavaSparkContext(conf);

        List<Integer> data = Arrays.asList(1, 2, 3, 4);
        JavaRDD<Integer> rdd1 = sc.parallelize(data,2);
        rdd1.saveAsTextFile("output1");

        JavaRDD<Integer> rdd2 = rdd1.map(new Function<Integer, Integer>() {
            @Override
            public Integer call(Integer v1) throws Exception {
                return v1 * 2;
            }
        });
        rdd2.saveAsTextFile("output2");

        sc.stop();
    }
}
