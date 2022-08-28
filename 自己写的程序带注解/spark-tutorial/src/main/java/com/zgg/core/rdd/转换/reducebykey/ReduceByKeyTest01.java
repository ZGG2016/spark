package com.zgg.core.rdd.转换.reducebykey;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**
 * 将数据按照相同的 Key 对 Value 进行聚合
 * */
public class ReduceByKeyTest01 {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("ReduceByKeyTest01");
        JavaSparkContext sc = new JavaSparkContext(conf);

        List<String> data = Arrays.asList("a", "a", "b", "c", "b");
        JavaRDD<String> rdd = sc.parallelize(data);

        JavaPairRDD<String, Integer> pairRDD = rdd.mapToPair(new PairFunction<String, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(String s) throws Exception {
                return new Tuple2<>(s, 1);
            }
        });
        // reduceByKey中如果key的数据只有一个，是不会参与运算的
        JavaPairRDD<String, Integer> res = pairRDD.reduceByKey(new Function2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer v1, Integer v2) throws Exception {
                return v1 + v2;
            }
        });

        res.collect().forEach(System.out::println);
        sc.stop();
    }
}
