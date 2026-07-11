package com.campus.eneity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SysDict {
    private int id;
    private String typeCode;
    private String itemCode;
    private String itemValue;
    private int sortOrder;
    private int status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isDeleted;
}
