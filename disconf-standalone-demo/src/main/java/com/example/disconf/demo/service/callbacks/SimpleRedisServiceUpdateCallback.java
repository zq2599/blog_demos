package com.example.disconf.demo.service.callbacks;

import com.baidu.disconf.client.common.annotations.DisconfUpdateService;
import com.baidu.disconf.client.common.update.IDisconfUpdate;
import com.example.disconf.demo.config.JedisConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * @author willzhao
 * @version V1.0
 * @Description: 更新配置时的回调函数
 * @email zq2599@gmail.com
 * @Date 17/5/5 下午5:18
 */
@Service
@Scope("singleton")
@DisconfUpdateService(classes = {JedisConfig.class})
public class SimpleRedisServiceUpdateCallback implements IDisconfUpdate {

    protected static final Logger LOGGER = LoggerFactory.getLogger(SimpleRedisServiceUpdateCallback.class);

    @Autowired
    private JedisConfig jedisConfig;

    /**
     *
     */
    public void reload() throws Exception {
        LOGGER.info("**********************************************[reload interface is invoked]---[" + jedisConfig.getHost() + ":" + jedisConfig.getPort() + "]");
    }

}
