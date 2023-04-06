package com.zgg.core.rdd.依赖;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

import java.util.Arrays;
import java.util.Iterator;

/**
 * 查看RDD的血缘关系
 * 当该 RDD 的部分分区数据丢失时，它可以根据这些信息来重新运算和恢复丢失的数据分区
 * */
public class DepTest01 {
    public static void main(String[] args) {
        SparkConf sparkConf = new SparkConf().setAppName("DepTest01").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);

        JavaRDD<String> data = sc.textFile("src/main/resources/wc.txt",2);
        // A description of this RDD and its recursive dependencies for debugging.
        System.out.println(data.toDebugString());
        System.out.println("------------------------------");

        JavaRDD<String> words = data.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public Iterator<String> call(String s) throws Exception {
                return Arrays.asList(s.split("\\|")).iterator();
            }
        });
        System.out.println(words.toDebugString());
        System.out.println("------------------------------");

        JavaPairRDD<String, Integer> wordCountPair = words.mapToPair(new PairFunction<String, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(String s) throws Exception {
                return new Tuple2<String,Integer>(s,1);
            }
        });
        System.out.println(wordCountPair.toDebugString());
        System.out.println("------------------------------");

        JavaPairRDD<String, Integer> res = wordCountPair.reduceByKey(new Function2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer v1, Integer v2) throws Exception {
                return v1 + v2;
            }
        });
        System.out.println(res.toDebugString());
        System.out.println("------------------------------");

        res.collect().forEach(System.out::println);
    }
}
