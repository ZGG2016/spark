package com.zgg.streaming.stopgracefully;

import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.StreamingContextState;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

import java.util.Arrays;

/**
 * 优雅地关闭
 */
public class ClosegracefullyDemo {
    public static void main(String[] args) throws Exception{
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("close-gracefully1-sparkstreaming");
        JavaStreamingContext ssc = new JavaStreamingContext(conf, Durations.seconds(3));
        JavaReceiverInputDStream<String> line = ssc.socketTextStream("bigdata101", 9999);
        JavaDStream<String> word = line.flatMap(s -> {
            String[] splits = s.split(" ");
            return Arrays.asList(splits).iterator();
        });
        word.print();

        ssc.start();

        // 如果想要关闭采集器，那么需要创建新的线程
        // 而且需要在第三方程序中增加关闭状态
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 优雅地关闭
                // 计算节点不在接收新的数据，而是将现有的数据处理完毕，然后关闭
                // Mysql : Table(stopSpark) => Row => data
                // Redis : Data（K-V）
                // ZK    : /stopSpark
                // HDFS  : /stopSpark
               /*
                while ( true ) {
                    if (true) {
                        // 获取SparkStreaming状态
                        StreamingContextState state = ssc.getState();
                        if ( state == StreamingContextState.ACTIVE ) {
                            ssc.stop(true, true);
                        }
                    }
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                */

                // 停止socket后，程序输出不会立马停止，而是处理完现有数据后再停止
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                StreamingContextState state = ssc.getState();
                if ( state == StreamingContextState.ACTIVE ) {
                    ssc.stop(true, true);
                }
                System.exit(0);
            }
        });

        ssc.awaitTermination();
    }
}
