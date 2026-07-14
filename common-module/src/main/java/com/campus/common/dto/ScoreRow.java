package com.campus.common.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ScoreRow {
    @ExcelProperty("学号")
    private String studentNo;

    @ExcelProperty("平时分")
    private BigDecimal regularScore;

    @ExcelProperty("卷面分")
    private BigDecimal examScore;

    @ExcelProperty("最终分")
    private BigDecimal finalScore;

    @ExcelProperty("缺勤")
    private Integer isAbsent;

    @ExcelProperty("作弊")
    private Integer isCheat;
}
