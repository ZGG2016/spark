package com.zgg.core.累加器;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.util.LongAccumulator;

import java.util.Arrays;
import java.util.List;
/**
 * 累加器用来把 Executor 端变量信息聚合到 Driver 端。
 * 在 Driver 程序中定义的变量，在 Executor 端的每个 Task 都会得到这个变量的一份新的副本，
 * 每个 task 更新这些副本的值后，传回 Driver 端进行 merge。
 *
 * 【分布式共享只写变量】
 * */
public class AccTest02 {
    public static void main(String[] args) {

        SparkConf sparkConf = new SparkConf().setMaster("local[*]").setAppName("AccTest02");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);

        List<Integer> lst = Arrays.asList(1, 2, 3, 4);
        JavaRDD<Integer> rdd1 = sc.parallelize(lst);

        // 获取系统累加器
        LongAccumulator sumAcc = sc.sc().longAccumulator("sum");

        rdd1.foreach(new VoidFunction<Integer>() {
            @Override
            public void call(Integer i) throws Exception {
                // 使用累加器
                sumAcc.add(i);
            }
        });

        // 获取累加器的值
        System.out.println("----------> " + sumAcc.value());

        sc.stop();
    }
}
