package com.bolingcavalry.coprocessfunction.function;

import com.bolingcavalry.Utils;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.co.CoProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2020-11-09 21:39
 * @description 一号流和二号流的每个元素都发射到下游
 */
public class CollectEveryOne extends CoProcessFunction<
        Tuple2<String, Integer>,
        Tuple2<String, Integer>,
        Tuple2<String, Integer>> {

    private static final Logger logger = LoggerFactory.getLogger(CollectEveryOne.class);

    @Override
    public void processElement1(Tuple2<String, Integer> value, Context ctx, Collector<Tuple2<String, Integer>> out) {
        logger.info("处理1号流的元素：{}, 时间戳 [{}]", value, Utils.time(ctx.timestamp()));
        out.collect(value);
    }

    @Override
    public void processElement2(Tuple2<String, Integer> value, Context ctx, Collector<Tuple2<String, Integer>> out) {
        logger.info("处理2号流的元素：{}, 时间戳 [{}]", value, Utils.time(ctx.timestamp()));
        out.collect(value);
    }
}