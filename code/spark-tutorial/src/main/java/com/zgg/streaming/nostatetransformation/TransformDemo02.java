package com.zgg.streaming.nostatetransformation;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.Optional;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;
/**
 * Transform例子
 */
public class TransformDemo02 {
    public static void main(String[] args) throws InterruptedException {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("TransformDemo02-sparkstreaming");
        JavaStreamingContext ssc = new JavaStreamingContext(conf, Durations.seconds(3));

        /*
          模拟创建黑名单
          数据格式：<String,Boolean> -> <"jack",true>
         */
        List<Tuple2<String,Boolean>> blacklist = new ArrayList<Tuple2<String,Boolean>>();
        blacklist.add(new Tuple2<>("jack",true));
        blacklist.add(new Tuple2<>("lili",true));
        JavaPairRDD<String,Boolean> blacklistRdd = ssc.sparkContext().parallelizePairs(blacklist);

        JavaReceiverInputDStream<String> userDstream = ssc.socketTextStream("bigdata101", 9999);

        /*
           参考理解：https://developer.aliyun.com/article/922026?spm=a2c6h.12873639.article-detail.33.30dd560bNDu00w&scm=20140722.ID_community@@article@@922026._.ID_community@@article@@922026-OR_rec-V_1
                  transform操作，应用在DStream上时，可以用于执行任意的RDD到RDD的转换操作。
                  它可以用于实现， DStream API中所没有提供的操作。
                  比如说，DStream API中，并没有提供将一个DStream中的每个 batch，与一个特定的RDD进行join的操作。
                  但是我们自己就可以使用transform操作来实现该功能。
                  DStream.join()，只能join其他DStream。在DStream每个batch的RDD计算出来之后，会去跟其他 DStream的RDD进行join。
         */

        /*
          源数据：2019-09-09 张三    -> 转换后的数据：  <"张三",2019-09-09 张三>
         */
        JavaPairDStream<String, String> userPair = userDstream.mapToPair(
                new PairFunction<String, String, String>() {
                    @Override
                    public Tuple2<String, String> call(String line) throws Exception {
                        return new Tuple2<String, String>(line.split(" ")[1],line);
                    }
                });

        /*
          <"张三",2019-09-09 张三> <"张三",true>
             -> leftOuterJoin
             -> <”张三”,<张三 2019-09-09,true>>
             -> filter 过滤掉==true的数据
         */
        JavaDStream<String> stream = userPair.transform(new Function<JavaPairRDD<String, String>, JavaRDD<String>>() {
            @Override
            public JavaRDD<String> call(JavaPairRDD<String, String> pair) throws Exception {

                // leftOuterJoin
                JavaPairRDD<String, Tuple2<String, Optional<Boolean>>> joinrdd = pair.leftOuterJoin(blacklistRdd);

                // filter
                JavaPairRDD<String, Tuple2<String, Optional<Boolean>>> filterjoinrdd =joinrdd.filter(
                        new Function<Tuple2<String, Tuple2<String, Optional<Boolean>>>, Boolean>() {
                            @Override
                            public Boolean call(Tuple2<String, Tuple2<String, Optional<Boolean>>> tuple) throws Exception {
                                return !tuple._2._2.isPresent() || !tuple._2._2.get();
                            }
                        });

                // map
                JavaRDD<String> rdd = filterjoinrdd.map(
                        new Function<Tuple2<String, Tuple2<String, Optional<Boolean>>>, String>() {
                            @Override
                            public String call(Tuple2<String, Tuple2<String, Optional<Boolean>>> tuple) throws Exception {
                                return tuple._1 + " : " + tuple._2._1 + " : " + tuple._2._2;
                            }
                        });

                return rdd;
            }
        });

        stream.print();

        ssc.start();
        ssc.awaitTermination();
    }
}
