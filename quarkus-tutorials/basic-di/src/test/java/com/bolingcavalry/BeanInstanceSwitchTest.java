package com.bolingcavalry;

import com.bolingcavalry.service.TryIfBuildProfile;
import com.bolingcavalry.service.TryLookupIfProperty;
import com.bolingcavalry.service.impl.TryIfBuildProfileProd;
import com.bolingcavalry.service.impl.TryLookupIfPropertyAlpha;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2022/3/15 8:37 PM
 * @description 功能介绍
 */
@QuarkusTest
public class BeanInstanceSwitchTest {

    @BeforeAll
    public static void setUp() {
        System.setProperty("service.alpha.enabled", "true");
    }

    @Inject
    Instance<TryLookupIfProperty> service;

    @Inject
    Instance<TryIfBuildProfile> tryIfBuildProfile;

    @Test
    public void testTryLookupIfProperty() {
        Assertions.assertEquals("from " + TryLookupIfPropertyAlpha.class.getSimpleName(),
                                service.get().hello());
    }

    @Test
    public void tryIfBuildProfile() {
        Assertions.assertEquals("from " + TryIfBuildProfileProd.class.getSimpleName(),
                tryIfBuildProfile.get().hello());
    }
}
