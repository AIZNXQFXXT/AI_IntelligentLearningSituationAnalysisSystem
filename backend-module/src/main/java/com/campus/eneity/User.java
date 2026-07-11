package com.campus.eneity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private int id;
    private String userName;
    private String password;
    private String role;
    private String avatar;
    private String phone;
    private int status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isDeleted;
}