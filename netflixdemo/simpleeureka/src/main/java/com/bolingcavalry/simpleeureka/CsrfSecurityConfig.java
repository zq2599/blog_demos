package com.bolingcavalry.simpleeureka;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * @Description: 关闭CSRF校验
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/8/17 8:27
 */
@EnableWebSecurity
public class CsrfSecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        super.configure(http);
    }
}