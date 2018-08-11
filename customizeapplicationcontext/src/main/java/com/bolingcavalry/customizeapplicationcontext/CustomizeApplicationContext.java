package com.bolingcavalry.customizeapplicationcontext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;

/**
 * @Description : 自定义的ApplicationContext实现类
 * @Author : zq2599@gmail.com
 * @Date : 2018-08-11 17:12
 */
public class CustomizeApplicationContext extends AnnotationConfigServletWebServerApplicationContext {

    Log logger = LogFactory.getLog(CustomizeApplicationContext.class);

    @Override
    protected void initPropertySources() {
        super.initPropertySources();
        logger.info("execute override initPropertySources");
    }

    @Override
    protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        super.postProcessBeanFactory(beanFactory);
        logger.info("execute override postProcessBeanFactory");
    }

    @Override
    protected void onRefresh() {
        super.onRefresh();
        logger.info("execute override onRefresh");
    }
}
