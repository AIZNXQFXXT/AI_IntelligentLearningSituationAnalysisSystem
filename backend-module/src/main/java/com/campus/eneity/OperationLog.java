package com.campus.eneity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OperationLog {
    private int id;
    private String username;
    private int operatorId;
    private String operation;
    private String targetType;
    private int targetId;
    private String detail;
    private String oldData;
    private String newData;
    private String ip;
    private String userAgent;
    private int durationMs;
    private String resultStatus;
    private String failReason;
    private LocalDateTime createdAt;
    private boolean isDeleted;
}
