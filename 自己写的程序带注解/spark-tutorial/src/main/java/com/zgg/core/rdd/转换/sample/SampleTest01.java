package com.zgg.core.rdd.转换.sample;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;

import java.util.Arrays;
import java.util.List;

/**
 * 根据指定的规则从数据集中抽取数据   （用在处理数据倾斜的场景中，抽取出一个分区的数据，查看数据的分布情况）
 */
public class SampleTest01 {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("SampleTest01");
        JavaSparkContext sc = new JavaSparkContext(conf);

        List<Integer> data = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        JavaRDD<Integer> rdd = sc.parallelize(data);

        /*
         sample算子需要传递三个参数
            1. 第一个参数表示，抽取数据后是否将数据返回 true（放回），false（丢弃）
            2. 第二个参数表示，
                 如果抽取不放回的场合：数据源中每条数据被抽取的概率，基准值的概念
                 如果抽取放回的场合：表示数据源中的每条数据被抽取的可能次数
            3. 第三个参数表示，抽取数据时随机算法的种子 【随机种子：一种以随机数作为对象的以真随机数（种子）为初始条件的随机数】
                 如果不传递第三个参数，那么使用的是当前系统时间
        * */
//        JavaRDD<Integer> res = rdd.sample(false, 0.4, 1);
//        JavaRDD<Integer> res = rdd.sample(false, 0.5, 1);
//        JavaRDD<Integer> res = rdd.sample(false, 0.4);
        JavaRDD<Integer> res = rdd.sample(true, 2);
        res.collect().forEach(System.out::println);

        sc.stop();
    }
}
