package com.campus.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class DiagnosisReport {
    @JsonProperty("overall")
    private String overall;
    @JsonProperty("strengths")
    private List<Map<String, String>> strengths;
    @JsonProperty("weaknesses")
    private List<Map<String, String>> weaknesses;
    @JsonProperty("trend")
    private String trend;
    @JsonProperty("suggestions")
    private List<String> suggestions;
    @JsonProperty("riskLevel")
    private String riskLevel;

    public String getOverall() { return overall; }
    public void setOverall(String overall) { this.overall = overall; }
    public List<Map<String, String>> getStrengths() { return strengths; }
    public void setStrengths(List<Map<String, String>> strengths) { this.strengths = strengths; }
    public List<Map<String, String>> getWeaknesses() { return weaknesses; }
    public void setWeaknesses(List<Map<String, String>> weaknesses) { this.weaknesses = weaknesses; }
    public String getTrend() { return trend; }
    public void setTrend(String trend) { this.trend = trend; }
    public List<String> getSuggestions() { return suggestions; }
    public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
}
