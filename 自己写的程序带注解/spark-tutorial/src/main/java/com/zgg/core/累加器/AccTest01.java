package com.zgg.core.累加器;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.VoidFunction;

import java.util.Arrays;
import java.util.List;

public class AccTest01 {
    public static void main(String[] args) {

        SparkConf sparkConf = new SparkConf().setMaster("local[*]").setAppName("AccTest01");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);

        List<Integer> lst = Arrays.asList(1, 2, 3, 4);
        JavaRDD<Integer> rdd1 = sc.parallelize(lst);

        int sum = 0;
        rdd1.foreach(new VoidFunction<Integer>() {
            @Override
            public void call(Integer i) throws Exception {
                System.out.println(sum + i);
            }
        });

        // foreach 算子在 executor 端执行，并不会将 executor 端执行的结果返回的 drvier 进行汇总计算
        // 所以，输出 0
        System.out.println("----------> " + sum);

        sc.stop();
    }
}
