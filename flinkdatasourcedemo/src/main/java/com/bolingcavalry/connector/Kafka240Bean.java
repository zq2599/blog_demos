package com.bolingcavalry.connector;

import com.bolingcavalry.Splitter;
import com.bolingcavalry.Student;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;

import java.util.Properties;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2020-03-15 19:56
 * @description 消费kafka2.4.0版本的消息，消息本身是个bean
 */
public class Kafka240Bean {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //设置并行度
        env.setParallelism(2);

        Properties properties = new Properties();
        //broker地址
        properties.setProperty("bootstrap.servers", "192.168.50.43:9092");
        //zookeeper地址
        properties.setProperty("zookeeper.connect", "192.168.50.43:2181");
        //消费者的groupId
        properties.setProperty("group.id", "flink-connector");
        //实例化Consumer类
        FlinkKafkaConsumer<Student> flinkKafkaConsumer = new FlinkKafkaConsumer<>(
                "test001",
                new StudentSchema(),
                properties
        );
        //指定从最新位置开始消费，相当于放弃历史消息
        flinkKafkaConsumer.setStartFromLatest();

        //通过addSource方法得到DataSource
        DataStream<Student> dataStream = env.addSource(flinkKafkaConsumer);

        //从kafka取得的JSON被反序列化成Student实例，统计每个name的数量，窗口是5秒
        dataStream.map(new MapFunction<Student, Tuple2<String, Integer>>() {
            @Override
            public Tuple2<String, Integer> map(Student student) throws Exception {
                return new Tuple2<>(student.getName(), 1);
            }
        })
                .keyBy(0)
                .timeWindow(Time.seconds(5))
                .sum(1)
                .print();

        env.execute("Connector DataSource demo : kafka bean");
    }
}
