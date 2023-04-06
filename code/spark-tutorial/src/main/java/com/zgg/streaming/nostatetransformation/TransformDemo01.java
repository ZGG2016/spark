package com.zgg.streaming.nostatetransformation;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

/**
 * Transform转换
 */
public class TransformDemo01 {
    public static void main(String[] args) throws InterruptedException {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("TransformDemo01-sparkstreaming");
        JavaStreamingContext ssc = new JavaStreamingContext(conf, Durations.seconds(3));

        JavaReceiverInputDStream<String> line = ssc.socketTextStream("bigdata101", 9999);

        /*
            transform方法可以将底层RDD获取到后进行操作，也就是操作 流的每个批次产生的RDD
               1. DStream功能不完善
               2. 需要代码周期性的执行

               也就是先操作一个批次中的每个元素，从而产生一个RDD，然后还可以再操作这个RDD，完成后，再返回

         */

        // Code : Driver端
        JavaDStream<Integer> ds1 = line.transform(new Function<JavaRDD<String>, JavaRDD<Integer>>() {
            @Override
            public JavaRDD<Integer> call(JavaRDD<String> str) throws Exception {
                // Code : Driver端，（周期执行）
                JavaRDD<Integer> rdd = str.map(s -> {
                    // Code : Executor端
                    int res = Integer.parseInt(s) + 1;
                    return res;
                });
                return rdd;
            }
        });
        ds1.print();

        // Code : Driver端
        JavaDStream<Integer> ds2 = line.map(new Function<String, Integer>() {
            @Override
            public Integer call(String s) throws Exception {
                // Code : Executor端
                int res = Integer.parseInt(s) + 1;
                return res;
            }
        });
        ds2.print();

        ssc.start();
        ssc.awaitTermination();
    }
}
