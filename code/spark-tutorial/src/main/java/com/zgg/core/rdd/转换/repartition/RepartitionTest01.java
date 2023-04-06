package com.zgg.core.rdd.转换.repartition;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.util.Arrays;
import java.util.List;

/**
 * 该操作内部其实执行的是 coalesce 操作，参数 shuffle 的默认值为 true。
 * 无论是将分区数多的RDD 转换为分区数少的 RDD，还是将分区数少的 RDD 转换为分区数多的 RDD，
 * repartition 操作都可以完成，因为无论如何都会经 shuffle 过程。
 */
public class RepartitionTest01 {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("RepartitionTest01");
        JavaSparkContext sc = new JavaSparkContext(conf);

        List<Integer> data = Arrays.asList(1, 2, 3, 4, 5, 6);
        JavaRDD<Integer> rdd = sc.parallelize(data,2);
        /*
          coalesce算子可以扩大分区的，但是如果不进行shuffle操作，是没有意义，不起作用。
               所以如果想要实现扩大分区的效果，需要使用shuffle操作
          spark提供了一个简化的操作
          缩减分区：coalesce，如果想要数据均衡，可以采用shuffle
          扩大分区：repartition, 底层代码调用的就是coalesce，而且肯定采用shuffle
         */
//        rdd.coalesce(3).saveAsTextFile("output");
        rdd.repartition(3).saveAsTextFile("output");

        sc.stop();
    }
}
