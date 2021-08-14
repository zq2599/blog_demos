package com.bolingcavalry.advanced.service.impl;

import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.IndicativeSentencesGeneration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Description: (这里用一句话描述这个类的作用)
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/10/7 10:46
 */
@SpringBootTest
@IndicativeSentencesGeneration(separator = "，测试方法：", generator = DisplayNameGenerator.ReplaceUnderscores.class)
public class IndicativeSentences_Test {

    @Test
    void if_it_is_one_of_the_following_years() {
    }
}

