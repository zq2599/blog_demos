package com.bolingcavalry.service.impl;

import com.bolingcavalry.service.MessageService;
import com.bolingcavalry.service.Processer;
import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.serializer.StringDecoder;
import kafka.utils.VerifiableProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author willzhao
 * @version V1.0
 * @Description: 实现消息服务
 * @email zq2599@gmail.com
 * @Date 2017/10/28 上午9:58
 */
@Service
public class MessageServiceImpl implements MessageService{

    private static final Logger logger = LoggerFactory.getLogger(MessageServiceImpl.class);

    private ConsumerConnector consumer;

    /**
     * 线程池
     */
    private ExecutorService executorPool;

    private static final String GROUP_ID = "testgroup001";
    private static final String ZK = "hostb1:2181,hostb2:2181,hostb3:2181";
    private static final String TOPIC = "test002";
    private static final int THREAD_NUM = 2;


    @PostConstruct
    public void init(){
        logger.info("start init kafka consumer service");
        // 1. 创建Kafka连接器
        consumer = Consumer.createJavaConsumerConnector(createConsumerConfig(ZK, GROUP_ID));

        Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        topicCountMap.put(TOPIC, THREAD_NUM);

        // 2. 指定数据的解码器
        StringDecoder keyDecoder = new StringDecoder(new VerifiableProperties());
        StringDecoder valueDecoder = new StringDecoder(new VerifiableProperties());

        // 3. 获取连接数据的迭代器对象集合
        /**
         * Key: Topic主题
         * Value: 对应Topic的数据流读取器，大小是topicCountMap中指定的topic大小
         */
        Map<String, List<KafkaStream<String, String>>> consumerMap = this.consumer.createMessageStreams(topicCountMap, keyDecoder, valueDecoder);

        // 4. 从返回结果中获取对应topic的数据流处理器
        List<KafkaStream<String, String>> streams = consumerMap.get(TOPIC);

        logger.info("streams size {}", streams.size());

        // 5. 创建线程池
        this.executorPool = new ThreadPoolExecutor(THREAD_NUM, THREAD_NUM,
                0,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new CustomThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());

        // 6. 构建数据输出对象
        int threadNumber = 0;
        for (final KafkaStream<String, String> stream : streams) {
            this.executorPool.submit(new Processer(stream, threadNumber));
            threadNumber++;
        }

        logger.info("end init kafka consumer service");
    }

    /**
     * 创建ConsumerConfig对象
     * @param zookeeper
     * @param groupId
     * @return Kafka连接信息
     */
    private ConsumerConfig createConsumerConfig(String zookeeper, String groupId) {
        // 1. 构建属性对象
        Properties prop = new Properties();
        // 2. 添加相关属性
        prop.put("group.id", groupId); // 指定分组id
        prop.put("zookeeper.connect", zookeeper); // 指定zk的连接url
        prop.put("zookeeper.session.timeout.ms", "400"); //
        prop.put("zookeeper.sync.time.ms", "200");
        prop.put("auto.commit.interval.ms", "1000");
        // 3. 构建ConsumerConfig对象
        return new ConsumerConfig(prop);
    }

    /**
     * 关闭连接
     */
    public void shutdown() {
        // 1. 关闭和Kafka的连接，这样会导致stream.hashNext返回false
        if (this.consumer != null) {
            this.consumer.shutdown();
        }

        // 2. 关闭线程池，会等待线程的执行完成
        if (this.executorPool != null) {
            // 2.1 关闭线程池
            this.executorPool.shutdown();

            // 2.2. 等待关闭完成, 等待五秒
            try {
                if (!this.executorPool.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.out.println("Timed out waiting for consumer threads to shut down, exiting uncleanly!!");
                }
            } catch (InterruptedException e) {
                System.out.println("Interrupted during shutdown, exiting uncleanly!!");
            }
        }

    }

    private class CustomThreadFactory implements ThreadFactory {

        private AtomicInteger count = new AtomicInteger(0);

        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            String threadName = "kafka-consumer-" + count.addAndGet(1);
            t.setName(threadName);
            return t;
        }
    }





}
