package com.bolingcavalry.druidtwosource;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * @Description: druid配置类
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/8/18 08:12
 */
@Configuration
public class TwoDataSourceConfig {

    @Primary
    @Bean(name = "firstDataSource")
    @ConfigurationProperties("spring.datasource.druid.first")
    public DataSource first() {
        return DruidDataSourceBuilder.create().build();
    }

    @Bean(name = "secondDataSource")
    @ConfigurationProperties("spring.datasource.druid.second")
    public DataSource second() {
        return DruidDataSourceBuilder.create().build();
    }
}