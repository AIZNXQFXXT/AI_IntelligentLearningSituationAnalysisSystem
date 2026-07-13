package com.campus.backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@TableName("teaching_task")
public class TeachingTask extends BaseEntity {
    private Long teacherId;
    private Long classId;
    private Long courseId;
    private String semester;
}