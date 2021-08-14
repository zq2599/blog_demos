package com.bolingcavalry.coprocessfunction;

import com.bolingcavalry.Utils;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.CoProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 实现双流业务逻辑的功能类
 */
public class ExecuteWithTimeoutCoProcessFunction extends CoProcessFunction<Tuple2<String, Integer>, Tuple2<String, Integer>, Tuple2<String, Integer>> {

    private static final Logger logger = LoggerFactory.getLogger(ExecuteWithTimeoutCoProcessFunction.class);

    /**
     * 等待时间
     */
    private static final long WAIT_TIME = 10000L;

    public ExecuteWithTimeoutCoProcessFunction(OutputTag<String> source1SideOutput, OutputTag<String> source2SideOutput) {
        super();
        this.source1SideOutput = source1SideOutput;
        this.source2SideOutput = source2SideOutput;
    }

    private OutputTag<String> source1SideOutput;

    private OutputTag<String> source2SideOutput;

    // 某个key在processElement1中存入的状态
    private ValueState<Integer> state1;

    // 某个key在processElement2中存入的状态
    private ValueState<Integer> state2;

    // 如果创建了定时器，就在状态中保存定时器的key
    private ValueState<Long> timerState;

    // onTimer中拿不到当前key，只能提前保存在状态中（KeyedProcessFunction的OnTimerContext有API可以取到，但是CoProcessFunction的OnTimerContext却没有）
    private ValueState<String> currentKeyState;

    @Override
    public void open(Configuration parameters) throws Exception {
        // 初始化状态
        state1 = getRuntimeContext().getState(new ValueStateDescriptor<>("myState1", Integer.class));
        state2 = getRuntimeContext().getState(new ValueStateDescriptor<>("myState2", Integer.class));
        timerState = getRuntimeContext().getState(new ValueStateDescriptor<>("timerState", Long.class));
        currentKeyState = getRuntimeContext().getState(new ValueStateDescriptor<>("currentKeyState", String.class));
    }

    /**
     * 所有状态都清理掉
     */
    private void clearAllState() {
        state1.clear();
        state2.clear();
        currentKeyState.clear();
        timerState.clear();
    }

    @Override
    public void processElement1(Tuple2<String, Integer> value, Context ctx, Collector<Tuple2<String, Integer>> out) throws Exception {
        logger.info("processElement1：处理元素1：{}", value);

        String key = value.f0;

        Integer value2 = state2.value();

        // value2为空，就表示processElement2还没有处理或这个key，
        // 这时候就把value1保存起来
        if(null==value2) {
            logger.info("processElement1：2号流还未收到过[{}]，把1号流收到的值[{}]保存起来", key, value.f1);
            state1.update(value.f1);

            currentKeyState.update(key);

            // 开始10秒的定时器，10秒后会进入
            long timerKey = ctx.timestamp() + WAIT_TIME;
            ctx.timerService().registerProcessingTimeTimer(timerKey);
            // 保存定时器的key
            timerState.update(timerKey);
            logger.info("processElement1：创建定时器[{}]，等待2号流接收数据", Utils.time(timerKey));
        } else {
            logger.info("processElement1：2号流收到过[{}]，值是[{}]，现在把两个值相加后输出", key, value2);

            // 输出一个新的元素到下游节点
            out.collect(new Tuple2<>(key, value.f1 + value2));

            // 删除定时器（这个定时器应该是processElement2创建的）
            long timerKey = timerState.value();
            logger.info("processElement1：[{}]的新元素已输出到下游，删除定时器[{}]", key, Utils.time(timerKey));
            ctx.timerService().deleteProcessingTimeTimer(timerKey);

            clearAllState();
        }
    }

    @Override
    public void processElement2(Tuple2<String, Integer> value, Context ctx, Collector<Tuple2<String, Integer>> out) throws Exception {
        logger.info("processElement2：处理元素2：{}", value);

        String key = value.f0;

        Integer value1 = state1.value();

        // value1为空，就表示processElement1还没有处理或这个key，
        // 这时候就把value2保存起来
        if(null==value1) {
            logger.info("processElement2：1号流还未收到过[{}]，把2号流收到的值[{}]保存起来", key, value.f1);
            state2.update(value.f1);

            currentKeyState.update(key);

            // 开始10秒的定时器，10秒后会进入
            long timerKey = ctx.timestamp() + WAIT_TIME;
            ctx.timerService().registerProcessingTimeTimer(timerKey);
            // 保存定时器的key
            timerState.update(timerKey);
            logger.info("processElement2：创建定时器[{}]，等待1号流接收数据", Utils.time(timerKey));
        } else {
            logger.info("processElement2：1号流收到过[{}]，值是[{}]，现在把两个值相加后输出", key, value1);

            // 输出一个新的元素到下游节点
            out.collect(new Tuple2<>(key, value.f1 + value1));

            // 删除定时器（这个定时器应该是processElement1创建的）
            long timerKey = timerState.value();
            logger.info("processElement2：[{}]的新元素已输出到下游，删除定时器[{}]", key, Utils.time(timerKey));
            ctx.timerService().deleteProcessingTimeTimer(timerKey);

            clearAllState();
        }
    }

    @Override
    public void onTimer(long timestamp, OnTimerContext ctx, Collector<Tuple2<String, Integer>> out) throws Exception {
        super.onTimer(timestamp, ctx, out);

        String key = currentKeyState.value();

        // 定时器被触发，意味着此key只在一个中出现过
        logger.info("[{}]的定时器[{}]被触发了", key, Utils.time(timestamp));

        Integer value1 = state1.value();
        Integer value2 = state2.value();

        if(null!=value1) {
            logger.info("只有1号流收到过[{}]，值为[{}]", key, value1);
            // 侧输出
            ctx.output(source1SideOutput, "source1 side, key [" + key+ "], value [" + value1 + "]");
        }

        if(null!=value2) {
            logger.info("只有2号流收到过[{}]，值为[{}]", key, value2);
            // 侧输出
            ctx.output(source2SideOutput, "source2 side, key [" + key+ "], value [" + value2 + "]");
        }

        clearAllState();
    }
}
