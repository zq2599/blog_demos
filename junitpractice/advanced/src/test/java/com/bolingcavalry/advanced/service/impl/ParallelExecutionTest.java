package com.bolingcavalry.advanced.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ParallelExecutionTest {

    @Order(1)
    @Execution(ExecutionMode.SAME_THREAD)
    @DisplayName("单线程执行10次")
    @RepeatedTest(value = 10, name="完成度：{currentRepetition}/{totalRepetitions}")
    void sameThreadTest(TestInfo testInfo, RepetitionInfo repetitionInfo) {
        log.info("测试方法 [{}]，当前第[{}]次，共[{}]次",
                testInfo.getTestMethod().get().getName(),
                repetitionInfo.getCurrentRepetition(),
                repetitionInfo.getTotalRepetitions());
    }

    @Order(2)
    @Execution(ExecutionMode.CONCURRENT)
    @DisplayName("多线程执行10次")
    @RepeatedTest(value = 10, name="完成度：{currentRepetition}/{totalRepetitions}")
    void concurrentTest(TestInfo testInfo, RepetitionInfo repetitionInfo) {
        log.info("测试方法 [{}]，当前第[{}]次，共[{}]次",
                testInfo.getTestMethod().get().getName(),
                repetitionInfo.getCurrentRepetition(),
                repetitionInfo.getTotalRepetitions());
    }


    @Order(3)
    @Execution(ExecutionMode.CONCURRENT)
    @DisplayName("多个int型入参")
    @ParameterizedTest
    @ValueSource(ints = { 1,2,3,4,5,6,7,8,9,0 })
    void intsTest(int candidate) {
        log.info("ints [{}]", candidate);
    }
}