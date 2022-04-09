package com.bolingcavalry.decorator.impl;

import com.bolingcavalry.decorator.Coffee;

import javax.enterprise.context.ApplicationScoped;

/**
 * 意式浓缩咖啡，价格3美元
 */
@ApplicationScoped
public class Espresso implements Coffee {

    @Override
    public String name() {
        return "Espresso";
    }

    @Override
    public int getPrice() {
        return 3;
    }
}
