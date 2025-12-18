Guide on how to set up ai-service with docker

Open docker desktop  

Go to settings

Open AI tab


Select - Enable Docker Model Runner

Select - Enable host-side TCP connection with port 12434 and all Cors allowed

! If you have an Nvidia Graphics card with driver not lower than xxx you can enable it as well - it'll allow model to run on gpu!

This is docker model runner documentation: https://docs.docker.com/ai/model-runner/get-started/

<img width="1918" height="1090" alt="image" src="https://github.com/user-attachments/assets/07884008-002b-4577-9900-95d0af7a2b6e" /> 

In Docker Hub pull llama3.2:3B-Q4_K_M AI model

<img width="1917" height="1072" alt="image" src="https://github.com/user-attachments/assets/fec48671-2e27-413c-87ab-29f70181f88e" />
<img width="1918" height="1078" alt="image" src="https://github.com/user-attachments/assets/7d076ecb-9b00-4cc5-a33c-104d969fb1ea" />

After you pulled model it should appear in Models tab on left side panel

That's it, you can now run ai-service in docker using DMR!


## Muscledia AI Service

This document describes how the `muscledia-ai-service` works when it talks directly to an Ollama server, instead of using the Docker model runner (OpenAI-compatible proxy).


---

### 1. High-Level Overview

- **Service**: `muscledia-ai-service` (port `8084`)
- **Responsibility**:
  - Provide general conversational AI answers.
  - Provide **structured workout recommendations** as JSON, based on:
    - User profile data from `muscledia-user-service` (via JWT).
    - Public workout routines filtered by level (via `PublicRoutinesFunction`).
- **AI Backend**: **Ollama** (running locally or in its own container), exposing a REST API.
- **Client Library**: **Spring AI** via `ChatClient` (configured by `spring.ai.*` properties).

---

### 2. Current Code Responsibilities

- `OllamaServiceImpl`:
  - Uses two `ChatClient` instances:
    - `memoryChatClient`: general conversational answers (`getGeneralAnswer`).
    - `statelessChatClient`: structured JSON answers (`getStructuredAnswer`).
  - Loads system prompts from `src/main/resources/static/ai/assistant_role.txt`.
  - Builds a prompt with:
    - User profile (from `UserServiceClient.getUserData(jwt)`).
    - Filtered public routines JSON (from `PublicRoutinesFunction`).
  - Calls the AI model and:
    - For general Q&A: returns plain text in `Answer`.
    - For recommendations: expects JSON and deserializes into `WorkoutRecommendation`.
- **Security / integration**:
  - JWT validation and security filters (`JwtService`, `JwtAuthenticationWebFilter`, etc.).
  - `user-service` URL configured via `user-service.url`.


---

### 3. Ollama Prerequisites

- **Install Ollama** on the host:
  - Windows: install from the official Ollama site and ensure the service is running.
  - Default API endpoint: `http://localhost:11434`.
- **Pull the model** you want to use, for example:

 
  ollama pull llama3
   or a smaller model if needed, e.g.
  ollama pull llama3.2
  - **Run the model** (Ollama server auto-loads models on demand, but you can preload):

 
  ollama run llama3
    The AI service will call the Ollama HTTP API; no extra proxy container is required.

---

### 4. Spring AI Configuration for Ollama

#### 4.1. Key Idea

Instead of configuring Spring AI to talk to an OpenAI-compatible proxy at:

spring:
  ai:
    openai:
      base-url: http://localhost:12434/engines
      chat:
        options:
          model: ai/llama3.2:3B-Q4_K_M 

we configure it to talk **directly to Ollama** with port 11434.

There are two main patterns:

1. **Use Spring AI’s built-in Ollama support** (`spring.ai.ollama.*`).
2. **Use Spring AI in OpenAI-compatible mode** pointing to Ollama’s OpenAI-style endpoint (if you enable one).

The recommended approach for hosting it is **(1)** since not all linux instances support full functionality of Docker Desktop.

---

### 5. Request Flow (with Ollama)

The runtime request flow is the same as with the Docker model runner:

- **General Chat** (`getGeneralAnswer`):
  1. Client sends a question to `muscledia-ai-service` with a JWT.
  2. `OllamaServiceImpl.getGeneralAnswer`:
     - Loads `assistant_role.txt` as system prompt.
     - Uses `memoryChatClient` to call the model.
  3. Spring AI builds an Ollama chat request and sends it to `base-url`/`/api/chat` (via its internal Ollama client).
  4. Response content is returned as `Answer`.

- **Structured Recommendation** (`getStructuredAnswer`):
  1. Client sends preferences + JWT.
  2. `OllamaServiceImpl`:
     - Calls `user-service` to get `UserDataDTO` (via `UserServiceClient`).
     - Builds `UserData` + user context string.
     - Gets filtered routines JSON via `PublicRoutinesFunction`.
     - Constructs a prompt that **explicitly asks for JSON** with a fixed schema.
     - Sends prompt via `statelessChatClient` to Ollama.
  3. Response:
     - May contain JSON (possibly wrapped in markdown fences).
     - Implementation strips code fences and deserializes with `ObjectMapper` into `WorkoutRecommendation`.

This logic is **independent of whether the model is behind a Docker runner or native Ollama**, as long as Spring AI is correctly configured.

---

### 6. Error Handling Expectations with Ollama

- **Network / connectivity**:
  - Same exceptions as any HTTP-based call (`WebClient`/Reactor errors).
  - Wrapped into `OllamaException` in `OllamaServiceImpl`.
- **Model / parsing issues**:
  - If Ollama returns malformed JSON for structured responses:
    - `ObjectMapper.readValue` will throw and will be wrapped in `OllamaException`.
  - Keep logs enabled to inspect `cleanedResponse` when debugging.

---

### 7. Local Development Checklist

1. **Run Ollama** locally:
   - Install and start Ollama.
   - Pull the desired model: `ollama pull llama3`.
2. **Update `application.yaml`** for native Ollama.
3. **Start dependencies**:
   - `muscledia-user-service` (port `8081`).
   - Service discovery and other required services if used.
4. **Run `muscledia-ai-service`**:
   - Via IDE or Maven: `./mvnw spring-boot:run`.
5. **Test endpoints**:
   - General chat endpoint (controller method in `OllamaController`).
   - Structured recommendation endpoint with a valid JWT.

---
