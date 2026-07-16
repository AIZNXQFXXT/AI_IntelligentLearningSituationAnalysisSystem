package com.campus.common.enums;

import lombok.Getter;

@Getter
public enum ErrorCode {
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或身份验证失败"),
    FORBIDDEN(403, "权限不足"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "数据冲突"),
    INTERNAL_ERROR(500, "服务器内部错误"),
    VALIDATION_FAILED(1001, "数据校验失败"),
    USERNAME_EXISTS(1002, "用户名已存在"),
    STUDENT_NO_EXISTS(1003, "学号已存在"),
    TEACHER_NO_EXISTS(1004, "工号已存在"),
    SCORE_OUT_OF_RANGE(1005, "成绩必须在0~100之间"),
    AI_SERVICE_ERROR(2001, "AI 服务异常，请稍后重试"),
    TASK_NOT_FOUND(2002, "任务不存在"),
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后再试");

    private final int code;
    private final String msg;

    ErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}