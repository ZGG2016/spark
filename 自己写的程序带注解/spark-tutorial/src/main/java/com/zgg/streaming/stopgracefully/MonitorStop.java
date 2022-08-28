package com.zgg.streaming.stopgracefully;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.spark.streaming.StreamingContextState;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class MonitorStop implements Runnable {

    JavaStreamingContext ssc;

    public MonitorStop(JavaStreamingContext ssc) {
        this.ssc = ssc;
    }

    @Override
    public void run() {
        FileSystem fs = null;
        try {
            fs = FileSystem.get(new URI("hdfs://bigdata101:9000"), new Configuration(), "root");
        } catch (Exception e) {
            e.printStackTrace();
        }

        while(true){
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            StreamingContextState sscState = ssc.getState();

            try {
                boolean exists = fs.exists(new Path("hdfs://bigdata101:9000/stopSpark"));
                if (exists && sscState == StreamingContextState.ACTIVE){
                    ssc.stop(true,true);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
