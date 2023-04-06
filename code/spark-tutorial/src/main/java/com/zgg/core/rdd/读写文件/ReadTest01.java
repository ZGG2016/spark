package com.zgg.core.rdd.读写文件;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;

public class ReadTest01 {
    public static void main(String[] args) {
        SparkConf sparkConf = new SparkConf().setAppName("ReadTest01").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);

        JavaRDD<String> rdd1 = sc.textFile("output1");
        rdd1.collect().forEach(System.out::println);

        // 对象文件是将对象序列化后保存的文件，采用 Java 的序列化机制
        JavaRDD<Object> rdd2 = sc.objectFile("output2");
        rdd2.collect().forEach(System.out::println);


    }
}
