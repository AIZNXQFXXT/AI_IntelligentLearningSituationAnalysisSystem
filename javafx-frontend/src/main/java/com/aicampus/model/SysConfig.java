package com.aicampus.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SysConfig {
    @JsonProperty("id")
    private int id;
    @JsonProperty("configKey")
    private String configKey;
    @JsonProperty("configValue")
    private String configValue;
    @JsonProperty("description")
    private String description;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getConfigKey() { return configKey; }
    public void setConfigKey(String configKey) { this.configKey = configKey; }
    public String getConfigValue() { return configValue; }
    public void setConfigValue(String configValue) { this.configValue = configValue; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
