package com.bolingcavalry.customize;

import com.bolingcavalry.Student;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2020-03-21 20:35
 * @description 连接MySQL的数据源
 */
public class MySQLDataSource extends RichSourceFunction<Student> {

    private Connection connection = null;

    private PreparedStatement preparedStatement = null;

    private volatile boolean isRunning = true;

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);

        if(null==connection) {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://192.168.50.43:3306/flinkdemo?useUnicode=true&characterEncoding=UTF-8", "root", "123456");
        }

        if(null==preparedStatement) {
            preparedStatement = connection.prepareStatement("select id, name from student");
        }
    }

    /**
     * 释放资源
     * @throws Exception
     */
    @Override
    public void close() throws Exception {
        super.close();

        if(null!=preparedStatement) {
            try {
                preparedStatement.close();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

        if(null==connection) {
            connection.close();
        }
    }

    @Override
    public void run(SourceContext<Student> ctx) throws Exception {
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next() && isRunning) {
            Student student = new Student();
            student.setId(resultSet.getInt("id"));
            student.setName(resultSet.getString("name"));
            ctx.collect(student);
        }
    }

    @Override
    public void cancel() {
        isRunning = false;
    }
}
