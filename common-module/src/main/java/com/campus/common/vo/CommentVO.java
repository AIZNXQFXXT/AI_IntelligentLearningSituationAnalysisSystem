package com.campus.common.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentVO {
    private Long id;
    private Long studentId;
    private String studentName;
    private String studentNo;
    private String className;
    private String semester;
    private String content;
    private Boolean isTeacherEdited;
    private String status;
    private String generatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}