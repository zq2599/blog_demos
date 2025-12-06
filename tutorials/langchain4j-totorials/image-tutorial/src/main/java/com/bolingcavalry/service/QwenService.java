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
    public QwenService(@Qualifier("imageVLModel") OpenAiChatModel imageVLModel,
            @Qualifier("imageGenModel") WanxImageModel imageGenModel,
            @Qualifier("imageEditModelParam") ImageEditModelParam imageEditModelParam) {
        this.imageVLModel = imageVLModel;
        this.imageGenModel = imageGenModel;
        this.imageEditModelParam = imageEditModelParam;
    }

    /**
     * 使用图片理解模型根据提示词处理图片
     * 
     * @param imageUrl 图片URL
     * @param prompt   图片处理提示词
     * @return 处理结果或错误信息
     */
    public String useImage(String imageUrl, String prompt) {
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