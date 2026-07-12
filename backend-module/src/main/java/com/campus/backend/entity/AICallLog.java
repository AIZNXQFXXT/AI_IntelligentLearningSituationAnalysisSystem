package com.campus.backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_call_log")
public class AICallLog extends BaseEntity {
    private Long callerId;
    private String callerRole;
    private String functionName;
    private String aiModel;
    private String requestBody;
    private String responseBody;
    private Integer httpStatus;
    private Integer tokensInput;
    private Integer tokensOutput;
    private Integer tokensTotal;
    private BigDecimal estimatedCost;
    private Integer durationMs;
    private Integer success;
    private String errorMessage;
    private String promptTemplate;
}