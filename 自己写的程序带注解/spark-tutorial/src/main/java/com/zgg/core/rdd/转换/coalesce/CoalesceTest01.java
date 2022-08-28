package com.zgg.core.rdd.转换.coalesce;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.util.Arrays;
import java.util.List;

/**
 * 根据数据量缩减分区，用于大数据集过滤后，提高小数据集的执行效率
 * 当 spark 程序中，存在过多的小任务的时候，可以通过 coalesce 方法，收缩合并分区，减少分区的个数，减小任务调度成本
 */
public class CoalesceTest01 {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("CoalesceTest01");
        JavaSparkContext sc = new JavaSparkContext(conf);

        List<Integer> data = Arrays.asList(1, 2, 3, 4, 5, 6);
        JavaRDD<Integer> rdd = sc.parallelize(data,3);
        /*
           coalesce方法默认情况下不会将分区的数据打乱重新组合
              这种情况下的缩减分区可能会导致数据不均衡，出现数据倾斜
              如果想要让数据均衡，可以进行shuffle处理
         */
//        rdd.coalesce(2).saveAsTextFile("output");
        rdd.coalesce(2,true).saveAsTextFile("output");

        sc.stop();
    }
}
