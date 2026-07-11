package com.campus.backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("operation_log")
public class OperationLog extends BaseEntity {
    private String username;
    private Long operatorId;
    private String operation;
    private String targetType;
    private Long targetId;
    private String detail;
    private String oldData;
    private String newData;
    private String ip;
    private String userAgent;
    private Long durationMs;
    private String resultStatus;
    private String failReason;
}