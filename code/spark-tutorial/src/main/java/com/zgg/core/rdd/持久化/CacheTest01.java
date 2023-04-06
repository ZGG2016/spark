package com.zgg.core.rdd.持久化;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.storage.StorageLevel;
import scala.Tuple2;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
/**
 * RDD 通过 Cache 或者 Persist 方法将前面的计算结果缓存，
 * 默认情况下会把数据以缓存在 JVM 的堆内存中。
 * 但是并不是这两个方法被调用时立即缓存，而是触发后面的 action 算子时，该 RDD 将会被缓存在计算节点的内存中，并供后面重用
 * */
public class CacheTest01 {
    public static void main(String[] args) {
        SparkConf sparkConf = new SparkConf().setAppName("CacheTest01").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);

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

//        pairRDD.cache();
        pairRDD.persist(StorageLevel.MEMORY_AND_DISK());

        JavaPairRDD<String, Integer> res1 = pairRDD.reduceByKey(Integer::sum);
        res1.collect().forEach(System.out::println);

        System.out.println("---------------------------------------------");
        /*
           textFile -> flatMap -> mapToPair 依次执行完成后，分别执行 reduceByKey 和 groupByKey
           此时，执行 reduceByKey 时，会先执行一遍 `textFile -> flatMap -> mapToPair` 流程。
               再执行 groupByKey 时，也会再执行一遍 `textFile -> flatMap -> mapToPair` 流程。
               通过查看 flatmap 算子中的输出可以发现。

           因为 RDD 中不存储数据，所以只能重用 RDD 对象，无法重用数据
           此时，可以在执行完 mapToPair 之后，将数据缓存，就可以重用数据。
           通过查看 flatmap 算子中的输出可以发现。

           cache 底层调用的是 def persist(): this.type = persist(StorageLevel.MEMORY_ONLY)
           如果想自定义存储级别，可以直接使用 persist 方法

           持久化操作并不是被调用时立即缓存，而是触发后面的 action 算子时，该 RDD 将会被缓存在计算节点的内存中，并供后面重用。

           当数据执行较长，或数据比较重要的场景也可以使用
         */
        JavaPairRDD<String, Iterable<Integer>> res2 = pairRDD.groupByKey();
        res2.collect().forEach(System.out::println);

    }
}
