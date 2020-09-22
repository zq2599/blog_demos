package com.bolingcavalry.api;

import com.bolingcavalry.Splitter;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2020-03-14 20:27
 * @description socket类型的DataSource的demo
 */
public class Socket {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //监听本地9999端口，读取字符串
        DataStream<String> socketDataStream = env.socketTextStream("localhost", 9999);

        //每五秒钟一次，将当前五秒内所有字符串以空格分割，然后统计单词数量，打印出来
        socketDataStream
                .flatMap(new Splitter())
                .keyBy(0)
                .timeWindow(Time.seconds(5))
                .sum(1)
                .print();

        env.execute("API DataSource demo : socket");
    }
}