package com.campus.common.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ClassExportVO {
    private Long id;
    private String grade;
    private String className;
    private Integer studentCount;
    private String headTeacherName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
