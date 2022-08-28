package com.zgg.core.rdd.转换.map;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.VoidFunction;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
/**
 * 将处理的数据逐条进行映射转换，这里的转换可以是类型的转换，也可以是值的转换。
 * */
public class MapTest01 {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("MapTest01");
        JavaSparkContext sc = new JavaSparkContext(conf);

        List<Integer> data = Arrays.asList(1, 2, 3, 4);
        JavaRDD<Integer> rdd = sc.parallelize(data);
        // 为每条数据调用一次 Function  , 泛型一个输入类型，一个输出类型
        JavaRDD<Integer> res1 = rdd.map(new Function<Integer, Integer>() {
            @Override
            public Integer call(Integer v1) throws Exception {
                return v1 * 2;
            }
        });
        res1.collect().forEach(System.out::println);

        sc.stop();
    }
}
