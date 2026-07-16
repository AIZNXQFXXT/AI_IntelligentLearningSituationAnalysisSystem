package com.campus.backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_suggestion")
public class AISuggestion extends BaseEntity {
    private Long studentId;
    private String semester;
    private Long diagnosisId;
    private String content;
    private String shortTerm;
    private String longTerm;
    private String dailyPlan;
    private String resources;
    private Integer tokensUsed;
}
