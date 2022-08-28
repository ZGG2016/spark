package com.zgg.core.rdd.转换.map;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;

import java.util.Arrays;
import java.util.List;
/**
 * 理解rdd的并行计算
 *   一个分区内的数据是一个一个执行逻辑
 *       - 只有前面一个数据全部的逻辑执行完毕后，才会执行下一个数据
 *       - 分区内数据的执行是有序的
 *       - 所以，1, 2, 3, 4 依次输出
 *   不同分区数据计算是无序的
 *       - 分区1： 1, 2  依次输出
 *       - 分区2： 3, 4  依次输出
 *       - 但 1, 3谁先输出就不确定
 * */
public class MapTest03 {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("MapTest03");
        JavaSparkContext sc = new JavaSparkContext(conf);

        List<Integer> data = Arrays.asList(1, 2, 3, 4);
//        JavaRDD<Integer> rdd = sc.parallelize(data,1);
        JavaRDD<Integer> rdd = sc.parallelize(data,2);
        JavaRDD<Integer> res1 = rdd.map(new Function<Integer, Integer>() {
            @Override
            public Integer call(Integer v1) throws Exception {
                System.out.println(">>>>>>>>>> " + v1);
                return v1;
            }
        });

        JavaRDD<Integer> res2 = res1.map(new Function<Integer, Integer>() {
            @Override
            public Integer call(Integer v1) throws Exception {
                System.out.println("---------> " + v1);
                return v1;
            }
        });

        res2.collect();
        sc.stop();

    }
}
