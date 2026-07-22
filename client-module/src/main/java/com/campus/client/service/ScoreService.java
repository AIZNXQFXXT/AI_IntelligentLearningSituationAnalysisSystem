package com.aicampus.service;

import com.aicampus.model.ApiResponse;
import com.aicampus.model.PageResult;
import com.aicampus.model.Score;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoreService {
    public static PageResult<Score> getScorePage(int page, int size) throws Exception {
        return ApiClient.get("/scores?page=" + page + "&size=" + size,
                new TypeReference<ApiResponse<PageResult<Score>>>() {});
    }

    public static PageResult<Score> getScorePage(int page, int size, int studentId) throws Exception {
        return ApiClient.get("/scores?page=" + page + "&size=" + size + "&studentId=" + studentId,
                new TypeReference<ApiResponse<PageResult<Score>>>() {});
    }

    public static PageResult<Score> getMyScores(int page, int size) throws Exception {
        return ApiClient.get("/my/scores?page=" + page + "&size=" + size,
                new TypeReference<ApiResponse<PageResult<Score>>>() {});
    }

    public static PageResult<Score> getMyScores(int page, int size, String semester) throws Exception {
        String path = "/my/scores?page=" + page + "&size=" + size;
        if (semester != null && !semester.isEmpty()) {
            path += "&semester=" + ApiClient.encodeParam(semester);
        }
        return ApiClient.get(path, new TypeReference<ApiResponse<PageResult<Score>>>() {});
    }

    public static String batchImportScore(File file, int examId, int classId) throws Exception {
        return ApiClient.upload("/scores/batch", file, examId, classId,
                new TypeReference<ApiResponse<String>>() {});
    }

    public static Score createScore(int studentId, int examId, int courseId, double regularScore, double examScore, double finalScore) throws Exception {
        Score score = new Score();
        score.setStudentId(studentId);
        score.setExamId(examId);
        score.setCourseId(courseId);
        score.setRegularScore(regularScore);
        score.setExamScore(examScore);
        score.setFinalScore(finalScore);
        return ApiClient.post("/scores", score, new TypeReference<ApiResponse<Score>>() {});
    }

    public static List<Score> getScoresByExamAndCourse(int examId, int courseId) throws Exception {
        return ApiClient.get("/scores?examId=" + examId + "&courseId=" + courseId + "&size=5000",
                new TypeReference<ApiResponse<PageResult<Score>>>() {}).getRecords();
    }

    public static Score updateScore(int id, double regularScore, double examScore, double finalScore, String reason) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("regularScore", regularScore);
        body.put("examScore", examScore);
        body.put("finalScore", finalScore);
        body.put("reason", reason);
        return ApiClient.put("/scores/" + id, body, new TypeReference<ApiResponse<Score>>() {});
    }

    public static Void archiveScore(int id) throws Exception {
        return ApiClient.put("/scores/" + id + "/status",
                Map.of("status", 1), new TypeReference<ApiResponse<Void>>() {});
    }
}
