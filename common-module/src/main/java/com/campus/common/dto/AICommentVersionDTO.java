package com.campus.common.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AICommentVersionDTO {
    private Long id;
    private Long commentId;
    private Integer versionNo;
    private String content;
    private String source;
    private Integer tokensUsed;
    private LocalDateTime createdAt;
}