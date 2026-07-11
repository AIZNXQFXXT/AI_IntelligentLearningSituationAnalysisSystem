package com.campus.backend.eneity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TaskRecord {
    private int id;
    private String taskType;
    private String status;
    private int progress;
    private int currentCount;
    private int totalCount;
    private String fileUrl;
    private String resultJson;
    private int createdBy;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isDeleted;
}
