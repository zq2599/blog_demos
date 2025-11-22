package com.bolingcavalry;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.dashscope.QwenChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LangChain4j的Hello World示例
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
        // 创建通义千问聊天模型
        ChatLanguageModel model = QwenChatModel.builder()
                .apiKey(apiKey)
                .modelName("qwen3-max")
                .build();
        
        logger.info("\n=== 真实AI聊天演示 ===");
        
        // 发送消息并获取响应
        String response = model.generate("你好，世界！请简要介绍一下你自己，包括详细的版本情况，再带上最新的年月日时分秒。");
        logger.info("AI响应: {}", response);
    }
}