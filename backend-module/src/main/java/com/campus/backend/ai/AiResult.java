package com.campus.backend.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiResult {
    private boolean success;
    private String content;
    private int tokensInput;
    private int tokensOutput;
    private int tokensTotal;
    private double estimatedCost;
    private long durationMs;
    private String errorMessage;

    public static AiResult success(String content, int tokensInput, int tokensOutput, long durationMs) {
        double cost = (tokensInput * 0.0000005 + tokensOutput * 0.000002);
        return AiResult.builder()
                .success(true).content(content)
                .tokensInput(tokensInput).tokensOutput(tokensOutput)
                .tokensTotal(tokensInput + tokensOutput)
                .estimatedCost(cost).durationMs(durationMs).build();
    }

    public static AiResult error(String error, long durationMs) {
        return AiResult.builder()
                .success(false).errorMessage(error).durationMs(durationMs).build();
    }
}