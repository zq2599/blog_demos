package com.bolingcavalry.interceptor.demo;

import com.bolingcavalry.interceptor.define.HandleConstruction;
import com.bolingcavalry.interceptor.define.HandleError;
import com.bolingcavalry.interceptor.define.HandleMethod;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2022/3/26 23:52
 * @description 模拟业务逻辑中的异常抛出，这里方法中直接抛出一个异常
 */
@ApplicationScoped
@HandleConstruction
@HandleError
public class ArroundInvokeDemo {

    @Inject
    Logger logger;
//
//    public ArroundInvokeDemo() {
//        super();
////        logger.infov("construction of %s", ArroundInvokeDemo.class.getSimpleName());
//    }

    @HandleMethod
    public void executeNormal() {
        logger.info("start executeNormal");
    }

    public void executeThrowError() {
        throw new IllegalArgumentException("this is business logic exception");
    }
}
