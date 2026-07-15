package com.campus.common.dto;

import com.campus.common.validator.Create;
import com.campus.common.validator.Update;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SysDictDTO {
    @NotNull(groups = Update.class) private Long id;
    @NotBlank(groups = Create.class) private String typeCode;
    @NotBlank(groups = Create.class) private String itemCode;
    @NotBlank private String itemValue;
    private Integer sortOrder;
    private Integer status;
}
