package com.campus.client.service;

import java.io.IOException;

public class ReportService {

    private final ApiService api = new ApiService();

    public byte[] exportScoreTable(Long classId, Long examId) throws IOException {
        StringBuilder path = new StringBuilder("/api/reports/excel/score-table?");
        if (classId != null) path.append("classId=").append(classId);
        if (examId != null) path.append("&examId=").append(examId);
        return api.getBytes(path.toString());
    }

    public byte[] exportComments(Long classId, Long courseId) throws IOException {
        StringBuilder path = new StringBuilder("/api/reports/excel/comments?");
        if (classId != null) path.append("classId=").append(classId);
        if (courseId != null) path.append("&courseId=").append(courseId);
        return api.getBytes(path.toString());
    }

    public byte[] exportRiskList(Long classId) throws IOException {
        StringBuilder path = new StringBuilder("/api/reports/excel/risk-list?");
        if (classId != null) path.append("classId=").append(classId);
        return api.getBytes(path.toString());
    }

    public byte[] exportStats(Long classId) throws IOException {
        StringBuilder path = new StringBuilder("/api/reports/excel/stats?");
        if (classId != null) path.append("classId=").append(classId);
        return api.getBytes(path.toString());
    }
}
