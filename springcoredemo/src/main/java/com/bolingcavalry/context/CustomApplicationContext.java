package com.bolingcavalry.context;

import org.springframework.beans.BeansException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class CustomApplicationContext extends ClassPathXmlApplicationContext {

    public CustomApplicationContext(String configLocation) throws BeansException {
        super(configLocation);
    }

    @Override
    protected void initPropertySources() {
        super.initPropertySources();

        getEnvironment().setRequiredProperties("REDIS_HOST1");
    }
}
