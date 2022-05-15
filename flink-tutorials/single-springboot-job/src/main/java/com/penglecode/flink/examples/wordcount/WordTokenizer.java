package com.penglecode.flink.examples.wordcount;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.util.Collector;

/**
 * 单词分词器
 */
public class WordTokenizer implements FlatMapFunction<String, Tuple2<String,Integer>> {
    @Override
    public void flatMap(String value, Collector<Tuple2<String, Integer>> out) {
        String[] words = value.split(" ");
        for(String word : words) {
            out.collect(new Tuple2<>(word, 1));
        }
    }
}