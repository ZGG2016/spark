package com.zgg.core.rdd.练习;

import org.apache.commons.collections.IteratorUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;

/**
 * 需求：统计出每一个省份 每个广告被点击数量排行的 Top3
 * 数据结构：时间戳，省份，城市，用户，广告，中间字段使用空格分隔 (agent.log)
 *
 * 方法二：聚合后就排序，而方法一将数据都加载到内存中排序了
 */
public class CountPV02 {
    public static void main(String[] args) {
        SparkConf sparkConf = new SparkConf().setAppName("CountPV02").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);

        JavaRDD<String> rdd = sc.textFile("src/main/resources/agent.log");
        // 1. 计算某个省份下的某个广告的点击量
        // 1.1 将 省份+广告 封装成一个key
        JavaPairRDD<String, Integer> pairRDD = rdd.mapToPair(new PairFunction<String, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(String s) throws Exception {
                String[] items = s.split(" ");
                return new Tuple2<>(items[1] + "-" + items[4], 1);
            }
        });

        // 1.2 以 省份+广告 为key进行聚合
        JavaPairRDD<String, Integer> reduceRDD = pairRDD.reduceByKey(Integer::sum);

        // 1.3 转换数据结构，并排序
        JavaRDD<String> sortRDD = reduceRDD.map(new Function<Tuple2<String, Integer>, String>() {
            @Override
            public String call(Tuple2<String, Integer> v1) throws Exception {
                return v1._1 + "-" + String.valueOf(v1._2);
            }
        }).sortBy(new Function<String, Integer>() {
            @Override
            public Integer call(String v1) throws Exception {
                // 这里不转整型，就是按字符串的自然顺序排序了，就错误了
                return Integer.valueOf(v1.split("-")[2]);
            }
        }, false, 1);

        // 2. 按省份分组，就得到了这个省份下的每个广告的点击量
        // 2.1 转换数据结构，省份为key，广告+聚合值为value
        JavaPairRDD<String, String> pairRDD2 = sortRDD.mapToPair(new PairFunction<String, String, String>() {
            @Override
            public Tuple2<String, String> call(String s) throws Exception {
                String[] splits = s.split("-");
                return new Tuple2<>(splits[0], splits[1] + "-" + splits[2]);
            }
        });

        // 2.1 按省份分组
        JavaPairRDD<String, Iterable<String>> groupRDD = pairRDD2.groupByKey();

        // 3. 排序（降序），取前3名
        JavaPairRDD<String, ArrayList<String>> res = groupRDD.mapValues(new Function<Iterable<String>, ArrayList<String>>() {
            @Override
            public ArrayList<String> call(Iterable<String> v1) throws Exception {
                List<String> list = IteratorUtils.toList(v1.iterator());
                ArrayList<String> resList = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    resList.add(list.get(i));
                }
                return resList;
            }
        });
        res.collect().forEach(System.out::println);

        sc.stop();
    }
}
