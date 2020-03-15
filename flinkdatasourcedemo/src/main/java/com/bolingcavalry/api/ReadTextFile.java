package com.bolingcavalry.api;

import com.bolingcavalry.Splitter;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author will
 * @email zq2599@gmail.com®
 * @date 2020-03-15 00:21
 * @description 从text文件获取数据的demo
 */
public class ReadTextFile {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //设置并行度为1
        env.setParallelism(1);

        //用txt文件作为数据源
        DataStream<String> textDataStream = env.readTextFile("file:///Users/zhaoqin/temp/202003/14/README.txt", "UTF-8");

        //统计单词数量并打印出来
        textDataStream
                .flatMap(new Splitter())
                .keyBy(0)
                .sum(1)
                .print();

        env.execute("API DataSource demo : readTextFile");
    }
}