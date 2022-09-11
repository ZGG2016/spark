package com.zgg.sql.program;

import com.zgg.sql.bean.BufferZone;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.expressions.Aggregator;
import org.apache.spark.sql.functions;

import java.util.*;

import static org.apache.spark.sql.functions.udf;

public class TopThree {
    public static void main(String[] args) {
        System.setProperty("HADOOP_USER_NAME", "root");
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("Top3")
                .set("spark.sql.warehouse.dir", "hdfs://bigdata101:9000/user/hive/warehouse");

        SparkSession sparkSession = SparkSession
                .builder()
                .enableHiveSupport()
                .config(conf)
                .getOrCreate();

        // 连接表，查询基本数据
        sparkSession
                .sql(
                "select\n" +
                        "a.*,\n" +
                        "p.product_name,\n" +
                        "c.area,\n" +
                        "c.city_name\n" +
                        "from user_visit_action a\n" +
                        "join product_info p on a.click_product_id = p.product_id\n" +
                        "join city_info c on a.city_id = c.city_id\n" +
                        "where a.click_product_id > -1")
                .createOrReplaceTempView("t1");

        // 根据区域，商品进行数据聚合
        sparkSession.udf().register("cityRemark", functions.udaf(new CityRemarkUDAF(),Encoders.STRING()));
        sparkSession
                .sql(
                "select\n" +
                        "area,\n" +
                        "product_name,\n" +
                        "count(*) as clickCnt, \n" +
                        "cityRemark(city_name) as city_remark\n" +
                        "from t1 group by area, product_name")
                .createOrReplaceTempView("t2");

        // 区域内对点击数量进行排行
        sparkSession
                .sql("select\n" +
                            "*,\n" +
                            "rank() over( partition by area order by clickCnt desc ) as rank\n" +
                            "from t2")
                .createOrReplaceTempView("t3");

        // 取前3名
        sparkSession
                .sql("select *\n" +
                            " from t3 where rank <= 3")
                .show(false);

        sparkSession.close();
    }
    /*
        自定义聚合函数：实现城市备注功能
          1. 继承Aggregator, 定义泛型
             IN ： 城市名称
             BUF : Buffer =>【总点击数量，Map[（city, cnt）, (city, cnt)]】
             OUT : 备注信息
          2. 重写方法（6）
     */
    static class CityRemarkUDAF extends Aggregator<String, BufferZone,String> {

        @Override
        public BufferZone zero() {
            return new BufferZone(0L, new HashMap<>());
        }

        @Override
        public BufferZone reduce(BufferZone buff, String city) {
            buff.setTotal(buff.getTotal() + 1L);

            long newCount = buff.getCityMap().getOrDefault(city, 0L) + 1L;

            buff.getCityMap().put(city,newCount);
            return buff;
        }

        @Override
        public BufferZone merge(BufferZone buff1, BufferZone buff2) {
            buff1.setTotal( buff1.getTotal()+buff2.getTotal() );

            HashMap<String, Long> cityMap1 = buff1.getCityMap();
            HashMap<String, Long> cityMap2 = buff2.getCityMap();

            cityMap2.forEach((k,v)->{
                long newCount = cityMap1.getOrDefault(k, 0L) + v;
                cityMap1.put(k,newCount);
            });
            System.out.println(cityMap1);
            // TODO A method named "setCityMap" is not declared in any enclosing class nor any supertype
            buff1.setCityMap(cityMap1);
            return buff1;
        }

        // 将统计的结果生成字符串信息
        @Override
        public String finish(BufferZone buff) {

            Long total = buff.getTotal();
            HashMap<String, Long> cityMap = buff.getCityMap();
            ArrayList<Map.Entry<String, Long>> list = new ArrayList<>(cityMap.entrySet());

            list.sort((o1, o2) -> (int) (o2.getValue()- o1.getValue()));

            long rsum = 0L;
            StringBuilder sb = new StringBuilder();
            List<Map.Entry<String, Long>> subList = list.subList(0, 3);
            for (Map.Entry<String, Long> entry:subList) {
                long r = entry.getValue() * 100L / total;
                sb.append(entry.getKey()).append(r).append("%");
                rsum += r;
            }
            if (cityMap.size()>2){
                sb.append("其他").append(100-rsum).append("%");
            }
            return sb.toString();
        }

        @Override
        public Encoder<BufferZone> bufferEncoder() {
            return Encoders.bean(BufferZone.class);
        }

        @Override
        public Encoder<String> outputEncoder() {
            return Encoders.STRING();
        }
    }
}
