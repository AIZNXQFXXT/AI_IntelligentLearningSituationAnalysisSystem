package com.campus.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AICommentDTO {
    @NotNull(message = "学生ID不能为空")
    private Long studentId;

    @NotBlank(message = "学期不能为空")
    private String semester;

    private String content;
}