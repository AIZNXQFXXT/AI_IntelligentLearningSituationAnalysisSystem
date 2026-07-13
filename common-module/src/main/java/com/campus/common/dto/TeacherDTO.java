package com.campus.common.dto;

import com.campus.common.validator.Create;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TeacherDTO {
    private Long id;
    @NotBlank(groups = Create.class)
    private String teacherNo;
    @NotBlank(groups = Create.class)
    private String name;
    private String username;
    private String password;
    private String title;
    private String subject;
    private String education;
    private String department;
    private Integer status;
}