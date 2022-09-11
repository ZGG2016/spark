package com.zgg.sql.readandwrite;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.Properties;

/**
 * 加载和保存数据 -- JDBC
 */
public class RWJDBC {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("RWJDBC");
        SparkSession sparkSession = SparkSession.builder().config(conf).getOrCreate();
        /*
            先添加mysql依赖
            这里使用的mysql8
         */
        Dataset<Row> jdbcDF = sparkSession.read()
                .format("jdbc")
                .option("url", "jdbc:mysql://bigdata101:3306/sparksql")
                .option("driver", "com.mysql.cj.jdbc.Driver")
                .option("user", "root")
                .option("password", "root")
                .option("dbtable", "person")
                .load();
        jdbcDF.show();

        Properties connectionProperties = new Properties();
        connectionProperties.put("user", "root");
        connectionProperties.put("password", "root");
        Dataset<Row> jdbcDF2 = sparkSession.read()
                .jdbc("jdbc:mysql://bigdata101:3306/sparksql", "sparksql.person", connectionProperties);
        jdbcDF2.show();

        // Saving data to a JDBC source
        jdbcDF.write()
                .format("jdbc")
                .option("url", "jdbc:mysql://bigdata101:3306/sparksql")
                .option("driver", "com.mysql.cj.jdbc.Driver")
                .option("user", "root")
                .option("password", "root")
                .option("dbtable", "person2")
                .save();

        jdbcDF.write()
                .jdbc("jdbc:mysql://bigdata101:3306/sparksql", "sparksql.person3", connectionProperties);

        // Specifying create table column data types on write
        jdbcDF.write()
                .option("createTableColumnTypes", "name varchar(40), age varchar(40)")
                .jdbc("jdbc:mysql://bigdata101:3306/sparksql", "sparksql.person4", connectionProperties);

        sparkSession.close();
    }
}
