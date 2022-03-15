package com.bolingcavalry.service.impl;

import com.bolingcavalry.service.TryLookupIfProperty;
import io.quarkus.arc.DefaultBean;
import io.quarkus.arc.lookup.LookupIfProperty;
import javax.enterprise.context.ApplicationScoped;

public class SelectBeanConfiguration {

    @LookupIfProperty(name = "service.alpha.enabled", stringValue = "true")
    @ApplicationScoped
    public TryLookupIfProperty tryLookupIfPropertyAlpah() {
        return new TryLookupIfPropertyAlpha();
    }

    @LookupIfProperty(name = "service.beta.enabled", stringValue = "true")
    @ApplicationScoped
    public TryLookupIfProperty tryLookupIfPropertyBeta() {
        return new TryLookupIfPropertyBeta();
    }

    @ApplicationScoped
    @DefaultBean
    public TryLookupIfProperty tryLookupIfPropertyDefault() {
        return new TryLookupIfPropertyDefault();
    }

}
