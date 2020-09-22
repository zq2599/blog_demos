package com.bolingcavalry.customize;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Description: 自定义sink
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/4/12 21:29
 */
public class MySQLSinkFunction extends RichSinkFunction<Student> {

    PreparedStatement preparedStatement;

    private Connection connection;

    private ReentrantLock reentrantLock = new ReentrantLock();

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);

        //准备数据库相关实例
        buildPreparedStatement();
    }

    @Override
    public void close() throws Exception {
        super.close();

        try{
            if(null!=preparedStatement) {
                preparedStatement.close();
                preparedStatement = null;
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        try{
            if(null!=connection) {
                connection.close();
                connection = null;
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void invoke(Student value, Context context) throws Exception {
        preparedStatement.setString(1, value.getName());
        preparedStatement.setInt(2, value.getAge());
        preparedStatement.executeUpdate();
    }

    /**
     * 准备好connection和preparedStatement
     * 获取mysql连接实例，考虑多线程同步，
     * 不用synchronize是因为获取数据库连接是远程操作，耗时不确定
     * @return
     */
    private void buildPreparedStatement() {
        if(null==connection) {
            boolean hasLock = false;
            try {
                hasLock = reentrantLock.tryLock(10, TimeUnit.SECONDS);

                if(hasLock) {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    connection = DriverManager.getConnection("jdbc:mysql://192.168.50.43:3306/flinkdemo?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=UTC", "root", "123456");
                }

                if(null!=connection) {
                    preparedStatement = connection.prepareStatement("insert into student (name, age) values (?, ?)");
                }
            } catch (Exception e) {
                //生产环境慎用
                e.printStackTrace();
            } finally {
                if(hasLock) {
                    reentrantLock.unlock();
                }
            }
        }
    }
}
