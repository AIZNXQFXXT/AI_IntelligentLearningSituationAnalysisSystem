package com.campus.backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam")
public class Exam extends BaseEntity {
    private String name;
    private String type;         // MOCK / MIDTERM / FINAL / RETEST
    private String semester;
    private Long classId;
    private LocalDate examDate;
    private Integer isArchived;
}