package com.bolingcavalry;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Description：解析配置，放入入参中保存
 * @author willzhao
 * @email zq2599@gmail.com
 * @date 2017/7/1 22:14
 */
public class ComputerBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {
    @Override
    protected Class<?> getBeanClass(Element element) {
        return Computer.class;
    }

    @Override
    protected void doParse(Element element, BeanDefinitionBuilder builder) {
        String os = element.getAttribute("os");
        String ramStr = element.getAttribute("ram");

        if(StringUtils.hasText(os)){
            builder.addPropertyValue("os", os);
        }
        if(StringUtils.hasText(ramStr)){
            builder.addPropertyValue("ram", Integer.valueOf(ramStr));
        }
    }
}
