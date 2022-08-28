package com.zgg.streaming.program;

import com.zgg.streaming.program.bean.AdClickData;
import com.zgg.streaming.program.util.JDBCUtil;
import com.zgg.streaming.program.util.KafkaUtil;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;
import scala.Tuple2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 * 需求一：广告黑名单
 * 将每天对某个广告点击超过 100 次的用户拉黑
 * 黑名单保存到 MySQL 中
 */
public class Demo01 {
    public static void main(String[] args) throws Exception {

        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("Demo01-sparkstreaming");
        JavaStreamingContext ssc = new JavaStreamingContext(conf, Durations.seconds(4));

        // 连接kafka，读取传来的数据
        JavaInputDStream<ConsumerRecord<String, String>> dStream = KafkaUtil.getKafkaStream("spark",ssc);

        // 将传来的数据封装成 POJO
        JavaDStream<AdClickData> adClickData = dStream.map(record -> {
            String[] split = record.value().split(" ");
            return new AdClickData(split[0], split[1], split[2], split[3], split[4]);
        });

        /*
            思考
              1. 这里使用transform而不是map的原因
                 因为需要连接数据库，从数据库周期性取出来黑名单用户 （map和transform的区别）
              2. 划分这两段操作的原因
                 两次操作数据库，一个是查询，一个是更新插入
         */
        // 得到每天每个非黑名单用户 对一个广告的点击量
        JavaDStream<String> transformDS = adClickData.transform(
                rdd -> {
                    // 从数据库周期性取出来黑名单用户（黑名单用户是动态更新的，所以也需要动态获取，所以对每个批次都要取一次这表）
                    ArrayList<String> blackList = new ArrayList<>();
                    Connection connection = JDBCUtil.getConnection();
                    PreparedStatement preparedStatement = connection.prepareStatement("select userid from black_list");
                    ResultSet rs = preparedStatement.executeQuery();
                    while (rs.next()) {
                        blackList.add(rs.getString(1));
                    }
                    rs.close();
                    preparedStatement.close();
                    connection.close();

                    // 对RDD中的每条数据执行
                    JavaRDD<String> countRDD = rdd
                            // 过滤掉在黑名单中的点击用户
                            .filter(data -> !blackList.contains(data.getUser()))
                            // 转换成pair对形式，(( day, user, ad ), 1)
                            .mapToPair(adClickData1 -> {
                                String s = adClickData1.getTs() + "-" + adClickData1.getUser() + "-" + adClickData1.getAd();
                                return new Tuple2<>(s, 1);
                            })
                            // 统计
                            .reduceByKey(Integer::sum)
                            // 将统计结果转换成字符串形式
                            .map(t -> t._1 + "-" + String.valueOf(t._2));

                    return countRDD;
                });

        // 进行条件判断，执行插入、更新操作
        transformDS.foreachRDD(
                rdd -> {
                    rdd.foreachPartition(
                            iter -> {
                                Connection connection = JDBCUtil.getConnection();
                                while (iter.hasNext()) {
                                    // 时间 用户 广告 点击
                                    String[] splits = iter.next().split("-");
                                    int count = Integer.parseInt(splits[3]);
                                    // 如果统计数量超过点击阈值(30)，那么将用户拉入到黑名单
                                    if (count >= 30) {
                                        String sql
                                                = "insert into black_list(userid) values (?) " +
                                                  "on DUPLICATE KEY UPDATE userid = ?";
                                        JDBCUtil.executeUpdate(connection, sql, Arrays.asList(splits[1], splits[1]));
                                    }
                                    // 如果没有超过阈值，那么需要将当天的广告点击数量进行更新
                                    else {
                                        // 判断是不是已经在点击表里
                                        String sql
                                                = "select * from user_ad_count " +
                                                  "where dt=? and userid=? and adid=?";
                                        boolean isExist1 = JDBCUtil.isExist(connection, sql, Arrays.asList(splits[0], splits[1], splits[2]));

                                        // 在点击表里的话，就更新
                                        if (isExist1){
                                            // 更新点击表
                                            String sql1
                                                    = "insert into black_list(userid) values (?) " +
                                                      "on DUPLICATE KEY UPDATE userid = ?";
                                            JDBCUtil.executeUpdate(connection, sql1, Arrays.asList(splits[3], splits[0], splits[1], splits[2]));
                                            // 判断更新后的点击数据是否超过阈值，如果超过，那么将用户拉入到黑名单
                                            String sql2
                                                    = "select * from user_ad_count " +
                                                      "where dt=? and userid=? and adid=?";
                                            boolean isExist2 = JDBCUtil.isExist(connection, sql2, Arrays.asList(splits[0], splits[1], splits[2]));
                                            if (isExist2){
                                                // 超过的话，更新黑名单表
                                                String sql3
                                                        = "insert into black_list(userid) values (?) " +
                                                        "on DUPLICATE KEY UPDATE userid = ?";
                                                JDBCUtil.executeUpdate(connection, sql3, Arrays.asList(splits[1], splits[1]));
                                            }
                                        }
                                        // 不在点击表里的话，就直接插入
                                        else{
                                            String sql4
                                                    = "insert into user_ad_count (dt, userid, adid, count) " +
                                                    "values (?, ?, ?, ?) ";
                                            JDBCUtil.executeUpdate(connection, sql4, Arrays.asList(splits[0], splits[1], splits[2], splits[3]));
                                        }
                                    }
                                }
                                connection.close();
                            }
                    );
                }
        );


        ssc.start();
        ssc.awaitTermination();
    }
}
