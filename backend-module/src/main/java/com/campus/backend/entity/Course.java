package com.campus.backend.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Course {
    private int id;
    private String name;
    private String type;
    private double credit;
    private String description;
    private int status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isDeleted;
}
