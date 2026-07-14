package com.campus.backend.async;

import com.alibaba.excel.EasyExcel;
import com.campus.common.dto.ScoreRow;
import com.campus.backend.entity.Student;
import com.campus.backend.service.ScoreService;
import com.campus.backend.service.StudentService;
import com.campus.backend.service.TaskService;
import com.campus.common.dto.ScoreDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class ScoreImportTask implements Runnable {
    private final Long taskId;
    private final String fileUrl;
    private final Long teacherId;
    private final Long examId;
    private final Long courseId;
    private final ScoreService scoreService;
    private final TaskService taskService;
    private final StudentService studentService;

    @Override
    public void run() {
        Path tempFile = null;
        try {
            tempFile = Path.of(URI.create(fileUrl));

            List<ScoreRow> rows;
            try (InputStream is = Files.newInputStream(tempFile)) {
                rows = EasyExcel.read(is).head(ScoreRow.class).sheet().doReadSync();
            }

            if (rows == null || rows.isEmpty()) {
                taskService.complete(taskId, "{\"success\":0,\"failed\":0,\"errors\":[]}");
                return;
            }

            taskService.updateProgress(taskId, 10, 0, rows.size());

            int success = 0, failed = 0;
            List<Map<String, Object>> errors = new ArrayList<>();
            for (int i = 0; i < rows.size(); i++) {
                try {
                    ScoreDTO dto = convertRow(rows.get(i));
                    scoreService.create(dto, teacherId);
                    success++;
                } catch (Exception e) {
                    failed++;
                    Map<String, Object> error = new HashMap<>();
                    error.put("row", i + 2);
                    error.put("reason", e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
                    errors.add(error);
                }
                if (i % 50 == 0 || i == rows.size() - 1) {
                    int progress = 10 + (int) ((long) (i + 1) * 80 / rows.size());
                    taskService.updateProgress(taskId, progress, success + failed, rows.size());
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            result.put("failed", failed);
            result.put("errors", errors);
            taskService.complete(taskId, new ObjectMapper().writeValueAsString(result));
        } catch (Throwable e) {
            try {
                taskService.fail(taskId, e.getClass().getName() + ": " + (e.getMessage() != null ? e.getMessage() : ""));
            } catch (Throwable ignored) {
                // fail() also failed — nothing more we can do
            }
        } finally {
            if (tempFile != null) {
                try { Files.deleteIfExists(tempFile); } catch (Exception ignored) {}
            }
        }
    }

    private ScoreDTO convertRow(ScoreRow row) {
        Student student = studentService.findByStudentNo(row.getStudentNo());
        if (student == null) {
            throw new RuntimeException("学号 " + row.getStudentNo() + " 不存在");
        }
        ScoreDTO dto = new ScoreDTO();
        dto.setStudentId(student.getId());
        dto.setExamId(this.examId);
        dto.setCourseId(this.courseId);
        dto.setRegularScore(row.getRegularScore());
        dto.setExamScore(row.getExamScore());
        dto.setFinalScore(row.getFinalScore());
        dto.setIsAbsent(row.getIsAbsent() != null ? row.getIsAbsent() : 0);
        dto.setIsCheat(row.getIsCheat() != null ? row.getIsCheat() : 0);
        return dto;
    }
}
