package com.bolingcavalry.junit5experience.service.impl;

import com.bolingcavalry.junit5experience.service.HelloService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.concurrent.TimeUnit;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * @Description: 演示Junit5的各类注解
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/9/26 19:07
 */
@SpringBootTest
@Slf4j
class HelloServiceImplTest {

    private static final String NAME = "Tom";

    @Autowired
    HelloService helloService;

    @BeforeAll
    static void beforeAll() {
        log.info("execute beforeAll");
    }

    @AfterAll
    static void afterAll() {
        log.info("execute afterAll");
    }

    /**
     * 每个测试方法执行前都会执行一次
     */
    @BeforeEach
    void beforeEach() {
        log.info("execute beforeEach");
    }

    /**
     * 每个测试方法执行后都会执行一次
     */
    @AfterEach
    void afterEach() {
        log.info("execute afterEach");
    }

    @Test
    @DisplayName("测试service层的hello方法")
    @Tag("string-result")
    void hello() {
        log.info("execute hello");
        assertThat(helloService.hello(NAME)).isEqualTo("Hello " + NAME);
    }

    @Test
    @DisplayName("测试service层的increase方法\uD83D\uDE31")
    @Tag("int-result")
    void increase() {
        log.info("execute increase");
        assertThat(helloService.increase(1)).isEqualByComparingTo(2);
    }

    @Test
    @Disabled
    void neverExecute() {
        log.info("execute neverExecute");
    }

    /**
     * 调用一个耗时1秒的方法，用Timeout设置超时时间是500毫秒，
     * 因此该用例会测试失败
     */
    @Test
    @Timeout(unit = TimeUnit.MILLISECONDS, value = 500)
    @Disabled
    void remoteRequest() {
        assertThat(helloService.remoteRequest()).isEqualTo(true);
    }

    @Test
    void abc() {
        assumeTrue(true, () -> "skip abc");

        assertThat(helloService.remoteRequest()).isEqualTo(false);
    }


}