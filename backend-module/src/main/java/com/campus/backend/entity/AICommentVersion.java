package com.campus.backend.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AICommentVersion {
    private int id;
    private int commentId;
    private int versionNo;
    private String content;
    private String source;
    private int tokensUsed;
    private LocalDateTime createdAt;
    private boolean isDeleted;
}
