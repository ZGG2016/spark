package com.zgg.core.rdd.行动;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.VoidFunction;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * foreach: 分布式遍历 RDD 中的每一个元素，调用指定函数
 * */
public class ActionTest05 {
    public static void main(String[] args) {
        SparkConf sparkConf = new SparkConf().setAppName("ActionTest05").setMaster("local[*]");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);

        List<Integer> data = Arrays.asList(1, 2, 3, 4);
        JavaRDD<Integer> rdd = sc.parallelize(data);
        /*
            算子 ： Operator（操作）
                RDD的方法和Scala集合对象的方法不一样
                集合对象的方法都是在同一个节点的内存中完成的。
                RDD的方法可以将计算逻辑发送到Executor端（分布式节点）执行
                为了区分不同的处理效果，所以将RDD的方法称之为算子。
                RDD的方法外部的操作都是在Driver端执行的，而方法内部的逻辑代码是在Executor端执行。
         */

        // Driver端内存集合的循环遍历方法
        rdd.collect().forEach(System.out::println);

        // Executor端内存数据打印
        rdd.foreach(new VoidFunction<Integer>() {
            @Override
            public void call(Integer integer) throws Exception {
                System.out.println(integer);
            }
        });

        sc.stop();
    }
}
