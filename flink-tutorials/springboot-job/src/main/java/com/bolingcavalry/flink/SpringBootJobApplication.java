package com.bolingcavalry.flink;

import com.bolingcavalry.flink.job.WordCountJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
@SpringBootApplication(scanBasePackageClasses=SpringBootJobApplication.class)
@Slf4j
public class SpringBootJobApplication implements ApplicationRunner {

    /**
     * 程序入口
     * @param args
     */
    public static void main(String[] args) {
        // 非web方式启动(Servlet,Reactive)
        new SpringApplicationBuilder(SpringBootJobApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    @Autowired
    private WordCountJob wordCountJob;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        wordCountJob.run();
    }

}
