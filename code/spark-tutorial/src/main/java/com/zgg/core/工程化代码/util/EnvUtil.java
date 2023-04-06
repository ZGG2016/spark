package com.zgg.core.工程化代码.util;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

public class EnvUtil {

    private static JavaSparkContext sc;

    public static JavaSparkContext createEnv(){
        String appName = "Application";
        String master = "local[*]";
        SparkConf sparkConf = new SparkConf().setAppName(appName).setMaster(master);
        sc = new JavaSparkContext(sparkConf);
        return sc;
    }

    public static JavaSparkContext createEnv(String appName, String master){
        SparkConf sparkConf = new SparkConf().setAppName(appName).setMaster(master);
        sc = new JavaSparkContext(sparkConf);
        return sc;
    }

    public static void stopEnv(){
        sc.stop();
    }

    static ThreadLocal<JavaSparkContext> scLocal = new ThreadLocal<JavaSparkContext>();

    public static void putEnvInThreadLocal(JavaSparkContext sc){
        scLocal.set(sc);
    }

    public static JavaSparkContext getEnvFromThreadLocal(){
        return scLocal.get();
    }

    public static void clearEnvInThreadLocal(){
        scLocal.remove();
    }

}
