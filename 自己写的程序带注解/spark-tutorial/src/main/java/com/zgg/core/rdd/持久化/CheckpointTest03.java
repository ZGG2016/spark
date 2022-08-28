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

public class CheckpointTest03 {
    public static void main(String[] args) {
        SparkConf sparkConf = new SparkConf().setAppName("CheckpointTest03").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);
        sc.setCheckpointDir("checkpoint");

        JavaRDD<String> data = sc.textFile("src/main/resources/wc.txt");
        JavaRDD<String> words = data.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public Iterator<String> call(String s) throws Exception {
                return Arrays.asList(s.split("\\|")).iterator();
            }
        });
        JavaPairRDD<String, Integer> pairRDD = words.mapToPair(new PairFunction<String, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(String s) throws Exception {
                return new Tuple2<String,Integer>(s,1);
            }
        });

        /*
          cache : 会在血缘关系中添加新的依赖。一旦，出现问题，可以重头读取数据
          checkpoint : 执行过程中，会切断血缘关系。重新建立新的血缘关系
                       checkpoint等同于改变数据源
         */

//        pairRDD.cache();
        pairRDD.checkpoint();

        System.out.println(pairRDD.toDebugString());
        JavaPairRDD<String, Integer> res1 = pairRDD.reduceByKey(Integer::sum);
        res1.collect().forEach(System.out::println);
        System.out.println("---------------------------------------------");
        System.out.println(pairRDD.toDebugString());

    }
}
