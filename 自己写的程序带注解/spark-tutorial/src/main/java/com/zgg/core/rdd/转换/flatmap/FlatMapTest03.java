package com.zgg.core.rdd.转换.flatmap;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;

import java.util.*;

/**
 * 练习：将 List(List(1,2),3,List(4,5))进行扁平化操作
 * */
public class FlatMapTest03 {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("FlatMapTest02");
        JavaSparkContext sc = new JavaSparkContext(conf);

        List<Object> data = new ArrayList<>();
        data.add(Arrays.asList(1,2));
        data.add(3);
        data.add(Arrays.asList(4,5));
        JavaRDD<Object> rdd = sc.parallelize(data);

        JavaRDD<Integer> res = rdd.flatMap(new FlatMapFunction<Object, Integer>() {
            @Override
            public Iterator<Integer> call(Object o) throws Exception {
//                System.out.println(o instanceof List);
                if (o instanceof List){
                    return ((List) o).iterator();
                }else if (o instanceof Integer){
                    return Collections.singletonList((Integer) o).iterator();
                }
                return null;
            }
        });
        res.collect().forEach(System.out::println);
        sc.stop();
    }
}
