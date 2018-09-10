package com.bolingcavalry.customizeimport.registrar;

import com.bolingcavalry.customizeimport.service.impl.CustomizeServiceImpl4;
import com.bolingcavalry.customizeimport.util.Utils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @Description: 自定义ImportBeanDefinitionRegistrar的实现类
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2018/9/10 6:35
 */
public class CustomizeImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    private final static String BEAN_NAME = "customizeService4";

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        if (!registry.containsBeanDefinition(BEAN_NAME)) {
            Utils.printTrack("start registerBeanDefinitions");
            GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
            beanDefinition.setBeanClass(CustomizeServiceImpl4.class);
            beanDefinition.setSynthetic(true);
            registry.registerBeanDefinition(BEAN_NAME, beanDefinition);
        }
    }
}
