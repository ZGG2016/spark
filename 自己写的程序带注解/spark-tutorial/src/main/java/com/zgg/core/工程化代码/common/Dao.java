package com.zgg.core.工程化代码.common;

import org.apache.spark.api.java.JavaRDD;

public interface Dao {
    JavaRDD<String> readFile(String path);
}
