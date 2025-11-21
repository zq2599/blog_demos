package com.bolingcavalry;

/**
 * LangChain4j的简单示例
 * 这个类演示了如何在不依赖外部API的情况下展示LangChain4j的基本概念（使用通义千问）
 */
public class SimpleExample {
    
    public static void main(String[] args) {
        System.out.println("LangChain4j 简单示例");
        System.out.println("==================");
        
        // 演示LangChain4j的基本概念
        System.out.println("\nLangChain4j主要组件:");
        System.out.println("1. Language Models - 语言模型接口");
        System.out.println("2. Prompts - 提示词构建和管理");
        System.out.println("3. Chains - 链式调用处理复杂任务");
        System.out.println("4. Memory - 管理对话历史和上下文");
        System.out.println("5. Agents - 自主决策的代理");
        System.out.println("6. Tools - 工具集成和使用");
        
        System.out.println("使用步骤:");
        System.out.println("1. 添加LangChain4j依赖到项目");
        System.out.println("2. 创建语言模型实例(如DashScopeChatModel)");
        System.out.println("3. 构建提示词");
        System.out.println("4. 调用模型生成响应");
        System.out.println("5. 处理和展示结果");
        
        System.out.println("\n参考主示例LangChain4jHelloWorld.java了解实际使用方法（使用通义千问）。");
        System.out.println("注意：使用通义千问需要设置DASHSCOPE_API_KEY环境变量。");
    }
}