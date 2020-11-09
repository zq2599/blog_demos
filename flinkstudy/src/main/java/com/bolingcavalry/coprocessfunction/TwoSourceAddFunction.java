package com.bolingcavalry.coprocessfunction;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.co.CoProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2020-11-09 21:39
 * @description 功能介绍
 */
public class TwoSourceAddFunction extends CoProcessFunction<
        Tuple2<String, Integer>,
        Tuple2<String, Integer>,
        Tuple2<String, Integer>> {

    private static final Logger logger = LoggerFactory.getLogger(TwoSourceAddFunction.class);

    @Override
    public void processElement1(Tuple2<String, Integer> value, Context ctx, Collector<Tuple2<String, Integer>> out) throws Exception {
        logger.info("processElement1, value {}, timestamp [{}]", value, ctx.timestamp());
    }

    @Override
    public void processElement2(Tuple2<String, Integer> value, Context ctx, Collector<Tuple2<String, Integer>> out) throws Exception {
        logger.info("processElement2, value {}, timestamp [{}]", value, ctx.timestamp());
    }
}