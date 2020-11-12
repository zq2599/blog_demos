package com.bolingcavalry.coprocessfunction;

import com.bolingcavalry.Utils;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
import org.apache.flink.streaming.api.functions.co.CoProcessFunction;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.util.OutputTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2020-11-11 09:48
 * @description 将两个流中相通key的value相加，当key在一个流中出现后，
 *              会在有限时间内等待它在另一个流中出现，如果超过等待时间任未出现就在旁路输出
 */
public class AddTwoSourceValueWithTimeout extends AbstractCoProcessFunctionExecutor {

    private static final Logger logger = LoggerFactory.getLogger(AddTwoSourceValueWithTimeout.class);



    // 假设aaa流入1号源后，在2号源超过10秒没有收到aaa，那么1号源的aaa就会流入source1SideOutput
    final OutputTag<String> source1SideOutput = new OutputTag<String>("source1-sideoutput"){};

    // 假设aaa流入2号源后，如果1号源超过10秒没有收到aaa，那么2号源的aaa就会流入source2SideOutput
    final OutputTag<String> source2SideOutput = new OutputTag<String>("source2-sideoutput"){};

    /**
     * 重写父类的方法，保持父类逻辑不变，仅增加了时间戳分配器，向元素中加入时间戳
     * @param port
     * @return
     */
    @Override
    protected KeyedStream<Tuple2<String, Integer>, Tuple> buildStreamFromSocket(StreamExecutionEnvironment env, int port) {
        return env
                // 监听端口
                .socketTextStream("localhost", port)
                // 得到的字符串"aaa,3"转成Tuple2实例，f0="aaa"，f1=3
                .map(new WordCountMap())
                // 设置时间戳分配器，用当前时间作为时间戳
                .assignTimestampsAndWatermarks(new AssignerWithPeriodicWatermarks<Tuple2<String, Integer>>() {

                    @Override
                    public long extractTimestamp(Tuple2<String, Integer> element, long previousElementTimestamp) {
                        long timestamp = System.currentTimeMillis();
                        logger.info("添加时间戳，值：{}，时间戳：{}", element, Utils.time(timestamp));
                        // 使用当前系统时间作为时间戳
                        return timestamp;
                    }

                    @Override
                    public Watermark getCurrentWatermark() {
                        // 本例不需要watermark，返回null
                        return null;
                    }
                })
                // 将单词作为key分区
                .keyBy(0);
    }

    @Override
    protected CoProcessFunction<Tuple2<String, Integer>, Tuple2<String, Integer>, Tuple2<String, Integer>> getCoProcessFunctionInstance() {
        return new ExecuteWithTimeoutCoProcessFunction(source1SideOutput, source2SideOutput);
    }

    @Override
    protected void doSideOutput(SingleOutputStreamOperator<Tuple2<String, Integer>> mainDataStream) {
        // 两个侧输出都直接打印
        mainDataStream.getSideOutput(source1SideOutput).print();
        mainDataStream.getSideOutput(source2SideOutput).print();
    }

    public static void main(String[] args) throws Exception {
        new AddTwoSourceValueWithTimeout().execute();
    }

}
