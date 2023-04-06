package com.zgg.core.rdd.转换.combinebykey;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;

/**
 * reduceByKey、foldByKey、aggregateByKey、combineByKey 的区别？
 *   - reduceByKey: 相同 key 的第一个数据不进行任何计算，分区内和分区间计算规则相同
 *   - foldByKey: 相同 key 的第一个数据和初始值进行分区内计算，分区内和分区间计算规则相同
 *   - aggregateByKey：相同 key 的第一个数据和初始值进行分区内计算，分区内和分区间计算规则可以不相同
 *   - combineByKey:当计算时，发现数据结构不满足要求时，可以让第一个数据转换结构。分区内和分区间计算规则不相同。
 * */
public class ByKeyTest01 {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("ByKeyTest01");
        JavaSparkContext sc = new JavaSparkContext(conf);

        List<Tuple2<String, Integer>> data = new ArrayList<Tuple2<String, Integer>>();
        data.add(new Tuple2<String, Integer>("a", 1));
        data.add(new Tuple2<String, Integer>("a", 2));
        data.add(new Tuple2<String, Integer>("b", 3));
        data.add(new Tuple2<String, Integer>("b", 4));
        data.add(new Tuple2<String, Integer>("b", 5));
        data.add(new Tuple2<String, Integer>("a", 6));
        JavaPairRDD<String, Integer> rdd = sc.parallelizePairs(data, 2);

        /*
        reduceByKey:

             combineByKeyWithClassTag[V](
                 (v: V) => v, // 第一个值不会参与计算
                 func, // 分区内计算规则
                 func, // 分区间计算规则
                 )

        aggregateByKey :

            combineByKeyWithClassTag[U](
                (v: V) => cleanedSeqOp(createZero(), v), // 初始值和第一个key的value值进行的分区内数据操作
                cleanedSeqOp, // 分区内计算规则
                combOp,       // 分区间计算规则
                )

        foldByKey:

            combineByKeyWithClassTag[V](
                (v: V) => cleanedFunc(createZero(), v), // 初始值和第一个key的value值进行的分区内数据操作
                cleanedFunc,  // 分区内计算规则
                cleanedFunc,  // 分区间计算规则
                )

        combineByKey :

            combineByKeyWithClassTag(
                createCombiner,  // 相同key的第一条数据进行的处理函数
                mergeValue,      // 表示分区内数据的处理函数
                mergeCombiners,  // 表示分区间数据的处理函数
                )

         */

        JavaPairRDD<String, Integer> reduceByKey = rdd.reduceByKey(Integer::sum);
        JavaPairRDD<String, Integer> aggregateByKey = rdd.aggregateByKey(0, Integer::sum, Integer::sum);
        JavaPairRDD<String, Integer> foldByKey = rdd.foldByKey(0, Integer::sum);
        JavaPairRDD<String, Integer> combineByKey = rdd.combineByKey((Function<Integer, Integer>) v1 -> v1, Integer::sum, Integer::sum);

        reduceByKey.collect().forEach(System.out::println);
        aggregateByKey.collect().forEach(System.out::println);
        foldByKey.collect().forEach(System.out::println);
        combineByKey.collect().forEach(System.out::println);
        sc.stop();
    }
}