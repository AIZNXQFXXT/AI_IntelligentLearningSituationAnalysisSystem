package com.aicampus.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AiCallLog {
    @JsonProperty("id")
    private int id;
    @JsonProperty("functionName")
    private String functionName;
    @JsonProperty("aiModel")
    private String aiModel;
    @JsonProperty("callerRole")
    private String callerRole;
    @JsonProperty("httpStatus")
    private int httpStatus;
    @JsonProperty("tokensInput")
    private int tokensInput;
    @JsonProperty("tokensOutput")
    private int tokensOutput;
    @JsonProperty("tokensTotal")
    private int tokensTotal;
    @JsonProperty("estimatedCost")
    private double estimatedCost;
    @JsonProperty("durationMs")
    private long durationMs;
    @JsonProperty("success")
    private boolean success;
    @JsonProperty("errorMessage")
    private String errorMessage;
    @JsonProperty("createdAt")
    private String createdAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getFunctionName() { return functionName; }
    public void setFunctionName(String functionName) { this.functionName = functionName; }
    public String getAiModel() { return aiModel; }
    public void setAiModel(String aiModel) { this.aiModel = aiModel; }
    public String getCallerRole() { return callerRole; }
    public void setCallerRole(String callerRole) { this.callerRole = callerRole; }
    public int getHttpStatus() { return httpStatus; }
    public void setHttpStatus(int httpStatus) { this.httpStatus = httpStatus; }
    public int getTokensInput() { return tokensInput; }
    public void setTokensInput(int tokensInput) { this.tokensInput = tokensInput; }
    public int getTokensOutput() { return tokensOutput; }
    public void setTokensOutput(int tokensOutput) { this.tokensOutput = tokensOutput; }
    public int getTokensTotal() { return tokensTotal; }
    public void setTokensTotal(int tokensTotal) { this.tokensTotal = tokensTotal; }
    public double getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(double estimatedCost) { this.estimatedCost = estimatedCost; }
    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
