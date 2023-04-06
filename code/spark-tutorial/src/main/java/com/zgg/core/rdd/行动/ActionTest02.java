package com.zgg.core.rdd.行动;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;

import java.util.Arrays;
import java.util.List;

public class ActionTest02 {
    public static void main(String[] args) {
        SparkConf sparkConf = new SparkConf().setAppName("ActionTest02").setMaster("local[*]");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);

        List<Integer> data = Arrays.asList(1, 2, 3, 4);
        JavaRDD<Integer> rdd = sc.parallelize(data, 2);

        /*
           aggregate : 分区的数据通过初始值和分区内的数据进行聚合，然后再和初始值进行分区间的数据聚合
              aggregateByKey : 初始值只会参与分区内计算
                 也就是 10+1+2=13  10+3+4=17   13+17=30
              aggregate : 初始值会参与分区内计算,并且和参与分区间计算
                 也就是 10+1+2=13  10+3+4=17   10+13+17=40
        */

        Integer res1 = rdd.aggregate(10,
                new Function2<Integer, Integer, Integer>() {
                    @Override
                    public Integer call(Integer v1, Integer v2) throws Exception {
                        return v1 + v2;
                    }
                },
                new Function2<Integer, Integer, Integer>() {
                    @Override
                    public Integer call(Integer v1, Integer v2) throws Exception {
                        return v1 + v2;
                    }
                });

        // fold: aggregate 的简化版操作，分区内操作和分区间操作相同
        Integer res2 = rdd.fold(10,
                new Function2<Integer, Integer, Integer>() {
                    @Override
                    public Integer call(Integer v1, Integer v2) throws Exception {
                        return v1 + v2;
                    }
                });

        System.out.println(res1);
        System.out.println(res2);

        sc.stop();
    }
}
