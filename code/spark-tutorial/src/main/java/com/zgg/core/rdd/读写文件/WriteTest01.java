package com.zgg.core.rdd.读写文件;

import org.apache.spark.Partitioner;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WriteTest01 {
    public static void main(String[] args) {
        SparkConf sparkConf = new SparkConf().setAppName("WriteTest01").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);

        List<Tuple2<String, Integer>> data = new ArrayList<Tuple2<String, Integer>>();
        data.add(new Tuple2<String, Integer>("a", 1));
        data.add(new Tuple2<String, Integer>("b", 2));
        data.add(new Tuple2<String, Integer>("c", 3));
        JavaPairRDD<String, Integer> rdd = sc.parallelizePairs(data);

        rdd.saveAsTextFile("output1");
        // 对象文件是将对象序列化后保存的文件，采用 Java 的序列化机制
        rdd.saveAsObjectFile("output2");
    }
}
