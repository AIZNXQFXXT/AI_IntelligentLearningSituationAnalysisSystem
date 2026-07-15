package com.campus.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SysConfigDTO {
    @NotBlank private String configKey;
    @NotBlank private String configValue;
    private String description;
}
