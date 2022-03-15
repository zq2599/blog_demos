package com.bolingcavalry;

import com.bolingcavalry.service.TryLookupIfProperty;
import com.bolingcavalry.service.impl.TryLookupIfPropertyAlpha;
import com.bolingcavalry.service.impl.TryLookupIfPropertyBeta;
import com.bolingcavalry.service.impl.TryLookupIfPropertyDefault;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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
        System.setProperty("service.beta.enabled", "false");
    }

    @Inject
    Instance<TryLookupIfProperty> service;

    @Test
    public void testTryLookupIfProperty() {
        Assertions.assertEquals("from " + TryLookupIfPropertyDefault.class.getSimpleName(),
                                service.get().hello());
    }
}
