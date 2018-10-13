package com.bolingcavalry.customizeservicestarter;

import com.bolingcavalry.addservice.service.impl.AddServiceImpl;
import com.bolingcavalry.api.service.AddService;
import com.bolingcavalry.api.service.MinusService;
import com.bolingcavalry.minusservice.service.impl.MinusServiceNotSupportNegativeImpl;
import com.bolingcavalry.minusservice.service.impl.MinusServiceSupportNegativeImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author wilzhao
 * @description 一句话介绍
 * @email zq2599@gmail.com
 * @time 2018/10/13 14:36
 */
@Configuration
public class CustomizeConfiguration {

    @Bean
    public AddService getAddService(){
        System.out.println("create addService");
        return new AddServiceImpl();
    }

    /**
     * 如果配置了com.bolingcavalry.supportnegative=true，
     * 就实例化MinusServiceSupportNegativeImpl
     * @return
     */
    @Bean
    @ConditionalOnProperty(prefix="com.bolingcavalry",name = "supportnegative", havingValue = "true")
    public MinusService getSupportMinusService(){
        System.out.println("create minusService support minus");
        return new MinusServiceSupportNegativeImpl();
    }

    /**
     * 如果没有配置com.bolingcavalry.supportnegative=true，
     * 就不会实例化MinusServiceSupportNegativeImpl，
     * 这里的条件是如果没有MinusService类型的bean，就在此实例化一个
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(MinusService.class)
    public MinusService getNotSupportMinusService(){
        System.out.println("create minusService not support minus");
        return new MinusServiceNotSupportNegativeImpl();
    }
}
