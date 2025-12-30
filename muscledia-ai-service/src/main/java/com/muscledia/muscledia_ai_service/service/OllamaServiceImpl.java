package com.muscledia.muscledia_ai_service.service;

import com.muscledia.muscledia_ai_service.dto.UserData;
import com.muscledia.muscledia_ai_service.dto.PreferencesDto;
import com.muscledia.muscledia_ai_service.dto.UserDataDTO;
import com.muscledia.muscledia_ai_service.exception.OllamaException.OllamaException;
import com.muscledia.muscledia_ai_service.function.PublicRoutinesFunction;
import com.muscledia.muscledia_ai_service.model.Answer;
import com.muscledia.muscledia_ai_service.model.Question;
import com.muscledia.muscledia_ai_service.model.WorkoutRecommendation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Application service for AI operations
 * Responsibility: orchestrate calls between domain services
 * OPTIMIZED: Uses minimal data and optimized prompts for fast responses
 */
@Slf4j
@Service
public class OllamaServiceImpl implements OllamaService {

    private final ChatClient memoryChatClient;
    private final ChatClient statelessChatClient;
    private final PublicRoutinesFunction publicRoutinesFunction;
    private final UserServiceClient userServiceClient;
    private final UserDataMapper userDataMapper;
    private final AiPromptService aiPromptService;
    private final AiResponseParser aiResponseParser;
    private final ResourceLoaderService resourceLoaderService;

    public OllamaServiceImpl(
            ChatClient.Builder builder,
            PublicRoutinesFunction publicRoutinesFunction,
            UserServiceClient userServiceClient,
            UserDataMapper userDataMapper,
            AiPromptService aiPromptService,
            AiResponseParser aiResponseParser,
            ResourceLoaderService resourceLoaderService) {

        this.publicRoutinesFunction = publicRoutinesFunction;
        this.userServiceClient = userServiceClient;
        this.userDataMapper = userDataMapper;
        this.aiPromptService = aiPromptService;
        this.aiResponseParser = aiResponseParser;
        this.resourceLoaderService = resourceLoaderService;

        this.memoryChatClient = builder.build();

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
            String conversationSystemPrompt = resourceLoaderService.loadPrompt("assistant_role.txt");

            String response = this.memoryChatClient.prompt()
                    .user(question.question())
                    .system(conversationSystemPrompt)
                    .advisors()
                    .call()
                    .content();

            return new Answer(response);
        } catch (RuntimeException e) {
            throw new OllamaException(
                    String.format("Error while calling Ollama API: %s", e.getMessage()), e
            );
        }
    }


    @Override
    public WorkoutRecommendation getStructuredAnswer(PreferencesDto preferences, String jwtToken) {
        validateInput(preferences, jwtToken);

        try {
            log.info("Starting optimized recommendation generation");

            UserDataDTO userDataDTO = userServiceClient.getUserData(jwtToken);
            UserData userData = userDataMapper.toDomain(userDataDTO);
            log.info("Retrieved user data for userId: {}", userData.userId());

            String userContext = aiPromptService.buildUserContext(userData, preferences);

            log.info("Retrieving OPTIMIZED routines for level: {}", preferences.lvlOfTraining());
            String routinesJson = publicRoutinesFunction.getOptimizedRoutineSummaries(
                    preferences.lvlOfTraining()
            );
            log.info("Optimized routines: {} characters", routinesJson.length());

            String prompt = aiPromptService.buildRecommendationPrompt(userContext, routinesJson);

            log.info("Calling AI with optimized prompt");

            // OPTIMIZATION: Use entity method for structured output
            WorkoutRecommendation recommendation = this.statelessChatClient.prompt()
                    .user(prompt)
                    .call()
                    .entity(WorkoutRecommendation.class);

            log.info("Successfully generated recommendation");

            return recommendation;

        } catch (OllamaException e) {
            throw e;
        } catch (Exception e) {
            throw new OllamaException(
                    String.format("Error generating recommendation: %s", e.getMessage()), e
            );
        }
    }

    /**
     * Input validation
     */
    private void validateInput(PreferencesDto preferences, String jwtToken) {
        if (preferences == null) {
            throw new IllegalArgumentException("Preferences cannot be null");
        }
        if (jwtToken == null || jwtToken.trim().isEmpty()) {
            throw new IllegalArgumentException("JWT token is required");
        }
    }
}