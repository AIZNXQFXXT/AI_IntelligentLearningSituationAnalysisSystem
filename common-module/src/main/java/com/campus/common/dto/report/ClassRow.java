package com.campus.common.dto.report;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassRow {
    @ExcelProperty("年级")
    private String grade;
    @ExcelProperty("班级名称")
    private String className;
    @ExcelProperty("班主任")
    private String headTeacherName;
    @ExcelProperty("学生人数")
    private Integer studentCount;
}
