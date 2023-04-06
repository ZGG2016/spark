package com.zgg.streaming.nostatetransformation;

import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import scala.Tuple2;

/**
 * join转换
 */
public class JoinDemo {
    public static void main(String[] args) throws InterruptedException {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("JoinDemo-sparkstreaming");
        JavaStreamingContext ssc = new JavaStreamingContext(conf, Durations.seconds(3));

        /*
            两个流之间的 join 需要两个流的批次大小一致，这样才能做到同时触发计算。
            计算过程就是对当前批次的两个流中各自的 RDD 进行 join，与两个 RDD 的 join 效果相同
         */
        JavaReceiverInputDStream<String> ds1 = ssc.socketTextStream("bigdata101", 9999);
        JavaReceiverInputDStream<String> ds2 = ssc.socketTextStream("bigdata101", 8888);

        JavaPairDStream<String, Integer> ds11 = ds1.mapToPair(s -> new Tuple2<>(s, 1));
        JavaPairDStream<String, Integer> ds22 = ds2.mapToPair(s -> new Tuple2<>(s, 1));

        JavaPairDStream<String, Tuple2<Integer, Integer>> res = ds11.join(ds22);
        res.print(); // (a,(1,1))

        ssc.start();
        ssc.awaitTermination();
    }
}
