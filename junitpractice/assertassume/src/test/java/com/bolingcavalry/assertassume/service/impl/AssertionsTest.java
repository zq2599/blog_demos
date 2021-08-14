package com.bolingcavalry.assertassume.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.springframework.boot.test.context.SpringBootTest;

import static java.time.Duration.ofMinutes;
import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.Assertions.*;


/**
 * @Description: (这里用一句话描述这个类的作用)
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/9/29 7:49
 */
@SpringBootTest
@Slf4j
public class AssertionsTest {

    @Test
    @DisplayName("最普通的判断")
    void standardTest() {
       assertEquals(2, Math.addExact(1, 1));
    }

    @Test
    @DisplayName("带失败提示的判断(拼接消息字符串的代码只有判断失败时才执行)")
    void assertWithLazilyRetrievedMessage() {
        int expected = 2;
        int actual = 1;

        assertEquals(expected,
                actual,
                // 这个lambda表达式，只有在expected和actual不相等时才执行
                ()->String.format("期望值[%d]，实际值[%d]", expected, actual));
    }

    @Test
    @DisplayName("批量判断(必须全部通过，否则就算失败)")
    void groupedAssertions() {
        // 将多个判断放在一起执行，只有全部通过才算通过，如果有未通过的，会有对应的提示
        assertAll("单个测试方法中多个判断",
                () -> assertEquals(1, 1),
                () -> assertEquals(2, 1),
                () -> assertEquals(3, 1)
        );
    }

    @Test
    @DisplayName("判断抛出的异常是否是指定类型")
    void exceptionTesting() {

        // assertThrows的第二个参数是Executable，
        // 其execute方法执行时，如果抛出了异常，并且异常的类型是assertThrows的第一个参数(这里是ArithmeticException.class)，
        // 那么测试就通过了，返回值是异常的实例
        Exception exception = assertThrows(ArithmeticException.class, () -> Math.floorDiv(1,0));

        log.info("assertThrows通过后，返回的异常实例：{}", exception.getMessage());
    }

    @Test
    @DisplayName("在指定时间内完成测试")
    void timeoutExceeded() {
        // 指定时间是1秒，实际执行用了2秒
        assertTimeout(ofSeconds(1), () -> {
            try{
              Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }


    @Test
    @DisplayName("在指定时间内完成测试")
    void timeoutNotExceededWithResult() {

        // 准备ThrowingSupplier类型的实例，
        // 里面的get方法sleep了1秒钟，然后返回一个字符串
        ThrowingSupplier<String> supplier = () -> {

            try{
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return "我是ThrowingSupplier的get方法的返回值";
        };

        // 指定时间是2秒，实际上ThrowingSupplier的get方法只用了1秒
        String actualResult = assertTimeout(ofSeconds(2), supplier);

        log.info("assertTimeout的返回值：{}", actualResult);
    }

    @Test
    void timeoutExceededWithPreemptiveTermination() {
        log.info("开始timeoutExceededWithPreemptiveTermination");
        assertTimeoutPreemptively(ofSeconds(2), () -> {
            log.info("开始sleep");
            try{
                Thread.sleep(10000);
                log.info("sleep了10秒");
            } catch (InterruptedException e) {
                log.error("线程sleep被中断了", e);
            }
        });
    }

}

