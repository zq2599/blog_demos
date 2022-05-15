package com.penglecode.flink.examples.boot;

import com.penglecode.flink.BasePackage;
import com.penglecode.flink.examples.FlinkExample;
import com.penglecode.flink.examples.FlinkExampleFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * Flink示例启动入口
 *
 * @author pengpeng
 * @version 1.0
 * @since 2021/11/29 23:12
 */
@SpringBootApplication(scanBasePackageClasses=BasePackage.class)
public class FlinkExampleApplication implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlinkExampleApplication.class);

    public static void main(String[] args) {
        //本例以非web方式(Servlet,Reactive)启动
        new SpringApplicationBuilder(FlinkExampleApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        LOGGER.info("【ApplicationRunner】==> args = {}", (Object) args.getSourceArgs());
        FlinkExample flinkExample = FlinkExampleFactory.getFlinkExample(args);
        if(flinkExample != null) {
            LOGGER.info("get example success, {}", flinkExample.getClass().getSimpleName());
            flinkExample.run(args);
        }
    }

}
