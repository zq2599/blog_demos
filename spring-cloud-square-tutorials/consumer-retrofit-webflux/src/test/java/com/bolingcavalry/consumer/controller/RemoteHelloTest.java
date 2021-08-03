package com.bolingcavalry.consumer.controller;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

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

//    @Autowired
    private MockMvc mvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    public void setUp() {
        // 在单元测试的时候，MockHttpServletResponse实例的characterEncoding默认是ISO-8859-1，
        // 得到的字符串打印出来也是乱码，
        // 下面的设置可以解决此问题
        if (null==mvc) {
            mvc = MockMvcBuilders
                    .webAppContextSetup(webApplicationContext)
                    .addFilter((request, response, chain) -> {
                        response.setCharacterEncoding("UTF-8"); // this is crucial
                        chain.doFilter(request, response);
                    }, "/*")
                    .build();
        }
    }

    @Test
    void hello() throws Exception {
        // 请求参数是用户名，实时生成一个
        String name = System.currentTimeMillis() + "程序员B";

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