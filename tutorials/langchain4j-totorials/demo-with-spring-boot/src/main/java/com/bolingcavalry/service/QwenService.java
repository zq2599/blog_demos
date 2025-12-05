package com.bolingcavalry.service;

import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.bolingcavalry.config.ImageEditModelParam;
import com.bolingcavalry.util.ImageUtils;
import dev.langchain4j.community.model.dashscope.WanxImageModel;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.data.message.*;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.Response;
import com.alibaba.dashscope.common.Role;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * 通义千问服务类，用于与通义千问模型进行交互
 */
@Service
public class QwenService {

    private static final Logger logger = LoggerFactory.getLogger(QwenService.class);

    // 注入OpenAiChatModel，用于与通义千问进行交互
    private final OpenAiChatModel openAiChatModel;

    // 注入OpenAiChatModel，用于图像理解任务
    private final OpenAiChatModel imageVLModel;

    // 注入WanxImageModel，用于图像生成任务
    private final WanxImageModel imageGenModel;

    // 注入WanxImageModel，用于图像编辑任务
    private final ImageEditModelParam imageEditModelParam;

    /**
     * 构造函数，通过依赖注入获取QwenChatModel实例
     * 
     * @param openAiChatModel QwenChatModel实例
     * @param imageModel      用于图像理解的QwenChatModel实例
     * @param imageGenModel   用于图像生成的QwenChatModel实例
     */
    @Autowired
    public QwenService(OpenAiChatModel openAiChatModel,
            @Qualifier("imageVLModel") OpenAiChatModel imageVLModel,
            @Qualifier("imageGenModel") WanxImageModel imageGenModel,
            @Qualifier("imageEditModelParam") ImageEditModelParam imageEditModelParam) {
        this.openAiChatModel = openAiChatModel;
        this.imageVLModel = imageVLModel;
        this.imageGenModel = imageGenModel;
        this.imageEditModelParam = imageEditModelParam;
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

    public String useImage(String imageUrl, String prompt) {
        // String imageUrl = "https://scpic.chinaz.net/files/pic/pic6/pic1103.jpg";

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
                    imageContent));

            // 调用模型进行处理
            logger.info("将图片内容发送给模型处理...");
            String result = imageVLModel.chat(messages).aiMessage().text();

            logger.info("模型返回结果: {}", result);
            return result + "[from useImage]";
        } catch (Exception e) {
            logger.error("处理图片时发生错误: {}", e.getMessage(), e);
            return "处理图片时发生错误: " + e.getMessage() + "[from useImage]";
        }
    }

    /**
     * 使用通义千问qwen3-image-plus模型生成图片
     * 
     * @param prompt 图片生成提示词
     * @return 生成的图片URL或相关信息
     */
    public String generateImage(String prompt, int imageNum) {
        try {
            logger.info("开始生成图片，提示词: {}", prompt);

            // 使用imageGenModel生成图片
            Response<List<Image>> result = imageGenModel.generate(prompt, imageNum);

            logger.info("图片生成成功，结果: {}", result);
            return result + "[from generateImage]";
        } catch (Exception e) {
            logger.error("生成图片时发生错误: {}", e.getMessage(), e);
            return "生成图片时发生错误: " + e.getMessage() + "[from generateImage]";
        }
    }

    public String editImage(List<String> imageUrls, String prompt) {
        MultiModalConversation conv = new MultiModalConversation();

        var contents = new ArrayList<Map<String, Object>>();
        for (String imageUrl : imageUrls) {
            contents.add(Collections.singletonMap("image", imageUrl));
        }
        contents.add(Collections.singletonMap("text", prompt));

        MultiModalMessage userMessage = MultiModalMessage.builder().role(Role.USER.getValue())
                .content(contents)
                .build();

        // qwen-image-edit-plus支持输出1-6张图片，此处以两张为例
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("watermark", false);
        parameters.put("negative_prompt", " ");
        parameters.put("n", 2);
        parameters.put("prompt_extend", true);
        // 仅当输出图像数量n=1时支持设置size参数，否则会报错
        // parameters.put("size", "1024*2048");

        MultiModalConversationParam param = MultiModalConversationParam.builder()
                .apiKey(imageEditModelParam.getApiKey())
                .model(imageEditModelParam.getModelName())
                .messages(Collections.singletonList(userMessage))
                .parameters(parameters)
                .build();

        try {
            MultiModalConversationResult result = conv.call(param);
            return result + "[from editImage]";
        } catch (Exception e) {
            logger.error("编辑图片时发生错误: {}", e.getMessage(), e);
            return "编辑图片时发生错误: " + e.getMessage() + "[from editImage]";
        }
    }
}