package com.zgg.core.rdd.转换.mappartitionwithindex;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;
import scala.Tuple2;

import java.util.*;

/**
 * 练习：查看数据都在哪个分区里
 * 格式  （分区索引，数据）
 * 如    （0，1）
 */
public class MapPartitionsWithIndexTest02 {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("MapPartitionsWithIndexTest02");
        JavaSparkContext sc = new JavaSparkContext(conf);

        List<Integer> data = Arrays.asList(1, 2, 3, 4);
        JavaRDD<Integer> rdd = sc.parallelize(data, 2);

        JavaRDD<Tuple2<Integer, Integer>> res1 = rdd.mapPartitionsWithIndex(new Function2<Integer, Iterator<Integer>, Iterator<Tuple2<Integer, Integer>>>() {
            @Override
            public Iterator<Tuple2<Integer, Integer>> call(Integer v1, Iterator<Integer> v2) throws Exception {
                ArrayList<Tuple2<Integer, Integer>> vals = new ArrayList<>();
                while (v2.hasNext()) {
                    vals.add(new Tuple2<Integer, Integer>(v1, v2.next()));
                }
                return vals.iterator();
            }
        }, false);
        res1.collect().forEach(System.out::println);
        sc.stop();
    }
}
