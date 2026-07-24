package com.campus.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class TaskResult {
    @JsonProperty("success")
    private int success;
    @JsonProperty("failed")
    private int failed;
    @JsonProperty("errors")
    private List<Map<String, Object>> errors;

    public int getSuccess() { return success; }
    public void setSuccess(int success) { this.success = success; }
    public int getFailed() { return failed; }
    public void setFailed(int failed) { this.failed = failed; }
    public List<Map<String, Object>> getErrors() { return errors; }
    public void setErrors(List<Map<String, Object>> errors) { this.errors = errors; }
}
