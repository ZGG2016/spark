package com.zgg.core.rdd.行动;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ActionTest03 {
    public static void main(String[] args) {
        SparkConf sparkConf = new SparkConf().setAppName("ActionTest03").setMaster("local[*]");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);

        List<Integer> data1 = Arrays.asList(1, 2, 3, 4);
        JavaRDD<Integer> rdd1 = sc.parallelize(data1, 2);

        List<Tuple2<String, Integer>> data2 = new ArrayList<Tuple2<String, Integer>>();
        data2.add(new Tuple2<String, Integer>("a", 1));
        data2.add(new Tuple2<String, Integer>("a", 2));
        data2.add(new Tuple2<String, Integer>("b", 3));
        JavaPairRDD<String, Integer> rdd2 = sc.parallelizePairs(data2, 2);

        /*
           countByValue: 统计每个值的个数
           countByKey: 统计每种 key 的个数
        */

        Map<Integer, Long> res1 = rdd1.countByValue();
        Map<String, Long> res2 = rdd2.countByKey();

        System.out.println(res1);
        System.out.println(res2);

        sc.stop();
    }
}
