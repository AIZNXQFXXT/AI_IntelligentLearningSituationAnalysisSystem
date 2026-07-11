package com.campus.eneity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ScoreCorrection {
    private int id;
    private int scoreId;
    private double oldFinalScore;
    private double newFinalScore;
    private String reason;
    private int operatorId;
    private LocalDateTime operatedAt;
    private LocalDateTime createdAt;
    private boolean isDeleted;
}
