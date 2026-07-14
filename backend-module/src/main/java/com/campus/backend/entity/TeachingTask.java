package com.campus.backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("teaching_task")
public class TeachingTask extends BaseEntity {
    private Long teacherId;
    private Long classId;
    private Long courseId;
    private String semester;
}