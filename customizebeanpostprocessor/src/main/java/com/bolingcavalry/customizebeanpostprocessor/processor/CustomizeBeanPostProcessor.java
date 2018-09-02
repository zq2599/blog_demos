package com.bolingcavalry.customizebeanpostprocessor.processor;

import com.bolingcavalry.customizebeanpostprocessor.service.CalculateService;
import com.bolingcavalry.customizebeanpostprocessor.util.Utils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class CustomizeBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if("calculateService".equals(beanName)) {
            Utils.printTrack("do postProcess before initialization");
            CalculateService calculateService = (CalculateService)bean;
            calculateService.setServiceDesc("desc from " + this.getClass().getSimpleName());
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if("calculateService".equals(beanName)) {
            Utils.printTrack("do postProcess after initialization");
        }
        return bean;
    }
}
