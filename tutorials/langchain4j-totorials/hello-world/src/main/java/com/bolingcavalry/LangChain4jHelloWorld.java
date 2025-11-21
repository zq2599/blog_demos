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
                logger.warn("警告：未设置DASHSCOPE_API_KEY环境变量");
                logger.info("将使用模拟模式进行演示");
                
                // 模拟模式：直接输出示例响应
                simulateChat();
            } else {
                // 真实模式：使用通义千问API
                logger.info("将使用通义千问API进行演示");
                realChat(apiKey);
            }
        } catch (Exception e) {
            logger.error("执行过程中出现错误", e);
            logger.info("\n切换到模拟模式进行演示:");
            simulateChat();
        }
    }
    
    /**
     * 模拟聊天功能，不依赖外部API
     */
    private static void simulateChat() {
        logger.info("\n=== 模拟聊天演示 ===");
        logger.info("用户: 你好，世界！");
        logger.info("AI: 你好！我是一个基于LangChain4j的AI助手。很高兴见到你！");
        logger.info("用户: LangChain4j是什么？");
        logger.info("AI: LangChain4j是一个Java库，用于构建基于大型语言模型(LLM)的应用程序。它提供了与各种LLM模型交互的统一接口。");
        logger.info("\n提示：要使用真实的AI功能，请设置DASHSCOPE_API_KEY环境变量。");
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