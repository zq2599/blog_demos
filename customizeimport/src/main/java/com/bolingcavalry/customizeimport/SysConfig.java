package com.bolingcavalry.customizeimport;

import com.bolingcavalry.customizeimport.registrar.CustomizeImportBeanDefinitionRegistrar;
import com.bolingcavalry.customizeimport.selector.CustomizeDeferredImportSelector;
import com.bolingcavalry.customizeimport.selector.CustomizeImportSelector;
import com.bolingcavalry.customizeimport.service.impl.CustomizeServiceImpl1;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @Description: 系统配置类
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2018/9/9 13:42
 */
@Configuration
@Import({CustomizeServiceImpl1.class,
        CustomizeImportSelector.class,
        CustomizeDeferredImportSelector.class,
        CustomizeImportBeanDefinitionRegistrar.class})
public class SysConfig {
}
