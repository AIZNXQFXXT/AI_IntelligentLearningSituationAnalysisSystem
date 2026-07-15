package com.campus.common.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ScoreArchiveVO {
    private Long id;
    private Long studentId;
    private String studentName;
    private String studentNo;
    private Long examId;
    private String examName;
    private Long courseId;
    private String courseName;
    private Long classId;
    private String className;
    private BigDecimal regularScore;
    private BigDecimal examScore;
    private BigDecimal finalScore;
    private Integer rankClass;
    private Integer rankGrade;
    private Integer isAbsent;
    private Integer isCheat;
    private String auditStatus;
    private String semester;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
