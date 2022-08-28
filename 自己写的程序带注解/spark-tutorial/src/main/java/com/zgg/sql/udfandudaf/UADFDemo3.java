package com.zgg.sql.udfandudaf;

import com.zgg.sql.bean.Average;
import com.zgg.sql.bean.Person;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.*;
import org.apache.spark.sql.expressions.Aggregator;

/**
 * UADF函数：早期版本的强类型实现
 *   早期版本中，spark不能在sql中使用强类型UDAF操作
 *   早期的UDAF强类型聚合函数使用DSL语法操作
 */
public class UADFDemo3 {
    public static void main(String[] args) {
        // 创建SparkSQL的运行环境
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("UADFDemo3");
        SparkSession sparkSession = SparkSession.builder().config(conf).getOrCreate();

        Dataset<Row> df = sparkSession.read().json("src/main/resources/people.json");
        Encoder<Person> personEncoder = Encoders.bean(Person.class);
        Dataset<Person> ds = df.as(personEncoder);

        MyAgeAvg ageAvg = new MyAgeAvg();
        TypedColumn<Person, Double> averageAge = ageAvg.toColumn().name("average_age");
        Dataset<Double> res = ds.select(averageAge);
        res.show();

        sparkSession.close();
    }

    /*
     自定义聚合函数类：计算年龄的平均值
     1. 继承org.apache.spark.sql.expressions.Aggregator, 定义泛型
         IN : 输入的数据类型 Person
         BUF : 缓冲区的数据类型 Average
         OUT : 输出的数据类型 Double
     2. 重写方法(6)
     */
    static class MyAgeAvg extends Aggregator<Person, Average, Double>{

        // 缓冲区的初始化
        @Override
        public Average zero() {
            return new Average(0L,0L);
        }

        // 根据输入的数据更新缓冲区的数据
        @Override
        public Average reduce(Average buffer, Person data) {
            long newSum = buffer.getSum() + data.getAge();
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
