package com.bolingcavalry.service;

import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 通义千问服务类，用于与通义千问模型进行交互
 */
@Service
public class QwenService {

    // 注入OpenAiChatModel，用于与通义千问进行交互
    private final OpenAiChatModel openAiChatModel;

    /**
     * 构造函数，通过依赖注入获取OpenAiChatModel实例
     * @param openAiChatModel OpenAiChatModel实例
     */
    @Autowired
    public QwenService(OpenAiChatModel openAiChatModel) {
        this.openAiChatModel = openAiChatModel;
    }

    /**
     * 调用通义千问模型进行对话
     * @param message 用户消息
     * @return AI回复
     */
    public String chat(String message) {
        return openAiChatModel.chat(message);
    }

    /**
     * 获取AI模型的响应（用于接口调用）
     * @param prompt 用户提示词
     * @return AI生成的回答
     */
    public String getResponse(String prompt) {
        return openAiChatModel.chat(prompt);
    }
}