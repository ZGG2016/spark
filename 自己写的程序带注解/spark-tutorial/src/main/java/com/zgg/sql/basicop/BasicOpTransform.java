package com.zgg.sql.basicop;

import com.zgg.sql.bean.Person;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.*;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.ArrayList;

/**
 * RDD  <--> DataFrame DataSet 转换
 */
public class BasicOpTransform {
    public static void main(String[] args) {
        // 创建SparkSQL的运行环境
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("BasicOpTransform");
        SparkSession sparkSession = SparkSession.builder().config(conf).getOrCreate();

        // RDD => DataFrame  使用反射推断schema
        JavaRDD<Person> rdd1 = sparkSession.read()
                .textFile("src/main/resources/person.txt")
                .javaRDD()
                .map(new Function<String, Person>() {
                    @Override
                    public Person call(String str) throws Exception {
                        String[] splits = str.split("\\|");
                        Person person = new Person();
                        person.setName(splits[0]);
                        person.setAge(Integer.parseInt(splits[1]));
                        return person;
                    }
                });
        Dataset<Row> df1 = sparkSession.createDataFrame(rdd1, Person.class);
        df1.show();

        // RDD => DataFrame  通过编程指定schema
        JavaRDD<String> tmpRDD = sparkSession.sparkContext()
                .textFile("src/main/resources/person.txt",1)
                .toJavaRDD();

        String schemaString = "name age";
        ArrayList<StructField> fields = new ArrayList<>();
        for (String fieldName : schemaString.split(" ")){
            StructField field = DataTypes.createStructField(fieldName, DataTypes.StringType, true);
            fields.add(field);
        }
        StructType schema = DataTypes.createStructType(fields);
        JavaRDD<Row> rdd2 = tmpRDD.map(new Function<String, Row>() {
            @Override
            public Row call(String str) throws Exception {
                String[] splits = str.split("\\|");
                return RowFactory.create(splits[0], splits[1].trim());
            }
        });
        Dataset<Row> df2 = sparkSession.createDataFrame(rdd2, schema);
        df2.show();

        // DataFrame => RDD
        JavaRDD<Row> javaRDD = df1.javaRDD();
        javaRDD.collect().forEach(System.out::println);

        // RDD => DataSet
        Encoder<Person> personEncoder = Encoders.bean(Person.class);
        Dataset<Person> ds1 = sparkSession.createDataset(rdd1.rdd(), personEncoder);

        // DataSet => RDD
        JavaRDD<Person> personJavaRDD = ds1.javaRDD();
        personJavaRDD.collect().forEach(System.out::println);

        // DataFrame => DataSet
        Dataset<Person> ds2 = df1.as(personEncoder);
        ds2.show();

        sparkSession.close();
    }
}
