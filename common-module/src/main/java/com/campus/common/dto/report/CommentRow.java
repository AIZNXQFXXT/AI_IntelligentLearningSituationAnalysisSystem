package com.campus.common.dto.report;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentRow {
    @ExcelProperty("学号")
    private String studentNo;
    @ExcelProperty("姓名")
    private String studentName;
    @ExcelProperty("班级")
    private String className;
    @ExcelProperty("学期")
    private String semester;
    @ExcelProperty("评语内容")
    private String content;
    @ExcelProperty("教师编辑")
    private String isTeacherEdited;
    @ExcelProperty("状态")
    private String status;
}
