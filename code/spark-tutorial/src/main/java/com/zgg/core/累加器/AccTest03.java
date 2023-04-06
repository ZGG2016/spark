package com.zgg.core.累加器;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.util.LongAccumulator;

import java.util.Arrays;
import java.util.List;

/**
 * 累加器用来把 Executor 端变量信息聚合到 Driver 端。
 * 在 Driver 程序中定义的变量，在 Executor 端的每个 Task 都会得到这个变量的一份新的副本，
 * 每个 task 更新这些副本的值后，传回 Driver 端进行 merge。
 * */
public class AccTest03 {
    public static void main(String[] args) {

        SparkConf sparkConf = new SparkConf().setMaster("local[*]").setAppName("AccTest02");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);

        List<Integer> lst = Arrays.asList(1, 2, 3, 4);
        JavaRDD<Integer> rdd1 = sc.parallelize(lst);

        LongAccumulator sumAcc = sc.sc().longAccumulator("sum");

        JavaRDD<Integer> mapRDD = rdd1.map(new Function<Integer, Integer>() {
            @Override
            public Integer call(Integer v1) throws Exception {
                sumAcc.add(v1);
                return v1;
            }
        });

        /*
           少加：转换算子中调用累加器，如果没有行动算子的话，那么不会执行   【输出0】
           多加：转换算子中调用累加器，如果有多个行动算子的话，那么会累加这多个行动算子的结果    【输出20】
           所以，一般情况下，累加器会放置在行动算子进行操作  【比如foreach】
         */

//        mapRDD.collect();
//        mapRDD.collect(); // 这样的话就会执行两次job，将执行两次job的结果相加

        System.out.println("----------> " + sumAcc.value());

        sc.stop();
    }
}
