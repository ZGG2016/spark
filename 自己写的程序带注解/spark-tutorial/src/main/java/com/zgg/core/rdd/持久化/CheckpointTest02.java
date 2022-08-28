package com.zgg.core.rdd.持久化;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

import java.util.Arrays;
import java.util.Iterator;

public class CheckpointTest02 {
    public static void main(String[] args) {
        SparkConf sparkConf = new SparkConf().setAppName("CheckpointTest02").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);
        sc.setCheckpointDir("checkpoint");

        JavaRDD<String> data = sc.textFile("src/main/resources/wc.txt");
        JavaRDD<String> words = data.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public Iterator<String> call(String s) throws Exception {
                System.out.println("调用了一次flatmap......");
                return Arrays.asList(s.split("\\|")).iterator();
            }
        });

        JavaPairRDD<String, Integer> pairRDD = words.mapToPair(new PairFunction<String, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(String s) throws Exception {
                return new Tuple2<String,Integer>(s,1);
            }
        });

        pairRDD.cache();
        pairRDD.checkpoint();

        JavaPairRDD<String, Integer> res1 = pairRDD.reduceByKey(Integer::sum);
        res1.collect().forEach(System.out::println);

        System.out.println("---------------------------------------------");
        /*
          cache : 将数据临时存储在内存中进行数据重用
          persist : 将数据临时存储在磁盘文件中进行数据重用
                    涉及到磁盘IO，性能较低，但是数据安全
                    如果作业执行完毕，临时保存的数据文件就会丢失
          checkpoint : 将数据长久地保存在磁盘文件中进行数据重用
                       涉及到磁盘IO，性能较低，但是数据安全
                       为了保证数据安全，所以一般情况下，会独立执行作业  (见 checkpoint源码.md)
                       为了能够提高效率，一般情况下，是需要和cache联合使用
                       （此时，不会执行两遍 textFile -> flatMap -> mapToPair）
         */
        JavaPairRDD<String, Iterable<Integer>> res2 = pairRDD.groupByKey();
        res2.collect().forEach(System.out::println);

    }
}
