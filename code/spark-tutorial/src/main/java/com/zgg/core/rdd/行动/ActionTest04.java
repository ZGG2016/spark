package com.zgg.core.rdd.行动;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
/**
 * 保存文件
 * */
public class ActionTest04 {
    public static void main(String[] args) {
        SparkConf sparkConf = new SparkConf().setAppName("ActionTest04").setMaster("local[*]");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);

        List<Tuple2<String, Integer>> data = new ArrayList<Tuple2<String, Integer>>();
        data.add(new Tuple2<String, Integer>("a", 1));
        data.add(new Tuple2<String, Integer>("a", 2));
        data.add(new Tuple2<String, Integer>("b", 3));
        JavaPairRDD<String, Integer> rdd = sc.parallelizePairs(data, 1);

       rdd.saveAsTextFile("output1");
       // Save this RDD as a SequenceFile of serialized objects
       rdd.saveAsObjectFile("output2");

        sc.stop();
    }
}
