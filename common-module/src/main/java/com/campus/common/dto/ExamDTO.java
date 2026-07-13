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
    private LocalDate examDate;
    private Integer isArchived;
}
