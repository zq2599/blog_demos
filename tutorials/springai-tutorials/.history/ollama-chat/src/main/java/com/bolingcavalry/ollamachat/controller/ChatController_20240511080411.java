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
	public String streamOutput(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        Prompt prompt = new Prompt(new UserMessage(message));
        Flux<ChatResponse> resp = chatClient.stream(prompt);
        resp.subscribe(new Consumer<ChatResponse>() {
            @Override
            public void accept(ChatResponse t) {
                System.out.println(t);
            }
        });


        return "ok";
    }
}