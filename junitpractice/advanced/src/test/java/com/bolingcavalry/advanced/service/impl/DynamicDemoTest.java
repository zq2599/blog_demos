package com.bolingcavalry.advanced.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

@SpringBootTest
@Slf4j
class DynamicDemoTest {

    @TestFactory
    Iterable<org.junit.jupiter.api.DynamicTest> testFactoryTest() {

        DynamicTest firstTest = dynamicTest(
            "一号动态测试用例",
            () -> {
                log.info("一号用例，这里编写单元测试逻辑代码");
            }
        );

        DynamicTest secondTest = dynamicTest(
                "二号动态测试用例",
                () -> {
                    log.info("二号用例，这里编写单元测试逻辑代码");
                }
        );


        return Arrays.asList(firstTest, secondTest);
    }




}