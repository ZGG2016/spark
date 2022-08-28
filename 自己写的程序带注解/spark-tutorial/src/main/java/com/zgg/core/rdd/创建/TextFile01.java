package com.zgg.core.rdd.创建;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.util.Arrays;
import java.util.List;

/**
 * 从外部存储（文件）创建 RDD  -  如何设置分区，数据如何分配
 * */
public class TextFile01 {
    public static void main(String[] args) {

        SparkConf sparkConf = new SparkConf().setMaster("local[*]").setAppName("RddPartitionTextFile01");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);

        /*
            textFile方法的第二个参数minPartitions表示最小分区数量

            1. 如果在textFile方法指定了minPartitions，那么就直接使用它作为最小分区数量
            2. 如果在textFile方法没有指定了minPartitions，那么执行的就是
                - def textFile(path: String,
                      minPartitions: Int = defaultMinPartitions): RDD[String]={}
                - def defaultMinPartitions: Int = math.min(defaultParallelism, 2)
                - TaskSchedulerImpl: backend.defaultParallelism()
                - LocalSchedulerBackend: scheduler.conf.getInt("spark.default.parallelism", totalCores)
               所以，首先从 spark.default.parallelism 中获取配置参数，
                    如果获取不到，那么使用 totalCores 属性，这个属性取值为当前运行环境的最大可用核数，
                    取到 defaultParallelism 后，再取和2的最小值作为默认的最小分区数量
         */

         /*
            但最小分区数量并不是结果RDD的实际分区数量

            def textFile(path: String, minPartitions: Int = defaultMinPartitions): RDD[String] = withScope {
                hadoopFile(path, classOf[TextInputFormat], classOf[LongWritable], classOf[Text],
                  minPartitions).map(pair => pair._2.toString).setName(path)
              }

            在hadoop中，InputFormat有一个getSplits方法用来切分数据，使数据进入不同的分区处理。
            所以，这里使用的就是Hadoop的读取方式:
                 - 数据以行为单位进行读取
                 - 数据读取时以偏移量为单位,偏移量不会被重复读取

            找到 InputFormat 的实现类，TextInputFormat 的父类 FileInputFormat 的 getSplits方 法

            见同目录下的 `textfile分区方式.md`

            `1.txt`文件内容： （共7个字节）
            1 (回车换行)
            2 (回车换行)
            3
            -------------------
            内容   偏移量
            1@@   0 1 2
            2@@   3 4 5
            3     6
            -------------------
            分区   偏移量范围    内容
             0      [0,3]      1 2
             1      [3,6]      3
             2      [6,7]
            -------------------
             视频中的方法：
             totalSize = 7
             goalSize =  7 / 2 = 3（byte）
             7 / 3 = 2...1 (1.1) + 1 = 3(分区)

          */

        JavaRDD<String> rdd = sc.textFile("src/main/resources/1.txt", 2);
//        JavaRDD<String> rdd = sc.textFile("src/main/resources/1.txt");
        System.out.println("========= " + rdd.getNumPartitions() + " =========" );   // 3
        rdd.saveAsTextFile("output");

        sc.stop();
    }
}
