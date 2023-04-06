package com.zgg.core.工程化代码.service;

import com.zgg.core.工程化代码.common.Service;
import com.zgg.core.工程化代码.controller.WordCountController;
import com.zgg.core.工程化代码.dao.WordCountDao;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * 服务层：业务逻辑处理
 */
public class WordCountService implements Service, Serializable {

    private WordCountDao wordCountDao = new WordCountDao();

    @Override
    public List<Tuple2<String, Integer>> dataAnalysis(){

        JavaRDD<String> data = wordCountDao.readFile("src/main/resources/wc.txt");

        JavaRDD<String> words = data.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public Iterator<String> call(String s) throws Exception {
                return Arrays.asList(s.split("\\|")).iterator();
            }
        });

        JavaPairRDD<String, Integer> wordCountPair = words.mapToPair(new PairFunction<String, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(String s) throws Exception {
                return new Tuple2<String,Integer>(s,1);
            }
        });

        JavaPairRDD<String, Integer> counts = wordCountPair.reduceByKey(new Function2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer v1, Integer v2) throws Exception {
                return v1 + v2;
            }
        });

        return counts.collect();
    }
}
