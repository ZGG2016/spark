package com.zgg.streaming.program.util;

import com.alibaba.druid.pool.DruidDataSourceFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class JDBCUtil {

    public static DataSource init() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("driverClassName", "com.mysql.cj.jdbc.Driver");
        properties.setProperty("url", "jdbc:mysql://bigdata101:3306/sparksql?useUnicode=true&characterEncoding=UTF-8");
        properties.setProperty("username", "root");
        properties.setProperty("password", "root");
        properties.setProperty("maxActive", "50");

        return DruidDataSourceFactory.createDataSource(properties);
    }

    public static Connection getConnection() throws Exception {
        return init().getConnection();
    }

    public static void executeUpdate(Connection connection, String sql, List<String> params){
        try {
            connection.setAutoCommit(false);
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            if(!params.isEmpty()){
                for (int i=0;i< params.size();i++){
                    preparedStatement.setObject(i+1,params.get(i));
                }
            }
            preparedStatement.executeUpdate();
            connection.commit();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean isExist(Connection connection, String sql, List<String> params){
        boolean flag = false;
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            if(!params.isEmpty()){
                for (int i=0;i< params.size();i++){
                    preparedStatement.setObject(i+1,params.get(i));
                }
            }
            flag = preparedStatement.executeQuery().next();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flag;
    }
}
