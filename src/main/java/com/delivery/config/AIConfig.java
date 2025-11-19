package com.delivery.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfig {

    @Bean
    public OllamaApi ollamaApi() {
        return new OllamaApi("http://localhost:11434");
    }

    @Bean
    public OllamaChatModel ollamaChatModel(OllamaApi ollamaApi) {
        return new OllamaChatModel(ollamaApi,
                OllamaOptions.create()
                        .withModel("gemma3:4b")
                        .withTemperature(0.3f)
        );
    }

    @Bean
    public ChatClient chatClient(OllamaChatModel ollamaChatModel) {
        return ChatClient.builder(ollamaChatModel)
                .build();
    }

    @Bean
    public String testAIConnection(ChatClient chatClient) {
        try {
            String response = chatClient.prompt()
                    .user("Bonjour! Répondez avec 'OK' si vous fonctionnez correctement.")
                    .call()
                    .content();

            System.out.println("Ollama connection successful: " + response);
            return response;
        } catch (Exception e) {
            System.err.println("Ollama connection failed: " + e.getMessage());
            System.err.println("Please ensure Ollama is running on http://localhost:11434");
            // Ne pas lancer d'exception pour permettre le fallback
            return "AI service unavailable - using fallback optimizers";
        }
    }
}