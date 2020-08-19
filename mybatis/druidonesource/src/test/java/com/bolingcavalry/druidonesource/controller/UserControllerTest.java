package com.bolingcavalry.druidonesource.controller;

import com.bolingcavalry.druidonesource.entity.User;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * @Description: 单元测试类
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/8/9 23:55
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    // user表的name字段，这里为了保证测试时新增和删除的记录是同一条，用UUID作为用户名
    static String testName;

    @BeforeAll
    static void init() {
        testName = UUID.randomUUID().toString().replaceAll("-","");
    }

    @Test
    @Order(1)
    void insertWithFields() throws Exception {
        String jsonStr = "{\"name\": \"" + testName + "\", \"age\": 10}";

        mvc.perform(
                MockMvcRequestBuilders.put("/user/insertwithfields")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonStr)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(testName)))
                .andDo(print())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    @Test
    @Order(2)
    void findByName() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/user/findbyname/"+ testName).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andDo(print());
    }


    @Test
    @Order(3)
    void delete() throws Exception {
        // 先根据名称查出记录
        String responseString = mvc.perform(MockMvcRequestBuilders.get("/user/findbyname/"+ testName).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andDo(print())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 反序列化得到数组
        JsonArray jsonArray = JsonParser.parseString(responseString).getAsJsonArray();

        // 反序列化得到user实例
        User user = new Gson().fromJson(jsonArray.get(0), User.class);

        // 执行删除
        mvc.perform(MockMvcRequestBuilders.delete("/user/"+ user.getId()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("1")))
                .andDo(print());
    }
}