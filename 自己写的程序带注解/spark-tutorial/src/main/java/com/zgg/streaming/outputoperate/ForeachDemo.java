package com.zgg.streaming.outputoperate;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import scala.Tuple2;

import java.util.Arrays;

/**
 * 与 RDD 中的惰性求值类似，
 * 如果一个 DStream 及其派生出的 DStream 都没有被执行输出操作，那么这些 DStream 就都不会被求值。
 * 如果 StreamingContext 中没有设定输出操作，整个 context 就都不会启动
 */
public class ForeachDemo {
    public static void main(String[] args) throws InterruptedException {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("ForeachDemo-sparkstreaming");
        JavaStreamingContext ssc = new JavaStreamingContext(conf, Durations.seconds(3));

        JavaReceiverInputDStream<String> line = ssc.socketTextStream("bigdata101", 9999);
        JavaDStream<String> word = line.flatMap( s -> {
            String[] splits = s.split(" ");
            return Arrays.asList(splits).iterator();
        });
        JavaPairDStream<String, Integer> pair = word.mapToPair(s -> new Tuple2<>(s, 1));
        JavaPairDStream<String, Integer> res = pair.reduceByKey(Integer::sum);

        /*
           foreachRDD(func)：这是最通用的输出操作，即将函数 func 用于产生于 stream 的每一个RDD。
                 其中参数传入的函数 func 应该实现将每一个 RDD 中数据推送到外部系统，如将 RDD 存入文件或者通过网络将其写入数据库。

            通用的输出操作 foreachRDD()，它用来对 DStream 中的 RDD 运行任意计算。
            这和 transform()有些类似，都可以让我们访问任意 RDD。

            在 foreachRDD()中，可以重用我们在 Spark 中实现的所有行动操作。
            比如，常见的用例之一是把数据写到诸如 MySQL 的外部数据库中。

            注意：
                1) 连接不能写在 driver 层面（序列化）
                2) 如果写在 foreach, 则每个 RDD 中的每一条数据都创建，得不偿失；
                3) 增加 foreachPartition，在分区创建（获取）。
         */
        res.foreachRDD(new VoidFunction<JavaPairRDD<String, Integer>>() {
            @Override
            public void call(JavaPairRDD<String, Integer> javaPairRDD) throws Exception {
                // 转换输出数据的格式
                javaPairRDD.foreach(t -> {
                    String s = t._1 + " -- " + t._2;
                    System.out.println(s);
                });
            }
        });

        ssc.start();
        ssc.awaitTermination();
    }
}
