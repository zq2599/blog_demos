package com.bolingcavalry.gateway;


import com.bolingcavalry.common.Constants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author willzhao
 * @version 1.0
 * @description TODO
 * @date 2021/8/10 6:40
 */
@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureWebTestClient
public class HelloTest {

    @Autowired
    private WebTestClient webClient;

    @Test
    void testLoadBalance() {
        webClient.get()
                .uri("/lbtest/str")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                // 验证状态
                .expectStatus().isOk()
                // 验证结果，注意结果是字符串格式
                .expectBody(String.class).consumeWith(result  -> assertTrue(result.getResponseBody().contains(Constants.LB_PREFIX)));
    }
}
