package com.zgg.streaming.program;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.*;

public class GenerateData {
    public static void main(String[] args) {
        // 生成模拟数据
        // 格式 ：timestamp area city userid adid
        // 含义： 时间戳   区域  城市 用户 广告

        // Application => Kafka => SparkStreaming => Analysis

        HashMap<String,Object> kafkaParams = new HashMap<>();
        kafkaParams.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "bigdata101:9092");
        kafkaParams.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        kafkaParams.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(kafkaParams);

        while (true){
            ArrayList<String> list = mockData();
            for (String item : list){
                ProducerRecord<String,String> record = new ProducerRecord<>("spark", item);
                kafkaProducer.send(record);
//                System.out.println(item);
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static ArrayList<String> mockData(){
        ArrayList<String> list = new ArrayList<>();

        List<String> areaList = Arrays.asList("华北", "华东", "华南");
        List<String> cityList = Arrays.asList("北京", "上海", "深圳");

        Random random = new Random();
        for (int i = 1; i < random.nextInt(50); i++) {
            String area = areaList.get(random.nextInt(3));
            String city = cityList.get(random.nextInt(3));
            int userid = new Random().nextInt(6) + 1;
            int adid = new Random().nextInt(6) + 1;

            String record = String.valueOf(System.currentTimeMillis()) + " " +
                            area + " " +
                            city + " " +
                            String.valueOf(userid) + " " +
                            String.valueOf(adid);
            list.add(record);
        }
        return list;
    }
}
