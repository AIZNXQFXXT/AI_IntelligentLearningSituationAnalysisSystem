package com.campus.client.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class AiDiagnosis {
    @JsonProperty("id")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int id;
    @JsonProperty("studentId")
    private int studentId;
    @JsonProperty("diagnosisText")
    private String diagnosisText;
    @JsonProperty("strengths")
    private List<Map<String, String>> strengths;
    @JsonProperty("weaknesses")
    private List<Map<String, String>> weaknesses;
    @JsonProperty("trend")
    private String trend;
    @JsonProperty("semester")
    private String semester;
    @JsonProperty("riskLevel")
    private String riskLevel;
    @JsonProperty("aiModel")
    private String aiModel;
    @JsonProperty("tokensUsed")
    private int tokensUsed;
    @JsonProperty("createdAt")
    private String createdAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public String getDiagnosisText() { return diagnosisText; }
    public void setDiagnosisText(String diagnosisText) { this.diagnosisText = diagnosisText; }
    public List<Map<String, String>> getStrengths() { return strengths; }
    public void setStrengths(List<Map<String, String>> strengths) { this.strengths = strengths; }
    public List<Map<String, String>> getWeaknesses() { return weaknesses; }
    public void setWeaknesses(List<Map<String, String>> weaknesses) { this.weaknesses = weaknesses; }
    public String getTrend() { return trend; }
    public void setTrend(String trend) { this.trend = trend; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getAiModel() { return aiModel; }
    public void setAiModel(String aiModel) { this.aiModel = aiModel; }
    public int getTokensUsed() { return tokensUsed; }
    public void setTokensUsed(int tokensUsed) { this.tokensUsed = tokensUsed; }
}
