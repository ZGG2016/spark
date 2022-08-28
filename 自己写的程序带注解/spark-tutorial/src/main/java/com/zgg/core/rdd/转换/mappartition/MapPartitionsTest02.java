package com.zgg.core.rdd.转换.mappartition;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;

import java.util.*;

/**
 * 小功能：获取每个数据分区的最大值
 * */
public class MapPartitionsTest02 {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("MapPartitionsTest02");
        JavaSparkContext sc = new JavaSparkContext(conf);

        List<Integer> data = Arrays.asList(1, 2, 3, 4);
        JavaRDD<Integer> rdd = sc.parallelize(data,2);

        JavaRDD<Integer> res1 = rdd.mapPartitions(new FlatMapFunction<Iterator<Integer>, Integer>() {
            @Override
            public Iterator<Integer> call(Iterator<Integer> integerIterator) throws Exception {
                ArrayList<Integer> tmp = new ArrayList<>();
                while (integerIterator.hasNext()){
                    tmp.add(integerIterator.next());
                }
                return Collections.singletonList(Collections.max(tmp)).iterator();
            }
        });
        res1.collect().forEach(System.out::println);
        sc.stop();
    }
}

/*
map 和 mapPartitions 的区别？

➢ 数据处理角度
Map 算子是分区内一个数据一个数据的执行，类似于串行操作。

而 mapPartitions 算子是以分区为单位进行批处理操作。

➢ 功能的角度
Map 算子主要目的将数据源中的数据进行转换和改变。但是不会减少或增多数据。

MapPartitions 算子需要传递一个迭代器，返回一个迭代器，没有要求的元素的个数保持不变，所以可以增加或减少数据

➢ 性能的角度
Map 算子因为类似于串行操作，所以性能比较低，

而是 mapPartitions 算子类似于批处理，所以性能较高。
但是 mapPartitions 算子会长时间占用内存，那么这样会导致内存可能不够用，出现内存溢出的错误。所以在内存有限的情况下，不推荐使用。使用 map 操作。
 */
