package com.aicampus.service;

import com.aicampus.model.ApiResponse;
import com.aicampus.model.PageResult;
import com.aicampus.model.Score;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;

public class ScoreService {
    public static PageResult<Score> getScorePage(int page, int size) throws Exception {
        return ApiClient.get("/scores?page=" + page + "&size=" + size,
                new TypeReference<ApiResponse<PageResult<Score>>>() {});
    }

    public static PageResult<Score> getScorePage(int page, int size, int studentId) throws Exception {
        return ApiClient.get("/scores?page=" + page + "&size=" + size + "&studentId=" + studentId,
                new TypeReference<ApiResponse<PageResult<Score>>>() {});
    }

    public static String batchImportScore(File file, int examId, int classId) throws Exception {
        return ApiClient.upload("/scores/import", file, examId, classId,
                new TypeReference<ApiResponse<String>>() {});
    }

    public static Void createScore(int studentId, int examId, int courseId, double regularScore, double examScore) throws Exception {
        Score score = new Score();
        score.setStudentId(studentId);
        score.setExamId(examId);
        score.setCourseId(courseId);
        score.setRegularScore(regularScore);
        score.setExamScore(examScore);
        return ApiClient.post("/scores", score, new TypeReference<ApiResponse<Void>>() {});
    }
}
