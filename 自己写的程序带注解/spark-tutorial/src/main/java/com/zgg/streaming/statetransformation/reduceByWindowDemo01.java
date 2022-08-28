package com.zgg.streaming.statetransformation;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import scala.Tuple2;

import java.util.Arrays;

/**
 * reduceByWindow(func, windowLength, slideInterval)
 */
public class reduceByWindowDemo01 {
    public static void main(String[] args) throws InterruptedException {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("reduceByWindow-sparkstreaming");
        JavaStreamingContext ssc = new JavaStreamingContext(conf, Durations.seconds(3));

        JavaReceiverInputDStream<String> line = ssc.socketTextStream("bigdata101", 9999);
        JavaDStream<Integer> data = line.map(Integer::valueOf);

        /*
            将一个reduce函数应用在一个窗口内（流的每个RDD）
         */
        JavaDStream<Integer> res = data.reduceByWindow(
                Integer::sum,
                Durations.seconds(6),
                Durations.seconds(6));

        res.print();
        ssc.start();
        ssc.awaitTermination();
    }
}
