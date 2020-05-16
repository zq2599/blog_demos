package com.bolingcavalry.processfunction;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.util.ArrayList;
import java.util.List;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2020-05-16 18:18
 * @description 体验SideOutput
 */
public class SideOutput {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 并行度为1
        env.setParallelism(1);

        // 定义OutputTag
        final OutputTag<String> outputTag = new OutputTag<String>("side-output"){};

        // 创建一个List，里面有两个Tuple2元素
        List<Tuple2<String, Integer>> list = new ArrayList<>();
        list.add(new Tuple2("aaa", 1));
        list.add(new Tuple2("bbb", 2));
        list.add(new Tuple2("ccc", 3));

        //通过List创建DataStream
        DataStream<Tuple2<String, Integer>> fromCollectionDataStream = env.fromCollection(list);

        //所有元素都进入mainDataStream，f1字段为奇数的元素进入SideOutput
        SingleOutputStreamOperator<String> mainDataStream = fromCollectionDataStream
                .process(new ProcessFunction<Tuple2<String, Integer>, String>() {
                    @Override
                    public void processElement(Tuple2<String, Integer> value, Context ctx, Collector<String> out) throws Exception {

                        //进入主流程的下一个算子
                        out.collect("main, name : " + value.f0 + ", value : " + value.f1);

                        //f1字段为奇数的元素进入SideOutput
                        if(1 == value.f1 % 2) {
                            ctx.output(outputTag, "side, name : " + value.f0 + ", value : " + value.f1);
                        }
                    }
                });

        // 禁止chanin，这样可以在页面上看清楚原始的DAG
        mainDataStream.disableChaining();

        // 取得旁路数据
        DataStream<String> sideDataStream = mainDataStream.getSideOutput(outputTag);


        mainDataStream.print();
        sideDataStream.print();

        env.execute("processfunction demo : sideoutput");
    }
}
