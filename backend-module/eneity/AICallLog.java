@Data
public class AICallLog {
    private int id;
    private int callerId;
    private String callerRole;
    private String functionName;
    private String aiModel;
    private String requestBody;
    private String responseBody;
    private int httpStatus;
    private int tokensInput;
    private int tokensOutput;
    private int tokensTotal;
    private double estimatedCost;
    private int durationMs;
    private int success;
    private String errorMessage;
    private String promptTemplate;
    private LocalDateTime createdAt;
    private boolean isDeleted;
}
