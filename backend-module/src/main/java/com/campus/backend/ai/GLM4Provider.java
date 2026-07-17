package com.campus.backend.ai;

import com.campus.backend.config.AiProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "campus.ai.provider", havingValue = "glm4")
public class GLM4Provider implements AiService {

    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;
    private OkHttpClient httpClient;

    @PostConstruct
    public void init() {
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(aiProperties.getTimeoutConnect(), TimeUnit.MILLISECONDS)
                .readTimeout(aiProperties.getTimeoutRead(), TimeUnit.MILLISECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public AiResult call(AiRequest request) {
        long start = System.currentTimeMillis();
        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("model", aiProperties.getGlmModel());
            body.put("temperature", request.getTemperature() != null ? request.getTemperature() : 0.7);
            body.put("max_tokens", request.getMaxTokens() != null ? request.getMaxTokens() : 2048);

            ArrayNode messages = objectMapper.createArrayNode();
            ObjectNode userMsg = objectMapper.createObjectNode();
            userMsg.put("role", "user");
            userMsg.put("content", request.getPrompt());
            messages.add(userMsg);
            body.set("messages", messages);

            Request httpRequest = new Request.Builder()
                    .url(aiProperties.getGlmBaseUrl() + "/chat/completions")
                    .addHeader("Authorization", "Bearer " + aiProperties.getGlmApiKey())
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
                    .build();

            try (Response response = httpClient.newCall(httpRequest).execute()) {
                long duration = System.currentTimeMillis() - start;
                if (!response.isSuccessful()) {
                    return AiResult.error("GLM API error: HTTP " + response.code(), duration);
                }
                String respBody = response.body() != null ? response.body().string() : "";
                JsonNode json = objectMapper.readTree(respBody);

                String content = json.get("choices").get(0).get("message").get("content").asText();

                JsonNode usage = json.get("usage");
                int inputTokens = usage.get("prompt_tokens").asInt();
                int outputTokens = usage.get("completion_tokens").asInt();

                return AiResult.success(content, inputTokens, outputTokens, duration);
            }
        } catch (IOException e) {
            long duration = System.currentTimeMillis() - start;
            log.error("GLM call failed", e);
            return AiResult.error("GLM network error: " + e.getMessage(), duration);
        }
    }
}
