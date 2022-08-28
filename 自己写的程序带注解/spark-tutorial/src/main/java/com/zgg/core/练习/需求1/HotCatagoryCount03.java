package com.zgg.core.练习.需求1;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.*;
import scala.Tuple2;
import scala.Tuple3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * 优化
 */
public class HotCatagoryCount03 {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setAppName("HotCatagoryCount03").setMaster("local[*]");
        JavaSparkContext sc = new JavaSparkContext(conf);

        /*
           存在的问题：
              - 存在了大量的 shuffle 操作  (reduceByKey)


            在 HotCatagoryCount02.java 中，先计算并转成（品类ID，点击数量） （品类ID，下单数量） （品类ID，支付数量） 结构 (reduceByKey)
                 再转换成（品类ID，点击数量，下单数量，支付数量）结构，再在对应的位置上累加 (reduceByKey)

            所以，为了避免使用过多的 reduceByKey 算子，可以在读数据之后，直接转成 （品类ID，点击数量，下单数量，支付数量）结构，
                 再在对应的位置上累加 (reduceByKey) 【类似wordcount】
         */

        JavaRDD<String> rdd = sc.textFile("src/main/resources/user_visit_action.txt");
        rdd.cache();

        JavaPairRDD<String, Tuple3<Integer, Integer, Integer>> flatRDD = rdd.flatMapToPair(new PairFlatMapFunction<String, String, Tuple3<Integer, Integer, Integer>>() {
            @Override
            public Iterator<Tuple2<String, Tuple3<Integer, Integer, Integer>>> call(String str) throws Exception {
                String[] splits = str.split("_");
                ArrayList<Tuple2<String, Tuple3<Integer, Integer, Integer>>> arrayList = new ArrayList<>();
                if (!splits[6].equals("-1")) {
                    Tuple3<Integer, Integer, Integer> innerTuple = new Tuple3<>(1, 0, 0);
                    Tuple2<String, Tuple3<Integer, Integer, Integer>> outerTuple = new Tuple2<>(splits[6], innerTuple);
                    arrayList.add(outerTuple);
                    return arrayList.iterator();
                } else if (!splits[8].equals("null")) {
                    String[] ss = splits[8].split(",");
                    for (String s : ss) {
                        Tuple3<Integer, Integer, Integer> innerTuple = new Tuple3<>(0, 1, 0);
                        Tuple2<String, Tuple3<Integer, Integer, Integer>> outerTuple = new Tuple2<>(s, innerTuple);
                        arrayList.add(outerTuple);
                    }
                    return arrayList.iterator();
                } else if (!splits[10].equals("null")) {
                    String[] ss = splits[10].split(",");
                    for (String s : ss) {
                        Tuple3<Integer, Integer, Integer> innerTuple = new Tuple3<>(0, 0, 1);
                        Tuple2<String, Tuple3<Integer, Integer, Integer>> outerTuple = new Tuple2<>(s, innerTuple);
                        arrayList.add(outerTuple);
                    }
                    return arrayList.iterator();
                }
                return Collections.emptyIterator();
            }
        });

        JavaRDD<Tuple2<String, String>> reduceRDD = flatRDD
                .reduceByKey(new Function2<Tuple3<Integer, Integer, Integer>, Tuple3<Integer, Integer, Integer>, Tuple3<Integer, Integer, Integer>>() {
                    @Override
                    public Tuple3<Integer, Integer, Integer> call(Tuple3<Integer, Integer, Integer> v1, Tuple3<Integer, Integer, Integer> v2) throws Exception {
                        int one = v1._1() + v2._1();
                        int two = v1._2() + v2._2();
                        int three = v1._3() + v2._3();
                        return new Tuple3<>(one, two, three);
                    }
                })
                .map(new Function<Tuple2<String, Tuple3<Integer, Integer, Integer>>, Tuple2<String, String>>() {
                    @Override
                    public Tuple2<String, String> call(Tuple2<String, Tuple3<Integer, Integer, Integer>> v) throws Exception {
                        String val = String.valueOf(v._2._1()) + "-" + String.valueOf(v._2._2()) + "-" + String.valueOf(v._2._3());
                        return new Tuple2<>(val, v._1);
                    }
                });

        List<Tuple2<String, String>> res = reduceRDD
                .sortBy(new Function<Tuple2<String, String>, String>() {
                    @Override
                    public String call(Tuple2<String, String> v) throws Exception {
                        return v._1;
                    }
                }, false, reduceRDD.getNumPartitions())
                .take(10);


        for (Tuple2<String, String> r : res) {
            System.out.println(r._2 + " : " + r._1);
        }

        sc.stop();
    }
}
