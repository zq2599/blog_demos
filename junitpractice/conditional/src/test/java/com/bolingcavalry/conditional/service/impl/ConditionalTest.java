package com.bolingcavalry.conditional.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @Description: (这里用一句话描述这个类的作用)
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date:  2020/10/2 22:18
 */
@SpringBootTest
@Slf4j
public class ConditionalTest {

    @Test
    @EnabledOnOs(OS.WINDOWS)
    @DisplayName("只有windows操作系统才会执行的用例")
    void onlyWindowsTest() {
        assertEquals(2, Math.addExact(1, 1));
    }

    @Test
    @EnabledOnOs({OS.WINDOWS, OS.LINUX})
    @DisplayName("windows和linux操作系统都会执行的用例")
    void windowsORLinuxTest() {
        assertEquals(2, Math.addExact(1, 1));
    }

    @Test
    @EnabledOnOs({OS.MAC})
    @DisplayName("只有MAC操作系统才会执行的用例")
    void onlyMacTest() {
        assertEquals(2, Math.addExact(1, 1));
    }
}