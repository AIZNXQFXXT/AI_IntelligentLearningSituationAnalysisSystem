package com.campus.backend.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ClassInfo {
    private int id;
    private String grade;
    private String className;
    private int headTeacherId;
    private int studentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isDeleted;
}