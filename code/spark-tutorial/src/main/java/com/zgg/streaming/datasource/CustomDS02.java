package com.zgg.streaming.datasource;

import org.apache.spark.SparkConf;
import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.receiver.Receiver;
import scala.Tuple2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Random;

/**
 * 自定义数据源，实现监控某个端口号，获取该端口号内容
 */
public class CustomDS02 {
    public static void main(String[] args) throws Exception {

        SparkConf conf = new SparkConf()
                .setMaster("local[*]")
                .setAppName("CustomDS02-sparkstreaming");
        JavaStreamingContext ssc = new JavaStreamingContext(conf, Durations.seconds(4));
        JavaReceiverInputDStream<String> line
                = ssc.receiverStream(new CustormReceiver("bigdata101",9999, StorageLevel.MEMORY_ONLY()));

        JavaDStream<String> word = line.flatMap(s -> {
            String[] splits = s.split(" ");
            return Arrays.asList(splits).iterator();
        });
        JavaPairDStream<String, Integer> pair = word.mapToPair(s -> new Tuple2<>(s, 1));
        JavaPairDStream<String, Integer> res = pair.reduceByKey(Integer::sum);

        res.print();

        ssc.start();
        ssc.awaitTermination();

    }

    static class CustormReceiver extends Receiver<String>{

        private final String host;
        private final Integer port;

        public CustormReceiver(String host, Integer port,StorageLevel storageLevel) {
            super(storageLevel);
            this.host = host;
            this.port = port;
        }

        Socket socket;
        BufferedReader reader;

        @Override
        public void onStart() {

            try {
                socket = new Socket(host, port);
            } catch (IOException e) {
                restart("Error connecting to $host:$port", e);
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    receive();
                }
            }).start();
        }

        public void receive(){

            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.defaultCharset()));
            } catch (IOException e) {
                e.printStackTrace();
            }

            String line = null;
            try {
                line = reader.readLine();
                while(!isStopped() && line != null){
                    store(line);
                    line = reader.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onStop() {
            if (socket != null) {
                try {
                    reader.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                reader = null;
                socket = null;
            }
        }
    }
}
