package com.bolingcavalry;

import com.bolingcavalry.service.impl.ResourceManager;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@QuarkusTest
public class DisposeTest {

    @Inject
    ResourceManager resourceManager;

    @Test
    public void test() {
        resourceManager.open();
    }
}
