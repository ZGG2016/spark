package com.zgg.core.累加器;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.util.AccumulatorV2;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * 累加器实现wordcount
 */
public class AccTest04 {
    public static void main(String[] args) {

        SparkConf sparkConf = new SparkConf().setMaster("local[*]").setAppName("AccTest02");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);

        List<String> lst = Arrays.asList("hello", "spark", "hello");
        JavaRDD<String> rdd1 = sc.parallelize(lst);

        // 1. 创建累加器对象
        wcAccumulator wcAcc = new wcAccumulator();
        // 2. 向Spark进行注册
        sc.sc().register(wcAcc, "wordCountAcc");

        rdd1.foreach(new VoidFunction<String>() {
            @Override
            public void call(String s) throws Exception {
                // 3. 数据的累加（使用累加器）
                wcAcc.add(s);
            }
        });

        // 4. 获取累加器累加的结果
        System.out.println(wcAcc.value());
        sc.stop();
    }

    /*
      自定义数据累加器：WordCount

      1. 继承AccumulatorV2, 定义泛型
         IN :  累加器输入的数据类型 String
         OUT : 累加器返回的数据类型 mutable.Map[String, Long]

      2. 重写方法（6）
     */

    private static class wcAccumulator extends AccumulatorV2<String, HashMap<String, Integer>> {

        HashMap<String, Integer> hm = new HashMap<>();

        // 判断是否初始状态
        @Override
        public boolean isZero() {
            return this.hm.isEmpty();
        }

        @Override
        public AccumulatorV2<String, HashMap<String, Integer>> copy() {
            wcAccumulator wcAccumulator = new wcAccumulator();
            wcAccumulator.hm = this.hm;
            return wcAccumulator;
        }

        @Override
        public void reset() {
            this.hm.clear();
        }

        // 获取累加器需要计算的值
        @Override
        public void add(String v) {
            String[] splits = v.split("\\|");
            for (String s : splits) {
                int value = this.hm.getOrDefault(s, 0) + 1;
                this.hm.put(s, value);
            }
        }

        // Driver合并多个累加器
        @Override
        public void merge(AccumulatorV2<String, HashMap<String, Integer>> other) {
            other.value().forEach((k, v) -> {
                if (this.hm.containsKey(k)) {
                    Integer i1 = this.hm.get(k);
                    Integer i2 = other.value().get(k);
                    this.hm.put(k, i1 + i2);
                } else {
                    this.hm.put(k, v);
                }
            });
        }

        // 累加器结果
        @Override
        public HashMap<String, Integer> value() {
            return this.hm;
        }
    }
}
