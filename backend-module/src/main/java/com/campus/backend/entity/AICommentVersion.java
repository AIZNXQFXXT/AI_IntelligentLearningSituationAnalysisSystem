package com.campus.backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_comment_version")
public class AICommentVersion extends BaseEntity {
    private Long commentId;
    private Integer versionNo;
    private String content;
    private String source;
    private Integer tokensUsed;
}