package com.bolingcavalry.simplebean.service;

import junit.framework.TestCase;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
/**
 * @Description: (这里用一句话描述这个类的作用)
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/9/20 23:41
 */
@SpringBootTest
//@AutoConfigureMockMvc
class DemoTest {

    private static final Logger logger = LoggerFactory.getLogger(DemoTest.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    HelloService helloService;

    @Test
    void hello(@Autowired MockMvc mvc) throws Exception {
        String name = "tom";
        mvc.perform(get("/" + name)).andExpect(status().isOk()).andExpect(content().string("Hello " + name));
        TestCase.assertNotNull(helloService);
        TestCase.assertEquals("Hello " + name, helloService.hello(name));
    }

}