package com.campus.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StudentGpa {
    @JsonProperty("gpa")
    private Double gpa;
    @JsonProperty("classRank")
    private Integer classRank;
    @JsonProperty("classTotal")
    private Integer classTotal;
    @JsonProperty("gradeRank")
    private Integer gradeRank;
    @JsonProperty("gradeTotal")
    private Integer gradeTotal;

    public Double getGpa() { return gpa; }
    public void setGpa(Double gpa) { this.gpa = gpa; }
    public Integer getClassRank() { return classRank; }
    public void setClassRank(Integer classRank) { this.classRank = classRank; }
    public Integer getClassTotal() { return classTotal; }
    public void setClassTotal(Integer classTotal) { this.classTotal = classTotal; }
    public Integer getGradeRank() { return gradeRank; }
    public void setGradeRank(Integer gradeRank) { this.gradeRank = gradeRank; }
    public Integer getGradeTotal() { return gradeTotal; }
    public void setGradeTotal(Integer gradeTotal) { this.gradeTotal = gradeTotal; }
}
