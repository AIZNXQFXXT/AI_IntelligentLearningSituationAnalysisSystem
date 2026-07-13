package com.campus.backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("teacher")
public class Teacher extends BaseEntity {
    private Long userId;
    private String teacherNo;
    private String name;
    private String title;
    private String subject;
    private String education;
    private String department;
    private Integer status;
}