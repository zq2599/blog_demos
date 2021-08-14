package com.bolingcavalry.gateway.service;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.Executor;

/**
 * @author willzhao
 * @version 1.0
 * @description 监听配置
 * @date 2021/8/14 17:21
 */
@Component
@Slf4j
public class RouteConfigListener {

    private String dataId = "gateway-json-routes";

    private String group = "DEFAULT_GROUP";

    @Value("${spring.cloud.nacos.config.server-addr}")
    private String serverAddr;

    @Autowired
    RouteOperator routeOperator;

    @PostConstruct
    public void dynamicRouteByNacosListener() throws NacosException {

        ConfigService configService = NacosFactory.createConfigService(serverAddr);

        // 添加监听，nacos上的配置变更后会执行
        configService.addListener(dataId, group, new Listener() {

            public void receiveConfigInfo(String configInfo) {
                // 解析和处理都交给RouteOperator完成
                routeOperator.refreshAll(configInfo);
            }

            public Executor getExecutor() {
                return null;
            }
        });

        // 获取当前的配置
        String initConfig = configService.getConfig(dataId, group, 5000);

        // 立即更新
        routeOperator.refreshAll(initConfig);
    }
}
