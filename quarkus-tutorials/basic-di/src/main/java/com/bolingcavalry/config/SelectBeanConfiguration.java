package com.bolingcavalry.config;

import com.bolingcavalry.service.TryLookupIfProperty;
import com.bolingcavalry.service.impl.TryLookupIfPropertyAlpha;
import com.bolingcavalry.service.impl.TryLookupIfPropertyBeta;
import io.quarkus.arc.lookup.LookupIfProperty;
import javax.enterprise.context.ApplicationScoped;

public class SelectBeanConfiguration {

    @LookupIfProperty(name = "service.alpha.enabled", stringValue = "true")
    @ApplicationScoped
    public TryLookupIfProperty tryLookupIfPropertyAlpha() {
        return new TryLookupIfPropertyAlpha();
    }

    @LookupIfProperty(name = "service.beta.enabled", stringValue = "true")
    @ApplicationScoped
    public TryLookupIfProperty tryLookupIfPropertyBeta() {
        return new TryLookupIfPropertyBeta();
    }
}
