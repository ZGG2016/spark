package com.zgg.core.wc;

import org.apache.commons.collections.IteratorUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

import java.util.*;
/**
 * 11种wordcount的方式
 * */
public class WordCount03 {
    public static void main(String[] args) {
        SparkConf sparkConf = new SparkConf().setAppName("WordCount03").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);

        JavaRDD<String> rdd = sc.textFile("src/main/resources/wc.txt");

        computeWC01(sc, rdd);
//        computeWC02(sc, rdd);
//        computeWC03(sc, rdd);
//        computeWC04(sc, rdd);
//        computeWC05(sc, rdd);
//        computeWC06(sc, rdd);
//        computeWC07(sc, rdd);
//        computeWC08(sc, rdd);
//        computeWC09(sc, rdd);
//        computeWC10(sc, rdd);
//        computeWC11(sc, rdd);

    }

    // 1. groupby  使用word做key，相同的其他words做value，再统计个数
    private static void computeWC01(JavaSparkContext sc, JavaRDD<String> rdd) {
        JavaRDD<String> flatMapRDD = rdd.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public Iterator<String> call(String s) throws Exception {
                return Arrays.asList(s.split("\\|")).iterator();
            }
        });

        JavaPairRDD<String, Iterable<String>> groupByRDD = flatMapRDD.groupBy(new Function<String, String>() {
            @Override
            public String call(String v1) throws Exception {
                return v1;
            }
        });
