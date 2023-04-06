package com.zgg.core.练习.需求1;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;
import scala.Tuple3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * 按照点击、下单、支付的量来统计热门品类 top10
 * 即，先按照点击数排名，靠前的就排名高；
 * 如果点击数相同，再比较下单数；
 * 下单数再相同，就比较支付数
 */
public class HotCatagoryCount01 {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setAppName("HotCatagoryCount01").setMaster("local[*]");
        JavaSparkContext sc = new JavaSparkContext(conf);

        // 1. 读取原始日志数据
        JavaRDD<String> rdd = sc.textFile("src/main/resources/user_visit_action.txt");

        // 2. 统计品类的点击数量 （品类ID，点击数量）
        // 如果点击的品类 ID 为-1，表示数据不是点击数据
        // 2.1 过滤出来是点击行为的数据
        JavaRDD<String> clickRDD = rdd.filter(new Function<String, Boolean>() {
            @Override
            public Boolean call(String v1) throws Exception {
                String[] ss = v1.split("_");
                return !ss[6].equals("-1");
            }
        });
        // 2.2 转成二元组，统计
        JavaPairRDD<String, Integer> clickCountRDD = clickRDD
                .mapToPair(new PairFunction<String, String, Integer>() {
                    @Override
                    public Tuple2<String, Integer> call(String s) throws Exception {
                        String[] ss = s.split("_");
                        return new Tuple2<>(ss[6], 1);
                    }
                })
                .reduceByKey(Integer::sum);

        // 3. 统计品类的下单数量：（品类ID，下单数量）
        // 如果本次不是下单行为，则数据采用 null 表示
        JavaRDD<String> orderRDD = rdd.filter(new Function<String, Boolean>() {
            @Override
            public Boolean call(String v1) throws Exception {
                String[] ss = v1.split("_");
                return !ss[8].equals("null");
            }
        });
        // 一次可以下单多个商品，所以品类 ID 和产品 ID 可以是多个，id 之间采用逗号分隔
        // 所以，需要将数据展平，并转成二元组形式
        JavaPairRDD<String, Integer> orderCountRDD = orderRDD
                .flatMapToPair(new PairFlatMapFunction<String, String, Integer>() {
                    @Override
                    public Iterator<Tuple2<String, Integer>> call(String s) throws Exception {
                        String[] ss1 = s.split("_");
                        String[] ss2 = ss1[8].split(",");
                        ArrayList<Tuple2<String, Integer>> arrayList = new ArrayList<>();
                        for (String s2 : ss2){
                            arrayList.add(new Tuple2<>(s2,1));
                        }
                        return arrayList.iterator();
                    }
                })
                .reduceByKey(Integer::sum);

        // 4. 统计品类的支付数量：（品类ID，支付数量）
        // 支付行为和下单行为类似
        JavaRDD<String> payRDD = rdd.filter(new Function<String, Boolean>() {
            @Override
            public Boolean call(String v1) throws Exception {
                String[] ss = v1.split("_");
                return !ss[10].equals("null");
            }
        });
        JavaPairRDD<String, Integer> payCountRDD = payRDD
                .flatMapToPair(new PairFlatMapFunction<String, String, Integer>() {
                    @Override
                    public Iterator<Tuple2<String, Integer>> call(String s) throws Exception {
                        String[] ss1 = s.split("_");
                        String[] ss2 = ss1[10].split(",");
                        ArrayList<Tuple2<String, Integer>> arrayList = new ArrayList<>();
                        for (String s2 : ss2){
                            arrayList.add(new Tuple2<>(s2,1));
                        }
                        return arrayList.iterator();
                    }
                })
                .reduceByKey(Integer::sum);

        // clickCountRDD （品类ID，点击数量）
        // orderCountRDD （品类ID，下单数量）
        // payCountRDD   （品类ID，支付数量）
        //   --->  （品类ID，点击数量，下单数量，支付数量） 依次排序
        // 思考使用哪个算子 join  union  leftOuterJoin  cogroup
        // 5. 将品类进行排序，并且取前10名
        // 5.1. 根据品类ID这个key，连接起来，形成  （品类ID，迭代器（点击数量），迭代器（下单数量），迭代器（支付数量））
        JavaPairRDD<String, Tuple3<Iterable<Integer>, Iterable<Integer>, Iterable<Integer>>> cogroupRDD = clickCountRDD.cogroup(orderCountRDD, payCountRDD);

        // 5.2. 根据品类ID这个key，对各个行为进行累加，形成 （品类ID，点击数量，下单数量，支付数量）
        JavaPairRDD<String, Tuple3<Integer, Integer, Integer>> mapValuesRDD = cogroupRDD.mapValues(new Function<Tuple3<Iterable<Integer>, Iterable<Integer>, Iterable<Integer>>, Tuple3<Integer, Integer, Integer>>() {
            @Override
            public Tuple3<Integer, Integer, Integer> call(Tuple3<Iterable<Integer>, Iterable<Integer>, Iterable<Integer>> v) throws Exception {
                Iterator<Integer> clickIter = v._1().iterator();
                int clickCount = 0;
                while (clickIter.hasNext()) {
                    clickCount += clickIter.next();
                }

                Iterator<Integer> orderIter = v._2().iterator();
                int orderCount = 0;
                while (orderIter.hasNext()) {
                    orderCount += orderIter.next();
                }

                Iterator<Integer> payIter = v._3().iterator();
                int payCount = 0;
                while (payIter.hasNext()) {
                    payCount += payIter.next();
                }

                return new Tuple3<>(clickCount, orderCount, payCount);
            }
        });

        // 5.3 转换数据结构，方便使用 sortBy 排序
        List<Tuple2<String, String>> res = mapValuesRDD
                .map(new Function<Tuple2<String, Tuple3<Integer, Integer, Integer>>, Tuple2<String, String>>() {
                    @Override
                    public Tuple2<String, String> call(Tuple2<String, Tuple3<Integer, Integer, Integer>> v) throws Exception {
                        String key = v._2._1() + "-" + v._2._2() + "-" + v._2._3();
                        return new Tuple2<>(key, v._1);
                    }
                }).sortBy(new Function<Tuple2<String, String>, String>() {
                    @Override
                    public String call(Tuple2<String, String> v) throws Exception {
                        return v._1;
                    }
                }, false, mapValuesRDD.getNumPartitions())
                .take(10);


        for (Tuple2<String, String> r : res) {
            System.out.println(r._2 + " : " + r._1);
        }

        sc.stop();
    }
}
