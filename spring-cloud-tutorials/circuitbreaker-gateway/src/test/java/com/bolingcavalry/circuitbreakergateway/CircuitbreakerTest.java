package com.bolingcavalry.circuitbreakergateway;


import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * @author willzhao
 * @version 1.0
 * @description TODO
 * @date 2021/8/10 6:40
 */
@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureWebTestClient
public class CircuitbreakerTest {

    // 测试的总次数
    private static int i=0;

    @Autowired
    private WebTestClient webClient;

    @Test
    @RepeatedTest(100)
    void testHelloPredicates() throws InterruptedException {
        // 低于50次时，gen在0和1之间切换，也就是一次正常一次超时，
        // 超过50次时，gen固定为0，此时每个请求都不会超时
        int gen = (i<50) ? (i % 2) : 0;

        // 次数加一
        i++;

        final String tag = "[" + i + "]";

        // 发起web请求
        webClient.get()
                .uri("/hello/account/" + gen)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectBody(String.class).consumeWith(result  -> System.out.println(tag + result.getRawStatusCode() + " - " + result.getResponseBody()));

        Thread.sleep(1000);
    }

}
