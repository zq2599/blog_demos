/*
 * @Author: 程序员欣宸 zq2599@gmail.com
 * @Date: 2025-11-28 09:41:33
 * @LastEditors: 程序员欣宸 zq2599@gmail.com
 * @LastEditTime: 2025-11-28 10:56:42
 * @FilePath: /langchain4j-totorials/demo-with-spring-boot/src/main/java/com/bolingcavalry/config/LangChain4jConfig.java
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
package com.bolingcavalry.config;

import dev.langchain4j.community.model.dashscope.WanxImageModel;
import dev.langchain4j.community.model.dashscope.WanxImageSize;
import dev.langchain4j.model.openai.OpenAiChatModel;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * LangChain4j配置类
 */
@Configuration
public class LangChain4jConfig {

    // 图片生成模型的配置
    @Value("${langchain4j.open-ai.chat-model.image-gen-model.api-key}")
    private String imageGenModelApiKey;

    @Value("${langchain4j.open-ai.chat-model.image-gen-model.model-name}")
    private String imageGenModelName;

    // 图片编辑模型的配置
    @Value("${langchain4j.open-ai.chat-model.image-edit-model.api-key}")
    private String imageEditModelApiKey;

    @Value("${langchain4j.open-ai.chat-model.image-edit-model.model-name}")
    private String imageEditModelName;

    @Value("${langchain4j.open-ai.chat-model.image-edit-model.base-url}")
    private String imageEditModelBaseUrl;

    // 视觉理解模型的配置
    @Value("${langchain4j.open-ai.chat-model.image-vl-model.api-key}")
    private String imageVLModelApiKey;

    @Value("${langchain4j.open-ai.chat-model.image-vl-model.model-name}")
    private String imageVLModelName;

    @Value("${langchain4j.open-ai.chat-model.image-vl-model.base-url}")
    private String imageVLModelBaseUrl;

    /**
     * 创建并配置用于图像生成的OpenAiChatModel实例
     * 
     * @return OpenAiChatModel实例，Bean名称为imageGenModel
     */
    @Bean("imageGenModel")
    public WanxImageModel imageGenModel() {
        return WanxImageModel.builder()
                .apiKey(imageGenModelApiKey)
                .modelName(imageGenModelName)
                .size(WanxImageSize.SIZE_1024_1024)
                .build();
    }

    /**
     * 创建数据结构实例，这只是个保管数据的对象，里面包含了图像编辑模型的配置参数
     * 
     * @return ImageEditModelParam实例，Bean名称为imageEditModelParam
     */
    @Bean("imageEditModelParam")
    public ImageEditModelParam imageEditModelParam() {
        ImageEditModelParam param = new ImageEditModelParam();
        param.setModelName(imageEditModelName);
        param.setBaseUrl(imageEditModelBaseUrl);
        param.setApiKey(imageEditModelApiKey);
        return param;
    }

    /**
     * 创建并配置用于视觉理解的OpenAiChatModel实例
     * 
     * @return OpenAiChatModel实例，Bean名称为imageVLModel
     */
    @Bean("imageVLModel")
    public OpenAiChatModel imageVLModel() {
        return OpenAiChatModel.builder()
                .apiKey(imageVLModelApiKey)
                .modelName(imageVLModelName)
                .baseUrl(imageVLModelBaseUrl)
                .build();
    }
}