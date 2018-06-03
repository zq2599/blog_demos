package com.bolingcavalry.messagettlproducer.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description : 第一类延时消息(在每条消息中指定过期时间)的配置
 * @Author : zq2599@gmail.com
 * @Date : 2018-06-02 22:59
 */
@Configuration
public class MessageTtlRabbitConfig {

    /**
     * 成为死信后，重新发送到的交换机的名称
     */
    @Value("${message.ttl.exchange}")
    private String MESSAGE_TTL_EXCHANGE_NAME;

    /**
     * 不会被消费的队列，投递到此队列的消息会成为死信
     */
    @Value("${message.ttl.queue.source}")
    private String MESSAGE_TTL_QUEUE_SOURCE;

    /**
     * 该队列被绑定到接收死信的交换机
     */
    @Value("${message.ttl.queue.process}")
    private String MESSAGE_TTL_QUEUE_PROCESS;

    /**
     * 配置一个队列，该队列的消息如果没有被消费，就会投递到死信交换机中，并且带上指定的routekey
     * @return
     */
    @Bean
    Queue messageTtlQueueSource() {
        return QueueBuilder.durable(MESSAGE_TTL_QUEUE_SOURCE)
                .withArgument("x-dead-letter-exchange", MESSAGE_TTL_EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", MESSAGE_TTL_QUEUE_PROCESS)
                .build();
    }

    @Bean("messageTtlQueueProcess")
    Queue messageTtlQueueProcess() {
        return QueueBuilder.durable(MESSAGE_TTL_QUEUE_PROCESS) .build();
    }

    @Bean("messageTtlExchange")
    DirectExchange messageTtlExchange() {
        return new DirectExchange(MESSAGE_TTL_EXCHANGE_NAME);
    }

    /**
     * 绑定指定的队列到死信交换机上
     * @param messageTtlQueueProcess
     * @param messageTtlExchange
     * @return
     */
    @Bean
    Binding bindingExchangeMessage(@Qualifier("messageTtlQueueProcess") Queue messageTtlQueueProcess, @Qualifier("messageTtlExchange") DirectExchange messageTtlExchange) {
        System.out.println("11111111111111111111111111111111111111111111111111");
        System.out.println("11111111111111111111111111111111111111111111111111");
        System.out.println("11111111111111111111111111111111111111111111111111");
        System.out.println("11111111111111111111111111111111111111111111111111");
        return BindingBuilder.bind(messageTtlQueueProcess)
                .to(messageTtlExchange)
                .with(MESSAGE_TTL_QUEUE_PROCESS);
    }
}
