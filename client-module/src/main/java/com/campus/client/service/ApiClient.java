package com.campus.client.service;

import com.campus.client.model.ApiResponse;
import com.campus.client.session.UserSession;
import com.campus.client.util.CrudHelper;
import com.campus.client.util.ViewLoader;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;

import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class ApiClient {
    private static final String BASE_URL = "http://localhost:8080/api";
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static HttpRequest.Builder buildRequest(String path) {
        String token = UserSession.getInstance().getToken();
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json");
        if (token != null && !token.isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
        }
        return builder;
    }

    private static void checkAuth(HttpResponse<String> response) throws Exception {
        if (response.statusCode() == 401 || response.statusCode() == 403) {
            Platform.runLater(() -> {
                UserSession.getInstance().logout();
                CrudHelper.showError("登录已过期，请重新登录");
                ViewLoader.loadScene("/fxml/LoginView.fxml", "AI智能校园学情分析系统 - 登录");
            });
            throw new RuntimeException("登录已过期，请重新登录");
        }
    }

    private static <T> T handleResponse(HttpResponse<String> response, TypeReference<ApiResponse<T>> typeRef) throws Exception {
        ApiResponse<T> apiResponse = MAPPER.readValue(response.body(), typeRef);
        if (apiResponse.isSuccess()) {
            return apiResponse.getData();
        }
        throw new RuntimeException(apiResponse.getMsg());
    }

    public static <T> T get(String path, TypeReference<ApiResponse<T>> typeRef) throws Exception {
        HttpRequest request = buildRequest(path).GET().build();
        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        checkAuth(response);
        return handleResponse(response, typeRef);
    }

    public static <T> T post(String path, Object body, TypeReference<ApiResponse<T>> typeRef) throws Exception {
        String json = MAPPER.writeValueAsString(body);
        HttpRequest request = buildRequest(path)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        checkAuth(response);
        return handleResponse(response, typeRef);
    }

    public static <T> T put(String path, Object body, TypeReference<ApiResponse<T>> typeRef) throws Exception {
        String json = MAPPER.writeValueAsString(body);
        HttpRequest request = buildRequest(path)
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        checkAuth(response);
        return handleResponse(response, typeRef);
    }

    public static <T> T patch(String path, Object body, TypeReference<ApiResponse<T>> typeRef) throws Exception {
        String json = body != null ? MAPPER.writeValueAsString(body) : "";
        HttpRequest request = buildRequest(path)
                .method("PATCH", HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        checkAuth(response);
        return handleResponse(response, typeRef);
    }

    public static <T> T delete(String path, TypeReference<ApiResponse<T>> typeRef) throws Exception {
        HttpRequest request = buildRequest(path).DELETE().build();
        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        checkAuth(response);
        return handleResponse(response, typeRef);
    }

    public static <T> T upload(String path, File file, int examId, int classId, TypeReference<ApiResponse<T>> typeRef) throws Exception {
        String boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW";
        String CRLF = "\r\n";

        StringBuilder bodyBuilder = new StringBuilder();
        bodyBuilder.append("--").append(boundary).append(CRLF);
        bodyBuilder.append("Content-Disposition: form-data; name=\"examId\"").append(CRLF).append(CRLF);
        bodyBuilder.append(examId).append(CRLF);
        bodyBuilder.append("--").append(boundary).append(CRLF);
        bodyBuilder.append("Content-Disposition: form-data; name=\"classId\"").append(CRLF).append(CRLF);
        bodyBuilder.append(classId).append(CRLF);
        bodyBuilder.append("--").append(boundary).append(CRLF);
        bodyBuilder.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(file.getName()).append("\"").append(CRLF);
        bodyBuilder.append("Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet").append(CRLF).append(CRLF);

        byte[] headerBytes = bodyBuilder.toString().getBytes();
        byte[] fileBytes = java.nio.file.Files.readAllBytes(file.toPath());
        byte[] footer = (CRLF + "--" + boundary + "--" + CRLF).getBytes();

        byte[] fullBody = new byte[headerBytes.length + fileBytes.length + footer.length];
        System.arraycopy(headerBytes, 0, fullBody, 0, headerBytes.length);
        System.arraycopy(fileBytes, 0, fullBody, headerBytes.length, fileBytes.length);
        System.arraycopy(footer, 0, fullBody, headerBytes.length + fileBytes.length, footer.length);

        String token = UserSession.getInstance().getToken();
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary);
        if (token != null && !token.isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
        }
        HttpRequest request = builder.POST(HttpRequest.BodyPublishers.ofByteArray(fullBody)).build();
        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        checkAuth(response);
        return handleResponse(response, typeRef);
    }

    public static <T> T uploadFile(String path, File file, TypeReference<ApiResponse<T>> typeRef) throws Exception {
        String boundary = "----FormBoundary" + System.currentTimeMillis();
        String CRLF = "\r\n";
        byte[] fileBytes = java.nio.file.Files.readAllBytes(file.toPath());
        String fileName = file.getName();

        String header = "--" + boundary + CRLF
                + "Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"" + CRLF
                + "Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" + CRLF + CRLF;
        byte[] footer = (CRLF + "--" + boundary + "--" + CRLF).getBytes();

        byte[] fullBody = new byte[header.getBytes().length + fileBytes.length + footer.length];
        System.arraycopy(header.getBytes(), 0, fullBody, 0, header.getBytes().length);
        System.arraycopy(fileBytes, 0, fullBody, header.getBytes().length, fileBytes.length);
        System.arraycopy(footer, 0, fullBody, header.getBytes().length + fileBytes.length, footer.length);

        String token = UserSession.getInstance().getToken();
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary);
        if (token != null && !token.isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
        }
        HttpRequest request = builder.POST(HttpRequest.BodyPublishers.ofByteArray(fullBody)).build();
        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        checkAuth(response);
        return handleResponse(response, typeRef);
    }

    public static byte[] downloadBytes(String path) throws Exception {
        HttpRequest request = buildRequest(path)
                .header("Content-Type", "application/octet-stream")
                .GET().build();
        HttpResponse<byte[]> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() == 401 || response.statusCode() == 403) {
            Platform.runLater(() -> {
                UserSession.getInstance().logout();
                CrudHelper.showError("登录已过期，请重新登录");
                ViewLoader.loadScene("/fxml/LoginView.fxml", "AI智能校园学情分析系统 - 登录");
            });
            throw new RuntimeException("登录已过期，请重新登录");
        }
        if (response.statusCode() != 200) {
            throw new RuntimeException("请求失败: HTTP " + response.statusCode());
        }
        return response.body();
    }

    public static String encodeParam(String value) {
        if (value == null || value.isEmpty()) return "";
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public static ObjectMapper getMapper() { return MAPPER; }
}
