package com.zgg.core.练习.需求2;

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

import java.util.*;

/**
 * 在需求一的基础上，增加每个品类用户 session 的点击统计，取top10
 *
 * 【类似 com.zgg.core.rdd.练习.CountPV01.java 需求】
 */
public class SessionTopTen {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setAppName("SessionTopTen").setMaster("local[*]");
        JavaSparkContext sc = new JavaSparkContext(conf);

        JavaRDD<String> rdd = sc.textFile("src/main/resources/user_visit_action.txt");

        List<String> hotCategories = hotCategoryCount(rdd);

        // 过滤出点击行为，且在热门品类中的数据
        JavaRDD<String> filterRDD = rdd.filter(new Function<String, Boolean>() {
            @Override
            public Boolean call(String v) throws Exception {
                String[] splits = v.split("_");
                return !splits[6].equals("-1") && hotCategories.contains(splits[6]);
            }
        });

        // 将过滤后的数据，转成 （品类id|sessionid，1） 的结构
        JavaPairRDD<String, Integer> pairRDD = filterRDD.mapToPair(new PairFunction<String, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(String s) throws Exception {
                String[] splits = s.split("_");
                return new Tuple2<>(splits[6] + "|" + splits[2], 1);
            }
        });
        // 得到 （品类id|sessionid，数量） --> （品类id, sessionid|数量）
        JavaPairRDD<String, String> reduceRDD = pairRDD
                .reduceByKey(new Function2<Integer, Integer, Integer>() {
                    @Override
                    public Integer call(Integer v1, Integer v2) throws Exception {
                        return v1 + v2;
                    }
                })
                .mapToPair(new PairFunction<Tuple2<String, Integer>, String, String>() {
                    @Override
                    public Tuple2<String, String> call(Tuple2<String, Integer> t) throws Exception {
                        String[] splits = t._1.split("\\|");
                        String val = splits[1] + "|" + String.valueOf(t._2);
                        return new Tuple2<>(splits[0], val);
                    }
                });

        // 按品类分组，方便后面取top10  （品类id, sessionid|数量）
        JavaPairRDD<String, Iterable<String>> groupRDD = reduceRDD.groupByKey();

        JavaPairRDD<String, ArrayList<String>> res = groupRDD.mapValues(new Function<Iterable<String>, ArrayList<String>>() {
            @Override
            public ArrayList<String> call(Iterable<String> v) throws Exception {
                ArrayList<String> tmpList = new ArrayList<>();
                Iterator<String> iterator = v.iterator();
                while (iterator.hasNext()) {
                    tmpList.add(iterator.next());
                }
                Collections.sort(tmpList, new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        int i1 = Integer.parseInt(o1.split("\\|")[1]);
                        int i2 = Integer.parseInt(o2.split("\\|")[1]);
                        return Integer.compare(i2, i1);
                    }
                });
                ArrayList<String> resList = new ArrayList<>();
                for (int i = 0; i < 10; i++) {
                    resList.add(tmpList.get(i));
                }
                return resList;
            }
        });
        res.collect().forEach(System.out::println);

        sc.stop();
    }

    public static List<String> hotCategoryCount(JavaRDD<String> rdd) {
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

        List<Tuple2<String, String>> sortRDD = reduceRDD
                .sortBy(new Function<Tuple2<String, String>, String>() {
                    @Override
                    public String call(Tuple2<String, String> v) throws Exception {
                        return v._1;
                    }
                }, false, reduceRDD.getNumPartitions())
                .take(10);

        ArrayList<String> res = new ArrayList<>();
        for (Tuple2<String, String> r : sortRDD) {
            res.add(r._2);
        }

        return res;
    }
}
