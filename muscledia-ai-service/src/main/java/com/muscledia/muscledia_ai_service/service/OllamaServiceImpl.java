package com.muscledia.muscledia_ai_service.service;


import com.muscledia.muscledia_ai_service.exception.OllamaException.OllamaException;
import com.muscledia.muscledia_ai_service.model.Answer;
import com.muscledia.muscledia_ai_service.model.Question;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.stereotype.Service;

@Service
public class OllamaServiceImpl implements OllamaService {
    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    public OllamaServiceImpl(ChatClient.Builder builder) {
        this.chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(20)
                .build();
        this.chatClient = builder
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    @Override
    public Answer getAnswer(Question question) {
        if (question == null || question.question().trim().isEmpty()) {
            throw new IllegalArgumentException("Question cannot be null");
        }
        try {
            String response = this.chatClient.prompt()
                    .user(question.question())
                    .advisors()
                    .call()
                    .content();


            return new Answer(response);
        }
        catch (RuntimeException e) {
            throw new OllamaException(String.format("Error while calling Ollama API: %s", e.getMessage()), e);
        }
    }
}
