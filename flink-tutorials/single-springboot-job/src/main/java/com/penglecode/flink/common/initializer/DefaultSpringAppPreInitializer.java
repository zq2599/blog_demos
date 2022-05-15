package com.penglecode.flink.common.initializer;

import com.penglecode.flink.common.util.SpringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 默认的SpringBoot程序前置初始化程序
 *
 * @author pengpeng
 * @version 1.0
 * @since 2021/11/29 22:20
 */
public class DefaultSpringAppPreInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSpringAppPreInitializer.class);

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        LOGGER.info(">>> Spring 应用启动前置初始化程序! applicationContext = {}", applicationContext);
        SpringUtils.setApplicationContext(applicationContext);
        SpringUtils.setEnvironment(applicationContext.getEnvironment());
    }
}
