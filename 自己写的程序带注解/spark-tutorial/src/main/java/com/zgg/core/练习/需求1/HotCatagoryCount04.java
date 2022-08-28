package com.zgg.core.练习.需求1;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.util.AccumulatorV2;
import scala.Tuple2;
import scala.Tuple3;

import java.util.*;

/**
 * 优化
 */
public class HotCatagoryCount04 {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf().setAppName("HotCatagoryCount03").setMaster("local[*]");
        JavaSparkContext sc = new JavaSparkContext(conf);

        /*
            在 HotCatagoryCount03.java 中，在读数据之后，直接转成 （品类ID，点击数量，下单数量，支付数量）结构，
                 再在对应的位置上累加 (reduceByKey) 【类似wordcount】

            既然转换成了 wordcount 类型的需求，那么也就可以使用累加器
         */

        HotCatagoryAccumulator acc = new HotCatagoryAccumulator();
        sc.sc().register(acc);

        JavaRDD<String> rdd = sc.textFile("src/main/resources/user_visit_action.txt");

        rdd.foreach(new VoidFunction<String>() {
            @Override
            public void call(String str) throws Exception {
                String[] splits = str.split("_");
                if (!splits[6].equals("-1")) {
                    acc.add(splits[6] + "-" + "click");
                } else if (!splits[8].equals("null")) {
                    String[] ss = splits[8].split(",");
                    for (String s : ss) {
                        acc.add(s + "-" + "order");
                    }
                } else if (!splits[10].equals("null")) {
                    String[] ss = splits[10].split(",");
                    for (String s : ss) {
                        acc.add(s + "-" + "pay");
                    }
                }
            }
        });

        HashMap<String, HotCategory> res = acc.value();
        if (res != null) {
            List<HotCategory> hotCategories = new ArrayList<>(res.values());

            Collections.sort(hotCategories, new Comparator<HotCategory>() {
                @Override
                public int compare(HotCategory o1, HotCategory o2) {
                    if (o1.getClickCount() > o2.getClickCount()) {
                        return -1;
                    } else if (o1.getClickCount() == o2.getClickCount()) {
                        if (o1.getOrderCount() > o2.getOrderCount()) {
                            return -1;
                        } else if (o1.getOrderCount() == o2.getOrderCount()) {
                            return o2.getPayCount() - o1.getPayCount();
                        } else {
                            return 1;
                        }
                    } else {
                        return 1;
                    }
                }
            });

            // 取top10
            for (int i = 0; i < 10; i++) {
                System.out.println(hotCategories.get(i));
            }

        } else {
            System.out.println("累加器为空");
        }
        sc.stop();

    }

    // 先确定好累加器的输入输出，再思考 add 方法的逻辑
    static class HotCatagoryAccumulator extends AccumulatorV2<String, HashMap<String, HotCategory>> {

        HashMap<String, HotCategory> hm = new HashMap<>();

        @Override
        public boolean isZero() {
            return this.hm.isEmpty();
        }

        @Override
        public AccumulatorV2<String, HashMap<String, HotCategory>> copy() {
            HotCatagoryAccumulator acc = new HotCatagoryAccumulator();
            acc.hm = this.hm;
            return acc;
        }

        @Override
        public void reset() {
            this.hm.clear();
        }

        @Override
        public void add(String str) {
            String[] splits = str.split("-");
            HotCategory hotCategory = this.hm.getOrDefault(splits[0], new HotCategory(splits[0], 0, 0, 0));
            switch (splits[1]) {
                case "click":
                    hotCategory.setClickCount(hotCategory.getClickCount() + 1);
                    break;
                case "order":
                    hotCategory.setOrderCount(hotCategory.getOrderCount() + 1);
                    break;
                case "pay":
                    hotCategory.setPayCount(hotCategory.getPayCount() + 1);
                    break;
            }
            this.hm.put(splits[0], hotCategory);
        }

        @Override
        public void merge(AccumulatorV2<String, HashMap<String, HotCategory>> other) {
            other.value().forEach((k, v) -> {
                HotCategory hotCategory = this.hm.getOrDefault(k, new HotCategory(k, 0, 0, 0));
                hotCategory.setClickCount(hotCategory.getClickCount() + v.getClickCount());
                hotCategory.setOrderCount(hotCategory.getOrderCount() + v.getOrderCount());
                hotCategory.setPayCount(hotCategory.getPayCount() + v.getPayCount());
                this.hm.put(k, hotCategory);
            });
        }

        @Override
        public HashMap<String, HotCategory> value() {
            return this.hm;
        }
    }
}
