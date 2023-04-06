package com.zgg.sql.basicop;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.*;
import static org.apache.spark.sql.functions.col;

/**
 * DataFrame基本操作
 */
public class BasicOpDataFrame {
    public static void main(String[] args) {
        // 创建SparkSQL的运行环境
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("BasicOpDataFrame");
        SparkSession sparkSession = SparkSession.builder().config(conf).getOrCreate();

        // DataFrame
        Dataset<Row> df = sparkSession.read().json("src/main/resources/people.json");
        df.show();

        // 1. sql语法
        df.createOrReplaceTempView("people");
        sparkSession.sql("select * from people").show();
        sparkSession.sql("select avg(age) from people").show();

        // 2. dsl语法
        df.select("name","age").show();
        df.select(col("name"), col("age").plus(1)).show();

        sparkSession.close();
    }
}
