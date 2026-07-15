package com.campus.common.dto;

import com.campus.common.validator.Create;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    @NotBlank(groups = Create.class)
    private String username;
    @NotBlank(groups = Create.class)
    private String password;
    @NotBlank(groups = Create.class)
    private String role;
    private Integer status;
}
