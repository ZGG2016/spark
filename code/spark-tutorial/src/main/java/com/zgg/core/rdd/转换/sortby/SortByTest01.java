package com.zgg.core.rdd.转换.sortby;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;

import java.util.Arrays;
import java.util.List;

/**
 * 该操作用于排序数据。
 * 在排序之前，可以将数据通过 f 函数进行处理，之后按照 f 函数处理的结果进行排序，默认为升序排列。
 * 排序后新产生的 RDD 的分区数与原 RDD 的分区数一致（如果不重置分区）。
 * 中间存在 shuffle 的过程
 */
public class SortByTest01 {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("SortByTest01");
        JavaSparkContext sc = new JavaSparkContext(conf);

        List<Integer> data = Arrays.asList(7, 3, 4, 1, 2, 5);
        JavaRDD<Integer> rdd = sc.parallelize(data,2);
        // Return this RDD sorted by the given key function.
        JavaRDD<Integer> res = rdd.sortBy(new Function<Integer, Integer>() {
            @Override
            public Integer call(Integer v1) throws Exception {
                return v1;
            }
        },false,4);

        res.saveAsTextFile("output");
        sc.stop();
    }
}
