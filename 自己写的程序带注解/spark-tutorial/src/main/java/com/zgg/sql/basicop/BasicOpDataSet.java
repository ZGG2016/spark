package com.zgg.sql.basicop;

import com.zgg.sql.bean.Person;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * DataSet基本操作
 */
public class BasicOpDataSet {
    public static void main(String[] args) {
        // 创建SparkSQL的运行环境
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("BasicOpDataSet");
        SparkSession sparkSession = SparkSession.builder().config(conf).getOrCreate();

        // DataSet
        // 1.Java基本类型
        List<Integer> list = Arrays.asList(1, 2, 3);
        Encoder<Integer> integerEncoder = Encoders.INT();
        Dataset<Integer> ds1 = sparkSession.createDataset(list, integerEncoder);
        ds1.show();

        // 2. Java bean类型
        Person person = new Person();
        person.setName("Andy");
        person.setAge(32);
        List<Person> personList = Collections.singletonList(person);
        Encoder<Person> personEncoder = Encoders.bean(Person.class);
        Dataset<Person> ds2 = sparkSession.createDataset(personList, personEncoder);
        ds2.show();

        sparkSession.close();
    }
}
