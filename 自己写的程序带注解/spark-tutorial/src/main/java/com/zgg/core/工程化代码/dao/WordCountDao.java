package com.zgg.core.工程化代码.dao;

import com.zgg.core.工程化代码.common.Dao;
import com.zgg.core.工程化代码.util.EnvUtil;
import org.apache.spark.api.java.JavaRDD;

import java.io.Serializable;

/**
 * 持久层: 和数据源（库）的交互
 */
public class WordCountDao implements Dao, Serializable {

    @Override
    public JavaRDD<String> readFile(String path){
        return EnvUtil.getEnvFromThreadLocal().textFile(path);
    }

}
