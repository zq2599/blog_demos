package com.bolingcavalry.advanced.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Description: 嵌套测试演示
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/10/7 11:50
 */
@SpringBootTest
@Slf4j
@DisplayName("嵌套测试演示")
public class NestedTest {

    @Nested
    @DisplayName("查找服务相关的测试")
    class FindService {
        @Test
        void findByIdTest() {}
        @Test
        void findByNameTest() {}
    }

    @Nested
    @DisplayName("删除服务相关的测试")
    class DeleteService {
        @Test
        void deleteByIdTest() {}
        @Test
        void deleteByNameTest() {}
    }
}
