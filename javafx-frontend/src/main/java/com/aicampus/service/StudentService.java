package com.aicampus.service;

import com.aicampus.model.ApiResponse;
import com.aicampus.model.PageResult;
import com.aicampus.model.Student;
import com.aicampus.session.UserSession;
import com.fasterxml.jackson.core.type.TypeReference;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;

public class StudentService {
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10)).build();

    public static PageResult<Student> getPage(int page, int size, String keyword, Integer classId) throws Exception {
        String path = "/students?page=" + page + "&size=" + size;
        if (keyword != null && !keyword.isEmpty()) {
            path += "&keyword=" + keyword;
        }
        if (classId != null) {
            path += "&classId=" + classId;
        }
        return ApiClient.get(path, new TypeReference<ApiResponse<PageResult<Student>>>() {});
    }

    public static PageResult<Student> getPage(int page, int size) throws Exception {
        return getPage(page, size, null, null);
    }

    public static Void create(Student student) throws Exception {
        return ApiClient.post("/students", student, new TypeReference<ApiResponse<Void>>() {});
    }

    public static Void update(int id, Student student) throws Exception {
        return ApiClient.put("/students/" + id, student, new TypeReference<ApiResponse<Void>>() {});
    }

    public static Void delete(int id) throws Exception {
        return ApiClient.delete("/students/" + id, new TypeReference<ApiResponse<Void>>() {});
    }

    public static Void batchImport(Path filePath) throws Exception {
        String token = UserSession.getInstance().getToken();
        String boundary = "----FormBoundary" + System.currentTimeMillis();
        byte[] fileBytes = java.nio.file.Files.readAllBytes(filePath);
        String fileName = filePath.getFileName().toString();

        String body = "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n"
                + "Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet\r\n\r\n"
                + new String(fileBytes, "ISO-8859-1") + "\r\n"
                + "--" + boundary + "--\r\n";

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/students/batch"))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(body));

        HttpResponse<String> response = HTTP_CLIENT.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        ApiResponse<Void> apiResponse = ApiClient.getMapper()
                .readValue(response.body(), new TypeReference<ApiResponse<Void>>() {});
        if (!apiResponse.isSuccess()) {
            throw new RuntimeException("Import failed: " + apiResponse.getMsg());
        }
        return null;
    }
}
