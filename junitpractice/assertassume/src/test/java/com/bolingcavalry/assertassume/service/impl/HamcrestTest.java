package com.bolingcavalry.assertassume.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


/**
 * @Description: Hamcrest体验
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/9/29 7:49
 */
@SpringBootTest
@Slf4j
public class HamcrestTest {

    @Test
    @DisplayName("体验hamcrest")
    void assertWithHamcrestMatcher() {
        assertThat(Math.addExact(1, 2), is(equalTo(5)));
    }
}

