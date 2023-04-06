package com.zgg.core.rdd.序列化;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.VoidFunction;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * 闭包检测： RDD算子中传递的函数是会包含闭包操作，那么就会进行检测功能
 * */
public class SerialTest01 {
    public static void main(String[] args) {
        SparkConf sparkConf = new SparkConf().setAppName("SerialTest01").setMaster("local[*]");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);

        List<Integer> data = Arrays.asList(1, 2, 3, 4);
        JavaRDD<Integer> rdd = sc.parallelize(data);
        /*
          报异常：
            Exception in thread "main" org.apache.spark.SparkException: Task not serializable
            ...
            Caused by: java.io.NotSerializableException: com.zgg.core.rdd.序列化.ActionTest06$User

            foreach 算子是在Executor端执行的，所以需要将User对象序列化后从Driver传给Executor

            所以在 foreach 算子中，要进行闭包检测：

            val cleanF = sc.clean(f)
            --->
            private[spark] def clean[F <: AnyRef](f: F, checkSerializable: Boolean = true): F = {
                ClosureCleaner.clean(f, checkSerializable)
                f
              }
            --->
            if (checkSerializable) {
              ensureSerializable(func)
            }

            所以，User类需要继承Serializable接口
         */

        User user = new User();

        rdd.foreach(new VoidFunction<Integer>() {
            @Override
            public void call(Integer integer) throws Exception {
                System.out.println(integer + user.age);
            }
        });

        sc.stop();
    }

//    static class User {
    static class User implements Serializable {
        int age = 10;
    }
}
