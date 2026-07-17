package com.campus.common.dto;

import com.campus.common.validator.Create;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ScoreDTO {
    private Long id;
    @NotNull(groups = Create.class)
    private Long studentId;
    private String studentNo;          // 学号，与 studentId 二选一
    @NotNull(groups = Create.class)
    private Long examId;
    @NotNull(groups = Create.class)
    private Long courseId;
    private String courseName;         // 课程名，与 courseId 二选一
    private BigDecimal regularScore;
    private BigDecimal examScore;
    @NotNull(groups = Create.class)
    @DecimalMin("0") @DecimalMax("100")
    private BigDecimal finalScore;
    private Integer isAbsent;
    private Integer isCheat;
    private String reason;           // 修改时必传
    private String auditStatus;
}