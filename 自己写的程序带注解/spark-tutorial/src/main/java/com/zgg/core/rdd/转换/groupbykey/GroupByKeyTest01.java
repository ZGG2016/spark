package com.zgg.core.rdd.转换.groupbykey;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 将数据源的数据根据 key 对 value 进行分组
 * */
public class GroupByKeyTest01 {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("GroupByKeyTest01");
        JavaSparkContext sc = new JavaSparkContext(conf);

        List<String> data = Arrays.asList("a", "a", "b", "c", "b");
        JavaRDD<String> rdd = sc.parallelize(data);

        JavaPairRDD<String, Integer> pairRDD = rdd.mapToPair(new PairFunction<String, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(String s) throws Exception {
                return new Tuple2<>(s, 1);
            }
        });

        /*
          groupByKey : 将数据源中的数据，相同key的数据分在一个组中，形成一个对偶元组
                       元组中的第一个元素就是key，
                       元组中的第二个元素就是相同key的value的集合
         */
        JavaPairRDD<String, Iterable<Integer>> res1 = pairRDD.groupByKey();

        JavaPairRDD<String, Iterable<Tuple2<String, Integer>>> res2 = pairRDD.groupBy(new Function<Tuple2<String, Integer>, String>() {
            @Override
            public String call(Tuple2<String, Integer> v1) throws Exception {
                return v1._1;
            }
        });

        res1.collect().forEach(System.out::println);
        res2.collect().forEach(System.out::println);
        sc.stop();
    }
}

/*
  groupByKey输出： 【key及 相同key的value的集合】
     (a,[1, 1])
     (b,[1, 1])
     (c,[1])

  groupBy输出：   【key及 相同key的原元组的集合】
     (a,[(a,1), (a,1)])
     (b,[(b,1), (b,1)])
     (c,[(c,1)])

   groupByKey就是按key分组，而groupBy可以自定义分组逻辑
 */

/*
  reduceByKey 和 groupByKey 的区别
  - 从 shuffle 的角度：
      reduceByKey 和 groupByKey 都存在 shuffle 的操作，但是 reduceByKey 可以在 shuffle 前对分区内相同 key 的数据进行预聚合（combine）功能，
      这样会减少落盘的数据量，而 groupByKey 只是进行分组，不存在数据量减少的问题，reduceByKey 性能比较高。
  - 从功能的角度：
       reduceByKey 其实包含分组和聚合的功能。GroupByKey 只能分组，不能聚合，
       所以在分组聚合的场合下，推荐使用 reduceByKey，如果仅仅是分组而不需要聚合。那么还是只能使用 groupByKey
 */
