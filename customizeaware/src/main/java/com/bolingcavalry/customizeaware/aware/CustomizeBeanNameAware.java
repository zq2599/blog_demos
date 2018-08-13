package com.bolingcavalry.customizeaware.aware;

import com.bolingcavalry.customizeaware.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.stereotype.Service;

/**
 * @Description :
 * @Author : zq2599@gmail.com
 * @Date : 2018-08-13 18:55
 */
@Service
public class CustomizeBeanNameAware implements BeanNameAware {
    private String beanName;

    @Override
    public void setBeanName(String beanName) {
        Utils.printTrack("beanName is set to " + beanName);
        this.beanName = beanName;
    }

    public String getBeanName() {
        return this.beanName;
    }
}
