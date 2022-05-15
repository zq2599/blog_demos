package com.penglecode.flink.examples;

import com.penglecode.flink.common.util.SpringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * FlinkExample工厂类
 *
 * @author pengpeng
 * @version 1.0
 * @since 2021/12/4 21:08
 */
public class FlinkExampleFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlinkExampleFactory.class);

    @SuppressWarnings("unchecked")
    public static <T extends FlinkExample> T getFlinkExample(ApplicationArguments args) {
        List<String> flinkExampleClassNames = args.getOptionValues("flink.example.class");

        LOGGER.info("flinkExampleClassNames {}", flinkExampleClassNames);


        if(!CollectionUtils.isEmpty(flinkExampleClassNames)) {
            String flinkExampleClassName = flinkExampleClassNames.get(0);
            try {
                Class<T> flinkExampleClass = (Class<T>) ClassUtils.forName(flinkExampleClassName, ClassUtils.getDefaultClassLoader());
                return SpringUtils.getBean(flinkExampleClass);
            } catch (ClassNotFoundException e) {
                LOGGER.error("No such FlinkExample class found! flink.example.class = {}", flinkExampleClassName);
            } catch (BeansException e) {
                LOGGER.error("No such FlinkExample bean found! flink.example.class = {}", flinkExampleClassName);
            }
        } else {
            LOGGER.error("No program arguments['flink.example.class'] found!");
        }
        return null;
    }

}
