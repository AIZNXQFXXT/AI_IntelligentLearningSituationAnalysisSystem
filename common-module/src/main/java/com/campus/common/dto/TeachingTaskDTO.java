package com.campus.common.dto;

import com.campus.common.validator.Create;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TeachingTaskDTO {
    @NotNull(groups = Create.class)
    private Long id;

    @NotNull(groups = Create.class)
    private Long teacherId;
    private String teacherNo;          // 教师工号，与 teacherId 二选一

    @NotNull(groups = Create.class)
    private Long classId;
    private String className;          // 班级名，与 classId 二选一

    @NotNull(groups = Create.class)
    private Long courseId;
    private String courseName;         // 课程名，与 courseId 二选一

    @NotBlank(groups = Create.class)
    private String semester;
}
