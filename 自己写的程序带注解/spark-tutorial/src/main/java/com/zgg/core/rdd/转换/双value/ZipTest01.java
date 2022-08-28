package com.zgg.core.rdd.转换.双value;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.util.Arrays;
import java.util.List;

public class ZipTest01 {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("ZipTest01");
        JavaSparkContext sc = new JavaSparkContext(conf);

        List<Integer> data1 = Arrays.asList(1, 2, 3, 4);
//        List<Integer> data2 = Arrays.asList(3, 4, 5, 6);
        List<Integer> data2 = Arrays.asList(3, 4, 5, 6, 7, 8);
        JavaRDD<Integer> rdd1 = sc.parallelize(data1, 2);
        JavaRDD<Integer> rdd2 = sc.parallelize(data2, 2);
//        JavaRDD<Integer> rdd2 = sc.parallelize(data2,4);
        /*
          Can't zip RDDs with unequal numbers of partitions: List(2, 4)
          两个数据源要求分区数量要保持一致

          Can only zip RDDs with same number of elements in each partition
          两个数据源要求分区中数据数量保持一致
         */
        JavaPairRDD<Integer, Integer> zip = rdd1.zip(rdd2);
        zip.collect().forEach(System.out::print);
        System.out.println();

        sc.stop();
    }
}
