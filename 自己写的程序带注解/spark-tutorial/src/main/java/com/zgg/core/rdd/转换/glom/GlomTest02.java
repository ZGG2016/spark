package com.zgg.core.rdd.转换.glom;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 练习：计算所有分区最大值求和（分区内取最大值，分区间最大值求和）
 * */
public class GlomTest02 {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("GlomTest01");
        JavaSparkContext sc = new JavaSparkContext(conf);

        List<Integer> data = Arrays.asList(1, 2, 3, 4);
        JavaRDD<Integer> rdd = sc.parallelize(data,2);

        JavaRDD<List<Integer>> glomRDD = rdd.glom();
        JavaRDD<Integer> mapRDD = glomRDD.map(new Function<List<Integer>, Integer>() {
            @Override
            public Integer call(List<Integer> v1) throws Exception {
                return Collections.max(v1);
            }
        });
        int res = mapRDD.collect().stream().mapToInt(e -> e).sum();

        System.out.println(res);
        sc.stop();
    }
}