//        JavaRDD<Tuple2<String, Integer>> res = groupByRDD.map(new Function<Tuple2<String, Iterable<String>>, Tuple2<String, Integer>>() {
//            @Override
//            public Tuple2<String, Integer> call(Tuple2<String, Iterable<String>> v1) throws Exception {
//                int countRes = IteratorUtils.toList(v1._2.iterator()).size();
//                return new Tuple2<>(v1._1, countRes);
//            }
//        });
        JavaPairRDD<String, Integer> res = groupByRDD.mapValues(new Function<Iterable<String>, Integer>() {
            @Override
            public Integer call(Iterable<String> v1) throws Exception {
                return IteratorUtils.toList(v1.iterator()).size();
            }
        });
        res.collect().forEach(System.out::println);
    }

    // 2.groupByKey  用word做key，(word,1)里的1做value，再统计个数
    private static void computeWC02(JavaSparkContext sc, JavaRDD<String> rdd) {
        JavaRDD<String> flatMapRDD = rdd.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public Iterator<String> call(String s) throws Exception {
                return Arrays.asList(s.split("\\|")).iterator();
            }
        });
        JavaPairRDD<String, Integer> pairRDD = flatMapRDD.mapToPair(new PairFunction<String, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(String s) throws Exception {
                return new Tuple2<>(s, 1);
            }
        });
        JavaPairRDD<String, Integer> res = pairRDD.groupByKey()
                .mapValues(new Function<Iterable<Integer>, Integer>() {
                    @Override
                    public Integer call(Iterable<Integer> v1) throws Exception {
                        return IteratorUtils.toList(v1.iterator()).size();
                    }
                });
        res.collect().forEach(System.out::println);
    }

    // 3.reduceByKey    groupByKey是分组后再使用mapvalue算子做聚合，而reduceByKey分组后直接聚合
    private static void computeWC03(JavaSparkContext sc, JavaRDD<String> rdd) {
        JavaRDD<String> flatMapRDD = rdd.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public Iterator<String> call(String s) throws Exception {
                return Arrays.asList(s.split("\\|")).iterator();
            }
        });
        JavaPairRDD<String, Integer> pairRDD = flatMapRDD.mapToPair(new PairFunction<String, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(String s) throws Exception {
                return new Tuple2<>(s, 1);
            }
        });
        JavaPairRDD<String, Integer> res = pairRDD.reduceByKey(new Function2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer v1, Integer v2) throws Exception {
                return v1 + v2;
            }
        });
        res.collect().forEach(System.out::println);
    }

    // 4.aggregateByKey
    private static void computeWC04(JavaSparkContext sc, JavaRDD<String> rdd) {
        JavaRDD<String> flatMapRDD = rdd.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public Iterator<String> call(String s) throws Exception {
                return Arrays.asList(s.split("\\|")).iterator();
            }
        });
        JavaPairRDD<String, Integer> pairRDD = flatMapRDD.mapToPair(new PairFunction<String, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(String s) throws Exception {
                return new Tuple2<>(s, 1);
            }
        });
        JavaPairRDD<String, Integer> res = pairRDD.aggregateByKey(0,
                new Function2<Integer, Integer, Integer>() {
                    @Override
                    public Integer call(Integer v1, Integer v2) throws Exception {
                        return v1 + v2;
                    }
                },
                new Function2<Integer, Integer, Integer>() {
                    @Override
                    public Integer call(Integer v1, Integer v2) throws Exception {
                        return v1 + v2;
                    }
                });
        res.collect().forEach(System.out::println);
    }

    // 5.combineByKey
    private static void computeWC05(JavaSparkContext sc, JavaRDD<String> rdd) {
        JavaRDD<String> flatMapRDD = rdd.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public Iterator<String> call(String s) throws Exception {
                return Arrays.asList(s.split("\\|")).iterator();
            }
        });
        JavaPairRDD<String, Integer> pairRDD = flatMapRDD.mapToPair(new PairFunction<String, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(String s) throws Exception {
                return new Tuple2<>(s, 1);
            }
        });
        JavaPairRDD<String, Integer> res = pairRDD.combineByKey(
                new Function<Integer, Integer>() {
                    @Override
                    public Integer call(Integer v1) throws Exception {
                        return v1;
                    }
                },
                new Function2<Integer, Integer, Integer>() {
                    @Override
                    public Integer call(Integer v1, Integer v2) throws Exception {
                        return v1 + v2;
                    }
                },
                new Function2<Integer, Integer, Integer>() {
                    @Override
                    public Integer call(Integer v1, Integer v2) throws Exception {
                        return v1 + v2;
                    }
                });
        res.collect().forEach(System.out::println);
    }

    // 6.foldByKey
    private static void computeWC06(JavaSparkContext sc, JavaRDD<String> rdd) {
        JavaRDD<String> flatMapRDD = rdd.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public Iterator<String> call(String s) throws Exception {
                return Arrays.asList(s.split("\\|")).iterator();
            }
        });
        JavaPairRDD<String, Integer> pairRDD = flatMapRDD.mapToPair(new PairFunction<String, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(String s) throws Exception {
                return new Tuple2<>(s, 1);
            }
        });
        JavaPairRDD<String, Integer> res = pairRDD.foldByKey(
                0,
                new Function2<Integer, Integer, Integer>() {
                    @Override
                    public Integer call(Integer v1, Integer v2) throws Exception {
                        return v1 + v2;
                    }
                });
        res.collect().forEach(System.out::println);
    }

    // 7.countByKey  得到(word,1)形式后，直接按key分组，count. 而groupByKey groupby 则都需要再调用一个算子做聚合
    private static void computeWC07(JavaSparkContext sc, JavaRDD<String> rdd) {
        JavaRDD<String> flatMapRDD = rdd.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public Iterator<String> call(String s) throws Exception {
                return Arrays.asList(s.split("\\|")).iterator();
            }
        });
        JavaPairRDD<String, Integer> pairRDD = flatMapRDD.mapToPair(new PairFunction<String, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(String s) throws Exception {
                return new Tuple2<>(s, 1);
            }
        });
        Map<String, Long> res = pairRDD.countByKey();
        System.out.println(res);
    }

    // 8.countByValue  无需得到(word,1)形式，展平后，直接统计个数
    private static void computeWC08(JavaSparkContext sc, JavaRDD<String> rdd) {
        JavaRDD<String> flatMapRDD = rdd.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public Iterator<String> call(String s) throws Exception {
                return Arrays.asList(s.split("\\|")).iterator();
            }
        });
        Map<String, Long> res = flatMapRDD.countByValue();
        System.out.println(res);
    }

    // 09.reduce  以一个数据结构为基准（这里是v1），两项两项的依次迭代
    private static void computeWC09(JavaSparkContext sc, JavaRDD<String> rdd) {
        JavaRDD<String> flatMapRDD = rdd.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public Iterator<String> call(String s) throws Exception {
                return Arrays.asList(s.split("\\|")).iterator();
            }
        });
        JavaRDD<HashMap<String, Integer>> mapRDD = flatMapRDD.map(new Function<String, HashMap<String, Integer>>() {
            @Override
            public HashMap<String, Integer> call(String v1) throws Exception {
                HashMap<String, Integer> hm = new HashMap<>();
                hm.put(v1, 1);
                return hm;
            }
        });
        HashMap<String, Integer> res = mapRDD.reduce(
                new Function2<HashMap<String, Integer>, HashMap<String, Integer>, HashMap<String, Integer>>() {
                    @Override
                    public HashMap<String, Integer> call(HashMap<String, Integer> v1, HashMap<String, Integer> v2) throws Exception {
                        // 以 v1 为基准，一直往下迭代
                        Set<String> ss1 = v1.keySet();
                        Set<String> ss2 = v2.keySet();
                        outerFor:for (String s2 : ss2) {
                            for (String s1 : ss1) {
                                //如果匹配得到的话，就更新v1里的项
                                if (s2.equals(s1)) {
                                    v1.put(s1, v1.get(s1) + v2.get(s2));
                                    break outerFor;   // 给外层循环取个别名，在这里，当匹配成功、且更新后，暂停外层循环
                                }
                            }
                            //如果遍历完都没有匹配得到的话，就直接添加到v1
                            v1.put(s2, v2.get(s2));
                        }
                        return v1;
                    }
                });

        Set<String> ss = res.keySet();
        for (String s : ss) {
            System.out.println(s + " -- " + res.get(s));
        }
    }

    // 10.aggregate
    private static void computeWC10(JavaSparkContext sc, JavaRDD<String> rdd) {
        JavaRDD<String> flatMapRDD = rdd.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public Iterator<String> call(String s) throws Exception {
                return Arrays.asList(s.split("\\|")).iterator();
            }
        });
        JavaRDD<HashMap<String, Integer>> mapRDD = flatMapRDD.map(new Function<String, HashMap<String, Integer>>() {
            @Override
            public HashMap<String, Integer> call(String v1) throws Exception {
                HashMap<String, Integer> hm = new HashMap<>();
                hm.put(v1, 1);
                return hm;
            }
        });
        HashMap<String, Integer> res = mapRDD.aggregate(
                new HashMap<>(),
                new Function2<HashMap<String, Integer>, HashMap<String, Integer>, HashMap<String, Integer>>() {
                    @Override
                    public HashMap<String, Integer> call(HashMap<String, Integer> v1, HashMap<String, Integer> v2) throws Exception {
                        // 以 v1 为基准，一直往下迭代
                        Set<String> ss1 = v1.keySet();
                        Set<String> ss2 = v2.keySet();
                        outerFor:for (String s2 : ss2) {
                            for (String s1 : ss1) {
                                //如果匹配得到的话，就更新v1里的项
                                if (s2.equals(s1)) {
                                    v1.put(s1, v1.get(s1) + v2.get(s2));
                                    break outerFor;   // 给外层循环取个别名，在这里，当匹配成功、且更新后，暂停外层循环
                                }
                            }
                            //如果遍历完都没有匹配得到的话，就直接添加到v1
                            v1.put(s2, v2.get(s2));
                        }
                        return v1;
                    }
                },
                new Function2<HashMap<String, Integer>, HashMap<String, Integer>, HashMap<String, Integer>>() {
                    @Override
                    public HashMap<String, Integer> call(HashMap<String, Integer> v1, HashMap<String, Integer> v2) throws Exception {
                        // 以 v1 为基准，一直往下迭代
                        Set<String> ss1 = v1.keySet();
                        Set<String> ss2 = v2.keySet();
                        outerFor:for (String s2 : ss2) {
                            for (String s1 : ss1) {
                                //如果匹配得到的话，就更新v1里的项
                                if (s2.equals(s1)) {
                                    v1.put(s1, v1.get(s1) + v2.get(s2));
                                    break outerFor;   // 给外层循环取个别名，在这里，当匹配成功、且更新后，暂停外层循环
                                }
                            }
                            //如果遍历完都没有匹配得到的话，就直接添加到v1
                            v1.put(s2, v2.get(s2));
                        }
                        return v1;
                    }
                });

        Set<String> ss = res.keySet();
        for (String s : ss) {
            System.out.println(s + " -- " + res.get(s));
        }
    }

    // 11.fold
    private static void computeWC11(JavaSparkContext sc, JavaRDD<String> rdd) {
        JavaRDD<String> flatMapRDD = rdd.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public Iterator<String> call(String s) throws Exception {
                return Arrays.asList(s.split("\\|")).iterator();
            }
        });
        JavaRDD<HashMap<String, Integer>> mapRDD = flatMapRDD.map(new Function<String, HashMap<String, Integer>>() {
            @Override
            public HashMap<String, Integer> call(String v1) throws Exception {
                HashMap<String, Integer> hm = new HashMap<>();
                hm.put(v1, 1);
                return hm;
            }
        });
        HashMap<String, Integer> res = mapRDD.fold(
                new HashMap<>(),
                new Function2<HashMap<String, Integer>, HashMap<String, Integer>, HashMap<String, Integer>>() {
                    @Override
                    public HashMap<String, Integer> call(HashMap<String, Integer> v1, HashMap<String, Integer> v2) throws Exception {
                        // 以 v1 为基准，一直往下迭代
                        Set<String> ss1 = v1.keySet();
                        Set<String> ss2 = v2.keySet();
                        outerFor:for (String s2 : ss2) {
                            for (String s1 : ss1) {
                                //如果匹配得到的话，就更新v1里的项
                                if (s2.equals(s1)) {
                                    v1.put(s1, v1.get(s1) + v2.get(s2));
                                    break outerFor;   // 给外层循环取个别名，在这里，当匹配成功、且更新后，暂停外层循环
                                }
                            }
                            //如果遍历完都没有匹配得到的话，就直接添加到v1
                            v1.put(s2, v2.get(s2));
                        }
                        return v1;
                    }
                });

        Set<String> ss = res.keySet();
        for (String s : ss) {
            System.out.println(s + " -- " + res.get(s));
        }
    }

    // 12.累加器实现wordcount  `src/main/java/com/zgg/core/累加器/AccTest04.java`
}
