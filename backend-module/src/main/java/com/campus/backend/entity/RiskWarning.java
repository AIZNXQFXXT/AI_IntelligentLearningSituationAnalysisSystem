package com.campus.backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("risk_warning")
public class RiskWarning extends BaseEntity {
    private Long studentId;
    private String semester;
    private String riskLevel;
    private String riskReason;
    private String aiAnalysis;
    private String handleStatus;
    private Long handlerId;
    private String handleRemark;
    private LocalDateTime handleAt;
}