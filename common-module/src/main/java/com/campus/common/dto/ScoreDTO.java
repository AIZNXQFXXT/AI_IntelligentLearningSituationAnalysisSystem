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
    @NotNull(groups = Create.class)
    private Long examId;
    @NotNull(groups = Create.class)
    private Long courseId;
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