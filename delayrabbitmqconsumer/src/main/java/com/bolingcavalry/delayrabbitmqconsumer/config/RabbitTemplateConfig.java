package com.bolingcavalry.delayrabbitmqconsumer.config;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * @Description : RabbitMQ连接的配置
 * @Author : zq2599@gmail.com
 * @Date : 2018-05-06 15:24
 */
@Configuration
public class RabbitTemplateConfig {

    @Value("${mq.rabbit.address}")
    String address;

    @Value("${mq.rabbit.username}")
    String username;

    @Value("${mq.rabbit.password}")
    String password;

    @Value("${mq.rabbit.virtualHost}")
    String mqRabbitVirtualHost;

    //创建mq连接
    @Bean(name = "connectionFactory")
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();

        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setVirtualHost(mqRabbitVirtualHost);
        connectionFactory.setPublisherConfirms(true);

        //该方法配置多个host，在当前连接host down掉的时候会自动去重连后面的host
        connectionFactory.setAddresses(address);

        return connectionFactory;
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    //必须是prototype类型
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate template = new RabbitTemplate(connectionFactory());
        return template;
    }
}