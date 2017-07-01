package com.bolingcavalry;


import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Description：负责将名称为computer的schema交给指定的解析器处理
 * @author willzhao
 * @email zq2599@gmail.com
 * @date 2017/7/1 22:04
 */
public class ComputerNamespaceHandler extends NamespaceHandlerSupport {
    public void init() {
        registerBeanDefinitionParser("computer", new ComputerBeanDefinitionParser());
    }
}
