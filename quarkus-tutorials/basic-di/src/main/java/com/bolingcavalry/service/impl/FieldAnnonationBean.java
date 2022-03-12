package com.bolingcavalry.service.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2022/3/12 4:59 PM
 * @description 功能介绍
 */
public class FieldAnnonationBean {

    @Produces
    @ApplicationScoped
    OtherServiceImpl otherServiceImpl = new OtherServiceImpl();
}
