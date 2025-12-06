package com.bolingcavalry.service;

import dev.langchain4j.data.message.*;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.openai.OpenAiChatModel;
import java.util.List;
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
     * 构造函数，通过依赖注入获取QwenChatModel实例
     * 
     * @param openAiChatModel QwenChatModel实例
     * @param imageModel      用于图像理解的QwenChatModel实例
     * @param imageGenModel   用于图像生成的QwenChatModel实例
     */
    public QwenService(OpenAiChatModel openAiChatModel) {
        this.openAiChatModel = openAiChatModel;
    }

    /**
     * 调用通义千问模型进行对话
     * 
     * @param message 用户消息
     * @return AI回复
     */
    public String chat(String message) {
        return openAiChatModel.chat(message);
    }

    /**
     * 获取AI模型的响应（用于接口调用）
     * 
     * @param prompt 用户提示词
     * @return AI生成的回答
     */
    public String getResponse(String prompt) {
        return openAiChatModel.chat(prompt);
    }

    @Autowired
    private Assistant assistant;

    /**
     * 调用AiService进行最简单的对话
     * 
     * @param prompt 用户提示词
     * @return 助手生成的回答
     */
    public String aiServiceSimpleChat(String prompt) {
        return assistant.simpleChat(prompt) + "[from aiservice simpleChat]";
    }

    /**
     * 调用AiService进行模板对话
     * 
     * @param name 模板中的变量
     * @return 助手生成的回答
     */
    public String aiServiceTemplateChat(String name) {
        return assistant.temlateChat(name) + "[from aiservice templateChat]";
    }

    public String aiServiceTemplateChatWithSysMsg(String name) {
        return assistant.temlateChatWithSysMsg(name) + "[from aiservice templateChatWithSysMsg]";
    }

    /**
     * 模拟多轮对话
     * 
     * @param prompt 模板中的变量
     * @return 助手生成的回答
     */
    public String simulateMultiRoundChat(String prompt) {
        List<ChatMessage> history = List.of(
                SystemMessage.from("你是历史学者，回答问题是简洁风格"),
                UserMessage.from("介绍曹操是谁"),
                AiMessage.from("曹操（155－220年），东汉末年杰出政治家、军事家、文学家，魏国奠基者。他统一北方，推行屯田，唯才是举，善用兵法，亦为建安文学代表人物，著有《观沧海》等诗作。"),
                UserMessage.from(prompt));

        AiMessage reply = openAiChatModel.chat(history).aiMessage();

        return reply.text() + "[from simulateMultiRoundChat]";
    }

    public String useChatRequest(String prompt) {
        List<ChatMessage> messages = List.of(
                SystemMessage.from("你是Java程序员，回答问题是简洁风格"),
                UserMessage.from(prompt));

        ChatRequest request = ChatRequest.builder()
                .messages(messages)
                .temperature(0.7)
                .maxOutputTokens(100)
                .build();

        return openAiChatModel.chat(request).aiMessage().text() + "[from useChatRequest]";
    }
}