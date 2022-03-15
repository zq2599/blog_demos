package com.bolingcavalry.service.impl;

import com.bolingcavalry.service.TryLookupIfProperty;

public class TryLookupIfPropertyBeta implements TryLookupIfProperty {
    @Override
    public String hello() {
        return "from " + this.getClass().getSimpleName();
    }
}
