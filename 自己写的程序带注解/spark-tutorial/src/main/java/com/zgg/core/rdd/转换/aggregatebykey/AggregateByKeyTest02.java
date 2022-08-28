package com.zgg.core.rdd.转换.aggregatebykey;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;

public class AggregateByKeyTest02 {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("AggregateByKeyTest02");
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
           获取相同key的数据的平均值 => (a, 3),(b, 4)
           def aggregateByKey[U: ClassTag](zeroValue: U, partitioner: Partitioner)
                                          (seqOp: (U, V) => U, combOp: (U, U) => U): RDD[(K, U)]

           zeroValue 的类型是 U

           分区内计算逻辑 seqOp 的第一次参数的类型就是 zeroValue 的类型 U，第二个参数就传来的参数，
               所以，一开始，是 zeroValue 和 第一个传来的值（这个值是键值对元素中的值）进行计算，得到一个类型为U的结果 ，然后这个类型为U的结果再和传来的值进行计算，依次往下

           分区间计算逻辑 combOp 的输入参数和输出参数类型一致都是 zeroValue 的类型
           aggregateByKey最终的返回数据结果应该和初始值的类型保持一致
         */

        // zerovalue 第一个是总和，第二个是个数
        JavaPairRDD<String, Tuple2<Integer, Integer>> pairRDD = rdd.aggregateByKey(new Tuple2<Integer, Integer>(0, 0),
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