package com.zgg.streaming.stopgracefully;

import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import scala.Tuple2;

import java.util.Arrays;

public class Test {
    public static void main(String[] args) throws Exception{

        // 通过这个检查点目录来恢复数据
        JavaStreamingContext ssc = JavaStreamingContext.getOrCreate("cp", Test::createSSC);
        new Thread(new MonitorStop(ssc)).start();

        ssc.start();
        ssc.awaitTermination();
    }

    public static JavaStreamingContext createSSC(){
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("close-gracefully-sparkstreaming");

        conf.set("spark.streaming.stopGracefullyOnShutdown", "true");

        JavaStreamingContext ssc = new JavaStreamingContext(conf, Durations.seconds(3));

        ssc.checkpoint("cp");

        JavaReceiverInputDStream<String> line = ssc.socketTextStream("bigdata101", 9999);

        JavaDStream<String> word = line.flatMap(s -> {
            String[] splits = s.split(" ");
            return Arrays.asList(splits).iterator();
        });
        JavaPairDStream<String, Integer> pair = word.mapToPair(s -> new Tuple2<>(s, 1));
        JavaPairDStream<String, Integer> res = pair.reduceByKey(Integer::sum);
        res.print();

        return ssc;
    }
}
