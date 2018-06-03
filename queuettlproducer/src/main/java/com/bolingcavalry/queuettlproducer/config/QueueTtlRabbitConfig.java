package com.bolingcavalry.queuettlproducer.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description : 第二类延时消息(给队列指定过期时间)的配置
 * @Author : zq2599@gmail.com
 * @Date : 2018-06-03 11:35
 */
@Configuration
public class QueueTtlRabbitConfig {

    /**
     * 成为死信后，重新发送到的交换机的名称
     */
    @Value("${queue.ttl.exchange}")
    private String QUEUE_TTL_EXCHANGE_NAME;

    /**
     * 不会被消费的队列，投递到此队列的消息会成为死信
     */
    @Value("${queue.ttl.queue.source}")
    private String QUEUE_TTL_QUEUE_SOURCE;

    /**
     * 该队列被绑定到接收死信的交换机
     */
    @Value("${queue.ttl.queue.process}")
    private String QUEUE_TTL_QUEUE_PROCESS;

    @Value("${queue.ttl.value}")
    private long QUEUE_TTL_VALUE;

    /**
     * 配置一个队列，该队列有消息过期时间，消息如果没有被消费，就会投递到死信交换机中，并且带上指定的routekey
     * @return
     */
    @Bean
    Queue queueTtlQueueSource() {
        return QueueBuilder.durable(QUEUE_TTL_QUEUE_SOURCE)
                .withArgument("x-dead-letter-exchange", QUEUE_TTL_EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", QUEUE_TTL_QUEUE_PROCESS)
                .withArgument("x-message-ttl", QUEUE_TTL_VALUE)
                .build();
    }

    @Bean("queueTtlQueueProcess")
    Queue queueTtlQueueProcess() {
        return QueueBuilder.durable(QUEUE_TTL_QUEUE_PROCESS) .build();
    }

    @Bean("queueTtlExchange")
    DirectExchange queueTtlExchange() {
        return new DirectExchange(QUEUE_TTL_EXCHANGE_NAME);
    }

    /**
     * 绑定
     * @param queueTtlQueueProcess
     * @param queueTtlExchange
     * @return
     */
    @Bean
    Binding bindingExchangeMessage(@Qualifier("queueTtlQueueProcess") Queue queueTtlQueueProcess, @Qualifier("queueTtlExchange") DirectExchange queueTtlExchange) {
        System.out.println("22222222222222222222222222222222222222222222222222");
        System.out.println("22222222222222222222222222222222222222222222222222");
        System.out.println("22222222222222222222222222222222222222222222222222");
        System.out.println("22222222222222222222222222222222222222222222222222");
        return BindingBuilder.bind(queueTtlQueueProcess)
                .to(queueTtlExchange)
                .with(QUEUE_TTL_QUEUE_PROCESS);
    }
}
