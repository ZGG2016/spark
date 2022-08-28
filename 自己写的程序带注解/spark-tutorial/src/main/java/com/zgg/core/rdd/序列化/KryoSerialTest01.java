package com.zgg.core.rdd.序列化;

import com.esotericsoftware.kryo.Kryo;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.serializer.KryoRegistrator;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Kryo 序列化
 * */
public class KryoSerialTest01 {
    public static void main(String[] args) {
        SparkConf sparkConf = new SparkConf()
                .setAppName("KryoSerialTest01")
                .setMaster("local[*]")
                .set("spark.serializer","org.apache.spark.serializer.KryoSerializer")
                .registerKryoClasses(new Class[]{User.class});
        JavaSparkContext sc = new JavaSparkContext(sparkConf);

        List<Integer> data = Arrays.asList(1, 2, 3, 4);
        JavaRDD<Integer> rdd = sc.parallelize(data);

        User user = new User();

        rdd.foreach(new VoidFunction<Integer>() {
            @Override
            public void call(Integer integer) throws Exception {
                System.out.println(integer + user.age);
            }
        });

        sc.stop();
    }

    static class User implements Serializable {
        int age = 10;
    }
}
