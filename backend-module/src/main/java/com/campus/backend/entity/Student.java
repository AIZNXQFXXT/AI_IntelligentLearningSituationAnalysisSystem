package com.campus.backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("student")
public class Student extends BaseEntity {
    private Long userId;
    private String studentNo;
    private String name;
    private String gender;
    private Long classId;
    private String enrollYear;
    private Integer status;      // 1=在读 0=休学 2=退学
    private String phone;
    private String guardianPhone;
}