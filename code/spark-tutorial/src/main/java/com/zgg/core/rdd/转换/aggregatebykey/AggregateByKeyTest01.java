package com.zgg.core.rdd.转换.aggregatebykey;

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
import java.util.Random;

/**
 * reduceByKey是分区内和分区间的聚合逻辑相同，而AggregateByKey分区内和分区间的聚合逻辑不同
 * 而且reduceByKey
 *
 * 将数据根据不同的规则进行分区内计算和分区间计算
 * */
public class AggregateByKeyTest01 {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("AggregateByKeyTest01");
        JavaSparkContext sc = new JavaSparkContext(conf);

        List<Tuple2<String,Integer>> data = new ArrayList<Tuple2<String,Integer>>();
        data.add(new Tuple2<String,Integer>("a",1));
        data.add(new Tuple2<String,Integer>("a",2));
        data.add(new Tuple2<String,Integer>("b",3));
        data.add(new Tuple2<String,Integer>("b",4));
        data.add(new Tuple2<String,Integer>("b",5));
        data.add(new Tuple2<String,Integer>("a",6));
        JavaPairRDD<String, Integer> rdd = sc.parallelizePairs(data, 2);
        /*
            第一个参数列表,需要传递一个参数，表示为初始值
                主要用于当碰见第一个key的时候，和value进行分区内计算
            第二个参数列表需要传递2个参数
                第一个参数表示分区内计算规则
                第二个参数表示分区间计算规则
         */
        JavaPairRDD<String, Integer> res = rdd.aggregateByKey(0,
                new Function2<Integer, Integer, Integer>() {
                    @Override
                    public Integer call(Integer v1, Integer v2) throws Exception {
                        return Math.max(v1, v2);
                    }
                },
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