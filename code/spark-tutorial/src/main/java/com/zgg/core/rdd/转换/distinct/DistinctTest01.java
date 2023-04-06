package com.zgg.core.rdd.转换.distinct;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.util.Arrays;
import java.util.List;

/**
 * 将数据集中重复的数据去重
 */
public class DistinctTest01 {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("DistinctTest01");
        JavaSparkContext sc = new JavaSparkContext(conf);

        List<Integer> data = Arrays.asList(1, 2, 3, 4, 1, 2, 3, 4);
        JavaRDD<Integer> rdd = sc.parallelize(data);
        /*
          去重原理：
          map(x => (x, null)) --> (1,null)(1,null)(2,null)(2,null)...
          .reduceByKey((x, _) => x, numPartitions) --> 【(1,null)(1,null)】 【(2,null)(2,null)】... -->(1,null)(2,null)
          .map(_._1) --> 1 2
         */
        rdd.distinct().collect().forEach(System.out::println);

        sc.stop();
    }
}
