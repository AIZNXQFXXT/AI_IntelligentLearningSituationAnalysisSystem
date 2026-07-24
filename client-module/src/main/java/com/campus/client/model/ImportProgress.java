package com.campus.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ImportProgress {
    @JsonProperty("status")
    private String status;
    @JsonProperty("progress")
    private int progress;
    @JsonProperty("currentCount")
    private int currentCount;
    @JsonProperty("totalCount")
    private int totalCount;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
    public int getCurrentCount() { return currentCount; }
    public void setCurrentCount(int currentCount) { this.currentCount = currentCount; }
    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
}
