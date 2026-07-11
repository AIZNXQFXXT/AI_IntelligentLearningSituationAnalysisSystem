package com.campus.eneity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TeachingTask {
    private int id;
    private int teacherId;
    private int classId;
    private int courseId;
    private String semester;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isDeleted;
}
