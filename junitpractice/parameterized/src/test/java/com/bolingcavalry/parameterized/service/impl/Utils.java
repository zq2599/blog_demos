package com.bolingcavalry.parameterized.service.impl;

import java.util.stream.Stream;

/**
 * @Description: (这里用一句话描述这个类的作用)
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/10/4 22:58
 */
public class Utils {

    public static Stream<String> getStringStream() {
        return Stream.of("apple2", "banana2");
    }

}
