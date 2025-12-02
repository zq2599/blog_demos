package com.bolingcavalry.service;

import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * QwenService的单元测试类
 */
public class QwenServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(QwenServiceTest.class);

    @Mock
    private OpenAiChatModel openAiChatModel;

    @InjectMocks
    private QwenService qwenService;

    /**
     * 测试准备工作
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * 测试useImage方法是否能正常处理图片
     */
    // @Test
    // void testUseImage() {
    // logger.info("开始测试useImage方法...");

    // // 准备测试数据
    // String prompt = "请描述这张图片";
    // String expectedResponse = "这是一张风景图片";

    // // 模拟返回的AiMessage
    // AiMessage aiMessage = AiMessage.from(expectedResponse);
    // ChatResponse chatResponse = ChatResponse.builder()
    // .aiMessage(aiMessage)
    // .build();

    // // 模拟OpenAiChatModel的行为，注意这里需要匹配UserMessage类型的参数
    // when(openAiChatModel.chat(any(UserMessage.class)))
    // .thenReturn(chatResponse);

    // // 调用被测试方法
    // String actualResponse = qwenService.useImage(prompt);

    // // 验证结果是否包含预期内容和后缀
    // assertEquals(expectedResponse + "[from useImage]", actualResponse);

    // // 验证OpenAiChatModel的方法被调用
    // verify(openAiChatModel, times(1)).chat(any(UserMessage.class));

    // logger.info("useImage方法测试通过");
    // }

    /**
     * 测试成功获取响应
     */
    @Test
    void testGetResponse_Success() {
        // 准备测试数据
        String prompt = "你好，请介绍一下自己";
        String expectedResponse = "我是通义千问，一个人工智能助手。";

        // 模拟OpenAiChatModel的行为
        when(openAiChatModel.chat(prompt)).thenReturn(expectedResponse);

        // 调用被测试方法
        String actualResponse = qwenService.getResponse(prompt);

        // 验证结果
        assertEquals(expectedResponse, actualResponse);

        // 验证OpenAiChatModel的方法被调用
        verify(openAiChatModel, times(1)).chat(prompt);
    }

    /**
     * 测试获取响应时抛出异常
     */
    @Test
    void testGetResponse_Exception() {
        // 准备测试数据
        String prompt = "你好，请介绍一下自己";
        String errorMessage = "API调用失败";

        // 模拟OpenAiChatModel抛出异常
        when(openAiChatModel.chat(prompt)).thenThrow(new RuntimeException(errorMessage));

        // 验证异常是否正确传播
        RuntimeException exception = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> qwenService.getResponse(prompt));

        // 验证异常信息
        assertEquals(errorMessage, exception.getMessage());

        // 验证OpenAiChatModel的方法被调用
        verify(openAiChatModel, times(1)).chat(prompt);
    }

    /**
     * 测试空提示词的处理
     */
    @Test
    void testGetResponse_EmptyPrompt() {
        // 准备测试数据
        String prompt = "";
        String expectedResponse = "提示词不能为空";

        // 模拟OpenAiChatModel的行为
        when(openAiChatModel.chat(prompt)).thenReturn(expectedResponse);

        // 调用被测试方法
        String actualResponse = qwenService.getResponse(prompt);

        // 验证结果
        assertEquals(expectedResponse, actualResponse);

        // 验证OpenAiChatModel的方法被调用
        verify(openAiChatModel, times(1)).chat(prompt);
    }
}