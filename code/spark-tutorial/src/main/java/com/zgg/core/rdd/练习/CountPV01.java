package com.zgg.core.rdd.练习;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

import java.util.*;

/**
 * 需求：统计出每一个省份 每个广告被点击数量排行的 Top3
 * 数据结构：时间戳，省份，城市，用户，广告，中间字段使用空格分隔 (agent.log)
 */
public class CountPV01 {
    public static void main(String[] args) {
        SparkConf sparkConf = new SparkConf().setAppName("CountPV01").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);

        JavaRDD<String> rdd = sc.textFile("src/main/resources/agent.log");
        // 1. 计算某个省份下的某个广告的点击量
        // 1.1 将 省份+广告 封装成一个key
        JavaPairRDD<Tuple2<String, String>, Integer> pairRDD = rdd.mapToPair(new PairFunction<String, Tuple2<String, String>, Integer>() {
            @Override
            public Tuple2<Tuple2<String, String>, Integer> call(String s) throws Exception {
                String[] items = s.split(" ");
                Tuple2<String, String> innerTuple = new Tuple2<>(items[1], items[4]);
                return new Tuple2<>(innerTuple, 1);
            }
        });

        // 1.2 以 省份+广告 为key进行聚合
        JavaPairRDD<Tuple2<String, String>, Integer> reduceRDD = pairRDD.reduceByKey(Integer::sum);

        // 2. 按省份分组，就得到了这个省份下的每个广告的点击量
        // 2.1 转换数据结构，省份为key，广告+聚合值为value
        JavaPairRDD<String, Tuple2<String, Integer>> pairRDD2 = reduceRDD.mapToPair(new PairFunction<Tuple2<Tuple2<String, String>, Integer>, String, Tuple2<String, Integer>>() {
            @Override
            public Tuple2<String, Tuple2<String, Integer>> call(Tuple2<Tuple2<String, String>, Integer> t) throws Exception {
                Tuple2<String, Integer> innerTuple = new Tuple2<>(t._1._2, t._2);
                return new Tuple2<>(t._1._1, innerTuple);
            }
        });

        // 2.1 按省份分组
        JavaPairRDD<String, Iterable<Tuple2<String, Integer>>> groupRDD = pairRDD2.groupByKey();

        // 3. 排序（降序），取前3名
        JavaPairRDD<String, ArrayList<Tuple2<String, Integer>>> res = groupRDD.mapValues(new Function<Iterable<Tuple2<String, Integer>>, ArrayList<Tuple2<String, Integer>>>() {
            @Override
            public ArrayList<Tuple2<String, Integer>> call(Iterable<Tuple2<String, Integer>> v1) throws Exception {
                TreeSet<Tuple2<String, Integer>> tmpSet = new TreeSet<>((o1, o2) -> o2._2.compareTo(o1._2));
                // 利用 TreeSet 的排序功能
                for (Tuple2<String, Integer> stringIntegerTuple2 : v1) {
                    tmpSet.add(stringIntegerTuple2);
                }

                ArrayList<Tuple2<String, Integer>> resList = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    resList.add(tmpSet.pollFirst());
                }
                return resList;
            }
        });
        res.collect().forEach(System.out::println);

        sc.stop();
    }
}
