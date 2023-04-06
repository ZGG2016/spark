package com.zgg.streaming.program;

import com.zgg.streaming.program.bean.AdClickData;
import com.zgg.streaming.program.util.KafkaUtil;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;
import scala.Tuple2;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 需求三：每10秒钟计算一次广告的点击总流量
 *        12:00 一个结果
 *        12:10 一个结果
 */
public class Demo03 {
    public static void main(String[] args) throws Exception {

        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("Demo03-sparkstreaming");
        JavaStreamingContext ssc = new JavaStreamingContext(conf, Durations.seconds(5));

        // 连接kafka，读取传来的数据
        JavaInputDStream<ConsumerRecord<String, String>> dStream = KafkaUtil.getKafkaStream("spark",ssc);

        // 将传来的数据封装成 POJO
        JavaDStream<AdClickData> adClickData = dStream.map(record -> {
            String[] split = record.value().split(" ");
            return new AdClickData(split[0], split[1], split[2], split[3], split[4]);
        });

        /*
            // 最近一分钟，每10秒计算一次
            // 12:01 => 12:00
            // 12:11 => 12:10
            // 12:19 => 12:10
            // 12:25 => 12:20
            // 12:59 => 12:50

            // 55 => 50, 49 => 40, 32 => 30
            // 55 / 10 * 10 => 50
            // 49 / 10 * 10 => 40
            // 32 / 10 * 10 => 30
         */
        // 涉及窗口计算
        JavaPairDStream<Long, Integer> reduceDS = adClickData
                .mapToPair(
                        data -> {
                            long ts = Long.parseLong(data.getTs());
                            return new Tuple2<>(ts / 10000 * 10000, 1);
                        }
                )
                .reduceByKeyAndWindow(Integer::sum,Durations.seconds(60),Durations.seconds(10));

//        reduceRDD.print();

        // 图展示
        reduceDS.foreachRDD(
                rdd -> {
                    ArrayList<String> arrayList = new ArrayList<>();
                    List<Tuple2<Long, Integer>> datas = rdd.sortByKey(true).collect();
                    for (Tuple2<Long, Integer> t: datas) {
                        String timeString = new SimpleDateFormat("mm:ss").format(new Date(t._1));
                        String cnt = String.valueOf(t._2);
                        String str = "{\"xtime\":\"%s\",\"yval\":\"%s\"}";
                        String res = String.format(str, timeString, cnt);
                        arrayList.add(res);
                    }

                    PrintWriter out = new PrintWriter(new FileWriter(new File("adclick.json")));
                    out.println(arrayList.toString());
                    out.flush();
                    out.close();
                }
        );

        ssc.start();
        ssc.awaitTermination();
    }
}
