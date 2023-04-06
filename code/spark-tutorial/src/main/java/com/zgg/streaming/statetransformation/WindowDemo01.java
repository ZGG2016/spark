package com.zgg.streaming.statetransformation;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.Optional;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.Seconds;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import scala.Tuple2;

import java.util.Arrays;
import java.util.List;

/**
 * Window Operations
 * 可以设置窗口的大小和滑动窗口的间隔来动态的获取当前 Steaming 的允许状态。【窗口结束的时候触发计算】
 * 所有基于窗口的操作都需要两个参数，分别为窗口时长以及滑动步长。
 *   ➢ 窗口时长：计算内容的时间范围
 *   ➢ 滑动步长：隔多久触发一次计算
 * 注意：这两者都必须为采集周期大小的整数倍
 */
public class WindowDemo01 {
    public static void main(String[] args) throws InterruptedException {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("WindowDemo01-sparkstreaming");
        JavaStreamingContext ssc = new JavaStreamingContext(conf, Durations.seconds(3));
//        ssc.checkpoint("cp");

        JavaReceiverInputDStream<String> line = ssc.socketTextStream("bigdata101", 9999);

        JavaDStream<String> word = line.flatMap( s -> {
            String[] splits = s.split(" ");
            return Arrays.asList(splits).iterator();
        });
        JavaPairDStream<String, Integer> pair = word.mapToPair(s -> new Tuple2<>(s, 1));

        /*
           窗口的范围（窗口长度和滑动步长）应该是采集周期的整数倍
           窗口可以滑动的，但是默认情况下，一个采集周期进行滑动
           这样的话，可能会出现重复数据的计算，为了避免这种情况，可以改变滑动的滑动（步长）
         */
        JavaPairDStream<String, Integer> windowDS = pair.window(Durations.seconds(6),Durations.seconds(6));

//        JavaPairDStream<String, Integer> res = windowDS.updateStateByKey(
//                 (values, state) -> {
//                    Integer newCount = state.orElse(0);
//                    for (Integer val : values) {
//                        newCount += val;
//                    }
//                    return Optional.of(newCount);
//                });
        JavaPairDStream<String, Integer> res = windowDS.reduceByKey(Integer::sum);
        res.print();

        ssc.start();
        ssc.awaitTermination();
    }
}
