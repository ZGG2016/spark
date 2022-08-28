package com.zgg.sql.udfandudaf;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.expressions.UserDefinedFunction;
import org.apache.spark.sql.types.DataTypes;

import static org.apache.spark.sql.functions.udf;

/**
 * UDF函数
 */
public class UDFDemo {
    public static void main(String[] args) {
        // 创建SparkSQL的运行环境
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("UDFDemo");
        SparkSession sparkSession = SparkSession.builder().config(conf).getOrCreate();

        Dataset<Row> df = sparkSession.read().json("src/main/resources/people.json");
        df.createOrReplaceTempView("people");

        UserDefinedFunction addPrefix = udf(
                (String name) -> "name: " + name,
                DataTypes.StringType
        );

        sparkSession.udf().register("addPrefix", addPrefix);
        // 给名字添加一个前缀
        sparkSession.sql("select addPrefix(name), age from people").show();

        sparkSession.close();
    }
}
