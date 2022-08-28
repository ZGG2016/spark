package com.zgg.core.wc;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
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
import java.util.List;

public class WordCount01 {
    public static void main(String[] args) {
        SparkConf sparkConf = new SparkConf().setAppName("wordcount01").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);

        JavaRDD<String> data = sc.textFile("src/main/resources/wc.txt");

        // 将包含多个单词的一行数据转换成一个一个的单词输出
        JavaRDD<String> words = data.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public Iterator<String> call(String s) throws Exception {
                return Arrays.asList(s.split("\\|")).iterator();
            }
        });
        // ----- flatMap: [hello, spark, hello, hadoop]
        System.out.println("----- flatMap: " +words.collect());

        // 将一个单词转换成一个形如 (word,1) 的二元组
        JavaPairRDD<String, Integer> wordCountPair = words.mapToPair(new PairFunction<String, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(String s) throws Exception {
                return new Tuple2<String,Integer>(s,1);
            }
        });
        // ----- mapToPair: [(hello,1), (spark,1), (hello,1), (hadoop,1)]
        System.out.println("----- mapToPair: " +wordCountPair.collect());

        JavaPairRDD<String, Integer> counts = wordCountPair.reduceByKey(new Function2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer v1, Integer v2) throws Exception {
                return v1 + v2;
            }
        });
        // ----- reduceByKey: [(spark,1), (hadoop,1), (hello,2)]
        System.out.println("----- reduceByKey: " +counts.collect());

        List<Tuple2<String, Integer>> outputs = counts.collect();
        for (Tuple2<String, Integer> output : outputs) {
            System.out.println(output._1 + " - " + output._2);
        }
    }
}
