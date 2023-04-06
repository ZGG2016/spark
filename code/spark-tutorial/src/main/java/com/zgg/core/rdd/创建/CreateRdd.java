package com.zgg.core.rdd.创建;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.util.Arrays;
import java.util.List;

/**
 * 创建RDD:
 *   - 从集合（内存）中创建 RDD
 *   - 从外部存储（文件）创建 RDD
 *   - 从其他 RDD 创建  （转换操作）
 *   - 直接创建 RDD（new）
 * */
public class CreateRdd {
    public static void main(String[] args) {

        SparkConf sparkConf = new SparkConf().setMaster("local[*]").setAppName("CreateRdd");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);

        // 1. 从集合（内存）中创建 RDD
//        List<Integer> lst = Arrays.asList(1, 2, 3, 4);
//        JavaRDD<Integer> rdd1 = sc.parallelize(lst);
//        rdd1.collect().forEach(System.out::println);

        //////////////////////////////////////////////

        /*
          2. 从外部存储（文件）创建 RDD
                 - path路径默认以当前环境的根路径为基准。可以写绝对路径，也可以写相对路径
                 - path路径可以是文件的具体路径，也可以目录名称
                 - path路径还可以使用通配符 *
                 - path还可以是分布式存储系统路径：HDFS
             textFile 以行为单位来读取数据，读取的数据都是字符串
             wholeTextFiles  以文件为单位读取数据
                 - 读取的结果表示为元组，第一个元素表示文件路径，第二个元素表示文件内容
         */

//        JavaRDD<String> rdd2 = sc.textFile("src/main/resources/wc.txt");
//        JavaRDD<String> rdd2 = sc.textFile("src/main/resources");
//        JavaRDD<String> rdd2 = sc.textFile("src/main/resources/wc.*");
//        JavaRDD<String> rdd2 = sc.textFile("hdfs://bigdata101:8020/wc.txt");
        JavaPairRDD<String, String> rdd2 = sc.wholeTextFiles("src/main/resources/wc.txt");
        rdd2.collect().forEach(System.out::println);


        sc.stop();
    }
}
