package com.campus.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ScoreDistribution {
    @JsonProperty("range")
    private String range;
    @JsonProperty("count")
    private int count;

    public String getRange() { return range; }
    public void setRange(String range) { this.range = range; }
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
}
