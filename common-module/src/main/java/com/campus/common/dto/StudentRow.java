package com.campus.common.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class StudentRow {
    @ExcelProperty("学号")
    private String studentNo;

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("性别")
    private String gender;

    @ExcelProperty("班级")
    private String className;

    @ExcelProperty("入学年份")
    private String enrollYear;

    @ExcelProperty("电话")
    private String phone;

    @ExcelProperty("监护人电话")
    private String guardianPhone;
}
