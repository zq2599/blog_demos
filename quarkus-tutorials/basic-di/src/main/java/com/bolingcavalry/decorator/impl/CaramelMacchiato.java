package com.bolingcavalry.decorator.impl;

import com.bolingcavalry.decorator.Coffee;
import io.quarkus.arc.Priority;
import io.quarkus.logging.Log;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

/**
 * 焦糖玛奇朵：拿铁+焦糖
 */
@Decorator
@Priority(10)
public class CaramelMacchiato implements Coffee {

    /**
     * 焦糖价格：1美元
     */
    private static final int CARAMEL_PRICE = 1;

    @Delegate
    @Inject
    Coffee delegate;

    @Override
    public String name() {
        return "CaramelMacchiato";
    }

    @Override
    public int getPrice() {
        // 将CaramelMacchiato的代理类打印出来，看quarkus注入的是否正确
        Log.infov("CaramelMacchiato's delegate type : " + this.delegate.name());
        return delegate.getPrice() + CARAMEL_PRICE;
    }
}
