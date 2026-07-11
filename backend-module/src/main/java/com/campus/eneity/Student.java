package com.campus.eneity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Student {
    private int id;
    private int userId;
    private String studentNo;
    private String name;
    private String gender;
    private int classId;
    private String enrollYear;
    private int status;
    private String phone;
    private String guardianPhone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isDeleted;
}