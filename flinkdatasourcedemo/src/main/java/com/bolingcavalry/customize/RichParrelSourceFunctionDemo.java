package com.bolingcavalry.customize;

import com.bolingcavalry.Student;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2020-03-21 20:33
 * @description RichSourceFunction的实现类
 */
public class RichParrelSourceFunctionDemo {

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //并行度为2
        env.setParallelism(2);

        DataStream<Student> dataStream = env.addSource(new MySQLParrelDataSource());
        dataStream.print();

        env.execute("Customize DataSource demo : RichParrelSourceFunction");
    }
}
