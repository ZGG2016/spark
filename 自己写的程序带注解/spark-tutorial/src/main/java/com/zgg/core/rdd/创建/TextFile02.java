package com.zgg.core.rdd.创建;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

/**
 * 从外部存储（文件）创建 RDD  -  再次测试
 * */
public class TextFile02 {
    public static void main(String[] args) {

        SparkConf sparkConf = new SparkConf().setMaster("local[*]").setAppName("RddPartitionTextFile02");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);

         /*
            `12.txt`文件内容： （共14个字节）
            1234567 (回车换行)
            89 (回车换行)
            0
            -------------------
            内容         偏移量
            1234567@@   0 1 2 3 4 5 6 7 8
            89@@        9 10 11 12
            0           13
            -------------------
            分区   偏移量范围    内容
             0      [0,7]      1234567
             1      [7,14]     890
             -------------------
             视频中的方法：
             14byte / 2 = 7byte
             14 / 7 = 2(分区)
          */

//        JavaRDD<String> rdd = sc.textFile("src/main/resources/12.txt", 2);
        // 如果数据源为多个文件，那么计算分区时以文件为单位进行分区，但是totalSize是所有文件的总大小（还是按照`textfile分区方式.md`执行）
        JavaRDD<String> rdd = sc.textFile("src/main/resources/1*.txt", 2);
        System.out.println("========= " + rdd.getNumPartitions() + " =========" );
        rdd.saveAsTextFile("output");

        sc.stop();
    }
}
