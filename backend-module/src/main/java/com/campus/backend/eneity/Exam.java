package com.campus.backend.eneity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Exam {
    private int id;
    private String name;
    private String type;
    private String semester;
    private int classId;
    private LocalDateTime examDate;
    private int isArchived;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isDeleted;
}
