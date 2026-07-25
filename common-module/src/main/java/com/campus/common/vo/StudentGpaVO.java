package com.campus.common.vo;

import lombok.Data;

@Data
public class StudentGpaVO {
    private Double gpa;
    private Integer classRank;
    private Integer classTotal;
    private Integer gradeRank;
    private Integer gradeTotal;
}
