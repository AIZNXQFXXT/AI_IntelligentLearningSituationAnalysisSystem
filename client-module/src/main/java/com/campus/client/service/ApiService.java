package com.campus.client.service;

import com.campus.client.util.SessionManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ApiService {

    private static final String BASE_URL = "http://localhost:8080";
    private static final MediaType JSON_TYPE = MediaType.parse("application/json; charset=utf-8");

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    private String buildUrl(String path, Map<String, String> params) {
        StringBuilder sb = new StringBuilder(BASE_URL + path);
        if (params != null && !params.isEmpty()) {
            sb.append("?");
            boolean first = true;
            for (Map.Entry<String, String> e : params.entrySet()) {
                if (!first) sb.append("&");
                sb.append(e.getKey()).append("=").append(e.getValue());
                first = false;
            }
        }
        return sb.toString();
    }

    private Request.Builder newRequest(String url) {
        Request.Builder builder = new Request.Builder().url(url);
        String token = SessionManager.getInstance().getToken();
        if (token != null && !token.isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
        }
        builder.header("Content-Type", "application/json");
        return builder;
    }

    public String get(String path, Map<String, String> params) throws IOException {
        String url = buildUrl(path, params);
        Request request = newRequest(url).get().build();
        try (Response response = CLIENT.newCall(request).execute()) {
            return response.body() != null ? response.body().string() : "";
        }
    }

    public String post(String path, Object body) throws IOException {
        String url = BASE_URL + path;
        String json = body != null ? MAPPER.writeValueAsString(body) : "{}";
        Request request = newRequest(url)
                .post(RequestBody.create(json, JSON_TYPE))
                .build();
        try (Response response = CLIENT.newCall(request).execute()) {
            return response.body() != null ? response.body().string() : "";
        }
    }

    public String put(String path, Object body) throws IOException {
        String url = BASE_URL + path;
        String json = body != null ? MAPPER.writeValueAsString(body) : "{}";
        Request request = newRequest(url)
                .put(RequestBody.create(json, JSON_TYPE))
                .build();
        try (Response response = CLIENT.newCall(request).execute()) {
            return response.body() != null ? response.body().string() : "";
        }
    }

    public String patch(String path, Object body) throws IOException {
        String url = BASE_URL + path;
        String json = body != null ? MAPPER.writeValueAsString(body) : "{}";
        Request request = newRequest(url)
                .patch(RequestBody.create(json, JSON_TYPE))
                .build();
        try (Response response = CLIENT.newCall(request).execute()) {
            return response.body() != null ? response.body().string() : "";
        }
    }

    public String delete(String path) throws IOException {
        String url = BASE_URL + path;
        Request request = newRequest(url).delete().build();
        try (Response response = CLIENT.newCall(request).execute()) {
            return response.body() != null ? response.body().string() : "";
        }
    }

    public byte[] getBytes(String path) throws IOException {
        String url = BASE_URL + path;
        Request request = newRequest(url).get().build();
        Response response = CLIENT.newCall(request).execute();
        if (response.body() != null) {
            byte[] bytes = response.body().bytes();
            response.close();
            return bytes;
        }
        response.close();
        return new byte[0];
    }

    public static ObjectMapper getMapper() {
        return MAPPER;
    }
}
