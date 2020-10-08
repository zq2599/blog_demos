package com.bolingcavalry.relatedoperation.controller;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * @Description: 单元测试类
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/8/9 23:55
 */
@SpringBootTest
@DisplayName("Web接口的单元测试")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Slf4j
public class ControllerTest {

    final static int TEST_USER_ID = 3;

    @Autowired MockMvc mvc;

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @DisplayName("用户服务")
    class User {

        /**
         * 通过用户ID获取用户信息有两种方式：left join和嵌套查询，
         * 从客户端来看，仅一部分path不同，因此将请求和检查封装到一个通用方法中，
         * 调用方法只需要指定不同的那一段path
         * @param subPath
         * @throws Exception
         */
        private void queryAndCheck(String subPath) throws Exception {
            String queryPath = "/user/" + subPath + "/" + TEST_USER_ID;

            log.info("query path [{}]", queryPath);

            mvc.perform(MockMvcRequestBuilders.get(queryPath).accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(TEST_USER_ID))
                    .andExpect(jsonPath("$..logs.length()").value(5))
                    .andDo(print());
        }


        @Test
        @DisplayName("通过用户ID获取用户信息(包含行为日志)，通过left join实现")
        @Order(1)
        void selUserWithLogsLeftJoin() throws Exception {
            queryAndCheck("leftjoin");
        }

        @Test
        @DisplayName("通过用户ID获取用户信息(包含行为日志)，通过嵌套查询实现")
        @Order(2)
        void selUserWithLogsNestedSelect() throws Exception {
            queryAndCheck("nested");
        }
    }

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @DisplayName("日志服务")
    class Log {

        final static int TEST_LOG_ID = 5;

        @Test
        @DisplayName("通过日志ID获取日志信息，关联了用户")
        @Order(1)
        void selTest() throws Exception {
            mvc.perform(MockMvcRequestBuilders.get("/log/" + TEST_LOG_ID)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(TEST_LOG_ID))
                    .andExpect(jsonPath("$.user.id").value(TEST_USER_ID))
                    .andDo(print());
        }
    }



}