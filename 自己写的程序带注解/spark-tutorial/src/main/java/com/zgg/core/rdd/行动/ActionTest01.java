package com.zgg.core.rdd.行动;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;

import java.util.Arrays;
import java.util.List;

public class ActionTest01 {
    public static void main(String[] args) {
        SparkConf sparkConf = new SparkConf().setAppName("ActionTest01").setMaster("local[*]");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);

        List<Integer> data = Arrays.asList(3, 1, 2, 4);
        JavaRDD<Integer> rdd = sc.parallelize(data, 2);

        /*
          行动算子
             所谓的行动算子，其实就是触发作业(Job)执行的方法
             底层代码调用的是环境对象的runJob方法
             底层代码中会创建ActiveJob，并提交执行。
        */

        // reduce  聚集 RDD 中的所有元素，先聚合分区内数据，再聚合分区间数据
        Integer res1 = rdd.reduce(new Function2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer v1, Integer v2) throws Exception {
                return v1 + v2;
            }
        });
        System.out.println(res1);

        // collect 方法会将不同分区的数据按照分区顺序采集到Driver端内存中，形成数组
        List<Integer> res2 = rdd.collect();
        System.out.println(res2);

        // count 返回 RDD 中元素的个数
        long res3 = rdd.count();
        System.out.println(res3);

        // first 返回 RDD 中的第一个元素
        Integer res4 = rdd.first();
        System.out.println(res4);

        // take 返回一个由 RDD 的前 n 个元素组成的数组
        List<Integer> res5 = rdd.take(3);
        System.out.println(res5);

        // takeOrdered返回该 RDD 排序后的前 n 个元素组成的数组
        List<Integer> res6 = rdd.takeOrdered(3);
        System.out.println(res6);

        sc.stop();
    }
}
