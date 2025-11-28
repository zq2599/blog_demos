/*
 * @Author: 程序员欣宸 zq2599@gmail.com
 * @Date: 2025-11-28 09:41:52
 * @LastEditors: 程序员欣宸 zq2599@gmail.com
 * @LastEditTime: 2025-11-28 11:37:52
 * @FilePath: /langchain4j-totorials/demo-with-spring-boot/src/main/java/com/bolingcavalry/controller/QwenController.java
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
package com.bolingcavalry.controller;

import com.bolingcavalry.service.QwenService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 通义千问控制器，处理与大模型交互的HTTP请求
 */
@RestController
@RequestMapping("/api/qwen")
public class QwenController {

    private final QwenService qwenService;

    /**
     * 构造函数，通过依赖注入获取QwenService实例
     * @param qwenService QwenService实例
     */
    @Autowired
    public QwenController(QwenService qwenService) {
        this.qwenService = qwenService;
    }

    /**
     * 提示词请求实体类
     */
    @Data
    static class PromptRequest {
        private String prompt;
    }

    /**
     * 响应实体类
     */
    @Data
    static class Response {
        private String result;

        public Response(String result) {
            this.result = result;
        }
    }

    /**
     * 处理POST请求，接收提示词并返回模型响应
     * @param request 包含提示词的请求体
     * @return 包含模型响应的ResponseEntity
     */
    @PostMapping("/chat")
    public ResponseEntity<Response> chat(@RequestBody PromptRequest request) {
        // 检查提示词是否为空
        if (request == null || request.getPrompt() == null || request.getPrompt().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new Response("提示词不能为空"));
        }

        try {
            // 调用QwenService获取模型响应
            String response = qwenService.getResponse(request.getPrompt());
            return ResponseEntity.ok(new Response(response));
        } catch (Exception e) {
            // 捕获异常并返回错误信息
            return ResponseEntity.status(500).body(new Response("请求处理失败: " + e.getMessage()));
        }
    }
    @PostMapping("/aiservicechat")
    public ResponseEntity<Response> aiServiceChat(@RequestBody PromptRequest request) {
        ResponseEntity<Response> checkRlt = check(request);
        if (checkRlt != null) {
            return checkRlt;
        }

        try {
            // 调用QwenService获取模型响应
            String response = qwenService.aiServiceChat(request.getPrompt());
            return ResponseEntity.ok(new Response(response));
        } catch (Exception e) {
            // 捕获异常并返回错误信息
            return ResponseEntity.status(500).body(new Response("请求处理失败: " + e.getMessage()));
        }
    }

    private ResponseEntity<Response> check(PromptRequest request) {
        if (request == null || request.getPrompt() == null || request.getPrompt().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new Response("提示词不能为空"));
        }
        return null;
    }
}