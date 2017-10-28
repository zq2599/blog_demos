package com.bolingcavalry.service;

import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.message.MessageAndMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author willzhao
 * @version V1.0
 * @Description: 消费kafka的数据
 * @email zq2599@gmail.com
 * @Date 2017/10/28 下午11:47
 */
public class Processer implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Processer.class);
    // Kafka数据流
    private KafkaStream<String, String> stream;
    // 线程ID编号
    private int threadNumber;

    public Processer(KafkaStream<String, String> stream, int threadNumber) {
        this.stream = stream;
        this.threadNumber = threadNumber;
    }

    public void run() {
        // 1. 获取数据迭代器
        ConsumerIterator<String, String> iter = this.stream.iterator();
        // 2. 迭代输出数据
        while (iter.hasNext()) {
            // 2.1 获取数据值
            MessageAndMetadata value = iter.next();

            // 2.2 输出
            logger.info(this.threadNumber + ":" + ":" + value.offset() + value.key() + ":" + value.message());
        }
        // 3. 表示当前线程执行完成
        System.out.println("Shutdown Thread:" + this.threadNumber);
    }
}
