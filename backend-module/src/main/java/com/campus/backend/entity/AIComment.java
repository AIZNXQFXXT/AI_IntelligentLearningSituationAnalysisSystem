package com.campus.backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_comment")
public class AIComment extends BaseEntity {
    private Long studentId;
    private Long teacherId;
    private String semester;
    private String content;
    private Integer isTeacherEdited;
    private String status;
    private String generatedBy;
    private Integer tokensUsed;
}