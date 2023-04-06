package com.zgg.streaming.datasource;

import org.apache.spark.SparkConf;
import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.receiver.Receiver;

import java.util.Random;

/**
 * 自定义数据源
 */
public class CustomDS01 {
    public static void main(String[] args) throws Exception {

        SparkConf conf = new SparkConf()
                .setMaster("local[*]")
                .setAppName("CustomDS01-sparkstreaming");
        JavaStreamingContext ssc = new JavaStreamingContext(conf, Durations.seconds(4));
        JavaReceiverInputDStream<String> res = ssc.receiverStream(new CustormReceiver(StorageLevel.MEMORY_ONLY()));
        res.print();

        ssc.start();
        ssc.awaitTermination();

    }
    /*
      自定义数据采集器：
        1. 继承 Receiver
        2. 实现 onStart、onStop 方法
     */
    static class CustormReceiver extends Receiver<String>{

        public CustormReceiver(StorageLevel storageLevel) {
            super(storageLevel);
        }

        private Boolean flag = true;

        /*
            1. 初始化资源，比如创建、启动线程，打开socket
            2. 在线程内，调用 store 方法接收数据
            3. 使用 reportError stop restart 方法处理线程内的错误
         */
        @Override
        public void onStart() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while(flag){
                        String message = "采集的数据为：" + String.valueOf(new Random().nextInt(10));
                        // 将一个接收的数据存到spark内存中。在存入前，可以进行聚合
                        store(message);
                    }
                }
            }).start();
        }

        // 用来清理一些配置，比如停止线程
        @Override
        public void onStop() {
            flag = false;
        }
    }
}
