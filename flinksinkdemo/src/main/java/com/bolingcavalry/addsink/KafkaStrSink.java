package com.bolingcavalry.addsink;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2020-03-14 22:08
 * @description kafka发送字符串的sink
 */
public class KafkaStrSink {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //并行度为1
        env.setParallelism(1);

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "192.168.50.43:9092");

        String topic = "test006";
        FlinkKafkaProducer<String> producer = new FlinkKafkaProducer<>(topic,
                new ProducerStringSerializationSchema(topic),
                properties,
                FlinkKafkaProducer.Semantic.EXACTLY_ONCE);


        //创建一个List，里面有两个Tuple2元素
        List<String> list = new ArrayList<>();
        list.add("aaa");
        list.add("bbb");
        list.add("ccc");
        list.add("ddd");
        list.add("eee");
        list.add("fff");
        list.add("aaa");

        //统计每个单词的数量
        env.fromCollection(list)
           .addSink(producer)
           .setParallelism(4);

        env.execute("sink demo : kafka str");
    }
}
