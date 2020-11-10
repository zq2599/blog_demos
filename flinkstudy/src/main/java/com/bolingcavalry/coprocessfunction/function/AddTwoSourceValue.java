package com.bolingcavalry.coprocessfunction.function;

import com.bolingcavalry.Utils;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.CoProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2020-11-09 21:39
 * @description 当同一个key在两个流中都出现之后，就将value之和输出到下游
 */
public class AddTwoSourceValue extends CoProcessFunction<
        Tuple2<String, Integer>,
        Tuple2<String, Integer>,
        Tuple2<String, Integer>> {

    private static final Logger logger = LoggerFactory.getLogger(AddTwoSourceValue.class);

    // 某个key在processElement1中存入的状态
    private ValueState<Integer> state1;

    // 某个key在processElement2中存入的状态
    private ValueState<Integer> state2;

    @Override
    public void open(Configuration parameters) throws Exception {
        // 初始化状态
        state1 = getRuntimeContext().getState(new ValueStateDescriptor<>("myState1", Integer.class));
        state2 = getRuntimeContext().getState(new ValueStateDescriptor<>("myState2", Integer.class));
    }

    @Override
    public void processElement1(Tuple2<String, Integer> value, Context ctx, Collector<Tuple2<String, Integer>> out) throws Exception {
        logger.info("处理元素1：{}, 时间戳 [{}]", value, Utils.time(ctx.timestamp()));

        String key = value.f0;

        Integer value2 = state2.value();

        // value2为空，就表示processElement2还没有处理或这个key，
        // 这时候就把value1保存起来
        if(null==value2) {
            logger.info("2号流还未收到过[{}]，把1号流收到的值[{}]保存起来", key, value.f1);
            state1.update(value.f1);
        } else {
            logger.info("2号流收到过[{}]，值是[{}]，现在把两个值相加后输出", key, value2);

            // 输出一个新的元素到下游节点
            out.collect(new Tuple2<>(key, value.f1 + value2));

            // 把2号流的状态清理掉
            state2.clear();
        }
    }

    @Override
    public void processElement2(Tuple2<String, Integer> value, Context ctx, Collector<Tuple2<String, Integer>> out) throws Exception {
        logger.info("处理元素2：{}, 时间戳 [{}]", value, Utils.time(ctx.timestamp()));

        String key = value.f0;

        Integer value1 = state1.value();

        // value1为空，就表示processElement1还没有处理或这个key，
        // 这时候就把value2保存起来
        if(null==value1) {
            logger.info("1号流还未收到过[{}]，把2号流收到的值[{}]保存起来", key, value.f1);
            state2.update(value.f1);
        } else {
            logger.info("1号流收到过[{}]，值是[{}]，现在把两个值相加后输出", key, value1);

            // 输出一个新的元素到下游节点
            out.collect(new Tuple2<>(key, value.f1 + value1));

            // 把1号流的状态清理掉
            state1.clear();
        }
    }
}