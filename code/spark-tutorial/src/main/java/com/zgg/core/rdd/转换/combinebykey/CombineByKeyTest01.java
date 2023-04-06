package com.zgg.core.rdd.转换.combinebykey;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;
/**
 * 最通用的对 key-value 型 rdd 进行聚集操作的聚集函数（aggregation function）。
 * 类似于 aggregate()，combineByKey()允许用户返回值的类型与输入不一致。
 * */
public class CombineByKeyTest01 {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("CombineByKeyTest01");
        JavaSparkContext sc = new JavaSparkContext(conf);

        List<Tuple2<String, Integer>> data = new ArrayList<Tuple2<String, Integer>>();
        data.add(new Tuple2<String, Integer>("a", 1));
        data.add(new Tuple2<String, Integer>("a", 2));
        data.add(new Tuple2<String, Integer>("b", 3));
        data.add(new Tuple2<String, Integer>("b", 4));
        data.add(new Tuple2<String, Integer>("b", 5));
        data.add(new Tuple2<String, Integer>("a", 6));
        JavaPairRDD<String, Integer> rdd = sc.parallelizePairs(data, 2);

        /*
          和aggregateByKey的第一个初始值参数的区别：
               AggregateByKeyTest02.java的案例中，在一开始就假定了不是实际数据的第一个值，即初始值zerovalue，但计算个数的时候不将其计算在内。
              而combineByKey是将相同key的第一个实际数据转换初始值

           combineByKey : 方法需要三个参数
                第一个参数表示：将相同key的第一个数据进行结构的转换，实现操作 （下面的操作就是将其变成值为1的二元组）
                第二个参数表示：分区内的计算规则
                第三个参数表示：分区间的计算规则

         */

        JavaPairRDD<String, Tuple2<Integer, Integer>> pairRDD = rdd.combineByKey(
                new Function<Integer, Tuple2<Integer, Integer>>() {
                    @Override
                    public Tuple2<Integer, Integer> call(Integer v1) throws Exception {
                        return new Tuple2<>(v1,1);
                    }
                },
                new Function2<Tuple2<Integer, Integer>, Integer, Tuple2<Integer, Integer>>() {
                    @Override
                    public Tuple2<Integer, Integer> call(Tuple2<Integer, Integer> v1, Integer v2) throws Exception {
                        return new Tuple2<>(v1._1 + v2, v1._2 + 1);
                    }
                },
                new Function2<Tuple2<Integer, Integer>, Tuple2<Integer, Integer>, Tuple2<Integer, Integer>>() {

                    @Override
                    public Tuple2<Integer, Integer> call(Tuple2<Integer, Integer> v1, Tuple2<Integer, Integer> v2) throws Exception {
                        return new Tuple2<>(v1._1 + v2._1, v1._2 + v2._2);
                    }
                });

        JavaPairRDD<String, Integer> res = pairRDD.mapValues(new Function<Tuple2<Integer, Integer>, Integer>() {
            @Override
            public Integer call(Tuple2<Integer, Integer> v1) throws Exception {
                return v1._1 / v1._2;
            }
        });

        res.collect().forEach(System.out::println);
        sc.stop();
    }
}