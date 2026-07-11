package com.campus.backend.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class KnowledgePoint {
    private int id;
    private int parentId;
    private String name;
    private String subjectType;
    private int level;
    private int sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isDeleted;
}
