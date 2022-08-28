package com.zgg.core.rdd.转换.sortbykey;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import scala.Tuple2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 在一个(K,V)的 RDD 上调用，K 必须实现 Ordered 接口，返回一个按照 key 进行排序的 RDD
 */
public class SortByKeyTest01 {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("SortByKeyTest01");
        JavaSparkContext sc = new JavaSparkContext(conf);

        List<Tuple2<String, Integer>> data = new ArrayList<Tuple2<String, Integer>>();
        data.add(new Tuple2<String, Integer>("a", 1));
        data.add(new Tuple2<String, Integer>("a", 2));
        data.add(new Tuple2<String, Integer>("b", 3));
        data.add(new Tuple2<String, Integer>("b", 4));
        data.add(new Tuple2<String, Integer>("b", 5));
        data.add(new Tuple2<String, Integer>("a", 6));
        JavaPairRDD<String, Integer> rdd = sc.parallelizePairs(data, 2);

//        JavaPairRDD<String, Integer> res1 = rdd.sortByKey(true);

        JavaPairRDD<String, Integer> res2 = rdd.sortByKey(new MyComparator());

//        res1.collect().forEach(System.out::println);
        res2.collect().forEach(System.out::println);
        sc.stop();
    }

    // 必须要实现 接口，否则报异常SparkException: Task not serializable
    public static class MyComparator implements Comparator<String>, Serializable{

        @Override
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }
    }
}