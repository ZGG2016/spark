package com.zgg.sql.udfandudaf;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.expressions.MutableAggregationBuffer;
import org.apache.spark.sql.expressions.UserDefinedAggregateFunction;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.apache.spark.sql.functions.udf;

/**
 * UADF函数：弱类型实现
 */
public class UADFDemo1 {
    public static void main(String[] args) {
        // 创建SparkSQL的运行环境
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("UADFDemo1");
        SparkSession sparkSession = SparkSession.builder().config(conf).getOrCreate();

        Dataset<Row> df = sparkSession.read().json("src/main/resources/people.json");
        df.createOrReplaceTempView("people");

        sparkSession.udf().register("ageAvg", new MyAvgUDAF());
        // 给名字添加一个前缀
        sparkSession.sql("select ageAvg(age) from people").show();

        sparkSession.close();
    }

    /*
        自定义聚合函数类：计算年龄的平均值
        1. 继承UserDefinedAggregateFunction
        2. 重写方法(8个)
    */
    static class MyAvgUDAF extends UserDefinedAggregateFunction {

        // 输入数据的结构
        @Override
        public StructType inputSchema() {
            StructField field = DataTypes.createStructField("age", DataTypes.StringType, false);
            List<StructField> structFields = Collections.singletonList(field);
            return DataTypes.createStructType(structFields);
        }

        // 缓冲区数据的结构
        @Override
        public StructType bufferSchema() {
            ArrayList<StructField> fields = new ArrayList<>();

            StructField field1 = DataTypes.createStructField("total", DataTypes.LongType, true);
            StructField field2 = DataTypes.createStructField("count", DataTypes.LongType, true);
            fields.add(field1);
            fields.add(field2);

            return DataTypes.createStructType(fields);
        }

        // 函数计算结果的数据类型
        @Override
        public DataType dataType() {
            return DataTypes.DoubleType;
        }

        // 函数的稳定性
        @Override
        public boolean deterministic() {
            return true;
        }

        // 缓冲区初始化
        @Override
        public void initialize(MutableAggregationBuffer buffer) {
            buffer.update(0, 0L);
            buffer.update(1, 0L);
        }

        // 根据输入的值更新缓冲区数据
        @Override
        public void update(MutableAggregationBuffer buffer, Row input) {
            buffer.update(0, buffer.getLong(0) + Long.parseLong(input.getString(0)));
            buffer.update(1, buffer.getLong(1) + 1);
        }

        // 缓冲区数据合并
        @Override
        public void merge(MutableAggregationBuffer buffer1, Row buffer2) {
            buffer1.update(0, buffer1.getLong(0) + buffer2.getLong(0));
            buffer1.update(1, buffer1.getLong(1) + buffer2.getLong(1));
        }

        // 计算平均值
        @Override
        public Object evaluate(Row buffer) {
            return buffer.getLong(0) / buffer.getLong(1);
        }
    }
}
