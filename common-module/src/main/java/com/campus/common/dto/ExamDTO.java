package com.campus.common.dto;

import com.campus.common.validator.Create;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ExamDTO {
    @NotNull(groups = Create.class)
    private Long id;
    @NotBlank(groups = Create.class)
    private String name;
    @NotBlank(groups = Create.class)
    private String type;
    @NotBlank(groups = Create.class)
    private String semester;
    @NotNull(groups = Create.class)
    private Long classId;
    private String className;          // 班级名，与 classId 二选一
    private String grade;              // 配合 className 定位班级
    private LocalDate examDate;
    private Integer isArchived;
}
