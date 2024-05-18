/*
 * @Author: zq2599 zq2599@gmail.com
 * @Date: 2024-05-05 23:24:44
 * @LastEditors: zq2599 zq2599@gmail.com
 * @LastEditTime: 2024-05-11 08:12:18
 * @FilePath: /springai-tutorials/ollama-chat/src/main/java/com/bolingcavalry/ollamachat/controller/ChatController.java
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
package com.bolingcavalry.ollamachat.controller;

import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.ai.chat.messages.UserMessage;

import reactor.core.publisher.Flux;

@RestController
public class ChatController {

    private final OllamaChatClient chatClient;


    public ChatController(OllamaChatClient chatClient) {
        // 依赖注入ollama的客户端类
        this.chatClient = chatClient;
    }

    @GetMapping(value = "/ai/streamresp", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<String> streamResp(@RequestParam(value = "message", defaultValue = "Hello!") String message) throws InterruptedException {
        // 提示词类，包裹了用户发来的问题
        Prompt prompt = new Prompt(new UserMessage(message));
        // 调用ollama的客户端类的API，将问题发到ollama，并获取流对象，Ollama响应的数据就会通过这个流对象持续输出
        Flux<ChatResponse> chatResp = chatClient.stream(prompt);
        // ollama客户端返回的数据包含了多个内容，例如system，assistant等，需要做一次变换，取大模型的回答内容返回给前端
        return chatResp.map(chatObj -> chatObj.getResult().getOutput().getContent());
    }
}