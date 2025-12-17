package com.muscledia.muscledia_ai_service.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.muscledia.muscledia_ai_service.dto.PreferencesDto;
import com.muscledia.muscledia_ai_service.dto.UserData;
import com.muscledia.muscledia_ai_service.dto.UserDataDTO;
import com.muscledia.muscledia_ai_service.exception.OllamaException.OllamaException;
import com.muscledia.muscledia_ai_service.function.PublicRoutinesFunction;
import com.muscledia.muscledia_ai_service.model.Answer;
import com.muscledia.muscledia_ai_service.model.Question;
import com.muscledia.muscledia_ai_service.model.WorkoutRecommendation;
import com.muscledia.muscledia_ai_service.util.AiPromptLoader;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class OllamaServiceImpl implements OllamaService {
    private final ChatClient memoryChatClient;
    private final ChatClient statelessChatClient;
    //private final ChatMemory chatMemory;
    private final ObjectMapper objectMapper;
    private final PublicRoutinesFunction publicRoutinesFunction;
    private final UserServiceClient userServiceClient;
    protected static final Logger logger = LogManager.getLogger();

    public OllamaServiceImpl(
            ChatClient.Builder builder,
            ObjectMapper objectMapper,
            PublicRoutinesFunction publicRoutinesFunction,
            UserServiceClient userServiceClient) {
        this.objectMapper = objectMapper;
        this.publicRoutinesFunction = publicRoutinesFunction;
        this.userServiceClient = userServiceClient;
//        this.chatMemory = MessageWindowChatMemory.builder()
//                .chatMemoryRepository(new InMemoryChatMemoryRepository())
//                .maxMessages(20)
//                .build();

        String conversationSystemPrompt = AiPromptLoader.loadPrompt("assistant_role.txt");

        // Build memoryChatClient with only conversation system prompt - no structured output
        // This client is used for general conversations and should NOT return structured JSON
        this.memoryChatClient = builder
                //.defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                //.defaultSystem(conversationSystemPrompt)
                .build();

        // Build statelessChatClient with structured output for workout recommendations
        // This client is used ONLY for structured output requests
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
            String conversationSystemPrompt = AiPromptLoader.loadPrompt("assistant_role.txt");
            String response = this.memoryChatClient.prompt()
                    .user(question.question())
                    .system(conversationSystemPrompt)
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
    public WorkoutRecommendation getStructuredAnswer(PreferencesDto preferences, String jwtToken) {
        if (preferences == null) {
            throw new IllegalArgumentException("Preferences cannot be null");
        }
        if (jwtToken == null || jwtToken.trim().isEmpty()) {
            throw new IllegalArgumentException("JWT token is required to retrieve user data");
        }
        try {
            logger.info("Starting structured answer generation");

            // Call user-service and retrieve user information from JWT token
            logger.info("Fetching user data from user-service using JWT token");
            UserDataDTO userDataDTO = userServiceClient.getUserData(jwtToken);
            
            // Convert UserDataDTO to UserData (userId is Long in DTO, String in UserData)
            UserData userData = UserData.of(
                    String.valueOf(userDataDTO.getUserId()),
                    userDataDTO.getHeight() != null ? userDataDTO.getHeight() : 0.0,
                    userDataDTO.getWeight() != null ? userDataDTO.getWeight() : 0.0,
                    userDataDTO.getGoalType() != null ? userDataDTO.getGoalType() : "BUILD_MUSCLE",
                    userDataDTO.getGender() != null ? userDataDTO.getGender() : "MALE",
                    userDataDTO.getAge() != null ? userDataDTO.getAge() : 25
            );
            
            logger.info("Retrieved user data for userId: {}, height: {}, weight: {}, goal: {}", 
                    userData.userId(), userData.height(), userData.weight(), userData.goalType());

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
                userData.userId(),
                userData.height(),
                userData.weight(),
                userData.age(),
                userData.gender(),
                userData.goalType(),
                preferences.frequency(),
                preferences.lvlOfTraining()
            );

            // Get filtered public routines using the function service based on training level
            // This significantly reduces token usage by filtering server-side before including in prompt
            logger.info("Retrieving filtered public routines via function service for training level: {}", preferences.lvlOfTraining());
            String publicRoutinesJson = publicRoutinesFunction.getFilteredPublicRoutinesJson(preferences.lvlOfTraining());
            
            logger.info("Filtered routines JSON length: {} characters (reduced from full dataset)", publicRoutinesJson.length());
            
            // Create prompt with user context and filtered routines
            // The function service has already filtered routines, reducing token usage significantly
            String promptText = String.format("""
                %s
                
                Available Workout Routines (filtered by your training level):
                %s
                
                Based on the user profile and training preferences, recommend the most suitable workout routine from the available routines.
                Return the recommendation as JSON with the following structure:
                {
                    "suggestedWorkoutRoutine": "routine title/name",
                    "routineId": "routine id from the available routines",
                    "description": "brief description of why this routine is suitable and how to perform it",
                    "difficultyLevel": "difficulty level of the routine",
                    "workoutSplit": "workout split type"
                }
                """, userContext, publicRoutinesJson);

            logger.info("Calling statelessChatClient for structured output");
            String response = this.statelessChatClient.prompt()
                    .user(promptText)
                    .call()
                    .content();

            // Parse the JSON response to WorkoutRecommendation
            // Remove markdown code blocks if present
            String cleanedResponse = response.trim();
            if (cleanedResponse.startsWith("```json")) {
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
            logger.info("Successfully generated workout recommendation");

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