package com.bolingcavalry.config;

import com.bolingcavalry.service.TryIfBuildProfile;
import com.bolingcavalry.service.TryLookupIfProperty;
import com.bolingcavalry.service.impl.*;
import io.quarkus.arc.DefaultBean;
import io.quarkus.arc.lookup.LookupIfProperty;
import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.logging.Log;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;

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

    @Produces
    @IfBuildProfile("test")
    public TryIfBuildProfile tryIfBuildProfileProd() {
        return new TryIfBuildProfileProd();
    }

    @Produces
    @DefaultBean
    public TryIfBuildProfile tryIfBuildProfileDefault() {
        return new TryIfBuildProfileDefault();
    }

    @RequestScoped
    public ResourceManager getResourceManager() {
        return new ResourceManager();
    }

    /**
     * 使用了Disposes注解后，ResourceManager类型的bean在销毁前，此方法都会执行
     * @param resourceManager
     */
    public void closeResource(@Disposes ResourceManager resourceManager) {
        // 在这里可以做一些额外的操作，不需要bean参与
        Log.info("do other things that bean do not care");

        // 也可以执行bean的方法
        resourceManager.closeAll();
    }
}
