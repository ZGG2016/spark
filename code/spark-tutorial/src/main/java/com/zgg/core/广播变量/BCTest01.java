package com.zgg.core.广播变量;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.broadcast.Broadcast;

import java.util.Arrays;
import java.util.List;

/**
 * 广播变量用来高效分发较大的对象。向所有工作节点发送一个较大的只读值，以供一个或多个 Spark 操作使用
 */
public class BCTest01 {
    public static void main(String[] args) {

        SparkConf sparkConf = new SparkConf().setMaster("local[*]").setAppName("BCTest01");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);

        List<String> data = Arrays.asList("hello", "spark", "hadoop");
        JavaRDD<String> rdd = sc.parallelize(data);

        String bcString = "spark";

        Broadcast<String> bc = sc.broadcast(bcString);
        JavaRDD<String> res = rdd.filter(new Function<String, Boolean>() {
            @Override
            public Boolean call(String v1) throws Exception {
                return !v1.equals(bc.value());
            }
        });
        res.collect().forEach(System.out::println);

        sc.stop();

    }
}
