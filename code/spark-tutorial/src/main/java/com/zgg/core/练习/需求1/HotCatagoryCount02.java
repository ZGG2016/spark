package com.zgg.core.练习.需求1;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;
import scala.Tuple3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 优化
 */
public class HotCatagoryCount02 {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setAppName("HotCatagoryCount02").setMaster("local[*]");
        JavaSparkContext sc = new JavaSparkContext(conf);

        /*
           存在的问题：
              - 1.读取数据后，产生的rdd，在计算点击、下单、支付时都需要使用，所以， textfile 就会读取数据多次
              - 2.cogroup性能较低，因为cogroup算子可能会产生shuffle  (CoGroupedRDD类中的getDependencies方法)
         */

        JavaRDD<String> rdd = sc.textFile("src/main/resources/user_visit_action.txt");
        // 针对第一个问题：可以将数据缓存  (也可以配合检查点一起使用)
        rdd.cache();

        JavaRDD<String> clickRDD = rdd.filter(new Function<String, Boolean>() {
            @Override
            public Boolean call(String v1) throws Exception {
                String[] ss = v1.split("_");
                return !ss[6].equals("-1");
            }
        });
        JavaPairRDD<String, Integer> clickCountRDD = clickRDD
                .mapToPair(new PairFunction<String, String, Integer>() {
                    @Override
                    public Tuple2<String, Integer> call(String s) throws Exception {
                        String[] ss = s.split("_");
                        return new Tuple2<>(ss[6], 1);
                    }
                })
                .reduceByKey(Integer::sum);

        JavaRDD<String> orderRDD = rdd.filter(new Function<String, Boolean>() {
            @Override
            public Boolean call(String v1) throws Exception {
                String[] ss = v1.split("_");
                return !ss[8].equals("null");
            }
        });
        JavaPairRDD<String, Integer> orderCountRDD = orderRDD
                .flatMapToPair(new PairFlatMapFunction<String, String, Integer>() {
                    @Override
                    public Iterator<Tuple2<String, Integer>> call(String s) throws Exception {
                        String[] ss1 = s.split("_");
                        String[] ss2 = ss1[8].split(",");
                        ArrayList<Tuple2<String, Integer>> arrayList = new ArrayList<>();
                        for (String s2 : ss2) {
                            arrayList.add(new Tuple2<>(s2, 1));
                        }
                        return arrayList.iterator();
                    }
                })
                .reduceByKey(Integer::sum);

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
                        for (String s2 : ss2) {
                            arrayList.add(new Tuple2<>(s2, 1));
                        }
                        return arrayList.iterator();
                    }
                })
                .reduceByKey(Integer::sum);

        /*
            在 HotCatagoryCount01.java 中，是先形成 （品类ID，点击数量） （品类ID，下单数量） （品类ID，支付数量） 结构，
                 再 cogroup 连接成 （品类ID，迭代器（点击数量），迭代器（下单数量），迭代器（支付数量）） 结构，
                 最后再累加成 （品类ID，点击数量，下单数量，支付数量） 结构。

            因为 cogroup 算子可能会产生 shuffle, 性能较低，所以不使用 cogroup 算子。

            所以，为了避免 cogroup 连接，可以直接转换成（品类ID，点击数量，下单数量，支付数量）结构，再在对应的位置上累加

         */

        JavaRDD<Tuple2<String, Tuple3<Integer, Integer, Integer>>> clickCountMapRDD = clickCountRDD.map(new Function<Tuple2<String, Integer>, Tuple2<String, Tuple3<Integer, Integer, Integer>>>() {
            @Override
            public Tuple2<String, Tuple3<Integer, Integer, Integer>> call(Tuple2<String, Integer> v) throws Exception {
                Tuple3<Integer, Integer, Integer> innerTuple = new Tuple3<>(v._2, 0, 0);
                return new Tuple2<>(v._1, innerTuple);
            }
        });

        JavaRDD<Tuple2<String, Tuple3<Integer, Integer, Integer>>> orderCountMapRDD = orderCountRDD.map(new Function<Tuple2<String, Integer>, Tuple2<String, Tuple3<Integer, Integer, Integer>>>() {
            @Override
            public Tuple2<String, Tuple3<Integer, Integer, Integer>> call(Tuple2<String, Integer> v) throws Exception {
                Tuple3<Integer, Integer, Integer> innerTuple = new Tuple3<>(0, v._2, 0);
                return new Tuple2<>(v._1, innerTuple);
            }
        });

        JavaRDD<Tuple2<String, Tuple3<Integer, Integer, Integer>>> payCountMapRDD = payCountRDD.map(new Function<Tuple2<String, Integer>, Tuple2<String, Tuple3<Integer, Integer, Integer>>>() {
            @Override
            public Tuple2<String, Tuple3<Integer, Integer, Integer>> call(Tuple2<String, Integer> v) throws Exception {
                Tuple3<Integer, Integer, Integer> innerTuple = new Tuple3<>(0, 0, v._2);
                return new Tuple2<>(v._1, innerTuple);
            }
        });

        /*
           此时，就分别得到了： (品类ID, (点击数量, 0, 0))   (品类ID, (0, 下单数量, 0))   (品类ID, (0, 0, 支付数量))

           然后，就可以先合并成一个rdd，再分组聚合
         */

        JavaPairRDD<String, Tuple3<Integer, Integer, Integer>> unionRDD = clickCountMapRDD
                .union(orderCountMapRDD)
                .union(payCountMapRDD)
                .mapToPair(new PairFunction<Tuple2<String, Tuple3<Integer, Integer, Integer>>, String, Tuple3<Integer, Integer, Integer>>() {
                    @Override
                    public Tuple2<String, Tuple3<Integer, Integer, Integer>> call(Tuple2<String, Tuple3<Integer, Integer, Integer>> v) throws Exception {
                        return new Tuple2<>(v._1, v._2);
                    }
                });

        JavaRDD<Tuple2<String, String>> reduceRDD = unionRDD.reduceByKey(new Function2<Tuple3<Integer, Integer, Integer>, Tuple3<Integer, Integer, Integer>, Tuple3<Integer, Integer, Integer>>() {
            @Override
            public Tuple3<Integer, Integer, Integer> call(Tuple3<Integer, Integer, Integer> v1, Tuple3<Integer, Integer, Integer> v2) throws Exception {
                int one = v1._1() + v2._1();
                int two = v1._2() + v2._2();
                int three = v1._3() + v2._3();
                return new Tuple3<>(one, two, three);
            }
        }).map(new Function<Tuple2<String, Tuple3<Integer, Integer, Integer>>, Tuple2<String, String>>() {
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
