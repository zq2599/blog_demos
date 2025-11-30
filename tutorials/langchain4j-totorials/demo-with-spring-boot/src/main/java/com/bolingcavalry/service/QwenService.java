package com.bolingcavalry.service;

import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.Response;

import java.net.URI;
import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import dev.langchain4j.data.message.*;

import dev.langchain4j.data.image.Image;
import com.bolingcavalry.util.ImageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 通义千问服务类，用于与通义千问模型进行交互
 */
@Service
public class QwenService {

    private static final Logger logger = LoggerFactory.getLogger(QwenService.class);

    // 注入OpenAiChatModel，用于与通义千问进行交互
    private final OpenAiChatModel openAiChatModel;

    /**
     * 构造函数，通过依赖注入获取OpenAiChatModel实例
     * 
     * @param openAiChatModel OpenAiChatModel实例
     */
    @Autowired
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

    public String useImage(String prompt) {
        String imageUrl = "https://scpic.chinaz.net/files/pic/pic6/pic1103.jpg";
        
        try {
            logger.info("开始处理图片: {}", imageUrl);
            
            // 使用ImageUtils类来创建Image对象，这样可以确保图片数据被正确加载
            Image image = ImageUtils.createImageFromUrl(imageUrl);
            
            // 验证图片是否成功加载（通过检查base64数据是否存在且有一定长度）
            String base64Data = ImageUtils.getImageBase64(image);
            if (base64Data == null || base64Data.isEmpty() || base64Data.length() < 10) {
                logger.error("图片加载失败：Base64数据无效或为空");
                return "图片加载失败，请检查URL或网络连接[from useImage]";
            }
            
            logger.info("图片成功加载，Base64数据长度: {} 字符", base64Data.length());
            
            // 创建图片内容
            ImageContent imageContent = new ImageContent(image, ImageContent.DetailLevel.HIGH);

            // 用户提问
            UserMessage messages = UserMessage.from(List.of(
                TextContent.from(prompt),
                imageContent
            ));

            // 调用模型进行处理
            logger.info("将图片内容发送给模型处理...");
            String result = openAiChatModel.chat(messages).aiMessage().text();
            
            logger.info("模型返回结果: {}", result);
            return result + "[from useImage]";
        } catch (Exception e) {
            logger.error("处理图片时发生错误: {}", e.getMessage(), e);
            return "处理图片时发生错误: " + e.getMessage() + "[from useImage]";
        }
    }
}