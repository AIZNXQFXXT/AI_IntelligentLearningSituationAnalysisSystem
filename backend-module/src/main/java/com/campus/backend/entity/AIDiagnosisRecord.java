package com.campus.backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_diagnosis_record")
public class AIDiagnosisRecord extends BaseEntity {
    private Long studentId;
    private String semester;
    private String diagnosisText;
    private String strengths;
    private String weaknesses;
    private String trendAnalysis;
    private String riskLevel;
    private Integer tokensUsed;
    private BigDecimal cost;
    private Integer durationMs;
    private String aiModel;
    private String promptTemplate;
}