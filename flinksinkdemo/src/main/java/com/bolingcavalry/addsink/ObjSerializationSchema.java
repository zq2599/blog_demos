package com.bolingcavalry.addsink;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.connectors.kafka.KafkaSerializationSchema;
import org.apache.kafka.clients.producer.ProducerRecord;

import javax.annotation.Nullable;

/**
 * @Description: Tuple2类型的序列化接口实现类
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/3/28 23:32
 */
public class ObjSerializationSchema implements KafkaSerializationSchema<Tuple2<String, Integer>> {

    private String topic;
    private ObjectMapper mapper;

    public ObjSerializationSchema(String topic) {
        super();
        this.topic = topic;
    }

    @Override
    public ProducerRecord<byte[], byte[]> serialize(Tuple2<String, Integer> stringIntegerTuple2, @Nullable Long timestamp) {
        byte[] b = null;
        if (mapper == null) {
            mapper = new ObjectMapper();
        }
        try {
            b= mapper.writeValueAsBytes(stringIntegerTuple2);
        } catch (JsonProcessingException e) {
            // 注意，在生产环境这是个非常危险的操作，
            // 过多的错误打印会严重影响系统性能，请根据生产环境情况做调整
            e.printStackTrace();
        }
        return new ProducerRecord<byte[], byte[]>(topic, b);
    }
}