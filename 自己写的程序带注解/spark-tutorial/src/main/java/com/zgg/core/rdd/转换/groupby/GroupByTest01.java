package com.zgg.core.rdd.转换.groupby;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;

import java.util.Arrays;
import java.util.List;
/**
 * 将数据根据指定的规则进行分组, 分区默认不变，但是数据会被打乱重新组合，
 * 我们将这样的操作称之为 shuffle。
 *
 * 极限情况下，数据可能被分在同一个分区中
 * 一个组的数据在一个分区中，但是并不是说一个分区中只有一个组   （分组和分区没有必然的关系）
 * */
public class GroupByTest01 {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("GroupByTest01");
        JavaSparkContext sc = new JavaSparkContext(conf);

        List<Integer> data = Arrays.asList(1, 2, 3, 4);
        JavaRDD<Integer> rdd = sc.parallelize(data, 2);
        // 每组都是由 a key and a sequence of elements mapping to that key
        // new Function<Integer, Integer> 第一个泛型是传入元素的类型，第二个泛型是分组的key的类型
        JavaPairRDD<Integer, Iterable<Integer>> res = rdd.groupBy(new Function<Integer, Integer>() {
            @Override
            public Integer call(Integer v1) throws Exception {
                if (v1 % 2 == 0) {
                    return 1; // 返回的是分组的key
                }
                return 0;
            }
        });

        res.collect().forEach(System.out::println);

        sc.stop();
    }
}
