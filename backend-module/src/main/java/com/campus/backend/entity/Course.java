package com.campus.backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course")
public class Course extends BaseEntity {
    private String name;
    private String type;         // ELECTIVE / REQUIRED / MAJOR
    private Double credit;
    private String description;
    private Integer status;      // 1=启用 0=停用
}