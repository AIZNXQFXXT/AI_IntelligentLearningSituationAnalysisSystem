package com.campus.backend.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Teacher {
    private int id;
    private int userId;
    private String teacherNo;
    private String name;
    private String title;
    private String subject;
    private String education;
    private String department;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isDeleted;
}