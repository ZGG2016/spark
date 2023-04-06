package com.zgg.streaming.statetransformation;

import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import scala.Tuple2;

import java.util.Arrays;

/**
 * countByWindow(windowLength, slideInterval)
 */
public class countByWindowDemo01 {
    public static void main(String[] args) throws InterruptedException {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("countByWindowDemo01-sparkstreaming");
        JavaStreamingContext ssc = new JavaStreamingContext(conf, Durations.seconds(3));
        ssc.checkpoint("cp");

        JavaReceiverInputDStream<String> line = ssc.socketTextStream("bigdata101", 9999);
        JavaDStream<String> word = line.flatMap( s -> {
            String[] splits = s.split(" ");
            return Arrays.asList(splits).iterator();
        });
        JavaPairDStream<String, Integer> pair = word.mapToPair(s -> new Tuple2<>(s, 1));

        /*
            计算一个窗口内（流的每个RDD）元素的数量
         */
        JavaDStream<Long> res = pair.countByWindow(Durations.seconds(6), Durations.seconds(6));

        res.print();
        ssc.start();
        ssc.awaitTermination();
    }
}
