package com.bolingcavalry.customizebeandefinitionregistrypostprocessor.registrypostprocessor;

import com.bolingcavalry.customizebeandefinitionregistrypostprocessor.service.impl.CalculateServiceImpl;
import com.bolingcavalry.customizebeandefinitionregistrypostprocessor.util.Utils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.stereotype.Component;

/**
 * @Description : 自定义BeanDefinitionRegistryPostProcessor实现类
 * @Author : zq2599@gamil.com
 * @Date : 2018-08-30 14:06
 */
@Component
public class CustomizeBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        Utils.printTrack("execute postProcessBeanDefinitionRegistry");

        RootBeanDefinition helloBean = new RootBeanDefinition(CalculateServiceImpl.class);

        //bean的定义注册到spring环境
        beanDefinitionRegistry.registerBeanDefinition("calculateService", helloBean);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        Utils.printTrack("execute postProcessBeanFactory");
    }
}
