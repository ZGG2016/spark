package com.zgg.core.rdd.转换.双value;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;

import java.util.Arrays;
import java.util.List;

/**
 * 交集，并集和差集要求两个数据源数据类型保持一致
 * 拉链操作两个数据源的类型可以不一致
 */
public class DoubleValuesTest01 {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("DoubleValuesTest01");
        JavaSparkContext sc = new JavaSparkContext(conf);

        List<Integer> data1 = Arrays.asList(1, 2, 3, 4);
        List<Integer> data2 = Arrays.asList(3, 4, 5, 6);
        JavaRDD<Integer> rdd1 = sc.parallelize(data1);
        JavaRDD<Integer> rdd2 = sc.parallelize(data2);

        // 对源 RDD 和参数 RDD 求交集后返回一个新的 RDD
        JavaRDD<Integer> intersection = rdd1.intersection(rdd2);
        intersection.collect().forEach(System.out::print);
        System.out.println();

        // 对源 RDD 和参数 RDD 求并集后返回一个新的 RDD
        JavaRDD<Integer> union = rdd1.union(rdd2);
        union.collect().forEach(System.out::print);
        System.out.println();

        // 以一个 RDD 元素为主，去除两个 RDD 中重复元素，将其他元素保留下来。求差集
        JavaRDD<Integer> subtract = rdd1.subtract(rdd2);
        subtract.collect().forEach(System.out::print);
        System.out.println();

        // 将两个 RDD 中的元素，以键值对的形式进行合并。
        // 其中，键值对中的 Key 为第 1 个 RDD 中的元素，Value 为第 2 个 RDD 中的相同位置的元素
        JavaPairRDD<Integer, Integer> zip = rdd1.zip(rdd2);
        zip.collect().forEach(System.out::print);
        System.out.println();

        sc.stop();
    }
}
