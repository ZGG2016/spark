package com.zgg.streaming.program;

import com.zgg.streaming.program.bean.AdClickData;
import com.zgg.streaming.program.util.JDBCUtil;
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 需求二：实时统计每天各地区各城市各广告的点击总流量，并将其存入 MySQL。
 */
public class Demo02 {
    public static void main(String[] args) throws Exception {

        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("Demo02-sparkstreaming");
        JavaStreamingContext ssc = new JavaStreamingContext(conf, Durations.seconds(4));

        // 连接kafka，读取传来的数据
        JavaInputDStream<ConsumerRecord<String, String>> dStream = KafkaUtil.getKafkaStream("spark",ssc);

        // 将传来的数据封装成 POJO
        JavaDStream<AdClickData> adClickData = dStream.map(record -> {
            String[] split = record.value().split(" ");
            return new AdClickData(split[0], split[1], split[2], split[3], split[4]);
        });


        // 在单个批次内，对数据进行按照天维度的聚合统计
        JavaPairDStream<String, Integer> reduceDS = adClickData
                .mapToPair(
                        data -> {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            String day = sdf.format(new Date(Long.parseLong(data.getTs())));
                            String s = day + "-" + data.getArea() + "-" + data.getCity() + "-" + data.getAd();
                            return new Tuple2<>(s, 1);
                        }
                )
                .reduceByKey(Integer::sum);

        // 结合 MySQL 数据，跟当前批次数据更新原有的数据
        reduceDS.foreachRDD(
            rdd -> {
                rdd.foreachPartition(
                        iter -> {
                            Connection connection = JDBCUtil.getConnection();
                            String sql
                                    = "insert into area_city_ad_count ( dt, area, city, adid, count )" +
                                      "values (?, ?, ?, ?, ?)" +
                                      "on DUPLICATE KEY " +
                                      "UPDATE count = count + ?";
                            PreparedStatement preparedStatement = connection.prepareStatement(sql);
                            while(iter.hasNext()){
                                Tuple2<String, Integer> t = iter.next();
                                String[] splits = t._1.split("-");
                                preparedStatement.setString(1,splits[0]);
                                preparedStatement.setString(2,splits[1]);
                                preparedStatement.setString(3,splits[2]);
                                preparedStatement.setString(4,splits[3]);
                                preparedStatement.setInt(5,t._2);
                                preparedStatement.setInt(6,t._2);
                                preparedStatement.executeUpdate();
                            }
                            preparedStatement.close();
                            connection.close();
                        }
                );
            }
        );

        ssc.start();
        ssc.awaitTermination();
    }
}
