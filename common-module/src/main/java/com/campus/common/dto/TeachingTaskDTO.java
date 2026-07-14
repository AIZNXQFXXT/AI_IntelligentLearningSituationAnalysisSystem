package com.campus.common.dto;

import com.campus.common.validator.Create;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TeachingTaskDTO {
    @NotNull(groups = Create.class)
    private Long id;

    @NotNull(groups = Create.class)
    private Long teacherId;

    @NotNull(groups = Create.class)
    private Long classId;

    @NotNull(groups = Create.class)
    private Long courseId;

    @NotBlank(groups = Create.class)
    private String semester;
}
