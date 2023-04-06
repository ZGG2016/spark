package com.zgg.streaming.wc;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import scala.Tuple2;

import java.util.Arrays;
import java.util.Iterator;

public class WordCount {
    public static void main(String[] args) throws InterruptedException {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("wordcount-sparkstreaming");
        // 第二个参数表示批量处理的周期（采集周期）
        JavaStreamingContext ssc = new JavaStreamingContext(conf, Durations.seconds(3));

        JavaReceiverInputDStream<String> line = ssc.socketTextStream("bigdata101", 9999);

        JavaDStream<String> word = line.flatMap( s -> {
            String[] splits = s.split(" ");
            return Arrays.asList(splits).iterator();
        });

        JavaPairDStream<String, Integer> pair = word.mapToPair(s -> new Tuple2<>(s, 1));

        /*
            注意： flatMap mapToPair reduceByKey
                      这些操作都是无状态的操作，只对当前的采集周期（当前RDD，一个采集周期产生一个RDD）内的数据进行处理
                 例如：reduceByKey()会归约每个时间区间中的数据，但不会归约不同区间之间的数据（不会累计汇总）
         */
        JavaPairDStream<String, Integer> res = pair.reduceByKey(Integer::sum);

        res.print();

        // 由于SparkStreaming采集器是长期执行的任务，所以不能直接关闭
        // 如果main方法执行完毕，应用程序也会自动结束。所以不能让main执行完毕
        //ssc.stop()
        // 1. 启动采集器
        ssc.start();
        // 2. 等待采集器的关闭
        ssc.awaitTermination();
    }
}
