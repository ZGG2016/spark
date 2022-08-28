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
 * reduceByKeyAndWindow(func, windowLength, slideInterval, [numTasks])
 */
public class ReduceByKeyAndWindowDemo01 {
    public static void main(String[] args) throws InterruptedException {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("ReduceByKeyAndWindowDemo01-sparkstreaming");
        JavaStreamingContext ssc = new JavaStreamingContext(conf, Durations.seconds(3));

        JavaReceiverInputDStream<String> line = ssc.socketTextStream("bigdata101", 9999);

        JavaDStream<String> word = line.flatMap( s -> {
            String[] splits = s.split(" ");
            return Arrays.asList(splits).iterator();
        });
        JavaPairDStream<String, Integer> pair = word.mapToPair(s -> new Tuple2<>(s, 1));

        /*
           在每个窗口上使用一个 reduceByKey 函数，返回一个新的 DStream
           类似，先应用window 再应用 reduceByKey （也就是 WindowDemo01.java 中的程序）
         */
//        JavaPairDStream<String, Integer> res = pair.reduceByKeyAndWindow(
//                Integer::sum,
//                Durations.seconds(6),
//                Durations.seconds(6));

        /*
           1. 当连续输入两个a，然后就不再输出入时，此时，会在3个连续窗口输出中都输出(a,2)，接下来就没有输出了
           2. 窗口长度太长，但滑动步长太小，那么两个窗口重叠的部分就重复计算
              （重叠的部分 当在窗口A时会计算一次，当在下一个窗口B时会再次计算一回）
         */
        JavaPairDStream<String, Integer> res = pair.reduceByKeyAndWindow(
                Integer::sum,
                Durations.seconds(9),
                Durations.seconds(3));

        res.print();

        ssc.start();
        ssc.awaitTermination();
    }
}
