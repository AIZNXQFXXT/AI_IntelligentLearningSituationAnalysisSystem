package com.campus.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ScoreDistribution {
    @JsonProperty("rangeLabel")
    private String rangeLabel;
    @JsonProperty("count")
    private int count;

    public String getRangeLabel() { return rangeLabel; }
    public void setRangeLabel(String rangeLabel) { this.rangeLabel = rangeLabel; }
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
}
