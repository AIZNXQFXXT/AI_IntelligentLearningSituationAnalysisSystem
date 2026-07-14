package com.campus.common.dto;

import com.campus.common.validator.Create;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CourseDTO {
    @NotNull(groups = Create.class)
    private Long id;
    @NotNull(groups = Create.class)
    private String name;
    @NotNull(groups = Create.class)
    private String type;
    @NotNull(groups = Create.class)
    private Double credit;
    private String description;
    private Integer status;
}
