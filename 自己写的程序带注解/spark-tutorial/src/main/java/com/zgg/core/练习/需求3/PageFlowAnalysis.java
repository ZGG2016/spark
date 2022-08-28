package com.zgg.core.练习.需求3;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.*;
import scala.Tuple2;

import java.util.*;

/**
 * 计算指定页面的单跳转化率
 */
public class PageFlowAnalysis {
    public static void main(String[] args) {

        SparkConf conf = new SparkConf().setAppName("PageFlowAnalysis").setMaster("local[*]");
        JavaSparkContext sc = new JavaSparkContext(conf);

        JavaRDD<String> rdd = sc.textFile("src/main/resources/user_visit_action.txt");
        JavaRDD<UserVisitAction> actionDataRDD = rdd
                .map(new Function<String, UserVisitAction>() {
                    @Override
                    public UserVisitAction call(String str) throws Exception {
                        String[] splits = str.split("_");
                        return new UserVisitAction(splits[0], Integer.parseInt(splits[1]),
                                splits[2], Integer.parseInt(splits[3]), splits[4], splits[5],
                                Integer.parseInt(splits[6]), Integer.parseInt(splits[7]), splits[8],
                                splits[9], splits[10], splits[11], Integer.parseInt(splits[12])
                        );
                    }
                })
                .cache();

        // 对指定的页面连续跳转进行统计
        List<Integer> pageIds = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
        ArrayList<Tuple2<Integer, Integer>> flowPageIds = new ArrayList<>();
        for (int i = 0; i < pageIds.size() - 1; i++) {
            flowPageIds.add(new Tuple2<>(pageIds.get(i), pageIds.get(i + 1)));
        }

        // 计算分母
        List<Tuple2<Integer, Integer>> pageIdCountLst = actionDataRDD
                .filter(new Function<UserVisitAction, Boolean>() {
                    @Override
                    public Boolean call(UserVisitAction uva) throws Exception {
                        // 注意这里，去掉了最后一个元素
                        return pageIds.subList(0,6).contains(uva.getPage_id());
                    }
                })
                .mapToPair(new PairFunction<UserVisitAction, Integer, Integer>() {
                    @Override
                    public Tuple2<Integer, Integer> call(UserVisitAction uva) throws Exception {
                        return new Tuple2<>(uva.getPage_id(), 1);
                    }
                })
                .reduceByKey(Integer::sum)
                .collect();
        // 转换数据结构
        HashMap<Integer, Integer> pageIdCount = new HashMap<>();
        for (Tuple2<Integer, Integer> pic : pageIdCountLst){
            pageIdCount.put(pic._1, pic._2);
        }

        /*
          计算分子
              就是计算 page a --> page b 的数量，
              --> 那么就得得到这种形式的数据结构
              --> 那么就得得到在一次会话中的页面浏览顺序，即 page a --> page b --> page c  --> page d 
              --> 那么就得 对数据分组、按访问时间排序
         */
        JavaPairRDD<String, Iterable<UserVisitAction>> groupRDD = actionDataRDD.groupBy(new Function<UserVisitAction, String>() {
            @Override
            public String call(UserVisitAction uva) throws Exception {
                return uva.getSession_id();
            }
        });

        JavaPairRDD<String, ArrayList<Tuple2<Tuple2<Integer, Integer>,Integer>>> mapValuesRDD = groupRDD
                .mapValues(new Function<Iterable<UserVisitAction>, ArrayList<Tuple2<Tuple2<Integer, Integer>,Integer>>>() {
                    @Override
                    public ArrayList<Tuple2<Tuple2<Integer, Integer>,Integer>> call(Iterable<UserVisitAction> v) throws Exception {
                        ArrayList<UserVisitAction> arrayList = new ArrayList<>();
                        Iterator<UserVisitAction> iter = v.iterator();
                        while (iter.hasNext()) {
                            arrayList.add(iter.next());
                        }
                        // 按访问时间排序
                        Collections.sort(arrayList, new Comparator<UserVisitAction>() {
                            @Override
                            public int compare(UserVisitAction o1, UserVisitAction o2) {
                                return o1.getAction_time().compareTo(o2.getAction_time());
                            }
                        });

                        // 过滤掉不合法的页面跳转
                        ArrayList<Integer> innerPageIds = new ArrayList<>();
                        for (UserVisitAction ts : arrayList) {
                            innerPageIds.add(ts.getPage_id());
                        }
                        // 取出来 页面间跳转关系
                        ArrayList<Tuple2<Integer, Integer>> innerFlowPageIds = new ArrayList<>();
                        for (int i = 0; i < innerPageIds.size() - 1; i++) {
                            innerFlowPageIds.add(new Tuple2<>(innerPageIds.get(i), innerPageIds.get(i + 1)));
                        }
                        // 过滤掉不合法的页面跳转
                        ArrayList<Tuple2<Tuple2<Integer, Integer>,Integer>> res = new ArrayList<>();
                        for (Tuple2<Integer, Integer> flowPageId : innerFlowPageIds) {
                            if (flowPageIds.contains(flowPageId)) {
                                res.add(new Tuple2<Tuple2<Integer, Integer>,Integer>(flowPageId, 1));
                            }
                        }
                        // 注意这里的返回值类型
                        return res;
                    }
                });

        // 取出来 ArrayList<Tuple2<Tuple2<Integer, Integer>, Integer>> 这一部分   ((page a,page b) 1)
        // 转换后，再计算
        JavaPairRDD<Tuple2<Integer, Integer>, Integer> dataRDD = mapValuesRDD
                .map(new Function<Tuple2<String, ArrayList<Tuple2<Tuple2<Integer, Integer>, Integer>>>, ArrayList<Tuple2<Tuple2<Integer, Integer>, Integer>>>() {
                    @Override
                    public ArrayList<Tuple2<Tuple2<Integer, Integer>, Integer>> call(Tuple2<String, ArrayList<Tuple2<Tuple2<Integer, Integer>, Integer>>> v) throws Exception {
                        return v._2;
                    }
                })
                .flatMapToPair(new PairFlatMapFunction<ArrayList<Tuple2<Tuple2<Integer, Integer>, Integer>>, Tuple2<Integer, Integer>, Integer>() {
                    @Override
                    public Iterator<Tuple2<Tuple2<Integer, Integer>, Integer>> call(ArrayList<Tuple2<Tuple2<Integer, Integer>, Integer>> t) throws Exception {
                        return t.iterator();
                    }
                })
                .reduceByKey(Integer::sum);

        // 计算单跳转换率
        dataRDD.foreach(new VoidFunction<Tuple2<Tuple2<Integer, Integer>, Integer>>() {
            @Override
            public void call(Tuple2<Tuple2<Integer, Integer>, Integer> t) throws Exception {
                Integer down = pageIdCount.getOrDefault(t._1._1, 0);
                double innerRes = 0.0;
                try{
                    innerRes = t._2 * 1.0 / down;
                    System.out.println("页面" + t._1._1 + "跳转到页面" + t._1._2 + "单跳转换率为: " + innerRes);
                } catch (ArithmeticException ae){
                    System.out.println("除数为0");
                }

            }
        });

        sc.stop();
    }
}
