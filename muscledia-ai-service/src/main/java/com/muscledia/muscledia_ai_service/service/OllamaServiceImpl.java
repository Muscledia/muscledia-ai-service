package com.muscledia.muscledia_ai_service.service;



import com.fasterxml.jackson.databind.ObjectMapper;
import com.muscledia.muscledia_ai_service.exception.OllamaException.OllamaException;
import com.muscledia.muscledia_ai_service.model.Answer;
import com.muscledia.muscledia_ai_service.model.Question;
import com.muscledia.muscledia_ai_service.model.WorkoutRecommendation;
import com.muscledia.muscledia_ai_service.model.WorkoutRoutineRequest;
import com.muscledia.muscledia_ai_service.util.AiPromptLoader;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
// import org.springframework.ai.model.function.FunctionCallback;
// import org.springframework.ai.model.function.FunctionCallbackWrapper;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.function.Function;


@Service
public class OllamaServiceImpl implements OllamaService {
    private final ChatClient memoryChatClient;
    private final ChatClient statelessChatClient;
    private final ChatMemory chatMemory;
    private final ObjectMapper objectMapper;

    public OllamaServiceImpl(ChatClient.Builder builder, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(20)
                .build();

        String conversationSystemPrompt = AiPromptLoader.loadPrompt("assistant_role.txt");

        this.memoryChatClient = builder
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultSystem(conversationSystemPrompt)
                .build();

        this.statelessChatClient = builder
                .defaultSystem("""
                You are a workout routines information service that returns structured data.
                Always respond with valid JSON that matches the requested schema.
                """)
                .build();
    }

    @Override
    public Answer getGeneralAnswer(Question question) {
        if (question == null || question.question().trim().isEmpty()) {
            throw new IllegalArgumentException("Question cannot be null");
        }
        try {
            String response = this.memoryChatClient.prompt()
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


    @Override
    public WorkoutRecommendation getStructuredAnswer(WorkoutRoutineRequest request) {
        if (request == null || request.userData() == null || request.preferences() == null) {
            throw new IllegalArgumentException("Request, userData, and preferences cannot be null");
        }
        try {
            // Load public routines JSON as context
            String publicRoutinesJson = AiPromptLoader.loadJsonData("public_routines.json");
            
            // Build user context prompt
            String userContext = String.format("""
                User Profile:
                - User ID: %s
                - Height: %.2f cm
                - Weight: %.2f kg
                - Age: %d years
                - Gender: %s
                - Goal: %s
                
                Training Preferences:
                - Frequency: %d times per week
                - Training Level: %s
                """,
                request.userData().userId(),
                request.userData().height(),
                request.userData().weight(),
                request.userData().age(),
                request.userData().gender(),
                request.userData().goalType(),
                request.preferences().frequency(),
                request.preferences().lvlOfTraining()
            );

            // Create prompt with context
            String promptText = String.format("""
                %s
                
                Available Workout Routines:
                %s
                
                Based on the user profile and training preferences, recommend the most suitable workout routine from the available routines.
                Return the recommendation as JSON with the following structure:
                {
                    "suggestedWorkoutRoutine": "routine title/name",
                    "routineId": "routine id from the available routines",
                    "description": "brief description of why this routine is suitable and how to perform it",
                    "difficultyLevel": "difficulty level of the routine",
                    "equipmentType": "equipment type required",
                    "workoutSplit": "workout split type"
                }
                """, userContext, publicRoutinesJson);

            // Use structured output with Spring AI
            // Spring AI 1.0.3 supports structured output through ChatClient
            String response = this.statelessChatClient.prompt()
                    .user(promptText)
                    .call()
                    .content();

            // Parse the JSON response to WorkoutRecommendation
            // Remove markdown code blocks if present
            String cleanedResponse = response.trim();
            if (cleanedResponse.startsWith("")) {
                cleanedResponse = cleanedResponse.substring(7);
            }
            if (cleanedResponse.startsWith("```")) {
                cleanedResponse = cleanedResponse.substring(3);
            }
            if (cleanedResponse.endsWith("```")) {
                cleanedResponse = cleanedResponse.substring(0, cleanedResponse.length() - 3);
            }
            cleanedResponse = cleanedResponse.trim();

            WorkoutRecommendation recommendation = objectMapper.readValue(
                cleanedResponse,
                WorkoutRecommendation.class
            );

            return recommendation;
        }
        catch (RuntimeException e) {
            throw new OllamaException(String.format("Error while calling Ollama API: %s", e.getMessage()), e);
        }
        catch (Exception e) {
            throw new OllamaException(String.format("Error while processing structured output: %s", e.getMessage()), e);
        }
    }
}