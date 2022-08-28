package com.zgg.core.rdd.转换.mappartition;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;

import java.util.*;
/**
 * 将待处理的数据以分区为单位发送到计算节点进行处理，这里的处理是指可以进行任意的处
 * 理，哪怕是过滤数据。
 * */
public class MapPartitionsTest01 {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("MapPartitionsTest01");
        JavaSparkContext sc = new JavaSparkContext(conf);

        List<Integer> data = Arrays.asList(1, 2, 3, 4);
        JavaRDD<Integer> rdd = sc.parallelize(data);
        /*
        * 为每个分区调用一次 FlatMapFunction
        * 但是会将整个分区的数据加载到内存进行引用
        * 如果处理完的数据是不会被释放掉，存在对象的引用。
        * 在内存较小，数据量较大的场合下，容易出现内存溢出。
        * */
        JavaRDD<Integer> res1 = rdd.mapPartitions(new FlatMapFunction<Iterator<Integer>, Integer>() {
            @Override
            public Iterator<Integer> call(Iterator<Integer> integerIterator) throws Exception {
                ArrayList<Integer> tmp = new ArrayList<>();
                while (integerIterator.hasNext()){
                    tmp.add(integerIterator.next() * 2);
                }
                return tmp.iterator();
            }
        });
        res1.collect().forEach(System.out::println);
        sc.stop();
    }
}
