package com.zgg.core.rdd.创建;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.util.Arrays;
import java.util.List;

/**
 * 从集合（内存）中创建 RDD  -  如何设置分区
 * */
public class Parallelize01 {
    public static void main(String[] args) {

        SparkConf sparkConf = new SparkConf().setMaster("local[*]").setAppName("RddPartitionParallelize01");
        sparkConf.set("spark.default.parallelism", String.valueOf(4));
        JavaSparkContext sc = new JavaSparkContext(sparkConf);

        /*
            parallelize方法的第二个参数numSlices表示分区数量

            1. 如果在parallelize方法指定了numSlices，那么就直接使用它作为分区数
            2. 如果在parallelize方法没有指定了numSlices，那么执行的就是 `parallelize(list, sc.defaultParallelism)`
                - parallelize(list, sc.defaultParallelism)
                - TaskSchedulerImpl: backend.defaultParallelism()
                - TODO] LocalSchedulerBackend: scheduler.conf.getInt("spark.default.parallelism", totalCores)
               所以，首先从 spark.default.parallelism 中获取配置参数，
                    如果获取不到，那么使用totalCores属性，这个属性取值为当前运行环境的最大可用核数

               而且，在 `parallelize(list, sc.defaultParallelism)` 中，也为numSlices指定了默认值
                    def parallelize[T: ClassTag](
                          seq: Seq[T],
                          numSlices: Int = defaultParallelism){}
         */

        List<Integer> lst = Arrays.asList(1, 2, 3, 4);
//        JavaRDD<Integer> rdd = sc.parallelize(lst,2);
        JavaRDD<Integer> rdd = sc.parallelize(lst);
        rdd.saveAsTextFile("output");

        sc.stop();
    }
}
