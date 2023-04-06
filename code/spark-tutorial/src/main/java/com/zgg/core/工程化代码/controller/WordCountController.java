package com.zgg.core.工程化代码.controller;

import com.zgg.core.工程化代码.common.Controller;
import com.zgg.core.工程化代码.service.WordCountService;
import scala.Tuple2;

import java.util.List;

/**
 * 控制器：总控整个流程
 */
public class WordCountController implements Controller {
    private WordCountService wordCountService = new WordCountService();

    @Override
    public void dispatch() {
        List<Tuple2<String, Integer>> outputs = wordCountService.dataAnalysis();

        for (Tuple2<String, Integer> output : outputs) {
            System.out.println(output._1 + " - " + output._2);
        }
    }
}