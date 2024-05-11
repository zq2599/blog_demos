/*
 * @Author: zq2599 zq2599@gmail.com
 * @Date: 2024-05-05 23:24:44
 * @LastEditors: zq2599 zq2599@gmail.com
 * @LastEditTime: 2024-05-11 08:07:29
 * @FilePath: /springai-tutorials/ollama-chat/src/main/java/com/bolingcavalry/ollamachat/controller/ChatController.java
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
package com.bolingcavalry.ollamachat.controller;

import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.ai.chat.messages.UserMessage;

import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.function.Consumer;

@RestController
public class ChatController {

    private final OllamaChatClient chatClient;

    public ChatController(OllamaChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping("/ai/generate")
    public Map<String,String> generate(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        return Map.of("generation", chatClient.call(message));
    }

    @GetMapping("/ai/generateStream")
	public Flux<ChatResponse> generateStream(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        Prompt prompt = new Prompt(new UserMessage(message));
        return chatClient.stream(prompt);
    }
    @GetMapping("/ai/streamoutput")
	public String streamOutput(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) throws InterruptedException {
        Prompt prompt = new Prompt(new UserMessage(message));
        Flux<ChatResponse> resp = chatClient.stream(prompt);
        resp.subscribe(new Consumer<ChatResponse>() {
            @Override
            public void accept(ChatResponse t) {
                System.out.print(t.getResult().getOutput().getContent());
            }
        });

        Thread.sleep(5000);

        return "ok";
    }
}