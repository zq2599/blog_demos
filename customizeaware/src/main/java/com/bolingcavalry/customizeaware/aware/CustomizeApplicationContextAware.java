package com.bolingcavalry.customizeaware.aware;

import com.bolingcavalry.customizeaware.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

/**
 * @Description :
 * @Author : zq2599@gmail.com
 * @Date : 2018-08-13 19:01
 */
@Service
public class CustomizeApplicationContextAware implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Utils.printTrack("applicationContext is set to " + applicationContext);
        this.applicationContext = applicationContext;
    }

    public ApplicationContext getApplicationContext(){
        return this.applicationContext;
    }
}
