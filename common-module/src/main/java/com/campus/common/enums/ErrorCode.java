package com.campus.common.enums;

import lombok.Getter;

@Getter
public enum ErrorCode {
    SUCCESS(200, "success"),
    BAD_REQUEST(400, "bad request"),
    UNAUTHORIZED(401, "unauthorized"),
    FORBIDDEN(403, "forbidden"),
    NOT_FOUND(404, "not found"),
    CONFLICT(409, "conflict"),
    INTERNAL_ERROR(500, "internal server error"),
    VALIDATION_FAILED(1001, "validation failed"),
    USERNAME_EXISTS(1002, "username already exists"),
    STUDENT_NO_EXISTS(1003, "student number already exists"),
    TEACHER_NO_EXISTS(1004, "teacher number already exists"),
    SCORE_OUT_OF_RANGE(1005, "score must be between 0 and 100"),
    AI_SERVICE_ERROR(2001, "AI service unavailable"),
    TASK_NOT_FOUND(2002, "task not found");

    private final int code;
    private final String msg;

    ErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}