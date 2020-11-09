package com.bolingcavalry.coprocessfunction;

import com.bolingcavalry.Splitter;
import com.bolingcavalry.keyedprocessfunction.ProcessTime;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2020-11-09 17:33
 * @description CoProcessFunction的基本功能体验(时间类型是处理时间)
 */
public class SimpleCoProcess {

    private static final Logger logger = LoggerFactory.getLogger(SimpleCoProcess.class);

    /**
     * 根据指定的端口监听，
     * 得到的数据先通过map转为Tuple2实例，
     * 给元素加入时间戳，
     * 再按f0字段分区，
     * 将分区后的KeyedStream返回
     * @param port
     * @return
     */
    private static KeyedStream<Tuple2<String, Integer>, Tuple> buildStreamFromSocket(StreamExecutionEnvironment env, int port) {
        return env
                // 监听端口
                .socketTextStream("localhost", port)
                // 得到的字符串"aaa,3"转成Tuple2实例，f0="aaa"，f1=3
                .map(new WordCountMapFunction())
                // 给元素加入时间戳
                .assignTimestampsAndWatermarks(new TimestampAssigner())
                // 将单词作为key分区
                .keyBy(0);
    }


    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 并行度1
        env.setParallelism(1);

        // 处理时间
        env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);

        // 监听9998端口的输入
        KeyedStream<Tuple2<String, Integer>, Tuple> stream1 = buildStreamFromSocket(env, 9998);

        // 监听9999端口的输入
        KeyedStream<Tuple2<String, Integer>, Tuple> stream2 = buildStreamFromSocket(env, 9999);

        stream1
            .connect(stream2)
            .process(new TwoSourceAddFunction())
            .print();

        // 执行
        env.execute("ProcessFunction demo : KeyedProcessFunction");
    }

    public static String time(long timeStamp) {
        return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(timeStamp));
    }
}
