package com.zgg.streaming.datasource;


import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 连接kafka
 *  添加依赖 spark-streaming-kafka-0-10_2.12
 *  不用手动添加 kafka-clients 依赖
 */
public class KafkaDS {
    public static void main(String[] args) throws Exception {

        SparkConf conf = new SparkConf()
                .setMaster("local[*]")
                .setAppName("KafkaDS-sparkstreaming");
        JavaStreamingContext ssc = new JavaStreamingContext(conf, Durations.seconds(4));

        Map<String,Object> kafkaParams = new HashMap<>();
//        kafkaParams.put("bootstrap.servers", "bigdata101:9092");
//        kafkaParams.put("key.deserializer", StringDeserializer.class);
//        kafkaParams.put("value.deserializer", StringDeserializer.class);
//        kafkaParams.put("group.id", "spark");
//        kafkaParams.put("auto.offset.reset", "latest");
//        kafkaParams.put("enable.auto.commit", false);
        kafkaParams.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "bigdata101:9092");
        kafkaParams.put(ConsumerConfig.GROUP_ID_CONFIG, "spark");
        kafkaParams.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        kafkaParams.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);


        JavaInputDStream<ConsumerRecord<String, String>> dStream
                = KafkaUtils.createDirectStream(
                    ssc,
                    LocationStrategies.PreferConsistent(),
                    ConsumerStrategies.Subscribe(Collections.singleton("spark"), kafkaParams)
        );

        dStream.map(ConsumerRecord::value).print();

        ssc.start();
        ssc.awaitTermination();

    }

}
