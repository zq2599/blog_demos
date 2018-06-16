package com.bolingcavalry.springbootrediskyrodemo.config;


import com.bolingcavalry.springbootrediskyrodemo.Serializer.KryoRedisSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @Description : Redis的配置
 * @Author : zq2599@gmail.com
 * @Date : 2018-05-06 21:39
 */
@Configuration
public class RedisConfig {

    /**
     * redisTemplate 序列化使用的Serializeable, 存储二进制字节码, 所以自定义序列化类
     * @param redisConnectionFactory
     * @return
     */
    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        // redis value使用的序列化器
        template.setValueSerializer(new KryoRedisSerializer<>(Object.class));
        // redis key使用的序列化器
        template.setKeySerializer(new StringRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }
}
