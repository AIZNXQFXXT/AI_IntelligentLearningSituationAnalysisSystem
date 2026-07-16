package com.campus.common.dto.report;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoreTableRow {
    @ExcelProperty("学号")
    private String studentNo;
    @ExcelProperty("姓名")
    private String studentName;
    @ExcelProperty("班级")
    private String className;
    @ExcelProperty("课程")
    private String courseName;
    @ExcelProperty("考试")
    private String examName;
    @ExcelProperty("平时成绩")
    private BigDecimal regularScore;
    @ExcelProperty("期末成绩")
    private BigDecimal examScore;
    @ExcelProperty("总评")
    private BigDecimal finalScore;
    @ExcelProperty("班级排名")
    private Integer rankClass;
    @ExcelProperty("缺考")
    private String isAbsent;
    @ExcelProperty("作弊")
    private String isCheat;
    @ExcelProperty("审核状态")
    private String auditStatus;
    @ExcelProperty("学期")
    private String semester;
}
