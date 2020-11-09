package com.bolingcavalry.coprocessfunction;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
import org.apache.flink.streaming.api.watermark.Watermark;

import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2020-11-09 21:10
 * @description 用于
 */
public class TimestampAssigner implements AssignerWithPeriodicWatermarks<Tuple2<String, Integer>> {

    private static final Logger logger = LoggerFactory.getLogger(TimestampAssigner.class);

    @Nullable
    @Override
    public Watermark getCurrentWatermark() {
        return null;
    }

    @Override
    public long extractTimestamp(Tuple2<String, Integer> element, long previousElementTimestamp) {
        // 使用当前系统时间作为时间戳
        long timestamp = System.currentTimeMillis();

        logger.info("element : {}, timestamp : [{}]", element, timestamp);

        return timestamp;
    }
}

