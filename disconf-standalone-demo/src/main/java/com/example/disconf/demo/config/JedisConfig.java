package com.example.disconf.demo.config;

import com.baidu.disconf.client.common.annotations.DisconfFile;
import com.baidu.disconf.client.common.annotations.DisconfFileItem;
import com.baidu.disconf.client.common.annotations.DisconfUpdateService;
import com.baidu.disconf.client.common.update.IDisconfUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * @author willzhao
 * @version V1.0
 * @Description: demo用的配置信息
 * @email zq2599@gmail.com
 * @Date 17/5/5 下午5:18
 */
@Service
@Scope("singleton")
@DisconfFile(filename = "redis.properties")
@DisconfUpdateService(classes = {JedisConfig.class})
public class JedisConfig implements IDisconfUpdate {

    protected static final Logger LOGGER = LoggerFactory.getLogger(JedisConfig.class);

    // 代表连接地址
    private String host;

    // 代表连接port
    private int port;

    /**
     * 地址, 分布式文件配置
     *
     * @return
     */
    @DisconfFileItem(name = "redis.host", associateField = "host")
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    /**
     * 端口, 分布式文件配置
     *
     * @return
     */
    @DisconfFileItem(name = "redis.port", associateField = "port")
    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
        LOGGER.info("i' m here: setting redis port");
    }

    public void reload() throws Exception {
        LOGGER.info("host: " + host);
    }
}
