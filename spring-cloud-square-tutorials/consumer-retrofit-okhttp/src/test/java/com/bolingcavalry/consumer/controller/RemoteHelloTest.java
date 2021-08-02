package com.bolingcavalry.consumer.controller;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * @Description: 单元测试类
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/8/9 23:55
 */
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
class RemoteHelloTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void hello() throws Exception {
        // 请求参数是用户名，实时生成一个
        String name = String.valueOf(System.currentTimeMillis());

        // 请求
        String responseString = mvc.perform(
                    MockMvcRequestBuilders
                            .get("/remote-obj")
                            .param("name", name)
                            .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())                           // 验证状态
                .andExpect(jsonPath("$.name", is(name)))    // 验证json中返回的字段是否含有name
                .andDo(print())
                .andReturn()
                .getResponse()
                .getContentAsString();

        log.info("response in junit test :\n" + responseString);

    }
}