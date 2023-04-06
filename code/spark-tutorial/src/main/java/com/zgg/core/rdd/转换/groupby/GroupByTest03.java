package com.zgg.core.rdd.转换.groupby;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

import java.util.stream.StreamSupport;

/**
 * 练习：从服务器日志数据 apache.log 中获取每个时间段访问量
 * */
public class GroupByTest03 {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("GroupByTest03");
        JavaSparkContext sc = new JavaSparkContext(conf);

        JavaRDD<String> rdd = sc.textFile("src/main/resources/apache.log",4);

        // 将读取的一条条数据转成(小时，1)的二元组形式
        JavaPairRDD<String, Integer> pairRDD = rdd.mapToPair(new PairFunction<String, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(String s) throws Exception {
                String[] items = s.split(" ");
                String[] splits = items[3].split(":");
                return new Tuple2<>(splits[1], 1);
            }
        });

        // 对上一步骤封装的二元组按第一个字段分组
        JavaPairRDD<String, Iterable<Tuple2<String, Integer>>> groupByRDD = pairRDD.groupBy(new Function<Tuple2<String, Integer>, String>() {
            @Override
            public String call(Tuple2<String, Integer> v1) throws Exception {
                return v1._1;
            }
        });

        // 对于每一组，计算有多少个元素，元素数量即是访问量
        JavaRDD<Tuple2<String, Integer>> res = groupByRDD.map(new Function<Tuple2<String, Iterable<Tuple2<String, Integer>>>, Tuple2<String, Integer>>() {
            @Override
            public Tuple2<String, Integer> call(Tuple2<String, Iterable<Tuple2<String, Integer>>> v1) throws Exception {

                int visitNum = (int) StreamSupport.stream(v1._2.spliterator(), false).count();
                return new Tuple2<>(v1._1, visitNum);
            }
        });

        res.collect().forEach(System.out::println);

        sc.stop();
    }
}
