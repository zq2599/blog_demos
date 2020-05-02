/*
 * Copyright 2019 Ververica GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bolingcavalry.producekafkamessage;

import com.bolingcavalry.beans.UserBehavior;
import com.bolingcavalry.json_serde.JsonSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;

import java.io.InterruptedIOException;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * @Description: 向kafka发送消息的类
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/5/2 15:02
 */
public class KafkaProducer implements Consumer<UserBehavior> {

    private final String topic;
    private final org.apache.kafka.clients.producer.KafkaProducer<byte[], byte[]> producer;
    private final JsonSerializer<UserBehavior> serializer;

    public KafkaProducer(String kafkaTopic, String kafkaBrokers) {
        this.topic = kafkaTopic;
        this.producer = new org.apache.kafka.clients.producer.KafkaProducer<>(createKafkaProperties(kafkaBrokers));
        this.serializer = new JsonSerializer<>();
    }

    @Override
    public void accept(UserBehavior record) {
        // 将对象序列化成byte数组
        byte[] data = serializer.toJSONBytes(record);
        // 封装
        ProducerRecord<byte[], byte[]> kafkaRecord = new ProducerRecord<>(topic, data);
        // 发送
        producer.send(kafkaRecord);

        // 通过sleep控制消息的速度，请依据自身kafka配置以及flink服务器配置来调整
        try {
            Thread.sleep(500);
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }

    /**
     * kafka配置
     * @param brokers The brokers to connect to.
     * @return A Kafka producer configuration.
     */
    private static Properties createKafkaProperties(String brokers) {
        Properties kafkaProps = new Properties();
        kafkaProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        kafkaProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getCanonicalName());
        kafkaProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getCanonicalName());
        return kafkaProps;
    }

}
