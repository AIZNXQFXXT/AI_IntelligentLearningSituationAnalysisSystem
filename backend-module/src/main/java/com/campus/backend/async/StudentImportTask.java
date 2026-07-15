package com.campus.backend.async;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.common.dto.StudentRow;
import com.campus.backend.entity.ClassInfo;
import com.campus.backend.mapper.ClassMapper;
import com.campus.backend.service.StudentService;
import com.campus.backend.service.TaskService;
import com.campus.common.dto.StudentDTO;
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
public class StudentImportTask implements Runnable {
    private final Long taskId;
    private final String fileUrl;
    private final StudentService studentService;
    private final TaskService taskService;
    private final ClassMapper classMapper;

    @Override
    public void run() {
        Path tempFile = null;
        try {
            tempFile = Path.of(URI.create(fileUrl));

            List<StudentRow> rows;
            try (InputStream is = Files.newInputStream(tempFile)) {
                rows = EasyExcel.read(is).head(StudentRow.class).sheet().doReadSync();
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
                    StudentDTO dto = convertRow(rows.get(i));
                    studentService.create(dto);
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
            }
        } finally {
            if (tempFile != null) {
                try { Files.deleteIfExists(tempFile); } catch (Exception ignored) {}
            }
        }
    }

    private StudentDTO convertRow(StudentRow row) {
        StudentDTO dto = new StudentDTO();
        dto.setStudentNo(row.getStudentNo());
        dto.setName(row.getName());
        dto.setGender(row.getGender());
        if (row.getClassName() != null && !row.getClassName().isEmpty()) {
            ClassInfo classInfo = classMapper.selectOne(
                    new LambdaQueryWrapper<ClassInfo>().eq(ClassInfo::getClassName, row.getClassName()));
            if (classInfo != null) {
                dto.setClassId(classInfo.getId());
            }
        }
        dto.setEnrollYear(row.getEnrollYear());
        dto.setPhone(row.getPhone());
        dto.setGuardianPhone(row.getGuardianPhone());
        return dto;
    }
}
