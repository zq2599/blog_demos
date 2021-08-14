package com.bolingcavalry.advanced.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RepeatedDemoTest {

    @Order(1)
    @DisplayName("重复测试")
    @RepeatedTest(5)
    void repeatTest(TestInfo testInfo) {
        log.info("测试方法 [{}]", testInfo.getTestMethod().get().getName());
    }


    @Order(2)
    @DisplayName("重复测试，从入参获取执行情况")
    @RepeatedTest(5)
    void repeatWithParamTest(TestInfo testInfo, RepetitionInfo repetitionInfo) {
        log.info("测试方法 [{}]，当前第[{}]次，共[{}]次",
                testInfo.getTestMethod().get().getName(),
                repetitionInfo.getCurrentRepetition(),
                repetitionInfo.getTotalRepetitions());
    }

    @Order(3)
    @DisplayName("重复测试，使用定制名称")
    @RepeatedTest(value = 5, name="完成度：{currentRepetition}/{totalRepetitions}")
    void repeatWithCustomDisplayNameTest(TestInfo testInfo, RepetitionInfo repetitionInfo) {
        log.info("测试方法 [{}]，当前第[{}]次，共[{}]次",
                testInfo.getTestMethod().get().getName(),
                repetitionInfo.getCurrentRepetition(),
                repetitionInfo.getTotalRepetitions());
    }

}