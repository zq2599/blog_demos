package com.bolingcavalry.tag.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @Description: tag的demo
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date:  2020/10/2 22:18
 */
@SpringBootTest
@Slf4j
@Tag("second")
public class SecondTest {

    @Test
    @Tag("easy")
    @DisplayName("second-1")
    void second1Test() {
        log.info("second1Test");
        assertEquals(2, Math.addExact(1, 1));
    }

    @Test
    @Tag("easy")
    @DisplayName("second-2")
    void second2Test() {
        log.info("second2Test");
        assertEquals(2, Math.addExact(1, 1));
    }

    @Test
    @Tag("hard")
    @Tag("important")
    @DisplayName("second-3")
    void second3Test() {
        log.info("second3Test");
        assertEquals(2, Math.addExact(1, 1));
    }


}