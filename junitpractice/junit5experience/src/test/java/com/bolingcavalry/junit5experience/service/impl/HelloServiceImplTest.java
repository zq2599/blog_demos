package com.bolingcavalry.junit5experience.service.impl;

import com.bolingcavalry.junit5experience.service.HelloService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.concurrent.TimeUnit;
import static org.assertj.core.api.Assertions.assertThat;

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

    /**
     * 在所有测试方法执行前被执行
     */
    @BeforeAll
    static void beforeAll() {
        log.info("execute beforeAll");
    }

    /**
     * 在所有测试方法执行后被执行
     */
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
    void hello() {
        log.info("execute hello");
        assertThat(helloService.hello(NAME)).isEqualTo("Hello " + NAME);
    }

    /**
     * DisplayName中带有emoji，在测试框架中能够展示
     */
    @Test
    @DisplayName("测试service层的increase方法\uD83D\uDE31")
    void increase() {
        log.info("execute increase");
        assertThat(helloService.increase(1)).isEqualByComparingTo(2);
    }

    /**
     * 不会被执行的测试方法
     */
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
    void remoteRequest() {
        assertThat(helloService.remoteRequest()).isEqualTo(true);
    }
}