package com.zgg.core.rdd.转换.foldbykey;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;

/**
 * 当分区内计算规则和分区间计算规则相同时，aggregateByKey 就可以简化为 foldByKey
 */
public class FoldByKeyTest01 {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("FoldByKeyTest01");
        JavaSparkContext sc = new JavaSparkContext(conf);

        List<Tuple2<String, Integer>> data = new ArrayList<Tuple2<String, Integer>>();
        data.add(new Tuple2<String, Integer>("a", 1));
        data.add(new Tuple2<String, Integer>("a", 2));
        data.add(new Tuple2<String, Integer>("b", 3));
        data.add(new Tuple2<String, Integer>("b", 4));
        data.add(new Tuple2<String, Integer>("b", 5));
        data.add(new Tuple2<String, Integer>("a", 6));
        JavaPairRDD<String, Integer> rdd = sc.parallelizePairs(data, 2);

        JavaPairRDD<String, Integer> res = rdd.foldByKey(0,
                new Function2<Integer, Integer, Integer>() {
                    @Override
                    public Integer call(Integer v1, Integer v2) throws Exception {
                        return v1 + v2;
                    }
                });

        res.collect().forEach(System.out::println);
        sc.stop();
    }
}