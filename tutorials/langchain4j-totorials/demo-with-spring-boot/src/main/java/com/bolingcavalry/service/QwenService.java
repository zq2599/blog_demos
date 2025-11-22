package com.bolingcavalry.service;

import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 通义千问服务类，用于调用LangChain4j的ChatLanguageModel获取模型响应
 */
@Service
public class QwenService {

    // 注入ChatLanguageModel，配置参数将从application.properties中读取
    private final ChatLanguageModel chatLanguageModel;

    /**
     * 构造函数，通过依赖注入获取ChatLanguageModel实例
     * @param chatLanguageModel ChatLanguageModel实例
     */
    @Autowired
    public QwenService(ChatLanguageModel chatLanguageModel) {
        this.chatLanguageModel = chatLanguageModel;
    }

    /**
     * 发送提示词到通义千问模型并获取响应
     * @param prompt 用户提示词
     * @return 模型响应文本
     */
    public String getResponse(String prompt) {
        return chatLanguageModel.generate(prompt);
    }
}