package com.zgg.sql.udfandudaf;

import com.zgg.sql.bean.Average;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.*;
import org.apache.spark.sql.expressions.Aggregator;

/**
 * UADF函数：强类型实现
 */
public class UADFDemo2 {
    public static void main(String[] args) {
        // 创建SparkSQL的运行环境
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("UADFDemo2");
        SparkSession sparkSession = SparkSession.builder().config(conf).getOrCreate();

        Dataset<Row> df = sparkSession.read().json("src/main/resources/people.json");
        df.createOrReplaceTempView("people");

        sparkSession.udf().register("ageAvg", functions.udaf(new MyAgeAvg(), Encoders.LONG()));

        sparkSession.sql("select ageAvg(age) from people").show();

        sparkSession.close();
    }

    /*
     自定义聚合函数类：计算年龄的平均值
     1. 继承org.apache.spark.sql.expressions.Aggregator, 定义泛型
         IN : 输入的数据类型 Long
         BUF : 缓冲区的数据类型 Average
         OUT : 输出的数据类型 Double
     2. 重写方法(6)
     */
    static class MyAgeAvg extends Aggregator<Long, Average, Double>{

        // 缓冲区的初始化
        @Override
        public Average zero() {
            return new Average(0L,0L);
        }

        // 根据输入的数据更新缓冲区的数据
        @Override
        public Average reduce(Average buffer, Long data) {
            long newSum = buffer.getSum() + data;
            long newCount = buffer.getCount() + 1;
            buffer.setSum(newSum);
            buffer.setCount(newCount);
            return buffer;
        }

        // 合并缓冲区
        @Override
        public Average merge(Average buffer1, Average buffer2) {
            long mergeSum = buffer1.getSum() + buffer2.getSum();
            long mergeCount = buffer1.getCount() + buffer2.getCount();
            buffer1.setSum(mergeSum);
            buffer1.setCount(mergeCount);
            return buffer1;
        }

        //计算结果
        @Override
        public Double finish(Average average) {
            return (double) (average.getSum() / average.getCount());
        }

        // 缓冲区的编码操作
        @Override
        public Encoder<Average> bufferEncoder() {
            return Encoders.bean(Average.class);
        }

        // 输出的编码操作
        @Override
        public Encoder<Double> outputEncoder() {
            return Encoders.DOUBLE();
        }
    }
}
