package com.bolingcavalry.coprocessfunction;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.co.CoProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2020-11-11 09:48
 * @description 功能介绍
 */
public class CollectEveryOne extends AbstractCoProcessFunctionExecutor {

    private static final Logger logger = LoggerFactory.getLogger(CollectEveryOne.class);

    @Override
    protected CoProcessFunction<Tuple2<String, Integer>, Tuple2<String, Integer>, Tuple2<String, Integer>> getCoProcessFunctionInstance() {
        return new CoProcessFunction<Tuple2<String, Integer>, Tuple2<String, Integer>, Tuple2<String, Integer>>() {

            @Override
            public void processElement1(Tuple2<String, Integer> value, Context ctx, Collector<Tuple2<String, Integer>> out) {
                logger.info("处理1号流的元素：{},", value);
                out.collect(value);
            }

            @Override
            public void processElement2(Tuple2<String, Integer> value, Context ctx, Collector<Tuple2<String, Integer>> out) {
                logger.info("处理2号流的元素：{}", value);
                out.collect(value);
            }
        };
    }

    public static void main(String[] args) throws Exception {
        new CollectEveryOne().execute();
    }
}
