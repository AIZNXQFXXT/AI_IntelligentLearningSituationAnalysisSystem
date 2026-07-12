package com.campus.backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("score_correction")
public class ScoreCorrection extends BaseEntity {
    private Long scoreId;
    private BigDecimal oldFinalScore;
    private BigDecimal newFinalScore;
    private String reason;
    private Long operatorId;
    private LocalDateTime operatedAt;
}