package com.campus.common.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class TeacherRow {
    @ExcelProperty("工号")
    private String teacherNo;

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("职称")
    private String title;

    @ExcelProperty("学科")
    private String subject;

    @ExcelProperty("学历")
    private String education;

    @ExcelProperty("院系")
    private String department;
}
