package com.zgg.core.工程化代码.application;

import com.zgg.core.工程化代码.controller.WordCountController;
import com.zgg.core.工程化代码.util.EnvUtil;
import org.apache.spark.api.java.JavaSparkContext;

/**
 * 程序入口
 */
public class WordCountApplication {
    public static void main(String[] args) {
        //JavaSparkContext sc = EnvUtil.createEnv("wordcountapp", "local[*]");
        JavaSparkContext sc = EnvUtil.createEnv();
        EnvUtil.putEnvInThreadLocal(sc);

        WordCountController controller = new WordCountController();
        controller.dispatch();

        EnvUtil.stopEnv();
        EnvUtil.clearEnvInThreadLocal();
    }

}
