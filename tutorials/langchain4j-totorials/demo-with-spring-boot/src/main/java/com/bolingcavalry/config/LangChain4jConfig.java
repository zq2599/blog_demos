/*
 * @Author: 程序员欣宸 zq2599@gmail.com
 * @Date: 2025-11-28 09:41:33
 * @LastEditors: 程序员欣宸 zq2599@gmail.com
 * @LastEditTime: 2025-11-28 10:56:42
 * @FilePath: /langchain4j-totorials/demo-with-spring-boot/src/main/java/com/bolingcavalry/config/LangChain4jConfig.java
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
package com.bolingcavalry.config;

import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import dev.langchain4j.service.AiServices;
import com.bolingcavalry.service.Assistant;

/**
 * LangChain4j配置类
 */
@Configuration
public class LangChain4jConfig {

    @Value("${langchain4j.open-ai.chat-model.api-key}")
    private String apiKey;

    @Value("${langchain4j.open-ai.chat-model.model-name:qwen-turbo}")
    private String modelName;

    @Value("${langchain4j.open-ai.chat-model.base-url}")
    private String baseUrl;

    // imageModel配置
    @Value("${langchain4j.open-ai.chat-model.image-model.api-key}")
    private String imageModelApiKey;

    @Value("${langchain4j.open-ai.chat-model.image-model.model-name}")
    private String imageModelName;

    @Value("${langchain4j.open-ai.chat-model.image-model.base-url}")
    private String imageModelBaseUrl;

    /**
     * 创建并配置OpenAiChatModel实例（使用通义千问的OpenAI兼容接口）
     * 
     * @return OpenAiChatModel实例
     */
    @Primary
    @Bean
    public OpenAiChatModel openAiChatModel() {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .baseUrl(baseUrl)
                .build();
    }

    /**
     * 创建并配置用于图像理解的OpenAiChatModel实例
     * 
     * @return OpenAiChatModel实例，Bean名称为imageModel
     */
    @Bean("imageModel")
    public OpenAiChatModel imageModel() {
        return OpenAiChatModel.builder()
                .apiKey(imageModelApiKey)
                .modelName(imageModelName)
                .baseUrl(imageModelBaseUrl)
                .build();
    }

    @Bean
    public Assistant assistant(OpenAiChatModel chatModel) {
        return AiServices.create(Assistant.class, chatModel);
    }
}