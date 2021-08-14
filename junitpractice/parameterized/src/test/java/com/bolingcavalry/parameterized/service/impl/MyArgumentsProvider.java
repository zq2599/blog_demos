package com.bolingcavalry.parameterized.service.impl;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.stream.Stream;

/**
 * @Description: 转换类
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/10/4 23:29
 */
public class MyArgumentsProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
        return Stream.of("apple4", "banana4").map(Arguments::of);
    }
}