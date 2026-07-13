package com.campus.backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("score")
public class Score extends BaseEntity {
    private Long studentId;
    private Long examId;
    private Long courseId;
    private BigDecimal regularScore;
    private BigDecimal examScore;
    private BigDecimal finalScore;
    private Integer rankClass;
    private Integer rankGrade;
    private Integer isAbsent;
    private Integer isCheat;
    private String auditStatus;    // DRAFT / SUBMITTED / ARCHIVED
    private Long enteredBy;
    private String reason;
}