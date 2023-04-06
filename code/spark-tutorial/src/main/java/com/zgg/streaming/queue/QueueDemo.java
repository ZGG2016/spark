package com.zgg.streaming.queue;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.*;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
/*
   测试过程中，可以通过使用 ssc.queueStream(queueOfRDDs)来创建 DStream，
   每一个推送到这个队列中的 RDD，都会作为一个 DStream 处理
 */
public class QueueDemo {
    public static void main(String[] args) throws Exception {

        SparkConf conf = new SparkConf()
                .setMaster("local[*]")
                .setAppName("QueueDemo-sparkstreaming");
        JavaStreamingContext ssc = new JavaStreamingContext(conf, Durations.seconds(1));

        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            list.add(i);
        }

        Queue<JavaRDD<Integer>> rddQueue = new LinkedList<>();
        for (int i = 0; i < 30; i++) {
            rddQueue.add(ssc.sparkContext().parallelize(list));
        }

        JavaDStream<Integer> inputStream = ssc.queueStream(rddQueue);
        JavaPairDStream<Integer, Integer> mappedStream = inputStream.mapToPair(i -> new Tuple2<>(i % 10, 1));
        JavaPairDStream<Integer, Integer> reducedStream = mappedStream.reduceByKey(Integer::sum);

        reducedStream.print();
        ssc.start();
        ssc.awaitTermination();
    }
}
