package com.campus.backend.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiRequest {
    private String prompt;
    private String model;
    private Double temperature;
    private Integer maxTokens;
    private Long callerId;
    private String functionName;
    private String promptTemplate;
}