package com.zgg.streaming.statetransformation;

import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import scala.Tuple2;

import java.util.Arrays;

/**
 * reduceByKeyAndWindow(func, invFunc, windowLength, slideInterval, [numTasks])
 */
public class ReduceByKeyAndWindowDemo02 {
    public static void main(String[] args) throws InterruptedException {
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("WindowDemo03-sparkstreaming");
        JavaStreamingContext ssc = new JavaStreamingContext(conf, Durations.seconds(3));
        ssc.checkpoint("cp");

        JavaReceiverInputDStream<String> line = ssc.socketTextStream("bigdata101", 9999);

        JavaDStream<String> word = line.flatMap(s -> {
            String[] splits = s.split(" ");
            return Arrays.asList(splits).iterator();
        });
        JavaPairDStream<String, Integer> pair = word.mapToPair(s -> new Tuple2<>(s, 1));

        /*
           针对 WindowDemo02.java 中提出的问题，使用 reduceByKeyAndWindow 的另一个重载方法解决
           （窗口长度太长，但滑动步长太小，那么两个窗口重叠的部分就重复计算）

           The reduced value of over a new window is calculated using the old window's reduce value :
           新窗口的值基于旧窗口的值计算得来

           它有两个过程： （参考同目录下的图片理解）
             1. 对进入窗口的新数据，执行 reduce  （也就是  使用 20+3+1 = 24）
             2. 对离开窗口的旧值，执行 "inverse reduce"  （也就是 使用 24-1-1=22）

            这比没有 "inverse reduce" 的 reduceByKeyAndWindow 更高效，
            但是，它只适用于“可逆的 reduce functions”

            注意观察执行输出理解：
            1. 当连续输入一个a，然后就不再输出入时，此时，会在3个连续窗口输出中都输出(a,1)，然后就会输出(a,0)
            2. 在测试时，当连续输入多个a时，下一个窗口B的值是 根据上一个窗口A的值 加上新来的值 减掉离开窗口B的值，
               而demo1里是每个窗口单独算
         */
        JavaPairDStream<String, Integer> res = pair.reduceByKeyAndWindow(
                (x, y) -> x + y,
                (x, y) -> x - y,
                Durations.seconds(9),
                Durations.seconds(3));

        res.print();

        ssc.start();
        ssc.awaitTermination();
    }
}
