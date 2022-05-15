package com.penglecode.flink.examples;


import org.springframework.beans.BeansException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

/**
 * Flink示例基类
 *
 * @author pengpeng
 * @version 1.0
 * @since 2021/11/29 23:25
 */
public abstract class FlinkExample implements ApplicationContextAware, EnvironmentAware {

    private ConfigurableEnvironment environment;

    private ConfigurableApplicationContext applicationContext;

    public abstract void run(ApplicationArguments args) throws Exception;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (ConfigurableEnvironment) environment;
    }

    protected ConfigurableEnvironment getEnvironment() {
        return environment;
    }

    protected ConfigurableApplicationContext getApplicationContext() {
        return applicationContext;
    }

}
