package com.campus.backend.eneity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SysConfig {
    private int id;
    private String configKey;
    private String configValue;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isDeleted;
}
