package com.zgg.sql.readandwrite;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;

/**
 * 加载和保存数据
 */
public class RWDemo1 {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("RWDemo1");
        SparkSession sparkSession = SparkSession.builder().config(conf).getOrCreate();
        // 1. SparkSQL 默认读取和保存的文件格式为 parquet
//        Dataset<Row> df1 = sparkSession.read().load("src/main/resources/users.parquet");
//        df1.show();
//        df1.write().save("src/main/resources/uses_bk");
//
//        // Caused by: java.lang.RuntimeException ....people.json is not a Parquet file.
//        Dataset<Row> df2 = sparkSession.read().load("src/main/resources/people.json");
//        df2.show();
//
//        // 2. 使用api读取不同格式的数据
//        Dataset<Row> df3 = sparkSession.read().format("json").load("src/main/resources/people.json");
//        df3.show();
//
//        // 3. 直接在文件上进行查询
//        Dataset<Row> df4 = sparkSession.sql("select * from json.`src/main/resources/people.json`");
//        df4.show();
//
//        // 4. 使用api将数据保存为不同格式
//        df1.write().format("json").save("src/main/resources/uses_bk2");
//
//        // 5. 在保存中使用 SaveMode 指明如何处理数据
//        df1.write().mode(SaveMode.Overwrite).format("json").save("src/main/resources/uses_bk2");

        // 6. 加载、写入csv文件
        Dataset<Row> df5 = sparkSession.read()
                .format("csv")
                .option("sep",",")
                .option("inferSchema", "true")
                .option("header", "true")
                .load("src/main/resources/people.csv");
        df5.show();
        Dataset<Row> df6 = sparkSession.sql("select * from csv.`src/main/resources/people.csv`");
        df6.show();
        df5.write().format("csv").save("src/main/resources/uses_bk3");
        
        sparkSession.close();
    }
}
