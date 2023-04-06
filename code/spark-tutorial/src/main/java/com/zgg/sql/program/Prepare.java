package com.zgg.sql.program;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;
// 准备工作：建表、导入数据
public class Prepare {
    public static void main(String[] args) {
        System.setProperty("HADOOP_USER_NAME", "root");
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("RWHive")
                .set("spark.sql.warehouse.dir", "hdfs://bigdata101:9000/user/hive/warehouse");

        SparkSession sparkSession = SparkSession
                .builder()
                .enableHiveSupport()
                .config(conf)
                .getOrCreate();

        sparkSession.sql(
                "CREATE TABLE user_visit_action(\n" +
                        "action_date string,\n" +
                        "user_id bigint,\n" +
                        "session_id string,\n" +
                        "page_id bigint,\n" +
                        "action_time string,\n" +
                        "search_keyword string,\n" +
                        "click_category_id bigint,\n" +
                        "click_product_id bigint,\n" +
                        "order_category_ids string,\n" +
                        "order_product_ids string,\n" +
                        "pay_category_ids string,\n" +
                        "pay_product_ids string,\n" +
                        "city_id bigint)\n" +
                        "row format delimited fields terminated by '\t';");

        sparkSession.sql(
                "load data local inpath 'src/main/resources/user_visit_action.txt'" +
                        "into table user_visit_action;");

        sparkSession.sql(
                "CREATE TABLE product_info(\n" +
                        "product_id bigint,\n" +
                        "product_name string,\n" +
                        "extend_info string)\n" +
                        "row format delimited fields terminated by '\t';");

        sparkSession.sql(
                "load data local inpath 'src/main/resources/product_info.txt' " +
                        "into table product_info;");

        sparkSession.sql(
                "CREATE TABLE city_info(\n" +
                        "city_id bigint,\n" +
                        "city_name string,\n" +
                        "area string)\n" +
                        "row format delimited fields terminated by '\t';");

        sparkSession.sql(
                "load data local inpath 'src/main/resources/city_info.txt' " +
                        "into table city_info;");

        sparkSession.close();
    }
}
