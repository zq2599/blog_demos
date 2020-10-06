package com.bolingcavalry.parameterized.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.converter.JavaTimeConversionPattern;
import org.junit.jupiter.params.provider.*;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @Description: 参数化的demo
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date:  2020/10/2 22:18
 */
@SpringBootTest
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HelloTest {

    @Order(1)
    @DisplayName("多个字符串型入参")
    @ParameterizedTest
    @ValueSource(strings = { "a", "b", "c" })
    void stringsTest(String candidate) {
        log.info("stringsTest [{}]", candidate);
        assertTrue(null!=candidate);
    }

    @Order(2)
    @DisplayName("多个int型入参")
    @ParameterizedTest
    @ValueSource(ints = { 1,2,3 })
    void intsTest(int candidate) {
        log.info("ints [{}]", candidate);
        assertTrue(candidate<3);
    }

    @Order(3)
    @DisplayName("增加一个入参，值为null")
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = { "a", "b", "c" })
    void nullSourceTest(String candidate) {
        log.info("nullSourceTest [{}]", candidate);
    }

    @Order(4)
    @DisplayName("增加一个入参，值为空字符串")
    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = { "a", "b", "c" })
    void emptySourceTest(String candidate) {
        log.info("emptySourceTest [{}]", candidate);
    }

    @Order(5)
    @DisplayName("增加两个入参，值为null和空字符串")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "a", "b", "c" })
    void nullAndEmptySourceTest(String candidate) {
        log.info("nullAndEmptySourceTest [{}]", candidate);
    }

    @Order(6)
    @DisplayName("多个枚举型入参")
    @ParameterizedTest
    @EnumSource
    void enumSourceTest(Types type) {
        log.info("enumSourceTest [{}]", type);
    }

    @Order(7)
    @DisplayName("多个枚举型入参(指定要执行的枚举值)")
    @ParameterizedTest
    @EnumSource(names={"SMALL", "UNKNOWN"})
    void enumSourceWithIncludeTest(Types type) {
        log.info("enumSourceWithIncludeTest [{}]", type);
    }

    @Order(8)
    @DisplayName("多个枚举型入参(指定不执行的枚举值)")
    @ParameterizedTest
    @EnumSource(mode= EnumSource.Mode.EXCLUDE, names={"SMALL", "UNKNOWN"})
    void enumSourceWithExcludeTest(Types type) {
        log.info("enumSourceWithExcludeTest [{}]", type);
    }


    static Stream<String> stringProvider() {
        return Stream.of("apple1", "banana1");
    }

    @Order(9)
    @DisplayName("静态方法返回集合，用此集合中每个元素作为入参")
    @ParameterizedTest
    @MethodSource("stringProvider")
    void methodSourceTest(String candidate) {
        log.info("methodSourceTest [{}]", candidate);
    }

    @Order(10)
    @DisplayName("静态方法返回集合，该静态方法在另一个类中")
    @ParameterizedTest
    @MethodSource("com.bolingcavalry.parameterized.service.impl.Utils#getStringStream")
    void methodSourceFromOtherClassTest(String candidate) {
        log.info("methodSourceFromOtherClassTest [{}]", candidate);
    }

    static Stream<String> methodSourceWithoutMethodNameTest() {
        return Stream.of("apple3", "banana3");
    }

    @Order(11)
    @DisplayName("静态方法返回集合，不指定静态方法名，自动匹配")
    @ParameterizedTest
    @MethodSource
    void methodSourceWithoutMethodNameTest(String candidate) {
        log.info("methodSourceWithoutMethodNameTest [{}]", candidate);
    }

    @Order(12)
    @DisplayName("CSV格式多条记录入参")
    @ParameterizedTest
    @CsvSource({
            "apple1, 11",
            "banana1, 12",
            "'lemon1, lime1', 0x0A"
    })
    void csvSourceTest(String fruit, int rank) {
        log.info("csvSourceTest, fruit [{}], rank [{}]", fruit, rank);
    }

    @Order(13)
    @DisplayName("CSV格式多条记录入参(识别null)")
    @ParameterizedTest
    @CsvSource(value = {
            "apple2, 21",
            "banana2, 22",
            "'lemon2, lime2', 0x0A",
            "NIL, 3" },
            nullValues = "NIL"
    )
    void csvSourceWillNullTokenTest(String fruit, int rank) {
        log.info("csvSourceWillNullTokenTest, fruit [{}], rank [{}]", fruit, rank);
    }

    @Order(14)
    @DisplayName("CSV文件多条记录入参")
    @ParameterizedTest
    @CsvFileSource(files = "src/test/resources/two-column.csv", numLinesToSkip = 1)
    void csvFileTest(String country, int reference) {
        log.info("csvSourceTest, country [{}], reference [{}]", country, reference);
    }

    @Order(15)
    @DisplayName("ArgumentsProvider接口的实现类提供的数据作为入参")
    @ParameterizedTest
    @ArgumentsSource(MyArgumentsProvider.class)
    void argumentsSourceTest(String candidate) {
        log.info("argumentsSourceTest [{}]", candidate);
    }

    @Order(16)
    @DisplayName("int型自动转为double型入参")
    @ParameterizedTest
    @ValueSource(ints = { 1,2,3 })
    void argumentConversionTest(double candidate) {
        log.info("argumentConversionTest [{}]", candidate);
    }

    @Order(17)
    @DisplayName("string型，指定转换器，转为LocalDate型入参")
    @ParameterizedTest
    @ValueSource(strings = { "01.01.2017", "31.12.2017" })
    void argumentConversionWithConverterTest(
            @JavaTimeConversionPattern("dd.MM.yyyy") LocalDate candidate) {
        log.info("argumentConversionWithConverterTest [{}]", candidate);
    }

    @Order(18)
    @DisplayName("CsvSource的多个字段聚合到ArgumentsAccessor实例")
    @ParameterizedTest
    @CsvSource({
            "Jane1, Doe1, BIG",
            "John1, Doe1, SMALL"
    })
    void argumentsAccessorTest(ArgumentsAccessor arguments) {
        Person person = new Person();
        person.setFirstName(arguments.getString(0));
        person.setLastName(arguments.getString(1));
        person.setType(arguments.get(2, Types.class));

        log.info("argumentsAccessorTest [{}]", person);
    }

    @Order(19)
    @DisplayName("CsvSource的多个字段，通过指定聚合类转为Person实例")
    @ParameterizedTest
    @CsvSource({
            "Jane2, Doe2, SMALL",
            "John2, Doe2, UNKNOWN"
    })
    void customAggregatorTest(@AggregateWith(PersonAggregator.class) Person person) {
        log.info("customAggregatorTest [{}]", person);
    }

    @Order(20)
    @DisplayName("CsvSource的多个字段，通过指定聚合类转为Person实例(自定义注解)")
    @ParameterizedTest
    @CsvSource({
            "Jane3, Doe3, BIG",
            "John3, Doe3, UNKNOWN"
    })
    void customAggregatorAnnotationTest(@CsvToPerson Person person) {
        log.info("customAggregatorAnnotationTest [{}]", person);
    }

    @Order(21)
    @DisplayName("CSV格式多条记录入参(自定义展示名称)")
    @ParameterizedTest(name = "序号 [{index}]，fruit参数 [{0}]，rank参数 [{1}]")
    @CsvSource({
            "apple3, 31",
            "banana3, 32",
            "'lemon3, lime3', 0x3A"
    })
    void csvSourceWithCustomDisplayNameTest(String fruit, int rank) {
        log.info("csvSourceWithCustomDisplayNameTest, fruit [{}], rank [{}]", fruit, rank);
    }
}