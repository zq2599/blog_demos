package com.bolingcavalry.helloopenai.controller;

import org.springframework.ai.chat.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


import java.util.Map;

@RestController
public class SimpleAiController {

	private final ChatClient chatClient;

	@Autowired
	public SimpleAiController(ChatClient chatClient) {
		this.chatClient = chatClient;
	}

	@PostMapping("/ai/simple")
	public Map<String, String> completion(@RequestBody Map<String,String> map) {
		return Map.of("generation", chatClient.call(map.get("message")));
	}
}