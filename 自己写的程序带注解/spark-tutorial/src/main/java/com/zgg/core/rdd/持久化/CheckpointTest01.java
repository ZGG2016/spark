package com.zgg.core.rdd.持久化;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.storage.StorageLevel;
import scala.Tuple2;

import java.util.Arrays;
import java.util.Iterator;
/**
 * 检查点其实就是通过将 RDD 中间结果写入磁盘
 * 由于血缘依赖过长会造成容错成本过高，这样就不如在中间阶段做检查点容错，
 * 如果检查点之后有节点出现问题，可以从检查点开始重做血缘，减少了开销。
 *
 * 对 RDD 进行 checkpoint 操作并不会马上被执行，必须执行 Action 操作才能触发。
 * */
public class CheckpointTest01 {
    public static void main(String[] args) {
        SparkConf sparkConf = new SparkConf().setAppName("CheckpointTest01").setMaster("local");
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

        pairRDD.checkpoint();

        JavaPairRDD<String, Integer> res1 = pairRDD.reduceByKey(Integer::sum);
        res1.collect().forEach(System.out::println);

        System.out.println("---------------------------------------------");
        /*
           checkpoint 需要落盘，需要指定检查点保存路径
             检查点路径保存的文件，当作业执行完毕后，不会被删除
             一般保存路径都是在分布式存储系统：HDFS

           此时，也会执行两遍 textFile -> flatMap -> mapToPair
           其中一遍是用来checkpoint
         */
        JavaPairRDD<String, Iterable<Integer>> res2 = pairRDD.groupByKey();
        res2.collect().forEach(System.out::println);

    }
}
