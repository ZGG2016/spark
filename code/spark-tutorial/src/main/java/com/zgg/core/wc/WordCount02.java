package com.zgg.core.wc;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.util.Arrays;
import java.util.List;

public class WordCount02 {
    public static void main(String[] args) {
        SparkConf sparkConf = new SparkConf().setAppName("wordcount02").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);

        JavaRDD<String> data = sc.textFile("src/main/resources/wc.txt");

        // 将包含多个单词的一行数据转换成一个一个的单词输出
        // FlatMapFunction 是一个函数式接口，所以可以使用lambda表达式改写  【看清输入输出是什么类型】
        JavaRDD<String> words = data.flatMap(line -> Arrays.asList(line.split("\\|")).iterator());

        // 将一个单词转换成一个形如 (word,1) 的二元组
        // PairFunction 是一个函数式接口，所以可以使用lambda表达式改写
        JavaPairRDD<String, Integer> wordCountPair = words.mapToPair(word -> new Tuple2<String, Integer>(word, 1));

        // 将这些形如 (word,1) 的二元组分组计算
        // Function2 是一个函数式接口，所以可以使用lambda表达式改写
//        JavaPairRDD<String, Integer> counts = wordCountPair.reduceByKey((v1,v2)->v1+v2);
        JavaPairRDD<String, Integer> counts = wordCountPair.reduceByKey(Integer::sum);

        List<Tuple2<String, Integer>> outputs = counts.collect();
        for (Tuple2<String, Integer> output : outputs) {
            System.out.println(output._1 + " - " + output._2);
        }
    }
}
