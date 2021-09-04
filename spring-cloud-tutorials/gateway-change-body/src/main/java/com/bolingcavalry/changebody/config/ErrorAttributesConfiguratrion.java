package com.bolingcavalry.changebody.config;

import com.bolingcavalry.changebody.bean.MyErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author willzhao
 * @version 1.0
 * @description TODO
 * @date 2021/9/2 8:27
 */
@Configuration
public class ErrorAttributesConfiguratrion {
    @Bean
    public ErrorAttributes errorAttributes() {
        return new MyErrorAttributes();
    }

}
