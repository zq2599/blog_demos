package com.bolingcavalry.addsink;

import org.apache.flink.streaming.connectors.kafka.KafkaSerializationSchema;
import org.apache.kafka.clients.producer.ProducerRecord;
import java.nio.charset.StandardCharsets;

/**
 * @Description: String类型的序列化接口实现类
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/3/28 23:32
 */
public class ProducerStringSerializationSchema implements KafkaSerializationSchema<String> {

    private String topic;

    public ProducerStringSerializationSchema(String topic) {
        super();
        this.topic = topic;
    }

    @Override
    public ProducerRecord<byte[], byte[]> serialize(String element, Long timestamp) {
        return new ProducerRecord<byte[], byte[]>(topic, element.getBytes(StandardCharsets.UTF_8));
    }
}