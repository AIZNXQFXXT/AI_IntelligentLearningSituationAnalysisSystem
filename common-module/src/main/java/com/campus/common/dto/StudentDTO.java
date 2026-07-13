package com.campus.common.dto;

import com.campus.common.validator.Create;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StudentDTO {
    private Long id;
    @NotBlank(groups = Create.class)
    private String studentNo;
    @NotBlank(groups = Create.class)
    private String name;
    private String gender;
    @NotNull(groups = Create.class)
    private Long classId;
    private String enrollYear;
    private Integer status;
    private String phone;
    private String guardianPhone;
    // 登录账号
    private String username;
    private String password;
}