package com.campus.common.dto.report;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskRow {
    @ExcelProperty("学号")
    private String studentNo;
    @ExcelProperty("姓名")
    private String studentName;
    @ExcelProperty("班级")
    private String className;
    @ExcelProperty("学期")
    private String semester;
    @ExcelProperty("风险等级")
    private String riskLevel;
    @ExcelProperty("风险原因")
    private String riskReason;
    @ExcelProperty("处理状态")
    private String handleStatus;
    @ExcelProperty("处理人")
    private String handlerName;
    @ExcelProperty("处理时间")
    private String handleAt;
}
