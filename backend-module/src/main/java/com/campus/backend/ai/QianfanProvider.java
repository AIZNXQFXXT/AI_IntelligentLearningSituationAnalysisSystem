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
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class QianfanProvider implements AiService {

    private static final String PROVIDER_NAME = "Qianfan";

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
            body.put("model", aiProperties.getQianfanModel());
            body.put("temperature", request.getTemperature() != null ? request.getTemperature() : 0.7);
            body.put("max_tokens", request.getMaxTokens() != null ? request.getMaxTokens() : 2048);

            ArrayNode messages = objectMapper.createArrayNode();
            ObjectNode userMsg = objectMapper.createObjectNode();
            userMsg.put("role", "user");
            userMsg.put("content", request.getPrompt());
            messages.add(userMsg);
            body.set("messages", messages);

            Request httpRequest = new Request.Builder()
                    .url(aiProperties.getQianfanBaseUrl() + "/chat/completions")
                    .addHeader("Authorization", "Bearer " + aiProperties.getQianfanApiKey())
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
                    .build();

            try (Response response = httpClient.newCall(httpRequest).execute()) {
                long duration = System.currentTimeMillis() - start;
                if (!response.isSuccessful()) {
                    return AiResult.error("Qianfan API error: HTTP " + response.code(), duration);
                }
                String respBody = response.body() != null ? response.body().string() : "";
                JsonNode json = objectMapper.readTree(respBody);

                String content = json.get("choices").get(0).get("message").get("content").asText();

                JsonNode usage = json.get("usage");
                int inputTokens = usage.get("prompt_tokens").asInt();
                int outputTokens = usage.get("completion_tokens").asInt();

                return AiResult.success(content, inputTokens, outputTokens, duration);
            }
        } catch (SocketTimeoutException e) {
            long duration = System.currentTimeMillis() - start;
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("connect")) {
                log.warn("{} connect timeout", PROVIDER_NAME);
                return AiResult.connectionTimeout("connect timeout", duration);
            }
            log.error("{} read/call timeout", PROVIDER_NAME, e);
            return AiResult.error(PROVIDER_NAME + " network error: " + e.getMessage(), duration);
        } catch (IOException e) {
            long duration = System.currentTimeMillis() - start;
            log.error("{} call failed", PROVIDER_NAME, e);
            return AiResult.error(PROVIDER_NAME + " network error: " + e.getMessage(), duration);
        }
    }
}
