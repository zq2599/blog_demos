package com.example.disconf.demo.task;

import com.baidu.disconf.client.usertools.DisconfDataGetter;
import com.example.disconf.demo.config.JedisConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author willzhao
 * @version V1.0
 * @Description: 定时输出最新的配置
 * @email zq2599@gmail.com
 * @Date 17/5/5 下午5:18
 */
@Service
public class DisconfDemoTask {

    protected static final Logger LOGGER = LoggerFactory.getLogger(DisconfDemoTask.class);

    @Autowired
    private JedisConfig jedisConfig;

    private static final String REDIS_KEY = "disconf_key";


    /**
     *
     */
    public int run() {

        try {

            while (true) {

                //
                // service demo
                //
                Thread.sleep(5000);

                LOGGER.info("redis config ( {} , {} )", jedisConfig.getHost(), jedisConfig.getPort());

                // 动态的写法
                LOGGER.info(DisconfDataGetter.getByFile("redis.properties").toString());
            }

        } catch (Exception e) {

            LOGGER.error(e.toString(), e);
        }

        return 0;
    }
}
