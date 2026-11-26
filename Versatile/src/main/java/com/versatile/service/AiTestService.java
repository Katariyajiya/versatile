package com.versatile.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AiTestService {
    private final ChatClient chatClient;

    public AiTestService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }
    public String generateSampleQuestion(String topic){
        return chatClient.prompt()
                .system("You are a strict technical interviewer. Ask exactly ONE concise interview question on the given topic. Do not provide the answer.")
                .user("Topic: " + topic)
                .call()
                .content();

    }
}
