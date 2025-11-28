package com.bolingcavalry;

import dev.langchain4j.model.openai.OpenAiChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 这个示例演示了如何使用LangChain4j创建一个简单的对话模型（使用通义千问）
 */
public class LangChain4jHelloWorld {
    
    private static final Logger logger = LoggerFactory.getLogger(LangChain4jHelloWorld.class);
    
    public static void main(String[] args) {
        logger.info("LangChain4j Hello World示例（使用通义千问）");
        logger.info("--------------------------------");
        
        try {
            // 注意：在实际使用中，您需要提供有效的通义千问API密钥
            // 这里我们使用一个简单的示例，可能需要替换为实际可用的模型
            String apiKey = System.getenv("DASHSCOPE_API_KEY");
            
            if (apiKey == null || apiKey.isEmpty()) {
                logger.warn("警告：未设置DASHSCOPE_API_KEY环境变量，立即结束");
            } else {
                realChat(apiKey);
            }
        } catch (Exception e) {
            logger.error("执行过程中出现错误", e);
        }
    }

    /**
     * 使用通义千问API进行真实聊天
     */
    private static void realChat(String apiKey) {
        // 在langchain4j 1.x版本中，直接使用OpenAiChatModel类
        OpenAiChatModel model = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName("qwen3-max")
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .build();
        
        logger.info("\n=== 真实AI聊天演示 ===");
        
        // 直接发送字符串消息
        String prompt = "你好，世界！请简要介绍一下你自己，包括详细的版本情况，再带上最新的年月日时分秒。";
        
        // 尝试直接使用字符串作为参数
        String response = model.chat(prompt);
        logger.info("AI响应: {}", response);
    }
}