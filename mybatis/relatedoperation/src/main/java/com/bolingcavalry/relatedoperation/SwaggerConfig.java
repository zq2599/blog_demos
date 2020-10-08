package com.bolingcavalry.relatedoperation;

import springfox.documentation.service.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @Description: swagger配置类
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/8/11 7:54
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .tags(new Tag("UserController", "用户服务"), new Tag("LogController", "日志服务"))
                .select()
                // 当前包路径
                .apis(RequestHandlerSelectors.basePackage("com.bolingcavalry.relatedoperation.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    //构建 api文档的详细信息函数,注意这里的注解引用的是哪个
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                //页面标题
                .title("MyBatis CURD操作")
                //创建人
                .contact(new Contact("程序员欣宸", "https://github.com/zq2599/blog_demos", "zq2599@gmail.com"))
                //版本号
                .version("1.0")
                //描述
                .description("API 描述")
                .build();
    }
}