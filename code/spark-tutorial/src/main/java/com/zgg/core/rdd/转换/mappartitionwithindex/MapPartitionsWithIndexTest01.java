package com.zgg.core.rdd.转换.mappartitionwithindex;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;

import java.util.*;

/**
 * 将待处理的数据以分区为单位发送到计算节点进行处理，这里的处理是指可以进行任意的处
 * 理，哪怕是过滤数据，在处理时同时可以获取当前分区索引。
 */
public class MapPartitionsWithIndexTest01 {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("MapPartitionsWithIndexTest01");
        JavaSparkContext sc = new JavaSparkContext(conf);

        List<Integer> data = Arrays.asList(1, 2, 3, 4);
        JavaRDD<Integer> rdd = sc.parallelize(data, 2);
        /*
         * 为每个分区调用一次 Function2
         *
         * 参数preservesPartitioning表示是否保留父RDD的partitioner分区信息。
         * 当不修改分区时，将它设置为false，如果您需要修改分区时，将它设置为true
         * */
        JavaRDD<Integer> res1 = rdd.mapPartitionsWithIndex(new Function2<Integer, Iterator<Integer>, Iterator<Integer>>() {
            @Override
            public Iterator<Integer> call(Integer v1, Iterator<Integer> v2) throws Exception {
                System.out.println(">>>>>>>>>>>>>>>>> " + v1); // 0 1  分区索引从0开始
                // 获取第二个数据分区的数据
                if (v1 == 1) {
                    return v2;
                }
                return Collections.emptyIterator();
            }
        }, false);

        res1.collect().forEach(System.out::println);
        sc.stop();
    }
}
