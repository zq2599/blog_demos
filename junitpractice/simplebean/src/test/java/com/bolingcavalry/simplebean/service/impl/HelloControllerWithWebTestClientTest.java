package com.bolingcavalry.simplebean.service.impl;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HelloControllerWithWebTestClientTest {

    private static final String NAME = "Tom";

    private static final Logger logger = LoggerFactory.getLogger(HelloControllerWithWebTestClientTest.class);

    @LocalServerPort
    private int port;

    @Test
    void hello(@Autowired WebTestClient webClient) throws Exception {

        webClient
                .get()
                .uri("/" + NAME)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .isEqualTo("Hello " + NAME);

        logger.info("web端口是[{}]", port);
    }
}
