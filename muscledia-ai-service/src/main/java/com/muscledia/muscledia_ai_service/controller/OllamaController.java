package com.muscledia.muscledia_ai_service.controller;


import com.muscledia.muscledia_ai_service.model.Answer;
import com.muscledia.muscledia_ai_service.model.Question;
import com.muscledia.muscledia_ai_service.service.OllamaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;

@RestController
@RequestMapping("/ollama")
@CrossOrigin("*")
@Tag(name = "Ollama API", description = "Operations related to the Ollama model")
public class OllamaController {

    private final OllamaService ollamaService;

    public OllamaController(final OllamaService ollamaService) {
        this.ollamaService = ollamaService;
    }

    @PostMapping("/ask")
    @Operation(summary = "Ask a question to Ollama model and get an answer")
    public ResponseEntity<Answer> getAnswer(@Valid @RequestBody Question question) {
        try {
            Answer answer = ollamaService.getGeneralAnswer(question);
            return ResponseEntity.ok(answer);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to get response from Ollama", e);
        }
    }


    @PostMapping("/structuredOutput")
    @Operation(summary = "Ask a question to Ollama model and get an answer")
    public ResponseEntity<Answer> getStructuredAnswer(@Valid @RequestBody Question question) {
        try {
            Answer answer = ollamaService.getGeneralAnswer(question);      //change
            return ResponseEntity.ok(answer);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to get response from Ollama", e);
        }
    }
}
