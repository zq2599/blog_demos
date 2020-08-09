package com.bolingcavalry.curd.controller;

import org.junit.Ignore;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * @Description: (这里用一句话描述这个类的作用)
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/8/9 23:55
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    @Order(2)
    void insertWithFields() throws Exception {
        String responseString = mvc.perform(MockMvcRequestBuilders.get("/user/insertwithfields/Tom/11").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Tom")))
                .andDo(print())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    @Test
    @Order(3)
    @Ignore
    void getUser() throws Exception {

    }

    @Test
    @Order(4)
    void insertBatch() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/user/insertwithfields/Prefix/6").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @Order(1)
    void init() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/user/clearall").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @Order(5)
    void clearAll() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/user/clearall").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}