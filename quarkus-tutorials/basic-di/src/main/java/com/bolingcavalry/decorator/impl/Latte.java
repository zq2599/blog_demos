package com.bolingcavalry.decorator.impl;

import com.bolingcavalry.decorator.Coffee;
import io.quarkus.arc.Priority;
import io.quarkus.logging.Log;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

@Decorator
@Priority(11)
public class Latte implements Coffee {
    /**
     * 牛奶价格：2美元
     */
    private static final int MILK_PRICE = 2;

    @Delegate
    @Inject
    Coffee delegate;

    @Override
    public String name() {
        return "Latte";
    }

    @Override
    public int getPrice() {
        // 将Latte的代理类打印出来，看quarkus注入的是否正确
        Log.info("Latte's delegate type : " + this.delegate.name());
        return delegate.getPrice() + MILK_PRICE;
    }
}
