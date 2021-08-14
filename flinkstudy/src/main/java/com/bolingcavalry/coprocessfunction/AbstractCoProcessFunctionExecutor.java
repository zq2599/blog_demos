package com.bolingcavalry.coprocessfunction;

import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.CoProcessFunction;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2020-11-09 17:33
 * @description 串起整个逻辑的执行类，用于体验CoProcessFunction
 */
public abstract class AbstractCoProcessFunctionExecutor {

    /**
     * 返回CoProcessFunction的实例，这个方法留给子类实现
     * @return
     */
    protected abstract CoProcessFunction<
            Tuple2<String, Integer>,
            Tuple2<String, Integer>,
            Tuple2<String, Integer>> getCoProcessFunctionInstance();

    /**
     * 监听根据指定的端口，
     * 得到的数据先通过map转为Tuple2实例，
     * 给元素加入时间戳，
     * 再按f0字段分区，
     * 将分区后的KeyedStream返回
     * @param port
     * @return
     */
    protected KeyedStream<Tuple2<String, Integer>, Tuple> buildStreamFromSocket(StreamExecutionEnvironment env, int port) {
        return env
                // 监听端口
                .socketTextStream("localhost", port)
                // 得到的字符串"aaa,3"转成Tuple2实例，f0="aaa"，f1=3
                .map(new WordCountMap())
                // 将单词作为key分区
                .keyBy(0);
    }

    /**
     * 如果子类有侧输出需要处理，请重写此方法，会在主流程执行完毕后被调用
     */
    protected void doSideOutput(SingleOutputStreamOperator<Tuple2<String, Integer>> mainDataStream) {
    }

    /**
     * 执行业务的方法
     * @throws Exception
     */
    public void execute() throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 并行度1
        env.setParallelism(1);

        // 监听9998端口的输入
        KeyedStream<Tuple2<String, Integer>, Tuple> stream1 = buildStreamFromSocket(env, 9998);

        // 监听9999端口的输入
        KeyedStream<Tuple2<String, Integer>, Tuple> stream2 = buildStreamFromSocket(env, 9999);

        SingleOutputStreamOperator<Tuple2<String, Integer>> mainDataStream = stream1
                // 两个流连接
                .connect(stream2)
                // 执行低阶处理函数，具体处理逻辑在子类中实现
                .process(getCoProcessFunctionInstance());

        // 将低阶处理函数输出的元素全部打印出来
        mainDataStream.print();

        // 侧输出相关逻辑，子类有侧输出需求时重写此方法
        doSideOutput(mainDataStream);

        // 执行
        env.execute("ProcessFunction demo : CoProcessFunction");
    }
}
