package com.bolingcavalry.controller;

import com.bolingcavalry.service.QwenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

/**
 * QwenController的单元测试类
 */
public class QwenControllerTest {

    @Mock
    private QwenService qwenService;

    @InjectMocks
    private QwenController qwenController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    /**
     * 测试准备工作
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(qwenController).build();
        objectMapper = new ObjectMapper();
    }

    /**
     * 测试成功的聊天请求
     */
    @Test
    void testChat_Success() throws Exception {
        // 准备测试数据
        String prompt = "你好，请介绍一下自己";
        String mockResponse = "我是通义千问，一个人工智能助手。";
        
        // 模拟QwenService的行为
        when(qwenService.getResponse(prompt)).thenReturn(mockResponse);
        
        // 创建请求体
        QwenController.PromptRequest request = new QwenController.PromptRequest();
        request.setPrompt(prompt);
        
        // 执行请求并验证结果
        mockMvc.perform(post("/api/qwen/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value(mockResponse));
        
        // 验证QwenService的方法被调用
        verify(qwenService, times(1)).getResponse(prompt);
    }

    /**
     * 测试空提示词的情况
     */
    @Test
    void testChat_EmptyPrompt() throws Exception {
        // 创建空提示词请求体
        QwenController.PromptRequest request = new QwenController.PromptRequest();
        request.setPrompt("");
        
        // 执行请求并验证结果
        mockMvc.perform(post("/api/qwen/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("提示词不能为空"));
        
        // 验证QwenService的方法没有被调用
        verify(qwenService, never()).getResponse(anyString());
    }

    /**
     * 测试服务抛出异常的情况
     */
    @Test
    void testChat_ServiceException() throws Exception {
        // 准备测试数据
        String prompt = "你好，请介绍一下自己";
        String errorMessage = "服务暂不可用";
        
        // 模拟QwenService抛出异常
        when(qwenService.getResponse(prompt)).thenThrow(new RuntimeException(errorMessage));
        
        // 创建请求体
        QwenController.PromptRequest request = new QwenController.PromptRequest();
        request.setPrompt(prompt);
        
        // 执行请求并验证结果
        mockMvc.perform(post("/api/qwen/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value(containsString("请求处理失败")))
                .andExpect(jsonPath("$.result").value(containsString(errorMessage)));
        
        // 验证QwenService的方法被调用
        verify(qwenService, times(1)).getResponse(prompt);
    }

    /**
     * 测试null请求体的情况
     */
    @Test
    void testChat_NullRequest() throws Exception {
        // 执行请求并验证结果
        mockMvc.perform(post("/api/qwen/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("提示词不能为空"));
        
        // 验证QwenService的方法没有被调用
        verify(qwenService, never()).getResponse(anyString());
    }
}