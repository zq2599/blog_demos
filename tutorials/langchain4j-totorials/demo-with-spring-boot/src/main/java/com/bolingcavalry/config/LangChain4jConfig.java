package com.bolingcavalry.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * LangChain4j配置类，用于创建和配置ChatLanguageModel Bean
 * 使用OpenAI兼容接口调用通义千问模型
 */
@Configuration
public class LangChain4jConfig {

    @Value("${langchain4j.open-ai.chat-model.api-key}")
    private String apiKey;

    @Value("${langchain4j.open-ai.chat-model.model-name:qwen-turbo}")
    private String modelName;

    @Value("${langchain4j.open-ai.chat-model.base-url}")
    private String baseUrl;

    /**
     * 创建并配置OpenAiChatModel实例（使用通义千问的OpenAI兼容接口）
     * @return ChatLanguageModel实例，将被自动注入到QwenService中
     */
    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .baseUrl(baseUrl)
                .build();
    }
}