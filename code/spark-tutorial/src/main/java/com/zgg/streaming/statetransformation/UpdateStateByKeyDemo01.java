package com.zgg.streaming.statetransformation;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.Optional;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import scala.Tuple2;

import java.util.Arrays;
import java.util.List;

/**
 * 使用 updateStateByKey 完成跨采集周期累加的wordcount
 * 需要保留数据统计结果（状态），实现数据的汇总
 */
public class UpdateStateByKeyDemo01 {
    public static void main(String[] args) throws InterruptedException {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("UpdateStateByKeyDemo-sparkstreaming");
        JavaStreamingContext ssc = new JavaStreamingContext(conf, Durations.seconds(3));
        ssc.checkpoint("cp");

        JavaReceiverInputDStream<String> line = ssc.socketTextStream("bigdata101", 9999);

        JavaDStream<String> word = line.flatMap( s -> {
            String[] splits = s.split(" ");
            return Arrays.asList(splits).iterator();
        });
        JavaPairDStream<String, Integer> pair = word.mapToPair(s -> new Tuple2<>(s, 1));

        /*
            updateStateByKey:
                Function2的三个泛型：Function2<T1, T2, R>
                T1 T2是输入参数类型  R是返回值类型
                第一个参数 T1 表示相同的key的value数据（传来的一个批次的输入数据）
                第二个参数 T2 表示缓存区中相同key的value数据（状态）
         */
        JavaPairDStream<String, Integer> res = pair.updateStateByKey(
                new Function2<List<Integer>, Optional<Integer>, Optional<Integer>>() {
                    @Override
                    public Optional<Integer> call(List<Integer> values, Optional<Integer> state) throws Exception {
                        Integer newCount = state.orElse(0);
                        for (Integer val : values) {
                            newCount += val;
                        }
                        return Optional.of(newCount);
                    }
                });
        res.print();

        ssc.start();
        ssc.awaitTermination();
    }
}
