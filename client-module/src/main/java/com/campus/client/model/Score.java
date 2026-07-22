package com.campus.client.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Score {
    @JsonProperty("id")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int id;
    @JsonProperty("studentId")
    private int studentId;
    @JsonProperty("examId")
    private int examId;
    @JsonProperty("courseId")
    private int courseId;
    @JsonProperty("regularScore")
    private double regularScore;
    @JsonProperty("examScore")
    private double examScore;
    @JsonProperty("finalScore")
    private double finalScore;
    @JsonProperty("rankClass")
    private int rankClass;
    @JsonProperty("rankGrade")
    private int rankGrade;
    @JsonProperty("auditStatus")
    private String auditStatus;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public int getExamId() { return examId; }
    public void setExamId(int examId) { this.examId = examId; }
    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }
    public double getRegularScore() { return regularScore; }
    public void setRegularScore(double regularScore) { this.regularScore = regularScore; }
    public double getExamScore() { return examScore; }
    public void setExamScore(double examScore) { this.examScore = examScore; }
    public double getFinalScore() { return finalScore; }
    public void setFinalScore(double finalScore) { this.finalScore = finalScore; }
    public int getRankClass() { return rankClass; }
    public void setRankClass(int rankClass) { this.rankClass = rankClass; }
    public int getRankGrade() { return rankGrade; }
    public void setRankGrade(int rankGrade) { this.rankGrade = rankGrade; }
    public String getAuditStatus() { return auditStatus; }
    public void setAuditStatus(String auditStatus) { this.auditStatus = auditStatus; }
}
