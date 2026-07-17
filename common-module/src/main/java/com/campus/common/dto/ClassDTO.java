package com.campus.common.dto;

import com.campus.common.validator.Create;
import com.campus.common.validator.Update;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClassDTO {
    @NotNull(groups = Create.class)
    private Long id;

    @NotBlank(groups = Create.class)
    private String grade;

    @NotBlank(groups = Create.class)
    private String className;

    private Long headTeacherId;
    private String headTeacherNo;      // 班主任工号，与 headTeacherId 二选一
}