package com.bolingcavalry.addsink;

import com.datastax.driver.mapping.Mapper;
import com.datastax.shaded.netty.util.Recycler;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.PrintSinkFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.cassandra.CassandraSink;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;

import java.util.Properties;

/**
 * @Description: kafka取得字符串，wordcount处理成pojo再写入cassandra
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/4/6 18:24
 */
public class CassandraPojoSink {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //设置并行度
        env.setParallelism(1);

        //连接kafka用到的属性对象
        Properties properties = new Properties();
        //broker地址
        properties.setProperty("bootstrap.servers", "192.168.50.43:9092");
        //zookeeper地址
        properties.setProperty("zookeeper.connect", "192.168.50.43:2181");
        //消费者的groupId
        properties.setProperty("group.id", "flink-connector");
        //实例化Consumer类
        FlinkKafkaConsumer<String> flinkKafkaConsumer = new FlinkKafkaConsumer<>(
                "test001",
                new SimpleStringSchema(),
                properties
        );

        //指定从最新位置开始消费，相当于放弃历史消息
        flinkKafkaConsumer.setStartFromLatest();

        //通过addSource方法得到DataSource
        DataStream<String> dataStream = env.addSource(flinkKafkaConsumer);

        DataStream<WordCount> result = dataStream
                .flatMap(new FlatMapFunction<String, WordCount>() {
                    @Override
                    public void flatMap(String s, Collector<WordCount> collector) throws Exception {
                        String[] words = s.toLowerCase().split("\\s");

                        for (String word : words) {
                            if (!word.isEmpty()) {
                                //cassandra的表中，每个word都是主键，因此不能为空
                                collector.collect(new WordCount(word, 1L));
                            }
                        }
                    }
                })
                .keyBy("word")
                .timeWindow(Time.seconds(5))
                .reduce(new ReduceFunction<WordCount>() {
                    @Override
                    public WordCount reduce(WordCount wordCount, WordCount t1) throws Exception {
                        return new WordCount(wordCount.getWord(), wordCount.getCount() + t1.getCount());
                    }
                });

        result.addSink(new PrintSinkFunction<>())
                .name("print Sink")
                .disableChaining();

        CassandraSink.addSink(result)
                .setHost("192.168.133.168")
                .setMapperOptions(() -> new Mapper.Option[] { Mapper.Option.saveNullFields(true) })
                .build()
                .name("cassandra Sink")
                .disableChaining();

        env.execute("kafka-2.4 source, cassandra-3.11.6 sink, pojo");
    }


}