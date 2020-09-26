package com.bolingcavalry.simplebean.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class HelloControllerWithWebTestClientTest {

    private static final String NAME = "Tom";

    @Test
    void hello(@Autowired WebTestClient webClient, @LocalServerPort int port) throws Exception {

        webClient
                .get()
                .uri("/" + NAME)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .isEqualTo("Hello " + NAME);

        log.info("web端口是[{}]", port);
    }
}
