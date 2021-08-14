package com.bolingcavalry.simplebean.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class HelloControllerTest {
    private static final String NAME = "Tom";
    @Test
    void hello(@Autowired MockMvc mvc) throws Exception {
        // 模拟
        mvc.perform(get("/" + NAME))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello " + NAME));
    }
}
