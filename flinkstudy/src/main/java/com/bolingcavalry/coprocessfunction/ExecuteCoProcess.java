package com.bolingcavalry.coprocessfunction;

import com.bolingcavalry.coprocessfunction.function.AddTwoSourceValue;
import com.bolingcavalry.coprocessfunction.function.CollectEveryOne;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2020-11-09 17:33
 * @description CoProcessFunction的基本功能体验(时间类型是处理时间)
 */
public class ExecuteCoProcess {

    private static final Logger logger = LoggerFactory.getLogger(ExecuteCoProcess.class);

    /**
     * 监听根据指定的端口，
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
                .map(new WordCountMap())
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
//            .process(new CollectEveryOne())
            .process(new AddTwoSourceValue())
            .print();

        // 执行
        env.execute("ProcessFunction demo : CoProcessFunction");
    }


}
